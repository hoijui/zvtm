package fr.inria.zvtm.gui.client;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.compositor.InputForwarder;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.gui.CursorHandler;
import fr.inria.zvtm.gui.MainEventHandler;
import fr.inria.zvtm.gui.Viewer;
import fr.inria.zvtm.gui.menu.PopMenu;


public class ClientMainEventHandler extends MainEventHandler {

	private	boolean MOVE_MODE = false;
	private int menuinvocationmarker= 0;
	private int move_mode_key = KeyEvent.VK_WINDOWS;
	private double vxTrace;
	private double vyTrace;
	private CursorHandler ch;
	public InputForwarder infw;

	@Override
	public void setViewer(Viewer viewer){		
		ClientViewer v = (ClientViewer)viewer;
		this.ch = v.getCursorHandler();
		this.infw = v.getInputForwarder();
		this.viewer = viewer;
	}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
		if(ch==null)return;
		ch.move(jpx,jpy, e);
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		if(locked)return;
		infw.move(v,ch.getX(),ch.getY(),e);
	}


	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpxx,int jpyy, MouseEvent e){
		menuinvocationmarker = 0;
		ch.move(jpxx,jpyy, e);
		int jpx = ch.getX();
		int jpy = ch.getY();
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		if(locked)return;
		infw.move(v,jpx,jpy,e);
	}

	private short lastwheeldirection = WHEEL_UP;

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpxx,int jpyy, MouseWheelEvent e){		
		if(locked)return;
		ch.move(jpxx,jpyy, e);
		int jpx = ch.getX();
		int jpy = ch.getY();
		infw.wheel(0, v, jpx, jpy, e);
		if(wheelDirection!=lastwheeldirection){//process to invoke the menu
			if(menuinvocationmarker==2){
				lastwheeldirection = -1;
				menuinvocationmarker = 0;
				invokeMenu();
			}
			else if(menuinvocationmarker==1){
				lastwheeldirection = wheelDirection;
				menuinvocationmarker=2 ;
			}
			else if(menuinvocationmarker==0){
				lastwheeldirection = wheelDirection;
				menuinvocationmarker=1 ;
			}
			else {
				lastwheeldirection = wheelDirection;
				menuinvocationmarker=0 ;
			}			
		}
		else{
			menuinvocationmarker = 0;
		}
	}

	private boolean controlHasBeenPressed = false;
	
	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if(locked)return;
		if(code==KeyEvent.VK_CONTROL){
			if(controlHasBeenPressed){
				toggleMenu();
				controlHasBeenPressed =false;
			}
			else{
				controlHasBeenPressed = true;
			}
		}else{
			controlHasBeenPressed = false;
		}
		if(code==move_mode_key)MOVE_MODE = true;
		if(MOVE_MODE){
			if (code==KeyEvent.VK_R){ch.resetCursorPos();}
			else if (code==KeyEvent.VK_ALT)ch.deactivate();
			else if (code==KeyEvent.VK_C)ch.getCursor().togglePhantomMode();
		}
		else{
			infw.Kpress(e);
		}
	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if(locked)return;
		if (code==move_mode_key){
			MOVE_MODE = false;
		}
		if(code==KeyEvent.VK_CONTROL){
			if(controlHasBeenPressed){
				toggleMenu();
				controlHasBeenPressed =false;
			}
			
		}else{
			controlHasBeenPressed = false;
		}
		if(!MOVE_MODE){
			infw.Kreles(e);
		}
		if (code==KeyEvent.VK_ALT)ch.activate();
	}

	public void toggleMenu(){
		int jpx = ch.getX();
		int jpy = ch.getY();
		((ClientViewer)viewer).popmenu.toggle(vxTrace, vyTrace, viewer.getFrameManager().get(infw.detectWindow(jpx, jpy)),PopMenu.DEFAULTACTIVERADIUS);
	}

	public void invokeMenu(){
		int jpx = ch.getX();
		int jpy = ch.getY();
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		((ClientViewer)viewer).popmenu.invoke(vxTrace, vyTrace, viewer.getFrameManager().get(infw.detectWindow(jpx, jpy)),PopMenu.DEFAULTACTIVERADIUS);
	}

	public void banishMenu(){
		lastwheeldirection = -1;
		menuinvocationmarker = 0;
		((ClientViewer)viewer).popmenu.banish();
	}

	@Override
	public void click(ViewPanel v, int mod, int jpxx, int jpyy,int clickNumber, MouseEvent e) {
		menuinvocationmarker = 0;
		if(locked)return;
		ch.move(jpxx,jpyy, e);
		int jpx = ch.getX();
		int jpy = ch.getY();
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		infw.click(mod, jpx, jpy, clickNumber, e);
	}

	@Override
	public void press(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		menuinvocationmarker = 0;
		if(locked)return;
		ch.move(jpxx,jpyy,e);
		int jpx = ch.getX();
		int jpy = ch.getY();
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		upStack(viewer.getFrameManager().get(infw.detectWindow(jpx, jpy)));//put on top the window
		infw.press(mod,v,jpx,jpy,e);
	}

	@Override
	public void release(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		menuinvocationmarker = 0;
		if(locked)return;
		ch.move(jpxx,jpyy, e);
		int jpx = ch.getX();
		int jpy = ch.getY();
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		infw.release(mod,v,jpx,jpy,e);
	}
}
