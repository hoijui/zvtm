/**
 * Copyright (C) 2001 by University of Maryland, College Park, MD 20742, USA 
 * and Martin Wattenberg, w@bewitched.com
 * All rights reserved.
 * Authors: Benjamin B. Bederson and Martin Wattenberg
 * http://www.cs.umd.edu/hcil/treemaps
 */

package fr.inria.zvtm.treemap;

/**
 * A simple implementation of
 * the MapModel interface.
 */
public class SimpleMapModel
{
    private Mappable[] items;
    private Rect bounds;
    
    public SimpleMapModel() {}
    
    public SimpleMapModel(Mappable[] items, Rect bounds)
    {
        this.items=items;
        this.bounds=bounds;
    }
    
    public void setBounds(Rect bounds)
    {
        this.bounds=bounds;
    }
    
    public Rect getBounds()
    {
        return bounds;
    }
    
    public Mappable[] getItems()
    {
        return items;
    }
    
    public void setItems(Mappable[] items)
    {
        this.items=items;
    }
}
