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
import fr.inria.zvtm.nodetrix.lll.LinLogNode;

/**
 * 
 * @author emmanuel pietriga
 * @author benjamin bach
 */
public class NTNode extends LinLogNode{

     private String name;
    
    /** Owning matrix */
    Matrix matrix;
    Vector<NTEdge> outgoingEdges, incomingEdges;
    /** relative offset of horizontal and vertical labels w.r.t matrix's center*/
	long wdx, wdy, ndx, ndy;
	/** stores the matrix centre coordinates*/
	long matrixX, matrixY; 
	/** Vertical label, can be null if matrix contains this node only */
	VTextOr labelN;
	/** Horizontal label */
	VText labelW;
	/**Stores the half width of the whole label (not only the text!), since double width is never used */
	private long widthHalf = 0;
	private long heightHalf = 0;
	/**Width of the text glyph only*/
	private long textWidth = -1; //-1 means that it is not yet set.
	/** Background box*/
	VRectangle gBackgroundW, gSensitiveW;
	VRectangleOr gBackgroundN, gSensitiveN;
	Color backgroundColor;
	/** If this node has no matrix*/
	boolean single;
	/** The Virtual Space this NTNode belongs to - stored here to simplify interaction*/
	VirtualSpace vs;
	
	private Object owner;
	
	/* interaction*/
	AnimationManager animManager; 
	int interactionState = NodeTrixViz.IA_STATE_DEFAULT;
	int newInteractionState = NodeTrixViz.IA_STATE_DEFAULT;
	/* which labels of the node should be affected by the next state */
	private boolean affectWest = true;
	private boolean affectNorth = true;
	
	/*Name of the the group this node belongs to, null if no group is assigned*/
	private String group = null;
//	private boolean reDraw = true;

	private long yOld;

	private long xOld;
	
	private NTInfoBox infoBox;
	
	public NTNode(String name){
		super(name, 1);
		this.name =  name;
        animManager = VirtualSpaceManager.INSTANCE.getAnimationManager();
        outgoingEdges = new Vector<NTEdge>();
      	incomingEdges = new Vector<NTEdge>();
    }
    
	
    
    void createGraphics(long wdx, long wdy, long ndx, long ndy, VirtualSpace vs, boolean single, Color colour)
    {
    	cleanGraphics(vs);
    	
        this.wdx = wdx;
	    this.wdy = wdy;
	    this.ndx = ndx;
	    this.ndy = ndy;
	    this.vs = vs;
	    this.backgroundColor = colour;
	    this.single = single;
	    
	    	labelW = new VText(-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER ,0 , 0, ProjectColors.NODE_TEXT[ProjectColors.COLOR_SCHEME], getName(), (single) ? VText.TEXT_ANCHOR_MIDDLE : VText.TEXT_ANCHOR_END);
	    	labelW.setSensitivity(false);
	    	gBackgroundW = new VRectangle(0, 0, 0, 0, NodeTrixViz.CELL_SIZE/2, backgroundColor);
	    	gBackgroundW.setDrawBorder(false);
	    	gBackgroundW.setSensitivity(false);
	    	gBackgroundW.stick(this.labelW);
	    	vs.addGlyph(gBackgroundW);
	    	vs.addGlyph(labelW);
//	    	System.out.println("[NTNODE] LABELWIDTH " + getName() +", "+ labelW.getBounds(0).x);
	    	
	    	gSensitiveW = new VRectangle(2, 2, 0, 0, NodeTrixViz.CELL_SIZE/2 -2, Color.red);
	    	gSensitiveW.setVisible(false);
	    	gBackgroundW.stick(this.gSensitiveW);
	    	gSensitiveW.setOwner(this);
	    	vs.addGlyph(gSensitiveW);
	    
		    if (!single){
	    	    labelN = new VTextOr(0, NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0, ProjectColors.NODE_TEXT[ProjectColors.COLOR_SCHEME], getName(), (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
    	    	labelN.setSensitivity(false);
	    	    gBackgroundN = new VRectangleOr(0,0, 0, 0, NodeTrixViz.CELL_SIZE/2, backgroundColor, (float)Math.PI/2f);
	    	    gBackgroundN.setDrawBorder(false);
	    	    gBackgroundN.setSensitivity(false);
	    	    gBackgroundN.stick(this.labelN);
	    		vs.addGlyph(gBackgroundN);
	    		vs.addGlyph(labelN);
	    		gSensitiveN = new VRectangleOr(2, 2, 0, 0, NodeTrixViz.CELL_SIZE/2 -2,  Color.red,  (float)Math.PI/2f);
	    	    gSensitiveN.setVisible(false);
	    	    gBackgroundN.stick(this.gSensitiveN);
	    	    gSensitiveN.setOwner(this);
	    		vs.addGlyph(gSensitiveN);
		   }
    }
    
    public void moveTo(long mx, long my){
        gBackgroundW.moveTo(mx+wdx, my+wdy);
        this.matrixX = mx; this.matrixY = my;
        if (gBackgroundN != null)	gBackgroundN.moveTo(mx+ndx, my+ndy);            
    }
	
    /** Moves booth labels to differentLocations along the matrix side.
     * This method is used for label reordering. 
     */
	public void updateLabelPosition(long wdy, long ndx){
		gBackgroundW.move(0, wdy - this.wdy);
		this.wdy = wdy;
		if(!single) {
			gBackgroundN.move(ndx - this.ndx, 0);
			this.ndx = ndx;
		}
	}
	

    public void matrixMoved(long dmx, long dmy){
    	this.matrixX += dmx; 
    	this.matrixY += dmy;
    	gBackgroundW.move(dmx, dmy);
//    	this.yOld += dmy;
    	
        if (!single){
        	gBackgroundN.move(dmx,dmy);        
//        	this.xOld += dmy;
        	
        }
    }
    
    //INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------INTERACTION------------------
    public void setNewInteractionState(int newState, boolean west, boolean north)
    {
    	newInteractionState = newState;
    	this.affectNorth = affectNorth || north;
    	this.affectWest = affectWest || west;
    }
    
    /** shifts the western labels to the west
     * @param xNew - absolute value in virtual space
     * @param animated - animated shifting, if true;
     **/
    public void shiftWesternLabels(long xNew, boolean animated)
    {
    	if(single) return;
    	xOld = gBackgroundW.vx;
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundW, 
    						new LongPoint(xNew,	gBackgroundW.vy),
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
    public void shiftNorthernLabels(long yNew, boolean animated)
    {
    	if(single) return;
    	yOld = gBackgroundN.vy;
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
//      gBackgroundN.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
    }
    
    
    /** Sets the northern labels to their initial position
     **/
    public void resetNorthernLabels(boolean animated)
    {
    	if(single) return;
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
    				.createGlyphTranslation(
    						NodeTrixViz.DURATION_NODEMOVE,
    						gBackgroundN, 
    						new LongPoint(gBackgroundN.vx, yOld),
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
    	if(single){
    		return;
    	}
    	if(animated){
    		animManager.startAnimation(animManager.getAnimationFactory()
	        		.createGlyphTranslation(
	        			NodeTrixViz.DURATION_NODEMOVE,
	        			gBackgroundW, 
	        			new LongPoint(xOld, gBackgroundW.vy),
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
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHT) highlight(ProjectColors.HIGHLIGHT[ProjectColors.COLOR_SCHEME]);
	    else if(newInteractionState == NodeTrixViz.IA_STATE_SELECTED) select();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_RELATED) highlight(ProjectColors.HIGHLIGHT_RELATED[ProjectColors.COLOR_SCHEME]);
	    else reset();
	    
        interactionState = newInteractionState;
    }
    
    public void reset()
    {
//    	System.out.println("[NTNODE] RESET " + this.name);
    	
		//COLOR
		gBackgroundW.setColor(backgroundColor);
		gBackgroundW.setTranslucencyValue(1);
		labelW.setColor(ProjectColors.NODE_TEXT[ProjectColors.COLOR_SCHEME]);
		
//		if(!matrix.isNodesVisibleNorth()) gBackgroundN.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
//		if(!matrix.isNodesVisibleWest()) gBackgroundW.setTranslucencyValue(NodeTrixViz.MATRIX_NODE_BKG_TRANSLUCENCY);
		
		if(!single)
		{
			gBackgroundN.setColor(backgroundColor);
			gBackgroundN.setTranslucencyValue(1);
			labelN.setColor(ProjectColors.NODE_TEXT[ProjectColors.COLOR_SCHEME]);
		}
		affectNorth = false;
		affectWest = false;
	}
    
    
    private void highlight(Color c)
    {
    	System.out.println("[NTNODE] HIGHLIGHT " + this.name);

//    	boolean oldNorth = this.permanentNorth;
//    	boolean oldWest = this.permanentWest;
		
    	if(affectNorth && !single){
    		this.gBackgroundN.setColor(c);
    		this.labelN.setColor(Color.black);
//    		if(!matrix.isNodesVisibleNorth()) this.shiftNorth(p[1] - Math.min(widthHalf, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2), true);
    	}
    	if(affectWest || single){
    		this.gBackgroundW.setColor(c);
    		this.labelW.setColor(Color.black);
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
    	vs.onTop(gSensitiveW);
    	vs.onTop(labelW);
    	if(!single){
    		vs.onTop(gBackgroundN);
    		vs.onTop(gSensitiveN);
    		vs.onTop(labelN);
    	}
    }
    
    public void isVisible(){
    	
    }
    
    //GETTER/SETTER--------------------------------------------------------------------------------------------
    
    
    public void setInfoBox(NTInfoBox ib){
    	infoBox = ib;
    }
    public NTInfoBox getInfoBox(){
    	return infoBox;
    }
    
    /**Method that sets the background box of this node according to the maximal text length of all nodes in
     * the matrix. A gradient is also applied according to the position of the node in the list.
     */
	public void setLabelWidth(long maxLength) {
		this.widthHalf = maxLength/2;
		if (heightHalf == 0){
		    this.heightHalf = gBackgroundW.getHeight();
		}
		    
		wdx -= widthHalf;
		ndy += widthHalf;
		
		this.gBackgroundW.setWidth(widthHalf);
		gSensitiveW.setWidth(widthHalf-2);
		
//		System.out.println("[NODE] " + this.widthHalf);
		if (!this.single){
			this.gBackgroundW.move(-widthHalf, 0);
			this.labelW.move(widthHalf, 0);
			gBackgroundN.setWidth(widthHalf);
			gSensitiveN.setWidth(widthHalf-2);
			this.gBackgroundN.move(0, widthHalf);
			this.labelN.move(0,-widthHalf);
		}
	}

	public long getLabelHalfWidth() 
	{
		return this.widthHalf;
	}
	
	public long getTextWidth(){
		if (textWidth == -1){
			textWidth = this.labelW.getBounds(0).x;
		}
		return textWidth;	
	}

	public long getHeight(){
		return this.heightHalf;
	}

    public void addOutgoingEdge(NTEdge e){
    	outgoingEdges.add(e);
    }
    
    public void addIncomingEdge(NTEdge e){
    	incomingEdges.add(e);
    }

    
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
    	return "N::"+getName()+"@"+hashCode();
    }
    
    public void setOwner(Object o){
    	this.owner = o;
    }
    
    public Object getOwner(){
    	return owner;
    }
    
//    long getLabelWidth(){
//    	return (widthHalf == 0) ? ((labelW == null) ? 0 : labelW.getBounds(0).x ): widthHalf;
//    }
    
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
		if(gBackgroundW != null) vs.removeGlyph(this.gBackgroundW);
		if(labelW != null) vs.removeGlyph(this.labelW);
		if(gSensitiveW != null) vs.removeGlyph(gSensitiveW);
		if(gBackgroundN != null) vs.removeGlyph(this.gBackgroundN);
		if(labelN != null) vs.removeGlyph(this.labelN);
		if(gSensitiveN != null) vs.removeGlyph(gSensitiveN);
	}



	public void updataRelationPositions() {
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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}


}
