/*   FILE: Test.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2011.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.tests;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;

import java.util.Vector;

import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.event.*;

public class Test {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewListener eh;

    View testView;
    Camera cam;
    
    Test(String vt){
        vsm=VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest(vt);
    }

    public void initTest(String vt){
        eh=new EventHandlerTest(this);
        vs = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        cam = vs.addCamera();
        Vector cameras=new Vector();
        cameras.add(vs.getCamera(0));
        vs.getCamera(0).setZoomFloor(-90f);
        testView = vsm.addFrameView(cameras, View.ANONYMOUS, vt, 800, 600, false, true, true, null);
        testView.setBackgroundColor(Color.LIGHT_GRAY);
        testView.setListener(eh);
        testView.setAntialiasing(true);

        rr = new VRoundRect(0, 0, 0, 200, 100, Color.WHITE, Color.BLACK, 20, 20);
        rr.setStroke(new BasicStroke(3f));
        vs.addGlyph(rr);
		        
        vsm.repaint();
    }
    
    VRoundRect rr;
    
    void doIt(){
        vs.removeGlyph(rr);
    }
    
    public static void main(String[] args){
        System.out.println("-----------------");
        System.out.println("General information");
        System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
        System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
        System.out.println("-----------------");
        System.out.println("Directory information");
        System.out.println("Java Classpath: "+System.getProperty("java.class.path"));	
        System.out.println("Java directory: "+System.getProperty("java.home"));
        System.out.println("Launching from: "+System.getProperty("user.dir"));
        System.out.println("-----------------");
        System.out.println("User informations");
        System.out.println("User name: "+System.getProperty("user.name"));
        System.out.println("User home directory: "+System.getProperty("user.home"));
        System.out.println("-----------------");
        new Test((args.length > 0) ? args[0] : View.STD_VIEW);
    }
    
}

class EventHandlerTest implements ViewListener {

    Test application;

    //remember last mouse coords to compute translation  (dragging)
    long lastJPX,lastJPY;

    EventHandlerTest(Test appli){
        application=appli;
    }
    
    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){application.doIt();}

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawDrag(true);
        application.vsm.getActiveView().mouse.setSensitivity(false);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.cam.setXspeed(0);
        application.cam.setYspeed(0);
        application.cam.setZspeed(0);
        v.setDrawDrag(false);
        application.vsm.getActiveView().mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            Camera c=application.vsm.getActiveCamera();
            double a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                application.cam.setXspeed(0);
                application.cam.setYspeed(0);
                application.cam.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.cam.setXspeed((c.altitude>0) ? ((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : ((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                application.cam.setYspeed((c.altitude>0) ? ((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : ((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                application.cam.setZspeed(0);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c=application.vsm.getActiveCamera();
        double a=(c.focal+Math.abs(c.altitude))/c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            application.vsm.repaint();
        }
        else {
            //wheelDirection == WHEEL_DOWN
            c.altitudeOffset(a*5);
            application.vsm.repaint();
        }
    }

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}

class TextStroke implements Stroke {
	private String text;
	private Font font;
	private boolean stretchToFit = false;
	private boolean repeat = false;
	private AffineTransform t = new AffineTransform();

	private static final float FLATNESS = 1;

	public TextStroke( String text, Font font ) {
		this( text, font, true, false );
	}

	public TextStroke( String text, Font font, boolean stretchToFit, boolean repeat ) {
		this.text = text;
		this.font = font;
		this.stretchToFit = stretchToFit;
		this.repeat = repeat;
	}

	public Shape createStrokedShape( Shape shape ) {
		FontRenderContext frc = new FontRenderContext(null, true, true);
		GlyphVector glyphVector = font.createGlyphVector(frc, text);

		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		boolean first = false;
		float next = 0;
		int currentChar = 0;
		int length = glyphVector.getNumGlyphs();

		if ( length == 0 )
            return result;

        float factor = stretchToFit ? measurePathLength( shape )/(float)glyphVector.getLogicalBounds().getWidth() : 1.0f;
        float nextAdvance = 0;

		while ( currentChar < length && !it.isDone() ) {
			type = it.currentSegment( points );
			switch( type ){
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				result.moveTo( moveX, moveY );
				first = true;
                nextAdvance = glyphVector.getGlyphMetrics( currentChar ).getAdvance() * 0.5f;
                next = nextAdvance;
				break;

			case PathIterator.SEG_CLOSE:
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....

			case PathIterator.SEG_LINETO:
				thisX = points[0];
				thisY = points[1];
				float dx = thisX-lastX;
				float dy = thisY-lastY;
				float distance = (float)Math.sqrt( dx*dx + dy*dy );
				if ( distance >= next ) {
					float r = 1.0f/distance;
					float angle = (float)Math.atan2( dy, dx );
					while ( currentChar < length && distance >= next ) {
						Shape glyph = glyphVector.getGlyphOutline( currentChar );
						Point2D p = glyphVector.getGlyphPosition(currentChar);
						float px = (float)p.getX();
						float py = (float)p.getY();
						float x = lastX + next*dx*r;
						float y = lastY + next*dy*r;
                        float advance = nextAdvance;
                        nextAdvance = currentChar < length-1 ? glyphVector.getGlyphMetrics(currentChar+1).getAdvance() * 0.5f : 0;
						t.setToTranslation( x, y );
						t.rotate( angle );
						t.translate( -px-advance, -py );
						result.append( t.createTransformedShape( glyph ), false );
						next += (advance+nextAdvance) * factor;
						currentChar++;
						if ( repeat )
							currentChar %= length;
					}
				}
                next -= distance;
				first = false;
				lastX = thisX;
				lastY = thisY;
				break;
			}
			it.next();
		}

		return result;
	}

	public float measurePathLength( Shape shape ) {
		PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
        float total = 0;

		while ( !it.isDone() ) {
			type = it.currentSegment( points );
			switch( type ){
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				break;

			case PathIterator.SEG_CLOSE:
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....

			case PathIterator.SEG_LINETO:
				thisX = points[0];
				thisY = points[1];
				float dx = thisX-lastX;
				float dy = thisY-lastY;
				total += (float)Math.sqrt( dx*dx + dy*dy );
				lastX = thisX;
				lastY = thisY;
				break;
			}
			it.next();
		}

		return total;
	}

}
