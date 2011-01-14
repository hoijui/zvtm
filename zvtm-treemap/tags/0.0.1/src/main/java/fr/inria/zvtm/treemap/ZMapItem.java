package fr.inria.zvtm.treemap;

import java.util.Collection;
import java.util.HashMap;

import fr.inria.zvtm.glyphs.Glyph;

/**
 * A MapItem class that also provides a user object
 * and a map of ZVTM glyphs that can be used to represent 
 * the tree.
 */
public class ZMapItem extends MapItem {
  protected Object userObject;
  protected HashMap<String, Glyph> graphicalObjects; 

  public ZMapItem(Object userObject){
    super();
    graphicalObjects = new HashMap<String, Glyph>();
    this.userObject = userObject;
  }

  public Object getUserObject(){
    return userObject;
  }

  public Glyph getGraphicalObject(String id){
    return graphicalObjects.get(id);
  }

  public Collection<Glyph> getGraphicalObjects(){
    return graphicalObjects.values();
  }

  public void putGraphicalObject(String id, Glyph glyph){
    graphicalObjects.put(id, glyph);
  }
}

