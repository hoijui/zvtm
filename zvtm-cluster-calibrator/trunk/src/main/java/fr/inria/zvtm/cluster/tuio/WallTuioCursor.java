package fr.inria.zvtm.cluster.tuio;

import TUIO.TuioCursor;
import TUIO.TuioPoint;

import fr.inria.zvtm.engine.VirtualSpace;


public class WallTuioCursor extends WallCursor{

	TuioCursor tcur;

	static final double thickness = 5;
	static final double length = 50;

	public WallTuioCursor(VirtualSpace target, TuioCursor tcur){
		super(target, thickness, length, TuioColors.getCursorColorById(tcur.getCursorID()));
		this.tcur = tcur;
	}

	public int getCursorID(){
		return tcur.getCursorID();
	}


}