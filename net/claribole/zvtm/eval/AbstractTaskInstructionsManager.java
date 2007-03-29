/*   FILE: InstructionsManager.java
 *   DATE OF CREATION:  Tue Apr 25 13:15:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.xerox.VTM.engine.SwingWorker;
import net.claribole.zvtm.engine.Java2DPainter;

class AbstractTaskInstructionsManager implements Java2DPainter {

    static final Color SAY_BKG_COLOR = Color.BLACK;
    static final Color SAY_FRG_COLOR = Color.WHITE;
    static final Color WARN_BKG_COLOR = Color.WHITE;
    static final Color WARN_FRG_COLOR = Color.RED;

    static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 16);

    ZLAbstractTask application;

    Color messageColor, messageBkgColor;
    String message = "";
    int hoffset = 100;
    int voffset = 40;

    AbstractTaskInstructionsManager(ZLAbstractTask app){
	this.application = app;
    }

    void say(String s){
	messageColor = SAY_FRG_COLOR;
	messageBkgColor = SAY_BKG_COLOR;
	message = s;
	application.vsm.repaintNow();
    }

    void warn(final String s1, final String s2, final int delay){
	messageColor = WARN_FRG_COLOR;
	messageBkgColor = WARN_BKG_COLOR;
	/* display error message for delay ms
	   and revert back to previous message */
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    message = s1;
		    application.vsm.repaintNow();
		    sleep(delay);
		    AbstractTaskInstructionsManager.this.say(s2);
		    return null; 
		}
	    };
	worker.start();
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(application.SELECTION_RECT_COLOR);
// 	if (application.lens != null){
// 	    g2d.fillRect(application.hpanelWidth+application.lens.lx,
// 			 application.hpanelHeight+application.lens.ly-ZLAbstractTask.H_CENTER_CROSS_SIZE,
// 			 1, application.CENTER_CROSS_SIZE);
// 	    g2d.fillRect(application.hpanelWidth+application.lens.lx-ZLAbstractTask.H_CENTER_CROSS_SIZE,
// 			 application.hpanelHeight+application.lens.ly,
// 			 application.CENTER_CROSS_SIZE, 1);
// 	}
// 	else {
// 	    g2d.fillRect(application.hpanelWidth, application.CENTER_N, 1, application.CENTER_CROSS_SIZE);
// 	    g2d.fillRect(application.CENTER_W, application.hpanelHeight, application.CENTER_CROSS_SIZE, 1);	    
// 	}
	drawFrame(g2d, viewWidth, viewHeight);
	writeInstructions(g2d, viewWidth, viewHeight);
    }

    static final int PADDING = 100;
    static final int PADDINGM1 = PADDING - 1;
    static final int DPADDINGM1 = 2 * PADDING - 1;

    void drawFrame(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(SAY_BKG_COLOR);
	g2d.fillRect(0,0,viewWidth,PADDING);
	g2d.fillRect(0,PADDING,PADDING,viewHeight-DPADDINGM1);
	g2d.fillRect(viewWidth-PADDINGM1,PADDING,PADDING,viewHeight-DPADDINGM1);
	g2d.setColor(messageBkgColor);
	g2d.fillRect(0,viewHeight-PADDINGM1,viewWidth,PADDING);
    }
    
    void writeInstructions(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(messageColor);
	g2d.setFont(MESSAGE_FONT);
	g2d.drawString(message, hoffset, viewHeight - voffset);
	g2d.setFont(ZLAbstractTask.DEFAULT_FONT);
    }

}