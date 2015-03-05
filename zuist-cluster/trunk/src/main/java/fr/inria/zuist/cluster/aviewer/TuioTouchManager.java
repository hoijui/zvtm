/*
 *   AUTHOR:    Olivier Chapuis <chapuis@lri.fr>
 *   Copyright (c) CNRS, 2015. All Rights Reserved
 *   Licensed under the GNU GPL.
 *
 */
package fr.inria.zuist.cluster.aviewer;


import java.util.Observable;
import java.util.Observer; 

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;

import fr.inria.zuist.cluster.aviewer.tuiotouch.TuioTouch;
import fr.inria.zuist.cluster.aviewer.ildaevent.IldaEvent;

class TuioTouchManager implements Observer
{

double width, height;
Viewer viewer;
TuioTouch tuioTouch;

TuioTouchManager(Viewer v)
{
	viewer = v;
	width = viewer.getDisplayWidth();
	height = viewer.getDisplayHeight();

	tuioTouch = new TuioTouch((double)width/(double)height, 0.002, 0.002, 0.04);
	tuioTouch.addObserver(this);
}

double prevMoveX = 0, prevMoveY = 0;
double prevPinchD = 0;

public void update(Observable obj, Object arg)
{
	//System.out.println("TuioTouchManager");
    if (!(arg instanceof IldaEvent.Base)) return;

	IldaEvent.Base e = (IldaEvent.Base)arg;

	switch(e.type)
	{
		case IldaEvent.START_MOVE:
		{
			IldaEvent.StartMove ee = (IldaEvent.StartMove)e;
			System.out.println("StartMove " + ee.x +" "+ ee.y);
			prevMoveX = ee.x; prevMoveY = ee.y;
			break;
		}
		case IldaEvent.MOVE:
		{
			IldaEvent.Move ee = (IldaEvent.Move)e;
			double dx = (ee.x - prevMoveX)*width;
			double dy = (ee.y - prevMoveY)*height;
			//System.out.println("Move "+ (-dx) + " " + dy + " " + ee.x + " "+ ee.y);
			viewer.directTranslate(-dx, dy);
			prevMoveX = ee.x; prevMoveY = ee.y;
			break;
		}
		case IldaEvent.END_MOVE:
		{
			IldaEvent.EndMove ee = (IldaEvent.EndMove)e;
			System.out.println("EndMove " + ee.x +" "+ ee.y);
			break;
		}
		case IldaEvent.START_PINCH:
		{
			IldaEvent.StartPinch ee = (IldaEvent.StartPinch)e;
			prevPinchD = ee.d; // prevPinchA = ee.a;
			System.out.println("StartPinch " + ee.d);
			break;
		}
		case IldaEvent.PINCH:
		{
			IldaEvent.Pinch ee = (IldaEvent.Pinch)e;
			//System.out.println("SPinch " + ee.d);
			if (ee.d != 0)
			{
				double f = prevPinchD/ee.d;
				viewer.centredZoom(f, ee.cx*width, ee.cy*height);
			}
			prevPinchD = ee.d;
			break;
		}
		case IldaEvent.END_PINCH:
		{
			IldaEvent.EndPinch ee = (IldaEvent.EndPinch)e;
			System.out.println("EndPinch ");
			break;
		}
	}
}

}