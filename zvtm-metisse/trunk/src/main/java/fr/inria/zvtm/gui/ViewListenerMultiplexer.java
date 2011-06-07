package fr.inria.zvtm.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedList;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;

public class ViewListenerMultiplexer implements ViewListener{
	
	private LinkedList<ViewListener> dispatchTable = new LinkedList<ViewListener>();
	
	public void addListerner(ViewListener vl){
		dispatchTable.addLast(vl);
	}
	
	public void removeListener(ViewListener vl){
		dispatchTable.remove(vl);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.Kpress(v,c,code,mod,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.Krelease(v,c,code,mod,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.Ktype(v,c,code,mod,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber,
			MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.click1(v,mod,jpx,jpy,clickNumber,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber,
			MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.click2(v,mod,jpx,jpy,clickNumber,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber,
			MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.click3(v,mod,jpx,jpy,clickNumber,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void enterGlyph(Glyph g) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.enterGlyph(g);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void exitGlyph(Glyph g) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.exitGlyph(g);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx,
			int jpy, MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.mouseDragged(v,mod,buttonNumber,jpx,jpy,e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {
		LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
				vl.mouseMoved(v,jpx,jpy,e);
			}
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx,
			int jpy, MouseWheelEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.mouseWheelMoved(v,wheelDirection,jpx,jpy,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.press1(v,mod,jpx,jpy,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.press2(v,mod,jpx,jpy,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.press3(v,mod,jpx,jpy,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.release1(v,mod,jpx,jpy,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.release2(v,mod,jpx,jpy,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.release3(v,mod,jpx,jpy,e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void viewActivated(View v) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.viewActivated(v);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void viewClosing(View v) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.viewClosing(v);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void viewDeactivated(View v) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.viewDeactivated(v);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void viewDeiconified(View v) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.viewDeiconified(v);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void viewIconified(View v) {
			LinkedList<ViewListener> list = (LinkedList<ViewListener>) dispatchTable.clone();
			for (ViewListener vl : list) {
			vl.viewIconified(v);
		}
	}

}
