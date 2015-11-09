/*   Copyright (c) INRIA, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: SmartiesManager.java 333 2014-06-11 21:39:31Z fdelcampo $
 */

package cl.inria.massda;


import java.awt.geom.Point2D;
import java.awt.Color;

//import fr.inria.zuist.cluster.viewer.WallCursor;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.awt.geom.Point2D;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;

import fr.lri.smarties.libserver.Smarties;
import fr.lri.smarties.libserver.SmartiesColors;
import fr.lri.smarties.libserver.SmartiesEvent;
import fr.lri.smarties.libserver.SmartiesPuck;
import fr.lri.smarties.libserver.SmartiesDevice;
import fr.lri.smarties.libserver.SmartiesWidget;
import fr.lri.smarties.libserver.SmartiesWidgetHandler;

import java.util.Observer;
import java.util.Observable;

import org.json.JSONObject;
import org.json.JSONException;

import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.Glyph;

import fr.inria.zvtm.event.PickerListener;

import java.awt.Font;

import fr.inria.zuist.viewer.JSkyFitsViewer;

import jsky.coords.WorldCoords;

public class SmartiesManager implements Observer {

    static final Font FONT_CURSOR = new Font("default", Font.PLAIN, 16);
    static final Font FONT_DIST = new Font("default", Font.PLAIN, 13);

    JSkyFitsViewer app;

    Smarties smarties;

    SmartiesPuck pinchPuck;
    SmartiesDevice pinchDevice;
    SmartiesPuck dragPuck;
    SmartiesDevice dragDevice;

    SmartiesWidget swWCS;
    SmartiesWidget swDist;

    float prevMFPinchD = 0;
    float prevMFMoveX = 0;
    float prevMFMoveY = 0;


    int countWidget;
    int ui_swwcs;
    int ui_swdist;
    int ui_swsystem;
    int ui_swrescale;
    int ui_swquery;

    Map<String, VSegment> linesDist;
    Map<String, VText> labelsDist;

    boolean query = false;
    Point2D.Double rightClickPress;
    //VCircle rightClickSelectionG = new VCircle(0, 0, 1000, 1, Color.BLACK, Color.RED, 0.1f);
    Point2D.Double coordClickPress;


    public SmartiesManager(JSkyFitsViewer app){


        this.app = app;
        smarties = new Smarties((int)app.SCENE_W , (int)app.SCENE_H,
                                6, 4);
        smarties.initWidgets(7,2);

        countWidget = 0;


        SmartiesWidget sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Global View", 4, 2, 1, 1);
        sw.handler = new EventGlobalView();
        countWidget++;
        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Center Cursor", 5, 2, 1, 1);
        sw.handler = new EventCenterCursor();
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Color <", 2, 1, 1, 1);
        sw.handler = new EventPreviousColor();
        countWidget++;
        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Color >", 2, 2, 1, 1);
        sw.handler = new EventNextColor();
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Scale <", 3, 1, 1, 1);
        sw.handler = new EventPreviousScale();
        countWidget++;
        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Scale >", 3, 2, 1, 1);
        sw.handler = new EventNextScale();
        countWidget++;

        swWCS = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "WCS: Off", 6, 1, 1, 1);
        swWCS.labelOn = new String("WCS: On");
        swWCS.handler = new EventWCS();
        ui_swwcs = countWidget;
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "Global", 4, 1, 1, 1);
        sw.labelOn = new String("Local");
        sw.handler = new EventRescale();
        ui_swrescale = countWidget;
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "Ecuatorial", 5, 1, 1, 1);
        sw.labelOn = new String("Galactical");
        sw.handler = new EventCoordinateSystem();
        ui_swsystem = countWidget;
        countWidget++;

        swDist = smarties.addWidget(
                               SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "Distance: Off", 6, 2, 1, 1);
        swDist.labelOn = new String("Distance: On");
        swDist.handler = new EventDistance();
        ui_swdist = countWidget;
        countWidget++;

/*
        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_POPUPMENU, "Positions", 3, 1, 1, 1);
        sw.handler = new PopupMenuButtonClicked();
        for(SavedPosition i: sp){
            System.out.println(i);
            sw.items.add(i.getName());
        }
        ui_swspos = countWidget;
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TEXT_BUTTON, "new Position", 4, 1, 1, 1);
        sw.handler = new EventSavePosition();
        countWidget++;
*/

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "-", 1, 2, 1, 1);
        sw.handler = new EventHigherView();
        countWidget++;
        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "+", 1, 1, 1, 1);
        sw.handler = new EventLowerView();
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Portal", 7, 2, 1, 1);
        sw.handler = new EventPortal();
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "Query", 7, 1, 1, 1);
        sw.handler = new EventQuery();
        ui_swquery = countWidget;
        countWidget++;

        smarties.addObserver(this);

        linesDist = new HashMap<String, VSegment>();
        labelsDist = new HashMap<String, VText>();

        smarties.Run();
    }


    public void update(Observable obj, Object arg){

        if (arg instanceof SmartiesEvent) {

            final SmartiesEvent se = (SmartiesEvent)arg;
            switch (se.type){

                case SmartiesEvent.SMARTIE_EVENTS_TYPE_CREATE:{
                    System.out.println("Create Puck: " + se.id);
                    se.p.app_data = new MyCursor(se.p.id, se.p.x, se.p.y);
                    MyCursor c = (MyCursor)se.p.app_data;
                    c.setVisible(true);
                    c.updateWCS();
                    if(swWCS.on){
                        c.labelSetVisible(true);
                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_SELECT:{
                    System.out.println("Select Puck: " + se.id);
                    //_checkWidgetState(e.device, e.p);
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_STORE:{
                    //repaint();
                    if (se.p != null){
                        MyCursor c = (MyCursor)se.p.app_data;
                        c.setVisible(false);
                        //_repaintCursor(c);
                    }
                    break;  
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_UNSTORE:{
                    if (se.p != null){
                        MyCursor c = (MyCursor)se.p.app_data;
                        c.move(se.p.x, se.p.y);
                        c.setVisible(true);
                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_DELETE:{
                    MyCursor c = (MyCursor)se.p.app_data;
                    c.dispose();
                    
                    updateDistance();
                    smarties.deletePuck(se.p.id);
                    break;
                }

                case SmartiesEvent.SMARTIE_EVENTS_TYPE_START_MFPINCH:{
                    System.out.println("SMARTIE_EVENTS_TYPE_START_MFPINCH");
                    if (pinchDevice == null){
                        pinchDevice = se.device;
                        pinchPuck = se.p;
                        prevMFPinchD = se.d;
                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_MFPINCH:{
                    System.out.println("SMARTIE_EVENTS_TYPE_MFPINCH");
                    if (pinchDevice == se.device){
                        if (se.d > 0){
                            double x = (se.p != null) ? se.p.x : se.x;
                            double y = (se.p != null) ? se.p.y : se.y;
                            MyCursor c = (se.p != null) ? (MyCursor)se.p.app_data : null;
                            if(c!= null && c.isPortal()){
                                c.setZoom(prevMFPinchD/se.d);
                            } else {
                                app.centeredZoom(prevMFPinchD/se.d,
                                                        x*(float)app.SCENE_W,
                                                        y*(float)app.SCENE_H);
                            }
                            
                        }
                        prevMFPinchD = se.d;
                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_END_MFPINCH:{
                    System.out.println("SMARTIE_EVENTS_TYPE_END_MFPINCH");
                    if (pinchDevice == se.device){
                        pinchDevice = null;
                        pinchPuck = null;
                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_START_MFMOVE:{
                    System.out.println("SMARTIE_EVENTS_TYPE_START_MFMOVE");
                    if (dragDevice == null){
                        // drag not locked by an other device
                        dragDevice = se.device;
                        dragPuck = se.p;
                        prevMFMoveX = se.x;
                        prevMFMoveY = se.y; 
                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_MFMOVE:{
                    System.out.println("SMARTIE_EVENTS_TYPE_MFMOVE");
                    if (dragDevice == se.device){
                        // this is the device that lock the drag, e.p should be == dragPuck
                        float dx = (se.x - prevMFMoveX)*(float)app.getDisplayWidth();
                        float dy = (se.y - prevMFMoveY)*(float)app.getDisplayHeight();
                        MyCursor c = (se.p != null) ? (MyCursor)se.p.app_data : null;
                        if(c!= null && c.isPortal()){
                            dx = (prevMFMoveX - se.x)*(float)app.getDisplayWidth();
                            dy = (prevMFMoveY - se.y)*(float)app.getDisplayHeight();
                            c.setLocation(-dx, dy);
                        } else {
                            app.directTranslate(-dx, dy);
                        }
                        prevMFMoveX = se.x;
                        prevMFMoveY = se.y;
                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_END_MFMOVE:
                    System.out.println("SMARTIE_EVENTS_TYPE_END_MFMOVE");
                    if (dragDevice == se.device){
                        // this is the device that lock the drag
                        dragDevice = null;
                        dragPuck = null;
                    }
                    break;
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_START_MOVE:{
                    System.out.println("SMARTIE_EVENTS_TYPE_START_MOVE");
                    if (se.p != null){
                        if (se.mode == SmartiesEvent.SMARTIE_GESTUREMOD_DRAG && dragDevice == null){//} && !modeDrawRect){
                            // drag not locked by an other device
                            dragDevice = se.device;
                            dragPuck = se.p;
                            prevMFMoveX = se.x;
                            prevMFMoveY = se.y;
                        } else if(query){
                            MyCursor c = (se.p != null) ? (MyCursor)se.p.app_data : null;
                            rightClickPress = c.getLocation();
                            double[] coordPress = app.coordinateTransform(app.getCursorCamera(), app.getMainCamera(), rightClickPress.getX(), rightClickPress.getY());
                            coordClickPress = new Point2D.Double(coordPress[0], coordPress[1]);

                            app.initClickSelection(rightClickPress);
                            
                        }

                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_MOVE:{
                    System.out.println("SMARTIE_EVENTS_TYPE_MOVE");
                    if (se.p != null){
                        MyCursor c = (MyCursor)se.p.app_data;
                        c.move(se.p.x, se.p.y);
                        setMoving(true);

                        if(swWCS.on) c.labelSetVisible(false);
                        if (se.mode == SmartiesEvent.SMARTIE_GESTUREMOD_DRAG && dragDevice == se.device ){//&& !modeDrawRect){
                            // this is the device that lock the drag, e.p should be == dragPuck
                            float dx = (se.x - prevMFMoveX)*(float)app.getDisplayWidth();
                            float dy = (se.y - prevMFMoveY)*(float)app.getDisplayHeight();
                            app.directTranslate(-dx, dy);
                            prevMFMoveX = se.x;
                            prevMFMoveY = se.y;
                        }
                        if(query && c != null){
                            Point2D.Double point = c.getLocation();
                            app.updateClickSelection(rightClickPress, point);
                        }

                    }

                    break;
                }

                case SmartiesEvent.SMARTIE_EVENTS_TYPE_END_MOVE:{
                    System.out.println("SMARTIE_EVENTS_TYPE_END_MOVE");
                    if (dragDevice == se.device){//} && !modeDrawRect){
                        // this is the device that lock the drag
                        dragDevice = null;
                        dragPuck = null;
                    }
                    if (se.p != null){
                        MyCursor c = (MyCursor)se.p.app_data;
                        c.updateWCS();
                        setMoving(false);
                        updateDistance();
                        if(swWCS.on) c.labelSetVisible(true);
                        se.p.app_data = c; // CHECK
                        if(query){
                            query = false;
                            rightClickPress = c.getLocation();
                            double[] coordRelease = app.coordinateTransform(app.getCursorCamera(), app.getMainCamera(), rightClickPress.getX(), rightClickPress.getY());
                            Point2D.Double coordClickRelease = new Point2D.Double(coordRelease[0], coordRelease[1]);
                            app.querySimbad(coordClickPress, coordClickRelease);
                        }
                    }
               
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_STRING_EDIT:
                    System.out.println("SMARTIE_EVENTS_TYPE_STRING_EDIT");
                    if (se.widget.handler != null) se.widget.handler.callback(se.widget, se, this);
                    break;
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_WIDGET:{
                    System.out.println("SMARTIE_EVENTS_TYPE_WIDGET");
                    System.out.println("OK");
                    System.out.println("get string: " + se.text + " Canceled?" + se.widget.cancel);
                    System.out.println("se: " + se + " se.widget: " + se.widget + " se.widget.label: " + se.widget.label);

                    if (se.widget.handler != null) se.widget.handler.callback(se.widget, se, this);
                    break;
                }

                case SmartiesEvent.SMARTIE_EVENTS_TYPE_MULTI_TAPS:
                    System.out.println("SMARTIE_EVENTS_TYPE_MULTI_TAPS");
                    //System.out.println("num_tapes: " + se.num_tapes + " num_fingers: " + se.num_fingers);
                    if(se.num_taps == 2 && se.num_fingers == 1 && se.p != null){
                        double x = se.p.x;
                        double y = se.p.y;
                        final MyCursor c = (MyCursor)se.p.app_data;
                        c.wc.setVisible(false);
                        app.traslateAnimated(x*app.getDisplayWidth(), y*app.getDisplayHeight(), null);
                        EndAction ea  = new EndAction(){
                            public void execute(Object subject, Animation.Dimension dimension){
                                c.move(se.p.x, se.p.y);
                                c.wc.setVisible(true);
                            }
                        };
                        double f = -(app.mCamera.getAltitude() + app.mCamera.getFocal())/1.3f;
                        app.zoomAnimated(f, ea);
                        smarties.movePuck(se.p.id,0.5f, 0.5f);
                        prevMFMoveX = se.x;
                        prevMFMoveY = se.y;
                        //prevMFPinchD = se.d;
                    }

                    break;
                //case SmartiesEvent.SMARTIE_EVENTS_TYPE_NONE:
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_KEYUP:
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_KEYDOWN:
                //case SmartiesEvent.SMARTIE_EVENTS_TYPE_KEY:

                    System.out.println("SMARTIE_EVENTS_TYPE_NONE / KEYUP / KEYDOWN");
                    break;
                default:{
                    //System.out.println("OTHER: " + se.type);
                    break;
                }
            }
        } else {
            System.out.println("!(arg instanceof SmartiesEvent)");
        }

    }

    public void updateDistance(){
        if(swDist.on){
            Map<Integer,SmartiesPuck> pucks = smarties.getPuckMapping();
            for (Map.Entry<Integer,SmartiesPuck> puck_a : pucks.entrySet() ) {
                for (Map.Entry<Integer,SmartiesPuck> puck_b : pucks.entrySet() ) {
                    if( !puck_a.getKey().equals(puck_b.getKey()) ){
                        MyCursor a = (MyCursor)(puck_a.getValue().app_data);
                        MyCursor b = (MyCursor)(puck_b.getValue().app_data);
                        if(a != null && b != null){
                            a.distanceLine(b);
                            a.distanceLabel(b);
                        }
                    }
                }
            }
        }
    }

    public void updateDistance(MyCursor c){
         if(swDist.on){
            Map<Integer,SmartiesPuck> pucks = smarties.getPuckMapping();
            for (Map.Entry<Integer,SmartiesPuck> puck_a : pucks.entrySet() ) {
                for (Map.Entry<Integer,SmartiesPuck> puck_b : pucks.entrySet() ) {
                    if( !puck_a.getKey().equals(puck_b.getKey()) ){
                        MyCursor a = (MyCursor)(puck_a.getValue().app_data);
                        MyCursor b = (MyCursor)(puck_b.getValue().app_data);
                        if(a != null && b != null && (a.equals(c) || b.equals(c)) ){
                            a.distanceLine(b);
                            a.distanceLabel(b);
                        }
                    }
                }
            }
        }
    }

    public void removeDistance(){
        if(linesDist!= null){
            Map<String,VSegment> lines = new HashMap<String,VSegment>(linesDist);
            for (Map.Entry<String,VSegment> line : lines.entrySet()) {
                app.cursorSpace.removeGlyph(line.getValue());
                linesDist.remove(line.getKey());
            }
        }
        if(labelsDist != null){
            Map<String,VText> labels = new HashMap<String,VText>(labelsDist);
            for (Map.Entry<String,VText> label : labels.entrySet()) {
                app.cursorSpace.removeGlyph(label.getValue());
                labelsDist.remove(label.getKey());
            }
        }
    }

    public void setMoving(boolean b){
        if(labelsDist != null){
            Map<String,VText> labels = new HashMap<String,VText>(labelsDist);
            for (Map.Entry<String,VText> label : labels.entrySet()) {
                label.getValue().setVisible(!b);
            }
        }
    }

    public void queryOn(){
        Map<String,SmartiesDevice> devices = smarties.getDevicesMapping();
        for(Map.Entry<String,SmartiesDevice> device : devices.entrySet()){
            smarties.sendWidgetOnState(ui_swquery, true, device.getValue());
        }
    }

    public void queryOff(){
        Map<String,SmartiesDevice> devices = smarties.getDevicesMapping();
        for(Map.Entry<String,SmartiesDevice> device : devices.entrySet()){
            smarties.sendWidgetOnState(ui_swquery, false, device.getValue());
        }
    }


    public class MyCursor implements Observer, PickerListener {

        public static final String T_SMARTIES = "Smarties";

        public int id;
        public double x, y;
        public Color color;
        public WallCursor wc;
        public Point2D.Double delta;
        public VText labelfst;
        public VText labelsnd;
        public final String TYPE = "MyCursor";
        
        boolean isLabelVisible = false;
        boolean isGalactical = false;

        String sexagesimal;
        String coordinate;

        String ecuatorial;
        String galactical;
        double ra;
        double dec;
        double l;
        double b;
        
        PortalManager prtMng;
        
        public MyCursor(int id, double x, double y){
            this.id = id;
            this.x = x;
            this.y = y;
            this.color = SmartiesColors.getPuckColorById(id);

            wc = new WallCursor(
                app.cursorSpace,
                (true) ? 10 : 2, (true) ? 100 : 20,
                this.color);
            labelsnd = new VText(0.0, 0.0, 0, color, Color.BLACK, "", VText.TEXT_ANCHOR_START, 1f, 1f);
            labelsnd.setFont(SmartiesManager.FONT_CURSOR);

            labelsnd.setVisible(isLabelVisible);
            app.cursorSpace.addGlyph(labelsnd);
            app.cursorSpace.onTop(labelsnd);

            labelfst = new VText(0.0, 0.0, 0, color, Color.BLACK, "", VText.TEXT_ANCHOR_START, 1f, 1f);
            labelfst.setFont(SmartiesManager.FONT_CURSOR);

            labelfst.setVisible(isLabelVisible);
            app.cursorSpace.addGlyph(labelfst);
            app.cursorSpace.onTop(labelfst);

            app.pythonWCS.addObserver(this);

            move(x, y);

        }

        public void enterGlyph(Glyph g){
            System.out.println("enterGlyph");
        }
        public void exitGlyph(Glyph g){
            System.out.println("exitGlyph");
        }

        public void dispose(){
            wc.dispose();
            if(labelfst != null) app.cursorSpace.removeGlyph(labelfst);
            if(labelsnd != null) app.cursorSpace.removeGlyph(labelsnd);
            app.pythonWCS.deleteObserver(this);
            removeDistance();
            if(prtMng != null) prtMng.killDM();
        }

        public void setVisible(boolean b){
            wc.setVisible(b);
            if(isLabelVisible){
                labelSetVisible(b);
            }
        }

        public void move(double x, double y){
            this.x = x; this.y = y;
            Point2D.Double point = getLocation();
            wc.moveTo((long)(point.getX()), (long)(point.getY()));
            wc.moveTo(point.getX(), point.getY());
            labelsnd.moveTo((long)(point.getX()+50), (long)(point.getY()+50));
            labelfst.moveTo((long)(point.getX()+50), (long)(point.getY()+70));
            updateDistance(this);

            if(app.rPicker != null){
                double[] loc = app.coordinateTransform(app.cursorCamera, app.mCamera, point.getX(), point.getY());
                //System.out.println("rPicker.setVSCoordinates");
                //System.out.println(loc[0]+", "+loc[1]);
                app.rPicker.setVSCoordinates(loc[0], loc[1]);

            }
        }

        public void setLocation(double x, double y){
            if(isPortal())
                prtMng.setLocation(x, y);
        }

        public void setZoom(double f){
            if(isPortal())
                prtMng.setZoom(f);
        }

        public boolean isPortal(){
            return prtMng != null;
        }

        public void togglePortal(){
            if(prtMng == null){

                Point2D.Double point = getLocation();
                double alt = app.getMainCamera().getAltitude();
                double[] loc = app.coordinateTransform(app.getCursorCamera(), app.getMainCamera(), point.getX(), point.getY());
                Location l = new Location(loc[0], loc[1], alt);
                prtMng = new PortalManager(app, color);
                double[] lPortal = app.coordinateTransform(app.getCursorCamera(), prtMng.getCamera(), point.getX(), point.getY());
                prtMng.createDM((int)(x*app.SCENE_W), (int)(y*app.SCENE_H), l);
                prtMng.resize(500, 500);
                /*double[] coord = app.coordinateTransform(app.cursorCamera, prtMng.getCamera(), point.getX(), point.getY());
                prtMng.moveTo(coord[0], coord[1]);*/
            } else {
                prtMng.killDM();
                prtMng = null;
            }
            
        }

        public void labelSetVisible(boolean b){
            /*
            if(!isLabelVisible && b){
                isLabelVisible = b;
                labelsnd.setVisible(b);
                labelfst.setVisible(b);
                updateWCS();
            }
            */
            if(isLabelVisible != b){
                isLabelVisible = b;
                labelsnd.setVisible(b);
                labelfst.setVisible(b);
            }
        }

        public boolean isGalactical(){
            return isGalactical;
        }

        public void setGalactical(boolean galactical){
            if(isGalactical != galactical){
                isGalactical = galactical;
                updateLabel();
            }
        }

        public boolean isLabelVisible(){
            return isLabelVisible;
        }

        public void updateWCS(){
            //System.out.println("updateWCS()");
            //Point2D.Double pWCS = new Point2D.Double(wc.getX(), wc.getY());
            //System.out.println("WallCursor: ");
            //System.out.println(pWCS);
            //System.out.println(getLocation());
            //app.coordinateWCS(pWCS, T_SMARTIES+"_"+id);
            app.coordinateWCS(getLocation(), T_SMARTIES+"_"+id);
            /*
            if(isLabelVisible){
                app.coordinateWCS(pWCS);
                //app.coordinateWCS(pWCS, this);
                //updateLabel(app.getRaDec(), app.getGalactic());//+" - Object: "+app.getObjectName(pWCS));
            }
            */
        }

        public Point2D.Double getLocation(){
            /*
            System.out.println("getLocation()");
            System.out.println(wc.getLocation());
            System.out.println( new Point2D.Double(x*app.SCENE_W - app.SCENE_W/2.0, app.SCENE_H/2.0 - y*app.SCENE_H) );
            */
            return new Point2D.Double(x*app.SCENE_W - app.SCENE_W/2.0, app.SCENE_H/2.0 - y*app.SCENE_H);
            
            //return (Point2D.Double)wc.getLocation();
        }


        public void updateLabel(){

            if(!isGalactical){
                sexagesimal = "Ecuatorial: " + ecuatorial;
                coordinate = "Ra: " + ra + " -- Dec: " + dec;
            } else {
                sexagesimal = "Galactic: " + galactical;
                coordinate = "L: " + l+ " -- B: " + b;
            }

            labelsnd.setText(coordinate);
            labelfst.setText(sexagesimal);
        }

        public void distanceLine(MyCursor c){
            String parid = (id < c.id) ? id+"-"+c.id : c.id + "-" + id;

            Point2D.Double coord1 = getLocation();
            Point2D.Double coord2 = c.getLocation();

            if(!linesDist.containsKey(parid)){
                //System.out.println("!linesDist.containsKey(parid)");
                VSegment line = new VSegment(coord1.getX(), coord1.getY(), coord2.getX(), coord2.getY(), 1, color);
                app.cursorSpace.addGlyph(line);
                linesDist.put(parid, line);
            } else {
                VSegment line = linesDist.get(parid);
                if(line != null){
                    line.setEndPoints(coord1.getX(), coord1.getY(), coord2.getX(), coord2.getY());
                }
            }
        }

        public void distanceLabel(MyCursor c){
            String parid = (id < c.id) ? id+"-"+c.id : c.id + "-" + id;
            String text;
            if(isGalactical){
                //double dl = l - c.l;
                //double db = b - c.b;

                //compute radius in arcmin
                final WorldCoords w1 = new WorldCoords(l, b);
                final WorldCoords w2 = new WorldCoords(c.l, c.b);

                final double dist = w1.dist(w2);
                System.out.println("Distance: " + dist + " arcminutes");

                text = dist + " arcminutes";

                //System.out.println(dl + " -- " + db); 
                //text = Math.sqrt(dl*dl) + " L -- " +Math.sqrt(db*db) + " B";
            } else {
                //double dra = ra - c.ra;
                //double ddec = dec - c.dec;
                //System.out.println(dra + " -- " + ddec);
                //text = Math.sqrt(dra*dra) + " RA -- " +Math.sqrt(ddec*ddec) + " DEC";
                //compute radius in arcmin
                final WorldCoords w1 = new WorldCoords(ra, dec);
                final WorldCoords w2 = new WorldCoords(c.ra, c.dec);

                final double dist = w1.dist(w2);
                System.out.println("Distance: " + dist + " arcminutes");

                text = dist + " arcminutes";
            }


            Point2D.Double coord1 = getLocation();
            Point2D.Double coord2 = c.getLocation();

            double x = (coord1.getX() + coord2.getX())/2;
            double y = (coord1.getY() + coord2.getY())/2;

            if(!labelsDist.containsKey(parid)){
                VText label = new VText(x, y, 1, color, Color.BLACK, text, VText.TEXT_ANCHOR_START, 1f, 1f);
                label.setFont(SmartiesManager.FONT_DIST);
                app.cursorSpace.addGlyph(label);
                labelsDist.put(parid, label);
            } else {
                VText label = labelsDist.get(parid);
                if(label != null){
                    label.moveTo(x, y);
                    label.setText(text);
                }
            }
            
        }

        

        @Override
        public void update(Observable obs, Object obj){
            System.out.println("update Observable");
            System.out.println(obj);

            /*
            if( obs instanceof PythonWCS){
                if( ( (((PythonWCS)obs).json).toString()).equals( ((JSONObject)obj).toString() ) ){
                    System.out.println("--- EQUALS ---");
                }
            }
            */
            

            if( obj instanceof JSONObject){
                try{
                    JSONObject json = (JSONObject) obj;

                    String id = json.getString("id");

                    System.out.println(id + " == " + T_SMARTIES+"_"+this.id);

                    if(id.equals(T_SMARTIES+"_"+this.id)){

                        ecuatorial = json.getString("ecuatorial");
                        galactical = json.getString("galactic");
                        ra = json.getDouble("ra");
                        dec = json.getDouble("dec");
                        l = json.getDouble("l");
                        b = json.getDouble("b");
                        
                        updateLabel();
                        System.out.println("updateLabel()");

                        //addObserver();

                    }
                    
                } catch(JSONException e){
                    System.out.println(e);
                }
            }
            
        }

    } // class MyCursor

/*
    class PopupMenuButtonClicked implements SmartiesWidgetHandler
    {
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data)
        {
            System.out.println("popupMenuButtonClicked item: " + sw.item);
            double[] region = app.savedPositions.get(sw.item).getRegion();
            if(region != null && region.length == 4) app.mView.centerOnRegion(app.mCamera, Viewer.ANIM_MOVE_LENGTH, region[0], region[1], region[2], region[3], null);
            return true;
        }
    }
*/

    class EventGlobalView implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("GlobalView");
            app.getGlobalView(null);
            return true;
        }
    }
    class EventHigherView implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("HigherView");
            app.getHigherView();
            return true;
        }
    }
    class EventLowerView implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("LowerView");
            app.getLowerView();
            return true;
        }
    }
    class EventCenterCursor implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("CenterCursor");
            if(se.p != null){
                smarties.movePuck(se.p.id, 0.5f, 0.5f);
                MyCursor c = (MyCursor)se.p.app_data;
                c.move(se.p.x, se.p.y);
                c.updateWCS();
            }
            return true;
        }
    }

    class EventWCS implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventWCS");
            if(se.p != null){
                /*
                MyCursor c = (MyCursor)se.p.app_data;
                c.labelSetVisible(sw.on);
                */
                Map<Integer,SmartiesPuck> pucks = smarties.getPuckMapping();
                for (Map.Entry<Integer,SmartiesPuck> puck : pucks.entrySet() ) {
                    MyCursor c = (MyCursor)(puck.getValue().app_data);
                    c.labelSetVisible(sw.on);
                }
            } else {
                System.out.println("WCS without Puck");
                smarties.sendWidgetLabel(ui_swwcs, "WCS: Off", se.device);
                smarties.sendWidgetOnState(ui_swwcs, false, se.device);
            }
            return true;
        }
    }

    class EventCoordinateSystem implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventCoordinateSystem");
            if(se.p != null){

                /*
                MyCursor c = (MyCursor)se.p.app_data;
                c.setGalactical(sw.on);
                */
                Map<Integer,SmartiesPuck> pucks = smarties.getPuckMapping();
                for (Map.Entry<Integer,SmartiesPuck> puck : pucks.entrySet() ) {
                    MyCursor c = (MyCursor)(puck.getValue().app_data);
                    c.setGalactical(sw.on);
                }


            } else {
                System.out.println("Coordinate System without Puck");
                smarties.sendWidgetLabel(ui_swsystem, "Ecuatorial", se.device);
                smarties.sendWidgetOnState(ui_swsystem, false, se.device);
            }
            return true;
        }
    }

    class EventRescale implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventRescale");
            app.rescaleGlobal(!sw.on);
            
            return true;
        }
    }

    class EventDistance implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventDistance");
            if(sw.on){
                Map<Integer,SmartiesPuck> pucks = smarties.getPuckMapping();
                for (Map.Entry<Integer,SmartiesPuck> puck_a : pucks.entrySet() ) {
                    //System.out.println(puck_a.getKey() + " - " + puck_a.getValue());
                    for (Map.Entry<Integer,SmartiesPuck> puck_b : pucks.entrySet() ) {
                        //System.out.println(puck_b.getKey() + " - " + puck_b.getValue());
                        if(puck_a.getKey() != puck_b.getKey()){
                            System.out.println("distance");
                            System.out.println(puck_a.getKey() + " - " + puck_a.getValue().app_data);
                            System.out.println(puck_b.getKey() + " - " + puck_b.getValue().app_data);
                            MyCursor a = (MyCursor)(puck_a.getValue().app_data);
                            MyCursor b = (MyCursor)(puck_b.getValue().app_data);
                            a.distanceLine(b);
                            a.distanceLabel(b);
                            //(MyCursor)(puck_a.getValue().app_data).distance( (MyCursor)(puck_b.getValue().app_data) );
                            
                        }
                    }
                }
            } else {
                removeDistance();
                /*
                Map<Integer,SmartiesPuck> pucks = smarties.getPuckMapping();
                for (Map.Entry<Integer,SmartiesPuck> puck : pucks.entrySet() ) {
                    MyCursor c = (MyCursor)(puck.getValue().app_data);
                    c.removeDistance();
                }
                */
            }

            return true;
        }
    }

    class EventPreviousColor implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventPreviousColor");
            System.out.println(app);
            System.out.println(app.menu);
            app.menu.selectPreviousColorMapping();
            return true;
        }
    }

    class EventNextColor implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventNextColor");
            app.menu.selectNextColorMapping();
            return true;
        }
    }

    class EventPreviousScale implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventPreviousScale");
            app.menu.selectPreviousScale();
            return true;
        }
    }

    class EventNextScale implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("EventNextScale");
            app.menu.selectNextScale();
            return true;
        }
    }

    class EventPortal implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out .println("EventPortal");
            if(se.p != null){
                MyCursor c = (MyCursor)se.p.app_data;
                c.togglePortal();
            }
            return true;
        }
    }

    class EventQuery implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out .println("EventQuery");
            query = true;
            queryOn();
            return true;
        }
    }

/*
    class EventSavePosition implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("SavePosition");
            System.out.println("get string: " + se.text + " Canceled?" + sw.cancel);

            if(!sw.cancel){
                double[] rb = app.mView.getVisibleRegion(app.mCamera);
                System.out.print("getVisibleRegion [");
                for(double r: rb) System.out.print(r + ", ");
                System.out.println("]");

                SavedPosition newPosition = new SavedPosition(se.text, rb[0], rb[1], rb[2], rb[3]);
                app.appendNewSavedPosition(newPosition);

                smarties.addItemInWidgetList(ui_swspos, se.text, -1); 
                System.out.println("added new positions");
            }

            return true;
        }
    }
*/

}


