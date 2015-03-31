/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits.examples;

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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Cursor;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.awt.geom.Point2D;

import java.awt.BasicStroke;
import java.awt.Stroke;

//import edu.jhu.pha.sdss.fits.FITSImage;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;

import fr.inria.zvtm.fits.JSkyFitsHistogram;

import jsky.image.ImageLookup;
import jsky.image.ImageProcessor;

import fr.inria.zvtm.glyphs.JSkyFitsImage;



public class JSkyFitsMenu implements ViewListener, PickerListener {


    public static final int WIDTH_MENU = 200;

    private static final String SCALE_DEFAULT = "LINEAR SCALE";
    private static final String COLOR_DEFAULT = "Standard";

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

    public static final float WHEEL_FACTOR = 15.0f;

    public static final ColorGradient[] COLOR_GRADIANT = {new Aips0Filter(), new BackgrFilter(),
        new BlueFilter(), new BlulutFilter(), new ColorFilter(), new GreenFilter(),
        new HeatFilter(), new Idl11Filter(), new Idl12Filter(),new Idl14Filter(),new Idl15Filter(),
        new Idl2Filter(),new Idl4Filter(), new Idl5Filter(), new Idl6Filter(), new IsophotFilter(),new LightFilter(),
        new ManycolFilter(), new PastelFilter(), new RainbowFilter(), new Rainbow1Filter(),
        new Rainbow2Filter(),new Rainbow3Filter(), new Rainbow4Filter(), new RampFilter(),
        new RandomFilter(), new Random1Filter(), new Random2Filter(), new Random3Filter(),new Random4Filter(),
        /*new Random5Filter(), new Random6Filter(),*/ new RealFilter(), new RedFilter(), new SmoothFilter(),
        /*new Smooth1Filter(),new Smooth2Filter(),new Smooth3Filter(),*/ new StaircaseFilter(), new Stairs8Filter(), new Stairs9Filter(),
        new StandardFilter()};

    public static final String[] COLOR_NAME = {"Aips0", "Background", "Blue", "Blulut", "Color", "Green",
        "Heat", "Idl11", "Idl12","Idl14","Idl15",
        "Idl2","Idl4","Idl5","Idl6","Isophot","Light","Manycolor",
        "Pastel","Rainbow","Rainbow1","Rainbow2","Rainbow3","Rainbow4","Ramp","Random","Random1","Random2",
        "Random3","Random4",/*"Random5","Random6",*/"Real","Red","Smooth",/*"Smooth1","Smooth2","Smooth3",*/
        "Staircase", "Stairs8", "Stairs9", "Standard"};

    //public static final int[] SCALE_METHOD = {ImageLookup.LINEAR_SCALE, ImageLookup.LOG_SCALE, ImageLookup.HIST_EQ, ImageLookup.SQRT_SCALE};
    public static final JSkyFitsImage.ScaleAlgorithm[] SCALE_METHOD = {JSkyFitsImage.ScaleAlgorithm.LINEAR, JSkyFitsImage.ScaleAlgorithm.LOG, JSkyFitsImage.ScaleAlgorithm.HIST_EQ, JSkyFitsImage.ScaleAlgorithm.SQRT};
    public static final String[] SCALE_NAME = {"LINEAR SCALE", "LOGARITHMIC", "HISTOGRAM EQUALIZATION", "SQUARE ROOT"};

    public int BORDER_BOTTON_FILTER;
    public int BORDER_TOP_FILTER;

    public int BORDER_TOP_HISTOGRAM;
    public int BORDER_BOTTON_HISTOGRAM;
    public int BORDER_LEFT_HISTOGRAM;
    public int BORDER_RIGHT_HISTOGRAM;

    PRectangle scale_selected;
    PRectangle color_selected;

//  int CONST = 636;

    //public static final FitsImage.ScaleMethod[] SCALE_METHOD = {FitsImage.ScaleMethod.ASINH, FitsImage.ScaleMethod.HISTOGRAM_EQUALIZATION, FitsImage.ScaleMethod.LINEAR, FitsImage.ScaleMethod.LOG, FitsImage.ScaleMethod.SQUARE, FitsImage.ScaleMethod.SQUARE_ROOT};

    //private static Vector<Point2D.Double> ASINH;
    //private static double FACTOR_H_ASINH = (HEIGHT_BUTTON-2)/2/3;
    //private static double FACTOR_W_ASINH = (WIDTH_MENU - 2*BORDER)/4;

    JSkyFitsExample app;
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

    JSkyFitsMenu(JSkyFitsExample app){
        this.app = app;
        mnSpace = app.mnSpace;
        drawFiltersColor();
    }

    private void drawFiltersColor(){

        BORDER_TOP_FILTER =  0;//app.VIEW_H/2;

        int py = app.VIEW_H/2 - 2*HEIGHT_BTN - BORDER;

        for(int i = 0; i < COLOR_GRADIANT.length; i++){
            MultipleGradientPaint grad = Utils.makeGradient((RGBImageFilter)COLOR_GRADIANT[i]);
            PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, grad, BORDER_COLOR_BTN);
            filter.setType(T_FILTER);
            filter.setOwner(COLOR_NAME[i]);

            if(COLOR_DEFAULT.equals(COLOR_NAME[i])){
                color_selected = filter;
                color_selected.setWidth(color_selected.getWidth()+DISPLACE*2);
                color_selected.move(DISPLACE, 0);
            }

            mnSpace.addGlyph(filter);
            py -= (HEIGHT_BTN + BORDER);
        }

        for(int i = 0; i < SCALE_METHOD.length; i++){
            PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, BACKGROUND_COLOR_BTN, BORDER_COLOR_BTN);
            filter.setType(T_SCALE);
            filter.setOwner(SCALE_METHOD[i]);
            mnSpace.addGlyph(filter);

            VText scaleText = new VText(-app.VIEW_W/2 + WIDTH_MENU/2, py - DISPLACE_TEXT_BTN, Z_BTN, TEXT_COLOR_BTN, SCALE_NAME[i], VText.TEXT_ANCHOR_MIDDLE);
            scaleText.setFont(FONT_BTN);
            scaleText.setSensitivity(false);
            mnSpace.addGlyph(scaleText);
            filter.stick(scaleText);

            if(SCALE_DEFAULT.equals(SCALE_NAME[i])){
                scale_selected = filter;
                scale_selected.setWidth(scale_selected.getWidth()+DISPLACE*2);
                scale_selected.move(DISPLACE, 0);
            }

            //Glyph ln = drawMethod(SCALE_METHOD[i]);
            //ln.moveTo(-app.VIEW_W/2 + WIDTH_MENU/2 + BORDER , py);
            //mnSpace.addGlyph(ln);
            py -= (HEIGHT_BTN + BORDER);
        }
        BORDER_BOTTON_FILTER = COLOR_GRADIANT.length * ( HEIGHT_BTN + BORDER*2 ) + SCALE_METHOD.length * ( HEIGHT_BTN + BORDER*2 ) + HEIGHT_BTN + BORDER;
    }



    public void drawHistogram(){

        if(app.img != null){
            hist = JSkyFitsHistogram.fromFitsImage(app.img);
            hist.moveTo(-app.VIEW_W/2 + (app.VIEW_W - hist.getWidth())/2 , -app.VIEW_H/2 + 50);

            BORDER_TOP_HISTOGRAM = (int)(app.VIEW_H - hist.getHeight() - 65 );
            BORDER_BOTTON_HISTOGRAM = app.VIEW_H - 65;
            BORDER_LEFT_HISTOGRAM = (int)( (app.VIEW_W - hist.getWidth())/2 - JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;
            BORDER_RIGHT_HISTOGRAM = (int)( (app.VIEW_W + hist.getWidth())/2 + JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;

            VRectangle board = new VRectangle(hist.vx+hist.getWidth()/2, hist.vy+hist.getHeight()/2, Z_BTN, BORDER_RIGHT_HISTOGRAM-BORDER_LEFT_HISTOGRAM, BORDER_BOTTON_HISTOGRAM-BORDER_TOP_HISTOGRAM+10, Color.WHITE, Color.WHITE, 0.8f);

            mnSpace.addGlyph(board);
            mnSpace.addGlyph(hist);
        }
    }

    private void adaptCutLevels(double left, double right){
        double[] originCutLevels = app.img.getOriginCutLevels();
        double min = originCutLevels[0];
        double max = originCutLevels[1];

        left = (left < 0) ? 0 : left;
        right = (right > 1) ? 1 : right;

        app.img.setCutLevels(min + left*(max - min), min + right*(max - min));

    }

    private void drawShadow(double x1, double x2){
        if(shadow != null){
            double w = Math.abs(x2-x1);
            double x = (x1 > x2) ? x1 - w/2 : x1 + w/2;
            shadow.moveTo(x, shadow.vy);
            shadow.setWidth(w);
        }
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM){
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
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM){

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
        //System.out.println(app.menu.BORDER_BOTTON_HISTOGRAM + " > " + jpy + " > " + app.menu.BORDER_TOP_HISTOGRAM);
        //System.out.println(hist.vx + " " + hist.vy + " " + hist.getWidth() + " " + hist.getHeight());
        //System.out.println(BORDER_LEFT_HISTOGRAM + " < " + jpx + " < " + BORDER_RIGHT_HISTOGRAM);
        //System.out.println(jpx + " < " + app.menu.WIDTH_MENU + " " + app.menu.BORDER_BOTTON_FILTER + " < " + jpy + " < " + app.menu.BORDER_TOP_FILTER);
        if((jpx < app.menu.WIDTH_MENU && jpy < app.menu.BORDER_BOTTON_FILTER && jpy > app.menu.BORDER_TOP_FILTER) ||
          (jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM && jpx > app.menu.BORDER_LEFT_HISTOGRAM && jpx < app.menu.BORDER_RIGHT_HISTOGRAM )){
            if(v.parent.getActiveLayer() != app.LAYER_MENU){
                v.parent.setActiveLayer(app.LAYER_MENU);
                v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
            }
        } else {
            if(v.parent.getActiveLayer() != app.LAYER_FITS){
                v.parent.setActiveLayer(app.LAYER_FITS);
                v.parent.setCursorIcon(Cursor.CUSTOM_CURSOR);
            }
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

                Point2D.Double point = app.viewToSpace(app.mnCamera, jpx, jpy);
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

    Glyph lastGlyph = null;

    public void enterGlyph(Glyph g){
        //System.out.println("enter: " + g.getType());
        if(g.getType().equals(T_FILTER)){

            if(color_selected != ((PRectangle)g)){
                app.img.setColorLookupTable((String)g.getOwner());
                if(color_selected != null){
                    color_selected.setWidth(color_selected.getWidth()-DISPLACE*2);
                    color_selected.move(-DISPLACE,0);
                }
                ((PRectangle)g).setWidth(((PRectangle)g).getWidth()+DISPLACE*2);
                g.move(DISPLACE, 0);
                color_selected = (PRectangle)g;
                //redrawHistogram();
            }
        } else if(g.getType().equals(T_SCALE)){
            lastGlyph = g;
            if(scale_selected != (PRectangle)g){
                app.img.setScaleAlgorithm((JSkyFitsImage.ScaleAlgorithm)g.getOwner());
                if(scale_selected != null){
                    scale_selected.setWidth(scale_selected.getWidth()-DISPLACE*2);
                    scale_selected.move(-DISPLACE,0);
                }
                ((PRectangle)g).setWidth(((PRectangle)g).getWidth()+DISPLACE*2);
                g.move(DISPLACE,0);
                scale_selected = (PRectangle)g;
                //redrawHistogram();
            }
        } else if(g.getType().equals(T_SCROLL) && !scroll){
            scroll = true;
            //v.parent.setCursorIcon(Cursor.);
        }
        lastGlyph = g;
    }

    public void exitGlyph(Glyph g){
        //System.out.println("exit: " + g.getType());
        if(g.getType().equals(T_SCROLL) && scroll){
            scroll = false;
            //v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}


}