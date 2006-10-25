/*   FILE: GeonamesRDFStore.java
 *   DATE OF CREATION:  Mon Oct 23 10:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.gnb;

import java.awt.Color;
import java.awt.Font;

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
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.LText;
import com.xerox.VTM.glyphs.LBText;

class GeonamesRDFStore implements RDFErrorHandler {

    /* location of geonames RDF files */
    static final String[] CITIES_DIR = {"GIS/test"};
    static final String[] REGIONS_DIR = {"GIS/RDFaustates", "GIS/RDFcaprovinces", "GIS/RDFrufederal", "GIS/RDFusstates"};
    static final String[] COUNTRIES_DIR = {"GIS/RDFcountries"};

    /* RDF parser settings */
    static final String RDFXML_AB = "RDF/XML-ABBREV";
    static final String ERROR_MODE_PN = "http://jena.hpl.hp.com/arp/properties/error-mode";
    static final String ERROR_MODE_PV = "lax";

    /* RDF/OWL geonames URIs */
    static final String GEONAMES_NS = "http://www.geonames.org/ontology#";
    static final String FEATURE_CLASS_PROPERTY = "featureClass";
    static final String FEATURE_CODE_PROPERTY = "featureCode";
    static final String CITY_FEATURE_CLASS_URI = GEONAMES_NS + "P";
    static final String REGION_FEATURE_CODE_URI = GEONAMES_NS + "A.ADM1";
    static final String COUNTRY_FEATURE_CODE_URI = GEONAMES_NS + "A.PCLI";
    static final String WGS84_POS_NS = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    static final String LATITUDE_PROPERTY = "lat";
    static final String LONGITUDE_PROPERTY = "long";
    static final String NAME_PROPERTY = "name";
    static final String ALT_NAME_PROPERTY = "alternateName";

    static final String LANG_EN = "en";
    
    /* Geometrical / display settings */
    static final long CITY_HALF_WIDTH = 4;
    static final long CITY_WIDTH = 2 * CITY_HALF_WIDTH;
    static final long CITY_LABEL_VOFFSET = 6; // vertical offset of labels w.r.t square representing the city itself
    static final Color CITY_COLOR = Color.YELLOW;
    static final Color REGION_COLOR = new Color(255,150,0); // orange
    static final Color COUNTRY_COLOR = Color.WHITE;
    static final Font CITY_FONT = new Font("Dialog", Font.PLAIN, 10);
    static final Font REGION_FONT = new Font("Dialog",Font.ITALIC,40);
    static final Font COUNTRY_FONT = new Font("Dialog",Font.BOLD,100);

    /*various altitudes that trigger changes w.r.t levels of detail*/
    /* The following table summarizes the visibility
       of various types of labels, seen through the
       standard view (S) and through the lens (L)
       depending on the level (L0, L1, L2, L3).
      ------------------------------------------------
      |    |   Country   |   Region    |    City     |
      |    |  S   |  L   |  S   |  L   |  S   |  L   |
      ------------------------------------------------
      | L0 |      |  X   |      |      |      |      |
      | L1 |  X   |  X   |      |  X   |      |      |
      | L2 |  X   |  X   |  X   |  X   |      |  X   |
      | L3 |      |      |  X   |  X   |  X   |  X   |
      ------------------------------------------------
    */
    static final short LEVEL_0 = 0;
    static final short LEVEL_1 = 1;
    static final short LEVEL_2 = 2;
    static final short LEVEL_3 = 3;

    static final float LEVEL_3_ALT = 40;
    static final float LEVEL_2_ALT = 2000;
    static final float LEVEL_1_ALT = 7000;

    /* current level of detail (0,1,2,3) depending on observation altitude */
    short lbd = LEVEL_0; // Level of Details

    /* Glyphs representing cities, regions, countries*/
    VRectangle[] cities;
    /*all city labels*/
    LText[] cityLabels;
    /*all country labels*/
    LText[] countryLabels;
    /*all region labels*/
    LText[] regionLabels;


    /* Jena RDF models */
    Model citiesRDF;
    Model regionsRDF;
    Model countriesRDF;

    /** associates RDF resource representing a feature to corresponding glyph on map */
    Hashtable resource2glyph;

    GeonamesBrowser application;

    RDFReader parser;

    GeonamesRDFStore(GeonamesBrowser application){
	this.application = application;
	resource2glyph = new Hashtable();
    }

    RDFReader initRDFParser(Model m){
	RDFReader p = m.getReader(RDFXML_AB);
	p.setErrorHandler(this);
	p.setProperty(ERROR_MODE_PN, ERROR_MODE_PV);
	return p;
    }

    void loadCountries(){
	countriesRDF = ModelFactory.createDefaultModel();
	RDFReader parser = initRDFParser(countriesRDF);
	for (int i=0;i<COUNTRIES_DIR.length;i++){
	    loadCountriesFromDir(COUNTRIES_DIR[i], parser);   
	}
	processCountryModel();
    }

    void loadCountriesFromDir(String dirS, RDFReader parser){
	File dir = new File(dirS);
	File[] countryFiles = dir.listFiles(new RDFFileFilter());
	ProgPanel pp = new ProgPanel(dirS, Messages.LOADING_COUNTRIES);
	FileInputStream fis;
	for (int i=0;i<countryFiles.length;i++){
	    try {
		fis = new FileInputStream(countryFiles[i]);
		parser.read(countriesRDF, fis, countryFiles[i].toURL().toString());
		pp.setPBValue(i*100/countryFiles.length);
	    }
	    catch(Exception ex){System.err.println("Error while processing country file " + countryFiles[i].toString());}
	}
	pp.destroy();
    }

    void processCountryModel(){
	StmtIterator si = countriesRDF.listStatements(null, countriesRDF.getProperty(GEONAMES_NS, FEATURE_CODE_PROPERTY),
						      countriesRDF.getResource(COUNTRY_FEATURE_CODE_URI));
	Vector countries = new Vector();
	Statement s;
	while (si.hasNext()){
	    s = si.nextStatement();
	    countries.add(s.getSubject());
	}
	si.close();
	Resource r;
	LText countryL;
	long lx, ly;
	Property longP = countriesRDF.getProperty(WGS84_POS_NS, LONGITUDE_PROPERTY);
	Property latP = countriesRDF.getProperty(WGS84_POS_NS, LATITUDE_PROPERTY);
	Property nameP = countriesRDF.getProperty(GEONAMES_NS, NAME_PROPERTY);
	Property altNameP = countriesRDF.getProperty(GEONAMES_NS, ALT_NAME_PROPERTY);
	Vector data = new Vector();
	for (int i=0;i<countries.size();i++){
	    r = (Resource)countries.elementAt(i);
	    lx = Math.round(r.getProperty(longP).getDouble() * GeonamesBrowser.HALF_MAP_WIDTH/180.0);
	    ly = Math.round(r.getProperty(latP).getDouble() * GeonamesBrowser.HALF_MAP_HEIGHT/90.0);
	    countryL = new LText(lx, ly, 0, COUNTRY_COLOR, getPropertyWithLang(r, altNameP, LANG_EN, nameP), LText.TEXT_ANCHOR_MIDDLE);
	    countryL.setSpecialFont(COUNTRY_FONT);
	    application.vsm.addGlyph(countryL, application.mapSpace);
 	    countryL.setVisible(false);
 	    countryL.setVisibleThroughLens(false);
	    data.add(countryL);
	}
	storeCountries(data);
    }

    void loadRegions(){
	regionsRDF = ModelFactory.createDefaultModel();
	RDFReader parser = initRDFParser(regionsRDF);
	for (int i=0;i<REGIONS_DIR.length;i++){
	    loadRegionsFromDir(REGIONS_DIR[i], parser);   
	}
	processRegionModel();
    }

    void loadRegionsFromDir(String dirS, RDFReader parser){
	File dir = new File(dirS);
	File[] regionFiles = dir.listFiles(new RDFFileFilter());
	ProgPanel pp = new ProgPanel(dirS, Messages.LOADING_REGIONS);
	FileInputStream fis;
	for (int i=0;i<regionFiles.length;i++){
	    try {
		fis = new FileInputStream(regionFiles[i]);
		parser.read(regionsRDF, fis, regionFiles[i].toURL().toString());
		pp.setPBValue(i*100/regionFiles.length);
	    }
	    catch(Exception ex){System.err.println("Error while processing region file " + regionFiles[i].toString());}
	}
	pp.destroy();
    }

    void processRegionModel(){
	StmtIterator si = regionsRDF.listStatements(null, regionsRDF.getProperty(GEONAMES_NS, FEATURE_CODE_PROPERTY),
						   regionsRDF.getResource(REGION_FEATURE_CODE_URI));
	Vector regions = new Vector();
	Statement s;
	while (si.hasNext()){
	    s = si.nextStatement();
	    regions.add(s.getSubject());
	}
	si.close();
	Resource r;
	LText regionL;
	long lx, ly;
	Property longP = regionsRDF.getProperty(WGS84_POS_NS, LONGITUDE_PROPERTY);
	Property latP = regionsRDF.getProperty(WGS84_POS_NS, LATITUDE_PROPERTY);
	Property nameP = regionsRDF.getProperty(GEONAMES_NS, NAME_PROPERTY);
	Vector data = new Vector();
	for (int i=0;i<regions.size();i++){
	    r = (Resource)regions.elementAt(i);
	    lx = Math.round(r.getProperty(longP).getDouble() * GeonamesBrowser.HALF_MAP_WIDTH/180.0);
	    ly = Math.round(r.getProperty(latP).getDouble() * GeonamesBrowser.HALF_MAP_HEIGHT/90.0);
	    regionL = new LText(lx, ly, 0, REGION_COLOR, r.getProperty(nameP).getString(), LText.TEXT_ANCHOR_MIDDLE);
	    regionL.setSpecialFont(REGION_FONT);
	    application.vsm.addGlyph(regionL, application.mapSpace);
 	    regionL.setVisible(false);
 	    regionL.setVisibleThroughLens(false);
	    data.add(regionL);
	}
	storeRegions(data);
    }

    String getPropertyWithLang(Resource r, Property p, String lang, Property fallBack){
	String res;
	StmtIterator si = r.listProperties(p);
	Statement s;
	while (si.hasNext()){
	    s = si.nextStatement();
	    if (s.getLanguage().equals(lang)){
		return s.getString();
	    }
	}
	si.close();
	// return value of property fallBack if a value for p tagged with lang could not be found
	return r.getProperty(fallBack).getString();
    }

    void loadCities(){
	citiesRDF = ModelFactory.createDefaultModel();
	RDFReader parser = initRDFParser(citiesRDF);
	for (int i=0;i<CITIES_DIR.length;i++){
	    loadCitiesFromDir(CITIES_DIR[i], parser);   
	}
	processCityModel();
    }

    void loadCitiesFromDir(String dirS, RDFReader parser){
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
	LBText cityL;
	long x, y, lx, ly;
	Property longP = citiesRDF.getProperty(WGS84_POS_NS, LONGITUDE_PROPERTY);
	Property latP = citiesRDF.getProperty(WGS84_POS_NS, LATITUDE_PROPERTY);
	Property nameP = citiesRDF.getProperty(GEONAMES_NS, NAME_PROPERTY);
	Vector data = new Vector();
	for (int i=0;i<cities.size();i++){
	    r = (Resource)cities.elementAt(i);
	    x = Math.round(r.getProperty(longP).getDouble() * GeonamesBrowser.HALF_MAP_WIDTH/180.0);
	    y = Math.round(r.getProperty(latP).getDouble() * GeonamesBrowser.HALF_MAP_HEIGHT/90.0);
	    lx = x;
	    ly = y + CITY_LABEL_VOFFSET;
	    cityG = new BRectangle(x, y, 0, CITY_HALF_WIDTH, CITY_HALF_WIDTH, CITY_COLOR);
	    cityG.setOwner(r);
	    cityL = new LBText(lx, ly, 0, CITY_COLOR, r.getProperty(nameP).getString());
	    cityL.setBorderColor(Color.BLACK);
	    application.vsm.addGlyph(cityG, application.mapSpace);
	    application.vsm.addGlyph(cityL, application.mapSpace);
 	    cityL.setVisible(false);
 	    cityL.setVisibleThroughLens(false);
	    resource2glyph.put(r, cityG);
	    data.add(cityG);
	    data.add(cityL);
	}
	storeCities(data);
    }

    /*store a set of cities*/
    void storeCities(Vector data){
	cityLabels = new LText[data.size()/2];
	cities = new VRectangle[data.size()/2];
 	for (int i=0;i<data.size()/2;i++){
 	    cities[i] = (VRectangle)data.elementAt(i*2);
 	    cityLabels[i] = (LText)data.elementAt((i*2)+1);
 	}
    }

    /*store a set of regions*/
    void storeRegions(Vector data){
	regionLabels = new LText[data.size()];
	for (int i=0;i<data.size();i++){
	    regionLabels[i] = (LText)data.elementAt(i);
	}
    }

    /*store a set of countries*/
    void storeCountries(Vector data){
	countryLabels = new LText[data.size()];
	for (int i=0;i<data.size();i++){
	    countryLabels[i] = (LText)data.elementAt(i);
	}
    }

    /* change level of details (depends on camera altitude) */
    void updateLabelLevel(float altitude){
	if (altitude < LEVEL_3_ALT){
	    updateLabelLevel(LEVEL_3);
	}
	else if (altitude < LEVEL_2_ALT){
	    updateLabelLevel(LEVEL_2);
	}
	else if (altitude < LEVEL_1_ALT){
	    updateLabelLevel(LEVEL_1);
	}
	else {
	    updateLabelLevel(LEVEL_0);
	}
    }

    /* change level of details (depends on camera altitude) */
    void updateLabelLevel(short level){
	if (level != lbd){
	    if (level == LEVEL_3){
		if (lbd == LEVEL_2){// arriving at level 3 from level 2
		    showCountryLabels(false, false);
		    showCityLabels(true);
		}
		else if (lbd == LEVEL_1){// arriving at level 3 from level 1
		    showCountryLabels(false, false);
		    showCityLabels(true, true);
		    showRegionLabels(true);
		}
		else {// arriving at level 3 from level 0
		    showCountryLabelsInLens(false);
		    showCityLabels(true, true);
		    showRegionLabels(true, true);
		}
		lbd = LEVEL_3;
	    }
	    else if (level == LEVEL_2){
		if (lbd == LEVEL_3){// arriving at level 2 from level 3
		    showCountryLabels(true, true);
		    showCityLabels(false);
		}
		else if (lbd == LEVEL_1){// arriving at level 2 from level 1
		    showRegionLabels(true);
		    showCityLabelsInLens(true);
		}
		else {// arriving at level 2 from level 0
		    showCountryLabels(true);
		    showRegionLabels(true, true);
		    showCityLabelsInLens(true);
		}
		lbd = LEVEL_2;
	    }
	    else if (level == LEVEL_1){
		if (lbd == LEVEL_2){// arriving at level 1 from level 2
		    showCityLabelsInLens(false);
		    showRegionLabels(false);
		}
		else if (lbd == LEVEL_0){// arriving at level 1 from level 0
		    showCountryLabels(true);
		    showRegionLabelsInLens(true);
		}
		else {// arriving at level 1 from level 3
		    showCityLabels(false, false);
		    showRegionLabels(false);
		    showCountryLabels(true, true);
		}
		lbd = LEVEL_1;
	    }
	    else {// LEVEL_0
		if (lbd == LEVEL_1){// arriving at level 0 from level 1
		    showCountryLabels(false);
		    showRegionLabelsInLens(false);
		}
		else if (lbd == LEVEL_2){// arriving at level 0 from level 2
		    showCountryLabels(false);
		    showCityLabelsInLens(false);
		    showRegionLabels(false, false);
		}
		else {// arriving at level 0 from level 3
		    showCountryLabelsInLens(true);
		    showCityLabels(false, false);
		    showRegionLabels(false, false);
		}
		lbd = LEVEL_0;
	    }
	}
    }

    /* visibility management methods (main view) */
    void showCityLabels(boolean b){
	for (int i=0;i<cityLabels.length;i++){
	    if (cityLabels[i] != null){cityLabels[i].setVisible(b);}
	}
    }
    
     void showRegionLabels(boolean b){
	for (int i=0;i<regionLabels.length;i++){
	    regionLabels[i].setVisible(b);
	}
     }
    
     void showCountryLabels(boolean b){
	for (int i=0;i<countryLabels.length;i++){
	    countryLabels[i].setVisible(b);
	}
     }

    /* visibility management methods (lens focus) */
    void showCityLabelsInLens(boolean b){
	for (int i=0;i<cityLabels.length;i++){
	    if (cityLabels[i] != null){cityLabels[i].setVisibleThroughLens(b);}
	}
    }
    
    void showRegionLabelsInLens(boolean b){
	for (int i=0;i<regionLabels.length;i++){
	    regionLabels[i].setVisibleThroughLens(b);
	}
    }
    
    void showCountryLabelsInLens(boolean b){
	for (int i=0;i<countryLabels.length;i++){
	    countryLabels[i].setVisibleThroughLens(b);
	}
    }

    /* visibility management methods (main view & lens focus) */
    void showCityLabels(boolean b1, boolean b2){
	for (int i=0;i<cityLabels.length;i++){
	    if (cityLabels[i] != null){
		cityLabels[i].setVisible(b1);
		cityLabels[i].setVisibleThroughLens(b2);
	    }
	}
    }
    
     void showRegionLabels(boolean b1, boolean b2){
	for (int i=0;i<regionLabels.length;i++){
	    regionLabels[i].setVisible(b1);
	    regionLabels[i].setVisibleThroughLens(b2);
	}
     }
    
    void showCountryLabels(boolean b1, boolean b2){
	for (int i=0;i<countryLabels.length;i++){
	    countryLabels[i].setVisible(b1);
	    countryLabels[i].setVisibleThroughLens(b2);
	}
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

class RDFFileFilter implements FileFilter {
    
    static final String RDF_EXT = ".rdf";

    public boolean accept(File f){
	return f.getName().toLowerCase().endsWith(RDF_EXT);
    }
    
}
