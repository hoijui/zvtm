/*   AUTHOR :           Benjamin Bach (bbach@lri.fr)
 *   Copyright (c) INRIA, 2010-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.engine.VirtualSpace;

public abstract class EdgeAppearance {

	protected NTEdge edge;
	protected AnimationManager animManager;
	protected VirtualSpace vs;
	protected long amount = 1;
	protected int index = 0;

	
	//LIFE-CYCLE
	public EdgeAppearance(NTEdge edge){
		this.edge = edge;
	}
	public abstract void createGraphics(VirtualSpace vs);
	public abstract void createGraphics();	
	protected abstract void clearGraphics();
	public void setEdgeSetPosition(int index, int amount){
		this.index = index;
		this.amount = amount;
	}
	
	//GENERAL BEHAVIOUR
	public abstract void move(double x, double y);
	public abstract void onTop();
	public abstract void updatePosition();
	public abstract void updateColor();
	
	//INTERACTION
	public abstract void reset();
	public abstract void highlight(Color c);
	public abstract void select();
	public abstract void fade();
	public abstract void show();
	
	//GETTER, SETTER
	public NTEdge getEdge(){
		return this.edge;
	}
	
}
