/*
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */
package net.claribole.zgrviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.cluster.ClusteredImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

/**
 * A cyclic menu, similar to the ones that various window managers
 * use to switch applications (i.e. the "alt-tab" menu).
 * A menu contains at least one MenuItem.
 */
class CyclicMenu {
    private int currentItem = 0;
    private ArrayList<MenuElement> elems;
    private VirtualSpace targetSpace;

    private VRectangle box; //main box

    /**
     * Constructs a new CyclicMenu.
     * @param item a List containing the menu items. The list should contain
     * at least one item.
     */
    CyclicMenu(VirtualSpace targetSpace, List<CyclicMenuItem> items){
        if(items.size() == 0){
            throw new IllegalArgumentException("'items' should contain at least one element");
        }
        this.elems = new ArrayList<MenuElement>();
      //  for(){
      //      elems.add(elem);
      //  }
        this.targetSpace = targetSpace;
        setCurrentItemHighlight(true);
    }

    /**
     * Gets the currently selected menu item index.
     * @return the 0-based index of the currently selected menu item
     */
    int getUserChoice(){ return currentItem; }

    /**
     * Returns the menu items.
     */
    List<CyclicMenuItem> getMenuItems(){
        ArrayList retval = new ArrayList<CyclicMenuItem>();
        for(MenuElement elt: elems){
            retval.add(elt.item);
        }
        return retval;
    }

    /** Moves to the previous item */
    void previous(){
        setCurrentItemHighlight(false);
        currentItem = (currentItem + elems.size() - 1) % elems.size();
        setCurrentItemHighlight(true);
    }

    /** Moves to the next item */
    void next(){
        setCurrentItemHighlight(false);
        currentItem = (currentItem + 1) % elems.size();
        setCurrentItemHighlight(true);
    }

    private void setCurrentItemHighlight(boolean value){
        elems.get(currentItem).highlight(value);
    }

    /** Shows the menu */
    void show(){
        targetSpace.show(box);
        for(MenuElement elt: elems){
            elt.show();
        }
    }

    /** Hides the menu */
    void hide(){
        for(MenuElement elt: elems){
            elt.hide();
        }
        targetSpace.hide(box);
    }

    /**
     * ZVTM Representation of a menu item.
     * Needs a reference to the target VirtualSpace, so cannot be static.
     */
     class MenuItemRepr {
        private VRectangle box;
        private ClusteredImage icon;

        MenuItemRepr(CyclicMenuItem mi, long halfWidth, long halfHeight){
            //??
        }

        void show(){
            targetSpace.show(box);
            targetSpace.show(icon);
        }

        void hide(){
            targetSpace.hide(box);
            targetSpace.hide(icon);
        }

        void highlight(boolean value){
        }
    }

    //dumb struct, associates a menu item and its representation
    private static class MenuElement {
        public CyclicMenuItem item;
        public MenuItemRepr repr; 

        public void show(){
            repr.show(); 
        }
        public void hide(){
            repr.hide();
        }
        public void highlight(boolean value){
            repr.highlight(value);
        }
    }
}

