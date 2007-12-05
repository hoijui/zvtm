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
import org.geotools.data.shapefile.shp.JTSUtilities;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VPolygon;
import com.xerox.VTM.engine.LongPoint;

import fr.inria.zuist.engine.PolygonDescription;
import fr.inria.zuist.engine.Region;

class GeoToolsManager {
    
    static final double CC = 21600 * 2 / 180.0;

    static final int LOD = 1;
    
    static final Color COUNTRY_BOUNDARY_COLOR = Color.YELLOW;

    WorldExplorer application;
    
    PolygonDescription[] countryBoundaries;
    
    static final String[] transitions = {Region.APPEAR_STR, Region.APPEAR_STR, Region.DISAPPEAR_STR, Region.DISAPPEAR_STR};

    int polygonID = 0;
    
    GeoToolsManager(WorldExplorer app){
        this.application = app;
        Region region = application.sm.createRegion(0, 0, 84600, 43200, 3, "BR0", 1,
                                                    transitions, Region.ORDERING_DISTANCE_STR,
                                                    false, "Boundaries", null, null);

        load(new File("data/shapefiles/misc/countries.shp"), "Loading countries...", region);
//        load(new File("data/shapefiles/ca_provinces/province.shp"), "Loading Canadian provinces...", region);
//        load(new File("data/shapefiles/us_states/statesp020.shp"), "Loading US states...", region);
//        load(new File("data/shapefiles/mx_states/mx_state.shp"), "Loading Mexican states...", region);
//        load(new File("data/shapefiles/russia/RUS1.shp"), "Loading Russian administrative divisions...", region);
//        load(new File("data/shapefiles/china/CHN0.shp"), "Loading Chinese administrative divisions...", region);
    }

    void load(File shapeFile, String msg, Region region){
        int progress = 0;
        application.gp.setValue(0);
        application.gp.setLabel(msg);
        Map connect = new HashMap();
        int newProgress = 0;
        try {
            connect.put("url", shapeFile.toURL());
            try {
                DataStore dataStore = DataStoreFinder.getDataStore(connect);
                String[] typeNames = dataStore.getTypeNames();
                String typeName = typeNames[0];
                FeatureCollection collection = dataStore.getFeatureSource(typeName).getFeatures();
                Feature[] features = (Feature[])collection.toArray();
                LongPoint[] zvtmCoords;
                Vector points = new Vector();
                countryBoundaries = new PolygonDescription[features.length];
                for (int i=0;i<features.length;i++){
                    Feature feature = features[i];
                    Geometry geometry = feature.getPrimaryGeometry();                    
                    Object[] polygons = PolygonExtracter.getPolygons(geometry).toArray();
                    for (int k=0;k<polygons.length;k++){
                    
                        Geometry simplifiedPolygon = DouglasPeuckerSimplifier.simplify((Geometry)polygons[k], 0.00001);
                        //Geometry simplifiedPolygon = (Geometry)polygons[k];
                        Coordinate[] coords = simplifiedPolygon.getCoordinates();
                        points.clear();
                        for (int j=0;j<coords.length;j+=1){
                            points.add(new LongPoint(Math.round(coords[j].x*CC), Math.round(coords[j].y*CC)));
                        }
                        zvtmCoords = new LongPoint[points.size()];
                        for (int j=0;j<zvtmCoords.length;j++){
                            zvtmCoords[j] = (LongPoint)points.elementAt(j);
                        }
                        application.sm.createPolygon(zvtmCoords, "B"+Integer.toString(polygonID++), region,
                            false, null, Color.YELLOW);
                    }
                    newProgress = i *100 / features.length;
                    if (newProgress > progress){
                        progress = newProgress;
                        application.gp.setValue(progress);
                    }
                }
            }
            catch (IOException ioex){
                ioex.printStackTrace();
            }
        }
        catch(MalformedURLException uex){
            uex.printStackTrace();
        }
    }
    
    void toggleBoundaryDisplay(){
        application.bCamera.setEnabled(!application.bCamera.isEnabled());
    }

}
