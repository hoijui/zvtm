package fr.inria.zvtm.fms;

import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Vector;

import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.animation.*;
import fr.inria.zvtm.animation.interpolation.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.lens.*;
import fr.inria.zvtm.widgets.*;
import java.awt.geom.Point2D;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.*;

public class Test2 {

    VirtualSpaceManager vsm;
    static final String mSpaceStr = "mSpace";
    VirtualSpace mSpace;
    Camera mCamera;
    View mView;
    ViewEventHandler eh;

	Lens lens;	
	
	boolean lensCursorSync = true;
    
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int LENS_ANIM_TIME = 300;
    static double MAG_FACTOR = 8.0;
    
	
    Test2(){
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest();
	}

    public void initTest(){
        mSpace = vsm.addVirtualSpace(mSpaceStr);
        mCamera = vsm.addCamera(mSpace);
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace(mSpaceStr).getCamera(0));
        mView = vsm.addExternalView(cameras, "FMS", View.STD_VIEW, 800, 600, false, true);
        mView.setBackgroundColor(Color.LIGHT_GRAY);
        eh = new EventHandlerTest2(this);
        mView.setEventHandler(eh);
        mView.setNotifyMouseMoved(true);
        for (int i=-5;i<=5;i++){
            for (int j=-5;j<=5;j++){
        		vsm.addGlyph(new VRectangle(i*30,j*30,0,10,10,Color.WHITE), mSpace);
            }
        }
        vsm.repaintNow();
    }
    
    
    
    void toggleLens(int x, int y){
        if (lens != null){
            unsetLens();
			//mView.getPanel().setDrawCursor(true);
        }
        else {
            setLens(x, y);
			//mView.getPanel().setDrawCursor(false);
        }
    }
    
    void setLens(int x, int y){
        lens = mView.setLens(getLensDefinition(x, y));
        lens.setBufferThreshold(1.5f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
		lens.setAbsolutePosition(x, y);
		lens.setXfocusOffset(0);
		lens.setYfocusOffset(0);
    }
    
    void unsetLens(){
        Animation a = vsm.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(),
            new EndAction(){public void execute(Object subject, Animation.Dimension dimension){doUnsetLens();}});
        vsm.getAnimationManager().startAnimation(a, false);
    }
    
    void doUnsetLens(){
        lens.dispose();
        mView.setLens(null);
        lens = null;
    }

    Lens getLensDefinition(int x, int y){
		//SCBLens linearLens = new SCBLens(1.0f, 0f, 1f, LENS_R1, x-mView.getPanelSize().width/2, y-mView.getPanelSize().height/2);
		FSLinearLens linearLens = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x-mView.getPanelSize().width/2, y-mView.getPanelSize().height/2);
		linearLens.setOuterRadiusColor(Color.BLACK);
		linearLens.setInnerRadiusColor(Color.BLACK);
        return linearLens;
	}
    
    void toggleLensCursorSync(){
        lensCursorSync = !lensCursorSync;
    }
    
    void moveLens(int x, int y, long time){
        if (lens == null){return;}
        lens.setAbsolutePosition(x, y);
        //((TemporalLens)lens).setAbsolutePosition(x, y, time);
        vsm.repaintNow();
    }

    public static void main(String[] args){
        new Test2();
    }
    
}

class EventHandlerTest2 implements ViewEventHandler{
	
    Test2 application;
	boolean precisionEnabled = false;
	
    long lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
		
    EventHandlerTest2(Test2 appli){
        application=appli;
	}
	
    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.toggleLens(jpx, jpy);
    }
	
    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }
	
    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		
    }
	
    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		
    }
	
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }
	
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
    }
	
    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawDrag(true);
        VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(false);
    }
	
    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
        v.setDrawDrag(false);
        VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(true);
    }
	
    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}
	
	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e) {
		if(precisionEnabled) return;
		if(application.lens != null) {
			application.lens.setXfocusOffset(0);
			application.lens.setYfocusOffset(0);
			application.moveLens(jpx, jpy, e.getWhen());
		}
	}
	
    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            Camera c=VirtualSpaceManager.INSTANCE.getActiveCamera();
            float a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
            }
            else {
                VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
            }
        }
    }
	
    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c=VirtualSpaceManager.INSTANCE.getActiveCamera();
        float a=(c.focal+Math.abs(c.altitude))/c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
        else {
            //wheelDirection == WHEEL_DOWN
            c.altitudeOffset(a*5);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
    }
	
    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }
	
    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }
	
    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_SPACE){application.toggleLensCursorSync();}
		
		if(c == 'p') {
			precisionEnabled = !precisionEnabled;
			application.mView.setFocusControlled(precisionEnabled, FocusControlHandler.SPEED_DEPENDENT_LINEAR);
			System.out.println("\n******************\n"
							   + "MOTOR PRECISION " + (precisionEnabled ? "ON" : "OFF")
							   + "\n******************\n");
		} else {
			if(c == 'c') {
				precisionEnabled = !precisionEnabled;
				application.mView.setFocusControlled(precisionEnabled, FocusControlHandler.CONSTANT);
				System.out.println("\n******************\n"
								   + "MOTOR PRECISION " + (precisionEnabled ? "ON" : "OFF")
								   + "\n******************\n");
			}
		}
    }
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
		
	}
	
    public void viewActivated(View v){}
	
    public void viewDeactivated(View v){}
	
    public void viewIconified(View v){}
	
    public void viewDeiconified(View v){}
	
    public void viewClosing(View v){System.exit(0);}
	
}
