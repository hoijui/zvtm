/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ObjectDescription.java,v 1.9 2007/10/04 13:59:00 pietriga Exp $
 */

package fr.inria.zuist.engine;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.glyphs.Glyph;

/** Description of objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public abstract class ObjectDescription {

    String id;
    
    long vx, vy;
    boolean sensitive = true;

    Integer loadRequest, unloadRequest;

    Region parentRegion;

    String takesTo;
    short takesToType;

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public abstract void createObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeIn);

    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public abstract void destroyObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeOut);

    public void setSensitive(boolean b){
	sensitive = b;
    }

    public boolean isSensitive(){
	return sensitive;
    }

    public abstract Glyph getGlyph();
    
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

    public String getID(){
	return id;
    }

}
