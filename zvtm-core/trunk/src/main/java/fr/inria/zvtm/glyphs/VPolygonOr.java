/*   Copyright (c) INRIA, 2012-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import fr.inria.zvtm.glyphs.projection.ProjPolygon;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Polygon. Can be resized. Can be reoriented.
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.FPolygon
 *@see fr.inria.zvtm.glyphs.VPolygon
 **/

public class VPolygonOr<T> extends VPolygon {

    /*array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjPolygon[] pc;

    /*store x,y vertex coords as relative coordinates w.r.t polygon's centroid*/
    // original coords (not reoriented) - {x,y,lx,ly}coords contain the oriented coords
    double[] oxcoords;
    double[] oycoords;

    /**
        *@param v list of x,y vertices ABSOLUTE coordinates in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        *@param or orientation
        */
    public VPolygonOr(Point2D.Double[] v, int z, Color c, double or){
        this(v, z, c, Color.BLACK, or, 1f);
    }

    /**
        *@param v list of x,y vertices ABSOLUTE coordinates i virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        *@param bc border color
        *@param or orientation
        */
    public VPolygonOr(Point2D.Double[] v, int z, Color c, Color bc, double or){
        this(v, z, c, bc, or, 1f);
    }

    /**
        *@param v list of x,y vertices ABSOLUTE coordinates i virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        *@param bc border color
        *@param or orientation
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VPolygonOr(Point2D.Double[] v, int z, Color c, Color bc, double or, float alpha){
        super(v, z, c, bc, alpha);
        oxcoords = new double[xcoords.length];
        oycoords = new double[ycoords.length];
        System.arraycopy(xcoords, 0, oxcoords, 0, xcoords.length);
        System.arraycopy(ycoords, 0, oycoords, 0, ycoords.length);
        orientTo(or);
    }

    @Override
    public double getOrient(){return orient;}

    @Override
    public void orientTo(double angle){
        this.orient = angle;
        updateOrient();
        System.out.println(orient);
    	VirtualSpaceManager.INSTANCE.repaint();
        System.out.println(xcoords[0]+" "+xcoords[1]+" "+xcoords[2]);
        System.out.println(ycoords[0]+" "+ycoords[1]+" "+ycoords[2]);
        System.out.println(oxcoords[0]+" "+oxcoords[1]+" "+oxcoords[2]);
        System.out.println(oycoords[0]+" "+oycoords[1]+" "+oycoords[2]);
    }

    void updateOrient(){
        for (int i=0;i<xcoords.length;i++){
            xcoords[i] = oxcoords[i]*Math.cos(orient) - oycoords[i]*Math.sin(orient);
            ycoords[i] = oxcoords[i]*Math.sin(orient) + oycoords[i]*Math.cos(orient);
        }
    }

    /** Vertex coordinates w.r.t centroid, not taking Glyph orientation into account.*/
    public Point2D.Double[] getAbsoluteVerticesNO(){
        Point2D.Double[] res = new Point2D.Double[oxcoords.length];
        for (int i = 0;i < oxcoords.length;i++){
            res[i] = new Point2D.Double(Math.round(oxcoords[i]+vx), Math.round(oycoords[i]+vy));
        }
        return res;
    }

    @Override
    public synchronized void reSize(double factor){
        size = 0;
        double f;
        for (int i=0;i<oxcoords.length;i++){
            oxcoords[i] = oxcoords[i] * factor;
            oycoords[i] = oycoords[i] * factor;
            f = Math.sqrt(oxcoords[i]*oxcoords[i] + oycoords[i]*oycoords[i]);
            if (f > size){size = f;}
        }
        size *= 2;
        updateOrient();
        VirtualSpaceManager.INSTANCE.repaint();
    }

    @Override
    public Object clone(){
        Point2D.Double[] lps = new Point2D.Double[oxcoords.length];
        for (int i = 0;i<lps.length;i++){
            lps[i] = new Point2D.Double(oxcoords[i]+vx,oycoords[i]+vy);
        }
        VPolygonOr res = new VPolygonOr(lps, getZindex(), color, borderColor, orient, (alphaC != null ) ? alphaC.getAlpha() : 1.0f);
        res.borderColor = this.borderColor;
        res.cursorInsideColor = this.cursorInsideColor;
        res.bColor = this.bColor;
        return res;
    }

}
