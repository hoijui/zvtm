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

    private transient float radius; //radius of the bounding circle

    public Composite(){
        vx = 0;
        vy = 0;
        children = new ArrayList<Glyph>();
        radius = 0;
    }

    /**
     * Adds a child Glyph to this Composite
     * @param Glyph to add
     */
    public void addChild(Glyph child){
        children.add(child);
        computeRadius(); 
    }

    /**
     * Removes a child Glyph from this Composite.
     * @param child Glyph to remove
     * @return <code>true</code> if child has been removed, <code>false</code> 
     * otherwise
     */
    public boolean removeChild(Glyph child){
        boolean removed = children.remove(child);
        if(removed){
            computeRadius(); 
        }
        return removed;
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
        for(Glyph child: children){
            child.removeCamera(index);
        }
    }

    @Override 
    public void addCamera(int index){
        for(Glyph child: children){
            child.addCamera(index);
        }
    }

    @Override
    public void initCams(int nbCam){
       for(Glyph child: children){
           child.initCams(nbCam);
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
        for(Glyph child: children){
            child.drawForLens(g,vW,vH,i,stdS,stdT,dx,dy);
        }
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
        for(Glyph child: children){
            child.draw(g,vW,vH,i,stdS,stdT,dx,dy);
        }
    }

    @Override
    public void projectForLens(Camera c,
                                    int lensWidth,
                                    int lensHeight,
                                    float lensMag,
                                    long lensx,
                                    long lensy){
        for(Glyph child: children){
            child.projectForLens(c,lensWidth,lensHeight,lensMag,lensx,lensy);
        }
    }

    @Override
    public void project(Camera c,
            Dimension d){
        for(Glyph child: children){
            child.project(c,d);
        }
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
        radius *= factor;
        for(Glyph child: children){
            child.reSize(factor);
        }
    }

    @Override
    public void sizeTo(float radius){
        //XXX ?
    }

    @Override
    public float getSize(){
        return radius;
    }

    @Override 
    public void move(long dx, long dy){
        vx += dx;
        vy += dy;
        for(Glyph child: children){
            child.move(dx, dy);
        } 
    }

    @Override
    public void moveTo(long x, long y){
        move(x - vx, y - vy);
    }

    private void computeRadius(){
        radius = 10f;
    }
}

