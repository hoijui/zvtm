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

import fr.inria.zvtm.glyphs.VText;
import java.awt.Font;

import fr.inria.zuist.viewer.FitsViewer;

public class SmartiesManager implements Observer {

    FitsViewer application;

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


    public SmartiesManager(FitsViewer app){


        this.application = app;
        smarties = new Smarties((int)application.SCENE_W , (int)application.SCENE_H,
                                8, 4);
        smarties.initWidgets(6,2);

        countWidget = 0;


        SmartiesWidget sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Global View", 2, 2, 2, 1);
        sw.handler = new EventGlobalView();
        countWidget++;
        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_BUTTON, "Center Cursor", 4, 2, 2, 1);
        sw.handler = new EventCenterCursor();
        countWidget++;

        swWCS = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "WCS: Off", 5, 1, 2, 1);
        swWCS.labelOn = new String("WCS: On");
        swWCS.handler = new EventWCS();
        ui_swwcs = countWidget;
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "Global", 3, 1, 1, 1);
        sw.labelOn = new String("Local");
        sw.handler = new EventRescale();
        ui_swrescale = countWidget;
        countWidget++;

        sw = smarties.addWidget(
                                SmartiesWidget.SMARTIES_WIDGET_TYPE_TOGGLE_BUTTON, "Ecuatorial", 4, 1, 1, 1);
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

        smarties.addObserver(this);

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
                            application.centeredZoom(prevMFPinchD/se.d,
                                                        x*(float)application.SCENE_W,
                                                        y*(float)application.SCENE_H);
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
                        float dx = (se.x - prevMFMoveX)*(float)application.getDisplayWidth();
                        float dy = (se.y - prevMFMoveY)*(float)application.getDisplayHeight();
                        application.directTranslate(-dx, dy);
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
                        }

                    }
                    break;
                }
                case SmartiesEvent.SMARTIE_EVENTS_TYPE_MOVE:{
                    System.out.println("SMARTIE_EVENTS_TYPE_MOVE");
                    if (se.p != null){
                        MyCursor c = (MyCursor)se.p.app_data;
                        c.move(se.p.x, se.p.y);

                        if (se.mode == SmartiesEvent.SMARTIE_GESTUREMOD_DRAG && dragDevice == se.device ){//&& !modeDrawRect){
                            // this is the device that lock the drag, e.p should be == dragPuck
                            float dx = (se.x - prevMFMoveX)*(float)application.getDisplayWidth();
                            float dy = (se.y - prevMFMoveY)*(float)application.getDisplayHeight();
                            application.directTranslate(-dx, dy);
                            prevMFMoveX = se.x;
                            prevMFMoveY = se.y;
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
                        se.p.app_data = c; // CHECK
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
                        application.traslateAnimated(x*application.getDisplayWidth(), y*application.getDisplayHeight(), null);
                        EndAction ea  = new EndAction(){
                            public void execute(Object subject, Animation.Dimension dimension){
                                c.move(se.p.x, se.p.y);
                                c.wc.setVisible(true);
                            }
                        };
                        double f = -(application.mCamera.getAltitude() + application.mCamera.getFocal())/1.3f;
                        application.zoomAnimated(f, ea);
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


    public class MyCursor implements Observer {

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


        Font FONT = new Font("default", Font.PLAIN, 16);
        
        public MyCursor(int id, double x, double y){
            this.id = id;
            this.x = x;
            this.y = y;
            this.color = SmartiesColors.getPuckColorById(id);

            wc = new WallCursor(
                application.cursorSpace,
                (true) ? 10 : 2, (true) ? 100 : 20,
                this.color);
            labelsnd = new VText(0.0, 0.0, 0, color, Color.BLACK, "", VText.TEXT_ANCHOR_START, 1f, 1f);
            labelsnd.setFont(FONT);

            labelsnd.setVisible(isLabelVisible);
            application.cursorSpace.addGlyph(labelsnd);
            application.cursorSpace.onTop(labelsnd);

            labelfst = new VText(0.0, 0.0, 0, color, Color.BLACK, "", VText.TEXT_ANCHOR_START, 1f, 1f);
            labelfst.setFont(FONT);

            labelfst.setVisible(isLabelVisible);
            application.cursorSpace.addGlyph(labelfst);
            application.cursorSpace.onTop(labelfst);

            application.pythonWCS.addObserver(this);

            move(x, y);

        }

        public void dispose(){
            wc.dispose();
            application.pythonWCS.deleteObserver(this);
        }

        public void setVisible(boolean b){
            wc.setVisible(b);
            if(isLabelVisible){
                labelSetVisible(b);
            }
        }

        public void move(double x, double y){
            this.x = x; this.y = y;
            //wc.moveTo((long)(x*application.getDisplayWidth() - application.getDisplayWidth()/2.0), (long)(application.getDisplayHeight()/2.0 - y*application.getDisplayHeight()));
            wc.moveTo((long)(x*application.SCENE_W - application.SCENE_W/2.0), (long)(application.SCENE_H/2.0 - y*application.SCENE_H));
            labelsnd.moveTo((long)(x*application.SCENE_W - application.SCENE_W/2.0+50), (long)(application.SCENE_H/2.0 - y*application.SCENE_H+50));
            labelfst.moveTo((long)(x*application.SCENE_W - application.SCENE_W/2.0+50), (long)(application.SCENE_H/2.0 - y*application.SCENE_H+70));
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
            System.out.println("updateWCS()");
            Point2D.Double pWCS = new Point2D.Double(wc.getX(), wc.getY());
            System.out.println("WallCursor: ");
            System.out.println(pWCS);
            application.coordinateWCS(pWCS, T_SMARTIES+"_"+id);
            /*
            if(isLabelVisible){
                application.coordinateWCS(pWCS);
                //application.coordinateWCS(pWCS, this);
                //updateLabel(application.getRaDec(), application.getGalactic());//+" - Object: "+application.getObjectName(pWCS));
            }
            */
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

        public void distance(MyCursor c){
            //Draw line
            //Draw distance
            if(isGalactical){
                double dl = l - c.l;
                double db = b - c.b;
                System.out.println(dl + " -- " + db);
            } else {
                double dra = ra - c.ra;
                double ddec = dec - c.dec;
                System.out.println(dra + " -- " + ddec);
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
            double[] region = application.savedPositions.get(sw.item).getRegion();
            if(region != null && region.length == 4) application.mView.centerOnRegion(application.mCamera, Viewer.ANIM_MOVE_LENGTH, region[0], region[1], region[2], region[3], null);
            return true;
        }
    }
*/

    class EventGlobalView implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("GlobalView");
            application.getGlobalView(null);
            return true;
        }
    }
    class EventHigherView implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("HigherView");
            application.getHigherView();
            return true;
        }
    }
    class EventLowerView implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("LowerView");
            application.getLowerView();
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
            application.rescaleGlobal(!sw.on);
            
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
                            a.distance(b);
                            //(MyCursor)(puck_a.getValue().app_data).distance( (MyCursor)(puck_b.getValue().app_data) );
                            
                        }
                    }
                }
            } 

            return true;
        }
    }

/*
    class EventSavePosition implements SmartiesWidgetHandler{
        public boolean callback(SmartiesWidget sw, SmartiesEvent se, Object user_data){
            System.out.println("SavePosition");
            System.out.println("get string: " + se.text + " Canceled?" + sw.cancel);

            if(!sw.cancel){
                double[] rb = application.mView.getVisibleRegion(application.mCamera);
                System.out.print("getVisibleRegion [");
                for(double r: rb) System.out.print(r + ", ");
                System.out.println("]");

                SavedPosition newPosition = new SavedPosition(se.text, rb[0], rb[1], rb[2], rb[3]);
                application.appendNewSavedPosition(newPosition);

                smarties.addItemInWidgetList(ui_swspos, se.text, -1); 
                System.out.println("added new positions");
            }

            return true;
        }
    }
*/

}


