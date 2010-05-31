/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LNode.java 1283 2008-09-08 08:02:30Z epietrig $
 */

package fr.inria.zuist.cluster.viewer;

import java.util.Vector;

import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VText;

class LNode extends LElem {

    String code, name;

    LEdge[] edges;
    short[] edgeDirections;

	VCircle nodeShape;
	VText nodeLabel;

    LNode(String code, String name, VCircle nodeShape, VText nodeLabel){
        this.code = code;
        this.name = name;
		this.nodeShape = nodeShape;
		this.nodeLabel = nodeLabel;
		this.nodeShape.setOwner(this);
		this.nodeLabel.setOwner(this);
        edges = new LEdge[0];
        edgeDirections = new short[0];
    }
    
    String getCode(){
        return code;
    }

    String getName(){
        return name;
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

	LEdge[] getAllArcs(){
		LEdge[] res = new LEdge[edges.length];
		System.arraycopy(edges, 0, res, 0, edges.length);
		return res;
	}
	
	/** Get all arcs incoming or outgoing from this node, except for the specified one. */
	LEdge[] getOtherArcs(LEdge arc){
		int count = 0;
		for (int i=0;i<edges.length;i++){
			if (arc != edges[i]){count++;}
		}
		LEdge[] res = new LEdge[count];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (arc != edges[i]){res[j++] = edges[i];}
		}
		return res;
	}

	LEdge[] getOutgoingArcs(){
		int oaCount = 0;
		for (int i=0;i<edgeDirections.length;i++){
			if (edgeDirections[i] == LEdge.OUTGOING){oaCount++;}
		}
		LEdge[] res = new LEdge[oaCount];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (edgeDirections[i] == LEdge.OUTGOING){
				res[j++] = edges[i];
			}
		}
		return res;
	}

	LEdge[] getIncomingArcs(){
		int oaCount = 0;
		for (int i=0;i<edgeDirections.length;i++){
			if (edgeDirections[i] == LEdge.INCOMING){oaCount++;}
		}
		LEdge[] res = new LEdge[oaCount];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (edgeDirections[i] == LEdge.INCOMING){
				res[j++] = edges[i];
			}
		}
		return res;
	}

	LEdge[] getUndirectedArcs(){
		int oaCount = 0;
		for (int i=0;i<edgeDirections.length;i++){
			if (edgeDirections[i] == LEdge.UNDIRECTED){oaCount++;}
		}
		LEdge[] res = new LEdge[oaCount];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (edgeDirections[i] == LEdge.UNDIRECTED){
				res[j++] = edges[i];
			}
		}
		return res;
	}
	
	LEdge getArcLeadingTo(LNode n){
		for (int i=0;i<edges.length;i++){
			if (edges[i].getOtherEnd(this) == n){
				return edges[i];
			}
		}
		return null;
	}

	VCircle getShape(){
		return nodeShape;
	}
	
	VText getLabel(){
		return nodeLabel;
	}
	
	void setTranslucency(float a){
		nodeShape.setTranslucencyValue(a);
		nodeLabel.setTranslucencyValue(a);
	}

	public String toString(){
		String res = code + " " + name + "[";
		for (int i=0;i<edges.length;i++){
			res += ((edges[i] != null) ? edges[i].weight + "@" + edges[i].hashCode() : "NULL") + "(" + edgeDirections[i] + ") ";
		}
		res += "]";
		return res;
	}

}
