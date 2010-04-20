package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.engine.Camera;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

public class FitsImage extends Glyph{
    //current representation
    private BufferedImage repr;

    //color transfer function

    //FITS data proper
    private Fits fits;

    public FitsImage(String filename){
        try{
            fits = new Fits(filename);
            BasicHDU hdu;
            while((hdu = fits.readHDU()) != null){
                if(hdu instanceof ImageHDU){
                }
            }
        } catch(FitsException fe){
            System.err.println("Could not load FITS data: " + fe);
        } catch(IOException ioe){
            System.err.println("Could not load FITS data: " + ioe);
        }

    }

    //Rebuild the representation to be displayed
    private void rebuildRepr(){
    }

    public void initCams(int nbCams){
    }

    public void addCamera(int verifIndex){
    }

    public void removeCamera(int index){
    }

    public boolean coordInside(int jpx,
            int jpy,
            int camIndex,
            long cvx,
            long cvy){
        return false;
    }

    public void resetMouseIn(){

    }

    public void resetMouseIn(int i){

    }

    public boolean fillsView(long w,
            long h,
            int camIndex){
        return false;
    }

    public short mouseInOut(int jpx,
                                 int jpy,
                                 int camIndex,
                                 long cvx,
                                 long cvy){
        return 0;
    }

    public Object clone(){
        return null;
    }

    public void draw(Graphics2D g,
            int vW,
            int vH,
            int i,
            Stroke stdS,
            AffineTransform stdT,
            int dx,
            int dy){

    }

    public void drawForLens(Graphics2D g,
            int vW,
            int vH,
            int i,
            Stroke stdS,
            AffineTransform stdT,
            int dx,
            int dy){

    }

    public void project(Camera c,
            Dimension d){

    }

    public void projectForLens(Camera c,
            int lensWidth,
            int lensHeight,
            float lensMag,
            long lensx,
            long lensy){

    }

    public void highlight(boolean b,
            Color selectedColor){
        
    }

    public void orientTo(float angle){
    }

    public float getOrient(){
        return 0;
    }

    public void reSize(float size){
    }

    public void sizeTo(float size){
    }

    public float getSize(){
        return 1;
    }

}

