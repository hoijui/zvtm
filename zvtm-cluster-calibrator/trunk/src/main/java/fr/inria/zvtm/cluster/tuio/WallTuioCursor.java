package fr.inria.zvtm.cluster.tuio;

import TUIO.TuioCursor;
import TUIO.TuioPoint;

import fr.inria.zvtm.engine.VirtualSpace;

import java.awt.Color;
import fr.inria.zvtm.glyphs.VCircle;


public class WallTuioCursor extends WallCursor{

	static final double thickness = 5;
	static final double length = 50;

	long totalMilliseconds;
	int cursorID;
	double xx;
	double yy;
	double x;
	double y;

	VirtualSpace target;

	public	WallTuioCursor(VirtualSpace target, int id, double x, double y, long timeMilliseconds){
		super(target, thickness, length, TuioColors.getCursorColorById(id) );
		cursorID = id;
		this.target = target;
		setTuioCursor(timeMilliseconds, x, y);
	}

	public int getCursorID(){
		return cursorID;
	}

	public void setTuioCursor(long timeMilliseconds, double x, double y){
		totalMilliseconds = timeMilliseconds;
		this.xx = this.x;
		this.yy = this.y;
		this.x = x;
		this.y = y;
        moveTo(x, y);
	}

	public long getTotalMilliseconds(){
		return totalMilliseconds;
	}

	@Override
	public double getX(){
		return x;
	}

	@Override
	public double getY(){
		return y;
	}

	public double getDistanceX(){
		return xx - x;
	}

	public double getDistanceY(){
		return yy - y;
	}

	public double getDistance(WallTuioCursor wtc){
		double x1, x2, y1, y2;
		x2 = wtc.getX();
		x1 = x;
		y2 = wtc.getY();
		y1 = y;
		return Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
	}

	public double getDistanceX(WallTuioCursor wtc){
		double x1, x2;
		x2 = wtc.getX();
		x1 = x;
		return (x2-x1);
	}

	public double getDistanceY(WallTuioCursor wtc){
		double y1, y2;
		y2 = wtc.getY();
		y1 = y;
		return (y2-y1);
	}

	@Override
	public String toString(){
		return "WallTuioCursor&cursorID="+cursorID+"&x="+x+"&y="+y+"&xx="+xx+"&yy="+yy+"&totalMilliseconds="+totalMilliseconds;
	}


}