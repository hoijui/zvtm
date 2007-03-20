/*   FILE: TrajectoryPlayer2D.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TrajectoryPlayer2D.java,v 1.7 2006/06/02 12:17:21 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Graphics2D;

import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class TrajectoryPlayer2D implements Runnable {

    static final int ANIM_TIME = 50;

    boolean FOLLOW_CAMERA = false;
    
    Thread runView;

    TrajectoryViewer2D application;
    CinematicInfo[] ci;
    
    // where are we in the current animation
    int progress = 0;

    boolean animationStarted = false;
    boolean animationEnded = true;
    boolean animationPaused = false;
    static final String PLAYBACK_PAUSE = " - PAUSE";
    static final String PLAYBACK_PLAY = " - PLAY";
    static final String PLAYBACK_STOP = " - STOP";
    String playbackStatus = PLAYBACK_PAUSE;

    VRectangle lastFrame;
    VCircle lastLens;

    long lastResumeTime;
    long timeAccumulation;

    public TrajectoryPlayer2D(TrajectoryViewer2D app){
	this.application = app;
	start();
    }
    
    public void start(){
	runView = new Thread(this);
	runView.setPriority(Thread.NORM_PRIORITY);
	runView.start();
    }

    public void stop(){
	runView = null;
	notify();
    }

    public void run(){
	Thread me = Thread.currentThread();
	while (runView == me){
	    if (animationStarted && !animationPaused){animate();}
	    try {
		runView.sleep(TrajectoryViewer2D.TIME_STEP);
	    }
	    catch(InterruptedException ex){ex.printStackTrace();}
	}
    }

    void reset(){
	progress = 0;
	animationEnded = false;
	animationStarted = false;
	lastFrame = null;
	lastLens = null;
	animationPaused = false;
	application.info = application.infoHeader + PLAYBACK_STOP;
	application.vsm.repaintNow();
    }

    void startAnimation(CinematicInfo[] ci){
	this.ci = ci;
	progress = 0;
	lastFrame = null;
	lastLens = null;
	animationEnded = false;
	animationStarted = true;
	lastResumeTime = System.currentTimeMillis();
	timeAccumulation = 0;
	application.info = application.infoHeader + PLAYBACK_PLAY;
    }

    void pauseOrResume(){
	if (animationPaused){
	    application.info = application.infoHeader + PLAYBACK_PLAY;
	    animationPaused = false;
	    lastResumeTime = System.currentTimeMillis();
	}
	else {
	    application.info = application.infoHeader + PLAYBACK_PAUSE;
	    animationPaused = true;
	    timeAccumulation += System.currentTimeMillis() - lastResumeTime;
	}
	application.vsm.repaintNow();
    }

    int oldProgress = 0;

    void animate(){
	if (animationEnded){return;}
	progress = (int)Math.round((timeAccumulation+System.currentTimeMillis()-lastResumeTime)/((double)ci[ci.length-1].time) * ci.length);
	if (progress < ci.length){
	    if (lastLens != null){application.mainVS.destroyGlyph(lastLens);}
	    if (FOLLOW_CAMERA){
		application.demoCamera.setAltitude(ci[progress].ca, false);
		application.demoCamera.moveTo(ci[progress].cx, ci[progress].cy);
	    }
	    else {
		if (lastFrame != null){application.mainVS.destroyGlyph(lastFrame);}
		for (int i=oldProgress;i<=progress;i++){// make old previous steps green (including those skipped)
		    application.trajectory[i].setColor(Color.GREEN);
		}
		lastFrame = new VRectangle(ci[progress].cx, ci[progress].cy, 0, ci[progress].viewHW, ci[progress].viewHH, Color.GREEN);
		lastFrame.setFilled(false);
		lastFrame.setBorderColor(Color.GREEN);
		application.vsm.addGlyph(lastFrame, application.mainVS);
	    }
	    if (ci[progress].lensStatus != CinematicInfo.NO_LENS){
		lastLens = new VCircle(ci[progress].lensVX, ci[progress].lensVY, 0, ci[progress].lensVR, Color.GREEN);
		lastLens.setFilled(false);
		lastLens.setBorderColor((ci[progress].lensStatus == CinematicInfo.ZOOMIN_LENS) ? Color.GREEN : Color.RED);
		application.vsm.addGlyph(lastLens, application.mainVS);
	    }
	    oldProgress = progress;
	}
	else {
	    animationEnded = true;
	    animationStarted = false;
	    application.info = application.infoHeader + PLAYBACK_STOP;
	    if (lastLens != null){lastLens.setBorderColor(ZLWorldTask.SELECTION_RECT_COLOR);}
	    if (!FOLLOW_CAMERA){lastFrame.setBorderColor(ZLWorldTask.SELECTION_RECT_COLOR);}
	    else {
		application.vsm.repaintNow(); // to update info message
	    }
	}
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (animationStarted){
	    g2d.setColor(Color.WHITE);
	    g2d.fillRect(0,viewHeight-10,viewWidth,10);
	    g2d.setColor(Color.BLACK);
	    g2d.fillRect(0,viewHeight-10,(int)Math.round((progress/((double)ci.length)) * viewWidth),10);
	}
    }

}