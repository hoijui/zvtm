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

class InstructionsManager implements Java2DPainter {

    static final Color SAY_BKG_COLOR = Color.BLACK;
    static final Color SAY_FRG_COLOR = Color.WHITE;
    static final Color WARN_BKG_COLOR = Color.BLACK;
    static final Color WARN_FRG_COLOR = Color.RED;

    static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 16);
    static final Font MESSAGE_FONT_BOLD = new Font("Arial", Font.BOLD, 16);

    ZLWorldTask application;

    Color messageColor, messageBkgColor;
    String[] message = {"", "", "", ""};
    Font[] fonts = {MESSAGE_FONT, MESSAGE_FONT_BOLD, MESSAGE_FONT, MESSAGE_FONT_BOLD};
    int[] hoffsets = {100, 150, 100, 150};
    int[] voffsets = {40,40,20,20};

    InstructionsManager(ZLWorldTask app){
	this.application = app;
    }

    void say(String s){
	messageColor = SAY_FRG_COLOR;
	messageBkgColor = SAY_BKG_COLOR;
	message[0] = s;
	message[1] = null;
	message[2] = null;
	message[3] = null;
	application.vsm.repaintNow();
    }

    void say(String[] s){
	messageColor = SAY_FRG_COLOR;
	messageBkgColor = SAY_BKG_COLOR;
	message[0] = s[0];
	message[1] = s[1];
	message[2] = s[2];
	message[3] = s[3];
	application.vsm.repaintNow();
    }

    void warn(final String s1, final String[] s2, final int delay){
	messageColor = WARN_FRG_COLOR;
	messageBkgColor = WARN_BKG_COLOR;
	/* display error message for delay ms
	   and revert back to previous message */
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    message[0] = s1;
		    message[1] = null;
		    message[2] = null;
		    message[3] = null;
		    application.vsm.repaintNow();
		    sleep(delay);
		    InstructionsManager.this.say(s2);
		    return null; 
		}
	    };
	worker.start();
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(application.SELECTION_RECT_COLOR);
	if (application.cameraOnFloor && application.SHOW_SELECTION_RECT){
	    g2d.drawRect(application.SELECTION_RECT_X, application.SELECTION_RECT_Y,
			 application.SELECTION_RECT_W, application.SELECTION_RECT_H);
	}
	// uncomment to draw a cross at the window center
// 	g2d.fillRect(hpanelWidth, ZLWorldTask.CENTER_N, 1, ZLWorldTask.CENTER_CROSS_SIZE);
// 	g2d.fillRect(ZLWorldTask.CENTER_W, hpanelHeight, ZLWorldTask.CENTER_CROSS_SIZE, 1);
	//drawFrame(g2d, viewWidth, viewHeight);
	writeInstructions(g2d, viewWidth, viewHeight);
	if (ZLWorldTask.SHOW_MEMORY_USAGE){showMemoryUsage(g2d, viewWidth, viewHeight);}
//  	if (ZLWorldTask.SHOW_COORDS){showLatLong(g2d, viewWidth, viewHeight);}

	if (application.paintLinks){
	    float coef=(float)(application.demoCamera.focal/(application.demoCamera.focal+application.demoCamera.altitude));
	    int dmRegionX = (viewWidth/2) + Math.round((application.dmRegion.vx-application.demoCamera.posx)*coef);
	    int dmRegionY = (viewHeight/2) - Math.round((application.dmRegion.vy-application.demoCamera.posy)*coef);
	    int dmRegionW = Math.round(application.dmRegion.getWidth()*coef);
	    int dmRegionH = Math.round(application.dmRegion.getHeight()*coef);
	    g2d.setColor(Color.RED);
	    g2d.drawLine(dmRegionX-dmRegionW, dmRegionY-dmRegionH, application.dmPortal.x, application.dmPortal.y);
	    g2d.drawLine(dmRegionX+dmRegionW, dmRegionY-dmRegionH, application.dmPortal.x+application.dmPortal.w, application.dmPortal.y);
	    g2d.drawLine(dmRegionX-dmRegionW, dmRegionY+dmRegionH, application.dmPortal.x, application.dmPortal.y+application.dmPortal.h);
	    g2d.drawLine(dmRegionX+dmRegionW, dmRegionY+dmRegionH, application.dmPortal.x+application.dmPortal.w, application.dmPortal.y+application.dmPortal.h);
	}


    }

    void drawFrame(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(SAY_BKG_COLOR);
	g2d.fillRect(0,0,viewWidth,100);
	g2d.fillRect(0,100,100,viewHeight-199);
	g2d.fillRect(viewWidth-99,100,100,viewHeight-199);
	g2d.setColor(messageBkgColor);
	g2d.fillRect(0,viewHeight-99,viewWidth,100);
    }
    
    void writeInstructions(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(messageColor);
	for (int i=0;i<message.length;i++){
	    if (message[i] != null){
		g2d.setFont(fonts[i]);
		g2d.drawString(message[i], hoffsets[i] , viewHeight - voffsets[i]);
	    }
	    else {
		break;
	    }
	}
	g2d.setFont(GeoDataStore.CITY_FONT);
    }

    long maxMem = Runtime.getRuntime().maxMemory();
    int totalMemRatio, usedMemRatio;

    void showMemoryUsage(Graphics2D g2d, int viewWidth, int viewHeight){
	totalMemRatio = (int)(Runtime.getRuntime().totalMemory() * 100 / maxMem);
	usedMemRatio = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / maxMem);
	g2d.setColor(Color.green);
	g2d.fillRect(20,
		     40,
		     200,
		     15);
	g2d.setColor(Color.orange);
	g2d.fillRect(20,
		     40,
		     totalMemRatio * 2,
		     15);
	g2d.setColor(Color.red);
	g2d.fillRect(20,
		     40,
		     usedMemRatio * 2,
		     15);
	g2d.setColor(Color.black);
	g2d.drawRect(20,
		     40,
		     200,
		     15);
	g2d.drawString(usedMemRatio + "%", 50, 52);
	g2d.drawString(totalMemRatio + "%", 100, 52);
	g2d.drawString(maxMem/1048576 + " Mb", 170, 52);	
    }

    void showLatLong(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(Color.RED);
	g2d.drawString(application.eh.latitude+"/"+application.eh.longitude, viewWidth - 100, viewHeight - 28);
    }

}