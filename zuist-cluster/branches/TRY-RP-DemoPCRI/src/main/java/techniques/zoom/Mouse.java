package techniques.zoom;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import fr.inria.zuist.cluster.viewer.Viewer;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;


public class Mouse extends AbstractZoomTechnique {
	
	public static final float WHEEL_ZOOMIN_FACTOR = 2*5f;
	public static final float WHEEL_ZOOMOUT_FACTOR = 2*5f;
	
	public static final int CYCLIC_TIMEOUT = 300;
	
	protected Robot robot = null;
	protected ViewListener handler;
	
	protected int currentDirection = 0;
	protected long lastEventTime = 0;
	
	public Mouse(String id, ORDER o) {
		super(id, o, false);
		// TODO Auto-generated constructor stub
		
		System.out.println("\tnew Mouse()");
		
		this.cyclic = false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initListeners() {
		
		/* ---------------------- Mouse Event handler -------------------*/

		class ViewerEventHandler implements ViewListener{

			private int lastJPX = -1;
			private int lastJPY = -1;

			private int robX = 640;
			private int robY = 300;

			boolean zeroOrder = false;
			boolean doRecenterCursor = false;

			Point tmpp = new Point();

			void recenterCursor(int x, int y, MouseEvent e) {
				if (!doRecenterCursor)
					return;
				tmpp.setLocation(robX, robY);
				SwingUtilities.convertPointToScreen(tmpp, e.getComponent());
				robot.mouseMove(tmpp.x, tmpp.y);
			}
			public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
				lastJPX = jpx;
				lastJPY = jpy;
				v.setDrawDrag(true);
				Viewer.getInstance().getVirtualSpaceManager().getActiveView().mouse.setSensitivity(false);
				// Viewer.getInstance().getCheckTargetTimer().start();
			}

			public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
				// Viewer.getInstance().firstOrderStop();
				v.setDrawDrag(false);
				Viewer.getInstance().getVirtualSpaceManager().getActiveView().mouse.setSensitivity(true);
				// Viewer.getInstance().getCheckTargetTimer().stop();
				// Viewer.getInstance().checkTargetHit(true);
			}

			public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

			public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

			public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

			public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

			public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
			}

			public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
			}

			public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

			boolean wait_robot = true;

			int myx = robX;
			int myy = robY;
			boolean getFirstMouseMove = false;
			int dragCDRX = 1; // 10
			int dragCDRY = 1; //  8

			public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
				if (doRecenterCursor)
				{
					if (!getFirstMouseMove)
					{
						myx = 0;
						myy = 0;
						Viewer.getInstance().setZoomOrigin(0, 0);
						getFirstMouseMove = true;
						recenterCursor(jpx,jpy,e);
						wait_robot = true;
						return;
					}
					if (!wait_robot && jpx != robX && jpy != robY)
					{
						myx = myx - dragCDRX*(robX-jpx);
						myy = myy - dragCDRY*(jpy-robY);
						//setZoomOrigin(v.getVCursor().vx, v.getVCursor().vy);
						Viewer.getInstance().setZoomOrigin(myx, myy);
						recenterCursor(jpx,jpy,e);
						wait_robot = true;
					}
					else
					{
						wait_robot = false;
					}
				}
				else
				{
					//setZoomOrigin(v.getVCursor().vx, v.getVCursor().vy); 
					//firstOrderTranslate(jpx-lastJPX, lastJPY-jpy);
					//lastJPX = jpx;
					//lastJPY = jpy;
				}

			}

			public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
				if (buttonNumber == 1){
				}
				else if (buttonNumber == 3){
				   //  Viewer.getInstance().firstOrderViewer(jpy-lastJPY);
				    addAltitudeSample();
				}
			}

			long prevTime = 0;
			long prevTime2 = 0;
			long prevTime3 = 0;
			public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){  
				
				int accel = 0;
				
				// true if this event corresponds to a cyclic move, i.e. the direction shouldn't change
				boolean cyclicMove = cyclic && System.currentTimeMillis() - lastEventTime < CYCLIC_TIMEOUT;
				
				// System.out.println(cyclic + ", " + (System.currentTimeMillis() - lastEventTime) + ", " + cyclicMove + ", " + currentDirection);
				
				if ( (cyclicMove && currentDirection == 1) || (!cyclicMove && wheelDirection == WHEEL_UP) ){
					
					currentDirection = 1;
					
					// zooming out
					
					accel = 1;
					if (e.getWhen()-prevTime < 50 && prevTime-prevTime2 < 50)
					{
						accel = 1;
						if (prevTime2-prevTime3 < 50)
							accel = 4;
					}
					
					//System.out.println("Wheel OUT Time: " + (e.getWhen()-prevTime) + " aceel: " + accel);
					
				}
				else if ( (cyclicMove && currentDirection == -1) || (!cyclicMove && wheelDirection == WHEEL_DOWN) ) {
					
					currentDirection = -1;
					
					// zooming in

					accel = 1;
					if (e.getWhen()-prevTime < 50 && prevTime-prevTime2 < 50)
					{
						accel = 2;
						if (prevTime2-prevTime3 < 50)
							accel = 4;
					}
					//System.out.println("Wheel IN Time: " + (e.getWhen()-prevTime) + " accel: " + accel);
					
				}
				
				Viewer.getInstance().zeroOrderZoom(currentDirection * accel * WHEEL_ZOOMIN_FACTOR);
				addAltitudeSample();
				prevTime3 = prevTime2;
				prevTime2 = prevTime;
				prevTime = e.getWhen();
				
				lastEventTime = System.currentTimeMillis();
				
			}

			public void enterGlyph(Glyph g){
			}

			public void exitGlyph(Glyph g){
			}

			public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

			public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
				// does not work with noe ?
			}

			public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

			public void viewActivated(View v){}

			public void viewDeactivated(View v){}

			public void viewIconified(View v){}

			public void viewDeiconified(View v){}

			public void viewClosing(View v){System.exit(0);}

		}
		
		Viewer.getInstance().getView().setListener(new ViewerEventHandler());

	}

	@Override
	public void startListening() {

	}

	@Override
	public void stopListening() {
		// TODO Auto-generated method stub

	}

}
