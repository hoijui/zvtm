/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;
import fr.inria.zvtm.nodetrix.viewer.Messages;

public class NTNode {

     String name;
    
    /* Owning matrix */
    public Matrix matrix;
    NTEdge[] outgoingEdges, incomingEdges;
    Vector <NTIntraEdgeSet> intraEdgeSets = new Vector<NTIntraEdgeSet>();
    
    /* relative offset of horizontal and vertical labels w.r.t matrix's center*/
	long wdx, wdy, ndx, ndy;
	/* Vertical label, can be null if matrix contains this node only */
	VTextOr labelN;
	/* Horizontal label */
	VText labelW;
	/* Background box*/
	VRectangle gBackgroundW, gGridV, gGridH;
	VRectangleOr gBackgroundN;
	Color backgroundColor;
	/* If this node has no matrix*/
	boolean single;
	/* The Virtual Space this NTNode belongs to - stored here to simplify interaction*/
	VirtualSpace vs;
	
	Object owner;

	private long width;
	
	/* interaction*/
	AnimationManager animManager; 
	int interactionState = NodeTrixViz.IA_STATE_DEFAULT;
	int newInteractionState = NodeTrixViz.IA_STATE_DEFAULT;
	
	
	
	public NTNode(String name){
        this.name = name;
        animManager = VirtualSpaceManager.INSTANCE.getAnimationManager();
    }
    
    
    void createGraphics(long wdx, long wdy, long ndx, long ndy, VirtualSpace vs, boolean single, Color colour){
        this.wdx = wdx;
	    this.wdy = wdy;
	    this.vs = vs;
	    this.backgroundColor = colour;
	    this.single = single;

	    labelW = new VText(-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER ,0 , 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (single) ? VText.TEXT_ANCHOR_MIDDLE : VText.TEXT_ANCHOR_END);
	    gBackgroundW = new VRectangle(0, 0, 0, 0, NodeTrixViz.CELL_SIZE/2, backgroundColor);
	    gBackgroundW.setTranslucencyValue(NodeTrixViz.NODE_BACKGROUND_TRANSLUCENCY);
	    gBackgroundW.setDrawBorder(false);
	    gBackgroundW.stick(this.labelW);
	    gBackgroundW.setOwner(this);
		vs.addGlyph(gBackgroundW);
	    vs.addGlyph(labelW);
	    
//	    gGridV = new VRectangle(vx + n.ndx, vy,0, NodeTrixViz.CELL_SIZE/2, bkg.getWidth(), NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_TRANSLUCENCY);
//		gGridV.setDrawBorder(false);
//		vs.addGlyph(gGridV);
//		gGridV.setSensitivity(false);
//		
	    
	    if (!single){
    	    this.ndx = ndx;
    	    this.ndy = ndy;
    	    labelN = new VTextOr(0, NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
    	    gBackgroundN = new VRectangleOr(0,0, 0, 0, NodeTrixViz.CELL_SIZE/2, backgroundColor, (float)Math.PI/2f);
    	    gBackgroundN.setTranslucencyValue(NodeTrixViz.NODE_BACKGROUND_TRANSLUCENCY);
    	    gBackgroundN.setDrawBorder(false);
    	    gBackgroundN.setOwner(this);
    	    gBackgroundN.stick(this.labelN);
    		vs.addGlyph(gBackgroundN);
    		vs.addGlyph(labelN);
    	    gBackgroundN.setOwner(this);

//    	    VRectangle gGridH = new VRectangle(0,0,0, bkg.getWidth(), NodeTrixViz.CELL_SIZE/2, NodeTrixViz.GRID_COLOR,  NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_TRANSLUCENCY);
//    	    gGridH.setDrawBorder(false);
//    	    vs.addGlyph(gGridH);
//    	    gGridH.setSensitivity(false);
//    	    bkg.stick(gGridH);
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
    
    //INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------
    public void setState(int newState)
    {
    	newInteractionState = newState;
    }
    
    public void perfomStateChange()
    {
    	if(newInteractionState == interactionState) return;
    	
    	if(newInteractionState == NodeTrixViz.IA_STATE_FADE) fade();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHTED) highlight();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_SELECTED) select();
	    else reset();
	    
	    interactionState = newInteractionState;
    }
    
    private void reset()
    {
        Animation a;
//    	a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.ANIM_DURATION,
//    			labelW, 1, false, SlowInSlowOutInterpolator2.getInstance(),null);	
//    	animManager.startAnimation(a, true);
//		a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.ANIM_DURATION,
//				gBackgroundW, 1, false, SlowInSlowOutInterpolator2.getInstance(),null);	
//		animManager.startAnimation(a, true);
		if(!single)
		{
//			a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.ANIM_DURATION,
//					labelN, 1, false, SlowInSlowOutInterpolator2.getInstance(),null);	
//			animManager.startAnimation(a, true);
//			a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.ANIM_DURATION,
//					gBackgroundN, 1, false, SlowInSlowOutInterpolator2.getInstance(),null);	
//			animManager.startAnimation(a, true);
			this.gBackgroundN.setColor(backgroundColor);
		}
		this.gBackgroundW.setColor(backgroundColor);
    }
    
    private void highlight()
    {
    	this.gBackgroundW.setColor(Color.yellow);
    	if(!single) this.gBackgroundN.setColor(Color.yellow);
    }
    
    private void select()
    {
    }
    
    private void fade()
    {
    }
    
    
    //GETTER/SETTER--------------------------------------------------------------------------------------------
    
    
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
    
    public void approachRelation(boolean west)
    {
    	if(single) return;
 		
 		long[] p = VirtualSpaceManager.INSTANCE.getActiveView().getVisibleRegion(VirtualSpaceManager.INSTANCE.getActiveCamera());
 		if(west){
 			if((gBackgroundW.vx - width) < p[0]){
// 				Animation a = animManager.getAnimationFactory().createGlyphTranslation(10000, gBackgroundW, new LongPoint(p[0], gBackgroundW.vy),false, SlowInSlowOutInterpolator2.getInstance(), null);
 				gBackgroundW.vx = p[0];
// 				animManager.startAnimation(a, true);
 			}
    	}else{
 			if((gBackgroundN.vy + width) > p[1]){	
 				gBackgroundN.vy = ndy - p[1];
 			}
    	}
    }
  
    public void resetPosition()
    {
		gBackgroundW.moveTo(0, 0);
		if(!single)gBackgroundN.moveTo(0, 0);
 	}
    
    public void addIntraEdgeSet(NTIntraEdgeSet ies)
    {
    	this.intraEdgeSets.add(ies);
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


}
