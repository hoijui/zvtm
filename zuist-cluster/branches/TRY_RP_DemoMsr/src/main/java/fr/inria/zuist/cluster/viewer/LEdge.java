/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LEdge.java 1299 2008-09-17 10:58:22Z epietrig $
 */

package fr.inria.zuist.cluster.viewer;

import java.util.Vector;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;

class LEdge extends LElem {

	static final short UNDIRECTED = 0;
	static final short INCOMING = 1;
	static final short OUTGOING = 2;

	static final String UNDIRECTED_STR = "--";
	static final String DIRECTED_STR = "->";

	boolean directed = false;

	int weight;

	LNode tail;
	LNode head;
	
	DPath edgeSpline;

	LEdge(int weight, DPath edgeSpline){
		this.weight = weight;
		this.edgeSpline = edgeSpline;
		this.edgeSpline.setOwner(this);
	}

	void setDirected(boolean b){
		directed = b;
	}

	boolean isDirected(){
		return directed;
	}

	boolean isLoop(){
		return tail == head;
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
	
	boolean isConnectedTo(LNode n){
		return (n == head) || (n == tail);
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

	DPath getSpline(){
		return edgeSpline;
	}

	void setTranslucency(float a){
		edgeSpline.setTranslucencyValue(a);
	}

	public String toString(){
		return weight + "@" + hashCode() + " [" + 
			((tail != null) ? tail.getCode() + "@" + tail.hashCode() : "NULL")+
			((directed) ? LEdge.DIRECTED_STR : LEdge.UNDIRECTED_STR) +
			((head != null) ? head.getCode() + "@" + head.hashCode() : "NULL") +
			"]";
	}

}
