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

import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.Toponym;
import org.geonames.WebService;
import org.geonames.Style;

import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.RectangleNR;

public class GeoNamesParser {
    
    static final Color SELECTED_FEATURE_COLOR = Color.ORANGE;
    static final Color FEATURE_COLOR = Color.GRAY;

    static final String OUTPUT_CSV_SEP = "\t";
    static final String OUTPUT_FILE_ENCODING = "UTF-8";

    static ToponymSearchResult getEntitiesFromWebService(String featureCode, int maxRows){
        try {
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setFeatureCode(featureCode);
            searchCriteria.setMaxRows(maxRows);
            return WebService.search(searchCriteria);            
        }
        catch (Exception e){e.printStackTrace();return null;} 
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
        getEntitiesFromWebService(args[0]);
        System.exit(0);
    }
    
    WorldExplorer application;
    
    GeoNamesParser(WorldExplorer app){
        this.application = app;
    }
    
    void loadFeatures(){
        try {
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setFeatureCode("PPLC");
            searchCriteria.setMaxRows(400);
            searchCriteria.setStyle(Style.FULL);
            ToponymSearchResult searchResult = WebService.search(searchCriteria);
            application.console.setText("Loaded "+searchResult.getTotalResultsCount()+" entities");
            for (Toponym toponym : searchResult.getToponyms()){
                ClosedShape g = new RectangleNR(Math.round(GeoToolsManager.CC*toponym.getLongitude()), Math.round(GeoToolsManager.CC*toponym.getLatitude()),
                                                1, 6, 6, FEATURE_COLOR);
                application.bSpace.addGlyph(g);
                g.setCursorInsideFillColor(Color.RED);
                g.setOwner(toponym);
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

}
