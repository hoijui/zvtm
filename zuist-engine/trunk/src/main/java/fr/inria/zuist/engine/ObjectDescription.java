/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

/** Description of objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public abstract class ObjectDescription {

    String id;

	int zindex = 0;

    boolean sensitive = true;

    Region parentRegion;

    String takesTo;
    short takesToType;
    
    /** Called automatically by scene manager. Can be called by client application to force loading of objects not actually visible. */
    public abstract void createObject(SceneManager sm, final VirtualSpace vs, boolean fadeIn);

    /** Called automatically by scene manager. Can be called by client application to force unloading of objects still visible. */
    public abstract void destroyObject(SceneManager sm, final VirtualSpace vs, boolean fadeOut);

    /** Should the object be sensitive to cursor entry/exit. */
    public void setSensitive(boolean b){
	    sensitive = b;
    }

    /** Is the object sensitive to cursor entry/exit. */
    public boolean isSensitive(){
	    return sensitive;
    }

    /** Get Glyph described by this description.
     *@return might return null if the description has not been loaded. This depends on the type of ObjectDescription.
     */
    public abstract Glyph getGlyph();
    
    /** Get z-index for this object. */
	public int getZindex(){
		return zindex;
	}

    /** Get Region this object belongs to. */
    public Region getParentRegion(){
	    return parentRegion;
    }

    /** Should take/"transport" to object/region whose ID is id
     *@param id set to null if should not take anywhere 
     */
    public void setTakesTo(String id, short t){
	    takesTo = id;
	    takesToType = t;
    }

    /** Get the ID of object where this one takes/"transports" to.
     *@return null if none
     */
    public String takesTo(){
	    return takesTo;
    }

    /**
     *@return one of SceneManager.{TAKES_TO_OBJECT, TAKES_TO_REGION}
     */
    public short takesToType(){
	    return takesToType;
    }

    /** Get this object description's ID. */
    public String getID(){
	    return id;
    }
    
    /** Get x-coordinate of object in virtual space. */
    public abstract double getX();
    
    /** Get y-coordinate of object in virtual space. */
    public abstract double getY();
    
    public abstract void moveTo(double x, double y);

}
