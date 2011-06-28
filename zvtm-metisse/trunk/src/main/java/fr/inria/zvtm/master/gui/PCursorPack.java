package fr.inria.zvtm.master.gui;

import java.awt.Dimension;

import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.gui.menu.GlyphEventDispatcherForMenu;
import fr.inria.zvtm.common.gui.menu.PopMenu;
import fr.inria.zvtm.engine.Camera;

public class PCursorPack {
	private PCursor cursor;
	private PopMenu menu;
	private MasterViewer viewer;
	protected GlyphEventDispatcherForMenu ged;
	private MasterMainEventHandler meh;
	private int lastMask= 0;
	private int lastpressed=-1;
	private boolean isPressed=false;
	private boolean consumed = false;



	public PCursorPack(MasterViewer v) {
		this.viewer = v;
		meh = new MasterMainEventHandler(this);
		meh.setViewer(viewer);
		cursor = new PCursor(viewer.getCursorSpace(), viewer.getVirtualSpace(), viewer.getMenuSpace(), viewer.getNavigationManager().getCamera(), viewer.getMenuCamera(), meh, 4, 80);
		cursor.setVisible(false);
		ged = new GlyphEventDispatcherForMenu(cursor, viewer.getMenuSpace(), viewer);
		menu = new PopMenu(viewer.getMenuSpace(), viewer,ged,4);
		menu.setOwner(this);
		ged.setMenu(menu);
		ged.setPriorityOn(meh);
		((GEDMultiplexer)viewer.getGlyphEventDispatcher()).subscribe(menu,ged);
	//	viewer.sendViewUpgrade();
	}

	public void end() {
		((GEDMultiplexer)viewer.getGlyphEventDispatcher()).unsubscribe(menu);
		menu.banish();
		cursor.end();
	}

	public void handlePointerEvent(double x, double y, int buttons) {
		if(!consumed ){
			cursor.setVisible(true);
			consumed = true;
		}
		int dif = buttons ^ lastMask;
		if(dif!=0){//some button states have changed
			for (int i = 0; i < 3; i++) {//one of the 3 buttons of the mouse
				if((dif&(1<<i))!=0){//button i has changed
					if((buttons&(1<<i))!=0){//it is now down
						isPressed = true;
						lastpressed = i;
						meh.press(x,y,i+1,buttons);
					}
					else {//it is now up
						isPressed = false;
						meh.release(x,y,i+1,buttons);
						if(lastpressed==i)meh.click(x,y,i+1,buttons);
					}
				}
			}
			
			for (int i = 3; i < 5; i++) {//buttons 4 and 5 for wheel move
				if((dif&(1<<i))!=0){//button i
					if((buttons&(1<<i))!=0){
						lastpressed = i;
					}
					else {
						if(lastpressed==i)meh.mouseWheelMove(x,y,i-3,buttons);
						lastpressed = -1;
					}
				}
			}
		}
		else{//simple mouseMove
			if(isPressed){
				int[] co = unproject(x, y);
				int jpxx = co[0];
				int jpyy = co[1];
				cursor.moveCursorTo(x, y, jpxx, jpyy);
				meh.mouseDragged(x,y,buttons);
				lastpressed= -1;
			}
			else {
				int[] co = unproject(x, y);
				int jpxx = co[0];
				int jpyy = co[1];
				cursor.moveCursorTo(x, y, jpxx, jpyy);
				meh.mouseMoved(x,y,buttons);
			}
		}
		lastMask = buttons;
	}

	public void handleRemoteKeyEvent(int keysym, int i) {
		if(i==1)meh.Kpress(keysym);
		if(i==0)meh.Krelease(keysym);
	}
	
	public PCursor getCursor(){
		return cursor;
	}

	public PopMenu getMenu() {
		return menu;
	}
	
	public int[] unproject(double vx,double vy){
		int[] res = new int[2];
		Camera c = viewer.getVirtualSpace().getCamera(0);
		Dimension d = viewer.getVirtualSpace().getCamera(0).getOwningView().getPanelSize();
		double coef = c.focal / (c.focal+c.altitude);
		int cx = (int)Math.round((d.width/2)+(vx-c.vx)*coef);
		int cy = (int)Math.round((d.height/2)-(vy-c.vy)*coef);
		res[0] = cx;
		res[1] = cy;
		return res;
	}

	
}
