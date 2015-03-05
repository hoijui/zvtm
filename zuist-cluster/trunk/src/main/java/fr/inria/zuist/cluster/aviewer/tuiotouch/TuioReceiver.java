package fr.inria.zuist.cluster.aviewer.tuiotouch;

import TUIO.*;

import java.util.Observable;  

public class TuioReceiver extends Observable implements TuioListener
{

private TuioClient client = null;
private boolean verbose = false;

public TuioReceiver()
{
	super(); // Observable
	int port = 3333;

	try {
		client = new TuioClient(port);
	} catch (Exception e1) {
		System.out.println("Tuio client error" + e1);
		//System.exit(0);
	}

	if (client!=null) {
		client.addTuioListener(this);
		client.connect();
	} else {
		System.out.println("Tuio client error");
		//System.exit(0);
	}
}

/**
 * This callback method is invoked by the TuioClient when a new TuioObject is added to the session.   
 *
 * @param  tobj  the TuioObject reference associated to the addTuioObject event
 */
public void addTuioObject(TuioObject tobj)
{
	if (verbose)
		System.out.println(
			"TuioReceiver: add obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+") "
			+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle());
}

/**
 * This callback method is invoked by the TuioClient when an existing TuioObject is updated during the session.   
 *
 * @param  tobj  the TuioObject reference associated to the updateTuioObject event
 */
public void updateTuioObject(TuioObject tobj)
{
	if (verbose) 
		System.out.println(
			"TuioReceiver: set obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+") "
			+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle()+" "
			+tobj.getMotionSpeed()+" "+tobj.getRotationSpeed()+" "
			+tobj.getMotionAccel()+" "+tobj.getRotationAccel());
}

/**
 * This callback method is invoked by the TuioClient when an existing TuioObject is removed from the session.   
 *
 * @param  tobj  the TuioObject reference associated to the removeTuioObject event
 */
public void removeTuioObject(TuioObject tobj)
{
	if (verbose) 
		System.out.println(
			"TuioReceiver: del obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+")");	
}

/**
 * This callback method is invoked by the TuioClient when a new TuioCursor is added to the session.   
 *
 * @param  tcur  the TuioCursor reference associated to the addTuioCursor event
 */
public void addTuioCursor(TuioCursor tcur)
{
	// down
	TouchEvent e = new TouchEvent(
		TouchEvent.DOWN, tcur.getCursorID(), tcur.getX(), tcur.getY(), 0);
	setChanged();
	notifyObservers(e);
	if (verbose) 
		System.out.println(
			"TuioReceiver: add cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+") "
			+tcur.getX()+" "+tcur.getY());
}

/**
 * This callback method is invoked by the TuioClient when an existing TuioCursor is updated during the session.   
 *
 * @param  tcur  the TuioCursor reference associated to the updateTuioCursor event
 */
public void updateTuioCursor(TuioCursor tcur)
{
	// move 
	TouchEvent e = new TouchEvent(
		TouchEvent.MOVE, tcur.getCursorID(), tcur.getX(), tcur.getY(), 0);
	setChanged();
	notifyObservers(e);
	if (verbose)
		System.out.println(
			"TuioReceiver: set cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+") "
			+tcur.getX()+" "+tcur.getY()+" "+tcur.getMotionSpeed()+" "
			+tcur.getMotionAccel());
}

/**
 * This callback method is invoked by the TuioClient when an existing TuioCursor is removed from the session.   
 *
 * @param  tcur  the TuioCursor reference associated to the removeTuioCursor event
 */
public void removeTuioCursor(TuioCursor tcur)
{
	// up
	TouchEvent e = new TouchEvent(
		TouchEvent.UP, tcur.getCursorID(), tcur.getX(), tcur.getY(), 0);
	setChanged();
	notifyObservers(e);
	if (verbose) 
		System.out.println(
			"TuioReceiver: del cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+")");
}

/**
 * This callback method is invoked by the TuioClient to mark the end of a received TUIO message bundle.   
 *
 * @param  ftime  the TuioTime associated to the current TUIO message bundle
 */
public void refresh(TuioTime ftime)
{
	if (verbose) 
		System.out.println("");
}

}