

package fr.inria.zuist.viewer;

//draw
import java.awt.MultipleGradientPaint;
import java.awt.Color;

import fr.inria.zvtm.fits.filters.*;
import fr.inria.zvtm.fits.Utils;

import fr.inria.zvtm.glyphs.FitsImage;

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
import java.awt.Cursor;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.awt.geom.Point2D;

import java.awt.BasicStroke;
import java.awt.Stroke;

import edu.jhu.pha.sdss.fits.FITSImage;
import edu.jhu.pha.sdss.fits.Histogram;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;




public class FitsMenu implements ViewListener, PickerListener {


    public static final int WIDTH_MENU = 200;

    public static final int HEIGHT_BTN = 20;
    public static final Color BORDER_COLOR_BTN = Color.BLACK;
    public static final Color TEXT_COLOR_BTN = Color.BLACK;
    public static final Color BACKGROUND_COLOR_BTN = Color.LIGHT_GRAY;
    public static final Font FONT_BTN = new Font("Bold", Font.PLAIN, 11);
    //public static final Color LINE_COLOR = Color.RED;
    //public static final Stroke LINE_STROKE = new BasicStroke(1f);

    private static final int BORDER = 5;

    public static final int Z_BTN = 0;

    public static final String T_FILTER = "Fltr";
    public static final String T_SCALE = "Scl";
    public static final String T_SCROLL = "Scrll";

    public static final float WHEEL_FACTOR = 15.0f;


    public static final ColorGradient[] COLOR_GRADIENT = {new NopFilter(),
        new HeatFilter(), new RainbowFilter(),
        new MousseFilter(), new StandardFilter(),
        new RandomFilter(), new HazeFilter(),
        new BlulutFilter(), new Idl4Filter()};

    //public static final int[] SCALE_METHOD = {FITSImage.SCALE_ASINH, FITSImage.SCALE_HISTOGRAM_EQUALIZATION, FITSImage.SCALE_LINEAR, FITSImage.SCALE_LOG, FITSImage.SCALE_SQUARE, FITSImage.SCALE_SQUARE_ROOT};
    public static final FitsImage.ScaleMethod[] SCALE_METHOD = {FitsImage.ScaleMethod.LINEAR, FitsImage.ScaleMethod.LOG,
                                                                FitsImage.ScaleMethod.HISTOGRAM_EQUALIZATION, FitsImage.ScaleMethod.SQUARE,
                                                                FitsImage.ScaleMethod.SQUARE_ROOT, FitsImage.ScaleMethod.ASINH};
    public static final String[] SCALE_NAME = {"LINEAR", "LOG", "HISTOGRAM_EQUALIZATION", "SQUARE", "SQUARE_ROOT", "ASINH"};


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

    FitsViewer app;
    VirtualSpace mnSpace;

    FitsHistogram hist;

    int lastJPX;

    VRectangle shadow;

    boolean scroll = false;
    boolean press3_scroll = false;

    FitsMenu(FitsViewer app){
        this.app = app;
        mnSpace = app.mnSpace;
        drawFiltersColor();
    }

    private void drawFiltersColor(){

        BORDER_TOP_FILTER =  app.VIEW_H/2;

        int py = app.VIEW_H/2 - 2*HEIGHT_BTN - BORDER;

        for(int i = 0; i < COLOR_GRADIENT.length; i++){
            MultipleGradientPaint grad = Utils.makeGradient((RGBImageFilter)COLOR_GRADIENT[i]);
            PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, grad, BORDER_COLOR_BTN);
            filter.setType(T_FILTER);
            filter.setOwner(COLOR_GRADIENT[i]);
            mnSpace.addGlyph(filter);
            py -= (HEIGHT_BTN + 2*BORDER);
        }

        for(int i = 0; i < SCALE_METHOD.length; i++){
            PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, BACKGROUND_COLOR_BTN, BORDER_COLOR_BTN);
            filter.setType(T_SCALE);
            filter.setOwner(SCALE_METHOD[i]);
            mnSpace.addGlyph(filter);

            VText scaleText = new VText(-app.VIEW_W/2 + WIDTH_MENU/2, py - BORDER, Z_BTN, TEXT_COLOR_BTN, SCALE_NAME[i], VText.TEXT_ANCHOR_MIDDLE);
            scaleText.setFont(FONT_BTN);
            mnSpace.addGlyph(scaleText);
            filter.stick(scaleText);
            //Glyph ln = drawMethod(SCALE_METHOD[i]);
            //ln.moveTo(-app.VIEW_W/2 + WIDTH_MENU/2 + BORDER , py);
            //mnSpace.addGlyph(ln);
            py -= (HEIGHT_BTN + 2*BORDER);
        }
        BORDER_BOTTON_FILTER = py + HEIGHT_BTN - 2*BORDER;
    }

    /*
    public void drawHistogram(){

        if(app.hi != null){
            hist = FitsHistogram.fromFitsImage(app.hi);
            hist.moveTo(-app.VIEW_W/2 + (app.VIEW_W - hist.getWidth())/2 , -app.VIEW_H/2 + 50);
        }

        BORDER_TOP_HISTOGRAM = (int)(app.VIEW_H - hist.getHeight() - 65 );
        BORDER_BOTTON_HISTOGRAM = app.VIEW_H - 65;
        BORDER_LEFT_HISTOGRAM = (int)( (app.VIEW_W - hist.getWidth())/2 - FitsHistogram.DEFAULT_BIN_WIDTH) ;
        BORDER_RIGHT_HISTOGRAM = (int)( (app.VIEW_W + hist.getWidth())/2 + FitsHistogram.DEFAULT_BIN_WIDTH) ;

        VRectangle board = new VRectangle(hist.vx+hist.getWidth()/2, hist.vy+hist.getHeight()/2, Z_BTN, BORDER_RIGHT_HISTOGRAM-BORDER_LEFT_HISTOGRAM, BORDER_BOTTON_HISTOGRAM-BORDER_TOP_HISTOGRAM+10, Color.WHITE, Color.WHITE, 0.8f);
        mnSpace.addGlyph(board);
        mnSpace.addGlyph(hist);
    }
    */
/*
    public void redrawHistogram(){
        mnSpace.removeGlyph(hist);
        hist = FitsHistogram.fromFitsImage(app.hi, scale_method);
        hist.moveTo(-app.VIEW_W/2 + (app.VIEW_W - hist.getWidth())/2 , -app.VIEW_H/2 + 50);
        mnSpace.addGlyph(hist);
    }
*/

/*
    private Glyph drawMethod(int scaleMethod){
        DPath ln;

        switch(scaleMethod){
            case FITSImage.SCALE_ASINH:
                ASINH = new Vector<Point2D.Double>();
                double x = -2;
                double y;
                while(x <= 2){
                    y = Math.log(x+Math.sqrt(1+x*x));
                    ASINH.add(new Point2D.Double(x,y));
                    x+=0.1;
                }
                ln = new DPath(ASINH.get(0).getX()*FACTOR_W_ASINH, ASINH.get(0).getY()*FACTOR_H_ASINH, Z_BUTTON, LINE_COLOR, 1f);
                for(Point2D.Double p : ASINH){
                    ln.addSegment(p.getX()*FACTOR_W_ASINH, p.getY()*FACTOR_H_ASINH, true);
                }
                break;
            case FITSImage.SCALE_HISTOGRAM_EQUALIZATION:
                ln = new DPath(ASINH.get(0).getX(), ASINH.get(0).getY(), Z_BUTTON, LINE_COLOR, 1f);
                for(Point2D.Double p : ASINH){
                    ln.addSegment(p.getX(), p.getY(), true);
                }
                break;
            case FITSImage.SCALE_LINEAR:
                ln = new DPath(0, 0, Z_BUTTON, LINE_COLOR, 1f);
                ln.addSegment(WIDTH_MENU - 2*BORDER, 0, true);
                break;
            case FITSImage.SCALE_SQUARE:
                ln = new DPath(ASINH.get(0).getX(), ASINH.get(0).getY(), Z_BUTTON, LINE_COLOR, 1f);
                for(Point2D.Double p : ASINH){
                    ln.addSegment(p.getX(), p.getY(), true);
                }
                break;
            case FITSImage.SCALE_SQUARE_ROOT:
                ln = new DPath(ASINH.get(0).getX(), ASINH.get(0).getY(), Z_BUTTON, LINE_COLOR, 1f);
                for(Point2D.Double p : ASINH){
                    ln.addSegment(p.getX(), p.getY(), true);
                }
                break;
            default:
                ln = new DPath(ASINH.get(0).getX(), ASINH.get(0).getY(), Z_BUTTON, LINE_COLOR, 1f);
                for(Point2D.Double p : ASINH){
                    ln.addSegment(p.getX(), p.getY(), true);
                }
                break;
        }
        ln.setStroke(LINE_STROKE);
        return ln;
    }
*/

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM){
            lastJPX = jpx;
            if(shadow != null)
                app.mnSpace.removeGlyph(shadow);
            /*
            VRectangle[] bars = hist.getBars();
            for(VRectangle b : bars){
                b.setColor(FitsHistogram.SELECTED_FILL_COLOR);
            }
            */
        }
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
    	/*
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM){
            /*
            VRectangle[] bars = hist.getBars();
            for(VRectangle b : bars){
                if(b.getLocation().getX() > ((lastJPX < jpx)? lastJPX : jpx) - app.VIEW_W/2 && b.getLocation().getX() < ((lastJPX < jpx)? jpx : lastJPX) - app.VIEW_W/2){
                    b.setColor(FitsHistogram.DEFAULT_FILL_COLOR);
                }
            }
            *
            double min = app.hi.getUnderlyingImage().getHistogram().getMin();
            double max = app.hi.getUnderlyingImage().getHistogram().getMax();
            //double left = ( ((lastJPX < jpx)? lastJPX : jpx) - (app.VIEW_W - hist.getWidth())/2 ) / hist.getWidth();
            //double right = ( ((lastJPX < jpx)? jpx : lastJPX) - (app.VIEW_W - hist.getWidth())/2 ) / hist.getWidth();

            double left = ( ((lastJPX < jpx)? lastJPX : jpx) - app.VIEW_W/2 + hist.getWidth()/2) / hist.getWidth();
            double right = ( ((lastJPX < jpx)? jpx : lastJPX) - app.VIEW_W/2 + hist.getWidth()/2) / hist.getWidth();


            System.out.println("left: " + left + " right: " + right);

            left = (left < 0) ? 0 : left;
            right = (right > 1) ? 1 : right;
            app.hi.rescale(min + left*(max - min), min + right*(max - min), 1);
            if(lastJPX < jpx){
                if(lastJPX < BORDER_LEFT_HISTOGRAM)
                    lastJPX = BORDER_LEFT_HISTOGRAM;
                if(jpx > BORDER_RIGHT_HISTOGRAM)
                    jpx = BORDER_RIGHT_HISTOGRAM;
            } else {
                if(jpx < BORDER_LEFT_HISTOGRAM)
                    jpx = BORDER_LEFT_HISTOGRAM;
                if(lastJPX > BORDER_RIGHT_HISTOGRAM)
                    lastJPX = BORDER_RIGHT_HISTOGRAM;
            }
            shadow = new VRectangle( ((lastJPX < jpx)? lastJPX : jpx) + Math.abs(lastJPX - jpx)/2 - app.VIEW_W/2, -app.VIEW_H/2 + 100, Z_BTN, Math.abs(lastJPX - jpx), hist.getHeight() + 10, Color.WHITE, Color.BLACK, .2f);
            shadow.setType(T_SCROLL);
            app.mnSpace.addGlyph(shadow);
            lastJPX = 0;
        }
        */
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        //System.out.println("clickNumber: " + clickNumber);
        /*
        if(clickNumber == 2){
            double min = app.hi.getUnderlyingImage().getHistogram().getMin();
            double max = app.hi.getUnderlyingImage().getHistogram().getMax();
            double left = 0;
            double right = 1;
            app.hi.rescale(min + left*(max - min), min + right*(max - min), 1);

            if(shadow != null)
                app.mnSpace.removeGlyph(shadow);

            /*
            VRectangle[] bars = hist.getBars();
            for(VRectangle b : bars){
                b.setColor(FitsHistogram.DEFAULT_FILL_COLOR);
            }
            *
        }
        */
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
        //if(scroll){
    	/*
            press3_scroll = false;
            lastJPX = 0;
            double min = app.hi.getUnderlyingImage().getHistogram().getMin();
            double max = app.hi.getUnderlyingImage().getHistogram().getMax();
            //System.out.println("shadow: " + shadow.vx + " " + (shadow.vx + shadow.vw) + " - " + hist.getWidth());
            double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
            double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());

            System.out.println("left: " + left + " right: " + right);

            left = (left < 0) ? 0 : left;
            right = (right > 1) ? 1 : right;
            app.hi.rescale(min + left*(max - min), min + right*(max - min), 1);
        //}
        */
    }
    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        //System.out.println(app.menu.BORDER_BOTTON_HISTOGRAM + " > " + jpy + " > " + app.menu.BORDER_TOP_HISTOGRAM);
        //System.out.println(hist.vx + " " + hist.vy + " " + hist.getWidth() + " " + hist.getHeight());
        //System.out.println(BORDER_LEFT_HISTOGRAM + " < " + jpx + " < " + BORDER_RIGHT_HISTOGRAM);
        if((jpx < app.menu.WIDTH_MENU && jpy > app.menu.BORDER_BOTTON_FILTER && jpy < app.menu.BORDER_TOP_FILTER) ||
          (jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM && jpx > app.menu.BORDER_LEFT_HISTOGRAM && jpx < app.menu.BORDER_RIGHT_HISTOGRAM )){
            v.parent.setActiveLayer(2);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        } else {
            v.parent.setActiveLayer(0);
            v.parent.setCursorIcon(Cursor.CUSTOM_CURSOR);
        }



    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if(press3_scroll){

            if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2 + (jpx - lastJPX)) > app.menu.BORDER_LEFT_HISTOGRAM &&
                (shadow.vx+shadow.vw/2 + app.VIEW_W/2 + (jpx - lastJPX)) < app.menu.BORDER_RIGHT_HISTOGRAM){

                shadow.move(jpx - lastJPX,0);
                lastJPX = jpx;

            } else if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2) > app.menu.BORDER_LEFT_HISTOGRAM &&
                (shadow.vx+shadow.vw/2 + app.VIEW_W/2) < app.menu.BORDER_RIGHT_HISTOGRAM){
                if( (jpx - lastJPX) > 0 ){
                    shadow.move( app.menu.BORDER_RIGHT_HISTOGRAM - (shadow.vx+shadow.vw/2 + app.VIEW_W/2) ,0);
                    lastJPX = jpx;

                } else if( (jpx - lastJPX) < 0 ){
                    shadow.move( app.menu.BORDER_LEFT_HISTOGRAM - (shadow.vx-shadow.vw/2 + app.VIEW_W/2) ,0);
                    lastJPX = jpx;

                }
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

            /*
            double min = app.hi.getUnderlyingImage().getHistogram().getMin();
            double max = app.hi.getUnderlyingImage().getHistogram().getMax();
            //System.out.println("shadow: " + shadow.vx + " " + (shadow.vx + shadow.vw) + " - " + hist.getWidth());
            double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
            double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());

            //System.out.println("left: " + left + " right: " + right);
            left = (left < 0) ? 0 : left;
            right = (right > 1) ? 1 : right;
            app.hi.rescale(min + left*(max - min), min + right*(max - min), 1);
            */
        }
    }

    public void enterGlyph(Glyph g){
        if(g.getType().equals(T_FILTER)){
            app.setColorFilter((ImageFilter)g.getOwner());
            if(color_selected != null){
                color_selected.setWidth(color_selected.getWidth()-BORDER*2);
                color_selected.move(-BORDER,0);
            }
            ((PRectangle)g).setWidth(((PRectangle)g).getWidth()+BORDER*2);
            g.move(BORDER, 0);
            color_selected = (PRectangle)g;
        } else if(g.getType().equals(T_SCALE) && scale_selected != (PRectangle)g){
        	app.setScaleMethod((FitsImage.ScaleMethod)g.getOwner());
            //app.hi.setScaleMethod((FitsImage.ScaleMethod)g.getOwner());
            if(scale_selected != null){
                scale_selected.setWidth(scale_selected.getWidth()-BORDER*2);
                scale_selected.move(-BORDER,0);
            }
            ((PRectangle)g).setWidth(((PRectangle)g).getWidth()+BORDER*2);
            g.move(BORDER,0);
            scale_selected = (PRectangle)g;
        } else if(g.getType().equals(T_SCROLL)){
            scroll = true;
            //v.parent.setCursorIcon(Cursor.);
        }
    }

    public void exitGlyph(Glyph g){
        if(g.getType().equals(T_SCROLL)){
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

    public void selectNextColorMapping(){}
    public void selectPreviousColorMapping(){}
    public void selectNextScale(){}
    public void selectPreviousScale(){}


}



class FitsHistogram extends Composite {
    public static final double DEFAULT_BIN_WIDTH = 6;
    public static final Color DEFAULT_FILL_COLOR = new Color(0,0,255,127);
    public static final Color SELECTED_FILL_COLOR = new Color(0,0,255,210);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0,0,255,180);

    double width;
    double height = 100;
    VRectangle[] bars = new VRectangle[128];

    public FitsHistogram(int[] data, int min, int max, Color fillColor){

        width = DEFAULT_BIN_WIDTH*data.length;
        VRectangle backgrown = new VRectangle(width/2, height/2, FitsMenu.Z_BTN, width, height, Color.GRAY, Color.BLACK, 0.2f);
        addChild(backgrown);

        int i = 0;
        //int val;
        for(int j = 0; j< data.length; j++){
            //val = data[j];
            double h = (Math.sqrt(data[j]) * height) / Math.sqrt(max - min);
            int hh = (int)(h);
            hh = ( hh % 2 == 0) ? hh : hh + 1;
            VRectangle bar = new VRectangle(i+DEFAULT_BIN_WIDTH/2, (int)(hh/2), FitsMenu.Z_BTN, DEFAULT_BIN_WIDTH, (int)(hh), fillColor);
            bar.setBorderColor(DEFAULT_BORDER_COLOR);
            addChild(bar);
            bars[j] = bar;
            i += DEFAULT_BIN_WIDTH;
        }

    }

    public VRectangle[] getBars(){
        return bars;
    }

    public FitsHistogram(int[] data, int min, int max){
        this(data, min, max, DEFAULT_FILL_COLOR);
    }

    //Scale method linear
    public static FitsHistogram fromFitsImage(FitsImage image, Color fillColor){
        Histogram hist = image.getUnderlyingImage().getHistogram();
        int[] data = new int[128];

        for(int i=0; i<hist.getCounts().length; ++i){
            data[i/(hist.getCounts().length / data.length)] += hist.getCounts()[i];
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int j=0; j<data.length; ++j){
            if (data[j] < min){
                min = data[j];
            }
            if(data[j] > max){
                max = data[j];
            }
        }
        return new FitsHistogram(data, min, max, fillColor);
    }

    /*
    public static FitsHistogram fromJSkyFitsImage(FitsImage image, Color fillColor){
        Histogram hist = image.getUnderlyingImage().getHistogram();
        int[] data = new int[128];

        for(int i=0; i<hist.getCounts().length; ++i){
            data[i/(hist.getCounts().length / data.length)] += hist.getCounts()[i];
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int j=0; j<data.length; ++j){
            if (data[j] < min){
                min = data[j];
            }
            if(data[j] > max){
                max = data[j];
            }
        }
        return new FitsHistogram(data, min, max, fillColor);
    }
    */


    public double getHeight(){
        return height;
    }
    public double getWidth(){
        return width;
    }

    public static FitsHistogram fromFitsImage(FitsImage image){
        return fromFitsImage(image, DEFAULT_FILL_COLOR);
    }


}
