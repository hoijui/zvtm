/*   FILE: ZElement.java
 *   DATE OF CREATION:  Thu Dec 16 16:18:19 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZElement.java,v 1.1 2005/01/10 14:21:34 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;

class ZElement {
        
    Vector glyphs;
    String url, title;

    ZElement(){
	glyphs = new Vector();
	this.url = "";
	this.title = "";
    }

    ZElement(String url, String title){
	glyphs = new Vector();
	this.url = (url != null) ? url : "";
	this.title = (title != null) ? title : "";
    }

    void addGlyph(Glyph g){
	glyphs.add(g);
    }

    String getURL(){
	return this.url;
    }

    String getTitle(){
	return this.url;
    }

}