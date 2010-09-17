package fr.inria.zvtm.cluster;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.fits.FitsHistogram;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.ZScale;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * A clustered viewer for FITS images.
 */
public class AstroRad {
    private VirtualSpace imageSpace;
    private VirtualSpace controlSpace;
    private Camera imageCamera; 
    private Camera controlCamera; 
    private final List<FitsImage> images = new ArrayList<FitsImage>();
    private RangeSelection rangeSel;
    private FitsImage selectedImage;
    private FitsHistogram hist;
    private WallCursor ctrlCursor;
    private WallCursor imgCursor;
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    private AROptions options;

    private boolean dragLeft = false; //XXX change
    private boolean dragRight = false; //XXX change
    private View masterView;

    private ComboBox combo;

    //add web interface? (embedded server, 'addImage' operation...?)

    //todo:
    // - image thumbnail (tile)
    // - xfer function chooser
    // - color map chooser

    private AstroRad(URL imgUrl, AROptions options){
        this.options = options;
        setup();
        addImage(imgUrl);
    }

    private void setup(){
        vsm.setMaster("AstroRad");
        imageSpace = vsm.addVirtualSpace("imageSpace");
        controlSpace = vsm.addVirtualSpace("controlSpace");
        imageCamera = imageSpace.addCamera();
        controlCamera = controlSpace.addCamera();
        ArrayList<Camera> imgCamList = new ArrayList<Camera>();
        imgCamList.add(imageCamera);
        ArrayList<Camera> controlCamList = new ArrayList<Camera>();
        controlCamList.add(controlCamera);
        Vector<Camera> cameras = new Vector<Camera>();
        cameras.add(imageCamera);
        cameras.add(controlCamera);
        masterView = vsm.addFrameView(cameras, "Master View",
                View.STD_VIEW, 800, 600, false, true, true, null);	
        masterView.setListener(new PanZoomEventHandler());
        masterView.getCursor().setColor(Color.GREEN);

        ClusterGeometry clGeom;
        ClusteredView imageView;
        ClusteredView controlView;
        if(options.debugView){
            //debugging clustered view, suitable for a single host
            clGeom = new ClusterGeometry(
                600,
                400,
                2, //columns
                1);
            imageView = new ClusteredView(clGeom,0,1,1,imgCamList);
            controlView = new ClusteredView(clGeom,1,1,1,controlCamList);
        } else {
            //WILD view
            clGeom = new ClusterGeometry(
                    2840,
                    1800,
                    8,
                    4);
            imageView = new ClusteredView(clGeom, 3, 6, 4, imgCamList);
            controlView = new ClusteredView(clGeom, 27, 2, 4, controlCamList);
        }
        assert(clGeom != null);
        assert(imageView != null);
        assert(controlView != null);
        vsm.addClusteredView(imageView);
        vsm.addClusteredView(controlView);

        ctrlCursor = new WallCursor(controlSpace);

        setupControlZone();
    }

    private void setupControlZone(){
        rangeSel = new RangeSelection();
        controlSpace.addGlyph(rangeSel);

        combo = new ComboBox(controlSpace, 0, -50, 
                new String[]{"nop", "heat", "rainbow"}, 
                new Color[]{Color.GRAY, Color.ORANGE, Color.PINK},
                60
                );

        //rangeSel.reSize(10);
        //rangeSel.move(-1000, 1000);
    }

    private void addImage(URL imgUrl, double x, double y){
        try{
            FitsImage image = new FitsImage(0,0,0,imgUrl);
            images.add(image);
            imageSpace.addGlyph(image);
            imageSpace.onTop(image);
            double[] scaleBounds = ZScale.computeScale(image.getUnderlyingImage());
            if(scaleBounds != null){
                image.rescale(scaleBounds[0], scaleBounds[1], 1.);
            }
            imageFocusChanged(image, scaleBounds[0], scaleBounds[1]);
        } catch(Exception ex){
            System.err.println(ex);
        }
    }

    private void addImage(URL imgUrl){
        addImage(imgUrl, 0, 0);
    }

    private void imageFocusChanged(FitsImage focused, double lowCut, 
            double highCut){
        if(focused == null){
            return;
        }
        if(hist != null){
            controlSpace.removeGlyph(hist);
        }
        hist = FitsHistogram.fromFitsImage(focused);
        double histWidth = hist.getBounds()[2] - hist.getBounds()[0];
        double rsWidth = rangeSel.getBounds()[2] - rangeSel.getBounds()[0];
        System.err.println("histWidth: " + histWidth);
        System.err.println("hist size: " + hist.getSize());
        System.err.println("rsWidth: " + rsWidth);
        System.err.println("rs size: " + rangeSel.getSize());
        controlSpace.addGlyph(hist);
        System.err.println("new hist size: " + rsWidth * hist.getSize()/histWidth);
        hist.sizeTo(rsWidth * hist.getSize()/histWidth * 0.9);
        System.err.println("new hist size(control): " + hist.getSize());
        System.err.println("new hist width: " + (hist.getBounds()[2] - hist.getBounds()[0]));
        hist.move(-rsWidth/2, 30);

        //draw bounding boxes around histogram and range selection?

        double min = focused.getUnderlyingImage().getHistogram().getMin();
        double max = focused.getUnderlyingImage().getHistogram().getMax();
        rangeSel.setTicksVal((lowCut-min)/(max-min), (highCut-min)/(max-min));

        selectedImage = focused;
    }

    private void removeSelectedImage(){
        if(selectedImage == null){
            return;
        }
        images.remove(selectedImage);
        imageSpace.removeGlyph(selectedImage);
        selectedImage = null;
    }

    public static void main(String[] args) throws Exception{
        AROptions options = new AROptions();
        CmdLineParser parser = new CmdLineParser(options);
        try{
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }

        if(options.arguments.size() < 1){
            System.err.println("Usage: AstroRad [options] image_URL");
            System.exit(0);
        }

        new AstroRad(new URL(args[0]), options);
    }

    private Point2D.Double viewToSpace(Camera cam, int jpx, int jpy){
        Location camLoc = cam.getLocation();
        double focal = cam.getFocal();
        double altCoef = (focal + camLoc.alt) / focal;
        Dimension viewSize = masterView.getPanelSize();

        //find coords of view origin in the virtual space
        double viewOrigX = camLoc.vx - 0.5*viewSize.width*altCoef;
        double viewOrigY = camLoc.vy + 0.5*viewSize.height*altCoef;

        return new Point2D.Double(
                viewOrigX + altCoef*jpx,
                viewOrigY - altCoef*jpy);
    }

    private class PanZoomEventHandler implements ViewListener{
        private int lastJPX;
        private int lastJPY;

        public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            Point2D.Double cursorPos = viewToSpace(vsm.getActiveCamera(), jpx, jpy);
            if(rangeSel.overLeftTick(cursorPos.x, cursorPos.y)){
                dragLeft = true;
            } else if(rangeSel.overRightTick(cursorPos.x, cursorPos.y)){
                dragRight = true;
            }
        }

        public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            dragLeft = false;
            dragRight = false;
            double min = selectedImage.getUnderlyingImage().getHistogram().getMin();
            double max = selectedImage.getUnderlyingImage().getHistogram().getMax();
            selectedImage.rescale(min + rangeSel.getLeftValue()*(max - min),
                    min + rangeSel.getRightValue()*(max - min),
                    1);
        }

        public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
            Point2D.Double spcCoords = viewToSpace(vsm.getActiveCamera(), jpx, jpy);
            combo.onClick1(spcCoords.x, spcCoords.y);
        }

        public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

        public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            lastJPX=jpx;
            lastJPY=jpy;
            v.setDrawDrag(true);
            vsm.getActiveView().mouse.setSensitivity(false);
            //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
        }

        public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            vsm.getAnimationManager().setXspeed(0);
            vsm.getAnimationManager().setYspeed(0);
            vsm.getAnimationManager().setZspeed(0);
            v.setDrawDrag(false);
            vsm.getActiveView().mouse.setSensitivity(true);
        }

        public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

        public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

        public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
            if(buttonNumber == 1){
                if(dragLeft) {
                    rangeSel.setLeftTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
                } else if(dragRight){
                    rangeSel.setRightTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
                }
            }

            if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
                Camera c=vsm.getActiveCamera();
                double a=(c.focal+Math.abs(c.altitude))/c.focal;
                if (mod == META_SHIFT_MOD) {
                    vsm.getAnimationManager().setXspeed(0);
                    vsm.getAnimationManager().setYspeed(0);
                    vsm.getAnimationManager().setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));

                }
                else {
                    vsm.getAnimationManager().setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/4.0) : (jpx-lastJPX)/(a*4));
                    vsm.getAnimationManager().setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));
                }
            }
        }

        public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

        public void enterGlyph(Glyph g){
        }

        public void exitGlyph(Glyph g){
        }

        public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

        public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        }

        public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

        public void viewActivated(View v){}

        public void viewDeactivated(View v){}

        public void viewIconified(View v){}

        public void viewDeiconified(View v){}

        public void viewClosing(View v){
            vsm.stop();
            System.exit(0);
        }
    }
}

class AROptions {
        @Option(name = "-d", aliases = {"--debug-view"}, usage = "debug view") 
        boolean debugView = false;

        // receives other command line parameters than options
        @Argument
        List<String> arguments = new ArrayList();
}

