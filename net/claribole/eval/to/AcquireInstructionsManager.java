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

public class AcquireInstructionsManager implements Java2DPainter {

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f);

    static int START_BUTTON_TL_X = 0;
    static int START_BUTTON_TL_Y = 0;
    static int START_BUTTON_BR_X = 0;
    static int START_BUTTON_BR_Y = 0;
    static final int START_BUTTON_W = 80;
    static final int START_BUTTON_H = 20;

    static final String C_BT = "CONTINUE";  // Continue button displayed between trials

    AcquireEval application;
    AcquireLogManager alm;

    String message = null;
    int halfMessageWidth = 0;

    static final int WARN_DELAY = 1000;
    String error = null;

    boolean showButton = false;

    AcquireInstructionsManager(AcquireEval app, AcquireLogManager alm){
	this.application = app;
	this.alm = alm;
    }

    void say(String msg, final int delay){
	showButton(false);
	say(msg);
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    sleep(delay);
		    AcquireInstructionsManager.this.showButton(true);
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
		    AcquireInstructionsManager.this.warn(null);
		    return null; 
		}
	    };
	worker.start();
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
	    if (alm.sessionStarted && !alm.trialStarted && showButton){
		// button
// 		g2d.setColor(Color.BLACK);
// 		g2d.fillRect(AcquireInstructionsManager.START_BUTTON_TL_X-10, AcquireInstructionsManager.START_BUTTON_TL_Y-10,
// 			     AcquireInstructionsManager.START_BUTTON_W+20, AcquireInstructionsManager.START_BUTTON_H+20);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(AcquireInstructionsManager.START_BUTTON_TL_X, AcquireInstructionsManager.START_BUTTON_TL_Y,
			     AcquireInstructionsManager.START_BUTTON_W, AcquireInstructionsManager.START_BUTTON_H);
		g2d.setColor(Color.RED);
		g2d.drawRect(AcquireInstructionsManager.START_BUTTON_TL_X, AcquireInstructionsManager.START_BUTTON_TL_Y,
			     AcquireInstructionsManager.START_BUTTON_W, AcquireInstructionsManager.START_BUTTON_H);
		g2d.setColor(Color.BLACK);
		g2d.drawString(C_BT, AcquireInstructionsManager.START_BUTTON_TL_X+15, AcquireInstructionsManager.START_BUTTON_TL_Y+15);
	    }
	}
	else {
	    g2d.setColor(AcquireEval.SELECTION_REGION_COLOR);
	    g2d.drawOval(viewWidth/2 - application.alm.selectionRegionHSize, viewHeight/2 - application.alm.selectionRegionHSize,
			 application.alm.selectionRegionSize, application.alm.selectionRegionSize);
	}
    }

}