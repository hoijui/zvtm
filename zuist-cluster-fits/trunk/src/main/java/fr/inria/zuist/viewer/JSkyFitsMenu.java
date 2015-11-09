

package fr.inria.zuist.viewer;

//draw
import java.awt.MultipleGradientPaint;
import java.awt.Color;

//import fr.inria.zvtm.fits.filters.HeatFilter;
//import fr.inria.zvtm.fits.filters.NopFilter;
import fr.inria.zvtm.fits.filters.*;
import fr.inria.zvtm.fits.Utils;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.PRectangle;


//event
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.PickerListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.Composite;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Cursor;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.awt.image.DataBuffer;
import java.awt.geom.Point2D;

import java.awt.BasicStroke;
import java.awt.Stroke;

//import edu.jhu.pha.sdss.fits.FITSImage;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;

import jsky.image.ImageLookup;
import jsky.image.ImageProcessor;

import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zvtm.glyphs.JSkyFitsHistogram;
import jsky.image.fits.codec.FITSImage;

import javax.media.jai.Histogram;


public class JSkyFitsMenu implements ViewListener, PickerListener {

    public static final int WIDTH_MENU = 200;

    private static final String SCALE_DEFAULT = "LINEAR SCALE";
    private static final String COLOR_DEFAULT = "Heat";

    public static final int HEIGHT_BTN = 15;
    public static final Color BORDER_COLOR_BTN = Color.BLACK;
    public static final Color TEXT_COLOR_BTN = Color.BLACK;
    public static final Color BACKGROUND_COLOR_BTN = Color.LIGHT_GRAY;
    public static final int FONT_SIZE_BTN = 10;
    public static final Font FONT_BTN = new Font("Bold", Font.PLAIN, FONT_SIZE_BTN);
    //public static int PIXELS_FONT = 16;//(int)(FONT_BTN.getSize()*1.333997172);
    public static int DISPLACE_TEXT_BTN = 4;

    //public static final Color LINE_COLOR = Color.RED;
    //public static final Stroke LINE_STROKE = new BasicStroke(1f);

    private static final int BORDER = 2;
    private static final int DISPLACE = 5;

    public static final int Z_BTN = 0;

    public static final String T_FILTER = "Fltr";
    public static final String T_SCALE = "Scl";
    public static final String T_SCROLL = "Scrll";
    public static final String T_SCROLL_BAR = "ScrllBr";
    public static final String T_ASTRO_OBJ = "AO";

    public static final float WHEEL_FACTOR = 15.0f;

    static LinkedHashMap<String,RGBImageFilter> COLOR_MAPPINGS = new LinkedHashMap();
    static {
        COLOR_MAPPINGS.put("Ramp", new RampFilter());
        COLOR_MAPPINGS.put("Heat",  new HeatFilter());
        COLOR_MAPPINGS.put("Blulut", new BlulutFilter());
        COLOR_MAPPINGS.put("Idl12", new Idl12Filter());
        COLOR_MAPPINGS.put("Rainbow1", new Rainbow1Filter());
        COLOR_MAPPINGS.put("Standard", new StandardFilter());
        COLOR_MAPPINGS.put("Idl4", new Idl4Filter());
        COLOR_MAPPINGS.put("Stairs8", new Stairs8Filter());
        COLOR_MAPPINGS.put("Red", new RedFilter());
        COLOR_MAPPINGS.put("Green", new GreenFilter());
        COLOR_MAPPINGS.put("Blue", new BlueFilter());
        /*
        COLOR_MAPPINGS.put("Aips0", new Aips0Filter());
        COLOR_MAPPINGS.put("Background", new BackgrFilter());
        COLOR_MAPPINGS.put("Blue", new BlueFilter());
        COLOR_MAPPINGS.put("Blulut", new BlulutFilter());
        COLOR_MAPPINGS.put("Color", new ColorFilter());
        COLOR_MAPPINGS.put("Green", new GreenFilter());
        COLOR_MAPPINGS.put("Heat",  new HeatFilter());
        COLOR_MAPPINGS.put("Idl11", new Idl11Filter());
        COLOR_MAPPINGS.put("Idl12", new Idl12Filter());
        COLOR_MAPPINGS.put("Idl14", new Idl14Filter());
        COLOR_MAPPINGS.put("Idl15", new Idl15Filter());
        COLOR_MAPPINGS.put("Idl2", new Idl2Filter());
        COLOR_MAPPINGS.put("Idl4", new Idl4Filter());
        COLOR_MAPPINGS.put("Idl5", new Idl5Filter());
        COLOR_MAPPINGS.put("Idl6", new Idl6Filter());
        COLOR_MAPPINGS.put("Isophot", new IsophotFilter());
        COLOR_MAPPINGS.put("Light", new LightFilter());
        COLOR_MAPPINGS.put("Manycolor", new ManycolFilter());
        COLOR_MAPPINGS.put("Pastel", new PastelFilter());
        COLOR_MAPPINGS.put("Rainbow", new RainbowFilter());
        COLOR_MAPPINGS.put("Rainbow1", new Rainbow1Filter());
        COLOR_MAPPINGS.put("Rainbow2", new Rainbow2Filter());
        COLOR_MAPPINGS.put("Rainbow3", new Rainbow3Filter());
        COLOR_MAPPINGS.put("Rainbow4", new Rainbow4Filter());
        COLOR_MAPPINGS.put("Ramp", new RampFilter());
        COLOR_MAPPINGS.put("Random", new RandomFilter());
        COLOR_MAPPINGS.put("Random1", new Random1Filter());
        COLOR_MAPPINGS.put("Random2", new Random2Filter());
        COLOR_MAPPINGS.put("Random3", new Random3Filter());
        COLOR_MAPPINGS.put("Random4", new Random4Filter());
        //COLOR_MAPPINGS.put("Random5", new Random5Filter());
        //COLOR_MAPPINGS.put("Random6", new Random6Filter());
        COLOR_MAPPINGS.put("Real", new RealFilter());
        COLOR_MAPPINGS.put("Red", new RedFilter());
        COLOR_MAPPINGS.put("Smooth", new SmoothFilter());
        //COLOR_MAPPINGS.put("Smooth1", new Smooth1Filter());
        //COLOR_MAPPINGS.put("Smooth2", new Smooth2Filter());
        //COLOR_MAPPINGS.put("Smooth3", new Smooth3Filter());
        COLOR_MAPPINGS.put("Staircase", new StaircaseFilter());
        COLOR_MAPPINGS.put("Stairs8", new Stairs8Filter());
        COLOR_MAPPINGS.put("Stairs9", new Stairs9Filter());
        COLOR_MAPPINGS.put("Standard", new StandardFilter());
        */
    }

    static HashMap<String,PRectangle> COLOR_MAPPING2GLYPH = new HashMap(COLOR_MAPPINGS.size(),1);

    static final Vector<String> ORDERED_COLOR_MAPPINGS = new Vector(COLOR_MAPPINGS.keySet());

    static LinkedHashMap<String,JSkyFitsImage.ScaleAlgorithm> SCALES = new LinkedHashMap(4,1);
    static {
        SCALES.put("LINEAR SCALE", JSkyFitsImage.ScaleAlgorithm.LINEAR);
        SCALES.put("LOGARITHMIC", JSkyFitsImage.ScaleAlgorithm.LOG);
        SCALES.put("HISTOGRAM EQUALIZATION", JSkyFitsImage.ScaleAlgorithm.HIST_EQ);
        SCALES.put("SQUARE ROOT", JSkyFitsImage.ScaleAlgorithm.SQRT);
    };

    static HashMap<String,PRectangle> SCALE2GLYPH = new HashMap(SCALES.size(),1);

    static final Vector<String> ORDERED_SCALES = new Vector(SCALES.keySet());

    // ********************************************
    public int BORDER_BOTTOM_FILTER;
    public int BORDER_TOP_FILTER;

    public int BORDER_TOP_HISTOGRAM;
    public int BORDER_BOTTOM_HISTOGRAM;
    public int BORDER_LEFT_HISTOGRAM;
    public int BORDER_RIGHT_HISTOGRAM;

    PRectangle selected_scaleG;
    PRectangle selected_colorG;

//  int CONST = 636;

    //public static final FitsImage.ScaleMethod[] SCALE_METHOD = {FitsImage.ScaleMethod.ASINH, FitsImage.ScaleMethod.HISTOGRAM_EQUALIZATION, FitsImage.ScaleMethod.LINEAR, FitsImage.ScaleMethod.LOG, FitsImage.ScaleMethod.SQUARE, FitsImage.ScaleMethod.SQUARE_ROOT};

    //private static Vector<Point2D.Double> ASINH;
    //private static double FACTOR_H_ASINH = (HEIGHT_BUTTON-2)/2/3;
    //private static double FACTOR_W_ASINH = (WIDTH_MENU - 2*BORDER)/4;

    JSkyFitsViewer app;
    VirtualSpace mnSpace;

    JSkyFitsHistogram hist;

    int lastJPX;

    VRectangle shadow;
    double shadow_vx;
    double shadow_vy;
    //int shadow_width;

    boolean scroll = false;
    boolean press3_scroll = false;
    boolean press1 = false;

    JSkyFitsMenu(JSkyFitsViewer app){
        this.app = app;
        mnSpace = app.mnSpace;
        buildColorMappingMenu();
        //buildHistogram();
    }

    private void buildColorMappingMenu(){

        //BORDER_TOP_FILTER =  0;//app.VIEW_H/2;
        //int py = app.VIEW_H/2 - 2*HEIGHT_BTN - BORDER;

        BORDER_TOP_FILTER =  50 - (HEIGHT_BTN + 2*BORDER);//app.VIEW_H/2;
        int py = app.VIEW_H/2 - (HEIGHT_BTN + 2*BORDER) - 50;

        for(String cm:COLOR_MAPPINGS.keySet()){
            RGBImageFilter f = COLOR_MAPPINGS.get(cm);
            MultipleGradientPaint grad = Utils.makeGradient(f);
            PRectangle filterG = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, grad, BORDER_COLOR_BTN);
            filterG.setType(T_FILTER);
            filterG.setOwner(cm);
            COLOR_MAPPING2GLYPH.put(cm, filterG);
            if(COLOR_DEFAULT.equals(cm)){
                selected_colorG = filterG;
                selected_colorG.setWidth(selected_colorG.getWidth()+DISPLACE*2);
                selected_colorG.move(DISPLACE, 0);
            }
            mnSpace.addGlyph(filterG);
            py -= (HEIGHT_BTN + 2*BORDER);
        }

        for(String sc:SCALES.keySet()){
            PRectangle scaleG = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, BACKGROUND_COLOR_BTN, BORDER_COLOR_BTN);
            scaleG.setType(T_SCALE);
            scaleG.setOwner(sc);
            mnSpace.addGlyph(scaleG);
            VText scaleLb = new VText(-app.VIEW_W/2 + WIDTH_MENU/2, py - DISPLACE_TEXT_BTN, Z_BTN, TEXT_COLOR_BTN, sc, VText.TEXT_ANCHOR_MIDDLE);
            scaleLb.setFont(FONT_BTN);
            scaleLb.setSensitivity(false);
            mnSpace.addGlyph(scaleLb);
            scaleG.stick(scaleLb);
            SCALE2GLYPH.put(sc, scaleG);
            if(SCALE_DEFAULT.equals(sc)){
                selected_scaleG = scaleG;
                selected_scaleG.setWidth(selected_scaleG.getWidth()+DISPLACE*2);
                selected_scaleG.move(DISPLACE, 0);
            }
            //Glyph ln = drawMethod(sc);
            //ln.moveTo(-app.VIEW_W/2 + WIDTH_MENU/2 + BORDER , py);
            //mnSpace.addGlyph(ln);
            py -= (HEIGHT_BTN + 2*BORDER);
        }
        //BORDER_BOTTOM_FILTER = COLOR_MAPPINGS.size() * ( HEIGHT_BTN + BORDER*2 ) + SCALES.size() * ( HEIGHT_BTN + BORDER*2 ) + HEIGHT_BTN + BORDER;
        BORDER_BOTTOM_FILTER = app.VIEW_H/2 - py - (HEIGHT_BTN + 2*BORDER);
    }

    public void buildHistogram(){

//        if(app.img != null){
//            hist = JSkyFitsHistogram.fromFitsImage(app.img);
		if(app.fitsImageDescRef != null && app.fitsImageDescRef.getGlyph() != null ){// && ((JSkyFitsImage)(app.fitsImageDescRef.getGlyph())).getRawFITSImage() != null){
			hist = JSkyFitsHistogram.fromFitsImage( (JSkyFitsImage)(app.fitsImageDescRef.getGlyph()) );

            hist.moveTo(-app.VIEW_W/2 + (app.VIEW_W - hist.getWidth())/2 , -app.VIEW_H/2 + 50);

            BORDER_TOP_HISTOGRAM = (int)(app.VIEW_H - hist.getHeight() - 65 );
            BORDER_BOTTOM_HISTOGRAM = app.VIEW_H - 65;
            BORDER_LEFT_HISTOGRAM = (int)( (app.VIEW_W - hist.getWidth())/2 - JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;
            BORDER_RIGHT_HISTOGRAM = (int)( (app.VIEW_W + hist.getWidth())/2 + JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;

            VRectangle board = new VRectangle(hist.vx+hist.getWidth()/2, hist.vy+hist.getHeight()/2, Z_BTN, BORDER_RIGHT_HISTOGRAM-BORDER_LEFT_HISTOGRAM, BORDER_BOTTOM_HISTOGRAM-BORDER_TOP_HISTOGRAM+10, Color.WHITE, Color.WHITE, 0.8f);

            mnSpace.addGlyph(board);
            mnSpace.addGlyph(hist);
        }
    }

    public void buildHistogram(JSkyFitsHistogram hist){

//        if(app.img != null){
//            hist = JSkyFitsHistogram.fromFitsImage(app.img);
        if(hist != null){

            hist.moveTo(-app.VIEW_W/2 + (app.VIEW_W - hist.getWidth())/2 , -app.VIEW_H/2 + 50);

            BORDER_TOP_HISTOGRAM = (int)(app.VIEW_H - hist.getHeight() - 65 );
            BORDER_BOTTOM_HISTOGRAM = app.VIEW_H - 65;
            BORDER_LEFT_HISTOGRAM = (int)( (app.VIEW_W - hist.getWidth())/2 - JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;
            BORDER_RIGHT_HISTOGRAM = (int)( (app.VIEW_W + hist.getWidth())/2 + JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;

            VRectangle board = new VRectangle(hist.vx+hist.getWidth()/2, hist.vy+hist.getHeight()/2, Z_BTN, BORDER_RIGHT_HISTOGRAM-BORDER_LEFT_HISTOGRAM, BORDER_BOTTOM_HISTOGRAM-BORDER_TOP_HISTOGRAM+10, Color.WHITE, Color.WHITE, 0.8f);

            if(this.hist == null){
                mnSpace.addGlyph(board);
                mnSpace.addGlyph(hist);
                this.hist = hist;
            } else {
                mnSpace.removeGlyph(this.hist);
                mnSpace.addGlyph(hist);
                this.hist = hist;
            }
            
        }
    }

    private void adaptCutLevels(double left, double right){
        //double[] originCutLevels = app.img.getOriginCutLevels();
        double[] originCutLevels = app.getGlobalScale();
        double min = originCutLevels[0];
        double max = originCutLevels[1];

        left = (left < 0) ? 0 : left;
        right = (right > 1) ? 1 : right;

        //app.img.setCutLevels(min + left*(max - min), min + right*(max - min));
        app.rescale(min + left*(max - min), min + right*(max - min));

    }

    private void drawShadow(double x1, double x2){
        if(shadow != null){
            double w = Math.abs(x2-x1);
            double x = (x1 > x2) ? x1 - w/2 : x1 + w/2;
            shadow.moveTo(x, shadow.vy);
            shadow.setWidth(w);
        }
    }

    boolean cursorInside(int jpx, int jpy){
        return (jpx < WIDTH_MENU && jpy < BORDER_BOTTOM_FILTER && jpy > BORDER_TOP_FILTER) ||
               (jpy > BORDER_TOP_HISTOGRAM && jpy < BORDER_BOTTOM_HISTOGRAM && jpx > BORDER_LEFT_HISTOGRAM && jpx < BORDER_RIGHT_HISTOGRAM);
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTOM_HISTOGRAM){
            lastJPX = jpx;
            if(shadow != null)
                app.mnSpace.removeGlyph(shadow);

            press1 = true;

            //Point2D.Double point = app.viewToSpace(app.mnCamera, jpx, jpy);

            shadow = new VRectangle( ((lastJPX < jpx)? lastJPX : jpx) + Math.abs(lastJPX - jpx)/2 - app.VIEW_W/2, -app.VIEW_H/2 + 100, Z_BTN, Math.abs(lastJPX - jpx), hist.getHeight() + 10, Color.WHITE, Color.BLACK, .2f);
            shadow.setType(T_SCROLL);
            app.mnSpace.addGlyph(shadow);
            shadow_vx = shadow.vx;
            shadow_vy = shadow.vy;
            /*
            VRectangle[] bars = hist.getBars();
            for(VRectangle b : bars){
                b.setColor(FitsHistogram.SELECTED_FILL_COLOR);
            }
            */
        }
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTOM_HISTOGRAM){

            double left = ( ((lastJPX < jpx)? lastJPX : jpx) - app.VIEW_W/2 + hist.getWidth()/2) / hist.getWidth();
            double right = ( ((lastJPX < jpx)? jpx : lastJPX) - app.VIEW_W/2 + hist.getWidth()/2) / hist.getWidth();

            adaptCutLevels(left, right);

            lastJPX = jpx;
            press1 = false;
        }

    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        if(clickNumber == 2){
            adaptCutLevels(0, 1);
            if(shadow != null){
                app.mnSpace.removeGlyph(shadow);
                shadow = null;
            }
            /*
            VRectangle[] bars = hist.getBars();
            for(VRectangle b : bars){
                b.setColor(FitsHistogram.DEFAULT_FILL_COLOR);
            }
            */
        }
    }


    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        if(scroll){
            press3_scroll = true;
            lastJPX = jpx;
        }
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        if(shadow != null && press3_scroll){
            press3_scroll = false;
            lastJPX = 0;

            /*
            double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
            double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
            adaptCutLevels(left, right);
            */
        }

    }
    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if (!cursorInside(jpx, jpy)){
            v.parent.setActiveLayer(app.LAYER_SCENE);
            v.parent.setCursorIcon(Cursor.CUSTOM_CURSOR);
        }
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if(press3_scroll){
            if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2 + (jpx - lastJPX)) > app.menu.BORDER_LEFT_HISTOGRAM &&
                (shadow.vx+shadow.vw/2 + app.VIEW_W/2 + (jpx - lastJPX)) < app.menu.BORDER_RIGHT_HISTOGRAM){

                shadow.move(jpx - lastJPX,0);
                lastJPX = jpx;
                double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
                double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
                adaptCutLevels(left, right);

            } else if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2) > app.menu.BORDER_LEFT_HISTOGRAM &&
                (shadow.vx+shadow.vw/2 + app.VIEW_W/2) < app.menu.BORDER_RIGHT_HISTOGRAM){
                if( (jpx - lastJPX) > 0 ){
                    shadow.move( app.menu.BORDER_RIGHT_HISTOGRAM - (shadow.vx+shadow.vw/2 + app.VIEW_W/2) ,0);
                    lastJPX = jpx;
                    double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
                    double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
                    adaptCutLevels(left, right);

                } else if( (jpx - lastJPX) < 0 ){
                    shadow.move( app.menu.BORDER_LEFT_HISTOGRAM - (shadow.vx-shadow.vw/2 + app.VIEW_W/2) ,0);
                    lastJPX = jpx;
                    double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
                    double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
                    adaptCutLevels(left, right);
                }
            }
        }
        if(press1){
            if(shadow != null){
                if(jpx > BORDER_RIGHT_HISTOGRAM) jpx = BORDER_RIGHT_HISTOGRAM;
                if(jpx < BORDER_LEFT_HISTOGRAM) jpx = BORDER_LEFT_HISTOGRAM;
                Point2D.Double point = app.mView.fromPanelToVSCoordinates(jpx, jpy, app.mnCamera, new Point2D.Double());
                drawShadow(shadow_vx, point.getX());
                //scroll = false;
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){

        if(shadow != null){

            double mvx = v.getVCursor().getVSXCoordinate();
            double mvy = v.getVCursor().getVSYCoordinate();

            if(wheelDirection == WHEEL_UP){
                if((shadow.vx+shadow.vw/2 + app.VIEW_W/2 + WHEEL_FACTOR) < BORDER_RIGHT_HISTOGRAM ) shadow.move( WHEEL_FACTOR, 0 );
                else if( (shadow.vx+shadow.vw/2 + app.VIEW_W/2) < BORDER_RIGHT_HISTOGRAM){
                    shadow.move( BORDER_RIGHT_HISTOGRAM - (shadow.vx+shadow.vw/2 + app.VIEW_W/2), 0 );
                }
            } else {
                if((shadow.vx-shadow.vw/2 + app.VIEW_W/2 - WHEEL_FACTOR) > BORDER_LEFT_HISTOGRAM ) shadow.move( -WHEEL_FACTOR, 0 );
                else if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2) > BORDER_LEFT_HISTOGRAM){
                    shadow.move( BORDER_LEFT_HISTOGRAM - (shadow.vx-shadow.vw/2 + app.VIEW_W/2) , 0 );
                }
            }

            double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
            double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
            adaptCutLevels(left, right);
        }

    }

    public void enterGlyph(Glyph g){
        if(g.getType().equals(T_FILTER)){
            if (selected_colorG != g){
                selectColorMapping((PRectangle)g);
                //rebuildHistogram();
            }
        } else if(g.getType().equals(T_SCALE)){
            if(selected_scaleG != (PRectangle)g){
                selectScale((PRectangle)g);
                //rebuildHistogram();
                System.out.println("hist : " + hist);
            }
        } else if(g.getType().equals(T_SCROLL) && !scroll){
            scroll = true;
            //v.parent.setCursorIcon(Cursor.);
        }
    }

    public void exitGlyph(Glyph g){
        //System.out.println("exit: " + g.getType());
        if(g.getType().equals(T_SCROLL) && scroll){
            scroll = false;
            //v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code == KeyEvent.VK_M){
            app.toggleMenu();
        }
        else if (code == KeyEvent.VK_F1){
            selectPreviousColorMapping();
        }
        else if (code == KeyEvent.VK_F2){
            selectNextColorMapping();
        }
        else if (code == KeyEvent.VK_F3){
            selectPreviousScale();
        }
        else if (code == KeyEvent.VK_F4){
            selectNextScale();
        }
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    void selectColorMapping(PRectangle cm){
        System.out.println("selectColorMapping()");
        //app.img.setColorLookupTable((String)cm.getOwner());
        app.setColorLookupTable((String)cm.getOwner());
        if(selected_colorG != null){
            selected_colorG.setWidth(selected_colorG.getWidth()-DISPLACE*2);
            selected_colorG.move(-DISPLACE,0);
        }
        cm.setWidth(cm.getWidth()+DISPLACE*2);
        cm.move(DISPLACE, 0);
        selected_colorG = cm;
    }

    public void selectNextColorMapping(){
        int scmIndex = ORDERED_COLOR_MAPPINGS.indexOf(selected_colorG.getOwner());
        int newIndex = 0;
        if (scmIndex != -1){
            if (scmIndex == ORDERED_COLOR_MAPPINGS.size()-1){
                newIndex = 0;
            }
            else {
                newIndex = scmIndex + 1;
            }
        }
        selectColorMapping(COLOR_MAPPING2GLYPH.get(ORDERED_COLOR_MAPPINGS.elementAt(newIndex)));
    }

    public void selectPreviousColorMapping(){
        int scmIndex = ORDERED_COLOR_MAPPINGS.indexOf(selected_colorG.getOwner());
        int newIndex = 0;
        if (scmIndex != -1){
            if (scmIndex == 0){
                newIndex = ORDERED_COLOR_MAPPINGS.size()-1;
            }
            else {
                newIndex = scmIndex - 1;
            }
        }
        selectColorMapping(COLOR_MAPPING2GLYPH.get(ORDERED_COLOR_MAPPINGS.elementAt(newIndex)));
    }

    void selectScale(PRectangle sc){
        //app.img.setScaleAlgorithm(SCALES.get(sc.getOwner()));
        app.setScaleAlgorithm(SCALES.get(sc.getOwner()));
        if(selected_scaleG != null){
            selected_scaleG.setWidth(selected_scaleG.getWidth()-DISPLACE*2);
            selected_scaleG.move(-DISPLACE,0);
        }
        sc.setWidth(sc.getWidth()+DISPLACE*2);
        sc.move(DISPLACE,0);
        selected_scaleG = sc;
    }

    public void selectNextScale(){
        int scIndex = ORDERED_SCALES.indexOf(selected_scaleG.getOwner());
        int newIndex = 0;
        if (scIndex != -1){
            if (scIndex == ORDERED_SCALES.size()-1){
                newIndex = 0;
            }
            else {
                newIndex = scIndex + 1;
            }
        }
        selectScale(SCALE2GLYPH.get(ORDERED_SCALES.elementAt(newIndex)));
    }

    public void selectPreviousScale(){
        int scIndex = ORDERED_SCALES.indexOf(selected_scaleG.getOwner());
        int newIndex = 0;
        if (scIndex != -1){
            if (scIndex == 0){
                newIndex = ORDERED_SCALES.size()-1;
            }
            else {
                newIndex = scIndex - 1;
            }
        }
        selectScale(SCALE2GLYPH.get(ORDERED_SCALES.elementAt(newIndex)));
    }

}




