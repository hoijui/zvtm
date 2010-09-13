package fr.inria.zvtm.cluster;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.fits.FitsHistogram;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;

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
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;

    //todo:
    // - image thumbnail (tile)
    // - xfer function chooser
    // - color map chooser

    private AstroRad(URL imgUrl){
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
        View view = vsm.addFrameView(cameras, "Master View",
                View.STD_VIEW, 800, 600, false, true, true, null);	
        view.setListener(new PanZoomEventHandler());

        //setup cluster geometry
        ClusterGeometry clGeom = new ClusterGeometry(
                2840,
                1800,
                8,
                4);
        //setup clustered views
        ClusteredView imageView = new ClusteredView(clGeom, 3, 6, 4, imgCamList);
        ClusteredView controlView = new ClusteredView(clGeom, 27, 2, 4, controlCamList);
        vsm.addClusteredView(imageView);
        vsm.addClusteredView(controlView);

        setupControlZone();
    }

    private void setupControlZone(){
        rangeSel = new RangeSelection();
        controlSpace.addGlyph(rangeSel);
        rangeSel.reSize(10);
        rangeSel.move(-1000, 1000);
    }

    private void addImage(URL imgUrl){
        try{
            FitsImage image = new FitsImage(0,0,0,imgUrl);
            images.add(image);
            imageSpace.addGlyph(image);
        } catch(Exception ex){
            System.err.println(ex);
        }
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
        if(args.length < 1){
            System.err.println("Usage: AstroRad image_URL");
            System.exit(0);
        }

        new AstroRad(new URL(args[0]));
    }

    private class PanZoomEventHandler implements ViewListener{
        private int lastJPX;
        private int lastJPY;

        public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

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

        public void viewClosing(View v){System.exit(0);}

    }
}

