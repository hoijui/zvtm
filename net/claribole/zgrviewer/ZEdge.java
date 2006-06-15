/*   FILE: ZEdge.java
 *   DATE OF CREATION:  Wed Dec 15 16:54:11 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZEdge.java,v 1.2 2005/05/24 12:36:29 skbo Exp $
 */ 


package net.claribole.zgrviewer;

import java.util.Vector;

class ZEdge extends ZElement{

    ZNode tail, head;

    ZEdge(){
	glyphs = new Vector();
	this.url = "";
    }
    
    ZEdge(String url){
	glyphs = new Vector();
	this.url = (url != null) ? url : "";
    }

    void setTail(ZNode n){
	this.tail = n;
    }

    void setHead(ZNode n){
	this.head = n;
    }

    ZNode getTail(){
	return this.tail;
    }

    ZNode getHead(){
	return this.head;
    }

}