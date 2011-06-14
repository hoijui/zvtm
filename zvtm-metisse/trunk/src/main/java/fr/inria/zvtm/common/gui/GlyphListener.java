package fr.inria.zvtm.common.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.engine.ViewPanel;

public interface GlyphListener {

	void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	void release1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	void click1(ViewPanel v, int mod, int jpxx, int jpyy, int clickNumber,
			MouseEvent e);

	void press2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	void release2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	void click2(ViewPanel v, int mod, int jpxx, int jpyy, int clickNumber,
			MouseEvent e);

	void press3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	void release3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e);

	void click3(ViewPanel v, int mod, int jpxx, int jpyy, int clickNumber,
			MouseEvent e);

	void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e);

	void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,
			int jpyy, MouseEvent e);

	void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpxx, int jpyy,
			MouseWheelEvent e);

	void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e);

	void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e);

	void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e);

	void glyphEntered();

	void glyphExited();

}
