/**
 * Copyright (C) 2001 by University of Maryland, College Park, MD 20742, USA 
 * and Martin Wattenberg, w@bewitched.com
 * All rights reserved.
 * Authors: Benjamin B. Bederson and Martin Wattenberg
 * http://www.cs.umd.edu/hcil/treemaps
 */

package fr.inria.zvtm.treemap;

/**
 * Layout using the pivot-by-middle algorithm.
 * 
 * This is essentially a wrapper class for the OrderedTreemap class.
 */
public class PivotByMiddle implements MapLayout
{
    OrderedTreemap orderedTreemap;
    
    public PivotByMiddle()
    {
        orderedTreemap=new OrderedTreemap();
        orderedTreemap.setPivotType(OrderedTreemap.PIVOT_BY_MIDDLE);
    }
    
    public void layout(MapModel model, Rect bounds, Insets insets)
    {
        orderedTreemap.layout(model, bounds, insets);
    }
    
    public String getName() {return "Pivot by Mid. / Ben B.";}
    public String getDescription() {return "Pivot by Middle, with stopping conditions "+
        "added by Ben Bederson.";}
}
