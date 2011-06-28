package fr.inria.zvtm.master.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.common.gui.MainEventHandler;
import fr.inria.zvtm.common.protocol.Keysym;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

public class MasterMainEventHandler extends MainEventHandler{

	private PCursorPack owner;
	private int menuinvocationmarker= 0;
	private short lastwheeldirection = WHEEL_UP;
	private boolean controlHasBeenPressed = false;
	private int currentwindow;


	public MasterMainEventHandler(PCursorPack owner) {
		super();
		this.owner = owner;
	}
	
	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}
	
	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpxx,int jpyy, MouseEvent e){}
	
	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpxx,int jpyy, MouseWheelEvent e){}
	
	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}
	
	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	@Override
	public void click(ViewPanel v, int mod, int jpxx, int jpyy,int clickNumber, MouseEvent e) {}

	@Override
	public void press(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}

	@Override
	public void release(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}

	
	/***************************************************************************************
	 * 						          Events from the network                               *
	 **************************************************************************************/
	
	
	public void press(double x, double y, int i,int buttonMask) {
		owner.ged.press(x,y,i);
		if(locked)return;
		currentwindow = detectWindow();
		upStack(viewer.getFrameManager().get(currentwindow));//put on top the window
		int[] p = unproject();
		if(p!=null && testRight())
		((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask,p[2]);
	}

	public void release(double x, double y, int i,int buttonMask) {
		owner.ged.release(x,y,i);
		if(locked)return;
		int[] p = unproject();
		if(p!=null&& testRight())
		((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask,p[2]);
	}

	public void click(double x, double y, int i,int buttonMask) {
		owner.ged.click(x,y,i);
	}

	public void mouseDragged(double x, double y,int buttonMask) {
		owner.ged.mouseDragged(x,y);
		if(locked)return;
		int[] p = unproject();
		if(p!=null&& testRight())
		((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask,p[2]);
	}

	public void mouseMoved(double x, double y,int buttonMask) {
		owner.ged.mouseMoved(x,y);
		if(locked)return;
		currentwindow = detectWindow();
		int[] p = unproject();
		if(p!=null&& testRight())
		((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask,p[2]);
	}

	public void mouseWheelMove(double x, double y, int wheelDirection,int buttonMask) {
		owner.ged.mouseWheelMove(x,y,wheelDirection);
		if(locked)return;
		currentwindow = detectWindow();
		int[] p = unproject();
		if(p!=null&& testRight()){
			if(wheelDirection==1){//up
				((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask|(1<<5),p[2]);
				((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask,p[2]);
			}
			else{
				((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask|(1<<4),p[2]);
				((MasterViewer) viewer).getBouncer().handleMouse(p[0],p[1],buttonMask,p[2]);
			}
		}
		if(wheelDirection!=lastwheeldirection){//process to invoke the menu
			if(menuinvocationmarker==2){
				lastwheeldirection = -1;
				menuinvocationmarker = 0;
				invokeMenu();
			}
			else if(menuinvocationmarker==1){
				lastwheeldirection = (short) wheelDirection;
				menuinvocationmarker=2 ;
			}
			else if(menuinvocationmarker==0){
				lastwheeldirection = (short) wheelDirection;
				menuinvocationmarker=1 ;
			}
			else {
				lastwheeldirection = (short) wheelDirection;
				menuinvocationmarker=0 ;
			}			
		}
		else{
			menuinvocationmarker = 0;
		}
		
	}

	public void Kpress(int keysym) {
		if(locked)return;
		if(keysym==Keysym.ControlL){
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
		currentwindow = detectWindow();
		if(testRight())
		((MasterViewer) viewer).getBouncer().handleKey(keysym,true,currentwindow);
	}
	
	public void Krelease(int keysym) {
		if(locked)return;
		if(keysym==Keysym.ControlL){
			if(controlHasBeenPressed){
				toggleMenu();
				controlHasBeenPressed =false;
			}

		}else{
			controlHasBeenPressed = false;
		}
		currentwindow = detectWindow();
		
		if(((MasterViewer) viewer).getBouncer().testOwnership(owner, currentwindow)&&viewer.getFrameManager().get(currentwindow)!=null)
		viewer.getFrameManager().get(currentwindow).endResize();
		
		if(testRight())
		((MasterViewer) viewer).getBouncer().handleKey(keysym,false,currentwindow);
		
	}

	public int detectWindow(){
		if(owner.getCursor().getPicker().getDrawOrderedPickedGlyphList(viewer.getVirtualSpace()).length==0)return -1;
		Glyph up = owner.getCursor().getPicker().pickOnTop(viewer.getVirtualSpace());
		if(up==null || !up.getClass().equals(MetisseWindow.class))return -1;
		return ((MetisseWindow)up).getId();
	}
	
	
	public void toggleMenu(){
		owner.getMenu().toggle(owner.getCursor().getVSXCoordinate(), owner.getCursor().getVSYCoordinate(), viewer.getFrameManager().get(currentwindow));
	}

	public void invokeMenu(){
		owner.getMenu().invoke(owner.getCursor().getVSXCoordinate(), owner.getCursor().getVSYCoordinate(), viewer.getFrameManager().get(currentwindow));
	}

	public void banishMenu(){
		lastwheeldirection = -1;
		menuinvocationmarker = 0;
		owner.getMenu().banish();
	}


	public int[] unproject(){
		if(viewer==null)return null;
		if(viewer.getFrameManager()==null)return null;
		int[] res = new int[3];
		double sf = 1;//scale factor
		int xx,yy;
		MetisseWindow cu = viewer.getFrameManager().get(currentwindow);
		if(cu==null)return null;
		double[] cubounds = cu.getBounds();
		sf = cu.getScaleFactor();
		xx = (int) (cu.getX()+ (owner.getCursor().getVSXCoordinate()-cubounds[0])/sf);
		yy = (int) (cu.getY()- (owner.getCursor().getVSYCoordinate()-cubounds[1])/sf);
		res[0] = xx;
		res[1] = yy;
		res[2] = currentwindow;
		return res;
	}

	
	private boolean testRight() {
	//	if(((MasterViewer) viewer).getBoucer().testOwnership(owner,currentwindow))return true;
		return viewer.getFrameManager().get(currentwindow)!=null &&viewer.getFrameManager().get(currentwindow).isShared();
	}
}
