/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zuist.engine;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * ZUIST description of a Glyph.
 */
public class GlyphDescription extends ObjectDescription {
  private Glyph glyph;

  public GlyphDescription(String id, Glyph glyph, int z, Region pr, boolean sensitive){
    super(id, z, pr, sensitive);
    this.glyph = glyph;
    glyph.setZindex(z);
  }

  @Override public void createObject(SceneManager sm, VirtualSpace vs, boolean fadeIn){
    vs.addGlyph(glyph);
  }

  @Override public void destroyObject(SceneManager sm, VirtualSpace vs, boolean fadeOut){
    vs.removeGlyph(glyph);
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
}

