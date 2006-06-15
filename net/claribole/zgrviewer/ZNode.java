/*   FILE: ZNode.java
 *   DATE OF CREATION:  Wed Dec 15 16:51:17 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZNode.java,v 1.2 2005/05/24 12:36:29 skbo Exp $
 */ 

package net.claribole.zgrviewer;

import java.util.Vector;

class ZNode extends ZElement {
    
    Vector incomingEdges;
    Vector outgoingEdges;
    
    ZNode(){
	glyphs = new Vector();
	this.url = "";
	incomingEdges = new Vector();
	outgoingEdges = new Vector();
    }

    ZNode(String url){
	glyphs = new Vector();
	this.url = (url != null) ? url : "";
	incomingEdges = new Vector();
	outgoingEdges = new Vector();
    }

    void addIncomingEdge(ZEdge e){
	incomingEdges.add(e);
    }

    void addOutgoingEdge(ZEdge e){
	outgoingEdges.add(e);
    }

    Vector getIncomingEdges(){
	return incomingEdges;
    }

    Vector getOutgoingEdges(){
	return outgoingEdges;
    }
    
}