/*   Copyright (c) INRIA, 2012. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import java.util.Vector;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRoundRect;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.svg.Metadata;

public class LGraph extends LElem {
    
    public static final short BOX_TYPE_NONE = 0;
    public static final short BOX_TYPE_RECT = 1;
    public static final short BOX_TYPE_ROUNDED_RECT = 2;

    LGraph(String title, Vector<Glyph> gls){
        this.title = title;
        boolean wasARoundedBoxIndeed = false;
        if (gls.size() == 8 && (gls.firstElement() instanceof VSegment || gls.firstElement() instanceof DPath)){
            // likely to be a rounded box
            wasARoundedBoxIndeed = attemptRoundedBoxReconstruction(gls);
        }
        if (!wasARoundedBoxIndeed){
            // if it is not a rounded box, then store the glyphs as is
            // - a regular box will be stored as a single VRectangle
            // - other glyphs, likely not boxes, will be stored the way they were declared in the SVG
            this.glyphs = new Glyph[gls.size()];
            for (int i=0;i<this.glyphs.length;i++){
                this.glyphs[i] = gls.elementAt(i);
            }
        }
    }
    
    boolean attemptRoundedBoxReconstruction(Vector<Glyph> gls){
        Vector<VSegment> sides = new Vector(4);
        Vector<Glyph> corners = new Vector(4);
        for (Glyph gl:gls){
            if (gl instanceof DPath){corners.add(gl);}
            else if (gl instanceof VSegment){sides.add((VSegment)gl);}
            // else do nothing
        }
        if (sides.size() == 4 && corners.size() == 4){
            // likely to be a rounded box
            this.glyphs = new Glyph[1];
            VSegment westSide = sides.firstElement();
            VSegment eastSide = sides.firstElement();
            VSegment northSide = sides.firstElement();
            VSegment southSide = sides.firstElement();
            for (int i=1;i<sides.size();i++){
                VSegment candidateSide = sides.elementAt(i);
                if (candidateSide.vx < westSide.vx){westSide = candidateSide;}
                if (candidateSide.vx > eastSide.vx){eastSide = candidateSide;}
                if (candidateSide.vy > northSide.vy){northSide = candidateSide;}
                if (candidateSide.vy < southSide.vy){southSide = candidateSide;}
            }
            //XXX: compute corner radius
            
            //XXX: instantiate a VRoundRect
            
            //XXX: replace all 8 glyphs in VirtualSpace by this VRoundRect, insert at the right place in drawing stack
            
            return true;
        }
        else {
            // not a rounded box
            return false;
        }
    }
    
    /** Get the type of box used to paint the boundaries of this subgraph.
     *@return one of BOX_TYPE_*
     */
    public short getBoxType(){
        if (glyphs.length == 1 && glyphs[0] instanceof VRectangle){
            return BOX_TYPE_RECT;
        }
        else if (glyphs.length == 1 && glyphs[0] instanceof VRoundRect){
            //XXX: TODO: will switch to glyphs.length == 1 && glyphs[0] instanceof VRoundRect
            //           once the constructor implements the DPath + VSegment -> VRoundRect conversion
            return BOX_TYPE_ROUNDED_RECT;
        }
        else {
            return BOX_TYPE_NONE;
        }
    }

}
