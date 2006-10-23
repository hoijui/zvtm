/*   FILE: GeonamesRDFStore.java
 *   DATE OF CREATION:  Mon Oct 23 10:08:06 2006
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

import com.xerox.VTM.glyphs.BRectangle;

class GeonamesRDFStore implements RDFErrorHandler {

    /* location of geonames RDF files */
    static final String[] CITIES_DIR = {"GIS/test"};
    static final String[] STATES_DIR = {"GIS/RDFaustates", "GIS/RDFcaprovinces", "GIS/RDFrufederal", "GIS/RDFusstates"};
    static final String[] COUNTRIES_DIR = {"GIS/RDFcountries"};

    /* RDF parser settings */
    private static final String RDFXML_AB = "RDF/XML-ABBREV";
    private static final String ERROR_MODE_PN = "http://jena.hpl.hp.com/arp/properties/error-mode";
    private static final String ERROR_MODE_PV = "lax";

    /* RDF/OWL geonames URIs */
    static final String GEONAMES_NS = "http://www.geonames.org/ontology#";
    static final String FEATURE_CLASS_PROPERTY = "featureClass";
    static final String CITY_FEATURE_CLASS_URI = "http://www.geonames.org/ontology#P";
    static final String WGS84_POS_NS = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    static final String LATITUDE_PROPERTY = "lat";
    static final String LONGITUDE_PROPERTY = "long";
    
    /* Geometrical / display settings */
    static final long CITY_HALF_WIDTH = 4;
    static final long CITY_WIDTH = 2 * CITY_HALF_WIDTH;
    static final Color CITY_COLOR = Color.YELLOW;

    Model citiesRDF;
    Model statesRDF;
    Model countriesRDF;

    /** associates RDF resource representing a feature to corresponding glyph on map */
    Hashtable resource2glyph;

    GeonamesBrowser application;

    RDFReader parser;

    GeonamesRDFStore(GeonamesBrowser application){
	this.application = application;
	resource2glyph = new Hashtable();
    }

    void initRDFParser(Model m){
	parser = m.getReader(RDFXML_AB);
	parser.setErrorHandler(this);
	parser.setProperty(ERROR_MODE_PN, ERROR_MODE_PV);
    }

    void loadCities(){
	citiesRDF = ModelFactory.createDefaultModel();
	initRDFParser(citiesRDF);
	for (int i=0;i<CITIES_DIR.length;i++){
	    loadCitiesFromDir(CITIES_DIR[i]);   
	}
	parser = null;
	processCityModel();
    }

    void loadCitiesFromDir(String dirS){
	File dir = new File(dirS);
	File[] cityFiles = dir.listFiles(new RDFFileFilter());
	ProgPanel pp = new ProgPanel(dirS, Messages.LOADING_CITIES);
	FileInputStream fis;
	for (int i=0;i<cityFiles.length;i++){
	    try {
		fis = new FileInputStream(cityFiles[i]);
		parser.read(citiesRDF, fis, cityFiles[i].toURL().toString());
		pp.setPBValue(i*100/cityFiles.length);
	    }
	    catch(Exception ex){System.err.println("Error while processing city file " + cityFiles[i].toString());}
	}
	pp.destroy();
    }
    

    void processCityModel(){
	StmtIterator si = citiesRDF.listStatements(null, citiesRDF.getProperty(GEONAMES_NS, FEATURE_CLASS_PROPERTY),
						   citiesRDF.getResource(CITY_FEATURE_CLASS_URI));
	Vector cities = new Vector();
	Statement s;
	while (si.hasNext()){
	    s = si.nextStatement();
	    cities.add(s.getSubject());
	}
	si.close();
	Resource r;
	BRectangle cityG;
	long x, y;
	Property longP = citiesRDF.getProperty(WGS84_POS_NS, LONGITUDE_PROPERTY);
	Property latP = citiesRDF.getProperty(WGS84_POS_NS, LATITUDE_PROPERTY);
	for (int i=0;i<cities.size();i++){
	    r = (Resource)cities.elementAt(i);
	    x = Math.round(r.getProperty(longP).getDouble() * GeonamesBrowser.HALF_MAP_WIDTH/180.0);
	    y = Math.round(r.getProperty(latP).getDouble() * GeonamesBrowser.HALF_MAP_HEIGHT/90.0);
	    cityG = new BRectangle(x, y, 0, CITY_HALF_WIDTH, CITY_HALF_WIDTH, CITY_COLOR);
	    cityG.setOwner(r);
	    application.vsm.addGlyph(cityG, application.mapSpace);
	    resource2glyph.put(r, cityG);
	}
    }

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

class RDFFileFilter implements FileFilter {
    
    static final String RDF_EXT = ".rdf";

    public boolean accept(File f){
	return f.getName().toLowerCase().endsWith(RDF_EXT);
    }
    
}
