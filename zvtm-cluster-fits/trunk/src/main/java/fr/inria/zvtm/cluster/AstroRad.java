package fr.inria.zvtm.cluster;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.fits.FitsHistogram;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.glyphs.FitsImage;

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

    //todo:
    // - image thumbnail (tile)
    // - xfer function chooser
    // - color map chooser

    private AstroRad(URL imgUrl){
        setup();
        addImage(imgUrl);
    }

    private void setup(){
        VirtualSpaceManager.INSTANCE.setMaster("AstroRad");
        imageSpace = VirtualSpaceManager.INSTANCE.addVirtualSpace("imageSpace");
        controlSpace = VirtualSpaceManager.INSTANCE.addVirtualSpace("controlSpace");
        imageCamera = imageSpace.addCamera();
        controlCamera = controlSpace.addCamera();
        ArrayList<Camera> imgCamList = new ArrayList<Camera>();
        imgCamList.add(imageCamera);
        ArrayList<Camera> controlCamList = new ArrayList<Camera>();
        controlCamList.add(controlCamera);
        Vector<Camera> cameras = new Vector<Camera>();
        cameras.add(imageCamera);
        cameras.add(controlCamera);
        View view = VirtualSpaceManager.INSTANCE.addFrameView(cameras, "Master View",
                View.STD_VIEW, 800, 600, false, true, true, null);	

        //setup cluster geometry
        ClusterGeometry clGeom = new ClusterGeometry(
                2740,
                1560,
                8,
                4);
        //setup clustered views
        ClusteredView imageView = new ClusteredView(clGeom, 3, 6, 4, imgCamList);
        ClusteredView controlView = new ClusteredView(clGeom, 27, 2, 4, controlCamList);
        VirtualSpaceManager.INSTANCE.addClusteredView(imageView);
        VirtualSpaceManager.INSTANCE.addClusteredView(controlView);

        setupControlZone();
    }

    private void setupControlZone(){
        rangeSel = new RangeSelection();
        controlSpace.addGlyph(rangeSel);
        rangeSel.sizeTo(2000);
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
}

