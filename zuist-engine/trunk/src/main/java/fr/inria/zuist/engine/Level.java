/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Level.java,v 1.5 2007/10/01 13:24:48 pietriga Exp $
 */

package fr.inria.zuist.engine;

import com.xerox.VTM.engine.LongPoint;

/** Information about a level in the scene. Levels contain regions which themselves contain objects.
 *@author Emmanuel Pietriga
 */

public class Level {

    float ceilingAlt;
    float floorAlt;
    
    Region[] regions = new Region[0];

    Level(float c, float f){
	ceilingAlt = c;
	floorAlt = f;
    }

    /** Get ceiling altitude for this level.
     */
    public float getCeilingAltitude(){
	return ceilingAlt;
    }
    
    /** Get floor altitude for this level.
     */
    public float getFloorAltitude(){
	return floorAlt;
    }

	/** Get region whose center is closest to a given location at this level.
		*@return null if no region at this level
		*/
	public Region getClosestRegion(LongPoint lp){
		if (regions.length > 1){
			Region res = regions[0];
			for (int i=1;i<regions.length;i++){
				if (Math.sqrt(Math.pow(regions[i].x-lp.x, 2) + Math.pow(regions[i].y-lp.y, 2)) < Math.sqrt(Math.pow(res.x-lp.x, 2) + Math.pow(res.y-lp.y, 2))){
					res = regions[i];
				}
			}
			return res;
		}
		else {
			return null;
		}
	}
	
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

    boolean inRange(float alt){
	return (alt >= floorAlt) && (alt < ceilingAlt);
    }

    public String toString(){
	return "Ceiling: " + ceilingAlt + "\n" +
	    "Floor: " + floorAlt + "\n";
    }

}
