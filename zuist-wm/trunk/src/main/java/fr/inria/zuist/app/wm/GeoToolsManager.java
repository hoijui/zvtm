/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.net.MalformedURLException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.Feature;
import org.geotools.factory.GeoTools;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;

import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VPolygon;
import com.xerox.VTM.engine.LongPoint;

class GeoToolsManager {
    
    static final double CC = 21600 * 2 / 180.0;

    static final int LOD = 1;
    
    static final Color COUNTRY_BOUNDARY_COLOR = Color.YELLOW;

    WorldExplorer application;
    
    VPolygon[] countryBoundaries;
    
    // polygon 968 is Canada, and contains an error
    // polygon 1182 is Chile, and contains an error
    
    GeoToolsManager(WorldExplorer app){
        this.application = app;
        init();
    }

    void init(){
        int progress = 0;
        application.gp.setValue(0);
        application.gp.setLabel("Loading country boundaries...");
        File file = new File("data/shapefiles/world_borders.shp");
        Map connect = new HashMap();
        int newProgress = 0;
        try {
            connect.put("url", file.toURL());
            try {
                DataStore dataStore = DataStoreFinder.getDataStore(connect);
                String[] typeNames = dataStore.getTypeNames();
                String typeName = typeNames[0];
                FeatureCollection collection = dataStore.getFeatureSource(typeName).getFeatures();
                Feature[] features = (Feature[])collection.toArray();
                LongPoint[] zvtmCoords;
                Vector points = new Vector();
                countryBoundaries = new VPolygon[features.length];
                for (int i=0;i<features.length;i++){
                    Feature feature = features[i];
                    Geometry geometry = feature.getDefaultGeometry();
                    Coordinate[] coords = geometry.getCoordinates();
                    points.clear();
                    for (int j=0;j<coords.length-LOD;j+=LOD){
                        points.add(new LongPoint(Math.round(coords[j].x*CC), Math.round(coords[j].y*CC)));
                    }
                    zvtmCoords = new LongPoint[points.size()];
                    for (int j=0;j<zvtmCoords.length;j++){
                        zvtmCoords[j] = (LongPoint)points.elementAt(j);
                    }
                    countryBoundaries[i] = new VPolygon(zvtmCoords, Color.BLACK, COUNTRY_BOUNDARY_COLOR);
                    countryBoundaries[i].setFilled(false);
                    newProgress = i *100 / features.length;
                    if (newProgress > progress){
                        progress = newProgress;
                        application.gp.setValue(progress);
                    }
                }
                showAllCountries();
            }
            catch (IOException ioex){
                ioex.printStackTrace();
            }
        }
        catch(MalformedURLException uex){
            uex.printStackTrace();
        }
    }

    void showAllCountries(){
        for (int i=0;i<countryBoundaries.length;i++){
            application.vsm.addGlyph(countryBoundaries[i], application.bSpace, false);
        }
        application.vsm.repaintNow();
    }

    void showCountry(int i){
        if (i<countryBoundaries.length){
            application.vsm.addGlyph(countryBoundaries[i], application.bSpace);
        }
    }
    
    void toggleBoundaryDisplay(){
        application.bCamera.setEnabled(!application.bCamera.isEnabled());
    }

}
