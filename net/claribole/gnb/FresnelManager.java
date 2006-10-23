/*   FILE: GeonamesRDFStore.java
 *   DATE OF CREATION:  Mon Oct 23 16:22:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.gnb;

import java.awt.Color;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;

import java.util.Hashtable;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.Glyph;

class FresnelManager implements RDFErrorHandler {

    static final Color FRAME_FILL_COLOR = Color.BLACK;
    static final Color FRAME_BORDER_COLOR = Color.WHITE;
    static final float FRAME_OPACITY = 0.8f;

    GeonamesBrowser application;

    VirtualSpace infoSpace;
    static final String infoSpaceName = "iSpace";
    Camera iCamera;

    VRectangleST frame;
    Vector informationItems = new Vector();

    FresnelManager(GeonamesBrowser app){
	this.application = app;	
    }

    void init(){
	frame = new VRectangleST(0, 0, 0, 10, 10, FRAME_FILL_COLOR);
	frame.setBorderColor(FRAME_BORDER_COLOR);
	frame.setTransparencyValue(FRAME_OPACITY);
	application.vsm.addGlyph(frame, infoSpace);
	infoSpace.hide(frame);
	iCamera.setAltitude(0);
    }

    static final long FRAME_HORIZONTAL_OFFSET = 10;
    static final long FRAME_VERTICAL_OFFSET = 10;

    /* city information display management */

    void showInformationAbout(Resource r, int jpx, int jpy){
	//XXX: retrieve information items to display according to lens
	//XXX: compute width and height of frame, position of text info
	long frameHalfWidth = 100;
	long frameHalfHeight = 150;
	
	frame.vx = jpx - application.panelWidth/2 + frameHalfWidth + FRAME_HORIZONTAL_OFFSET;
	frame.vy = application.panelHeight/2 - jpy - frameHalfHeight - FRAME_VERTICAL_OFFSET;
	frame.setWidth(frameHalfWidth);
	frame.setHeight(frameHalfHeight);
	infoSpace.show(frame);
	//XXX: add text info
    }
    
    void hideInformationAbout(){
	for (int i=0;i<informationItems.size();i++){
	    infoSpace.destroyGlyph((Glyph)informationItems.elementAt(i));
	}
	informationItems.clear();
	infoSpace.hide(frame);
    }

    /* RDF error handling (jena parsing) */

    public void error(Exception e){
	System.err.println("RDFErrorHandler:Error: " + format(e));
    }
    
    public void fatalError(Exception e){
	System.err.println("RDFErrorHandler:Fatal Error: " + format(e));
    }

    public void warning(Exception e){
	System.err.println("RDFErrorHandler:Warning: " + format(e));
    }

    private static String format(Exception e){
	String msg = e.getMessage();
	if (msg==null){msg = e.toString();}
	if (e instanceof org.xml.sax.SAXParseException){
	    org.xml.sax.SAXParseException spe=(org.xml.sax.SAXParseException)e;
	    return msg + "[Line = " + spe.getLineNumber() + ", Column = " + spe.getColumnNumber() + "]";
	}
	else {
	    return e.toString();
	}
    }

}