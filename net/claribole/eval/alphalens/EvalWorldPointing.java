
package net.claribole.eval.alphalens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import javax.swing.ImageIcon;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

public class EvalWorldPointing extends EvalPointing {

    static final String MAP_PATH = "images/world/1000_2800x2000.png";

    VImage map;
    
    public EvalWorldPointing(short t){
	initGUI();
	this.technique = t;
	mViewName = TECHNIQUE_NAMES[this.technique];
	eh = new BaseEventHandlerPointing(this);
	mView.setEventHandler(eh);
	initScene();
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(100.0f);
    }

    void initScene(){
	mView.setBackgroundColor(EvalPointing.BACKGROUND_COLOR);
	map = new VImage(0, 0, 0, (new ImageIcon(MAP_PATH)).getImage());
	map.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(map, mSpaceName);
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
	    System.err.println("No cmd line parameter to indicate technique, defaulting to Fading Lens");
	    new EvalWorldPointing(EvalPointing.TECHNIQUE_FL);
	}
    }

}
