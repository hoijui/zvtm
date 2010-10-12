package fr.inria.zvtm.cluster;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import fr.inria.vit.pan.IPhodPan;
import fr.inria.vit.pan.PanEventSource;
import fr.inria.vit.point.ClutchEventType;
import fr.inria.vit.point.MouseLaserPoint;
import fr.inria.vit.point.PointEventSource;
import fr.inria.vit.point.PointListener;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.fits.FitsHistogram;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.filters.*;
import fr.inria.zvtm.fits.ZScale;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * A clustered viewer for FITS images.
 */
public class AstroRad {
    private static final double CTRLVIEW_XOFFSET = 7*2840;
    private static final double IMGVIEW_XOFFSET = 3*2840;
    private static final double VIEW_YOFFSET = 2*1800;

    private static final int UNSEL_IMG_ZINDEX = 0;
    private static final int SEL_IMG_ZINDEX = 1; //selected image z-index
    private static final int IMGCURSOR_ZINDEX = 2;
    private static final int CTRLCURSOR_ZINDEX = 2;

    private VirtualSpace imageSpace;
    private VirtualSpace controlSpace;
    private Camera imageCamera; 
    private Camera controlCamera; 
    private final List<FitsImage> images = new ArrayList<FitsImage>();
    private RangeManager range;
    private SliderManager slider;
    private ComboBox combo;
    private FitsImage selectedImage = null;
    private FitsImage draggedImage = null;
    private FitsHistogram hist;
    private VText wcsCoords;
    private WallCursor ctrlCursor;
    private WallCursor imgCursor;

    private MouseLaserPoint pointSource;

    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    private AROptions options;

    private View masterView;

    //todo:
    // - image thumbnail (tile)
    // - xfer function chooser

    public AstroRad(URL imgUrl, AROptions options){
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
            imageView.setBackgroundColor(Color.BLACK);
            controlView = new ClusteredView(clGeom, 27, 2, 4, controlCamList);
            controlView.setBackgroundColor(new Color(0, 40, 0));
        }
        assert(clGeom != null);
        assert(imageView != null);
        assert(controlView != null);
        vsm.addClusteredView(imageView);
        vsm.addClusteredView(controlView);

        ctrlCursor = new WallCursor(controlSpace);
        ctrlCursor.onTop(CTRLCURSOR_ZINDEX);
        imgCursor = new WallCursor(imageSpace, 20, 160, Color.GREEN);
        imgCursor.onTop(IMGCURSOR_ZINDEX);

        pointSource = new MouseLaserPoint(masterView.getPanel());
        pointSource.addListener(new PointListener(){
            boolean dragging = false;

               public void coordsChanged(double x, double y, boolean relative){
                   assert(!relative);
                   ctrlCursor.moveTo(x-CTRLVIEW_XOFFSET, -y+VIEW_YOFFSET);
                   imgCursor.moveTo(x-IMGVIEW_XOFFSET, -y+VIEW_YOFFSET);

                   Point2D.Double pos = ctrlCursor.getPosition();
                   Point2D.Double imgCurPos = imgCursor.getPosition();
                   if(dragging){
                       range.onDrag(pos.x, pos.y);
                       slider.onDrag(pos.x, pos.y);
                       imgCursorDragged(imgCurPos.x, imgCurPos.y);
                   }
               }
               public void pressed(boolean pressed){
                   //forward click events to RangeSel
                   //forward click events to ComboBox 
                   Point2D.Double pos = ctrlCursor.getPosition();
                   Point2D.Double imgCurPos = imgCursor.getPosition();
                   if(pressed){
                      dragging = true; 
                      range.onPress1(pos.x, pos.y);
                      slider.onPress1(pos.x, pos.y);
                      imgCursorPressed(imgCurPos.x, imgCurPos.y);
                   } else {
                       draggedImage = null;
                       dragging = false;
                       combo.onClick1(pos.x, pos.y);
                       range.onRelease1();
                       slider.onRelease1();
                   }
               }
               public void clutched(ClutchEventType event){}

        });
        pointSource.start();

        if(options.debugView){
            setupControlZone(0, 0, 400, 300);
        } else {
            setupControlZone(0,0, 4000, 5000);
        }
    }

    //@param x
    //@param y
    //@param width approximate width for the control zone 
    //@param height approximate height for the control zone
    private void setupControlZone(double x, double y, double width, double height){
        range = new RangeManager(controlSpace, 0, 500, width);

        combo = new ComboBox(controlSpace, -height/4, -height/5, 
                new String[]{"gray", "heat", "rainbow"}, 
                new LinearGradientPaint[]{NopFilter.getGradientS((float)height/5f), HeatFilter.getGradientS((float)height/5f), RainbowFilter.getGradientS((float)height/5f)},
                height/5
                );
        slider = new SliderManager(controlSpace, 0, -2.*height/5, width);

        range.addObserver(new RangeStateObserver(){
            public void onStateChange(RangeManager source, double low, double high){
                if(selectedImage == null){
                    return;
                }
                double min = selectedImage.getUnderlyingImage().getHistogram().getMin();
                double max = selectedImage.getUnderlyingImage().getHistogram().getMax();
                selectedImage.rescale(min + low*(max - min),
                    min + high*(max - min),
                    1);
            }
        });

        //XXX maybe separate this step from construction to avoid escaping 'this'
        combo.addObserver(new ComboStateObserver(){
            public void onStateChange(ComboBox source, int activeIdx, String label){
                if(selectedImage == null){
                    return;
                }

                if(activeIdx == 0){
                    selectedImage.setColorFilter(FitsImage.ColorFilter.NOP);
                } else if(activeIdx == 1){
                    selectedImage.setColorFilter(FitsImage.ColorFilter.HEAT);
                } else {
                    selectedImage.setColorFilter(FitsImage.ColorFilter.RAINBOW);
                }
            }
        });

        slider.addObserver(new SliderStateObserver(){
            public void onStateChange(SliderManager source, double value){
                if(selectedImage == null){
                    return;
                }

                selectedImage.setTranslucencyValue((float)value);
            }
        });

        wcsCoords = new VText(-width/4, height/3, 0, Color.YELLOW, "unknown coords");
        controlSpace.addGlyph(wcsCoords);
    }

    private void imgCursorPressed(double x, double y){
        int highestZindex = -1;
        for(FitsImage img: images){
            if(AstroUtil.isInside(img, x, y) && (img.getZindex() > highestZindex)){
                draggedImage = img;
            }
        }
        if(draggedImage != null){
            imageSpace.onTop(draggedImage, SEL_IMG_ZINDEX);
            imageFocusChanged(draggedImage);
        }
    }

    private void imgCursorDragged(double x, double y){
        if(draggedImage == null){
            return;
        }
        draggedImage.moveTo(x, y);
    }

    private void addImage(URL imgUrl, double x, double y){
        try{
            FitsImage image = new FitsImage(0,0,0,imgUrl);
            images.add(image);
            imageSpace.addGlyph(image);
            double[] scaleBounds = ZScale.computeScale(image.getUnderlyingImage());
            if(scaleBounds != null){
                image.rescale(scaleBounds[0], scaleBounds[1], 1.);
            }
            imageFocusChanged(image);
        } catch(Exception ex){
            System.err.println(ex);
        }
    }

    //package-accessible (may be invoked by an embedded web server)
    void addImage(URL imgUrl){
        addImage(imgUrl, 0, 0);
    }

    private void imageFocusChanged(FitsImage focused){
        if(focused == null){
            return;
        }
        for(FitsImage img: images){
            img.setDrawBorder(false);
            imageSpace.atBottom(img, UNSEL_IMG_ZINDEX); 
        }
        focused.setBorderColor(Color.PINK); //XXX move to addImage
        focused.setStrokeWidth(3); //XXX move to addImage
        focused.setDrawBorder(true);
        imageSpace.onTop(focused, SEL_IMG_ZINDEX);
        if(hist != null){
            controlSpace.removeGlyph(hist);
        }
        hist = FitsHistogram.fromFitsImage(focused, Color.YELLOW);
        slider.setTickVal(focused.getTranslucencyValue());
       // double histWidth = hist.getBounds()[2] - hist.getBounds()[0];
       // double rsWidth = rangeSel.getBounds()[2] - rangeSel.getBounds()[0];
       // System.err.println("histWidth: " + histWidth);
       // System.err.println("hist size: " + hist.getSize());
       // System.err.println("rsWidth: " + rsWidth);
       // System.err.println("rs size: " + rangeSel.getSize());
        controlSpace.addGlyph(hist);
        hist.sizeTo(3000);
       // System.err.println("new hist size: " + rsWidth * hist.getSize()/histWidth);
       // hist.sizeTo(rsWidth * hist.getSize()/histWidth * 0.9);
       // System.err.println("new hist size(control): " + hist.getSize());
       // System.err.println("new hist width: " + (hist.getBounds()[2] - hist.getBounds()[0]));
        //hist.move(-rsWidth/2, 30);
        hist.move(-1500, 1300);

        //draw bounding boxes around histogram and range selection?

        double min = focused.getUnderlyingImage().getHistogram().getMin();
        double max = focused.getUnderlyingImage().getHistogram().getMax();
        double[] scaleParams = focused.getScaleParams();
        range.setTicksVal((scaleParams[0]-min)/(max-min), (scaleParams[1]-min)/(max-min));

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

        AstroRad ar = new AstroRad(new URL(args[0]), options);
        new AstroServer(ar, 8000);

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
            Point2D.Double spcCoords = viewToSpace(controlCamera, jpx, jpy);
            range.onPress1(spcCoords.x, spcCoords.y);
        }

        public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            range.onRelease1();
        }

        public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
            Point2D.Double spcCoords = viewToSpace(controlCamera, jpx, jpy);
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

        public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
            if(options.debugView && selectedImage != null){
                Point2D.Double spcCoords = viewToSpace(imageCamera, jpx, jpy);
                Point2D.Double coords = selectedImage.pix2wcs(spcCoords.x - (selectedImage.vx - (selectedImage.getWidth()/2)), spcCoords.y - (selectedImage.vy - (selectedImage.getHeight()/2)));
                wcsCoords.setText(coords == null? "unknown coords" : coords.x + ", " + coords.y); 
            }
        }

        public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
            if(buttonNumber == 1){
                Point2D.Double spcCoords = viewToSpace(controlCamera, jpx, jpy);
                range.onDrag(spcCoords.x, spcCoords.y); 
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

