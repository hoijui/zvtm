/*   FILE: Location.java
 *   DATE OF CREATION:   Fri Jan 31 14:13:31 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Mon Feb 03 10:50:18 2003 by Emmanuel Pietriga
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id $
 */

package fr.inria.zvtm.engine;

import java.awt.geom.Point2D;

import java.util.Vector;

/** Store a camera location - provides 2 double fields for X,Y and a double field for altitude. */

public class Location {

    /** Get the difference between two locations  in position and altitude.
     *@param l1 first location
     *@param l2 second location
     *@return a vector whose first element is the altitude difference (as a Double) and second element is a Point2D.Double for X,Y difference.
     */
    public static Vector getDifference(Location l1, Location l2){
        Vector res=new Vector();
        Double f=new Double(l2.getAltitude()-l1.getAltitude());
        res.add(f);
        Point2D.Double p=new Point2D.Double(l2.getX()-l1.getX(),l2.getY()-l1.getY());
        res.add(p);
        return res;
    }

    public static boolean equals(Location l1,Location l2){
        if (l1.getX()==l2.getX() && l1.getY()==l2.getY() && l1.getAltitude()==l2.getAltitude()){return true;}
        else return false;
    }

    public Location(double x, double y, double a){
        vx=x;
        vy=y;
        alt=a;
    }

    /**a zvtm X coordinate*/
    public double vx;

    /**a zvtm Y coordinate*/
    public double vy;

    /**a zvtm altitude*/
    public double alt;

    /** Set (x,y) position.
     *@param p (x,y) coordinates
     */
    public void setPosition(Point2D.Double p){
        vx=p.x;
        vy=p.y;
    }

    /** Set x-coordinate only.
     *@param x coordinate on x-axis.
     */
    public void setPositionX(double x){
        vx=x;
    }

    /** Set y-coordinate only.
     *@param y coordinate on y-axis.
     */
    public void setPositionY(double y){
        vy=y;
    }

    /** Set altitude.
     *@param a altitude.
     */
    public void setAltitude(double a){
        alt=a;
    }

    /** Get (x,y) position.
     *@return (x,y) coordinates
     */
    public Point2D.Double getPosition(){
        return new Point2D.Double(vx,vy);
    }

    public double getX(){
        return vx;
    }

    public double getY(){
        return vy;
    }

    public double getAltitude(){
        return alt;
    }

    public String toString(){
        return "x="+vx+", y="+vy+", alt="+alt;
    }

}
