/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.jgroups.Message;
import org.jgroups.Address;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ClusteredView extends DefaultIdentifiable {
    private final ObjId objId = ObjIdFactory.next();
    private final int origin; //bottom-left block number
    private final int viewCols;
    private final int viewRows;
    private final int id;
    private ClusterGeometry clGeom;
    private final ArrayList<Camera> cameras;
    private Color bgColor;
    private Camera overlayCamera = null;
    private boolean drawPortalsOffScreen = false;

    /**
     * Constructs a new ClusteredView.
     * @param clGeom cluster geometry
     * @param origin origin (bottom-left) block number
     * @param viewRows number of rows in the view (viewRows <= nbRows)
     * @param viewCols number of columns in the view (viewCols <= nbCols)
     * @param cameras a list of cameras observed by this ClusteredView.
     */
    public ClusteredView(ClusterGeometry clGeom,
            int origin,
            int viewCols, int viewRows,
            List<Camera> cameras)
    {
        this(clGeom, origin, viewCols, viewRows, cameras, 0);
    }
    public ClusteredView(ClusterGeometry clGeom,
            int origin,
            int viewCols, int viewRows,
            List<Camera> cameras, int id){
        this.clGeom = clGeom;
        this.origin = origin;
        this.viewCols = viewCols;
        this.viewRows = viewRows;
        this.cameras = new ArrayList<Camera>(cameras);
        this.id = id;
        this.bgColor = Color.DARK_GRAY;

        if(origin < 0){
            throw new IllegalArgumentException("Blocks are 0-based naturals");
        }
        if((viewRows <= 0) || (viewCols <= 0)){
            throw new IllegalArgumentException("View row and column counts should be greater than 0");
        }
        if(viewRows > clGeom.getRows()){ //XXX
            throw new IllegalArgumentException("View row(s) outside of cluster");
        }
        if(viewCols + colNum(origin) > clGeom.getColumns()){
            throw new IllegalArgumentException("View column(s) outside of cluster");
        }
    }

    public ObjId getObjId(){ return objId; }

    public int getId(){ return id; }
    
    /**
     * Sets the background color for this ClusteredView.
     * @param color new background color
     */
    public void setBackgroundColor(Color color){
        this.bgColor = color;
    }

    /**
     * Gets the background color.
     * @return the current background color
     */
    public Color getBackgroundColor(){
        return bgColor;
    }

    /**
     * Returns the size of this ClusteredView, in pixels.
     * The width of this view is equal to blockWidth*viewCols
     * The height of this view is equal to blockHeight*viewRows
     */
    public Dimension getSize(){
        return new Dimension(clGeom.getBlockWidth()*viewCols,
                clGeom.getBlockHeight()*viewRows);
    }

    /**
     * @throws IllegalArgumentException If cam does not belong to this
     *                                 ClusteredView
     * @param xPos point x-coordinate, in VirtualSpace coords
     * @param yPos point y-coordinate, in VirtualSpace coords
     * @return the coordinates of a point at (xPos, yPos), in view
     *         coordinates (ie (0,0) top-left, x increases to the
     *         right, y increases downwards)
     */
    public Point spaceToViewCoords(Camera cam, double xPos, double yPos){
        if(!this.ownsCamera(cam)){
            throw new IllegalArgumentException("this view does not own Camera 'cam'");
        }

        Location camLoc = cam.getLocation();

        double focal = cam.getFocal();
        double altCoef = (focal + camLoc.alt) / focal;
        Dimension viewSize = getSize();

        return new Point(viewSize.width/2+(int)Math.round((xPos-camLoc.vx)/altCoef), viewSize.height/2-(int)Math.round((yPos-camLoc.vy)/altCoef));
    }

    /**
     * Converts the coordinates of a point given in ClusteredView
     * coordinates into VirtualSpace coordinates.
     * @throws IllegalArgumentException If cam does not belong to this
     *                                 ClusteredView
     * @param xPos point x-coodinate, in View coords
     * @param yPos point y-coordinate, in View coords
     */
    public Point2D.Double viewToSpaceCoords(Camera cam, int xPos, int yPos){
        if(!this.ownsCamera(cam)){
            throw new IllegalArgumentException("this view does not own Camera 'cam'");
        }

        Location camLoc = cam.getLocation();
        double focal = cam.getFocal();
        double altCoef = (focal + camLoc.alt) / focal;
        Dimension viewSize = getSize();

        //find coords of view origin in the virtual space
        double viewOrigX = camLoc.vx - (0.5*viewSize.width*altCoef);
        double viewOrigY = camLoc.vy + (0.5*viewSize.height*altCoef);

        return new Point2D.Double(
                viewOrigX + (altCoef*xPos),
                viewOrigY - (altCoef*yPos));
    }

    /**
     * Gets the origin (bottom-left) block number for
     * this ClusteredView. Note that blocks are ordered column-wise,
     * and block numbers start at zero.
     */
    int getOrigin() { return origin; }

    ClusterGeometry getClusterGeometry(){ return clGeom; }

    int getViewRows() { return viewRows; }

    int getViewCols() { return viewCols; }

    //vector for compatibility with zvtm views
    Vector<Camera> getCameras(){
        return new Vector(cameras);
    }

    void setOverlayCamera(Camera c){
        overlayCamera = c;
    }
    void destroyOverlayCamera(){
        overlayCamera = null;
    }

    // -----------------------------------------
    // Sync stuff (master!)

    private boolean isSyncronous = false;
    private boolean needLateSyncPaint = false;
    private int num_slaves = 0;
    private long lastPaintTime = 0;
    private long lastMsgTime = 0;
    private HashMap<String,SlaveInfo> slaves = new HashMap<String,SlaveInfo>();
    private Timer stimer;
    private int stimerDelay;
    private ActionListener taskPerformer;

    /**Set slaves synchronous rendering */
    public void setSynchronous(boolean b){
        if(VirtualSpaceManager.INSTANCE.isMaster()){
            System.out.println("master ClusteredView set Synchronous "+b);
            isSyncronous = b;
            VirtualSpaceManager.INSTANCE.sendDelta(new SyncDelta(this, b));
        }
        else {
            // should not happen
            System.out.println("one try to use setSynchronous  with a slave ClusteredView ???? "+b);
        }
    }

    /**Is slaves rendering set synchronous */
    public boolean isSynchronous(){
        return isSyncronous;
    }

    // needed for zuist at least for now...
    public void setLatePaintSync(int delay)
    {   
        if(!VirtualSpaceManager.INSTANCE.isMaster()){
            // should not happen
            System.out.println("one try to use setLatePaintSync with a slave ClusteredView ???? "+delay);
            return;
        }
        taskPerformer = new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                //System.out.println("lateSyncPaint actionPerformed");
                lateSyncPaint();
            }
        };
        stimer = new Timer(delay, taskPerformer);
    }

    public void lateSyncPaint()
    {
        for(SlaveInfo i : slaves.values()){
            //System.err.println("VirtualSpaceManager.lateSyncPaint "+i.paintAsked);
            if (i.paintAsked){
                VirtualSpaceManager.INSTANCE.sendDeltaPI(i.address, new PaintImmediatelyDelta(0));
            }
        }
    }

    void handleSyncHello(Message msg){
        num_slaves++;
        Address add = msg.getSrc(); 
        String s = (msg.getSrc()).toString();
        slaves.put(s,new SlaveInfo(add));
    }

    void handleSyncBye(Message msg){
        String s = (msg.getSrc()).toString();
        num_slaves--;
        slaves.remove(s);
    }

    void  handleSyncPaint(Message msg)
    {    
        if(!VirtualSpaceManager.INSTANCE.isMaster()){
            // should not happen
            System.out.println("handleSyncPaint with a slave ClusteredView ???? "+msg);
            return;
        }
        long ct = System.currentTimeMillis();
        //System.err.println(msg.getSrc()+ ": "+ msg.getObject() + " "+ (ct - lastMsgTime));
        lastMsgTime = ct;
        String m = (msg.getObject()).toString();
        String s = (msg.getSrc()).toString();
        if (m.equals("Hello")){
            num_slaves++;
            Address add = msg.getSrc(); 
            slaves.put(s,new SlaveInfo(add));
        }
        else if (m.equals("Bye")){
            num_slaves--;
            slaves.remove(s);
        }
        else {
            Long L = (Long)msg.getObject();
            long l = L.longValue();
            SlaveInfo si = slaves.get(s);
            long pmt = 0;
            if (si != null){
                pmt = si.atTime;
                si.repaintCount = l;
                si.atTime = ct;
                si.paintAsked = true;
            }
            else{
                System.err.println("ERROR in handleSyncPaint");
                return;
            }
            long maxtest = 0;
            long mintest = -1;
            long count = 0;
            for(SlaveInfo i : slaves.values())
            {
                    if (i.paintAsked) count++;
                    if (maxtest < i.repaintCount){
                        maxtest = i.repaintCount;
                    }
                    if (mintest == -1 || mintest > i.repaintCount){
                        mintest = i.repaintCount;
                    }
            }
            
            if (mintest == maxtest || count == num_slaves)
            {
                if (stimer != null && stimer.isRunning()) { 
                    stimer.stop();
                }
                if (count < num_slaves){
                    //System.out.println("MASTER: LESS PAINT "+this);
                    for(SlaveInfo i : slaves.values()){
                        if (i.paintAsked){
                            VirtualSpaceManager.INSTANCE.sendDeltaPI(i.address, new PaintImmediatelyDelta(maxtest));
                        }
                    }
                }
                else {
                    //System.out.println("MASTER: ALL PAINT "+this);
                    VirtualSpaceManager.INSTANCE.sendDeltaPI(new PaintImmediatelyDelta(maxtest));
                }
                long pp = lastPaintTime;
                lastPaintTime = ct;
                // System.out.println("master paint delay "+ (lastPaintTime - pp) +" "+(ct - pmt)+" with lock: "+ numLock);
                for(SlaveInfo i : slaves.values()){
                    // System.out.print(i.paintCount+" "+(lastPaintTime-i.atTime)+" "+ i.skipped+ " / ");
                    i.skipped = 0;
                    i.paintAsked = false;
                }
                // System.out.println("");
                return;
            }
            if (stimer != null){
                if (stimer.isRunning()){
                    stimer.restart();
                }
                else{
                    stimer.start();
                }
            }
            si.skipped++;
        }
    }

    // --------------------------------------------------------------
    // the slaves listener
    ClusteredViewListener cvListener = null;

    public void setListener(ClusteredViewListener cvl){
        cvListener = cvl;
    }
    public void removeListener(){
        cvListener = null;
    }

    // FIXME...
    int blockXToViewX(int b, int x) { return x; }
    int blockYToViewY(int b, int y) { return y; }

    public void slavePress(int blockNumber, int button, int mod, int jpx, int jpy){
        if (cvListener == null){ return; }
        jpx = blockXToViewX(blockNumber, jpx);
        jpy = blockYToViewY(blockNumber, jpy);
        if (button == 1){
            cvListener.press1(blockNumber, mod, jpx, jpy);
        }
        else if (button == 2){
            cvListener.press2(blockNumber, mod, jpx, jpy);
        }
        else if (button == 3){
            cvListener.press3(blockNumber, mod, jpx, jpy);
        }
    }
    public void slaveClick(int blockNumber, int button, int mod, int jpx, int jpy, int clickNumber){
        if (cvListener == null){ return; }
        jpx = blockXToViewX(blockNumber, jpx);
        jpy = blockYToViewY(blockNumber, jpy);
        if (button == 1){
            cvListener.click1(blockNumber, mod, jpx, jpy, clickNumber);
        }
        else if (button == 2){
            cvListener.click2(blockNumber, mod, jpx, jpy, clickNumber);
        }
        else if (button == 3){
            cvListener.click3(blockNumber, mod, jpx, jpy, clickNumber);
        }
    }
    public void slaveRelease(int blockNumber, int button, int mod, int jpx, int jpy){
        if (cvListener == null){ return; }
        jpx = blockXToViewX(blockNumber, jpx);
        jpy = blockYToViewY(blockNumber, jpy);
        if (button == 1){
            cvListener.release1(blockNumber, mod, jpx, jpy);
        }
        else if (button == 2){
            cvListener.release2(blockNumber, mod, jpx, jpy);
        }
        else if (button == 3){
            cvListener.release3(blockNumber, mod, jpx, jpy);
        }
    }
    public void slaveMouseMoved(int blockNumber, int jpx, int jpy){
        if (cvListener == null){ return; }
        jpx = blockXToViewX(blockNumber, jpx);
        jpy = blockYToViewY(blockNumber, jpy);
        cvListener.mouseMoved(blockNumber, jpx, jpy);
    }
    public void slaveMouseDragged(int blockNumber, int button, int mod, int jpx, int jpy){
        if (cvListener == null){ return; }
        jpx = blockXToViewX(blockNumber, jpx);
        jpy = blockYToViewY(blockNumber, jpy);
        cvListener.mouseDragged(blockNumber, mod, button, jpx, jpy);
    }
    public void slaveMouseWheelMoved(int blockNumber, short wheelDirection, int jpx, int jpy){
        if (cvListener == null){ return; }
        jpx = blockXToViewX(blockNumber, jpx);
        jpy = blockYToViewY(blockNumber, jpy);
        cvListener.mouseWheelMoved(blockNumber, wheelDirection, jpx, jpy);
    }
    public void slaveKtype(int blockNumber, char c, int code, int mod){
        if (cvListener == null){ return; }
        cvListener.Ktype(blockNumber, c, code, mod);
    }
    public void slaveKpress(int blockNumber, char c, int code, int mod){
        if (cvListener == null){ return; }
        cvListener.Kpress(blockNumber, c, code, mod);
    }
    public void slaveKrelease(int blockNumber, char c, int code, int mod){
        if (cvListener == null){ return; }
        cvListener.Krelease(blockNumber, c, code, mod);
    }
    // --------------------------------------------------------------

    /**tell to renderer (or not) the portals with offscreen buffers */
    public void setDrawPortalsOffScreen(boolean v){
        drawPortalsOffScreen = v;
    }
    /**do the portals are renderered with offscreen buffers? */
    public boolean getDrawPortalsOffScreen(){
        return drawPortalsOffScreen;
    }

    /**
     * Tests whether the block 'blockNumber' belongs
     * to this ClusteredView.
     */
    boolean ownsBlock(int blockNumber){
        int col = colNum(blockNumber);
        int row = rowNum(blockNumber);
        int origCol = colNum(origin);
        int origRow = rowNum(origin);
        return
            (row <= origRow) &&
            (col >= origCol) &&
            (row > (origRow - viewRows)) &&
            (col < (origCol + viewCols));
    }

    //returns the column number associated with a block number.
    //blocks are ordered column-wise
    //column 0 is the leftmost column
    //XXX move to ClusterGeometry?
    int colNum(int blockNum){
        return blockNum / clGeom.getRows();
    }

    //returns the row number associated with a block number
    //blocks are ordered column-wise
    //row 0 is the topmost one
    //XXX move to ClusterGeometry?
    int rowNum(int blockNum){
        return blockNum % clGeom.getRows();
    }

    /**
     * @param x1 first point x-coordinate, in VirtualSpace coords
     * @param y1 first point y-coordinate, in VirtualSpace coords
     * @param x2 second point x-coordinate, in VirtualSpace coords
     * @param y2 second point y-coordinate, in VirtualSpace coords
     */
    public Location centerOnRegion(Camera cam, double x1, double y1, double x2, double y2){
       if(!this.ownsCamera(cam)){
           throw new IllegalArgumentException("this view does not own Camera 'cam'");
       }

       double west = Math.min(x1,x2);
       double north = Math.max(y1,y2);
       double east = Math.max(x1,x2);
       double south = Math.min(y1,y2);

       double newX = (west + east) / 2;
       double newY = (north + south) / 2;

       Dimension viewSize = getSize();
       //new altitude to fit horizontally
       double nah = (east-newX)*2*cam.getFocal() / viewSize.width - cam.getFocal();
       //new altitude to fit vertically
       double nav = (north-newY)*2*cam.getFocal()/ viewSize.height - cam.getFocal();
       double newAlt = Math.max(nah, nav);

       return new Location(newX, newY, newAlt);
    }

   // public Location centerOnGlyph(Camera cam, Glyph glyph){
   // }

    boolean ownsCamera(Camera cam){
        return (cameras.contains(cam) || (overlayCamera != null && cam == overlayCamera));
    }

    List<Camera> peekCameras(){
        return Collections.unmodifiableList(cameras);
    }
}

