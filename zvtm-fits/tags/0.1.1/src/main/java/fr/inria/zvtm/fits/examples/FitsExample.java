/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.fits.examples;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.fits.FitsHistogram;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.ZScale;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.net.MalformedURLException;
import java.net.URL;
/**
 * Sample FITS application.
 */
public class FitsExample {
	//shortcut
	private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; 
    private FitsImage hi;
    private double[] scaleBounds;
    private boolean dragLeft = false, dragRight = false;
    private RangeSelection rs;
    private View view;

	FitsExample(String imgUrl) throws IOException {
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");
		Camera cam = vs.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(cam);	
        
        view = vsm.addFrameView(cameras, "Master View",
                View.STD_VIEW, 800, 600, false, true, true, null);	
        view.setBackgroundColor(Color.GRAY);
        view.setEventHandler(new PanZoomEventHandler());

        hi = new FitsImage(0,0,0,new URL(imgUrl));
        hi.setScaleMethod(FitsImage.ScaleMethod.LINEAR);
        vs.addGlyph(hi, false);	
        //hi.zRescale();
        scaleBounds = ZScale.computeScale(hi.getUnderlyingImage());
        hi.rescale(scaleBounds[0], scaleBounds[1], 1);

        FitsHistogram hist = FitsHistogram.fromFitsImage(hi);
        hist.reSize(0.8f);
        vs.addGlyph(hist);
        rs = new RangeSelection();
        double min = hi.getUnderlyingImage().getHistogram().getMin();
        double max = hi.getUnderlyingImage().getHistogram().getMax();
        rs.setTicksVal((scaleBounds[0]-min)/(max-min), (scaleBounds[1]-min)/(max-min));
        vs.addGlyph(rs);
        rs.move(0, -30);
    }

    private LongPoint viewToSpace(Camera cam, int jpx, int jpy){
        Location camLoc = cam.getLocation();
        float focal = cam.getFocal();
        float altCoef = (focal + camLoc.alt) / focal;
        Dimension viewSize = view.getPanelSize();

        //find coords of view origin in the virtual space
        long viewOrigX = camLoc.vx - (long)(0.5*viewSize.width*altCoef);
        long viewOrigY = camLoc.vy + (long)(0.5*viewSize.height*altCoef);

        return new LongPoint(
                viewOrigX + (long)(altCoef*jpx),
                viewOrigY - (long)(altCoef*jpy));
    }

	public static void main(String[] args) throws IOException{
        if(args.length == 0){
            System.err.println("usage: FitsExample image_URL");
            return;
        }    
		new FitsExample(args[0]);
	}

	private class PanZoomEventHandler implements ViewEventHandler{
		private int lastJPX;
		private int lastJPY;

		public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            LongPoint cursorPos = viewToSpace(vsm.getActiveCamera(), jpx, jpy);
            if(rs.overLeftTick(cursorPos.x, cursorPos.y)){
                dragLeft = true;
            } else if(rs.overRightTick(cursorPos.x, cursorPos.y)){
                dragRight = true;
            }
        }

		public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            dragLeft = false;
            dragRight = false;
            double min = hi.getUnderlyingImage().getHistogram().getMin();
            double max = hi.getUnderlyingImage().getHistogram().getMax();
            hi.rescale(min + rs.getLeftValue()*(max - min),
                    min + rs.getRightValue()*(max - min),
                    1);
        }

		public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
			lastJPX=jpx;
			lastJPY=jpy;
			v.setDrawDrag(true);
			vsm.activeView.mouse.setSensitivity(false);
			//because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
		}

		public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
			vsm.getAnimationManager().setXspeed(0);
			vsm.getAnimationManager().setYspeed(0);
			vsm.getAnimationManager().setZspeed(0);
			v.setDrawDrag(false);
			vsm.activeView.mouse.setSensitivity(true);
		}

		public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

		public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
            if(buttonNumber == 1){
                if(dragLeft) {
                    rs.setLeftTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
                } else if(dragRight){
                    rs.setRightTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
                }
            }

			if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
				Camera c=vsm.getActiveCamera();
				float a=(c.focal+Math.abs(c.altitude))/c.focal;
				if (mod == META_SHIFT_MOD) {
					vsm.getAnimationManager().setXspeed(0);
					vsm.getAnimationManager().setYspeed(0);
					vsm.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/4.0f)) : (long)((lastJPY-jpy)/(a*4)));

				}
				else {
					vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/4.0f)) : (long)((jpx-lastJPX)/(a*4)));
					vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/4.0f)) : (long)((lastJPY-jpy)/(a*4)));
				}
			}
		}

		public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

		public void enterGlyph(Glyph g){
		}

		public void exitGlyph(Glyph g){
		}

		public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
            if(c == '-'){
                scaleBounds[1] -= 100;
                hi.rescale(scaleBounds[0], scaleBounds[1], 1);
            } else if (c == '+'){
                scaleBounds[1] += 100;
                hi.rescale(scaleBounds[0], scaleBounds[1], 1);
            }
        }

		public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		}

		public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

		public void viewActivated(View v){}

		public void viewDeactivated(View v){}

		public void viewIconified(View v){}

		public void viewDeiconified(View v){}

		public void viewClosing(View v){System.exit(0);}

	}
}

