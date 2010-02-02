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

    //transform pair struct
    private static class TPair {
        public AffineTransform drawTransform;
        public AffineTransform invTransform;
        public TPair(){
            drawTransform = new AffineTransform();
            invTransform = new AffineTransform();
        }
    }

    //not the object state proper
    private transient ArrayList<TPair> transforms; //one per camera
    private transient float radius; //radius of the bounding circle

    public Composite(){
        children = new ArrayList<Glyph>();
        radius = 0;
    }

    /**
     * Adds a child Glyph to this Composite
     * @param Glyph to add
     */
    public void addChild(Glyph child){
        children.add(child);
        //recompute bounding radius?
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
            //recompute bounding radius?
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
        if( (index >= transforms.size()) ||
                (transforms.get(index) == null) ){
            System.err.println("Cannot remove camera at index " + index);
            return;
        }
        transforms.set(index, null);
    }

    @Override 
    public void addCamera(int index){
        //'index' is just a verification index, should be transforms.size()
        if(index != transforms.size()){
            System.err.println("Could not add camera at index " + index);
            return;
        }
        transforms.add(new TPair());
    }

    @Override
    public void initCams(int nbCam){
        //nop (no init step necessary)
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
        g.transform(transforms.get(i).drawTransform);
        for(Glyph child: children){
            child.drawForLens(g,vW,vH,i,stdS,stdT,dx,dy);
        }
        g.transform(transforms.get(i).invTransform);
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
        g.transform(transforms.get(i).drawTransform);
        for(Glyph child: children){
            child.draw(g,vW,vH,i,stdS,stdT,dx,dy);
        }
        g.transform(transforms.get(i).invTransform);
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
        //d: view dimension
        int camIdx = c.getIndex();
        if(camIdx >= transforms.size()){
            System.err.println("Could not find camera at index " + camIdx);
            return;
        }
        coef=(float)(c.focal/(c.focal+c.altitude));

        transforms.get(camIdx).drawTransform.setToIdentity();
        transforms.get(camIdx).drawTransform.translate(vx - c.posx,
                -(vy - c.posy));
        transforms.get(camIdx).drawTransform.scale(coef,coef);
        try{
        transforms.get(camIdx).invTransform = transforms.get(camIdx).drawTransform.createInverse();
        } catch(java.awt.geom.NoninvertibleTransformException ex){
            throw new AssertionError();
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
        //XXX ?
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
        //
    }
}

