/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: MenuManager.java,v 1.11 2007/10/15 12:33:33 pietriga Exp $
 */

package fr.inria.zuist.app.lri;

import java.awt.Color;
import javax.swing.ImageIcon;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VRectangleST;

public class MenuManager {

    LRIExplorer application;

    /* half menu item width and height*/
    static final int NAV_MENU_ITEM_WIDTH = 50;
    static final int NAV_MENU_ITEM_HEIGHT = 10;

    static final int HOME_BT_WIDTH = 12;
    static final int HOME_BT_HEIGHT = NAV_MENU_ITEM_HEIGHT;

    static final int UP_BT_WIDTH = 12;
    static final int UP_BT_HEIGHT = NAV_MENU_ITEM_HEIGHT;

    static final int BACK_BT_WIDTH = 12;
    static final int BACK_BT_HEIGHT = NAV_MENU_ITEM_HEIGHT;

    static final int ABOUT_BT_WIDTH = 42;
    static final int ABOUT_BT_HEIGHT = NAV_MENU_ITEM_HEIGHT;
    
    static final int MENU_TITLE_HOFFSET = 10;
    static final int MENU_TITLE_VOFFSET = 6;
    static final int MENU_ITEM_HOFFSET = 20;
    static final int MENU_ITEM_VOFFSET = -14;
    static final int SELECTION_DISC_HOFFSET = 10;
    static final int SELECTION_DISC_VOFFSET = -12;
    static final int HOME_BT_HOFFSET = 10;
    
    static final int SELECTION_DISC_SIZE = 3;
    
    static final Color MENU_FILL_COLOR = Color.BLACK;
    static final Color MENU_BORDER_COLOR = Color.WHITE;
    static final float MENU_TRANSLUCENCE = 0.8f;
    
    static final int MENU_ANIM_TIME = 300;

	static final int MENU_ZONE_WIDTH = 2 * (NAV_MENU_ITEM_WIDTH + HOME_BT_WIDTH + UP_BT_WIDTH + BACK_BT_WIDTH + ABOUT_BT_WIDTH);
	static final int MENU_ZONE_HEIGHT = 2 * NAV_MENU_ITEM_HEIGHT;
	
	static final String HOME_ICON_PATH = "/images/home-icon.png";
	static final String UP_ICON_PATH = "/images/up-icon.png";
	static final String BACK_ICON_PATH = "/images/back-icon.png";

    VRectangleST navMenuTitleBox;
    VText navMenuTitleLabel;
    VRectangle navMenuSelected;

    VRectangleST homeBtBox;
    VImage homeBtIcon;

    VRectangleST upBtBox;
    VImage upBtIcon;
    VRectangleST backBtBox;
    VImage backBtIcon;

    VRectangleST aboutBtBox;
    VText aboutBtLabel;
    
    VRectangleST breadcrumbsBox;
    
    /* Name of item also stored in box's type field */
    static final String[] navMenuItems = {LRIExplorer.NAV_MODE_DEFAULT_STR, LRIExplorer.NAV_MODE_FISHEYE_STR, LRIExplorer.NAV_MODE_FISHEYE2_STR, LRIExplorer.NAV_MODE_SS_STR};
    VRectangleST[] navMenuItemBoxes = new VRectangleST[navMenuItems.length];
    VText[] navMenuItemLabels = new VText[navMenuItems.length];
    
    MenuManager(LRIExplorer app){
        this.application = app;
        initMenu();
    }
    
    void initMenu(){
        for (int i=0;i<navMenuItems.length;i++){
            navMenuItemBoxes[i] = new VRectangleST(0, 0, 0, NAV_MENU_ITEM_WIDTH, NAV_MENU_ITEM_HEIGHT,
                MENU_FILL_COLOR, MENU_BORDER_COLOR, MENU_TRANSLUCENCE);
            navMenuItemBoxes[i].setType(navMenuItems[i]);
            navMenuItemLabels[i] = new VText(0, 0, 0, MENU_BORDER_COLOR, navMenuItems[i]);
            application.vsm.addGlyph(navMenuItemBoxes[i], application.mnSpace);
            application.vsm.addGlyph(navMenuItemLabels[i], application.mnSpace);
        }
        navMenuSelected = new VRectangle(0, 0, 0, SELECTION_DISC_SIZE, SELECTION_DISC_SIZE, MENU_BORDER_COLOR);
        navMenuSelected.setDrawBorder(false);
        application.vsm.addGlyph(navMenuSelected, application.mnSpace);
        /* add title after items so that items are below title (looks better when contracting/expanding menu) */
        navMenuTitleBox = new VRectangleST(0, 0, 0, NAV_MENU_ITEM_WIDTH, NAV_MENU_ITEM_HEIGHT,
            MENU_FILL_COLOR, MENU_BORDER_COLOR, MENU_TRANSLUCENCE);
        navMenuTitleLabel = new VText(0, 0, 0, MENU_BORDER_COLOR, "Navigation Mode");
        application.vsm.addGlyph(navMenuTitleBox, application.mnSpace);
        application.vsm.addGlyph(navMenuTitleLabel, application.mnSpace);
        homeBtBox = new VRectangleST(0, 0, 0, HOME_BT_WIDTH, HOME_BT_HEIGHT,
            MENU_FILL_COLOR, MENU_BORDER_COLOR, MENU_TRANSLUCENCE);
        homeBtIcon = new VImage(0, 0, 0, (new ImageIcon(this.getClass().getResource(HOME_ICON_PATH))).getImage());
        application.vsm.addGlyph(homeBtBox, application.mnSpace);
        application.vsm.addGlyph(homeBtIcon, application.mnSpace);
        upBtBox = new VRectangleST(0, 0, 0, UP_BT_WIDTH, UP_BT_HEIGHT,
            MENU_FILL_COLOR, MENU_BORDER_COLOR, MENU_TRANSLUCENCE);
        upBtIcon = new VImage(0, 0, 0, (new ImageIcon(this.getClass().getResource(UP_ICON_PATH))).getImage());
        application.vsm.addGlyph(upBtBox, application.mnSpace);
        application.vsm.addGlyph(upBtIcon, application.mnSpace);
        backBtBox = new VRectangleST(0, 0, 0, BACK_BT_WIDTH, BACK_BT_HEIGHT,
            MENU_FILL_COLOR, MENU_BORDER_COLOR, MENU_TRANSLUCENCE);
        backBtIcon = new VImage(0, 0, 0, (new ImageIcon(this.getClass().getResource(BACK_ICON_PATH))).getImage());
        application.vsm.addGlyph(backBtBox, application.mnSpace);
        application.vsm.addGlyph(backBtIcon, application.mnSpace);
        aboutBtBox = new VRectangleST(0, 0, 0, ABOUT_BT_WIDTH, ABOUT_BT_HEIGHT,
            MENU_FILL_COLOR, MENU_BORDER_COLOR, MENU_TRANSLUCENCE);
        aboutBtLabel = new VText(0, 0, 0, MENU_BORDER_COLOR, "About ZUIST...", VText.TEXT_ANCHOR_MIDDLE);
        application.vsm.addGlyph(aboutBtBox, application.mnSpace);
        application.vsm.addGlyph(aboutBtLabel, application.mnSpace);
        breadcrumbsBox = new VRectangleST(0, 0, 0, (LRIExplorer.VIEW_MAX_W-MENU_ZONE_WIDTH)/2, UP_BT_HEIGHT,
            MENU_FILL_COLOR, MENU_BORDER_COLOR, MENU_TRANSLUCENCE);
        application.vsm.addGlyph(breadcrumbsBox, application.mnSpace);
        layoutMenuItems();
    }
    
    void layoutMenuItems(){
        navMenuTitleBox.moveTo(-application.panelWidth/2+NAV_MENU_ITEM_WIDTH, application.panelHeight/2-NAV_MENU_ITEM_HEIGHT);
        navMenuTitleLabel.moveTo(navMenuTitleBox.vx-navMenuTitleBox.getWidth()+MENU_TITLE_HOFFSET, navMenuTitleBox.vy-navMenuTitleBox.getHeight()+MENU_TITLE_VOFFSET);
        for (int i=0;i<navMenuItems.length;i++){
            navMenuItemBoxes[i].moveTo(-application.panelWidth/2+NAV_MENU_ITEM_WIDTH,
                application.panelHeight/2-NAV_MENU_ITEM_HEIGHT+2*NAV_MENU_ITEM_HEIGHT+1);
            navMenuItemLabels[i].moveTo(-application.panelWidth/2+MENU_ITEM_HOFFSET,
                application.panelHeight/2+MENU_ITEM_VOFFSET+2*NAV_MENU_ITEM_HEIGHT+1);
        }
        navMenuSelected.moveTo(-application.panelWidth/2+SELECTION_DISC_HOFFSET,
            application.panelHeight/2+SELECTION_DISC_VOFFSET+2*NAV_MENU_ITEM_HEIGHT+1);
        homeBtBox.moveTo(navMenuTitleBox.vx+navMenuTitleBox.getWidth()+homeBtBox.getWidth(),
 			navMenuTitleBox.vy);
        homeBtIcon.moveTo(homeBtBox.vx, homeBtBox.vy);
        upBtBox.moveTo(homeBtBox.vx+homeBtBox.getWidth()+upBtBox.getWidth(),
 			navMenuTitleBox.vy);
        upBtIcon.moveTo(upBtBox.vx, upBtBox.vy);
        backBtBox.moveTo(upBtBox.vx+upBtBox.getWidth()+backBtBox.getWidth(),
 			navMenuTitleBox.vy);
        backBtIcon.moveTo(backBtBox.vx, backBtBox.vy);
        aboutBtBox.moveTo(backBtBox.vx+backBtBox.getWidth()+aboutBtBox.getWidth(),
 			navMenuTitleBox.vy);
        aboutBtLabel.moveTo(aboutBtBox.vx, aboutBtBox.vy-aboutBtBox.getHeight()+MENU_TITLE_VOFFSET);
        breadcrumbsBox.moveTo(aboutBtBox.vx+aboutBtBox.getWidth()+breadcrumbsBox.getWidth(),
 			navMenuTitleBox.vy);
    }

    // expand = true => expand, contract if false
    void expandNavMenu(boolean expand){
        LongPoint animData;
        if (expand){
            for (int i=0;i<navMenuItems.length;i++){
                animData = new LongPoint(0, -(i+2)*2*NAV_MENU_ITEM_HEIGHT);
                application.vsm.animator.createGlyphAnimation(MENU_ANIM_TIME, AnimManager.GL_TRANS_SIG, animData, navMenuItemBoxes[i].getID());
                application.vsm.animator.createGlyphAnimation(MENU_ANIM_TIME, AnimManager.GL_TRANS_SIG, animData, navMenuItemLabels[i].getID());
            }
            animData = new LongPoint(0, -(application.NAV_MODE+2)*2*NAV_MENU_ITEM_HEIGHT);
            application.vsm.animator.createGlyphAnimation(MENU_ANIM_TIME, AnimManager.GL_TRANS_SIG, animData, navMenuSelected.getID());
        }
        else {
            for (int i=0;i<navMenuItems.length;i++){
                animData = new LongPoint(0, application.panelHeight/2-NAV_MENU_ITEM_HEIGHT+2*NAV_MENU_ITEM_HEIGHT+1 - navMenuItemBoxes[i].vy);
                application.vsm.animator.createGlyphAnimation(MENU_ANIM_TIME, AnimManager.GL_TRANS_SIG, animData, navMenuItemBoxes[i].getID());
                animData = new LongPoint(0, application.panelHeight/2+MENU_ITEM_VOFFSET+2*NAV_MENU_ITEM_HEIGHT+1 - navMenuItemLabels[i].vy);
                application.vsm.animator.createGlyphAnimation(MENU_ANIM_TIME, AnimManager.GL_TRANS_SIG, animData, navMenuItemLabels[i].getID());
            }
            animData = new LongPoint(0, application.panelHeight/2+SELECTION_DISC_VOFFSET+2*NAV_MENU_ITEM_HEIGHT+1 - navMenuSelected.vy);
            application.vsm.animator.createGlyphAnimation(MENU_ANIM_TIME, AnimManager.GL_TRANS_SIG, animData, navMenuSelected.getID());            
        }
        
    }

}
