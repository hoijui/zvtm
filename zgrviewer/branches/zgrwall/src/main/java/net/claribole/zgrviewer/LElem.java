/*   FILE: LNode.java
 *   DATE OF CREATION:  Thu Mar 15 19:18:17 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.svg.Metadata;

class LElem {
	
	static final String PORT_SEPARATOR = ":";

    String title;
    // URLs associated with each glyph (there might be different URLs associated with
    // the various glyphs constituting a node or edge)
    String[] URLs;

    Glyph[] glyphs;

    LElem(){}

    LElem(Metadata md){
        this.title = md.getTitle();
        this.URLs = new String[1];
        this.URLs[0] = md.getURL();
    }

    String getTitle(){
        return title;
    }

    String getURL(Glyph g){
        return URLs[0];
    }

	Glyph[] getGlyphs(){
		Glyph[] res = new Glyph[glyphs.length];
		System.arraycopy(glyphs, 0, res, 0, glyphs.length);
		return res;
	}

}
