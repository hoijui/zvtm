/*   FILE: LogicalStructure.java
 *   DATE OF CREATION:  Thu Mar 15 18:33:17 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package net.claribole.zgrviewer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.svg.Metadata;

class LogicalStructure {

    static final String EDGE_DISAMBIGUATION_STR = "->";

    public static LogicalStructure build(Vector glyphs){
	Glyph g;
	Metadata md;
	Hashtable title2edge = new Hashtable();
	Hashtable title2node = new Hashtable();
	String title;
	LogicalStructure res = new LogicalStructure();
	Vector v;
	for (int i=0;i<glyphs.size();i++){
	    g = (Glyph)glyphs.elementAt(i);
	    md = (Metadata)g.getOwner();
	    if (md != null && (title=md.getTitle()) != null){
		if (title.contains(EDGE_DISAMBIGUATION_STR)){
		    // dealing with a glyph that is part of an edge
		    if (title2edge.containsKey(title)){
			v = (Vector)title2edge.get(title);
			v.add(g);
		    }
		    else {
			v = new Vector();
			v.add(g);
			title2edge.put(title, v);
		    }
		}
		else {
		    // dealing with a glyph that is part of a node
		    if (title2node.containsKey(title)){
			v = (Vector)title2node.get(title);
			v.add(g);
		    }
		    else {
			v = new Vector();
			v.add(g);
			title2node.put(title, v);
		    }
		}
	    }
	    // remain silent if structural information could not be extracted
	}
	// start by declaring nodes
	for (Enumeration e=title2node.keys();e.hasMoreElements();){
	    title = (String)e.nextElement();
	    res.declareNode(title, (Vector)title2node.get(title));
	}
	// then declare arcs that link these nodes
	for (Enumeration e=title2edge.keys();e.hasMoreElements();){
	    title = (String)e.nextElement();
	    res.declareEdge(title, (Vector)title2edge.get(title));
	}
	title2edge.clear();
	title2node.clear();
	return (res.isEmpty()) ? null : res;
    }

    LNode[] nodes;
    LEdge[] edges;
    
    LogicalStructure(){
	nodes= new LNode[0];
	edges = new LEdge[0];
    }

    void declareNode(String nodeTitle, Vector glyphs){
	System.out.println("N "+nodeTitle+" "+glyphs.size());
    }

    /* Edges must be declared after the nodes they link. */
    void declareEdge(String edgeTitle, Vector glyphs){
	System.out.println("E "+edgeTitle+" "+glyphs.size());	
    }

    boolean isEmpty(){
	return (nodes.length==0 || edges.length==0);
    }
    
}
