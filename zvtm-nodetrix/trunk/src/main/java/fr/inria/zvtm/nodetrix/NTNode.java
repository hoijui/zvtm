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
	/* stores the matrix centre coordinates*/
	long mx, my; 
	/* Vertical label, can be null if matrix contains this node only */
	VTextOr labelN;
	/* Horizontal label */
	VText labelW;
	/* Background box*/
	VRectangle gBackgroundW;
	VRectangleOr gBackgroundN;
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

	private boolean westToScreen = false;
	private boolean northToScreen = false;

	private long surfStop_x = 100000000, surfStop_y = -10000000;

	
	
	
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
//		vs.onTop(gBackgroundW);
	    vs.addGlyph(labelW);
	    
	    
	    if (!single){
    	    this.ndx = ndx;
    	    this.ndy = ndy;
    	    labelN = new VTextOr(0, NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
    	    gBackgroundN = new VRectangleOr(0,0, 0, 0, NodeTrixViz.CELL_SIZE/2, backgroundColor, (float)Math.PI/2f);
    	    gBackgroundN.setTranslucencyValue(NodeTrixViz.NODE_BACKGROUND_TRANSLUCENCY);
    	    gBackgroundN.setDrawBorder(false);
    	    gBackgroundN.setOwner(this);
    	    gBackgroundN.stick(this.labelN);
    	    gBackgroundN.setOwner(this);
    		vs.addGlyph(gBackgroundN);
//    		vs.onTop(gBackgroundN);
    		vs.addGlyph(labelN);


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
	    else if(newInteractionState == NodeTrixViz.IA_STATE_SCREENSURF) surfToScreen();
	    else reset();
	    
	    interactionState = newInteractionState;
    }
    
    public void reset()
    {
		//COLOR
		gBackgroundW.setColor(backgroundColor);
		gBackgroundW.setTranslucencyValue(1);
		
		northToScreen = false;
		westToScreen = false;
		surfStop_x = 100000000; 
		surfStop_y = -100000000;
		
		if(!single)
		{
			//POSITIONS
			Animation a = animManager.getAnimationFactory()
			.createGlyphTranslation(NodeTrixViz.DURATION_NODEMOVE, gBackgroundW, 
					new LongPoint(mx + wdx,my + wdy),
					false, 
					SlowInSlowOutInterpolator2.getInstance(), 
					null);
			animManager.startAnimation(a, true);
			
			a = animManager.getAnimationFactory()
			.createGlyphTranslation(NodeTrixViz.DURATION_NODEMOVE, gBackgroundN, 
					new LongPoint(mx + ndx,my + ndy),
					false, 
					SlowInSlowOutInterpolator2.getInstance(), 
					null);
			animManager.startAnimation(a, true);
			gBackgroundN.setColor(backgroundColor);
			gBackgroundN.setTranslucencyValue(1);
		}
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
    
    
    /**Method that is called to force the labels of this Node come onto sceen when user clicks and holds
     * on a relation in the matrix.
     * @param tail - if true, the western label is moved, if false, the northern one.
     */
    public void surfToScreen()
    {
    	System.out.println("CHECK SURFING " + this.name);
    	if(single) return;	
    	long[] p = VirtualSpaceManager.INSTANCE.getActiveView().getVisibleRegion(VirtualSpaceManager.INSTANCE.getActiveCamera());
    	if(westToScreen){
    		if((mx + wdx - widthHalf) < p[0]){
    			long xNew = Math.min(p[0] + widthHalf, this.surfStop_x - widthHalf);
    			Animation a = animManager.getAnimationFactory()
 					.createGlyphTranslation(NodeTrixViz.DURATION_NODEMOVE, gBackgroundW, 
 							new LongPoint(xNew, gBackgroundW.vy),
 							false, 
 							SlowInSlowOutInterpolator2.getInstance(), 
 							null);
 				animManager.startAnimation(a, true);
    		}
    	}
    	if(northToScreen){
    		
    		if((my + ndy + widthHalf) > p[1]){	
    			long yNew = Math.max(p[1] - widthHalf, this.surfStop_y + widthHalf);
    			Animation a = animManager.getAnimationFactory()
 					.createGlyphTranslation(NodeTrixViz.DURATION_NODEMOVE, gBackgroundN, 
 							new LongPoint(gBackgroundN.vx, yNew),
 							false, 
 							SlowInSlowOutInterpolator2.getInstance(), 
 							null);
 				animManager.startAnimation(a, true);
 	    	}
    	}
    }

    
    public void onTop() {
    	vs.onTop(gBackgroundW);
    	vs.onTop(labelW);
    	if(!single){
    		vs.onTop(gBackgroundN);
    		vs.onTop(labelN);
    	}
    }
    
    
    //GETTER/SETTER--------------------------------------------------------------------------------------------
    
    public void setTailToScreen(long x, long y){
    	westToScreen = true;
    	this.surfStop_x = Math.min(this.surfStop_x, x - NodeTrixViz.CELL_SIZE_HALF);
    	this.surfStop_y = Math.max(this.surfStop_y, y + NodeTrixViz.CELL_SIZE_HALF);
    }
    public void setHeadToScreen(long x, long y){
    	northToScreen = true;
    	this.surfStop_x = Math.min(this.surfStop_x, x - NodeTrixViz.CELL_SIZE_HALF);
    	this.surfStop_y = Math.max(this.surfStop_y, y + NodeTrixViz.CELL_SIZE_HALF);
    }
    
    /**Method that sets the background box of this node according to the maximal text length of all nodes in
     * the matrix. A gradiant is applied according to the position of the node in the list.
     */
	public void setBackgroundBox(long maxLength) {
		this.widthHalf = maxLength/2;
		wdx -= widthHalf;
		ndy += widthHalf;
		this.gBackgroundW.setWidth(widthHalf);
		if (!this.single){
			this.gBackgroundW.move(-widthHalf, 0);
			this.labelW.move(widthHalf, 0);
			gBackgroundN.setWidth(widthHalf);
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

}
