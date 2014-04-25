

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
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.FitsImage;

import java.awt.Cursor;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.awt.geom.Point2D;

import edu.jhu.pha.sdss.fits.FITSImage;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;


public class FitsMenu implements ViewListener{
	

	public static final int WIDTH_MENU = 200;

	public static final int HEIGHT_BUTTON = 20;
	public static final Color BORDER_COLOR_BTN = Color.BLACK;

	private static final int BORDER = 5;

	public static final int Z_BUTTON = 0;

	public static final String T_FILTER = "Fltr";
	public static final String T_SCALE = "Scl";

	public static final ColorGradient[] COLOR_GRADIANT = {new NopFilter(), new HeatFilter(), new RainbowFilter(), new MousseFilter(), new StandardFilter(), new RandomFilter()};

	public static final int[] SCALE_METHOD = {FITSImage.SCALE_ASINH, FITSImage.SCALE_HISTOGRAM_EQUALIZATION, FITSImage.SCALE_LINEAR, FITSImage.SCALE_LOG, FITSImage.SCALE_SQUARE, FITSImage.SCALE_SQUARE_ROOT};
	//public static final FitsImage.ScaleMethod[] SCALE_METHOD = {FitsImage.ScaleMethod.ASINH, FitsImage.ScaleMethod.HISTOGRAM_EQUALIZATION, FitsImage.ScaleMethod.LINEAR, FitsImage.ScaleMethod.LOG, FitsImage.ScaleMethod.SQUARE, FitsImage.ScaleMethod.SQUARE_ROOT};


	FitsExample app;
	VirtualSpace mnSpace;


	FitsMenu(FitsExample app){
		this.app = app;
		mnSpace = app.mnSpace;
		drawFiltersColor();

	}

	private void drawFiltersColor(){

		System.out.println("view_h: " + app.VIEW_H);
		int py = app.VIEW_H/2 - 2*HEIGHT_BUTTON - BORDER;
		for(int i = 0; i < COLOR_GRADIANT.length; i++){
			MultipleGradientPaint grad = Utils.makeGradient((RGBImageFilter)COLOR_GRADIANT[i]);
			PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2 + BORDER, py, Z_BUTTON, WIDTH_MENU - 2*BORDER, HEIGHT_BUTTON, grad, BORDER_COLOR_BTN);
	        filter.setType(T_FILTER);
	        filter.setOwner(COLOR_GRADIANT[i]);
	        mnSpace.addGlyph(filter);
	        py -= HEIGHT_BUTTON + 2*BORDER;
		}

		for(int i = 0; i < SCALE_METHOD.length; i++){
			PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2 + BORDER, py, Z_BUTTON, WIDTH_MENU - 2*BORDER, HEIGHT_BUTTON, Color.WHITE, BORDER_COLOR_BTN);
			filter.setType(T_SCALE);
			filter.setOwner(SCALE_METHOD[i]);
			mnSpace.addGlyph(filter);
	        py -= HEIGHT_BUTTON + 2*BORDER;
		}
	}

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if(jpx < app.WIDTH_MENU){
            v.parent.setActiveLayer(2);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        } else {
            v.parent.setActiveLayer(0);
            v.parent.setCursorIcon(Cursor.CUSTOM_CURSOR);
        }
    }

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){

	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

	public void enterGlyph(Glyph g){
        if(g.getType().equals(T_FILTER)){
            app.setColorFilter((ImageFilter)g.getOwner());
        } else if(g.getType().equals(T_SCALE)){
        	app.setScaleMethod((Integer)g.getOwner());
        }

	}

	public void exitGlyph(Glyph g){
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