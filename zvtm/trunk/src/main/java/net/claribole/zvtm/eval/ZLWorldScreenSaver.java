/*   FILE: ZLWorldScreenSaver.java
 *   DATE OF CREATION:  Wed Apr  5 12:07:02 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.event.InputEvent;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;

public class ZLWorldScreenSaver implements Runnable {

    /*map manager thread sleeping time*/
    static final int SLEEP_TIME = 250;

    static final long TIME_BETWEEN_TWO_ACTIONS = 1000;
    long lastActionStartTime = 0;

    static final short ZIP1_ACTION = 0;
    static final short ZIP2_ACTION = 1;
    static final short ZOP1_ACTION = 2;
    static final short ZOP2_ACTION = 3;
    static final short MVCUR_ACTION = 4;
    static final short MVCAM_ACTION = 5;

    ZLWorldTask application;
    Thread runView;

    boolean work = false;

    boolean lensIsActive = false;

    /*map manager constructor*/
    ZLWorldScreenSaver(ZLWorldTask app){
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
	application.robot.mouseMove(application.VIEW_X + application.VIEW_W/2,
				    application.VIEW_Y + application.VIEW_H/2);
	while (runView == me){
	    if (work){doAction();}
	    try {
		runView.sleep(SLEEP_TIME);
	    }
	    catch(InterruptedException ex){ex.printStackTrace();}
	}
    }
    
    void switchSS(){
 	work = !work;
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Screen Saver: "+((work) ? "On\n" : "Off\n"));}
    }
    
    void doAction(){
	if ((System.currentTimeMillis() - lastActionStartTime) > TIME_BETWEEN_TWO_ACTIONS){
	    short actionType = (short)Math.floor(Math.random() * 5.0);
	    if (actionType <= 1){
		actionType = (lensIsActive) ? ZIP2_ACTION : ZIP1_ACTION;
	    }
	    else if (actionType <= 2){
		actionType = (lensIsActive) ? ZOP2_ACTION : ZOP1_ACTION;
	    }
	    else if (actionType <= 3){
		actionType = MVCUR_ACTION;
	    }
	    else {
		actionType = MVCAM_ACTION;
	    }
	    switch (actionType){
	    case ZIP1_ACTION:{zip1();break;}
	    case ZIP2_ACTION:{zip2();break;}
	    case ZOP1_ACTION:{zop1();break;}
	    case ZOP2_ACTION:{zop2();break;}
	    case MVCUR_ACTION:{moveCursor();break;}
	    case MVCAM_ACTION:{moveCamera();break;}
	    }
	    lastActionStartTime = System.currentTimeMillis();
	}
    }

    void zip1(){
	application.robot.mousePress(InputEvent.BUTTON1_MASK);
	application.robot.mouseRelease(InputEvent.BUTTON1_MASK);
	lensIsActive = true;
    }

    void zip2(){
	application.robot.mousePress(InputEvent.BUTTON1_MASK);
	application.robot.mouseRelease(InputEvent.BUTTON1_MASK);
	lensIsActive = false;
    }

    void zop1(){
	if (application.demoCamera.getAltitude() < ZLWorldTask.START_ALTITUDE/4){
	    application.robot.mousePress(InputEvent.BUTTON3_MASK);
	    application.robot.mouseRelease(InputEvent.BUTTON3_MASK);
	    lensIsActive = true;
	}
    }

    void zop2(){
	application.robot.mousePress(InputEvent.BUTTON3_MASK);
	application.robot.mouseRelease(InputEvent.BUTTON3_MASK);
	lensIsActive = false;
    }

    void moveCursor(){
	int x = application.VIEW_X + (int)Math.round(Math.random() * (application.VIEW_W-200)) + 100;
	int y = application.VIEW_Y + (int)Math.round(Math.random() * (application.VIEW_H-200)) + 100;
	application.robot.mouseMove(x, y);
    }

    void moveCamera(){
	long dx = Math.round(ZLWorldTask.MAP_WIDTH * Math.random()) - ZLWorldTask.HALF_MAP_WIDTH - application.demoCamera.posx;
	long dy = Math.round(ZLWorldTask.MAP_HEIGHT * Math.random()) - ZLWorldTask.HALF_MAP_HEIGHT - application.demoCamera.posy;
	application.vsm.animator.createCameraAnimation(500, AnimManager.CA_TRANS_SIG,
						       new LongPoint(dx, dy),
						       application.demoCamera.getID());
    }

}
