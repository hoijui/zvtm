package fr.inria.zuist.cluster.aviewer.tuiotouch;

public class TouchEvent
{

public static final int DOWN = 1;
public static final int UP = 2;
public static final int MOVE = 3;

public int action;
public int id;
public double x,y;
public long t;

public TouchEvent(int a, int i, double xx, double yy, long tt)
{
	action = a; id = i; x = xx; y = yy; t = tt;
}

}