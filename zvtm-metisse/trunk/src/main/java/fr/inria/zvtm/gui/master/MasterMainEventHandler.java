package fr.inria.zvtm.gui.master;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.gui.MainEventHandler;
import fr.inria.zvtm.gui.NavigationManager;
import fr.inria.zvtm.gui.Viewer;

public class MasterMainEventHandler extends MainEventHandler{


	private NavigationManager nm;
	private MasterViewer viewer;

	@Override
	public void setViewer(Viewer viewer) {
		MasterViewer v = (MasterViewer)viewer;
		this.nm = v.getNavigationManager();
		this.viewer = v;
	}
	
	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}
	
	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpxx,int jpyy, MouseEvent e){}
	
	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpxx,int jpyy, MouseWheelEvent e){}
	
	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}
	
	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	@Override
	public void click(ViewPanel v, int mod, int jpxx, int jpyy,int clickNumber, MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void press(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void release(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
