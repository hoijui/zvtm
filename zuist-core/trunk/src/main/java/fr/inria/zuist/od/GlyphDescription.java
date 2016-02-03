/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.od;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;

/**
 * ZUIST description of a Glyph.
 */
public class GlyphDescription extends ObjectDescription {

  public static final String OBJECT_TYPE_GLYPH= "zc-g";

  private Glyph glyph;

  public GlyphDescription(String id, Glyph glyph, int z, Region pr, boolean sensitive){
    super(id, z, pr, sensitive);
    this.glyph = glyph;
    glyph.setZindex(z);
  }

  /** Type of object.
   *@return type of object.
   */
  @Override
  public String getType(){
      return OBJECT_TYPE_GLYPH;
  }

  @Override public void createObject(SceneManager sm, VirtualSpace vs, boolean fadeIn){
    vs.addGlyph(glyph);
    sm.objectCreated(this, vs);
  }

  @Override public void destroyObject(SceneManager sm, VirtualSpace vs, boolean fadeOut){
    vs.removeGlyph(glyph);
    sm.objectDestroyed(this, vs);
  }

  @Override public Glyph getGlyph(){
    return glyph;
  }

  @Override public double getX(){
    return glyph.vx;
  }

  @Override public double getY(){
    return glyph.vy;
  }

  @Override public void moveTo(double x, double y){
    glyph.moveTo(x, y);
  }

    /** Are the supplied coordinates inside the object described.
     * This will not work for objects that need to know which camera is observing them (SIRectangle, SICircle, VText).
     *@return true if the supplied coordinates are inside the object described.
     */
  @Override
  public boolean coordInside(double pvx, double pvy){
      return this.glyph.coordInsideV(pvx, pvy, null);
  }

}
