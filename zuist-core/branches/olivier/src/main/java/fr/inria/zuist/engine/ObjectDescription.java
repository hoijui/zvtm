/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ObjectDescription.java 5516 2015-04-27 15:09:27Z epietrig $
 */

package fr.inria.zuist.engine;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

/** Description of objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public abstract class ObjectDescription {

    protected String id;

    protected int zindex = 0;

    protected boolean sensitive = true;

    protected Region parentRegion;

    protected String takesTo;
    protected short takesToType;

    protected int loadCount = 0;

    //should we deprecate this ctor?
    ObjectDescription(){}

    protected ObjectDescription(String id, int z, Region pr, boolean sensitive){
    this.id = id;
    this.zindex = z;
    this.parentRegion = pr;
    this.setParentRegion(pr);
    this.sensitive = sensitive;
    }

    /** Type of object.
     *@return type of object.
     */
    public abstract String getType();
    
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

    protected void setZindex(int i){
        zindex = i;
    }

    /** Get Region this object belongs to. */
    public Region getParentRegion(){
        return parentRegion;
    }

    protected void setParentRegion(Region pr){
        parentRegion = pr;
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

    public int getLoadCount(){
        return loadCount;
    }

    /** Get x-coordinate of object in virtual space. */
    public abstract double getX();

    /** Get y-coordinate of object in virtual space. */
    public abstract double getY();

    public abstract void moveTo(double x, double y);

    /** Are the supplied coordinates inside the object described.
     *@return true if the supplied coordinates are inside the object descrived.
     */
    public abstract boolean coordInside(double pvx, double pvy);

}
