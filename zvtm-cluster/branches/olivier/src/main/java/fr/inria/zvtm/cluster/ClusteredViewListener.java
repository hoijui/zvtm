/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface  ClusteredViewListener {

	public void press1(int blockNum, int mod, int jpx, int jpy);
	public void release1(int blockNum, int mod, int jpx, int jpy);
	public void click1(int blockNum, int mod, int jpx, int jpy, int clickNumber);
	public void press2(int blockNum, int mod, int jpx, int jpy);
	public void release2(int blockNum, int mod, int jpx, int jpy);
	public void click2(int blockNum, int mod, int jpx, int jpy, int clickNumber);
	public void press3(int blockNum, int mod, int jpx, int jpy);
	public void release3(int blockNum, int mod, int jpx, int jpy);
	public void click3(int blockNum, int mod, int jpx, int jpy, int clickNumber);
	public void mouseMoved(int blockNum, int jpx, int jpy);
	public void mouseDragged(int blockNum, int mod, int buttonNumber, int jpx, int jpy);
	public void mouseWheelMoved(int blockNum, short wheelDirection, int jpx, int jpy);
	public void Ktype(int blockNum, char c, int code, int mod);
	public void Kpress(int blockNum, char c, int code, int mod);
	public void Krelease(int blockNum, char c, int code, int mod);
	//
	public void viewActivated(int blockNum);
	public void viewDeactivated(int blockNum);
	public void viewDeiconified(int blockNum);
	public void viewClosing(int blockNum);
}

