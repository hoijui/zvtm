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
import fr.inria.zvtm.nodetrix.lll.LinLogNode;

public class NTEdge extends LinLogEdge{


	NTNode tail, head;
    Color edgeColor;
    int interactionState = NodeTrixViz.IA_STATE_DEFAULT;
    int newInteractionState = NodeTrixViz.IA_STATE_HIGHLIGHTED;
    private EdgeAppearance appearance;	//responsible for graphical rendering.
    private EdgeAppearance newAppearance;
    
    Object owner;
    
    
    public NTEdge(NTNode startNode, NTNode endNode, Color c) {
    	super(startNode, endNode, 1);
    	this.edgeColor = c;
    	this.tail = startNode;
    	this.head = endNode;
    }

    public void setNodes(NTNode t, NTNode h){
    	this.tail = t;
        this.head = h;
    }
    
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

    public void setInteractionState(int newState)
    {
//    	if(interactionState == NodeTrixViz.IA_STATE_SELECTED && newState == NodeTrixViz.IA_STATE_FADE) return;
    	this.newInteractionState = newState;
    }
    
    public void performInteractionStateChange()
    {
    	if(newInteractionState == interactionState) return;
    	
	    if(newInteractionState == NodeTrixViz.IA_STATE_FADE) appearance.fade();
	    else if(newInteractionState == NodeTrixViz.IA_STATE_HIGHLIGHTED) appearance.highlight(NodeTrixViz.EXTRA_EDGE_HIGHLIGHT_COLOR);
	    else if(newInteractionState == NodeTrixViz.IA_STATE_SELECTED) appearance.select();
	    else appearance.reset();
	    
	    interactionState = newInteractionState;
    }
    
    public void createGraphics( VirtualSpace vs)
    {
    	if(appearance == null) return;
    	appearance.createGraphics(vs);
    }

    public void setEdgeSetPosition(int index, int amount){
    	appearance.setEdgeSetPosition(index, amount);
    }
    
    
//    public void moveTo(long x, long y)
//    {
//    	if(appearance == null) return;
//    	appearance.moveTo(x,y);
//    }
    
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
	
	
}
