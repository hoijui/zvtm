/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2011. All Rights Reserved
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

    
    public void adjustAppearanceState(){
    	if(tail.matrix == null || head.matrix == null){
    		newAppearance = new ExtraEdgeAppearance(this);
    		return;
    	}

    	if(tail.matrix.equals(head.matrix)){
    		newAppearance = new IntraEdgeAppearance(this);
        }else{
        	newAppearance = new ExtraEdgeAppearance(this);
        }
    	
    }
    
    public void performAppearanceStateChange(){
    	if(appearance != null) appearance.clearGraphics();
    	appearance = newAppearance;
    	
    	if (newAppearance != null) return;
    	
    	if(visible)
    		newAppearance.show();
    	else
    		newAppearance.fade();
    }

    public void setNewInteractionState(int newState)
    {
     	this.newInteractionState = newState;
    }

    
    public void setVisible()
    {
    	if(visible || appearance == null) return;
    	visible = true;	
    	appearance.show();
    }
    
    public void setFaded()
    {
    	if(!visible || appearance == null) return;
    	visible = !true;	
    	appearance.fade();
    }
    
    public void performInteractionStateChange()
    {
    	if(newInteractionState == interactionState) return;
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHT) appearance.highlight();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHT_INCOMING) appearance.highlight();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHT_OUTGOING) appearance.highlight();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_SELECTED) appearance.select();
	    else{
	    		appearance.reset();
	    }
	    interactionState = newInteractionState;
    }
    
    public void createGraphics(VirtualSpace vs)
    {
    	if(appearance == null) return;
    	appearance.createGraphics(vs);
    	interactionState = NodeTrixViz.IA_STATE_DEFAULT;
    	newInteractionState = NodeTrixViz.IA_STATE_DEFAULT;
    	performInteractionStateChange();
    	
    	if(!visible){
    		appearance.fade();
    	}
    }

    public void setEdgeSetPosition(int index, int amount){
    	appearance.setEdgeSetPosition(index, amount);
    }

    
    public void move(double x, double y){
    	if(appearance == null) return;
    	appearance.move(x, y);
    }
    
    public void updatePosition(){
    	if(appearance == null) return;
    	appearance.updatePosition();
    }
    
    public int getState(){
    	if(appearance instanceof ExtraEdgeAppearance) return NodeTrixViz.APPEARANCE_EXTRA_EDGE;
    	
    	return NodeTrixViz.APPEARANCE_INTRA_EDGE;
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
	
    public void setColor(Color c, Color inverseColor){
//    	if(this.edgeColor != c) System.out.println("[NTEDGE] color differs!");
    	if(c != null) this.edgeColor = c;
    	if(inverseColor != null) this.inverseColor = inverseColor;
    	if(appearance != null)appearance.updateColor();
    }
    
    public void setSymmetric(boolean sym){
    	symmetric = sym;
    }
    public boolean isSymmetric(){
    	return symmetric;
    }
	
	
	public boolean hasInverse(){ return inverseColor != null; }
	
	public Color getInverseColor(){ return inverseColor; }
	
	public Color getColor(){ return this.edgeColor; }
	
	public boolean isVisible(){
		return visible;
	}
}
