/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.io.IOException;
import java.net.URL;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a FITS images and creates / releases the corresponding
 * ZVTM glyph on demand.
 */
public class FitsImageDescription extends ResourceDescription {

    private static final Logger logger = LoggerFactory.getLogger(FitsImageDescription.class);

    private float scaleFactor = 1;
    private FitsImage.ScaleMethod scaleMethod;
    private FitsImage.ColorFilter colorFilter;
    private URL src;
    private String id;
    private double vx;
    private double vy;
    private int zindex;

    private volatile boolean display = true;
    //private Color strokeColor = null;

    private FitsImage glyph; //the actual FITS image

    public FitsImageDescription(String id, double x, double y, int z, URL src,
            Region parentRegion,
            float scaleFactor, FitsImage.ScaleMethod scaleMethod,
            FitsImage.ColorFilter colorFilter){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.src = src;

        this.scaleFactor = scaleFactor;
        this.scaleMethod = scaleMethod;
        this.colorFilter = colorFilter;
    }

    public String getType(){
        return FitsResourceHandler.RESOURCE_TYPE_FITS;
    }

    public void setColorFilter(FitsImage.ColorFilter colorFilter){
        this.colorFilter = colorFilter;
        if(glyph != null){
            glyph.setColorFilter(colorFilter);
        }
    }

    public void setScaleMethod(FitsImage.ScaleMethod scaleMethod){
        this.scaleMethod = scaleMethod;
        if(glyph != null){
            glyph.setScaleMethod(scaleMethod);
        }
    }

    public void rescale(){
    }

    //public void createObject(final VirtualSpace vs, final boolean fadeIn){
    public void createObject(final SceneManager sm, final VirtualSpace vs, final boolean fadeIn){
        try{
            glyph = new FitsImage(vx,vy,zindex,src,scaleFactor,false);
            System.out.println(glyph);
        } catch(Exception ioe){
            System.out.println("Could not create FitsImage");
            throw new Error("Could not create FitsImage");
        }
        glyph.setScaleMethod(scaleMethod);
        glyph.setColorFilter(colorFilter);
        glyph.setDrawBorder(false);
        vs.addGlyph(glyph,false);
    }

    public void destroyObject(final SceneManager sm, final VirtualSpace vs, boolean fadeOut){
        vs.removeGlyph(glyph);
        glyph = null;
    }

    @Override
    public Glyph getGlyph(){
        return glyph;
    }

    @Override
    public double getX(){
        return glyph.vx;
    }

    @Override
    public double getY(){
        return glyph.vy;
    }

    @Override
    public void moveTo(double x, double y){
        glyph.moveTo(x, y);
    }

}

