/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.awt.Color;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

// http://www.geonames.org/source-code/javadoc/index.html?org/geonames/package-summary.html
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.Toponym;
import org.geonames.WebService;
import org.geonames.Style;

import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.SIRectangle;
import fr.inria.zvtm.glyphs.VText;

import au.com.bytecode.opencsv.CSVReader;

public class GeoNamesParser {
    
    static final Color SELECTED_FEATURE_COLOR = Color.ORANGE;
    static final Color FEATURE_COLOR = Color.GRAY;
    static final Color COUNTRY_LABEL_COLOR = Color.WHITE;

    static final String OUTPUT_CSV_SEP = ";";
    static final String OUTPUT_FILE_ENCODING = "UTF-8";

    static void getEntitiesFromWebService(String featureCode, int maxRows, String of){
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(of)), OUTPUT_FILE_ENCODING));
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setFeatureCode(featureCode);
            searchCriteria.setMaxRows(maxRows);
            searchCriteria.setStyle(Style.LONG);
            ToponymSearchResult searchResults = WebService.search(searchCriteria);
            for (Toponym toponym : searchResults.getToponyms()) {
                bw.write(toponym.getCountryName()+OUTPUT_CSV_SEP+toponym.getLatitude()+OUTPUT_CSV_SEP+toponym.getLongitude()+OUTPUT_CSV_SEP+toponym.getPopulation());
        	    bw.newLine();
            }
    	    bw.flush();
            System.out.println("\nReturned a total of " + searchResults.getTotalResultsCount()  + "results\n");
        }
        catch (Exception e){e.printStackTrace();} 
    }

    static void getEntitiesFromWebService(String fileName){
        try {
            getEntitiesFromWebService(new OutputStreamWriter(new FileOutputStream(new File(fileName)), OUTPUT_FILE_ENCODING));
        }
        catch (Exception e){e.printStackTrace();}
    }
    
    static void getEntitiesFromWebService(OutputStreamWriter osw){
        try {
            BufferedWriter bw = new BufferedWriter(osw);
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setFeatureCode("PPLA");
            searchCriteria.setMaxRows(200);
            ToponymSearchResult searchResult = WebService.search(searchCriteria);
            for (Toponym toponym : searchResult.getToponyms()) {
                bw.write(toponym.getName()+OUTPUT_CSV_SEP+toponym.getCountryName()+OUTPUT_CSV_SEP+toponym.getLatitude()+OUTPUT_CSV_SEP+toponym.getLongitude());
        	    bw.newLine();
            }
    	    bw.flush();
        }
        catch (Exception e){e.printStackTrace();} 
    }

    public static void main (String[] args){
        // args[0] = featureCode, args[1] = maxRows, args[2] = output file
        getEntitiesFromWebService(args[0], Integer.parseInt(args[1]), args[2]);
        System.exit(0);
    }
    
    /*------------------------------------------*/
    
    WorldExplorer application;
    
    GeoNamesParser(WorldExplorer app){
        this.application = app;
        loadCountries();
    }

    private static final char INPUT_CSV_SEP_CHAR = ';';

    void loadCountries(){
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(GeoNamesParser.class.getResourceAsStream("/countries.csv"), "UTF-8"),
                INPUT_CSV_SEP_CHAR);
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String country = nextLine[0];
                double lat = Double.parseDouble(nextLine[1]);
                double lon = Double.parseDouble(nextLine[2]);
                long population = Long.parseLong(nextLine[3]);
                float popFactor = (float)(2*Math.log10(population));
                VText t = new VText(GeoToolsManager.CC*lon, GeoToolsManager.CC*lat, 1, COUNTRY_LABEL_COLOR, country, VText.TEXT_ANCHOR_MIDDLE, popFactor);
                application.bSpace.addGlyph(t);
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }
    
    void loadCities(){
        try {
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setFeatureCode("PPLC");
            searchCriteria.setMaxRows(400);
            searchCriteria.setStyle(Style.FULL);
            ToponymSearchResult searchResult = WebService.search(searchCriteria);
            application.console.setText("Loaded "+searchResult.getTotalResultsCount()+" entities");
            for (Toponym toponym : searchResult.getToponyms()){
                ClosedShape g = new SIRectangle(GeoToolsManager.CC*toponym.getLongitude(), GeoToolsManager.CC*toponym.getLatitude(),
                                                2, 6, 6, FEATURE_COLOR);
                application.bSpace.addGlyph(g);
                g.setCursorInsideFillColor(Color.RED);
                g.setOwner(toponym);
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

}
