/*   FILE: GeoDataStore.java
 *   DATE OF CREATION:  Tue Mar 07 18:26:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: GeoDataStore.java,v 1.24 2006/06/02 14:01:42 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JFileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.BRectangle;
import com.xerox.VTM.glyphs.LText;
import com.xerox.VTM.glyphs.LBText;

public class GeoDataStore {

    static final String CSV_SEP = ";";

    /*get info about country/region/city label show/hide actions*/
    static final boolean DEBUG = false;

    /*various altitudes that trigger changes w.r.t levels of detail*/
    static final short LEVEL_0 = 0;
    static final short LEVEL_1 = 1;
    static final short LEVEL_2 = 2;
    static final short LEVEL_3 = 3;

    static final float LEVEL_3_ALT = 40;
    static final float LEVEL_2_ALT = 2000;
    static final float LEVEL_1_ALT = 7000;

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

    static final long TARGET_HALF_WIDTH = 4;
    static final long TARGET_WIDTH = 2 * TARGET_HALF_WIDTH;

    static final int MIN_TARGET_SIZE = 4;
    static final int MAX_TARGET_SIZE = 10;

    final static short CITIES = 0;
    final static short COUNTRIES = 1;
    final static short REGIONS = 2;

    /*color of country labels*/
    static final Color COUNTRY_COLOR = Color.WHITE;
    /*color of city labels*/
    static final Color CITY_COLOR = Color.YELLOW;
    /*color of target city label*/
    static final Color TARGET_COLOR = Color.RED;
    /*color of region labels*/
    static final Color REGION_COLOR = new Color(255,150,0); // orange
    /*font of city labels*/
    static final Font CITY_FONT = new Font("Dialog",Font.PLAIN,10);
    /*font of country labels*/
    static final Font COUNTRY_FONT = new Font("Dialog",Font.BOLD,100);
    /*font of region labels*/
    static final Font REGION_FONT = new Font("Dialog",Font.ITALIC,40);
    /*vertical offset of labels w.r.t square representing the city itself*/
    static final long CITY_LABEL_VOFFSET = 6;

    ZLWorldTask application;

    /*squares representing cities*/
    VRectangle[] cities;
    /*all city labels*/
    LText[] cityLabels;

    /*all country labels*/
    LText[] countryLabels;
    /*all region labels*/
    LText[] regionLabels;

    /* current level of detail (0,1,2,3) depending on observation altitude */
    short lbd = LEVEL_0; // Level of Details

    boolean trainingData = false;

    /*data store constructor*/
    GeoDataStore(ZLWorldTask app, boolean td){
	this.application = app;
	this.trainingData = td;
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

    /*build all countries, regions and cities*/
    void buildAll(){
	// countries
	buildCountries();
	// regions
	buildRegions();
	// cities
 	buildCities();
    }

    /*process all files containing city data*/
    void buildCities(){
	Vector data = new Vector();
// 	// capitales
// 	processFile("data/capitals_f224_xy.csv", CITIES, data);
// 	// world cities
//  	processFile("data/world_cities_f2058_xy.csv", CITIES, data);
// 	// US cities
// 	processFile("data/us_cities_f80000_xy.csv", CITIES, data);
// 	// Canadian cities
// 	processFile("data/ca_cities_f40000_xy.csv", CITIES, data);
// 	// Mexican cities
//  	processFile("data/mx_cities_f80000_xy.csv", CITIES, data);
 	// filtered data
 	processFile((trainingData) ? "data/training_cities.csv" : "data/finalCitySet.csv", CITIES, data);
	storeCities(data);
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Loaded "+(data.size()/2)+" cities total\n", Console.GRAY_STYLE);}
    }

    /*process all files containing country data*/
    void buildCountries(){
	Vector data = new Vector();
	processFile((trainingData) ? "data/training_countries.csv" : "data/countries_f192_xy.csv", COUNTRIES, data);
	storeCountries(data);
    }

    /*process all files containing region data*/
    void buildRegions(){
	Vector data = new Vector();
	if (trainingData){
	    processFile("data/training_states.csv", REGIONS, data);
	}
	else {
	    // US states
	    processFile("data/us_states_f50_xy.csv", REGIONS, data);
	    // Canadian provinces
	    processFile("data/ca_provinces_13_xy.csv", REGIONS, data);
	}
	storeRegions(data);
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Loaded "+data.size()+" regions total\n", Console.GRAY_STYLE);}
    }

    int processFile(String fileName, short dataType, Vector data){
	int count = 0;
	try {
	    File f = new File(fileName);
	    FileInputStream fis = new FileInputStream(f);
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    String line = br.readLine();
	    switch (dataType){
	    case CITIES:{
		while (line != null){
		    if (!line.startsWith("#")){// ignore lines commented out
			addCity(line.split(CSV_SEP), data);
			count++;
		    }
		    line = br.readLine();
		}
		if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Loaded "+count+" cities from "+fileName+"\n", Console.GRAY_STYLE);}
		break;
	    }
	    case COUNTRIES:{
		while (line != null){
		    if (!line.startsWith("#")){// ignore lines commented out
			addCountry(line.split(CSV_SEP), data);
			count++;
		    }
		    line = br.readLine();
		}
		if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Loaded "+count+" countries from "+fileName+"\n", Console.GRAY_STYLE);}
		break;
	    }
	    case REGIONS:{
		while (line != null){
		    if (!line.startsWith("#")){// ignore lines commented out
			addRegion(line.split(CSV_SEP), data);
			count++;
		    }
		    line = br.readLine();
		}
		if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Loaded "+count+" regions from "+fileName+"\n", Console.GRAY_STYLE);}
		break;
	    }
	    }
	    br.close();
	    isr.close();
	    fis.close();
	}
	catch(IOException ex){ex.printStackTrace();}
	return count;
    }

    void addCity(String[] rawData, Vector data){
	long x = Long.parseLong(rawData[2]);
	long y = Long.parseLong(rawData[1]);
	long tx, ty;
	if (rawData.length == 4){
	    // no specific coordinates for city label, put it at the default location
	    tx = x;
	    ty = y + CITY_LABEL_VOFFSET;
	}
	else {
	    // specific coordinates for the city label
	    tx = Long.parseLong(rawData[5]);
	    ty = Long.parseLong(rawData[4]);
	}
	data.add(application.vsm.addGlyph(new BRectangle(x, y, 0, TARGET_HALF_WIDTH, TARGET_HALF_WIDTH, CITY_COLOR),
					  application.mainVSname));
	LBText t = new LBText(tx, ty, 0, CITY_COLOR, rawData[0]);
	t.setVisible(false);
	t.setVisibleThroughLens(false);
	t.setBorderColor(Color.BLACK);
	data.add(application.vsm.addGlyph(t, application.mainVSname));
    }
    
    void addCountry(String[] rawData, Vector data){
	long x = Long.parseLong(rawData[2]);
	long y = Long.parseLong(rawData[1]);
	LText t = (LText)application.vsm.addGlyph(new LText(x, y, 0, COUNTRY_COLOR, rawData[0], LText.TEXT_ANCHOR_MIDDLE), application.mainVSname);
	t.setVisible(false);
	t.setVisibleThroughLens(true);
	t.setSpecialFont(COUNTRY_FONT);
	data.add(t);
    }

    void addRegion(String[] rawData, Vector data){	
	long x = Long.parseLong(rawData[2]);
	long y = Long.parseLong(rawData[1]);
	LText t = (LText)application.vsm.addGlyph(new LText(x, y, 0, REGION_COLOR, rawData[0], LText.TEXT_ANCHOR_MIDDLE), application.mainVSname);
	t.setVisible(false);
	t.setVisibleThroughLens(false);
	t.setSpecialFont(REGION_FONT);
	data.add(t);
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
 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Switching labels from level "+lbd+" to level "+level+"\n", Console.GRAY_STYLE);}
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
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Cities in main view: "+b+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<cityLabels.length;i++){
	    if (cityLabels[i] != null){cityLabels[i].setVisible(b);}
	}
    }
    
    void showRegionLabels(boolean b){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Regions in main view: "+b+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<regionLabels.length;i++){
	    regionLabels[i].setVisible(b);
	}
    }
    
    void showCountryLabels(boolean b){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Countries in main view: "+b+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<countryLabels.length;i++){
	    countryLabels[i].setVisible(b);
	}
    }

    /* visibility management methods (lens focus) */
    void showCityLabelsInLens(boolean b){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Cities in lens focus: "+b+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<cityLabels.length;i++){
	    if (cityLabels[i] != null){cityLabels[i].setVisibleThroughLens(b);}
	}
    }
    
    void showRegionLabelsInLens(boolean b){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Regions in lens focus: "+b+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<regionLabels.length;i++){
	    regionLabels[i].setVisibleThroughLens(b);
	}
    }
    
    void showCountryLabelsInLens(boolean b){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Countries in lens focus: "+b+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<countryLabels.length;i++){
	    countryLabels[i].setVisibleThroughLens(b);
	}
    }

    /* visibility management methods (main view & lens focus) */
    void showCityLabels(boolean b1, boolean b2){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Cities: main "+b1+"   lens "+b2+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<cityLabels.length;i++){
	    if (cityLabels[i] != null){
		cityLabels[i].setVisible(b1);
		cityLabels[i].setVisibleThroughLens(b2);
	    }
	}
    }
    
    void showRegionLabels(boolean b1, boolean b2){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Regions: main "+b1+"   lens "+b2+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<regionLabels.length;i++){
	    regionLabels[i].setVisible(b1);
	    regionLabels[i].setVisibleThroughLens(b2);
	}
    }
    
    void showCountryLabels(boolean b1, boolean b2){
	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.console.append("Countries: main "+b1+"   lens "+b2+"\n", Console.GRAY_STYLE);}
	for (int i=0;i<countryLabels.length;i++){
	    countryLabels[i].setVisible(b1);
	    countryLabels[i].setVisibleThroughLens(b2);
	}
    }

    int getCityIndex(Glyph g){
	int res = -1;
	for (int i=0;i<cities.length;i++){
	    if (g == cities[i]){
		return i;
	    }
	}
	return res;
    }

    void deleteCity(int i){
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Deleting city at index "+i+": "+cityLabels[i].getText()+" ... ");}
	application.mainVS.destroyGlyph(cities[i]);
	application.mainVS.destroyGlyph(cityLabels[i]);
	cities[i] = null;
	cityLabels[i] = null;
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append("done\n");}
    }

    void countCities(){
	int count = 0;
	for (int i=0;i<cities.length;i++){
	    if (cities[i] != null){count++;}
	}
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append(count + " cities left\n");}
    }

    static final String DATA_DIR = "data";
    static final String DATA_DIR_FULL = System.getProperty("user.dir") + File.separator + DATA_DIR;

    void saveCities(){
	JFileChooser fc = new JFileChooser(new File(DATA_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Data File");
	int returnVal= fc.showSaveDialog(application.demoView.getFrame());
	File dataFile = null;
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    dataFile = fc.getSelectedFile();
	}
	else {
	    return;
	}
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append("Saving cities to "+dataFile.toString()+" ... ");}	
	int count = 0;
	try {
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), "UTF-8"));
	    for (int i=0;i<cities.length;i++){
		if (cities[i] != null){
		    bw.write(cityLabels[i].getText() + CSV_SEP +
			     cities[i].vy + CSV_SEP +
			     cities[i].vx + CSV_SEP +
			     "0");
		    if (cityLabels[i].vx != cities[i].vx
			|| cityLabels[i].vy+CITY_LABEL_VOFFSET != cities[i].vy){
			bw.write(CSV_SEP + cityLabels[i].vy +
				 CSV_SEP + cityLabels[i].vx);
		    }
		    bw.newLine();
		    count++;
		}
	    }
	    bw.flush();
	    bw.close();
	}
	catch (IOException ex){ex.printStackTrace();}
	if (ZLWorldTask.SHOW_CONSOLE){application.console.append("done\nSaved "+count+" cities\n");}
    }
    
}