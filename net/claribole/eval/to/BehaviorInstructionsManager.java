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
import com.xerox.VTM.glyphs.Translucent;
import net.claribole.zvtm.engine.Java2DPainter;

public class BehaviorInstructionsManager implements Java2DPainter {

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f);

    static int START_BUTTON_TL_X = 0;
    static int START_BUTTON_TL_Y = 0;
    static int START_BUTTON_BR_X = 0;
    static int START_BUTTON_BR_Y = 0;
    static final int START_BUTTON_W = 40;
    static final int START_BUTTON_H = 20;

    static final String C_BT = "Next";  // Continue button displayed between trials

    BehaviorEval application;
    BehaviorLogManager blm;

    String message = null;
    int halfMessageWidth = 0;

    static final int WARN_DELAY = 1000;
    String error = null;

    String indication = null;
    boolean showIndications = false;
    
    boolean buttonEnabled = false;

    BehaviorInstructionsManager(BehaviorEval app, BehaviorLogManager blm){
	this.application = app;
	this.blm = blm;
    }

//     void say(String msg, final int delay){
// 	enableButton(false);
// 	say(msg);
// 	final SwingWorker worker=new SwingWorker(){
// 		public Object construct(){
// 		    sleep(delay);
// 		    //BehaviorInstructionsManager.this.enableButton(true);
// 		    return null; 
// 		}
// 	    };
// 	worker.start();
//     }

    void say(String msg){
	if (msg != null && msg.length() == 0){msg = null;}
	message = msg;
	if (message != null){
	    halfMessageWidth = application.mView.getGraphicsContext().getFontMetrics().stringWidth(message) / 2;
	}
	enableButton(false);
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

    void toggleIndications(){
	showIndications = !showIndications;
    }

    void indicate(String s){
	indication = s;
    }

    /* tells whether cursor is currently in button or not
       (set or unset by cursorAt(), checked by enableButton(delay) before actually enabling the button
       because cursor might have exited button during the BehaviorLogManager.MIN_TIME_INSIDE_NEXT_TRIAL_BUTTON delay) */
    boolean cursorCurrentlyInButton = false;

    void cursorAt(int jpx, int jpy){
	if (jpx >= START_BUTTON_TL_X && jpy >= START_BUTTON_TL_Y &&
	    jpx <= START_BUTTON_BR_X && jpy <= START_BUTTON_BR_Y){
	    if (!cursorCurrentlyInButton){
		enableButton(BehaviorLogManager.MIN_TIME_INSIDE_NEXT_TRIAL_BUTTON);
		cursorCurrentlyInButton = true;
	    }
	}
	else {
	    cursorCurrentlyInButton = false;
	}
    }

    void enableButton(final int delay){
	final SwingWorker worker = new SwingWorker(){
		public Object construct(){
		    while (BehaviorInstructionsManager.this.application.to.getDistance() > 10){
			sleep(200);
		    }
// 		    sleep(delay);
		    if (cursorCurrentlyInButton){enableButton(true);}
		    return null; 
		}
	    };
	worker.start();
    }

    void enableButton(boolean b){
	buttonEnabled = b;
	application.vsm.repaintNow();
    }

    boolean clickOnStartButton(int jpx, int jpy){
	return (buttonEnabled &&
		jpx >= START_BUTTON_TL_X && jpy >= START_BUTTON_TL_Y &&
		jpx <= START_BUTTON_BR_X && jpy <= START_BUTTON_BR_Y);
    }

    static final Color ENABLED_BUTTON_FOREGROUND = Color.BLACK;
    static final Color ENABLED_BUTTON_BACKGROUND = Color.YELLOW;
    static final Color DISABLED_BUTTON_FOREGROUND = Color.GRAY;
    static final Color DISABLED_BUTTON_BACKGROUND = Color.LIGHT_GRAY;
    static final Color ENABLED_BUTTON_BORDER = Color.RED;
    static final Color DISABLED_BUTTON_BORDER = Color.GRAY;

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
	    g2d.fillRect(0, viewHeight / 2 - 100, viewWidth, 180);
	    g2d.setComposite(Translucent.acO);
	    g2d.setColor(Color.WHITE);
	    g2d.drawString(message, viewWidth/2 - halfMessageWidth + BehaviorEval.C_OFFSET_X, viewHeight/2 + BehaviorEval.C_OFFSET_Y);
	    if (blm.sessionStarted && !blm.trialStarted){
		// button
		g2d.setColor((buttonEnabled) ? ENABLED_BUTTON_BACKGROUND : DISABLED_BUTTON_BACKGROUND);
		g2d.fillRect(BehaviorInstructionsManager.START_BUTTON_TL_X, BehaviorInstructionsManager.START_BUTTON_TL_Y,
			     BehaviorInstructionsManager.START_BUTTON_W, BehaviorInstructionsManager.START_BUTTON_H);
		g2d.setColor((buttonEnabled) ? ENABLED_BUTTON_BORDER : DISABLED_BUTTON_BORDER);
		g2d.drawRect(BehaviorInstructionsManager.START_BUTTON_TL_X, BehaviorInstructionsManager.START_BUTTON_TL_Y,
			     BehaviorInstructionsManager.START_BUTTON_W, BehaviorInstructionsManager.START_BUTTON_H);
		g2d.setColor((buttonEnabled) ? ENABLED_BUTTON_FOREGROUND : DISABLED_BUTTON_FOREGROUND);
		g2d.drawString(C_BT, BehaviorInstructionsManager.START_BUTTON_TL_X+8, BehaviorInstructionsManager.START_BUTTON_TL_Y+15);
	    }
	}
    }

}