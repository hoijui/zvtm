/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ScreenSaver.java,v 1.4 2007/10/09 11:44:05 pietriga Exp $
 */

package fr.inria.zuist.app.lri;

import java.awt.Robot;
import java.awt.event.InputEvent;

/* Fires actions in screen saver mode. Also takes care of manually calling garbage collector. */

public class ScreenSaver implements Runnable {
    
    /* thread sleeping time */
    static final int SLEEP_TIME = 1000;

    /* thread sleeping time */
    static final int DEEP_SLEEP_TIME = 6000;
    
    LRIExplorer application;
    
    Thread runView;
    
    Robot robot;
    
    ScreenSaver(LRIExplorer app){
        this.application = app;
        try {
            robot = new Robot();
            start();
        }
        catch (java.awt.AWTException ex){ex.printStackTrace();}
    }

    public void start(){
        runView = new Thread(this);
        runView.setPriority(Thread.MIN_PRIORITY);
        runView.start();
    }

    public void stop(){
        runView = null;
        notify();
    }

    public void run(){
        Thread me = Thread.currentThread();
        int processedRequestCount = 0;
        while (runView == me){
            if (application.usedMemRatio > 50){
		// if percentage of used memory exceeds 50%, force garbage collection
                System.out.println("Force garbage collection");
                System.gc();
            }
            if (application.getNavigationMode() == LRIExplorer.NAV_MODE_SS){
                clickSomewhere();
                goToSleep(SLEEP_TIME);
            }
            else {
                goToSleep(DEEP_SLEEP_TIME);
            }
        }
    }

    void goToSleep(int time){
        try {
            runView.sleep(time);
        }
        catch(InterruptedException ex){ex.printStackTrace();}
    }
    
    void clickSomewhere(){
        int x = (int)Math.round(Math.random() * application.panelWidth);
        int y = (int)Math.round(Math.random() * (application.panelHeight-MenuManager.NAV_MENU_ITEM_HEIGHT) + MenuManager.NAV_MENU_ITEM_HEIGHT);
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON2_MASK);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON2_MASK);
    }
    
}
