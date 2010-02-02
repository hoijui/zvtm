/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

public class NTNode {

     String name;
    
    /* Owning matrix */
    public Matrix matrix;
    
    NTEdge[] outgoingEdges, incomingEdges;
    
    /* relative offset of horizontal and vertical labels w.r.t matrix's center*/
	long wdx, wdy, ndx, ndy;
	/* Vertical label, can be null if matrix contains this node only */
	VTextOr labelN;
	/* Horizontal label */
	VText labelW;
	/* Background box*/
	VRectangle gBackgroundW;
	VRectangleOr gBackgroundN;
	/* Grid in matrix */
	VRectangle gGridH, gGridV;
	
	/* If this node has no matrix*/
	boolean single;
	boolean odd;
	/* The Virtual Space this NTNode belongs to - stored here to simplify interaction*/
	VirtualSpace vs;
	
	Object owner;

	private long width;
	
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
//        System.out.println("+++++");
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
    
    void createGraphics(long wdx, long wdy, long ndx, long ndy, VirtualSpace vs, boolean single, Color c, boolean b){
        this.wdx = wdx;
	    this.wdy = wdy;
	    this.vs = vs;
	    labelW = new VText(-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER ,0 , 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (single) ? VText.TEXT_ANCHOR_MIDDLE : VText.TEXT_ANCHOR_END);
//	    labelW.vy = - (NodeTrixViz.CELL_SIZE - labelW.getBounds()[1] )/ 2;
	    labelW.setOwner(this);
	    
	    this.gBackgroundW = new VRectangle(0, 0, 0, 0, NodeTrixViz.CELL_SIZE/2, c);
	    this.gBackgroundW.setTranslucencyValue(NodeTrixViz.NODE_BACKGROUND_TRANSLUCENCY);
		vs.addGlyph(gBackgroundW);
	    vs.addGlyph(labelW);
		this.gBackgroundW.setDrawBorder(false);
	    this.gBackgroundW.setOwner(this);
	    this.gBackgroundW.stick(this.labelW);
	    this.single = single;
	    this.odd = b;
	    if (!single){
    	    this.ndx = ndx;
    	    this.ndy = ndy;
    	    labelN = new VTextOr(0, NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
    	    labelN.setOwner(this);
//    	    labelN.vy = -(NodeTrixViz.CELL_SIZE - labelN.getBounds()[1] )/ 2;
    	    this.gBackgroundN = new VRectangleOr(0,0, 0, 0, NodeTrixViz.CELL_SIZE/2, c, (float)Math.PI/2f);
    		vs.addGlyph(gBackgroundN);
    		vs.addGlyph(labelN);
    		this.gBackgroundN.stick(this.labelN);
    	    this.gBackgroundN.setTranslucencyValue(NodeTrixViz.NODE_BACKGROUND_TRANSLUCENCY);
    		this.gBackgroundN.setDrawBorder(false);
    		this.gBackgroundN.setOwner(this);
		
    		//applying grid
    		if(this.odd)
    		{
	    		this.gGridV = new VRectangle(0,0,0, NodeTrixViz.CELL_SIZE/2, this.matrix.bkg.getWidth(), NodeTrixViz.GRID_COLOR);
				gGridV.setDrawBorder(false);
				gGridV.setTranslucencyValue( NodeTrixViz.GRID_TRANSLUCENCY);
	    	    vs.addGlyph(gGridV);
	    	    gGridV.setSensitivity(false);
	    	    this.gBackgroundN.stick(gGridV);
	    	  
	    	    this.gGridH = new VRectangle(0,0,0, this.matrix.bkg.getWidth(), NodeTrixViz.CELL_SIZE/2, NodeTrixViz.GRID_COLOR);
				gGridH.setDrawBorder(false);
				gGridH.setTranslucencyValue( NodeTrixViz.GRID_TRANSLUCENCY);
				vs.addGlyph(gGridH);
	    	    gGridH.setSensitivity(false);
				this.gBackgroundW.stick(gGridH);
    		}   
    	}
    }
    
    public void moveTo(long x, long y){
//        labelW.moveTo(x+wdx, y+wdy + LABEL_Y_CENTERING_OFFSET);
        gBackgroundW.moveTo(x+wdx, y+wdy);
//        if (labelN != null)			labelN.moveTo(x+ndx - LABEL_Y_CENTERING_OFFSET, y+ndy);            
        if (gBackgroundN != null)	gBackgroundN.moveTo(x+ndx, y+ndy);            
    }
    
    public void move(long x, long y){
//        labelW.move(x, y);
        gBackgroundW.move(x,y);
//        if (labelN != null)		    labelN.move(x, y);
        if (gBackgroundN != null)	gBackgroundN.move(x,y);            
    }

    /**Method that sets the background box of this node according to the maximal text length of all nodes in
     * the matrix. A gradiant is applied according to the position of the node in the list.
     */
	public void setBackgroundBox(long maxLength) {
		this.width = maxLength;
		this.gBackgroundW.setWidth(maxLength/2);
		if (!this.single){
			this.gBackgroundW.move(-maxLength/2, 0);
			this.labelW.move(maxLength/2, 0);
			gBackgroundN.setWidth(maxLength/2);
			this.gBackgroundN.move(0, maxLength/2);
			this.labelN.move(0,-maxLength/2);
			
			if(odd){
				this.gGridH.vx += this.gBackgroundW.getWidth() + this.matrix.bkg.getWidth();
				this.gGridV.vy -= (this.gBackgroundN.getWidth() + this.matrix.bkg.getWidth());
			}
	  	    

		}
	}

	public int getBoxWidth(boolean west) {
//		return west ? this.gBackgroundW.getBounds().length : ((this.gBackgroundN != null) ? this.gBackgroundN.getBounds().length : 0);
		return (int)this.width/2;
	}

	public long getWidth() 
	{
		return this.width/2;
	}

}
