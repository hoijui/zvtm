package  fr.inria.zuist.cluster.aviewer.tuiotouch;

import java.util.Observable;
import java.util.Observer; 

import fr.inria.zuist.cluster.aviewer.ildaevent.IldaEvent; 
import fr.inria.zuist.cluster.aviewer.ildaevent.IldaEvent.*;

public class TuioTouch  extends Observable implements Observer
{

private IldaEvent ildaEvent;
private double dragThreshold, pinchThreshold, clusterThreshold;

public TuioTouch(double dragT, double pinchT, double clusterT)
{
	super(); // Observable
	dragThreshold = dragT;
	pinchThreshold= pinchT; 
	clusterThreshold = clusterT;

	TuioReceiver tr = new TuioReceiver();
	tr.addObserver(this);
	ildaEvent = new IldaEvent();
	System.out.println("TuioTouch created");
}

static final int MODE_STOP = -1;
static final int MODE_READY = 0;
static final int MODE_MOVE = 1;
static final int MODE_PINCH = 2;

int _numDown = 0;
int _idNum = 0;
int _mode = 0;
int _firstId;
int _secondId;

double sx1 = 0, sy1 = 0;
double cx1 = 0, cy1 = 0;
double sx2 = 0, sy2 = 0;
double cx2 = 0, cy2 = 0;
double delta = 0;

double dist(double x1, double y1, double x2, double y2)
{
	return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
}

public void update(Observable obj, Object arg)
{
	//System.out.println("update Tuio");

	if (!(arg instanceof TouchEvent)) { return; }

	TouchEvent e = (TouchEvent)arg;

	switch(e.action)
	{
	case TouchEvent.DOWN:
		_numDown++;
		if (_mode == MODE_READY)
		{
			if (_numDown == 1)
			{
				_firstId = e.id;
				cx1 = sx1 = e.x; cy1 = sy1 = e.y;
				_idNum = 1;
			}
			else if (_numDown > 1 && _idNum == 1)
			{
				// distance check !!
				if (dist(cx1, cy1, e.x, e.y) > clusterThreshold)
				{
					_secondId = e.id;
					_idNum = 2;
					cx2 = sx2 = e.x; cy2 = sy2 = e.y;
					delta = dist(cx1, cy1, e.x, e.y);
				}
			}
		}
	break;
	
	case TouchEvent.MOVE:
		double x1 = cx1, y1 = cy1, x2 = cx2, y2 = cy2;
		if (_idNum >= 1 && e.id == _firstId)
		{
			x1 = e.x; y1 = e.y;
		}
		else if (_idNum >= 2 && e.id == _secondId)
		{
			x2 = e.x; y2 = e.y;
		}
		else
		{
			break;
		}

		if (_mode == MODE_READY)
		{
			if (_idNum == 1)
			{
				if (dist(sx1, sy1, e.x, e.y) > dragThreshold)
				{
					_mode = MODE_MOVE;
					// do drag x1 y1 vs. sx1, sx2
					IldaEvent.StartMove ie = ildaEvent.new StartMove(sx1, sy1);
					setChanged(); notifyObservers(ie);
					IldaEvent.Move iem = ildaEvent.new Move(x1, y1);
					setChanged(); notifyObservers(iem);
				}
			}
			else if (_idNum == 2)
			{
				double nd = dist(x1, y1, x2, y2);
				if (Math.abs(nd-delta) > pinchThreshold)
				{
					_mode = MODE_PINCH;
					// FIXME: compute angle
					IldaEvent.StartPinch ie = ildaEvent.new StartPinch((sx1+sx2)/2, (sy1+sy2)/2, delta, 0);
					setChanged(); notifyObservers(ie);
					IldaEvent.Pinch iem = ildaEvent.new Pinch((x1+x2)/2, (y1+y2)/2, nd, 0);
					setChanged(); notifyObservers(iem);

				}
			}
		}
		else if (_mode == MODE_MOVE)
		{
			// do drag x1 y1 vs. cx1, cx2
			IldaEvent.Move ie = ildaEvent.new Move(x1, y1);
			setChanged(); notifyObservers(ie);

		}
		else if (_mode == MODE_PINCH)
		{
			double nd = dist(x1, y1, x2, y2);
			// FIXME: compute angle
			IldaEvent.Pinch ie = ildaEvent.new Pinch((x1+x2)/2, (y1+y2)/2, nd, 0);
			setChanged(); notifyObservers(ie);
		}
		cx1 = x1; cy1 = y1;
		cx2 = x2; cy2 = y2;
		delta = dist(cx1, cy1, cx2, cy2);
	break;

	case TouchEvent.UP:
		_numDown--;
		if ((e.id == _firstId) || (_idNum == 2 && e.id == _secondId))
		{
			if (_mode == MODE_MOVE)
			{
				IldaEvent.EndMove ie = ildaEvent.new EndMove(cx1, cy1);
				setChanged(); notifyObservers(ie);

			}
			else if (_mode == MODE_PINCH)
			{
				IldaEvent.Pinch ie = ildaEvent.new Pinch((cx1+cx2)/2, (cy1+cy2)/2, 0, 0);
				setChanged(); notifyObservers(ie);
			}
			_mode = MODE_STOP;
			_idNum = 0;
		}
		if (_numDown == 0) 
		{
			_mode = MODE_READY;
			_idNum = 0;
		}
	break;
	}
}

}