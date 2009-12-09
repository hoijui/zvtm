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
	long wdx, wdy, ndx, ndy, edx, edy, sdx, sdy;
	/* Vertical label */
	VTextOr labelN, labelS;
	/* Horizontal label */
	VText labelW, labelE;
	
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
    
    long getLabelWidth(){
        return labelE.getBounds(0).x;
    }
    
    void createGraphics(long wdx, long wdy, long ndx, long ndy, long edx, long edy, long sdx, long sdy, VirtualSpace vs){
        this.wdx = wdx;
	    this.wdy = wdy;
	    this.ndx = ndx;
	    this.ndy = ndy;
        this.edx = edx;
	    this.edy = edy;
	    this.sdx = sdx;
	    this.sdy = sdy;
	    labelW = new VText(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, VText.TEXT_ANCHOR_END);
	    labelN = new VTextOr(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
	    labelE = new VText(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, VText.TEXT_ANCHOR_START);
	    labelS = new VTextOr(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, -(float)Math.PI/2f, VText.TEXT_ANCHOR_START);
	    vs.addGlyph(labelW);
	    vs.addGlyph(labelN);
	    vs.addGlyph(labelE);
	    vs.addGlyph(labelS);
    }
    
    public void moveTo(long x, long y){
        labelW.moveTo(x+wdx, y+wdy);
        labelN.moveTo(x+ndx, y+ndy);
        labelE.moveTo(x+edx, y+edy);
        labelS.moveTo(x+sdx, y+sdy);
    }

}
