/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zuist.app.wm;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.Feature;
import org.geotools.factory.GeoTools;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;

import com.xerox.VTM.glyphs.VSegment;

class GeoToolsManager {
    
    static final double CC = 21600 * 2 / 180.0;

    static final int LOD = 2;

    WorldExplorer application;
    
    GeoToolsManager(WorldExplorer app){
        this.application = app;
        init();
    }

    void init(){
        try {
            File file = new File("data/shapefiles/world_borders.shp");

            Map connect = new HashMap();
            connect.put("url", file.toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(connect);
            String[] typeNames = dataStore.getTypeNames();
            String typeName = typeNames[0];
            FeatureSource featureSource = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = featureSource.getFeatures();
            FeatureIterator iterator = collection.features();
            int length = 0;
            try {
                while(iterator.hasNext()){
                    Feature feature = iterator.next();
                    Geometry geometry = feature.getDefaultGeometry();
                    
                    Coordinate[] coords = geometry.getCoordinates();
                    for (int i=0;i<coords.length-LOD;i+=LOD){
                        application.vsm.addGlyph(new VSegment(Math.round(coords[i].x*CC), Math.round(coords[i].y*CC),
                                                              0, java.awt.Color.YELLOW,
                                                              Math.round(coords[i+LOD].x*CC), Math.round(coords[i+LOD].y*CC)),
                                                 application.mSpace);
                    }
                    
                }
            }
            finally {
                iterator.close();
            }
            System.out.println( "Total length "+length );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}