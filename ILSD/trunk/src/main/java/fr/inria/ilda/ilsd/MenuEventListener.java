/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file LICENSE.
 *
 * $Id: $
 */

package fr.inria.ilda.ilsd;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.PickerListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.widgets.PieMenuFactory;
import fr.inria.zvtm.widgets.PieMenu;

class MenuEventListener implements ViewListener, PickerListener {

    static final String MPM_GLOBAL_VIEW = "Global View";
    static final String MPM_FOO1 = "...";
    static final String MPM_FOO2 = "...";
    static final String MPM_FOO3 = "...";
    static final String[] MPM_COMMANDS = {MPM_FOO1, MPM_FOO2, MPM_FOO3, MPM_GLOBAL_VIEW};
    static final Point2D.Double[] MPM_OFFSETS = {new Point2D.Double(0,0), new Point2D.Double(-10,0),
                                                 new Point2D.Double(0,-10), new Point2D.Double(10,0)};

    ILSD app;

    PieMenu mainPieMenu;

    MenuEventListener(ILSD app){
        this.app = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        Glyph g = v.lastGlyphEntered();
        if (g != null){
            if (g.getType() == Config.T_MPMI){
                mainPieMenuEvent(g);
            }
        }
        hideMainPieMenu();
        app.mView.setActiveLayer(ILSD.ANNOT_LAYER);
    }

    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
        updateMenuSpacePicker(jpx, jpy);
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
        updateMenuSpacePicker(jpx, jpy);
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){
        if (g.getType() != null){
            if (g.getType().equals(Config.T_MPMI)){
                g.highlight(true, null);
            }
        }
        else {
            if (mainPieMenu != null && g == mainPieMenu.getBoundary()){
                mainPieMenu.setSensitivity(true);
            }
        }
    }

    public void exitGlyph(Glyph g){
        if (g.getType() != null){
            if (g.getType().equals(Config.T_MPMI)){
                // exiting a pie menu item
                g.highlight(false, null);
            }
        }
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
        app.exit();
    }

    /*------------------ Picking in this layer ------------*/

    Point2D.Double vsCoords = new Point2D.Double();

    void updateMenuSpacePicker(int jpx, int jpy){
        app.mView.fromPanelToVSCoordinates(jpx, jpy, app.mnCamera, vsCoords);
        app.mnSpacePicker.setVSCoordinates(vsCoords.x, vsCoords.y);
        app.mnSpacePicker.computePickedGlyphList(app.mnCamera);
    }

    /*------------------ Pie menu -------------------------*/

    void displayMainPieMenu(){
        app.mView.setActiveLayer(ILSD.MENU_LAYER);
        PieMenuFactory.setSensitivityRadius(1);
        PieMenuFactory.setRadius(140);
        PieMenuFactory.setTranslucency(0.7f);
        mainPieMenu = PieMenuFactory.createPieMenu(MPM_COMMANDS, MPM_OFFSETS, 0, app.mView);
        Glyph[] items = mainPieMenu.getItems();
        for (Glyph item:items){
            item.setType(Config.T_MPMI);
        }
    }

    void hideMainPieMenu(){
        if (mainPieMenu == null){return;}
        mainPieMenu.destroy(0);
        mainPieMenu = null;
        app.mView.setActiveLayer(ILSD.ANNOT_LAYER);
    }

    void mainPieMenuEvent(Glyph menuItem){
        int index = mainPieMenu.getItemIndex(menuItem);
        if (index != -1){
            String label = mainPieMenu.getLabels()[index].getText();
            if (label == MPM_GLOBAL_VIEW){
                app.nav.getGlobalView(null);
            }
        }
    }

}
