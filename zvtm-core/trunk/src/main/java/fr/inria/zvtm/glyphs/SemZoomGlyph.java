package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import fr.inria.zvtm.engine.Camera;

/**
 * A class that provides support for a basic form of semantic zooming.
 * A SemZoomGlyph instance is given a list of underlying glyph, and a list
 * of transition altitudes. When painting itself, the SemZoomGlyph delegates
 * to the appropriate underlying Glyph.
 */
public class SemZoomGlyph extends Glyph {
    private final ArrayList<Glyph> glyphs = new ArrayList<Glyph>();
    private final ArrayList<Double> transitions = new ArrayList<Double>();

    //note: the glyph should maintain bounds that are the union of
    //underlying glyph bounds
    public SemZoomGlyph(Glyph[] glyphs, double[] transitions){
        if(glyphs == null){
            throw new IllegalArgumentException("null array of Glyphs");
        }
        if(transitions.length != (glyphs.length - 1)){
            throw new IllegalArgumentException("incorrect number of transitions (" + (glyphs.length - 1) + " transitions expected)");
        }
        //check that transitions are ordered? (and possibly unique?)
    }

    /**
     * {@inheritDoc}
     */
    @Override
        public boolean fillsView(double w, double h, int camIndex){
            return false; //safe option
        }

    /**
     * {@inheritDoc}
     */
    @Override
        public short mouseInOut(int jpx, int jpy, int camIndex, double cvx, double cvy){
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
        public boolean coordInside(int jpx, int jpy, int camIndex, double cvx, double cvy){
            //XXX implement
            return true;
        }

    @Override 
        public void removeCamera(int index){
            for(Glyph glyph: glyphs){
                glyph.removeCamera(index);
            }
        }

    @Override 
        public void addCamera(int index){
            for(Glyph glyph: glyphs){
                glyph.addCamera(index);
            }
        }

    @Override
        public void initCams(int nbCam){
            for(Glyph glyph: glyphs){
                glyph.initCams(nbCam);
            } 
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
            Glyph glyph = selectChild(i); 
            glyph.drawForLens(g, vW, vH, i, stdS, stdT, dx, dy);
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
                double lensx,
                double lensy){
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
        }

    @Override
        public void orientTo(double angle){
            //XXX ?
        }

    @Override
        public double getOrient(){
            return 0f;
        }

    @Override 
        public void reSize(double factor){
            //XXX implement
        }

    @Override
        public void sizeTo(double newSize){
            //XXX implement
        }

    @Override 
        public void move(double dx, double dy){
        }

    @Override
        public void moveTo(double x, double y){
        }

    @Override 
        public double getSize(){
            return 10;// XXX implement
        }

    //private methods
    private Glyph selectChild(int camIndex){
        return null;
    }
}

