/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

public class NTNode {

    static int LABEL_Y_CENTERING_OFFSET = -3;

    String name;
    
    /* Owning matrix */
    Matrix matrix;
    
    NTEdge[] outgoingEdges, incomingEdges;
    
    /* relative offset of horizontal and vertical labels w.r.t matrix's center*/
	long wdx, wdy, ndx, ndy;
	/* Vertical label, can be null if matrix contains this node only */
	VTextOr labelN;
	/* Horizontal label */
	VText labelW;
	
	Object owner;
	
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
        return "N::"+name+"@"+hashCode();
    }
    
    public void setOwner(Object o){
        this.owner = o;
    }
    
    public Object getOwner(){
        return owner;
    }
    
    long getLabelWidth(){
        return (labelW == null) ? 0 : labelW.getBounds(0).x;
    }
    
    void createGraphics(long wdx, long wdy, long ndx, long ndy, VirtualSpace vs, boolean single){
        this.wdx = wdx;
	    this.wdy = wdy;
	    labelW = new VText(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (single) ? VText.TEXT_ANCHOR_MIDDLE : VText.TEXT_ANCHOR_END);
	    vs.addGlyph(labelW);
	    labelW.setOwner(this);
        if (!single){
    	    this.ndx = ndx;
    	    this.ndy = ndy;
    	    labelN = new VTextOr(0, 0, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
    	    vs.addGlyph(labelN);
    	    labelN.setOwner(this);
        }
    }
    
    public void moveTo(long x, long y){
        labelW.moveTo(x+wdx, y+wdy + LABEL_Y_CENTERING_OFFSET);
        if (labelN != null){
            labelN.moveTo(x+ndx - LABEL_Y_CENTERING_OFFSET, y+ndy);            
        }
    }
    
    public void move(long x, long y){
        labelW.move(x, y);
        if (labelN != null){
            labelN.move(x, y);
        }
    }

}
