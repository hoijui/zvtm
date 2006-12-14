
package net.claribole.eval.alphalens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Graphics2D;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

public class EvalGraphPointing extends EvalPointing {
    
    public EvalGraphPointing(short t){
	initGUI();
	this.technique = t;
	mViewName = TECHNIQUE_NAMES[this.technique];
	eh = new BaseEventHandlerPointing(this);
	mView.setEventHandler(eh);
	initScene();
	Location l = vsm.getGlobalView(mCamera);
	mCamera.moveTo(l.vx, l.vy);
	mCamera.setAltitude(l.alt);
    }

    void initScene(){
	mView.setBackgroundColor(EvalPointing.BACKGROUND_COLOR);
	
    }
    
    public static void main(String[] args){
	try {
	    if (args.length >= 3){
		EvalWorldPointing.VIEW_MAX_W = Integer.parseInt(args[1]);
		EvalWorldPointing.VIEW_MAX_H = Integer.parseInt(args[2]);
	    }
	    new EvalWorldPointing(Short.parseShort(args[0]));
	}
	catch (Exception ex){
	    ex.printStackTrace();
	    System.err.println("No cmd line parameter to indicate technique, defaulting to Fading Lens");
	    new EvalWorldPointing(EvalPointing.TECHNIQUE_FL);
	}
    }

}
