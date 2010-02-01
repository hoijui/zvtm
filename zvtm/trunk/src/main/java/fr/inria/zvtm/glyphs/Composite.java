package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import fr.inria.zvtm.engine.Camera;

/**
 * Composite glyph.
 */
public class Composite extends Glyph {
    private ArrayList<Glyph> children;

    private static class TPair {
        public AffineTransform drawTransform;
        public AffineTransform pickTransform;
    }

    //not the object state proper
    private transient ArrayList<TPair> transforms; //one per camera

    public Composite(){
        children = new ArrayList<Glyph>();
    }

    public void addChild(Glyph child){
        children.add(child);
    }

    public void removeChild(Glyph child){
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Composite clone(){
        Composite retval = (Composite)super.clone();  
        retval.children = new ArrayList<Glyph>();
        for(Glyph g: children){
            retval.children.add((Glyph)g.clone());
        } 
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fillsView(long w, long h, int camIndex){
        return false; //safe option
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
        //XXX implement
        return NO_CURSOR_EVENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetMouseIn(){
        //XXX ?
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void resetMouseIn(int i){
        //XXX ?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        //XXX implement
        return true;
    }

    @Override 
    public void removeCamera(int index){
        //XXX implement
    }

    @Override 
    public void addCamera(int index){
        //XXX implement
    }

    @Override
    public void initCams(int nbCam){
        //XXX implement
    }

    @Override
    public void drawForLens(Graphics2D g,
           int vW,
           int vH,
           int i,
           Stroke stdS,
           AffineTransform stdT,
           int dx,
           int dy){
            //XXX implement
    }

    @Override
    public void draw(Graphics2D g,
                          int vW,
                          int vH,
                          int i,
                          Stroke stdS,
                          AffineTransform stdT,
                          int dx,
                          int dy){
        //XXX implement
    }

    @Override
    public void projectForLens(Camera c,
                                    int lensWidth,
                                    int lensHeight,
                                    float lensMag,
                                    long lensx,
                                    long lensy){
        //XXX implement
    }

    @Override
    public void project(Camera c,
                             Dimension d){
        //XXX implement
    }

    @Override
    public void highlight(boolean b,
                               Color selectedColor){
        for(Glyph g: children){
            g.highlight(b, selectedColor);
        }
    }

    @Override
    public void orientTo(float angle){
        //XXX ?
    }

    @Override
    public float getOrient(){
        return 0f;
    }

    @Override 
    public void reSize(float factor){
        //XXX ?
    }

    @Override
    public void sizeTo(float radius){
        //XXX ?
    }

    @Override
    public float getSize(){
        //XXX ?
        return 1f;
    }

    @Override 
    public void move(long dx, long dy){
        vx += dx;
        vy += dy;
        for(Glyph g: children){
        }
    }
}

