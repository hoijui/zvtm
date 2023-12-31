/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.geom.Point2D;

/** A level in the scene. Levels correspond to ranges of altitude (defined by a floor and ceiling).
 * Levels cannot overlap.
 * Levels contain regions which themselves contain objects.
 *@author Emmanuel Pietriga
 */

public class Level {

    double ceilingAlt;
    double floorAlt;

    Region[] regions = new Region[0];

    /** Create a new level.
     *@param c ceiling altitude
     *@param f floor altitude
     */
    Level(double c, double f){
        ceilingAlt = c;
        floorAlt = f;
    }

    /** Get ceiling altitude for this level.
     */
    public double getCeilingAltitude(){
        return ceilingAlt;
    }

    /** Get floor altitude for this level.
     */
    public double getFloorAltitude(){
        return floorAlt;
    }

    /** Get region whose center is closest to a given location at this level.
        *@return null if no region at this level
        */
    public Region getClosestRegion(Point2D.Double lp){
        if (regions.length >= 1){
            Region res = regions[0];
            for (int i=1;i<regions.length;i++){
                if ((regions[i].x-lp.x)*(regions[i].x-lp.x) + (regions[i].y-lp.y)*(regions[i].y-lp.y)
                    < (res.x-lp.x)*(res.x-lp.x) + (res.y-lp.y)*(res.y-lp.y)){
                    res = regions[i];
                }
            }
            return res;
        }
        else {
            return null;
        }
    }

    /** Does Region r belongs to this level. */
    public boolean contains(Region r){
        for (int i=0;i<regions.length;i++){
            if (regions[i] == r){
                return true;
            }
        }
        return false;
    }

    void addRegion(Region r){
        Region[] tmpR = new Region[regions.length+1];
        System.arraycopy(regions, 0, tmpR, 0, regions.length);
        regions = tmpR;
        regions[regions.length-1] = r;
    }

    void removeRegion(Region r){
        for (int i=0;i<regions.length;i++){
            if (regions[i] == r){
                Region[] tmpR = new Region[regions.length-1];
                System.arraycopy(regions, 0, tmpR, 0, i);
                System.arraycopy(regions, i+1, tmpR, i, regions.length-i-1);
                regions = tmpR;
                return;
            }
        }
    }

    boolean inRange(double alt){
        return (alt >= floorAlt) && (alt < ceilingAlt);
    }

    public String toString(){
        return "Ceiling: " + ceilingAlt + "\n" +
            "Floor: " + floorAlt + "\n";
    }

    /** Get the bounding box enclosing all regions at this level.
     *@return the west, north, east and south bounds of the box. null if no region.
     */
    public double[] getBounds(){
        if (regions.length > 0){
            double[] res = {regions[0].wnes[0], regions[0].wnes[1], regions[0].wnes[2], regions[0].wnes[3]};
            for (int i=1;i<regions.length;i++){
                if (regions[i].wnes[0] < res[0]){res[0] = regions[i].wnes[0];}
                if (regions[i].wnes[1] > res[1]){res[1] = regions[i].wnes[1];}
                if (regions[i].wnes[2] > res[2]){res[2] = regions[i].wnes[2];}
                if (regions[i].wnes[3] < res[3]){res[3] = regions[i].wnes[3];}
            }
            return res;
        }
        else {
            return null;
        }
    }

}
