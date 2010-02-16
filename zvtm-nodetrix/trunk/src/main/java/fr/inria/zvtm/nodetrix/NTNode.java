/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

public class NTNode {

     String name;
    
    /* Owning matrix */
    public Matrix matrix;
    NTEdge[] outgoingEdges, incomingEdges;
    private Vector <NTIntraEdgeSet> intraEdgeSets = new Vector<NTIntraEdgeSet>();
    
    /* relative offset of horizontal and vertical labels w.r.t matrix's center*/
	long wdx, wdy, ndx, ndy;
	/* stores the matrix centre coordinates*/
	long mx, my; 
	/* Vertical label, can be null if matrix contains this node only */
	VTextOr labelN;
	/* Horizontal label */
	VText labelW;
	/* Background box*/
	VRectangle gBackgroundW, gBackgroundWSensitive;
	VRectangleOr gBackgroundN, gBackgroundNSensitive;
	Color backgroundColor;
	/* If this node has no matrix*/
	boolean single;
	/* The Virtual Space this NTNode belongs to - stored here to simplify interaction*/
	VirtualSpace vs;
	
	Object owner;
	
	/**Stores the half width, since double width is never used */
	private long widthHalf;
	
	/* interaction*/
	AnimationManager animManager; 
	int interactionState = NodeTrixViz.IA_STATE_DEFAULT;
	int newInteractionState = NodeTrixViz.IA_STATE_DEFAULT;
	/* which labels of the node should be affected by the next state */
	private boolean affectWest = false;
	private boolean affectNorth = false;
	
	
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
	    gBackgroundW.setDrawBorder(false);
	    gBackgroundW.stick(this.labelW);
	    vs.addGlyph(gBackgroundW);
	    vs.addGlyph(labelW);

	    gBackgroundWSensitive = new VRectangle(2, 2, 0, 0, NodeTrixViz.CELL_SIZE/2 -2, Color.red);
	    gBackgroundWSensitive.setTranslucencyValue(0f);
	    gBackgroundW.stick(this.gBackgroundWSensitive);
	    gBackgroundWSensitive.setOwner(this);
		vs.addGlyph(gBackgroundWSensitive);
	    
	    
	    if (!single){
    	    this.ndx = ndx;
    	    this.ndy = ndy;
    	    labelN = new VTextOr(0, NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
    	    gBackgroundN = new VRectangleOr(0,0, 0, 0, NodeTrixViz.CELL_SIZE/2, backgroundColor, (float)Math.PI/2f);
    	    gBackgroundN.setDrawBorder(false);
    	    gBackgroundN.stick(this.labelN);
    		vs.addGlyph(gBackgroundN);
    		vs.addGlyph(labelN);
    		gBackgroundNSensitive = new VRectangleOr(2, 2, 0, 0, NodeTrixViz.CELL_SIZE/2 -2,  Color.red,  (float)Math.PI/2f);
    	    gBackgroundNSensitive.setTranslucencyValue(0f);
    	    gBackgroundN.stick(this.gBackgroundNSensitive);
    	    gBackgroundNSensitive.setOwner(this);
    		vs.addGlyph(gBackgroundNSensitive);
	    }
    }
    
    public void moveTo(long x, long y){
        gBackgroundW.moveTo(x+wdx, y+wdy);
        mx = x; my = y;
        if (gBackgroundN != null)	gBackgroundN.moveTo(x+ndx, y+ndy);            
    }
    
    public void move(long x, long y){
    	mx += x; my += y;
    	gBackgroundW.move(x,y);
        if (gBackgroundN != null)	gBackgroundN.move(x,y);        
    }
    
    //INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------
    public void setState(int newState, boolean west, boolean north)
    {
    	newInteractionState = newState;
    	this.affectNorth = affectNorth || north;
    	this.affectWest = affectWest || west;
    }
    
    public void shiftWest(long xNew, boolean animated)
    {
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundW, 
    						new LongPoint(xNew,	gBackgroundW.vy ),
    						false, 
    						SlowInSlowOutInterpolator2.getInstance(), 
    						null),
    						true);
    	}else{
    		gBackgroundW.move(xNew - gBackgroundW.vx, 0);
    	}
    	gBackgroundW.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
    }
    
    public void shiftNorth(long yNew, boolean animated)
    {
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundN, 
    						new LongPoint(gBackgroundN.vx, yNew),
    						false, 
    						SlowInSlowOutInterpolator2.getInstance(), 
    						null),
    						true);
      	}else{
      		gBackgroundN.move(0, yNew - gBackgroundN.vy);
        }
      	gBackgroundN.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
    }
    
    public void surfBackNorth(long yNew, boolean animated)
    {
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundN, 
    						new LongPoint(gBackgroundN.vx, yNew),
    						false, 
    						SlowInSlowOutInterpolator2.getInstance(), 
    						null),
    						true);
    	}else{
      		gBackgroundN.move(0, yNew - gBackgroundN.vy);
        }
    	gBackgroundN.setTranslucencyValue(1);
    }
    
    public void surfBackWest(long xNew, boolean animated)
    {
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
	        		.createGlyphTranslation(
	        			NodeTrixViz.DURATION_NODEMOVE,
	        			gBackgroundW, 
	        			new LongPoint(xNew, gBackgroundW.vy),
	        			false, 
	        			SlowInSlowOutInterpolator2.getInstance(), 
	        			null),
        		true);
    	}else{
    		gBackgroundW.move(xNew - gBackgroundW.vx, 0);
    	}
    	gBackgroundW.setTranslucencyValue(1);
    }

    
    
    public void perfomStateChange()
    {
//    	if(newInteractionState == interactionState) return;

    	if(newInteractionState == NodeTrixViz.IA_STATE_FADE) fade();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHTED) highlight(NodeTrixViz.MATRIX_NODE_HIGHLIGHT_COLOR);
	    else if(newInteractionState == NodeTrixViz.IA_STATE_SELECTED) select();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_RELATED) highlight(NodeTrixViz.MATRIX_NODE_RELATED_COLOR);
	    else reset();
	    
        interactionState = newInteractionState;
    }
    
    public void reset()
    {
		//COLOR
		gBackgroundW.setColor(backgroundColor);
		gBackgroundW.setTranslucencyValue(1);
		
//		if(!matrix.isNodesVisibleNorth()) gBackgroundN.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
//		if(!matrix.isNodesVisibleWest()) gBackgroundW.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
		
		affectNorth = false;
		affectWest = false;
		
		if(!single)
		{
			gBackgroundN.setColor(backgroundColor);
			gBackgroundN.setTranslucencyValue(1);
		}
		
    }
    
    private void highlight(Color c)
    {
//    	boolean oldNorth = this.permanentNorth;
//    	boolean oldWest = this.permanentWest;
		long[] p = VirtualSpaceManager.INSTANCE.getActiveView().getVisibleRegion(VirtualSpaceManager.INSTANCE.getActiveCamera());
		
    	if(affectNorth){
    		this.gBackgroundN.setColor(c);
//    		if(!matrix.isNodesVisibleNorth()) this.shiftNorth(p[1] - Math.min(widthHalf, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2), true);
    	}
    	if(affectWest || single){
    		this.gBackgroundW.setColor(c);
//    		if(!matrix.isNodesVisibleWest()) this.shiftWest(p[0] + Math.min(widthHalf, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2), true);
    	}
    	
//    	this.permanentNorth = oldNorth;
//    	this.permanentWest = oldWest;
    }
    
    private void select()
    {
    }
    
    private void fade()
    {
//    	if(affectWest || single){
//    		this.gBackgroundW.setTranslucencyValue(.6f);
//    	}
//    	if(affectNorth){
//    		this.gBackgroundN.setTranslucencyValue(.6f);
//    	}
    }
    
    public void onTop() {
    	vs.onTop(gBackgroundW);
    	vs.onTop(gBackgroundWSensitive);
    	vs.onTop(labelW);
    	if(!single){
    		vs.onTop(gBackgroundN);
    		vs.onTop(gBackgroundNSensitive);
    		vs.onTop(labelN);
    	}
    }
    
    public void isVisible(){
    	
    }
    
    //GETTER/SETTER--------------------------------------------------------------------------------------------
    
    /**Method that sets the background box of this node according to the maximal text length of all nodes in
     * the matrix. A gradiant is applied according to the position of the node in the list.
     */
	public void setBackgroundBox(long maxLength) {
		this.widthHalf = maxLength/2;
		wdx -= widthHalf;
		ndy += widthHalf;
		this.gBackgroundW.setWidth(widthHalf);
		gBackgroundWSensitive.setWidth(widthHalf-2);
		if (!this.single){
			this.gBackgroundW.move(-widthHalf, 0);
			this.labelW.move(widthHalf, 0);
			gBackgroundN.setWidth(widthHalf);
			gBackgroundNSensitive.setWidth(widthHalf-2);
			this.gBackgroundN.move(0, widthHalf);
			this.labelN.move(0,-widthHalf);
		}
	}

	public int getBoxWidth(boolean west) {
//		return west ? this.gBackgroundW.getBounds().length : ((this.gBackgroundN != null) ? this.gBackgroundN.getBounds().length : 0);
		return (int)this.widthHalf;
	}

	public long getWidth() 
	{
		return this.widthHalf;
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


	public Vector<NTIntraEdgeSet> getIntraEdgeSets() {
		return this.intraEdgeSets;
	}
    
    

}
