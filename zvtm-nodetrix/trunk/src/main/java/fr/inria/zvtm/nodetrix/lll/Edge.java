//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 

/* $Id$*/

/* Modified version of Andreas Noack's code from
   http://code.google.com/p/linloglayout/
   also licensed under LGPL. Modifications only
   to integrate it better with the zvtm-nodetrix
   datastructure */
package fr.inria.zvtm.nodetrix.lll;

/**
 * Weighted graph edge.
 * 
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @version 13.01.2008
 */
public class Edge {
    /** Start node. */ 
    public final Node startNode;
    /** End node. */ 
    public final Node endNode;
    /** Edge weight. */
    public final double weight;
    /** Edge density = <code>weight / (startNode.weight*endNode.weight)</code>. */
    public final double density;
    
    /**
     * Initializes the attributes.
     */
    public Edge(Node startNode, Node endNode, double weight) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.weight = weight;
        this.density = weight / (startNode.weight*endNode.weight);
    }
    
    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return startNode.getName() + "->" + endNode.getName() + " " + weight;
    }
}


