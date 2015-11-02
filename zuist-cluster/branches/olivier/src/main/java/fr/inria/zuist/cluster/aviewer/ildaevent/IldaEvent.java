package fr.inria.zuist.cluster.aviewer.ildaevent;


public class IldaEvent {

public static final int START_MOVE=2;
public static final int MOVE=3;
public static final int END_MOVE=4;

public static final int START_PINCH=5;
public static final int PINCH=6;
public static final int END_PINCH=7;

public IldaEvent() {}

public class Base {
	public int type = 0;
	public Base() {}
}

public class XY extends Base {
	public double x,y;
	public XY(double xx, double yy) { 
		super();
		x = xx; y = yy;
	}
}
public class StartMove extends XY {
	public StartMove(double xx, double yy) {
		super(xx, yy);
		type = START_MOVE;
	}
}
public class Move extends XY {
	public Move(double xx, double yy) {
		super(xx, yy);
		type = MOVE;
	}
}
public class EndMove extends XY {
	public EndMove(double xx, double yy) {
		super(xx, yy);
		type = END_MOVE;
	}
}

public class Pinch extends Base {
	public double cx,cy,d,a;
	public Pinch(double cxx, double cyy, double dd, double aa) {
		cx = cxx; cy = cyy; d = dd; a = aa;
		type = PINCH;
	}
}
public class StartPinch extends Pinch {
	public StartPinch(double cxx, double cyy, double dd, double aa) {
		super(cxx,cyy,dd,aa);
		type = START_PINCH;
	}
}
public class EndPinch extends Pinch {
	public EndPinch(double cxx, double cyy, double dd, double aa) {
		super(cxx,cyy,dd,aa);
		type = END_PINCH;
	}
}



}