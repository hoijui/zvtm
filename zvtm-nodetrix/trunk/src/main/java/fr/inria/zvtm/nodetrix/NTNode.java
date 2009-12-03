/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

public class NTNode {

    String name;
    
    /* Owning matrix */
    Matrix matrix;
    
    NTEdge[] outgoingEdges, incomingEdges;
    
    /* relative offset of horizontal and vertical labels w.r.t matrix's center*/
	long hdx, hdy, vdx, vdy;
	/* Vertical label */
	VTextOr labelV;
	/* Horizontal label */
	VText labelH;
	
    public NTNode(String name){
        this.name = name;
    }
    
    public void addOutgoingEdge(NTEdge e){
        if (outgoingEdges == null){
            outgoingEdges = new NTEdge[1];
            outgoingEdges[0] = e;
        }
        else {
            NTEdge[] na = new NTEdge[outgoingEdges.length+1];
            System.arraycopy(outgoingEdges, 0, na, 0, outgoingEdges.length);
            na[outgoingEdges.length] = e;
            outgoingEdges = na;
        }
    }
    
    public void addIncomingEdge(NTEdge e){
        if (incomingEdges == null){
            incomingEdges = new NTEdge[1];
            incomingEdges[0] = e;
        }
        else {
            NTEdge[] na = new NTEdge[incomingEdges.length+1];
            System.arraycopy(incomingEdges, 0, na, 0, incomingEdges.length);
            na[incomingEdges.length] = e;
            incomingEdges = na;
        }
    }
    
    /**
     *@return null if empty
     */
    public NTEdge[] getIncomingEdges(){
        return incomingEdges;
    }

    /**
     *@return null if empty
     */
    public NTEdge[] getOutgoingEdges(){
        return outgoingEdges;
    }
    
    public void setMatrix(Matrix m){
        this.matrix = m;
    }
    
    public Matrix getMatrix(){
        return this.matrix;
    }

    public String toString(){
        return name+"@"+hashCode();
    }
    
    void createGraphics(long hdx, long hdy, long vdx, long vdy, VirtualSpace vs){
        this.hdx = hdx;
	    this.hdy = hdy;
	    this.vdx = vdx;
	    this.vdy = vdy;
	    labelH = new VText(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, VText.TEXT_ANCHOR_END);
	    labelV = new VTextOr(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
	    vs.addGlyph(labelH);
	    vs.addGlyph(labelV);
    }
    
    public void moveTo(long x, long y){
        labelH.moveTo(x+hdx, y+hdy);
        labelV.moveTo(x+vdx, y+vdy);
    }

}
