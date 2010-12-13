/**
 * Copyright (C) 2001 by University of Maryland, College Park, MD 20742, USA 
 * and Martin Wattenberg, w@bewitched.com
 * All rights reserved.
 * Authors: Benjamin B. Bederson and Martin Wattenberg
 * Modified by: Romain Primet
 * http://www.cs.umd.edu/hcil/treemaps
 */

package fr.inria.zvtm.treemap;

import java.util.*;

/**
 *
 * An implementation of MapModel that represents
 * a hierarchical structure. It currently cannot
 * handle structural changes to the tree, since it
 * caches a fair amount of information.
 *
 * Note: this class was called TreeModel in the original
 * UMD library. It was renamed to Tree because TreeModel is a well-known
 * Swing class that is likely to be used together with Tree instances, and
 * using the fully qualified names is a pain.
 */
public class Tree<M extends Mappable> implements MapModel
{
    private M mapItem;
    private Mappable[] childItems;
    private Mappable[] cachedTreeItems; // we assume tree structure doesn't change.
    private MapModel[] cachedLeafModels;
    private Tree parent;
    private Vector<Tree> children=new Vector<Tree>();
    private boolean sumsChildren;
    
 //   public Tree()
 //   {
 //       this.mapItem=new MapItem();
 //       sumsChildren=true;
 //   }
    
    public Tree(M mapItem)
    {
        this.mapItem=mapItem;
    }
    
    
    public void setOrder(int order)
    {
        mapItem.setOrder(order);
    }
    
    public MapModel[] getLeafModels()
    {
        if (cachedLeafModels!=null)
            return cachedLeafModels;
        Vector v=new Vector();
        addLeafModels(v);
        int n=v.size();
        MapModel[] m=new MapModel[n];
        v.copyInto(m);
        cachedLeafModels=m;
        return m;
    }
    
    private Vector addLeafModels(Vector v)
    {
        if (!hasChildren())
        {
            System.err.println("Somehow tried to get child model for leaf!!!");
            return v;
        }
        if (!getChild(0).hasChildren())
            v.addElement(this);
        else
            for (int i=childCount()-1; i>=0; i--)
                getChild(i).addLeafModels(v);
        return v;
        
    }
    
    public int depth()
    {
        if (parent==null) return 0;
        return 1+parent.depth();
    }
    
    public void layout(MapLayout tiling, double textHeight, Insets insets)
    {
        layout(tiling, mapItem.getBounds(), textHeight, insets);
    }
    
    public void layout(MapLayout tiling, Rect bounds, double textHeight, Insets insets)
    {
        mapItem.setBounds(bounds);
        //if (!hasChildren()) return;
        if(!hasChildren()){
            Rect leafBounds = new Rect(bounds);
            leafBounds.h -= textHeight;
            mapItem.setBounds(leafBounds);
            return;
        }
        double s=sum();
        bounds.h -= textHeight;
        tiling.layout(this, bounds, insets);
        for (int i=childCount()-1; i>=0; i--)
            getChild(i).layout(tiling, textHeight, insets);
    }
    
    public Mappable[] getTreeItems()
    {
        if (cachedTreeItems!=null)
            return cachedTreeItems;
            
        Vector v=new Vector();
        addTreeItems(v);
        int n=v.size();
        Mappable[] m=new Mappable[n];
        v.copyInto(m);
        cachedTreeItems=m;
        return m;
    }
    
    private void addTreeItems(Vector v)
    {
       // if (!hasChildren())
       //     v.addElement(mapItem);
       // else
       //     for (int i=childCount()-1; i>=0; i--)
       //         getChild(i).addTreeItems(v);
	     v.addElement(mapItem);
	     if(hasChildren()){
            for (int i=childCount()-1; i>=0; i--)
                getChild(i).addTreeItems(v);
	     }
    }
    
    private double sum()
    {
        //if (!sumsChildren)
        //    return mapItem.getSize();
	      if(!hasChildren())
		    return mapItem.getSize();

        double s=0;
        for (int i=childCount()-1; i>=0; i--)
            s+=getChild(i).sum();
        mapItem.setSize(s);
        return s;
    }
    
    public Mappable[] getItems()
    {
        if (childItems!=null)
            return childItems;
        int n=childCount();
        childItems=new Mappable[n];
        for (int i=0; i<n; i++)
        {
            childItems[i]=getChild(i).getMapItem();
            childItems[i].setDepth(1+depth());
        }
        return childItems;
    }
    
    public M getMapItem()
    {
        return mapItem;
    }
    
    public void addChild(Tree<M> child)
    {
        child.setParent(this);
        children.addElement(child);
        childItems=null;
    }
    
    public void setParent(Tree parent)
    {
        for (Tree p=parent; p!=null; p=p.getParent())
            if (p==this) throw new IllegalArgumentException("Circular ancestry!");
        this.parent=parent;
    }
    
    public Tree getParent()
    {
        return parent;
    }
    
    public int childCount()
    {
        return children.size();
    }
    
    public Tree getChild(int n)
    {
        return children.elementAt(n);
    }
    
    public boolean hasChildren()
    {
        return children.size()>0;
    }
    
    public void print()
    {
        print("");
    }
    
    private void print(String prefix)
    {
        System.out.println(prefix+"size="+mapItem.getSize());
        for (int i=0; i<childCount(); i++)
            getChild(i).print(prefix+"..");
    }

    /**
     * Do a preorder traversal of this Tree
     */
    public void traversePre(Walker<Tree<M>> walker){
      walker.visitNode(this);
      for(int i=0; i<childCount(); ++i){
        getChild(i).traversePre(walker);
      }
    }

    /**
     * Do a postorder traversal of this Tree
     */
    public void traversePost(Walker<Tree<M>> walker){
      for(int i=0; i<childCount(); ++i){
        getChild(i).traversePost(walker);
      }
      walker.visitNode(this);
    }
}
