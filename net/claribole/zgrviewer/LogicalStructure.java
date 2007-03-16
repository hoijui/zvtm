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

    public static LogicalStructure build(Vector glyphs){
	Glyph g;
	Metadata md;
	Hashtable title2edge = new Hashtable();
	Hashtable title2node = new Hashtable();
	String title;
	Vector v;
	for (int i=0;i<glyphs.size();i++){
	    g = (Glyph)glyphs.elementAt(i);
	    md = (Metadata)g.getOwner();
	    if (md != null && (title=md.getTitle()) != null){
		if (title.contains(LEdge.DIRECTED_STR) || title.contains(LEdge.UNDIRECTED_STR)){
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
	LogicalStructure res = new LogicalStructure(title2node, title2edge);
	title2edge.clear();
	title2node.clear();
	return (res.isEmpty()) ? null : res;
    }

    /* ----------------------------------- */

    LNode[] nodes;
    LEdge[] edges;
    
    LogicalStructure(Hashtable title2node, Hashtable title2edge){
	String title;
	// construct nodes
	nodes= new LNode[title2node.size()];
	int i = 0;
	for (Enumeration e=title2node.keys();e.hasMoreElements();){
	    title = (String)e.nextElement();
	    nodes[i] = new LNode(title, (Vector)title2node.get(title));
	    i++;
	}
	// construct edges
	i = 0;
	edges = new LEdge[title2edge.size()];
	for (Enumeration e=title2edge.keys();e.hasMoreElements();){
	    title = (String)e.nextElement();
	    edges[i] = new LEdge(title, (Vector)title2edge.get(title));
	    i++;
	}
	// link nodes and edges
	for (int j=0;j<edges.length;j++){
	    int id = edges[j].title.indexOf(LEdge.DIRECTED_STR);
	    if (id != -1){
		edges[j].setDirected(true);
		edges[j].setTail(getNode(edges[j].title.substring(0, id)));
		edges[j].setHead(getNode(edges[j].title.substring(id+2)));
	    }
	    else {
		id = edges[j].title.indexOf(LEdge.UNDIRECTED_STR);
		if (id != -1){
		    edges[j].setDirected(false);
		    edges[j].setTail(getNode(edges[j].title.substring(0, id)));
		    edges[j].setHead(getNode(edges[j].title.substring(id+2)));
		}
	    }
	}
    }

    LNode getNode(String title){
	LNode res = null;
	for (int i=0;i<nodes.length;i++){
	    if (nodes[i].title.equals(title)){return nodes[i];}
	}
	return null;
    }

    boolean isEmpty(){
	return (nodes.length==0 || edges.length==0);
    }
    
    public String toString(){
	String res = "";
	for (int i=0;i<nodes.length;i++){
	    res += nodes[i].toString() + "\n";
	}
	for (int i=0;i<edges.length;i++){
	    res += edges[i].toString() + "\n";
	}
	return res;
    }

}
