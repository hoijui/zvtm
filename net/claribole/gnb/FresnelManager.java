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
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.Glyph;

import org.w3c.IsaViz.fresnel.FSLPath;
import org.w3c.IsaViz.fresnel.FSLNSResolver;

class FresnelManager implements RDFErrorHandler {

    public static final String FRESNEL_NAMESPACE_URI = "http://www.w3.org/2004/09/fresnel#";
    public static final String RDF_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /* RDF properties */
    public static final String _type = "type";
    public static final String _first = "first";
    public static final String _rest = "rest";
    public static final String _nil = RDF_NAMESPACE_URI + "nil";

    /* Fresnel properties */
    public static final String _primaryClasses = "primaryClasses";
    public static final String _classLensDomain = "classLensDomain";
    public static final String _instanceLensDomain = "instanceLensDomain";
    public static final String _classFormatDomain = "classFormatDomain";
    public static final String _instanceFormatDomain = "instanceFormatDomain";
    public static final String _propertyFormatDomain = "propertyFormatDomain";
    public static final String _purpose = "purpose";
    public static final String _showProperties = "showProperties";
    public static final String _hideProperties = "hideProperties";
    public static final String _property = "property";
    public static final String _sublens = "sublens";
    public static final String _depth = "depth";
    public static final String _use = "use";
    public static final String _group = "group";
    public static final String _resourceStyle = "resourceStyle";
    public static final String _propertyStyle = "propertyStyle";
    public static final String _label = "label";
    public static final String _labelStyle = "labelStyle";
    public static final String _value = "value";
    public static final String _valueStyle = "valueStyle";
    public static final String _valueFormat = "valueFormat";
    public static final String _labelFormat = "labelFormat";
    public static final String _propertyFormat = "propertyFormat";
    public static final String _resourceFormat = "resourceFormat";
    public static final String _contentBefore = "contentBefore";
    public static final String _contentAfter = "contentAfter";
    public static final String _contentFirst = "contentFirst";
    public static final String _contentLast = "contentLast";
    public static final String _contentNoValue = "contentNoValue";
    public static final String _alternateProperties = "alternateProperties";
    public static final String _mergeProperties = "mergeProperties";
    Property _restProperty, _firstProperty, _typeProperty;


    /* Fresnel default values */
    public static final String _Group = FRESNEL_NAMESPACE_URI + "Group";
    public static final String _Lens = FRESNEL_NAMESPACE_URI + "Lens";
    public static final String _Format = FRESNEL_NAMESPACE_URI + "Format";
    public static final String _labelLens = FRESNEL_NAMESPACE_URI + "labelLens";
    public static final String _defaultLens = FRESNEL_NAMESPACE_URI + "defaultLens";
    public static final String _allProperties = FRESNEL_NAMESPACE_URI + "allProperties";
    public static final String _member = FRESNEL_NAMESPACE_URI + "member";
    public static final String _externalLink = FRESNEL_NAMESPACE_URI + "externalLink";
    public static final String _uri = FRESNEL_NAMESPACE_URI + "uri";
    public static final String _image = FRESNEL_NAMESPACE_URI + "image";
    public static final String _none = FRESNEL_NAMESPACE_URI + "none";

    /* Fresnel selector languages */
    public static final String _fslSelector = FRESNEL_NAMESPACE_URI + "fslSelector";
    public static final String _sparqlSelector = FRESNEL_NAMESPACE_URI + "sparqlSelector";

    static final short _BASIC_SELECTOR = 0;
    static final short _FSL_SELECTOR = 1;
    static final short _SPARQL_SELECTOR = 2;



    static final String N3 = "N3";
    static final File LAYOUT_LENS_FILE = new File("GIS/fresnel/gnb-layout.n3");
    static final File DETAIL_LENS_FILE = new File("GIS/fresnel/gnb-detail.n3");

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
    FresnelLens[] detailLenses;
    FresnelFormat[] detailFormats;
    FresnelGroup[] detailGroups;
    Hashtable group2lenses, group2formats;


    FresnelManager(GeonamesBrowser app){
	this.application = app;
	initNSResolver();
	group2lenses = new Hashtable();
	group2formats = new Hashtable();
	buildLayoutLenses();
	buildDetailLenses();
    }

    void initNSResolver(){
	nsr = new FSLNSResolver();
	nsr.addPrefixBinding("rdf", RDF_NAMESPACE_URI);
	nsr.addPrefixBinding("gn", GeonamesRDFStore.GEONAMES_NS);
	nsr.addPrefixBinding("wgs84_pos", GeonamesRDFStore.WGS84_POS_NS);
    }

    void init(){
	frame = new VRectangleST(0, 0, 0, 10, 10, FRAME_FILL_COLOR);
	frame.setBorderColor(FRAME_BORDER_COLOR);
	frame.setTransparencyValue(FRAME_OPACITY);
	application.vsm.addGlyph(frame, infoSpace);
	infoSpace.hide(frame);
	iCamera.setAltitude(0);
    }

    RDFReader initRDFParser(Model m){
	RDFReader p = m.getReader(N3);
	p.setErrorHandler(this);
	p.setProperty(GeonamesRDFStore.ERROR_MODE_PN, GeonamesRDFStore.ERROR_MODE_PV);
	return p;
    }

    void buildLayoutLenses(){
	//XXX: TBW
    }

    void buildDetailLenses(){
	// parse detail lenses N3 RDF file
	Model detailRDF = ModelFactory.createDefaultModel();
	RDFReader parser = initRDFParser(detailRDF);
	String baseURL;
	try {
	    baseURL = DETAIL_LENS_FILE.toURL().toString();
	    if (baseURL.startsWith("file:/") && !baseURL.startsWith("file:///")){
		// ugly hack to address the file:/ vs. file:/// problem
		baseURL = baseURL.substring(0, 6) + "//" + baseURL.substring(6);
	    }
	}
	catch (MalformedURLException ex){
	    baseURL = "";
	}
	try {
	    FileInputStream fis = new FileInputStream(DETAIL_LENS_FILE);
	    parser.read(detailRDF, fis, baseURL);
	}
	catch(Exception ex){System.err.println("Error while processing country file " + DETAIL_LENS_FILE.toString());}
	_firstProperty = detailRDF.getProperty(RDF_NAMESPACE_URI+_first);
	_restProperty = detailRDF.getProperty(RDF_NAMESPACE_URI+_rest);
	_typeProperty = detailRDF.getProperty(RDF_NAMESPACE_URI+_type);
	// lenses
	StmtIterator si = detailRDF.listStatements(null, _typeProperty, detailRDF.getResource(_Lens));
	Vector v = new Vector();
	while (si.hasNext()){
	    v.add(si.nextStatement().getSubject());
	}
	si.close();
	detailLenses = new FresnelLens[v.size()];
	for (int i=0;i<v.size();i++){
	    detailLenses[i] = buildLens((Resource)v.elementAt(i), detailRDF, baseURL);
	}
	v.clear();
	// formats
	si = detailRDF.listStatements(null, _typeProperty, detailRDF.getResource(_Format));
	while (si.hasNext()){
	    v.add(si.nextStatement().getSubject());
	}
	si.close();
	detailFormats = new FresnelFormat[v.size()];
	for (int i=0;i<detailFormats.length;i++){
	    detailFormats[i] = buildFormat((Resource)v.elementAt(i), detailRDF);
	}
	v.clear();
	// groups
	Property groupProperty = detailRDF.getProperty(FRESNEL_NAMESPACE_URI, _group);
	while (si.hasNext()){
	    v.add(si.nextStatement().getSubject());
	}
	si.close();
	detailGroups = new FresnelGroup[v.size()];
	for (int i=0;i<detailGroups.length;i++){
	    detailGroups[i] = buildGroup((Resource)v.elementAt(i), groupProperty);
	}
	v.clear();
	group2lenses.clear();
	group2formats.clear();	
    }

    FresnelLens buildLens(Resource lensNode, Model model, String baseURL){
	FresnelLens res = new FresnelLens((lensNode.isAnon()) ? lensNode.getId().toString() : lensNode.getURI(), baseURL);
	/* process instanceLensDomain properties */
	StmtIterator si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _instanceLensDomain));
	RDFNode n;
	while (si.hasNext()){
	    n = si.nextStatement().getObject();
	    if (n instanceof Resource){
		res.addInstanceDomain(((Resource)n).getURI(), _BASIC_SELECTOR);
	    }
	    else {// instanceof Literal
	        Literal l = (Literal)n;
		String value = Utils.delLeadingAndTrailingSpaces(l.getLexicalForm());
		String dt = l.getDatatypeURI();
		if (dt == null){// basic selector (in theory, should not happen as basic selectors are givenas URIs,
		    //             not literals whose text is a URI, but we support it for robustness
		    res.addInstanceDomain(value, _BASIC_SELECTOR);
		}
		else if (dt.equals(_fslSelector)){
		    res.addInstanceDomain(FSLPath.pathFactory(value, nsr, FSLPath.NODE_STEP), _FSL_SELECTOR);
		}
		else if (dt.equals(_sparqlSelector)){
		    res.addInstanceDomain(value, _SPARQL_SELECTOR);
		}
		else {
		    System.out.println("Fresnel: Unknown selector language: "+dt);
		}
	    }
	}
	si.close();
	/* process classLensDomain properties */
	si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _classLensDomain));
	while (si.hasNext()){
	    res.addClassDomain(si.nextStatement().getResource().getURI());
	}
	si.close();
	Statement s;
	/* process property visibility */
	Vector toShow = new Vector();
	Vector toHide = new Vector();
	si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _showProperties));
	if (si.hasNext()){
	    // only take the first one, a lens is not supposed to declare multiple showProperties
	    s = si.nextStatement();
	    processSelectorList(s.getResource(), toShow);
	}
	si.close();
	// deal with hideProperties only if special value fresnel:allProperties appears in showProperties
	int apIndex = toShow.indexOf(_allProperties);
	if (apIndex != -1){
	    si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _hideProperties));
	    if (si.hasNext()){
		// only take the first one, a lens is not supposed to declare multiple hideProperties
		s = si.nextStatement();
		processSelectorList(s.getResource(), toHide);
	    }
	    si.close();
	}
	res.setPropertiesVisibility(toShow, toHide, apIndex);
	// deal with group declarations (store them temporarily until they get processed by buildGroup())
	Vector lenses;
	Resource group;
	String groupId;
	si = lensNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _group));
	while (si.hasNext()){
	    group = si.nextStatement().getResource();
	    groupId = (group.isAnon()) ? group.getId().toString() : group.getURI();
	    lenses = (group2lenses.containsKey(groupId)) ? (Vector)group2lenses.get(groupId) : new Vector();
	    lenses.add(res);
	    group2lenses.put(groupId, lenses);
	}
	si.close();
	return res;
    }

    FresnelFormat buildFormat(Resource formatNode, Model model){
	FresnelFormat res = new FresnelFormat((formatNode.isAnon()) ? formatNode.getId().toString() : formatNode.getURI());
	/* process propertyFormatDomain properties */
	StmtIterator si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _propertyFormatDomain));
	RDFNode n;
	while (si.hasNext()){
	    n = si.nextStatement().getObject();
	    if (n instanceof Resource){
		res.addPropertyDomain(((Resource)n).getURI(), _BASIC_SELECTOR);
	    }
	    else {// instanceof Literal
	        Literal l = (Literal)n;
		String value = Utils.delLeadingAndTrailingSpaces(l.getLexicalForm());
		String dt = l.getDatatypeURI();
		if (dt == null){// basic selector (in theory, should not happen as basic selectors are givenas URIs,
		    //             not literals whose text is a URI, but we support it for robustness
		    res.addPropertyDomain(value, _BASIC_SELECTOR);
		}
		else if (dt.equals(_fslSelector)){
		    res.addPropertyDomain(FSLPath.pathFactory(value, nsr, FSLPath.NODE_STEP), _FSL_SELECTOR);
		}
		else {
		    System.out.println("Fresnel: Unsupported selector language for property format domain: "+dt);
		}
	    }
	}
	si.close();
	si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _value));
	if (si.hasNext()){
	    // only take the first one, a format is not supposed to declare multiple fresnel:value properties
	    res.setValue(si.nextStatement().getResource().getURI());
	}
	si.close();
	// deal with group declarations (store them temporarily until they get processed by buildGroup())
	Vector formats;
	Resource group;
	String groupId;
	si = formatNode.listProperties(model.getProperty(FRESNEL_NAMESPACE_URI, _group));
	while (si.hasNext()){
	    group = si.nextStatement().getResource();
	    groupId = (group.isAnon()) ? group.getId().toString() : group.getURI();
	    formats = (group2formats.containsKey(groupId)) ? (Vector)group2formats.get(groupId) : new Vector();
	    formats.add(res);
	    group2formats.put(groupId, formats);
	}
	si.close();
	return res;
    }

    FresnelGroup buildGroup(Resource groupNode, Property groupProperty){
	FresnelGroup res = new FresnelGroup((groupNode.isAnon()) ? groupNode.getId().toString() : groupNode.getURI());
	if (group2lenses.containsKey(res.uri)){
	    Vector v = (Vector)group2lenses.get(res.uri);
	    for (int i=0;i<v.size();i++){
		res.addLens((FresnelLens)v.elementAt(i));
	    }
	}
	if (group2formats.containsKey(res.uri)){
	    Vector v = (Vector)group2formats.get(res.uri);
	    for (int i=0;i<v.size();i++){
		res.addFormat((FresnelFormat)v.elementAt(i));
	    }
	}
	return res;
    }

    private void processSelectorList(Resource r, Vector values){
	// process item at this level
	if (r.hasProperty(_firstProperty)){
	    RDFNode n = r.getProperty(_firstProperty).getObject();
	    if (n instanceof Resource){
		Resource r2 = (Resource)n;
		if (r2.isAnon()){
		    //XXX: TBW  (complex case where there is info about what sublens to use)
		}
		else {
		    values.add((r2).toString());
		}
	    }
	    else {// n instanceof Literal
		Literal l = (Literal)n;
		String value = Utils.delLeadingAndTrailingSpaces(l.getLexicalForm());
		String dt = l.getDatatypeURI();
		if (dt == null){
		    values.add(value);
		}
		else if (dt.equals(_fslSelector)){
		    values.add(FSLPath.pathFactory(value, nsr, FSLPath.ARC_STEP));
		}
		// SPARQL not supported here yet
	    }
	}
	// recursive call to process next item
	if (r.hasProperty(_restProperty)){
	    Resource o = r.getProperty(_restProperty).getResource();
	    if (o.isAnon() || !o.getURI().equals(_nil)){
		processSelectorList(o, values);
	    }
	}
    }


    /* city information display management */

    void showInformationAbout(Resource r, int jpx, int jpy){
	//XXX: retrieve information items to display according to lens
	//XXX: compute width and height of frame, position of text info
	long frameHalfWidth = 100;
	long frameHalfHeight = 150;
	// adapt frame geometry
	frame.vx = jpx - application.panelWidth/2 + frameHalfWidth + FRAME_HORIZONTAL_OFFSET;
	frame.vy = application.panelHeight/2 - jpy - frameHalfHeight - FRAME_VERTICAL_OFFSET;
	frame.setWidth(frameHalfWidth);
	frame.setHeight(frameHalfHeight);
	infoSpace.show(frame);
	//XXX: add text info
	VText t = new VText(frame.vx, frame.vy, 0, FRAME_BORDER_COLOR, r.getURI(), VText.TEXT_ANCHOR_MIDDLE);
	informationItems.add(t);
	for (int i=0;i<informationItems.size();i++){
	    application.vsm.addGlyph((Glyph)informationItems.elementAt(i), infoSpace);
	}
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