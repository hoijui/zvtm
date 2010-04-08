/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.nodetrix.lll.LinLogEdge;

public class NTEdge extends LinLogEdge{


	NTNode tail, head;
    private Color edgeColor;
    protected int interactionState = NodeTrixViz.IA_STATE_DEFAULT;
    protected int newInteractionState = NodeTrixViz.IA_STATE_HIGHLIGHT;
    private EdgeAppearance appearance;	//responsible for graphical rendering.
    private EdgeAppearance newAppearance;
    Object owner;
    private Color inverseColor;
    private boolean symmetric = false;
    private boolean visible = true;
    
    
    public NTEdge(NTNode startNode, NTNode endNode, Color c) {
    	super(startNode, endNode, 1);
    	this.edgeColor = c;
    	this.tail = startNode;
    	this.head = endNode;
    }

//    public void setNodes(NTNode t, NTNode h){
//    	this.tail = t;
//        this.head = h;
//    }
    
    public void adjustAppearanceState(){
    	if(tail.matrix == null || head.matrix == null){
    		newAppearance = new ExtraEdgeAppearance(this);
    		return;
    	}

    	if(tail.matrix.getName().equals(head.matrix.getName())){
    		newAppearance = new IntraEdgeAppearance(this);
        }else{
        	newAppearance = new ExtraEdgeAppearance(this);
        }
    }
    
    public void performAppearanceStateChange(){
    	if(appearance != null) appearance.clearGraphics();
    	appearance = newAppearance;
    }

    public void setNewInteractionState(int newState)
    {
     	this.newInteractionState = newState;
    }
    
    public void setVisibility(boolean b){
    	if(visible == b || appearance == null) return;
    	
    	if(b) appearance.show();
    	else appearance.fade();
    	
    	visible = b;
    }
    
    
    public void performInteractionStateChange()
    {
    	if(!visible 
    			|| newInteractionState == interactionState) return;
//    	if(!visible && newInteractionState != NodeTrixViz.IA_STATE_FADE){appearance.show();}
//    	
//	    if(newInteractionState == NodeTrixViz.IA_STATE_FADE) {
//	    	appearance.fade();
//	    	visible = false;
//	    }
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHT) appearance.highlight(NodeTrixViz.COLOR_EDGE_HIGHLIGHT_OUTGOING);
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHT_INCOMING) appearance.highlight(NodeTrixViz.COLOR_EDGE_HIGHLIGHT_INCOMING);
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHT_OUTGOING) appearance.highlight(NodeTrixViz.COLOR_EDGE_HIGHLIGHT_OUTGOING);
	    else if(newInteractionState == NodeTrixViz.IA_STATE_SELECTED) appearance.select();
	    else{
//	    	if(!visible)
//	    		appearance.fade();
//	    	else
	    		appearance.reset();
	    }
	    interactionState = newInteractionState;
    }
    
    public void createGraphics( VirtualSpace vs)
    {
    	if(appearance == null) return;
    	appearance.createGraphics(vs);
    	interactionState = NodeTrixViz.IA_STATE_DEFAULT;
    	newInteractionState = NodeTrixViz.IA_STATE_DEFAULT;
    	performInteractionStateChange();
    	
    	if(!visible){
    		System.out.println("NOT VISIBLE");
//    		setVisibility(false);
    		appearance.fade();
    	}
    }

    public void setEdgeSetPosition(int index, int amount){
    	appearance.setEdgeSetPosition(index, amount);
    }

    
    public void move(long x, long y){
    	if(appearance == null) return;
    	appearance.move(x, y);
    }
    
    public void updatePosition(){
    	if(appearance == null) return;
    	appearance.updatePosition();
    }
    
    public int getState(){
    	if(appearance instanceof ExtraEdgeAppearance) return NodeTrixViz.APPEARANCE_EXTRA_EDGE;
    	else return NodeTrixViz.APPEARANCE_INTRA_EDGE;
    }
    
    public boolean isIntraEdge(){
    	return tail.matrix.equals(head.getMatrix());
    }
    
    public void onTop(){
    	if(appearance == null) return;
    	appearance.onTop();
    }
    
    public NTNode getTail(){
        return tail;
    }
    
    public NTNode getHead(){
        return head;
    }
    
    public void setOwner(Object o){
        this.owner = o;
    }
    
    public Object getOwner(){
        return owner;
    }
    
    public void cleanGraphics(){
    	if(appearance == null) return;
    	appearance.clearGraphics();
    }
	
    public void setColor(Color c){
    	this.edgeColor = c;
    	appearance.updateColor();
    }
    public void setSymmetric(boolean sym){
    	symmetric = sym;
    }
    public boolean isSymmetric(){
    	return symmetric;
    }
	
	public void setInverseColor(Color c){
		inverseColor = c;
	}
	public boolean hasInverse(){ return inverseColor != null; }
	
	public Color getInverseColor(){ return inverseColor; }
	
	public Color getColor(){ return edgeColor; }
	
	public boolean isVisible(){
		return visible;
	}
}
