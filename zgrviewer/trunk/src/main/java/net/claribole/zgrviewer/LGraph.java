/*   Copyright (c) INRIA, 2012. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import java.util.Vector;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.svg.Metadata;

public class LGraph extends LElem {

    LGraph(String title, Vector<Glyph> gls){
        this.title = title;
        this.glyphs = new Glyph[gls.size()];
        for (int i=0;i<this.glyphs.length;i++){
            this.glyphs[i] = gls.elementAt(i);
        }
    }

}
