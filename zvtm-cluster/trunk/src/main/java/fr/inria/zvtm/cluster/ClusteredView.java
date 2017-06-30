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
    private ArrayList<Camera>  overlayCameras = null;
    private boolean drawPortalOffScreen = false;

    /**
     * Constructs a new ClusteredView (with identifier 0).
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

    /**
     * Constructs a new ClusteredView with a given identifier. If you need to use several ClusteredView
     * use one identifier for each ClusteredView. If different than 0, this identifier should be
     * specified to the slaves by using the --id option. 
     * @param clGeom cluster geometry
     * @param origin origin (bottom-left) block number
     * @param viewRows number of rows in the view (viewRows <= nbRows)
     * @param viewCols number of columns in the view (viewCols <= nbCols)
     * @param cameras a list of cameras observed by this ClusteredView
     * @param id the identifier of this ClusteredView
     */
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

    /** return the identifier of this clustered view */ 
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
    public int getOrigin() { return origin; }

    public ClusterGeometry getClusterGeometry(){ return clGeom; }

    public int getViewRows() { return viewRows; }

    public int getViewCols() { return viewCols; }

    //vector for compatibility with zvtm views
    Vector<Camera> getCameras(){
        return new Vector(cameras);
    }

    /** Set the overlay cameras for this clustered view (should be called before the clustered
    * view is added to the VirtualSpaceManager) 
    *@param  cams cameras of the overlay
    */
    public void setOverlayCameras(Vector<Camera> cameras){
        this.overlayCameras = new ArrayList<Camera>(cameras);
    }
    void removeOverlayCameras(){
        this.overlayCameras = null;
    }
    Vector<Camera> getOverlayCameras(){
        if (overlayCameras == null) return new Vector<Camera>();
        return new Vector(overlayCameras);
    }

    /**Specify whether the portals are rendered with offscreen buffers for this view,
    * by default thi is not the case (should be called before the clustered
    * view is added to the VirtualSpaceManager) 
    *@param  v if true render the portals using offscreen buffers
    */
    public void setDrawPortalOffScreen(boolean v){
        drawPortalOffScreen = v;
    }
    /**Do the portals are renderered with offscreen buffers? */
    public boolean getDrawPortalOffScreen(){
        return drawPortalOffScreen;
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

    /** Get a camera's coordinates so that it is centered on a given region. Does not actually move the camera. Returns the camera's location.
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
        return (cameras.contains(cam) || (overlayCameras != null && overlayCameras.contains(cam)));
    }

    List<Camera> peekCameras(){
        return Collections.unmodifiableList(cameras);
    }
}
