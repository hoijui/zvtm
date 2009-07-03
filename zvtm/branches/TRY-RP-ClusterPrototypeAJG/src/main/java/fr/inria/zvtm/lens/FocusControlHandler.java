/*   AUTHOR : Caroline Appert (appert@lri.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$ 
 */
 
package fr.inria.zvtm.lens;

import java.awt.Point;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.glyphs.Glyph;

public class FocusControlHandler implements ViewEventHandler {

	int lastXLensCenter = -1;
	int lastYLensCenter = -1;
	
	int dx, dy;
	double speed;
	double magFactor;
	Point ptRobot = new Point();
	
	Robot robot;
	SpeedFunction sf;
	View view;
	
	boolean started = false;
	
	ViewEventHandler actualViewEventHandler;
	Java2DPainter    actualAfterPortalsPainter;
	Java2DPainter paintCursor;
	Lens lens;
	TemporalLens tLens;

	public static short CONSTANT = 0;
	public static short SPEED_DEPENDENT_LINEAR = 1;
	
	
	public FocusControlHandler(View v, ViewEventHandler aveh, short speedBehavior){
		view = v;
		if(speedBehavior == SPEED_DEPENDENT_LINEAR) {
			sf = new LSpeedFunction();
		} else {
			if(speedBehavior == CONSTANT) {
				sf = new SpeedFunction() {
					public double getSpeedCoeff(long currentTime, int x, int y) {
						return 0;
					}
				};
			} else {
				sf = new SpeedFunction() {
					public double getSpeedCoeff(long currentTime, int x, int y) {
						return 0;
					}
				};
			}
		}
		this.actualViewEventHandler = aveh;
		robot = null;
		try {
			robot = new Robot();
		} catch(AWTException e) { 
			e.printStackTrace();
		}
		
		actualAfterPortalsPainter = v.getJava2DPainter(Java2DPainter.AFTER_PORTALS);
		paintCursor = new Java2DPainter() {
			public void paint(Graphics2D g2d, int viewWidth, int viewHeight) {
				if(view.getLens() != null) {
					g2d.setColor(Color.BLACK);
					g2d.drawLine(lastXLensCenter - 10, lastYLensCenter, lastXLensCenter + 10, lastYLensCenter);
					g2d.drawLine(lastXLensCenter, lastYLensCenter - 10, lastXLensCenter, lastYLensCenter + 10);
				}
			}
		};
		
	}
	
	public void start(Lens l) { 
		started = true;
		if(view.getPanel() != null) view.setEventHandler(this);
	    lens = l;
		if (l instanceof TemporalLens){
		    tLens = (TemporalLens)l;
		}
		else {
            tLens = null;
		}
		Dimension d = view.getPanelSize();
		lastXLensCenter = lens.lx + d.width/2;
		lastYLensCenter = lens.ly + d.height/2;
		view.getPanel().setDrawCursor(false);
		view.setJava2DPainter(paintCursor, Java2DPainter.AFTER_PORTALS);
	}
	
	public void stop() {
		lens.setXfocusOffset(0);
		lens.setYfocusOffset(0);
		started = false; 
		view.getPanel().setDrawCursor(true);
		if(view.getPanel() != null) {
			view.setEventHandler(actualViewEventHandler);
			view.setJava2DPainter(actualAfterPortalsPainter, Java2DPainter.AFTER_PORTALS);
		}
	}
	
	public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.press1(v, mod, jpx, jpy, e);
	}

	public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.release1(v, mod, jpx, jpy, e);
	}
	
	public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.click1(v, mod, jpx, jpy, clickNumber, e);
	}
	
	public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.press2(v, mod, jpx, jpy, e);
	}
	
	public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.release2(v, mod, jpx, jpy, e);
	}
	
	public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.click2(v, mod, jpx, jpy, clickNumber, e);
	}
	
	public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.press3(v, mod, jpx, jpy, e);
	}
	
	public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.release3(v, mod, jpx, jpy, e);
	}
	
	public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.click3(v, mod, jpx, jpy, clickNumber, e);
	}
	
	public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {
		
		if(!started || robot == null) return;
		
		if(e.getX() == lastXLensCenter && e.getY() == lastYLensCenter)
			return;
			
		dx = lens.getXfocusOffset() + (e.getX() - lastXLensCenter);
		dy = lens.getYfocusOffset() + (e.getY() - lastYLensCenter);
		
		speed = sf.getSpeedCoeff(e.getWhen(), e.getX(), e.getY());
		magFactor = 1 + (1-speed) * (lens.getMaximumMagnification() - 1);
		
		lastXLensCenter = lastXLensCenter + dx / (int)magFactor;
		lastYLensCenter = lastYLensCenter + dy / (int)magFactor;
		
		if(robot != null) {
			ptRobot.setLocation(lastXLensCenter, lastYLensCenter);
			SwingUtilities.convertPointToScreen(ptRobot, e.getComponent());
			robot.mouseMove((int)ptRobot.getX(), (int)ptRobot.getY());
		}
		
		lens.setXfocusOffset(dx % (int)magFactor);
		lens.setYfocusOffset(dy % (int)magFactor);
		
		if (tLens != null){
		    tLens.setAbsolutePosition(lastXLensCenter, lastYLensCenter, e.getWhen());
		}
		else {
		    lens.setAbsolutePosition(lastXLensCenter, lastYLensCenter);
		}
		view.repaintNow();
	}
	
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.mouseDragged(v, mod, buttonNumber, jpx, jpy, e);
	}
	
	public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.mouseWheelMoved(v, wheelDirection, jpx, jpy, e);
	}
	
	public void enterGlyph(Glyph g) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.enterGlyph(g);
	}
	
	public void exitGlyph(Glyph g) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.exitGlyph(g);
	}
	
	public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.Ktype(v, c, code, mod, e);
	}
	
	public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.Kpress(v, c, code, mod, e);
	}
	
	public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.Krelease(v, c, code, mod, e);
	}
	
	public void viewActivated(View v) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.viewActivated(v);
	}
	
	public void viewDeactivated(View v) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.viewDeactivated(v);
	}
	
	public void viewIconified(View v) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.viewIconified(v);
	}
	
	public void viewDeiconified(View v) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.viewDeiconified(v);
	}
	
	public void viewClosing(View v) {
		if(actualViewEventHandler != null)
			actualViewEventHandler.viewClosing(v);
	}
	
}

interface SpeedFunction {
	double getSpeedCoeff(long currentTime, int x, int y);
}

class LSpeedFunction implements SpeedFunction {

	static final int NB_SPEED_POINTS = 4;
	static final int MIN_SPEED = 100;
	static final int MAX_SPEED = 300;
	long[] cursor_time = new long[NB_SPEED_POINTS];
	int[] cursor_x = new int[NB_SPEED_POINTS];
	int[] cursor_y = new int[NB_SPEED_POINTS];

	float[] speeds = new float[NB_SPEED_POINTS-1];
	float mean_speed = 0;

	public LSpeedFunction() { }

	public void getSpeed(long currentTime, int x, int y) {
		// compute mean speed over last 3 points
		for (int i=1;i<NB_SPEED_POINTS;i++){
			cursor_time[i-1] = cursor_time[i];
			cursor_x[i-1] = cursor_x[i];
			cursor_y[i-1] = cursor_y[i];
		}
		cursor_time[NB_SPEED_POINTS-1] = currentTime;
		cursor_x[NB_SPEED_POINTS-1] = x;
		cursor_y[NB_SPEED_POINTS-1] = y;
		for (int i=0;i<speeds.length;i++){
			if(cursor_time[i+1] != cursor_time[i])
				speeds[i] = (float)Math.sqrt(Math.pow(cursor_x[i+1]-cursor_x[i],2)+Math.pow(cursor_y[i+1]-cursor_y[i],2)) / (float)(cursor_time[i+1]-cursor_time[i]);
			else
				speeds[i] = 0;
		}
		mean_speed = 0;
		for (int i=0;i<speeds.length;i++){
			mean_speed += speeds[i];
		}
		mean_speed = mean_speed / (float)speeds.length * 1000;
	}

	public double getSpeedCoeff(long currentTime, int x, int y) {
		getSpeed(currentTime, x, y);
		if(mean_speed <= MIN_SPEED) return 0;
		if(mean_speed >= MAX_SPEED) return 1;
		return (mean_speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
	}

}
