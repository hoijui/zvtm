package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.engine.VirtualSpace;

public abstract class EdgeAppearance {

	protected NTEdge edge;
	protected AnimationManager animManager;
	protected VirtualSpace vs;
	protected Color color;
	protected long amount = 1;
	protected int index = 0;

	
	//LIFE-CYCLE
	public EdgeAppearance(NTEdge edge){
		this.edge = edge;
	}
//	public abstract void createGraphics(long height, long y, long x, long index, VirtualSpace vs);
	public abstract void createGraphics(VirtualSpace vs);
	public abstract void createGraphics();	
	protected abstract void clearGraphics();
	public void setEdgeSetPosition(int index, int amount){
		this.index = index;
		this.amount = amount;
	}
	
	//GENERAL BEHAVIOUR
	public abstract void move(long x, long y);
//	public abstract void moveTo(long x, long y);
	public abstract void onTop();
	public abstract void updatePosition();
	
	//INTERACTION
	public abstract void reset();
	public abstract void highlight(Color c);
	public abstract void select();
	public abstract void fade();
	
	//GETTER, SETTER
	public NTEdge getEdge(){
		return this.edge;
	}
	
}
