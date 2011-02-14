/*****************************************************************************
 * Copyright (C) 2003-2005 Jean-Daniel Fekete and INRIA, France              * Modified by: Romain Primet
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the X11 Software License    *
 *****************************************************************************/

package fr.inria.zvtm.treemap;

import java.util.ListIterator;

/**
 * Squarified Treemap Algorithm.
 * 
 * @author Jean-Daniel Fekete
 * 
 * @infovis.factory TreemapFactory Squarified
 */
public class Squarified {
    /** Instance. */
    public static final Squarified INSTANCE = new Squarified();
    protected static final double leftInset = 3;
    protected static final double rightInset = 3;
    protected static final double topInset = 20;
    protected static final double bottomInset = 3;

    //We never want to propagate this exception to callers
    private static class MissingSpaceException extends RuntimeException {
        MissingSpaceException(String reason){ super(reason); }
    }

    public String getName() {
        return "Squarified";
    }

    /**
     * Lays out a tree.
     */
    public void computeShapes(Rect bounds,Tree node) {
        //the "0" magic constants are unused; the are only useful
        //when requesting an exception if the available space is 
        //insufficient, usually in order to redo the layout with
        //a bigger surface
        visit(bounds.getX(), bounds.getY(), 
                bounds.getMaxX(), bounds.getMaxY(),
                node, false, 0, 0);
    }

    /**
     * Lays out a tree.
     */
    public Rect computeShapesResize(Rect bounds, Tree node, double minWidth, double minHeight){
        if(bounds.getWidth() <= 0 ||
                bounds.getHeight() <=0){
            throw new IllegalArgumentException("Gimme some space");
        }
        final double R2 = 1.414; 
        Rect finalBounds = new Rect(bounds);
        while(true){
            try{
                visit(finalBounds.getX(), finalBounds.getY(), 
                    finalBounds.getMaxX(), finalBounds.getMaxY(),
                    node, true, minWidth, minHeight);
                break;
            } catch(MissingSpaceException ex){
                finalBounds.setRect(finalBounds.getX(),
                        finalBounds.getY(),
                        finalBounds.getWidth() * R2,
                        finalBounds.getHeight() * R2);
            }
        }
        return finalBounds;
    }

    protected boolean beginBox(Rect box, double minWidth, 
            double minHeight){
        return box.getWidth() >= minWidth &&
            box.getHeight() >= minHeight;
        //return true;
    }

    protected int visit(double xmin, double ymin, double xmax,
            double ymax, Tree node, 
            boolean throwIfMissingSpace, 
            double minWidth, double minHeight) {
        Rect box = new Rect();
        box.setRect(xmin, ymin, (xmax - xmin), (ymax - ymin));
        if (!beginBox(box, minWidth, minHeight)) {
            if(throwIfMissingSpace){
                throw new MissingSpaceException("insufficient space");
            } else {
               // node.getMapItem().setBounds(box);
               // return 0;
            }
        }

        int ret = 1;

        if (node.isLeaf()) {
            node.getMapItem().setBounds(box);
            box = null;
        }
        else {
            node.getMapItem().setBounds(borderShape(xmin, ymin, xmax, ymax, node));
            removeBorder(box, node);
            ret = visitStrips(
                    box.x,
                    box.y,
                    box.x + box.w,
                    box.y + box.h, 
                    node,
                    throwIfMissingSpace,
                    minWidth,
                    minHeight);
        }
        return ret;
    } 

    private Rect borderShape(double xmin, double ymin, double xmax, double ymax, Tree node){
        return new Rect(xmin, ymin, xmax-xmin, ymax-ymin); 
    }

    private void removeBorder(Rect box, Tree node){
        box.x += leftInset;
        box.y += bottomInset;
        box.w -= (leftInset + rightInset);
        box.h -= (topInset + bottomInset);
    }

    protected boolean isVertical(double xmin, double ymin, double xmax,
            double ymax, Tree node) {
        return (xmax - xmin) > (ymax - ymin);
    }

    /**
     * Computes the rectangles that fill the containing rectangle allocated to
     * the specified node.
     *  
     */
    protected int visitStrips(double xmin, double ymin, double xmax,
            double ymax, Tree node, boolean throwIfMissingSpace, double minWidth, double minHeight) {
        double tw = node.getMapItem().getSize();
        int ret = 1;

        // The sizeColumn of the current node -- sum of weights of children
        // nodes --
        // is tw. It will fill a surface of width*height so the scale is
        // surface / tw.
        double scale = ((xmax - xmin) * (ymax - ymin)) / tw;

        if (scale == 0) {
            return 0;
        }

        // Split in strips
        for (ListIterator<Tree> it = node.childrenIterator(); it.hasNext();) {
            if (isVertical(xmin, ymin, xmax, ymax, node)) {
                // Vertical strip case, height is fixed, compute the widths of
                // strips
                double h = ymax - ymin;
                double y = ymin; // vertical position of this stip
                ListIterator<Tree> it2 = IteratorUtils.copy(it, node.peekAtChildren());
                // Compute the end of the strip, leaving the first of the next
                // strip
                // in the iterator.
                // invariant: squarify always advances the iterator or return 0.
                // returns the strip width, given its height.
                double width = squarify(it, h, scale);

                if (width == 0) {
                    return ret;
                }

                while (IteratorUtils.peek(it2) != IteratorUtils.peek(it)) {
                    Tree i = it2.next();
                    // compute the node height.
                    double nh = (i.getMapItem().getSize() * scale)
                            / width;

                    ret += visit(
                            xmin, 
                            y, 
                            xmin + width,
                            y + nh,
                            i,
                            throwIfMissingSpace,
                            minWidth,
                            minHeight);
                    y += nh;
                }

                xmin += width;
            } else {
                double w = xmax - xmin;
                double x = xmin;
                ListIterator<Tree> it2 = IteratorUtils.copy(it, node.peekAtChildren());

                double height = squarify(it, w, scale);

                if (height == 0) {
                    return ret;
                }

                while (IteratorUtils.peek(it2) != IteratorUtils.peek(it)) {
                    Tree i = it2.next();
                    double nw = (i.getMapItem().getSize() * scale)
                            / height;

                    ret += visit(
                            x,
                            ymin, 
                            x + nw,
                            ymin + height, 
                            i,
                            throwIfMissingSpace,
                            minWidth,
                            minHeight);
                    x += nw;
                }

                ymin += height;
            }
        }

        //assert(Math.abs(xmin-xmax)<1 || Math.abs(ymin-ymax)<1);

        return ret;
    }

    //returns an aspect ratio?
    protected double squarify(
            ListIterator<Tree> it,
            double length,
            double scale) {
        double s = 0;

        // First, find an initial non-empty rectangle to start with
        while (it.hasNext() && (s == 0)) {
            s = it.next().getMapItem().getSize() * scale;
        }

        // We have a first tentative width now
        double width = s / length;

        // We could have reached the end (the width might be zero then)
        if (!it.hasNext()) {
            return width;
        }

        // Prepare to iterate until the the worst aspect ratio stops to improve.
        double s2 = s * s;
        double min_area = s;
        double max_area = s;
        double worst = Math.max(length / width, width / length);
        double w2 = length * length;

        while (it.hasNext()) {
            // See if adding the next rectangle will improve the worst aspect
            // ratio
            double area = IteratorUtils.peek(it).getMapItem().getSize() * scale;

            // Skip empty rectangles.
            if (area == 0) {
                it.next();

                continue;
            }

            s += area;
            s2 = s * s;

            double cur_min_area = (area < min_area) ? area : min_area;
            double cur_max_area = (area > max_area) ? area : max_area;

            double cur_worst = Math.max((w2 * cur_max_area) / s2, s2
                    / (w2 * cur_min_area));

            if (cur_worst > worst) {
                // If result is worst, revert to previous area and return
                s -= area;
                break;
            }
            min_area = cur_min_area;
            max_area = cur_max_area;
            worst = cur_worst;
            it.next();
        }

        return s / length;
    }

}
