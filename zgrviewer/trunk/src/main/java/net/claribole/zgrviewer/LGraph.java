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
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.svg.Metadata;

public class LGraph extends LElem {
    
    public static final short BOX_TYPE_NONE = 0;
    public static final short BOX_TYPE_RECT = 1;
    public static final short BOX_TYPE_ROUNDED_RECT = 2;

    LGraph(String title, Vector<Glyph> gls){
        this.title = title;
        this.glyphs = new Glyph[gls.size()];
        for (int i=0;i<this.glyphs.length;i++){
            this.glyphs[i] = gls.elementAt(i);
        }
    }
    
    /** Get the type of box used to paint the boundaries of this subgraph.
     *@return one of BOX_TYPE_*
     */
    public short getBoxType(){
        if (glyphs.length == 1 && glyphs[0] instanceof VRectangle){
            return BOX_TYPE_RECT;
        }
        else if (glyphs.length == 8 && (glyphs[0] instanceof VSegment || glyphs[0] instanceof DPath)){
            //XXX: TODO: will switch to glyphs.length == 1 && glyphs[0] instanceof VRoundRect
            //           once the constructor implements the DPath + VSegment -> VRoundRect conversion
            return BOX_TYPE_ROUNDED_RECT;
        }
        else {
            return BOX_TYPE_NONE;
        }
    }

}
