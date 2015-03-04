/*
 *   AUTHOR:    Olivier Chapuis <chapuis@lri.fr>
 *   Copyright (c) CNRS, 2013. All Rights Reserved
 *   Licensed under the GNU GPL.
 *
 */

package fr.inria.zuist.cluster.aviewer;

import java.util.Vector;

import java.util.Observable;
import java.util.Observer; 

import java.awt.geom.Point2D;
import java.awt.Color;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;

import fr.lri.smarties.libserver.Smarties;
import fr.lri.smarties.libserver.SmartiesColors;
import fr.lri.smarties.libserver.SmartiesEvent;
import fr.lri.smarties.libserver.SmartiesPuck;
import fr.lri.smarties.libserver.SmartiesDevice;
import fr.lri.smarties.libserver.SmartiesWidget;
import fr.lri.smarties.libserver.SmartiesWidgetHandler;
import fr.lri.smarties.libserver.SmartiesWidgetHandler;
 
class vSmarties implements Observer
{

double width, height;
Viewer viewer;
Smarties m_smarties;
Vector<String> xmlfiles;

vSmarties(Viewer v)
{
	viewer = v;
	width = viewer.getDisplayWidth();
	height = viewer.getDisplayHeight();

	m_smarties = new Smarties((int)width, (int)height, 8, 4);
	//m_smarties.setPureTouchpad(true);
	//m_smarties.createOnePuckByDevice(true);


	// add some Smarties widgets into a 3 x 3 grid
	m_smarties.initWidgets(3,1);
	SmartiesWidget w;
	// SmartiesWidget w = m_smarties.addWidget(
	// 	SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Mark Position", 1, 1, 1, 1);
	// //w.handler = new markMark();
	// w = m_smarties.addWidget(
	// 	SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Prev Mark", 2, 1, 0.66f, 1);
	// //w.handler = new markPrev();
	// w = m_smarties.addWidget(
	// 	SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Next Mark", 2.66f, 1, 0.66f, 1);
	// //w.handler = new markNext();
	// w = m_smarties.addWidget(
	// 	SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Curr Mark", 3.33f, 1, 0.66f, 1);
	// //w.handler = new markNext();

	// global widgets

	w = m_smarties.addWidget(
		SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Reset View", 1, 1, 1, 1);
	w.handler = new globalViewClicked();

	w = m_smarties.addWidget(
		SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "Draw Under Bezel: Off", 2, 1, 1, 1);
	w.labelOn = "Draw Under Bezel: On";
	w.on = true;
	w.handler = new drawUnderBezel();

	w = m_smarties.addWidget(
		SmartiesWidget.SMARTIES_WIDGET_TYPE_SPINNER, "Open Images", 3, 1, 1, 1);
	w.handler = new changeImage();
	w.items.add("Paris 26 Giga Pixels");
	//w.items.add("NASA Curiosity");
	w.items.add("moon");
	w.items.add("cosmos");
	w.items.add("galaxy");
	w.items.add("france");
	w.items.add("vela snr");

	xmlfiles = new Vector();
	//xmlfiles.add("/usr/local/share/bigimages/zuist/paris26GP/scene_fullL0.xml");
	//xmlfiles.add("/usr/local/share/bigimages/zuist/paris26GP/scene_generated.xml");
	//xmlfiles.add("/usr/local/share/bigimages/zuist/NASA-Curiosity/ZVTM/scene.xml");
 	xmlfiles.add("/media/ssd/Demos/zvtm/paris26GP/scene_fullL0.xml");
 	xmlfiles.add("/media/ssd/Demos/zvtm/moon/scene.xml");
 	xmlfiles.add("/media/ssd/Demos/zvtm/cosmos/scene.xml");
 	xmlfiles.add("/media/ssd/Demos/zvtm/GLIMPSE360/scene.xml");
 	xmlfiles.add("/media/ssd/Demos/zvtm/france_bright/scene.xml");
 	xmlfiles.add("/media/ssd/Demos/zvtm/VelaSNR_d/scene.xml");

	m_smarties.addObserver(this);
	m_smarties.Run();
	System.out.println("Smarties running " + width + " " + height);
}

// ------------------------------------------------------------------------
// local "data" attached to a puck 

class myCursor
{

public int id;
public double x, y;
public Color color;
public WallCursor wc;
public Point2D.Double delta;

public void move(double x, double y)
{
	this.x = x; this.y = y;
	wc.moveTo((long)(x*width - width/2.0), (long)(height/2.0 - y*height));
}

public myCursor(int id, double x, double y)
{
	this.id = id;
	this.x = x;
	this.y = y;
	this.color = SmartiesColors.getPuckColorById(id);

	wc = new WallCursor(
		viewer.cursorSpace,
		(viewer.desktoponly)? 2 : 20, (viewer.desktoponly) ? 16 : 80*4,
		this.color);
	move(x, y);
}

} // class myCursor


// ------------------------------------------------------------------------
// events handleing

SmartiesPuck pinchPuck = null;
SmartiesDevice pinchDevice = null;
SmartiesPuck dragPuck = null;
SmartiesDevice dragDevice = null;

float prevMFPinchD = 0f;
float prevMFMoveX = 0f;
float prevMFMoveY = 0f;

public void update(Observable obj, Object arg)
{

        if (!(arg instanceof SmartiesEvent)) 
	{
		return;
	}

	SmartiesEvent e = (SmartiesEvent)arg;

	switch (e.type) {
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_CREATE:
	{
		System.out.println("Create Puck: " + e.id);
		e.p.app_data = new myCursor(e.p.id, e.p.x, e.p.y);
		myCursor c = (myCursor)e.p.app_data;
		c.wc.setVisible(true);
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_SELECT:
	{
		System.out.println("Select Puck: " + e.id);
		//_checkWidgetState(e.device, e.p);
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_STORE:
	{
		//repaint();
		if (e.p != null)
		{
			myCursor c = (myCursor)e.p.app_data;
			c.wc.setVisible(false);
			//_repaintCursor(c);
		}
		break;	
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_UNSTORE:	
	{
		if (e.p != null)
		{
			myCursor c = (myCursor)e.p.app_data;
			c.move(e.p.x, e.p.y);
			c.wc.setVisible(true);
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_DELETE:
	{
		//System.out.println("Delete Puck: " + e.id);
		myCursor c = (myCursor)e.p.app_data;
		c.wc.dispose();
		m_smarties.deletePuck(e.p.id);
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_MULTI_TAPS:
	{
		// we support multi finger multi tap
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_START_MOVE:
	{
		//System.out.println("Move Puck: " + e.id);
		if (e.p != null)
		{
			if (e.mode == SmartiesEvent.SMARTIE_GESTUREMOD_DRAG && dragDevice == null)
			{
				// drag not locked by an other device
				dragDevice = e.device;
				dragPuck = e.p;
				prevMFMoveX = e.x;
				prevMFMoveY = e.y;
			}
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_MOVE:
	{
		//System.out.println("Move Puck: " + e.id);
		if (e.p != null)
		{
			myCursor c = (myCursor)e.p.app_data;
			c.move(e.p.x, e.p.y);
			if (e.mode == SmartiesEvent.SMARTIE_GESTUREMOD_DRAG && dragDevice == e.device)
			{
				// this is the device that lock the drag, e.p should be == dragPuck
				float dx = (e.x - prevMFMoveX)*(float)width;
				float dy = (e.y - prevMFMoveY)*(float)height;
				viewer.directTranslate(-dx, dy);
				prevMFMoveX = e.x;
				prevMFMoveY = e.y;
			}
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_END_MOVE:
	{
		if (dragDevice == e.device)
		{
			// this is the device that lock the drag
			dragDevice = null;
			dragPuck = null;
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_START_MFPINCH:
	{
		//System.out.println("Start MF Pinch: " + e.id);
		if (pinchDevice == null)
		{
			// zoom not locked by an other device
			pinchDevice = e.device;
			pinchPuck = e.p;
			prevMFPinchD = e.d;
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_MFPINCH:
	{
		//System.out.println("MF Pinch: " + e.id);
		if (pinchDevice == e.device)
		{
			// this is the device that lock the zoom e.p shoul be == pinchPuck
			// TODO: zoom centred on pinchPuck (if null center on the center of the pinch)
			if (e.d != 0)
			{
				double x = e.x;
				double y = e.y;
				if (e.p != null)
				{
					x = e.p.x;
					y = e.p.y;
				}
				float f = prevMFPinchD/e.d;
				viewer.centredZoom(f, x*width, y*height);
			}
			prevMFPinchD = e.d;
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_END_MFPINCH:
	{
		//System.out.println("End MF Pinch: " + e.id);
		if (pinchDevice == e.device)
		{
			// this is the device that lock the zoom
			pinchDevice = null;
			pinchPuck = null;
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_START_MFMOVE:
	{
		//System.out.println("Start MF Move: " + e.id);
		if (dragDevice == null)
		{
			// drag not locked by an other device
			dragDevice = e.device;
			dragPuck = e.p;
			prevMFMoveX = e.x;
			prevMFMoveY = e.y;
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_MFMOVE:
	{
		//System.out.println("MF Move: " + e.id);
		if (dragDevice == e.device)
		{
			// this is the device that lock the drag, e.p should be == dragPuck
			float dx = e.num_fingers*(e.x - prevMFMoveX)*(float)width;
			float dy =  e.num_fingers*(e.y - prevMFMoveY)*(float)height;
			viewer.directTranslate(-dx, dy);
			prevMFMoveX = e.x;
			prevMFMoveY = e.y;
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_END_MFMOVE:
	{
		//System.out.println("End MF Move: " + e.id);
		if (dragDevice == e.device)
		{
			// this is the device that lock the drag
			dragDevice = null;
			dragPuck = null;
		}
		break;
	}
	case SmartiesEvent.SMARTIE_EVENTS_TYPE_WIDGET:
	{
		//System.out.println("SMARTIE_EVENTS_TYPE_WIDGET: " + e.widget.uid);
		if (e.widget.handler != null)
		{
			e.widget.handler.callback(e.widget, e, this);
		}
		break;
	}
	default:
	{
		break;
	}
	} // ens switch

}


// ------------------------------------------------------------------------
// widgets handler ...

public class globalViewClicked implements SmartiesWidgetHandler
{
	public boolean callback(SmartiesWidget w, SmartiesEvent e, Object user_data)
	{
		System.out.println("globalViewClicked ");
		viewer.getGlobalView(null);
		return true;
	}
}

public class drawUnderBezel implements SmartiesWidgetHandler
{
	public boolean callback(SmartiesWidget w, SmartiesEvent e, Object user_data)
	{
		System.out.println("drawUnderBezel " + w.on);
		viewer.toggleClusterView();
		return true;
	}
}

public class changeImage implements SmartiesWidgetHandler
{
	public boolean callback(SmartiesWidget w, SmartiesEvent e, Object user_data)
	{
		System.out.println("Change Images: " + w.item);

		if (w.item < xmlfiles.size())
		{
			viewer.openSceneDestroy(xmlfiles.get(w.item));
		} 
		return true;
	}
}

}
