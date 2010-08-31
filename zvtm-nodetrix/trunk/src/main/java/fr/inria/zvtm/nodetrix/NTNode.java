/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;
import fr.inria.zvtm.nodetrix.lll.LinLogNode;

public class NTNode extends LinLogNode{

     String name;
    
    /* Owning matrix */
    public Matrix matrix;
    Vector<NTEdge> outgoingEdges, incomingEdges;
    
    /* relative offset of horizontal and vertical labels w.r.t matrix's center*/
	double wdx, wdy, ndx, ndy;
	/* stores the matrix centre coordinates*/
	double mx, my; 
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
	
	private Object owner;
	
	/**Stores the half width, since double width is never used */
	private double widthHalf = 0;
	
	/* interaction*/
	AnimationManager animManager; 
	int interactionState = NodeTrixViz.IA_STATE_DEFAULT;
	int newInteractionState = NodeTrixViz.IA_STATE_DEFAULT;
	/* which labels of the node should be affected by the next state */
	private boolean affectWest = true;
	private boolean affectNorth = true;
	
	/*Name of the the group this node belongs to, null if no group is assigned*/
	private String group = null;
	private boolean reDraw = true;

	private double yOld;

	private double xOld;
	
	
	public NTNode(String name){
		super(name, 1);
        this.name = name;
        animManager = VirtualSpaceManager.INSTANCE.getAnimationManager();
        outgoingEdges = new Vector<NTEdge>();
      	incomingEdges = new Vector<NTEdge>();
    }
    
	
    
    void createGraphics(double wdx, double wdy, double ndx, double ndy, VirtualSpace vs, boolean single, Color colour)
    {
    	cleanGraphics(vs);
    	
        this.wdx = wdx;
	    this.wdy = wdy;
	    this.ndx = ndx;
	    this.ndy = ndy;
	    this.vs = vs;
	    this.backgroundColor = colour;
	    this.single = single;
	    
    	    gBackgroundW = new VRectangle(0, 0, 0, 0, NodeTrixViz.CELL_SIZE, backgroundColor);
	    	labelW = new VText(-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER ,0 , 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (single) ? VText.TEXT_ANCHOR_MIDDLE : VText.TEXT_ANCHOR_END);
	    	gBackgroundWSensitive = new VRectangle(2, 2, 0, 0, NodeTrixViz.CELL_SIZE -4, Color.red);
            vs.addGlyph(gBackgroundW);
	    	vs.addGlyph(labelW);
	    	vs.addGlyph(gBackgroundWSensitive);
            gBackgroundW.setDrawBorder(false);
	    	labelW.setOwner(this);
	    	gBackgroundWSensitive.setOwner(this);
	    	gBackgroundWSensitive.setTranslucencyValue(0f);
	    	gBackgroundW.stick(this.labelW);
            gBackgroundW.stick(this.gBackgroundWSensitive);
	    	
		    if (!single){
	    	    gBackgroundN = new VRectangleOr(0,0, 0, 0, NodeTrixViz.CELL_SIZE, backgroundColor, (float)Math.PI/2f);
	    	    labelN = new VTextOr(0, NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0, NodeTrixViz.MATRIX_STROKE_COLOR, name, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
	    		gBackgroundNSensitive = new VRectangleOr(2, 2, 0, 0, NodeTrixViz.CELL_SIZE -4,  Color.red,  (float)Math.PI/2f);
	    		vs.addGlyph(gBackgroundN);
	    		vs.addGlyph(labelN);
	    		vs.addGlyph(gBackgroundNSensitive);
	    	    gBackgroundN.setDrawBorder(false);
	    	    gBackgroundNSensitive.setTranslucencyValue(0f);
	    	    gBackgroundN.stick(this.labelN);
	    	    gBackgroundN.stick(this.gBackgroundNSensitive);
	    		labelN.setOwner(this);
	    	    gBackgroundNSensitive.setOwner(this);
		   }
    }
    
    public void moveTo(double mx, double my){
        gBackgroundW.moveTo(mx+wdx, my+wdy);
        this.mx = mx; this.my = my;
        if (gBackgroundN != null)	gBackgroundN.moveTo(mx+ndx, my+ndy);            
    }
	
    /** Moves booth labels to differentLocations along the matrix side.
     * This method is used for label reordering. 
     */
	public void repositionLabels(double wdy, double ndx){
		gBackgroundW.move(0, wdy - this.wdy);
		this.wdy = wdy;
		if(!single) {
			gBackgroundN.move(ndx - this.ndx, 0);
			this.ndx = ndx;
		}
	}
	

    public void matrixMoved(double mx, double my){
    	this.mx += mx; 
    	this.my += my;
    	gBackgroundW.move(mx, my);
        if (gBackgroundN != null)	gBackgroundN.move(mx,my);        
    }
    
    //INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------
    public void setNewState(int newState, boolean west, boolean north)
    {
    	newInteractionState = newState;
    	this.affectNorth = affectNorth || north;
    	this.affectWest = affectWest || west;
    }
    
    /** shifts the western labels to the west
     * @param xNew - absolute value in virtual space
     * @param animated - animated shifting, if true;
     **/
    public void shiftWesternLabels(double xNew, boolean animated)
    {
    	xOld = gBackgroundW.vx;
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundW, 
    						new Point2D.Double(xNew, gBackgroundW.vy ),
    						false, 
    						SlowInSlowOutInterpolator2.getInstance(), 
    						null),
    						true);
    	}else{
    		gBackgroundW.move(xNew - gBackgroundW.vx, 0);
    	}
//    	gBackgroundW.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
    }
    
    /** shifts the northern labels to the west
     * @param yNew - absolute value in virtual space
     * @param animated - animated shifting, if true;
     **/
    public void shiftNorthernLabels(double yNew, boolean animated)
    {
    	yOld = gBackgroundN.vy;
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundN, 
    						new Point2D.Double(gBackgroundN.vx, yNew),
    						false, 
    						SlowInSlowOutInterpolator2.getInstance(), 
    						null),
    						true);
      	}else{
      		gBackgroundN.move(0, yNew - gBackgroundN.vy);
        }
//      gBackgroundN.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
    }
    
    
    /** Sets the northern labels to their initial position
     **/
    public void resetNorthernLabels(boolean animated)
    {
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundN, 
    						new Point2D.Double(gBackgroundN.vx, yOld),
    						false, 
    						SlowInSlowOutInterpolator2.getInstance(), 
    						null),
    						true);
    	}else{
      		gBackgroundN.move(0, yOld - gBackgroundN.vy);
        }
    	gBackgroundN.setTranslucencyValue(1);
    }
    
    /** Sets the northern labels to their initial position
     **/
    public void resetWesternLabels(boolean animated)
    {
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
	        		.createGlyphTranslation(
	        			NodeTrixViz.DURATION_NODEMOVE,
	        			gBackgroundW, 
	        			new Point2D.Double(xOld, gBackgroundW.vy),
	        			false, 
	        			SlowInSlowOutInterpolator2.getInstance(), 
	        			null),
        		true);
    	}else{
    		gBackgroundW.move(xOld - gBackgroundW.vx, 0);
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
		
    	if(affectNorth && !single){
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
	public void setBackgroundBox(double maxLength) {
		if(widthHalf == 0) this.widthHalf = maxLength/2;
		wdx -= widthHalf;
		ndy += widthHalf;
		this.gBackgroundW.setWidth(2*widthHalf);

		gBackgroundWSensitive.setWidth(2*widthHalf-4);
		if (!this.single){
			this.gBackgroundW.move(-widthHalf, 0);
			this.labelW.move(widthHalf, 0);
			gBackgroundN.setWidth(widthHalf*2);
			gBackgroundNSensitive.setWidth(2*widthHalf-4);
			this.gBackgroundN.move(0, widthHalf);
			this.labelN.move(0,-widthHalf);
		}
	}

	public int getBoxWidth(boolean west) {
//		return west ? this.gBackgroundW.getBounds().length : ((this.gBackgroundN != null) ? this.gBackgroundN.getBounds().length : 0);
		return (int)this.widthHalf;
	}

	public double getWidth() 
	{
		return this.widthHalf;
	}
    public void addOutgoingEdge(NTEdge e){
    	outgoingEdges.add(e);
    }
    
    public void addIncomingEdge(NTEdge e){
//    	if(e instanceof NTIntraEdge){internalRelations.add(e);}
    	incomingEdges.add(e);
    }
    
//    public void addIntraEdgeSet(NTIntraEdgeSet ies)
//    {
//    	this.intraEdgeSets.add(ies);
//    }
    
    /**
     *@return null if empty
     */
    public Vector<NTEdge> getIncomingEdges(){
    	return incomingEdges;
    }
    
    /**
     *@return null if empty
     */
    public Vector<NTEdge> getOutgoingEdges(){
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
    
    double getLabelWidth(){
    	return (labelW == null) ? 0 : labelW.getBounds(0).x;
    }

    public boolean isParentMatrixSingle(){
        return this.single;
    }
    
//	public Vector<NTIntraEdgeSet> getIntraEdgeSets() {
//		return this.intraEdgeSets;
//	}


//	public Vector<NTEdge> getInternalRelations() {
//		return internalRelations;
//	}


	public int getDegree() {
		int degree = 0;
		for(NTEdge e : outgoingEdges){
			if(e.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE){
				degree++;
			}
		}
		return degree;
	}


	public void removeOutgoingEdge(NTEdge ee) {
		outgoingEdges.remove(ee);	
	}
	public void removeIncomingEdge(NTEdge ee) {
//		if(ee instanceof NTIntraEdge) internalRelations.remove(ee);
		incomingEdges.remove(ee);	
	}
	
	public void setGroup(String name){
//		if(name.equals("Thing"))
		group = name;
	}
	public String getGroupName(){
		return group;
	}



	void cleanGraphics(VirtualSpace vs) {
		if(this.gBackgroundW != null) vs.removeGlyph(this.gBackgroundW);
		if(this.labelW != null) vs.removeGlyph(this.labelW);
		if(this.gBackgroundN != null) vs.removeGlyph(this.gBackgroundN);
		if(this.labelN != null) vs.removeGlyph(this.labelN);
	}



	public void repositionRelations() {
		for(NTEdge e : this.outgoingEdges){
			e.updatePosition();
		}
//		for(NTEdge e : internalRelations){ e.updatePosition();}
////		for(NTIntraEdgeSet es : this.intraEdgeSets){
////			es.updatePosition();
////		}
		
	}

	public void cleanInternalRelations() {
		for(NTEdge e : outgoingEdges){
			if(e.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE){
			e.cleanGraphics();
			}
		}
			
//		for(NTIntraEdgeSet ie : intraEdgeSets){
//			ie.cleanGraphics();
//		}
//		intraEdgeSets = new Vector<NTIntraEdgeSet>();
	}


}
