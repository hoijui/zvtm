/*   FILE: LEdge.java
 *   DATE OF CREATION:  Thu Mar 15 19:18:17 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.svg.Metadata;

class LEdge extends LElem {

    static final short UNDIRECTED = 0;
    static final short INCOMING = 1;
    static final short OUTGOING = 2;

    static final String UNDIRECTED_STR = "--";
    static final String DIRECTED_STR = "->";

    boolean directed = false;

    Glyph[] glyphs;

    LNode tail;
    LNode head;

    LEdge(String title, Vector glyphs){
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
    }

    void setDirected(boolean b){
	directed = b;
    }

    boolean isDirected(){
	return directed;
    }

    void setTail(LNode n){
	tail = n;
	if (tail != null){
	    tail.addArc(this, (directed) ? LEdge.OUTGOING : LEdge.UNDIRECTED);
	}
    }

    void setHead(LNode n){
	head = n;
	if (head != null){
	    head.addArc(this, (directed) ? LEdge.INCOMING : LEdge.UNDIRECTED);
	}
    }

    LNode getTail(){
	return tail;
    }

    LNode getHead(){
	return head;
    }

    LNode getOtherEnd(LNode n){
	return (n == tail) ? head : tail;
    }

    VPath getSpline(){
	for (int i=0;i<glyphs.length;i++){
	    if (glyphs[i] instanceof VPath){return (VPath)glyphs[i];}
	}
	return null;
    }

    public String toString(){
	return title + "@" + hashCode() + " [" + 
	    ((tail != null) ? tail.getTitle() + "@" + tail.hashCode() : "NULL")+
	    ((directed) ? LEdge.DIRECTED_STR : LEdge.UNDIRECTED_STR) +
	    ((head != null) ? head.getTitle() + "@" + head.hashCode() : "NULL") +
	    "]";
    }

}
