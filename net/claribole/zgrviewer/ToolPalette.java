/*   FILE: ZGRViewer.java
 *   DATE OF CREATION:   Wed Aug 30 12:02:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id: $
 */ 

package net.claribole.zgrviewer;

import javax.swing.ImageIcon;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.glyphs.VImage;

public class ToolPalette {
    
    ZGRViewer application;

    static final String PALETTE_SPACE_NAME = "tps";
    VirtualSpace paletteSpace;
    Camera paletteCamera;

    static final String[] ICON_PATHS = {"/images/stdnav24b.png",
					"/images/dmnav24b.png",
					"/images/plnav24b.png"};
    static final String[] SELECTED_ICON_PATHS = {"/images/stdnav24g.png",
						 "/images/dmnav24g.png",
						 "/images/plnav24g.png"};
    VImage[] buttons;
    VImage[] selectedButtons;
    static final int VERTICAL_STEP_BETWEEN_ICONS = 30;
    int selectedIconIndex = -1; // -1 means no button selected

    boolean visible = false;

    static final int ANIM_TIME = 200;
    static final int TRIGGER_ZONE_WIDTH = 48;
    static final int TRIGGER_ZONE_HEIGHT = ICON_PATHS.length * (VERTICAL_STEP_BETWEEN_ICONS) + 24;

    ToolPalette(ZGRViewer app){
	this.application = app;
	initZVTMelements();
    }
    
    void initZVTMelements(){
	paletteSpace = application.vsm.addVirtualSpace(PALETTE_SPACE_NAME);
	paletteCamera = application.vsm.addCamera(PALETTE_SPACE_NAME);
	paletteCamera.setAltitude(0);
	buttons = new VImage[ICON_PATHS.length];
	selectedButtons = new VImage[ICON_PATHS.length];
	for (int i=0;i<buttons.length;i++){
	    buttons[i] = new VImage(0, -i*VERTICAL_STEP_BETWEEN_ICONS, 0,
				    (new ImageIcon(this.getClass().getResource(ICON_PATHS[i]))).getImage());
	    selectedButtons[i] = new VImage(0, -i*VERTICAL_STEP_BETWEEN_ICONS, 0,
					    (new ImageIcon(this.getClass().getResource(SELECTED_ICON_PATHS[i]))).getImage());
	    application.vsm.addGlyph(buttons[i], paletteSpace);
	    application.vsm.addGlyph(selectedButtons[i], paletteSpace);
	}
	selectButton(buttons[0]);
    }

    void selectButton(VImage icon){
	for (int i=0;i<buttons.length;i++){
	    if (buttons[i] == icon || selectedButtons[i] == icon){
		paletteSpace.show(selectedButtons[i]);
		paletteSpace.hide(buttons[i]);
		selectedIconIndex = i;
	    }
	    else {
		paletteSpace.hide(selectedButtons[i]);
		paletteSpace.show(buttons[i]);
	    }
	}
    }

    void updateHiddenPosition(View v){
	long[] wnes = v.getVisibleRegion(paletteCamera);
	for (int i=0;i<buttons.length;i++){
	    buttons[i].moveTo(wnes[0]-buttons[i].getWidth()+1, wnes[1]-(i+1)*VERTICAL_STEP_BETWEEN_ICONS);
	    selectedButtons[i].moveTo(wnes[0]-buttons[i].getWidth()+1, wnes[1]-(i+1)*VERTICAL_STEP_BETWEEN_ICONS);
	}
    }

    void show(){
	if (!visible){
	    visible = true;
	    application.vsm.animator.createCameraAnimation(ANIM_TIME, AnimManager.CA_TRANS_SIG, new LongPoint(-2*buttons[0].getWidth()-5, 0),
							   paletteCamera.getID(), null);
	}
    }

    void hide(){
	if (visible){
	    visible = false;
	    application.vsm.animator.createCameraAnimation(ANIM_TIME, AnimManager.CA_TRANS_SIG, new LongPoint(2*buttons[0].getWidth()+5, 0),
							   paletteCamera.getID(), null);	
	}
    }

    boolean insidePaletteTriggerZone(int jpx, int jpy){
	return (jpx < TRIGGER_ZONE_WIDTH && jpy < TRIGGER_ZONE_HEIGHT);
    }

    boolean isShowing(){
	return visible;
    }

    Camera getPaletteCamera(){
	return paletteCamera;
    }
    
}