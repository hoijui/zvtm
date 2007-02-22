/*   FILE: AcquireInstructionsManager.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.AlphaComposite;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.Transparent;
import net.claribole.zvtm.engine.Java2DPainter;

public class BehaviorInstructionsManager implements Java2DPainter {

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f);

    static int START_BUTTON_TL_X = 0;
    static int START_BUTTON_TL_Y = 0;
    static int START_BUTTON_BR_X = 0;
    static int START_BUTTON_BR_Y = 0;
    static final int START_BUTTON_W = 80;
    static final int START_BUTTON_H = 20;

    static final String C_BT = "CONTINUE";  // Continue button displayed between trials

    BehaviorEval application;
    BehaviorLogManager blm;

    String message = null;
    int halfMessageWidth = 0;

    static final int WARN_DELAY = 1000;
    String error = null;

    String indication = null;

    boolean showButton = false;

    BehaviorInstructionsManager(BehaviorEval app, BehaviorLogManager blm){
	this.application = app;
	this.blm = blm;
    }

    void say(String msg, final int delay){
	showButton(false);
	say(msg);
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    sleep(delay);
		    BehaviorInstructionsManager.this.showButton(true);
		    return null; 
		}
	    };
	worker.start();
    }

    void say(String msg){
	if (msg != null && msg.length() == 0){msg = null;}
	message = msg;
	if (message != null){
	    halfMessageWidth = application.mView.getGraphicsContext().getFontMetrics().stringWidth(message) / 2;
	}
	application.vsm.repaintNow();
    }

    void warn(String msg){
	error = msg;
	application.vsm.repaintNow();
	final SwingWorker worker = new SwingWorker(){
		public Object construct(){
		    sleep(WARN_DELAY);
		    BehaviorInstructionsManager.this.warn(null);
		    return null; 
		}
	    };
	worker.start();
    }

    boolean showIndications = false;
    
    void toggleIndications(){
	showIndications = !showIndications;
    }

    void indicate(String s){
	indication = s;
    }

    void showButton(boolean b){
	showButton = b;
	application.vsm.repaintNow();
    }

    boolean clickOnStartButton(int jpx, int jpy){
	return (showButton &&
		jpx >= START_BUTTON_TL_X && jpy >= START_BUTTON_TL_Y &&
		jpx <= START_BUTTON_BR_X && jpy <= START_BUTTON_BR_Y);
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (showIndications && indication != null){
	    g2d.setColor(Color.DARK_GRAY);
	    g2d.drawString(indication, 10, 10);
	}
	if (error != null){
	    g2d.setColor(Color.BLACK);
	    g2d.fillRect(0, viewHeight/2-50, viewWidth, 100);
	    g2d.setColor(Color.RED);
	    g2d.drawString(error, viewWidth/2, viewHeight/2);
	}
	else if (message != null){
	    // message at center of screen (translucent black strip + text)
	    g2d.setColor(Color.BLACK);
	    g2d.setComposite(acST);
	    g2d.fillRect(0, viewHeight / 2 - 100, viewWidth, 220);
	    g2d.setComposite(Transparent.acO);
	    g2d.setColor(Color.WHITE);
	    g2d.drawString(message, viewWidth/2 - halfMessageWidth, viewHeight/2);
	    if (blm.sessionStarted && !blm.trialStarted && showButton){
		// button
		g2d.setColor(Color.WHITE);
		g2d.fillRect(BehaviorInstructionsManager.START_BUTTON_TL_X, BehaviorInstructionsManager.START_BUTTON_TL_Y,
			     BehaviorInstructionsManager.START_BUTTON_W, BehaviorInstructionsManager.START_BUTTON_H);
		g2d.setColor(Color.RED);
		g2d.drawRect(BehaviorInstructionsManager.START_BUTTON_TL_X, BehaviorInstructionsManager.START_BUTTON_TL_Y,
			     BehaviorInstructionsManager.START_BUTTON_W, BehaviorInstructionsManager.START_BUTTON_H);
		g2d.setColor(Color.BLACK);
		g2d.drawString(C_BT, BehaviorInstructionsManager.START_BUTTON_TL_X+15, BehaviorInstructionsManager.START_BUTTON_TL_Y+15);
	    }
	}
    }

}