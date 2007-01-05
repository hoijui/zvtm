/*   FILE: GeonamesRDFStore.java
 *   DATE OF CREATION:  Mon Oct 23 16:22:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.gnb;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.FontMetrics;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.MalformedURLException;

import java.util.Hashtable;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.Glyph;

import fr.inria.jfresnel.FresnelParser;
import fr.inria.jfresnel.FresnelDocument;
import fr.inria.jfresnel.Constants;
import fr.inria.jfresnel.Lens;
import fr.inria.jfresnel.Format;
import fr.inria.jfresnel.Group;
import fr.inria.jfresnel.jena.JenaLens;
import fr.inria.jfresnel.jena.JenaFormat;
import fr.inria.jfresnel.fsl.FSLPath;
import fr.inria.jfresnel.fsl.FSLNSResolver;
import fr.inria.jfresnel.fsl.FSLHierarchyStore;
import fr.inria.jfresnel.fsl.FSLJenaEvaluator;

class FresnelManager {

    static final String N3 = "N3";
    static final File LAYOUT_LENS_FILE = new File("data/GIS/RDF/fresnel/gnb-layout.n3");
    static final File DETAIL_LENS_FILE = new File("data/GIS/RDF/fresnel/gnb-detail.n3");

    static final Color FRAME_FILL_COLOR = Color.BLACK;
    static final Color FRAME_BORDER_COLOR = Color.WHITE;
    static final float FRAME_OPACITY = 0.8f;

    static final long FRAME_HORIZONTAL_OFFSET = 10;
    static final long FRAME_VERTICAL_OFFSET = 10;

    GeonamesBrowser application;

    VirtualSpace infoSpace;
    static final String infoSpaceName = "iSpace";
    Camera iCamera;

    VRectangleST frame;
    Vector informationItems = new Vector();

    FSLNSResolver nsr;
    FSLHierarchyStore fhs;

    FSLJenaEvaluator layoutFSLEvaluator;
    JenaLens[] layoutLenses;
    JenaLens selectedLayoutLens;
    Format[] layoutFormats;
    Group[] layoutGroups;

    FSLJenaEvaluator detailFSLEvaluator;
    JenaLens[] detailLenses;
    JenaLens selectedDetailLens;
    Format[] detailFormats;
    Group[] detailGroups;

    static final int DETAIL_FRAME_MIN_WIDTH = 50;
    static final int DETAIL_FRAME_MIN_HEIGHT = 10;
    /* used to get information about font rendering when computing the detail box's layout */
    Graphics2D gc;
    FontMetrics fontMetrics;
    int fontHeight;

    /* true means that mouse entering a city glyph will give information about that city */
    boolean ssd = true;

    FresnelManager(GeonamesBrowser app){
	this.application = app;
	initNSResolver();
	fhs = new FSLHierarchyStore();
	layoutFSLEvaluator = new FSLJenaEvaluator(nsr, fhs);
	detailFSLEvaluator = new FSLJenaEvaluator(nsr, fhs);
	buildLayoutLenses();
	buildDetailLenses();
    }

    void initNSResolver(){
	nsr = new FSLNSResolver();
	nsr.addPrefixBinding("rdf", GeonamesRDFStore.RDF_NS);
	nsr.addPrefixBinding("rdfs", GeonamesRDFStore.RDF_NS);
	nsr.addPrefixBinding("gn", GeonamesRDFStore.GEONAMES_NS);
	nsr.addPrefixBinding("wgs84_pos", GeonamesRDFStore.WGS84_POS_NS);
	nsr.addPrefixBinding("foaf", GeonamesRDFStore.FOAF_NS);
    }

    void init(){
	frame = new VRectangleST(0, 0, 0, 10, 10, FRAME_FILL_COLOR);
	frame.setBorderColor(FRAME_BORDER_COLOR);
	frame.setTransparencyValue(FRAME_OPACITY);
	application.vsm.addGlyph(frame, infoSpace);
	infoSpace.hide(frame);
	iCamera.setAltitude(0);
	gc = (Graphics2D)application.mView.getGraphicsContext();
	fontMetrics = gc.getFontMetrics(Messages.CITY_FONT);
	fontHeight = fontMetrics.getHeight();
	layoutFSLEvaluator.setModel(application.gs.citiesRDF);
	detailFSLEvaluator.setModel(application.gs.citiesRDF);
    }

    void buildLayoutLenses(){
	// parse layout lenses N3 RDF file
	FresnelParser fp = new FresnelParser(Constants.JENA_API, nsr, fhs);
	FresnelDocument fd = fp.parse(LAYOUT_LENS_FILE, Constants.N3_READER);
	Lens[] l = fd.getLenses();
	layoutLenses = new JenaLens[l.length];
	System.arraycopy(l, 0, layoutLenses, 0, l.length);
	layoutFormats = fd.getFormats();
	layoutGroups = fd.getGroups();
 	try {
 	    selectedLayoutLens = layoutLenses[0];
 	}
 	catch (ArrayIndexOutOfBoundsException ex){selectedLayoutLens = null;}
    }

    void buildDetailLenses(){
	// parse layout lenses N3 RDF file
	FresnelParser fp = new FresnelParser(Constants.JENA_API, nsr, fhs);
	FresnelDocument fd = fp.parse(DETAIL_LENS_FILE, Constants.N3_READER);
	Lens[] l = fd.getLenses();
	detailLenses = new JenaLens[l.length];
	System.arraycopy(l, 0, detailLenses, 0, l.length);
	detailFormats = fd.getFormats();
	detailGroups = fd.getGroups();
	try {
	    selectedDetailLens = detailLenses[0];
	}
	catch (ArrayIndexOutOfBoundsException ex){selectedDetailLens = null;}
    }

    Hashtable statements2formats = new Hashtable();

    /* city information display management */
    synchronized void showInformationAbout(Resource r, int jpx, int jpy){
	if (!ssd){return;}
	// check that this resource can indeed be handled by the current Fresnel lens
	if (selectedDetailLens.selects(r, detailFSLEvaluator)){
	    Vector statementsToDisplay = selectedDetailLens.getPropertyValuesToDisplay(r, detailFSLEvaluator);
	    statements2formats.clear();
	    Statement s;
	    Format f;
	    for (int i=0;i<statementsToDisplay.size();i++){
		s = (Statement)statementsToDisplay.elementAt(i);
		f = selectedDetailLens.getBestFormatForProperty(s, detailFSLEvaluator);
		if (f != null){
		    statements2formats.put(s, f);
		}
	    }
	    Vector lines = new Vector();
	    Vector v = new Vector();
	    Object currentStatement = statementsToDisplay.firstElement();
	    v.add(currentStatement);
	    lines.add(v);
	    Object previousStatement;
	    Object formatOfPreviousStatement, formatOfCurrentStatement;
	    for (int i=1;i<statementsToDisplay.size();i++){
		previousStatement = currentStatement;
		currentStatement = statementsToDisplay.elementAt(i);
		formatOfPreviousStatement = statements2formats.get(previousStatement);
		formatOfCurrentStatement = statements2formats.get(currentStatement);
		if (formatOfCurrentStatement != null && formatOfCurrentStatement == formatOfPreviousStatement &&
		    ((Format)formatOfCurrentStatement).hasValueFormattingInstructions()){
		    // new value goes on same line as previous value
		    ((Vector)lines.lastElement()).add(currentStatement);
		}
		else {// new value goes on a new line
		    v = new Vector();
		    v.add(currentStatement);
		    lines.add(v);
		}
	    }
	    String[] textLines = new String[lines.size()];
	    JenaFormat jf;
	    for (int i=0;i<textLines.length;i++){
		v = (Vector)lines.elementAt(i);
		String text = "";
		// all statements on a line have the same format (as a result of the previous loop)
		jf = (JenaFormat)statements2formats.get(v.firstElement());
		for (int j=0;j<v.size();j++){// apply contentBefore and contentAfter instructions, if any
		    text += applyFormattingInstructions((Statement)v.elementAt(j), jf, j==0, j==v.size()-1);
		}
		textLines[i] = text;
		// apply label, contentFirst and contentLast instructions, if any
		if (jf != null){
		    if (jf.getContentFirst() != null){textLines[i] = jf.getContentFirst() + textLines[i];}
		    if (jf.getValueLabel() != null){textLines[i] = jf.getValueLabel() + textLines[i];}
		    if (jf.getContentLast() != null){textLines[i] += jf.getContentLast();}
		}
	    }
	    long frameWidth = (statementsToDisplay.size() > 0) ? 0 : DETAIL_FRAME_MIN_WIDTH;
	    long frameHalfHeight = DETAIL_FRAME_MIN_HEIGHT;
 	    long vertCoordOfLines[] = new long[textLines.length]; // relative vertical position of text line inside box
 	    long lineWidth;
	    for (int i=0;i<vertCoordOfLines.length;i++){
		vertCoordOfLines[i] = (2 * i + 2) * fontHeight;
		frameHalfHeight += fontHeight;
		lineWidth = fontMetrics.stringWidth(textLines[i]);
		if (lineWidth > frameWidth){
		    frameWidth = lineWidth;
		}
	    }
	    frameWidth *= 1.2;
	    // adapt frame geometry
	    frame.vx = jpx - application.panelWidth/2 + frameWidth/2 + FRAME_HORIZONTAL_OFFSET;
	    frame.vy = application.panelHeight/2 - jpy - frameHalfHeight - FRAME_VERTICAL_OFFSET;
	    frame.setWidth(frameWidth/2);
	    frame.setHeight(frameHalfHeight);
	    infoSpace.show(frame);
	    // add text info lines
	    for (int i=0;i<textLines.length;i++){
		VText t = new VText(frame.vx-frame.getWidth()+10, frame.vy+frame.getHeight()-vertCoordOfLines[i], 0, FRAME_BORDER_COLOR, textLines[i]);
		application.vsm.addGlyph(t, infoSpace);
		informationItems.add(t);
	    }
	}
    }

    String applyFormattingInstructions(Statement s, JenaFormat f, boolean firstItem, boolean lastItem){
 	return (f != null) ? f.apply(s, firstItem, lastItem) : JenaFormat.defaultFormat(s);
    }
    
    synchronized void hideInformationAbout(){
	for (int i=0;i<informationItems.size();i++){
	    infoSpace.destroyGlyph((Glyph)informationItems.elementAt(i));
	}
	informationItems.clear();
	infoSpace.hide(frame);
    }

    void switchShowDetails(){
	ssd = !ssd;
    }

}