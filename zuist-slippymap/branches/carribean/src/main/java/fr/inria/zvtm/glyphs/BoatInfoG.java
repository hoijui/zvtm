/*   Copyright (c) INRIA, 2015. All Rights Reserved
 * $Id:  $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.Shape;
import java.awt.Font;
import java.awt.Image;
import java.awt.Polygon;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.projection.ProjectedCoords;

public class BoatInfoG extends Glyph {

    /*------------------------------------------*/
    static final Font INFO_FONT = new Font("Dialog", Font.PLAIN, 10);

    // public static final int L_RING_IRADIUS = 8;
    public static final int L_RING_RADIUS = 9;
    public static final int L_RING_ORADIUS = 11;
    public static final int S_RING_RADIUS = 5;
    // public static final int S_RING_ORADIUS = 6;

    static final Stroke BSW2 = new BasicStroke(2f);
    static final Stroke BSW3 = new BasicStroke(3f);
    static final Stroke BSW4 = new BasicStroke(4f);

    public static final int LABEL_H_OFFSET = 2;

    static final float DETAIL_LEVEL_1_ALT = Camera.DEFAULT_FOCAL / (Camera.DEFAULT_FOCAL + 300);
    static final float DETAIL_LEVEL_2_ALT = Camera.DEFAULT_FOCAL / (Camera.DEFAULT_FOCAL + 50);

    static final int INFO_CHAR_W = 6;

    static final Color EXPOSED_COLOR = Color.RED;
    static final Color NOT_EXPOSED_COLOR = Color.BLACK;

    /*------------------------------------------*/
    ProjectedCoords[] pc;

    String boatType, boatNameAndCallsign, boatCrewAndPassengers, boatTimeToImpact;
    boolean exposed = false;
    int boatNameLPx = 0;
    int boatTimeToImpactLPx = 0;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     */
    public BoatInfoG(double x, double y, int z,
                     String type, String nameCS, String crewPass, String tti, boolean exp){
        vx = x;
        vy = y;
        vz = z;
        //XXX ADAPT TO SIZE
        size = 1;   //radius of the bounding circle
        setInfo(type, nameCS, crewPass, tti, exp);
    }

    public void setInfo(String type, String nameCS, String crewPass, String tti, boolean exp){
        boatType = (type!=null && !type.trim().isEmpty()) ? type.trim() : null;
        boatNameAndCallsign = (nameCS!=null && !nameCS.trim().isEmpty()) ? nameCS.trim() : null;
        boatNameLPx = (boatNameAndCallsign != null) ? boatNameAndCallsign.length() * INFO_CHAR_W : 0;
        boatCrewAndPassengers = (crewPass!=null && !crewPass.trim().isEmpty()) ? crewPass.trim() : null;
        boatTimeToImpact = (tti!=null && !tti.trim().isEmpty()) ? tti.trim() : null;
        boatTimeToImpactLPx = (boatTimeToImpact != null) ? boatTimeToImpact.length() * INFO_CHAR_W : 0;
        exposed = exp;
    }

    public String getBoatType(){
        return boatType;
    }

    public String getBoatNameAndCallsign(){
        return boatNameAndCallsign;
    }

    public String getBoatCrewAndPassengers(){
        return boatCrewAndPassengers;
    }

    public String getBoatTimeToImpact(){
        return boatTimeToImpact;
    }

    public boolean isExposed(){
        return exposed;
    }

    @Override
    public void initCams(int nbCam){
        pc = new ProjectedCoords[nbCam];
        for (int i=0;i<nbCam;i++){
            pc[i] = new ProjectedCoords();
        }
    }

    @Override
    public void addCamera(int verifIndex){
        if (pc != null){
            if (verifIndex == pc.length){
                ProjectedCoords[] ta = pc;
                pc = new ProjectedCoords[ta.length+1];
                for (int i=0;i<ta.length;i++){
                    pc[i] = ta[i];
                }
                pc[pc.length-1] = new ProjectedCoords();
            }
            else {System.err.println("BoatInfoG:Error while adding camera "+verifIndex);}
        }
        else {
            if (verifIndex == 0){
                pc = new ProjectedCoords[1];
                pc[0] = new ProjectedCoords();
            }
            else {System.err.println("BoatInfoG:Error while adding camera "+verifIndex);}
        }
    }

    @Override
    public void removeCamera(int index){
        pc[index] = null;
    }

    /** Cannot be resized (it makes on sense). */
    @Override
    public void sizeTo(double s){}

    /** Cannot be resized (it makes on sense). */
    @Override
    public void reSize(double factor){}

    /** Set the glyph's absolute orientation.
     *@param angle in [0:2Pi[
     */
     @Override
    public void orientTo(double angle){
        orient = angle;
        VirtualSpaceManager.INSTANCE.repaint();
    }

    //XXX ADAPT TO SIZE
    @Override
    public double getSize(){return 1.0f;}

    @Override
    public double getOrient(){return orient;}

    @Override
    public boolean fillsView(double w,double h,int camIndex){
        return false;
    }

    @Override
    public boolean coordInside(int jpx, int jpy, Camera c, double cvx, double cvy){ // int camIndex,
        return coordInsideP(jpx, jpy, c);//amIndex);
    }

    @Override
    public boolean coordInsideV(double cvx, double cvy, Camera c){//, int camIndex){
        //XXX:test should not be against L_RING_RADIUS/2d but the apparent unprojected size of this glyph in virtualspace
        return (Math.sqrt((cvx-vx)*(cvx-vx) + (cvy-vy)*(cvy-vy)) <= L_RING_RADIUS/2d);
    }

    @Override
    public boolean coordInsideP(int jpx, int jpy, Camera c){ //int camIndex){
        int camIndex = c.getIndex();
        return (Math.sqrt((jpx-pc[camIndex].cx)*(jpx-pc[camIndex].cx) + (jpy-pc[camIndex].cy)*(jpy-pc[camIndex].cy)) <= L_RING_RADIUS/2d);
    }

    @Override
    public boolean visibleInDisc(double dvx, double dvy, double dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
        return Math.sqrt(Math.pow(vx-dvx, 2)+Math.pow(vy-dvy, 2)) <= dvr;
    }

    @Override
    public void project(Camera c, Dimension d){
        int i = c.getIndex();
        coef = c.focal / (c.focal+c.altitude);
        //find coordinates of object's geom center wrt to camera center and project
        //translate in JPanel coords
        pc[i].cx = (int)Math.round((d.width/2)+(vx-c.vx)*coef);
        pc[i].cy = (int)Math.round((d.height/2)-(vy-c.vy)*coef);
    }

    @Override
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, double lensx, double lensy){
        int i = c.getIndex();
        coef = c.focal/(c.focal+c.altitude) * lensMag;
        //find coordinates of object's geom center wrt to camera center and project
        //translate in JPanel coords
        pc[i].lcx = (int)Math.round((lensWidth/2) + (vx-(lensx))*coef);
        pc[i].lcy = (int)Math.round((lensHeight/2) - (vy-(lensy))*coef);
    }

    @Override
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        int x = dx+pc[i].cx;
        int y = dy+pc[i].cy;
        // g.setColor((exposed) ? EXPOSED_COLOR : NOT_EXPOSED_COLOR);
        g.setColor(NOT_EXPOSED_COLOR);
        if (coef > DETAIL_LEVEL_1_ALT){
            // semantic zooming (show bigger circle with boat type only if sufficiently zoomed-in)
            g.setStroke(BSW4);
            g.drawOval(x - L_RING_RADIUS, y - L_RING_RADIUS, 2 * L_RING_RADIUS, 2 * L_RING_RADIUS);
            g.setStroke(BSW2);
            int lro_p4 = (int)Math.round(L_RING_ORADIUS * Math.cos(Math.PI/4f));
            g.setFont(INFO_FONT);
            if (boatTimeToImpact != null){
                g.drawLine(x-2*L_RING_ORADIUS-lro_p4, y+lro_p4, x-2*L_RING_ORADIUS-1, y+1);
                g.drawLine(x-3*L_RING_ORADIUS-lro_p4, y+lro_p4+1, x-2*L_RING_ORADIUS-lro_p4-1, y+lro_p4+1);
                g.drawString(boatTimeToImpact, x-3*L_RING_ORADIUS-lro_p4-boatTimeToImpactLPx-LABEL_H_OFFSET, y+lro_p4+2);
            }
            if (boatNameAndCallsign != null){
                g.drawLine(x-2*L_RING_ORADIUS, y, x-L_RING_ORADIUS, y);
                g.drawLine(x-2*L_RING_ORADIUS-lro_p4, y-lro_p4, x-2*L_RING_ORADIUS-1, y-1);
                g.drawLine(x-3*L_RING_ORADIUS-lro_p4, y-lro_p4-1, x-2*L_RING_ORADIUS-lro_p4-1, y-lro_p4-1);
                g.drawString(boatNameAndCallsign, x-3*L_RING_ORADIUS-lro_p4-boatNameLPx-LABEL_H_OFFSET, y-lro_p4+7);
            }
            if (coef > DETAIL_LEVEL_2_ALT){
                g.drawLine(x+L_RING_ORADIUS, y, x+2*L_RING_ORADIUS, y);
                if (boatType != null){
                    g.drawLine(x+2*L_RING_ORADIUS+lro_p4, y-lro_p4, x+2*L_RING_ORADIUS+1, y-1);
                    g.drawLine(x+3*L_RING_ORADIUS+lro_p4, y-lro_p4-1, x+2*L_RING_ORADIUS+lro_p4+1, y-lro_p4-1);
                    g.drawString(boatType, x+3*L_RING_ORADIUS+lro_p4+2*LABEL_H_OFFSET, y-lro_p4+7);
                }
                if (boatCrewAndPassengers != null){
                    g.drawLine(x+2*L_RING_ORADIUS+lro_p4, y+lro_p4, x+2*L_RING_ORADIUS+1, y+1);
                    g.drawLine(x+3*L_RING_ORADIUS+lro_p4, y+lro_p4+1, x+2*L_RING_ORADIUS+lro_p4+1, y+lro_p4+1);
                    g.drawString(boatCrewAndPassengers, x+3*L_RING_ORADIUS+lro_p4+2*LABEL_H_OFFSET, y+lro_p4+2);
                }
            }
            g.setStroke(stdS);
            g.setComposite(acO);
        }
        else {
            g.setStroke(BSW3);
            g.drawOval(x - S_RING_RADIUS, y - S_RING_RADIUS, 2 * S_RING_RADIUS, 2 * S_RING_RADIUS);
        }
        g.setStroke(stdS);
    }

    @Override
    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        // g.setColor(wColor);
        // g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, 1, 1);
        //XXX:TBW
    }

    @Override
    public Shape getJava2DShape(){
        return null;
    }

    @Override
    public void highlight(boolean b, Color selectedColor){}

}
