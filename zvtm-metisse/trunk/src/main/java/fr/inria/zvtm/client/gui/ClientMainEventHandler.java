package fr.inria.zvtm.client.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.client.compositor.ForwardingFrameManager;
import fr.inria.zvtm.common.compositor.InputForwarder;
import fr.inria.zvtm.common.gui.CursorHandler;
import fr.inria.zvtm.common.gui.MainEventHandler;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.engine.ViewPanel;

/**
 * This is a specific inherited class from {@link MainEventHandler}, designed to handle a cursor's virtual mode (off screen) and to send events to the zvtm server
 * 
 * @author Julien Altieri
 * 
 */
public class ClientMainEventHandler extends MainEventHandler {

	private	boolean MOVE_MODE = false;
	private int menuinvocationmarker= 0;
	private int move_mode_key = KeyEvent.VK_WINDOWS;
	private double vxTrace;
	private double vyTrace;
	private CursorHandler ch;
	private int[]buttonState = new int[32];
	private short lastwheeldirection = WHEEL_UP;
	private boolean controlHasBeenPressed = false;
	public InputForwarder infw;

	@Override
	public void setViewer(Viewer viewer){		
		ClientViewer v = (ClientViewer)viewer;
		this.ch = v.getCursorHandler();
		this.infw = v.getInputForwarder();
		this.viewer = viewer;
	}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
		if(ch==null||ch.getCursor()==null||viewer==null||viewer.getFrameManager()==null)return;
		ch.move(jpx,jpy, e);
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		if(locked)return;
		if(ch.VirtualMode()){
			((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(true, -1));
		}
		infw.move(v,ch.getX(),ch.getY(),e);
	}


	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpxx,int jpyy, MouseEvent e){
		if(ch==null||ch.getCursor()==null||viewer==null||viewer.getFrameManager()==null)return;
		menuinvocationmarker = 0;
		ch.move(jpxx,jpyy, e);
		int jpx = ch.getX();
		int jpy = ch.getY();
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		if(ch.VirtualMode()){
			((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(true,e.getButton()));
			return;
		}
		if(locked)return;
		infw.move(v,jpx,jpy,e);
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpxx,int jpyy, MouseWheelEvent e){		
		if(locked)return;
		if(ch==null||ch.getCursor()==null||viewer==null||viewer.getFrameManager()==null)return;
		ch.move(jpxx,jpyy, e);
		int jpx = ch.getX();
		int jpy = ch.getY();
		if(ch.VirtualMode()){
			if(wheelDirection==WHEEL_UP){
				((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(true,8));
				((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(false,8));
			}
			else{
				((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(true,16));
				((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(false,16));				
			}
			return;
		}
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

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if(locked)return;
		if(ch==null||ch.getCursor()==null||viewer==null||viewer.getFrameManager()==null)return;
		if(code==KeyEvent.VK_F12){
			ch.resetCursorPos();
		}
		if(ch.VirtualMode()){
			((ForwardingFrameManager)viewer.getFrameManager()).sendKeyEvent(InputForwarder.getKeysym(c,code),1);
			return;
		}
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
			if (code==KeyEvent.VK_ALT)ch.deactivate();
		}
		else{
			infw.Kpress(e);
		}
	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if(locked)return;
		if(ch==null||ch.getCursor()==null||viewer==null||viewer.getFrameManager()==null)return;
		if(ch.VirtualMode()){
			((ForwardingFrameManager)viewer.getFrameManager()).sendKeyEvent(InputForwarder.getKeysym(c,code),0);
			return;
		}
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

	/**
	 * Invokes the PopMenu if it is not already done,
	 * Banish it if already invoked
	 */
	public void toggleMenu(){
		int jpx = ch.getX();
		int jpy = ch.getY();
		((ClientViewer)viewer).popmenu.toggle(vxTrace, vyTrace, viewer.getFrameManager().get(infw.detectWindow(jpx, jpy)));
	}

	/**
	 * Invokes the PopMenu at the current cursor's position
	 */
	public void invokeMenu(){
		int jpx = ch.getX();
		int jpy = ch.getY();
		vxTrace = ch.getVX();
		vyTrace = ch.getVY();
		((ClientViewer)viewer).popmenu.invoke(vxTrace, vyTrace, viewer.getFrameManager().get(infw.detectWindow(jpx, jpy)));
	}

	/**
	 * Make the Menu Disappear
	 */
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
		if(ch.VirtualMode()){

			return;
		}
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
		if(ch.VirtualMode()){
			((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(true,e.getButton()));
			return;
		}
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
		if(ch.VirtualMode()){
			((ForwardingFrameManager)viewer.getFrameManager()).sendDoublePointerEvent(ch.getCursor().getWallX(),ch.getCursor().getWallY(),getButtonMask(false,e.getButton()));
			return;
		}
		infw.release(mod,v,jpx,jpy,e);
	}

	private int getButtonMask(boolean pressed,int j) {
		if(pressed){
			switch (j) {
			case MouseEvent.BUTTON1:
				buttonState[0] = 1;
				break;
			case MouseEvent.BUTTON2:
				buttonState[1] = 1;
				break;
			case MouseEvent.BUTTON3:
				buttonState[2] = 1;
				break;
			case 8:
				buttonState[3] = 1;
				break;
			case 16:
				buttonState[4] = 1;
				break;
			default:
				break;
			}
		}
		else {
			switch (j){
			case MouseEvent.BUTTON1:
				buttonState[0] = 0;
				break;
			case MouseEvent.BUTTON2:
				buttonState[1] = 0;
				break;
			case MouseEvent.BUTTON3:
				buttonState[2] = 0;
				break;
			case 8:
				buttonState[3] = 0;
				break;
			case 16:
				buttonState[4] = 0;
				break;
			default:
				break;
			}
		}
		int a = 0;
		for (int i = 0; i < 32; i++) {
			a = a | (buttonState[i]<<i);
		}

		return a;

	}
}
