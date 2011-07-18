package fr.inria.zvtm.common.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

public interface GlyphListener {

	/**
	 * Callback for the MousePressed event when the button is the left button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	/**
	 * Callback for the MouseReleased event when the button is the left button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void release1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	/**
	 * Callback for the MouseClicked event when the button is the left button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param clicknumber
	 * @param e The related {@link MouseEvent}
	 */
	void click1(ViewPanel v, int mod, int jpxx, int jpyy, int clickNumber,MouseEvent e);

	/**
	 * Callback for the MousePressed event when the button is the middle button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void press2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	/**
	 * Callback for the MouseReleased event when the button is the middle button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void release2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);
	
	/**
	 * Callback for the MouseClicked event when the button is the middle button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param clicknumber
	 * @param e The related {@link MouseEvent}
	 */
	void click2(ViewPanel v, int mod, int jpxx, int jpyy, int clickNumber,MouseEvent e);

	/**
	 * Callback for the MousePressed event when the button is the right button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void press3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	/**
	 * Callback for the MouseReleased event when the button is the right button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void release3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	/**
	 * Callback for the MouseClicked event when the button is the right button.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param clicknumber
	 * @param e The related {@link MouseEvent}
	 */
	void click3(ViewPanel v, int mod, int jpxx, int jpyy, int clickNumber,MouseEvent e);

	/**
	 * Callback for the MouseMoved event.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e);

	/**
	 * Callback for the MouseDragged event.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param mod modifier mask
	 * @param buttonNumber (1 for left, 2 for middle, 3 for right)
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e);
	
	/**
	 * Callback for the WheelMoved event.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param wheelDirection (0 for down, 1 for up)
	 * @param jpxx x coordinate in the panel
	 * @param jpyy y coordinate in the panel
	 * @param e The related {@link MouseEvent}
	 */
	void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpxx, int jpyy,MouseWheelEvent e);

	/**
	 * Callback for KeyPressed event.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param c The corresponding character (if applicable) 
	 * @param code the virtual key code
	 * @param mod modifier mask
	 * @param e The related {@link KeyEvent}
	 */
	void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e);

	/**
	 * Callback for KeyReleased event.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param c The corresponding character (if applicable) 
	 * @param code the virtual key code
	 * @param mod modifier mask
	 * @param e The related {@link KeyEvent}
	 */
	void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e);

	/**
	 * Callback for KeyTyped event.
	 * @param v The original {@link ViewPanel} where the event was triggered.
	 * @param c The corresponding character (if applicable) 
	 * @param code the virtual key code
	 * @param mod modifier mask
	 * @param e The related {@link KeyEvent}
	 */
	void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e);

	/**
	 * Callback for when a {@link Glyph} is entered.
	 */
	void glyphEntered();

	/**
	 * Callback for when a {@link Glyph} is exited.
	 */
	void glyphExited();

}
