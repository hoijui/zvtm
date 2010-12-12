/**
 * Copyright (C) 2001 by University of Maryland, College Park, MD 20742, USA 
 * and Martin Wattenberg, w@bewitched.com
 * All rights reserved.
 * Authors: Benjamin B. Bederson and Martin Wattenberg
 * http://www.cs.umd.edu/hcil/treemaps
 */

package fr.inria.zvtm.treemap;

/**
 * "Squarified" treemap layout invented by
 * J.J. van Wijk.
 */
public class SquarifiedLayout extends AbstractMapLayout
{
    public void layout(Mappable[] items, Rect bounds, Insets insets)
    {
        
        layout(sortDescending(items),0,items.length-1,bounds,insets);
    }
    
    public void layout(Mappable[] items, int start, int end, Rect bounds, Insets insets)
    {
        if (start>end) return;
            
        if (end-start<2)
        {
            SliceLayout.layoutBest(items,start,end,bounds,insets);
            return;
        }
        
        double x=bounds.x, y=bounds.y, w=bounds.w, h=bounds.h;
        
        double total=sum(items, start, end);
        int mid=start;
        double a=items[start].getSize()/total;
        double b=a;
        
        if (w<h)
        {
            // height/width
            while (mid<=end)
            {
                double aspect=normAspect(h,w,a,b);
                double q=items[mid].getSize()/total;
                if (normAspect(h,w,a,b+q)>aspect) break;
                mid++;
                b+=q;
            }
            SliceLayout.layoutBest(items,start,mid,insets.applyVert(new Rect(x,y,w,h*b)), insets);
            layout(items,mid+1,end,insets.applyVert(new Rect(x,y+h*b,w,h*(1-b))), insets);
        }
        else
        {
            // width/height
            while (mid<=end)
            {
                double aspect=normAspect(w,h,a,b);
                double q=items[mid].getSize()/total;
                if (normAspect(w,h,a,b+q)>aspect) break;
                mid++;
                b+=q;
            }
            SliceLayout.layoutBest(items,start,mid,insets.applyHoriz(new Rect(x,y,w*b,h)), insets);
            layout(items,mid+1,end,insets.applyHoriz(new Rect(x+w*b,y,w*(1-b),h)), insets);
        }
        
    }
    
    private double aspect(double big, double small, double a, double b)
    {
        return (big*b)/(small*a/b);
    }
    
    private double normAspect(double big, double small, double a, double b)
    {
        double x=aspect(big,small,a,b);
        if (x<1) return 1/x;
        return x;
    }
    
    private double sum(Mappable[] items, int start, int end)
    {
        double s=0;
        for (int i=start; i<=end; i++)
            s+=items[i].getSize();
        return s;
    }
    
    public String getName()
    {
        return "Squarified";
    }
    
    public String getDescription()
    {
        return "Algorithm used by J.J. van Wijk.";
    }
    
    
}
