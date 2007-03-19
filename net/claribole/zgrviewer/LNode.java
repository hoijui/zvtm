/*   FILE: LNode.java
 *   DATE OF CREATION:  Thu Mar 15 19:18:17 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package net.claribole.zgrviewer;

import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.svg.Metadata;

class LNode extends LElem {

    Glyph[] glyphs;

    LEdge[] edges;
    short[] edgeDirections;

    LNode(String title, Vector glyphs){
	this.title = title;
	this.glyphs = new Glyph[glyphs.size()];
	for (int i=0;i<this.glyphs.length;i++){
	    this.glyphs[i] = (Glyph)glyphs.elementAt(i);
	}
	Metadata md = (Metadata)this.glyphs[0].getOwner();
	if (md != null){this.url = md.getURL();}
	for (int i=0;i<this.glyphs.length;i++){
	    this.glyphs[i].setOwner(this);
	}
	edges = new LEdge[0];
	edgeDirections = new short[0];
    }

    String getTitle(){
	return title;
    }

    void addArc(LEdge e, short direction){
	LEdge[] nedges = new LEdge[edges.length+1];
	short[] nedgeDirections = new short[nedges.length];
	System.arraycopy(edges, 0, nedges, 0, edges.length);
	System.arraycopy(edgeDirections, 0, nedgeDirections, 0, edgeDirections.length);
	nedges[edges.length] = e;
	nedgeDirections[edgeDirections.length] = direction;
	edges = nedges;
	edgeDirections = nedgeDirections;
    }

    public String toString(){
	String res = title + "[";
	for (int i=0;i<edges.length;i++){
	    res += ((edges[i] != null) ? edges[i].title + "@" + edges[i].hashCode() : "NULL") + "(" + edgeDirections[i] + ") ";
	}
	res += "]";
	return res;
    }

}
