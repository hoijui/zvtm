/*   FILE: ZGRViewer.java
 *   DATE OF CREATION:   Wed Aug 30 12:02:31 2006
 *   Copyright (c) INRIA, 2006-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */ 

package net.claribole.zgrviewer;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import javax.swing.ImageIcon;
import java.util.Vector;
import java.util.HashMap;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

public class ToolPalette {
    
    GraphicsManager grMngr;

    static final String PALETTE_SPACE_NAME = "tps";
    VirtualSpace paletteSpace;
    Camera paletteCamera;

    // these correpond to values of selectedIconIndex, (vertically ordered icons)
    static final short STD_NAV_MODE = 0;
    static final short FL_NAV_MODE = 1;
    static final short DM_NAV_MODE = 2;
    static final short PL_NAV_MODE = 3;
    static final short HIGHLIGHT_MODE = 4;
    static final short BRING_AND_GO_MODE = 5;
    static final short LINK_SLIDING_MODE = 6;
    static final short EDIT_MODE = 7;

    static final String[] ICON_PATHS = {"/images/stdnav24b.png",
					"/images/flnav24b.png",
					"/images/dmnav24b.png",
					"/images/plnav24b.png",
					"/images/hl24b.png",
					"/images/fl24b.png",
					"/images/ls24b.png",
					"/images/edit24b.png"};

    static final String[] SELECTED_ICON_PATHS = {"/images/stdnav24g.png",
						 "/images/flnav24g.png",
						 "/images/dmnav24g.png",
						 "/images/plnav24g.png",
						 "/images/hl24g.png",
						 "/images/fl24g.png",
						 "/images/ls24g.png",
 						 "/images/edit24g.png"};

    VImage[] buttons;
    VImage[] selectedButtons;
    static final int VERTICAL_STEP_BETWEEN_ICONS = 30;
    short selectedIconIndex = -1; // -1 means no button selected

    boolean visible = false;
    boolean paintPalette = true; // set to false temporarily during panel resizing operations ; used as an optimization indicator
    boolean enabled = true;

    public static final int ANIM_TIME = 200;
    public static final int TRIGGER_ZONE_WIDTH = 48;
    public static int TRIGGER_ZONE_HEIGHT = ICON_PATHS.length * (VERTICAL_STEP_BETWEEN_ICONS) + 24;

	ToolPalette(GraphicsManager gm){
		this.grMngr = gm;
		initZVTMelements();
        loadPluginModes();
        selectDefaultMode();
	}
    
    void initZVTMelements(){
		paletteSpace = grMngr.vsm.addVirtualSpace(PALETTE_SPACE_NAME);
		paletteCamera = paletteSpace.addCamera();
		paletteCamera.setAltitude(0);
		buttons = new VImage[ICON_PATHS.length];
		selectedButtons = new VImage[ICON_PATHS.length];
		for (int i=0;i<buttons.length;i++){
			buttons[i] = new VImage(0, -i*VERTICAL_STEP_BETWEEN_ICONS, 0,
				(new ImageIcon(this.getClass().getResource(ICON_PATHS[i]))).getImage());
			selectedButtons[i] = new VImage(0, -i*VERTICAL_STEP_BETWEEN_ICONS, 0,
				(new ImageIcon(this.getClass().getResource(SELECTED_ICON_PATHS[i]))).getImage());
			paletteSpace.addGlyph(buttons[i]);
			paletteSpace.addGlyph(selectedButtons[i]);
		}
	}
	
	void selectDefaultMode(){
	    if (defaultPluginModeClassName != null && pluginsWithMode != null && pluginsWithMode.size() > 0){
            for (Short index:pluginsWithMode.keySet()){
                Plugin p = pluginsWithMode.get(index);
                if (p.getClass().getName().equals(defaultPluginModeClassName)){
                    selectButton(buttons[index.shortValue()]);
                    return;
                }
            }
	    }
	    selectButton(buttons[0]);	        
	}
    
    public void setEnabled(boolean b){
        enabled = b;
		paletteCamera.setEnabled(b);
    }
    
    public boolean isEnabled(){
        return enabled;
    }

    public boolean isStdNavMode(){
	return selectedIconIndex == STD_NAV_MODE;
    }

    public boolean isFadingLensNavMode(){
	return selectedIconIndex == FL_NAV_MODE;
    }

    public boolean isDragMagNavMode(){
	return selectedIconIndex == DM_NAV_MODE;
    }

    public boolean isProbingLensNavMode(){
	return selectedIconIndex == PL_NAV_MODE;
    }

    public boolean isHighlightMode(){
	return selectedIconIndex == HIGHLIGHT_MODE;
    }

    public boolean isBringAndGoMode(){
		return selectedIconIndex == BRING_AND_GO_MODE;
    }

    public boolean isLinkSlidingMode(){
		return selectedIconIndex == LINK_SLIDING_MODE;
    }
    
    public boolean isEditMode(){
        return selectedIconIndex == EDIT_MODE;
    }
    
    public short getMode(){
        return selectedIconIndex;
    }

    public void selectButton(VImage icon){
        boolean newIconSelected = false;
        short oldSelectedIconIndex = selectedIconIndex;
        for (short i=0;i<buttons.length;i++){
            // only try to find it in the list of unselected buttons
            if (buttons[i] == icon){
                // this way we are sure it is not the one already selected
                paletteSpace.show(selectedButtons[i]);
                paletteSpace.hide(buttons[i]);
                selectedIconIndex = i;
                newIconSelected = true;
            }
        }
        if (newIconSelected){
            // if a new button has been selected,
            for (int i=0;i<selectedIconIndex;i++){
                // unselect other buttons
                paletteSpace.hide(selectedButtons[i]);
                paletteSpace.show(buttons[i]);
            }
            for (int i=selectedIconIndex+1;i<buttons.length;i++){
                paletteSpace.hide(selectedButtons[i]);
                paletteSpace.show(buttons[i]);
            }
            // and discard resources associated with old mode
            if (oldSelectedIconIndex == DM_NAV_MODE){
                grMngr.killDM();
            }
            else if (oldSelectedIconIndex == BRING_AND_GO_MODE){
                grMngr.exitBringAndGoMode();
            }
            else if (oldSelectedIconIndex == STD_NAV_MODE){
                grMngr.activateDynaSpot(false, false);
            }
            if (selectedIconIndex == BRING_AND_GO_MODE){
                grMngr.enterBringAndGoMode();
            }
            else if (selectedIconIndex == STD_NAV_MODE){
                if (ConfigManager.DYNASPOT){
                    try {grMngr.activateDynaSpot(true, false);}
                    catch (NullPointerException ex){}                    
                }
            }
            else if (oldSelectedIconIndex == EDIT_MODE){
                grMngr.geom.clearSplineEditingGlyphs();
            }
            else if (oldSelectedIconIndex > EDIT_MODE){
                // a plugin mode, notify the plugin
                getPlugin(oldSelectedIconIndex).exitMode();
            }
            if (selectedIconIndex > EDIT_MODE){
                // a plugin mode, notify the plugin
                getPlugin(selectedIconIndex).enterMode();
            }
        }
    }

    /* Called with false when resizing the main view to temporarily hide the palette
       until it actually gets relocated to the top-left corner of that window.
       It is then called with true.*/
    public void displayPalette(boolean b){
		if (paintPalette == b){return;}
		for (int i=0;i<buttons.length;i++){
			buttons[i].setVisible(b);
			selectedButtons[i].setVisible(b);
		}
		paintPalette = b;
	}

    public void updateHiddenPosition(){
		double[] wnes = grMngr.mainView.getVisibleRegion(paletteCamera);
		for (int i=0;i<buttons.length;i++){
			buttons[i].moveTo(wnes[0]-buttons[i].getWidth()/2+1, wnes[1]-(i+1)*VERTICAL_STEP_BETWEEN_ICONS);
			selectedButtons[i].moveTo(wnes[0]-buttons[i].getWidth()/2+1, wnes[1]-(i+1)*VERTICAL_STEP_BETWEEN_ICONS);
		}
		displayPalette(true);
	}

    public void show(){
        if (!visible){
            visible = true;
            Animation a = grMngr.vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(ANIM_TIME, paletteCamera,
                new Point2D.Double(-buttons[0].getWidth()-2, 0), true,
                SlowInSlowOutInterpolator.getInstance(), null);
            grMngr.vsm.getAnimationManager().startAnimation(a, false);
            grMngr.mainView.setCursorIcon(Cursor.HAND_CURSOR);
            grMngr.mainView.setActiveLayer(2);
        }
    }

    public void hide(){
        if (visible){
            visible = false;
            Animation a = grMngr.vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(ANIM_TIME, paletteCamera,
                new Point2D.Double(buttons[0].getWidth()+2, 0), true,
                SlowInSlowOutInterpolator.getInstance(), null);
            grMngr.vsm.getAnimationManager().startAnimation(a, false);
            grMngr.mainView.setCursorIcon(Cursor.CUSTOM_CURSOR);
            grMngr.mainView.setActiveLayer(0);	
        }
    }

    /** 
     * return false if palette is temporarily disabled
     */
    public boolean insidePaletteTriggerZone(int jpx, int jpy){
	    return (paintPalette && jpx < TRIGGER_ZONE_WIDTH && jpy < TRIGGER_ZONE_HEIGHT);
    }

    public boolean isShowing(){
	return visible;
    }

    public Camera getPaletteCamera(){
	return paletteCamera;
    }

    public void showLogicalTools(){
        for (int i=4;i<=6;i++){
            if (!buttons[i].isSensitive()){buttons[i].setSensitivity(true);}
            if (!buttons[i].isVisible()){buttons[i].setVisible(true);}
            if (!selectedButtons[i].isSensitive()){selectedButtons[i].setSensitivity(true);}
            if (!selectedButtons[i].isVisible()){selectedButtons[i].setVisible(true);}
        }
    }

	public void hideLogicalTools(){
		if (isHighlightMode() || isBringAndGoMode() || isLinkSlidingMode()){
			// if a tool that makes needs to know about the logical structure is selected,
			// select something else as they are about to be disabled
			selectButton(buttons[0]);
		}
		for (int i=4;i<=6;i++){
			if (buttons[i].isSensitive()){buttons[i].setSensitivity(false);}
			if (buttons[i].isVisible()){buttons[i].setVisible(false);}
			if (selectedButtons[i].isSensitive()){selectedButtons[i].setSensitivity(false);}
			if (selectedButtons[i].isVisible()){selectedButtons[i].setVisible(false);}
		}
	}
	
	static String defaultPluginModeClassName = null;
	static void setDefaultPluginMode(String pluginClassName){
	    defaultPluginModeClassName = pluginClassName;
	}
	
	HashMap<Short,Plugin> pluginsWithMode;
	
	void loadPluginModes(){
	    Plugin[] plugins = grMngr.cfgMngr.plugins;
	    Vector<Plugin> pwm = new Vector<Plugin>(plugins.length);
		for (int i=0;i<plugins.length;i++){
		    if (plugins[i].hasMode()){
		        pwm.add(plugins[i]);
		    }
		}
	    if (pwm.isEmpty()){return;}
	    pluginsWithMode = new HashMap<Short,Plugin>(pwm.size());
	    VImage[] nbuttons = new VImage[buttons.length+pwm.size()];
	    VImage[] nselectedButtons = new VImage[nbuttons.length];
	    System.arraycopy(buttons, 0, nbuttons, 0, buttons.length);
	    System.arraycopy(selectedButtons, 0, nselectedButtons, 0, selectedButtons.length);
	    buttons = nbuttons;
	    selectedButtons = nselectedButtons;
	    for (int i=EDIT_MODE+1;i<EDIT_MODE+1+pwm.size();i++){
	        Plugin p = pwm.elementAt(i-EDIT_MODE-1);
	        pluginsWithMode.put(new Short((short)i), p);
			buttons[i] = new VImage(0, -i*VERTICAL_STEP_BETWEEN_ICONS, 0, p.getModeIcon());
			selectedButtons[i] = new VImage(0, -i*VERTICAL_STEP_BETWEEN_ICONS, 0, p.getModeIcon());
			paletteSpace.addGlyph(buttons[i]);
			paletteSpace.addGlyph(selectedButtons[i]);	        
	    }
	    // update original trigger zone height
	    TRIGGER_ZONE_HEIGHT = buttons.length * (VERTICAL_STEP_BETWEEN_ICONS) + 24;
	}
	
	Plugin getPlugin(short index){
	    return pluginsWithMode.get(new Short(index));
	}
    
}
