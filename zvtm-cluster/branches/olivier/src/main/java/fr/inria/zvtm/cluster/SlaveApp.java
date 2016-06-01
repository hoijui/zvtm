/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.OverviewPortal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Generic slave application.
 * Instantiates a View, then hands off control to a SlaveUpdater.
 * The generic slave application display the contents of one block
 * of a clustered view (i.e. one slave should be instantiated for
 * every block of the clustered view).
 * @see ClusteredView
 */
public class SlaveApp {
    private final SlaveOptions options;
    VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; //shortcut
    private View view = null; //local view
    private ClusteredView clusteredView = null;
    private JPanel panel = null;

    static SlaveUpdater updater;

    SlaveApp(SlaveOptions options){
        this.options = options;
        vsm.setDebug(options.debug);
    }

    public static void main(String[] args){
        SlaveOptions options = new SlaveOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try{
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }

        if(options.help){
            System.err.println("Usage: SlaveApp [options] where options are: ");
            parser.printUsage(System.err);
            return;
        }

        SlaveApp app = new SlaveApp(options);
        updater = new SlaveUpdater(options.appName, options.blockNumber);
        updater.setAppDelegate(app);
        updater.startOperation();
    }

    public View getView() {
        return view;
    }

    protected String getViewType(){
        return options.openGl ? View.OPENGL_VIEW : View.STD_VIEW;
    }

    void destroyLocalView(ClusteredView cv){
        //if(!cv.ownsBlock(options.blockNumber)){
        if (!ownsBlock(cv)){
            return;
        }
        if(view != null){
            view.destroyView();
            view = null;
        }
    }

    boolean ownsBlock(ClusteredView cv){
        return (cv.ownsBlock(options.blockNumber) && cv.getId() == options.id);
    }
    void createLocalView(ClusteredView cv){
        if (!ownsBlock(cv)) {
            return;
        }

        if (view != null){
            throw new IllegalStateException("local view already exists");
        }
        clusteredView = cv;
        view = vsm.addFrameView(cv.getCameras(),
                "slaveView " + options.blockNumber +"-"+cv.getId(),
                getViewType(),
                cv.getClusterGeometry().getBlockWidth()*options.width,
                cv.getClusterGeometry().getBlockHeight()*options.height,
                false, false, !options.undecorated, null);
        view.setBackgroundColor(cv.getBackgroundColor());
        view.setDrawPortalsOffScreen(cv.getDrawPortalsOffScreen());
        //view.setListener(new SlaveEventHandler());
        enableEventForwarding(cv.getEventForwarding());
        panel = (JPanel)view.getPanel().getComponent();
        view.getPanel().setRefreshRate(options.refreshPeriod);
        if (options.antialiasing){
            view.setAntialiasing(true);
        }
        System.out.println("Antialiasing " + ((options.antialiasing) ? "enabled" : "disabled"));
        // inputs: block width, block height, fullscreen
        if (options.fullscreen){
            System.out.println("Attempting to go fullscreen on: "+options.device);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();
            if (devices.length == 0){
                throw new Error("No screen devices found");
            }
            Map<String, GraphicsDevice> devMap = new HashMap<String, GraphicsDevice>();
            System.out.print("Available devices: ");
            for(int i=0;i<devices.length;i++){
                devMap.put(devices[i].getIDstring(), devices[i]);
            }
            for(int i=0;i<devices.length-1;i++){
                System.out.print(devices[i].getIDstring()+", ");
            }
            System.out.println(devices[devices.length-1].getIDstring());
            GraphicsDevice device = null;
            if (!options.device.equals("")){
                device = devMap.get(options.device);
                if (device != null){
                    System.out.println("Assigning view to device: " + options.device);
                }
                else {
                    System.out.println("Warning: could not find device: " + options.device);
                }
            }
            if (null == device){
                device = ge.getDefaultScreenDevice();
                System.out.println("Display device not found: falling back to default device "+device);
            }
            System.out.println("Entering fullscreen mode");
            ((JFrame)view.getFrame()).removeNotify();
            ((JFrame)view.getFrame()).setUndecorated(true);
            device.setFullScreenWindow((JFrame)view.getFrame());
            ((JFrame)view.getFrame()).addNotify();
        }
        else {
            ((JFrame)view.getFrame()).setLocation(options.xOffset, options.yOffset);
            view.setVisible(true);
        }
        //
        updater.setSyncPaintImmediately();
        ToMasterStarted d = new ToMasterStarted(options.id, options.blockNumber);
        updater.sendMessage(d);
    }

    SlaveMsgEventHandler smeh = null;
    boolean eventFWD = false;

    void enableEventForwarding(boolean v){
        if (view == null) { return; }
        eventFWD = v;
        if (v){
            smeh = new SlaveMsgEventHandler();
            view.setListener(smeh);
            view.getPanel().getComponent().addComponentListener(smeh);
        }
        else{
            if (smeh != null) {
                view.getPanel().getComponent().removeComponentListener(smeh);
                smeh = null;
            }
            view.setListener(new SlaveEventHandler());
        }
    }

    void paintImmediately(){
        Dimension size = panel.getSize();
        panel.paintImmediately(0, 0, size.width, size.height);
    }

    void setOverlayCamera(Camera c, ClusteredView cv){
        if (clusteredView == null || !ownsBlock(cv)) { return; }
        //!cv.ownsBlock(options.blockNumber)) { return; }
        clusteredView.setOverlayCamera(c);
        VirtualSpaceManager.INSTANCE.setOverlayCamera(c, view);
    }

    void destroyOverlayCamera(ClusteredView cv){
        if (clusteredView == null ||  !ownsBlock(cv)) { return; }
        clusteredView.destroyOverlayCamera();
        VirtualSpaceManager.INSTANCE.destroyOverlayCamera(view);
    }

    void setCameraLocation(Location masterLoc, Camera slaveCamera){
        if(clusteredView == null || (!clusteredView.ownsCamera(slaveCamera))){
            if (slaveCamera != null) {
                slaveCamera.setLocation(masterLoc);
            }
            else{
                System.out.println("SlaveApp[setCameraLocation] WARN slaveCamera null");
            }
            if (view != null){
                view.checkRepaintExt(slaveCamera);
            }
            return;
        }
        double focal = slaveCamera.getFocal();
        double altCoef = (focal + masterLoc.alt) / focal;
        //block (screen) width in virtualspace coords
        double virtBlockWidth = clusteredView.getClusterGeometry().getBlockWidth() * altCoef;
        //block (screen) height in virtualspace coords
        double virtBlockHeight = clusteredView.getClusterGeometry().getBlockHeight() * altCoef;

        //row and col take origin block into account
        int viewRows = clusteredView.getViewRows();
        int viewCols = clusteredView.getViewCols();
        int row = clusteredView.rowNum(options.blockNumber) - clusteredView.rowNum(clusteredView.getOrigin());
        int col = clusteredView.colNum(options.blockNumber) - clusteredView.colNum(clusteredView.getOrigin());

        double xOffset = -((viewCols-1)*virtBlockWidth)/2 + ((options.width-1)*virtBlockWidth/2);
        double yOffset = ((viewRows-1)*virtBlockHeight)/2 + ((options.height-1)*virtBlockHeight/2);

        double newX = xOffset + masterLoc.vx + col*virtBlockWidth;
        double newY = -yOffset + masterLoc.vy - row*virtBlockHeight;

        slaveCamera.setLocation(new Location(newX, newY, masterLoc.alt));
    }

    void setOverviewPortalObservedViewLocationAndSize(OverviewPortal op){
        if(clusteredView == null) return; // FIXME own portal ???
        int virtBlockWidth = clusteredView.getClusterGeometry().getBlockWidth();
        int virtBlockHeight = clusteredView.getClusterGeometry().getBlockHeight();
        int row = clusteredView.rowNum(options.blockNumber) ;
        int col = clusteredView.colNum(options.blockNumber) ;
        
        double dx = (col == 0)? 0: (col-1)*virtBlockWidth + virtBlockWidth/2;
        double dy = (row == 0)? 0: (row-1)*virtBlockHeight + virtBlockHeight/2;
        op.setObservedViewLocationAndSize(dx, dy, 
                clusteredView.getClusterGeometry().getWidth(),
                clusteredView.getClusterGeometry().getHeight());
    }

    void setPortalLocationAndSize(Portal p, int x, int y, int w, int h) {
        int virtBlockWidth = clusteredView.getClusterGeometry().getBlockWidth();
        int virtBlockHeight = clusteredView.getClusterGeometry().getBlockHeight();

        int row = clusteredView.rowNum(options.blockNumber) ;
        int col = clusteredView.colNum(options.blockNumber) ;

        p.moveTo(x-col*virtBlockWidth,y-row*virtBlockHeight);
        p.sizeTo(w,h);
    }

    void setPortalLocation(Portal p, int x, int y) {
        int virtBlockWidth = clusteredView.getClusterGeometry().getBlockWidth();
        int virtBlockHeight = clusteredView.getClusterGeometry().getBlockHeight();

        int row = clusteredView.rowNum(options.blockNumber) ;
        int col = clusteredView.colNum(options.blockNumber) ;

        p.moveTo(x-col*virtBlockWidth,y-row*virtBlockHeight);
    }

    void setPortalSize(Portal p, int w, int h) {
        p.sizeTo(w,h);
    }

    void stop(){
        System.exit(0);
    }

    void setBackgroundColor(ClusteredView cv, Color bgColor){
        //find if cv owns the local view.
        if(!ownsBlock(cv)) {
            return;
        }
        if(view == null){
            return;
        }
        view.setBackgroundColor(bgColor);
    }

    //Simple event handler. All callbacks are no-ops except viewClosing
    //which should ensure graceful application closure.
    protected static class SlaveEventHandler extends ViewAdapter {
        @Override
        public void viewClosing(View v){System.exit(0);}
    }

// -------------------------------------------------------------------------------
// -------------------------------------------------------------------------------
 
    protected class SlaveMsgEventHandler extends ViewAdapter implements ComponentListener
    {
       
        @Override   
        public void press1(ViewPanel v,int mod, int jpx, int jpy, MouseEvent e){
            ToMasterPress d = new ToMasterPress(options.id, options.blockNumber, 1, mod, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            ToMasterRelease d = new ToMasterRelease(options.id, options.blockNumber, 1, mod, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void click1(ViewPanel v,int mod,int jpx,int jpy, int clickNumber, MouseEvent e){
            ToMasterClick d = new ToMasterClick(options.id, options.blockNumber, 1, mod, jpx, jpy, clickNumber);
            updater.sendMessage(d);
        }

        @Override public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            ToMasterPress d = new ToMasterPress(options.id, options.blockNumber, 2, mod, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            ToMasterRelease d = new ToMasterRelease(options.id, options.blockNumber,2, mod, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
            ToMasterClick d = new ToMasterClick(options.id, options.blockNumber, 2, mod, jpx, jpy, clickNumber);
            updater.sendMessage(d);
        }

        @Override public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            ToMasterPress d = new ToMasterPress(options.id, options.blockNumber, 3, mod, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            ToMasterRelease d = new ToMasterRelease(options.id, options.blockNumber,3, mod, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
            ToMasterClick d = new ToMasterClick(options.id, options.blockNumber, 3, mod, jpx, jpy, clickNumber);
            updater.sendMessage(d);
        }

        @Override public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
            ToMasterMouseMoved d = new ToMasterMouseMoved(options.id, options.blockNumber, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
            ToMasterMouseDragged d = new ToMasterMouseDragged(options.id, options.blockNumber, buttonNumber, mod, jpx, jpy);
            updater.sendMessage(d);
        }

        @Override public void mouseWheelMoved(
            ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e) {
             ToMasterMouseWheelMoved d = new ToMasterMouseWheelMoved(options.id, options.blockNumber, wheelDirection, jpx, jpy);
            updater.sendMessage(d);
        }

        //@Override public void enterGlyph(Glyph g){}
        //@Override public void exitGlyph(Glyph g){}

        @Override
        public void Ktype(ViewPanel v, char c,int code, int mod, KeyEvent e){
            ToMasterKtype d = new ToMasterKtype(options.id, options.blockNumber, c, code, mod);
            updater.sendMessage(d);
        }

        @Override public void Kpress(ViewPanel v,char c, int code,int mod, KeyEvent e){
            ToMasterKpress d = new ToMasterKpress(options.id, options.blockNumber, c, code, mod);
            updater.sendMessage(d);
        }

        @Override public void Krelease(ViewPanel v,char c, int code, int mod, KeyEvent e){
            ToMasterKrelease d = new ToMasterKrelease(options.id, options.blockNumber, c, code, mod);
            updater.sendMessage(d);
        }

        // NEED THAT ????
        @Override public void viewActivated(View v){
        }

        @Override public void viewDeactivated(View v){}

        @Override public void viewIconified(View v){}

        @Override public void viewDeiconified(View v){}

        @Override public void viewClosing(View v){
            System.exit(0);
        }

        /*ComponentListener*/
        @Override     public void componentHidden(ComponentEvent e){}
        @Override     public void componentMoved(ComponentEvent e){}
        @Override     public void componentResized(ComponentEvent e){}
        @Override     public void componentShown(ComponentEvent e){}

    } 

}

// -------------------------------------------------------------------------------
// -------------------------------------------------------------------------------

class SlaveOptions {
    @Option(name = "-b", aliases = {"--block"}, usage = "clustered view block number (slave index)")
        int blockNumber = 0;
    
    @Option(name = "-i", aliases = {"--id"}, usage = "slave id")
        int id = 0;
    
    @Option(name = "-n", aliases = {"--app-name"}, usage = "application name (should match master program)")
        String appName = "zvtmApplication";

    @Option(name = "-f", aliases = {"--fullscreen"}, usage = "open in full screen mode")
        boolean fullscreen = false;

    @Option(name = "-a", aliases = {"--antialiasing"}, usage = "enable antialiased rendering")
        boolean antialiasing = false;

    @Option(name = "-o", usage = "enable OpenGL acceleration")
        boolean openGl = false;

    @Option(name = "-d", aliases = {"--device-name"}, usage = "use chosen device (fullscreen only)")
        String device = "";

    @Option(name = "-g", aliases = {"--debug"}, usage = "show ZVTM debug information")
        boolean debug = false;

    @Option(name = "-h", aliases = {"--help"}, usage = "print this help message and exit")
        boolean help = false;

    @Option(name = "-u", aliases = {"--undecorated"}, usage = "remove window decorations (borders)")
        boolean undecorated = false;

    @Option(name = "-x", aliases = {"--x-offset"}, usage = "window x offset (ignored if fullscreen set)")
        int xOffset = 0;

    @Option(name = "-y", aliases = {"--y-offset"}, usage = "window y offset (ignored if fullscreen set)")
        int yOffset = 0;

    @Option(name = "-r", aliases = {"--refresh-period"}, usage = "time between two scene repaints (milliseconds)")
        int refreshPeriod = 25;
    
    @Option(name = "-wb", aliases = {"--w-block"}, usage = "width in block (default 1)")
    	int width = 1;

    @Option(name = "-hb", aliases = {"--h-block"}, usage = "height in block (default 1)")
		int height = 1;
}

