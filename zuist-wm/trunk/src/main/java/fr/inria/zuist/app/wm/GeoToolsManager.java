/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.awt.Color;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.net.MalformedURLException;

// GeoTools
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.factory.GeoTools;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.geometry.jts.LiteShape;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zuist.engine.Region;

class GeoToolsManager {
    
    static final double CC = 21600 * 2 / 180.0;

    static final Color COUNTRY_COLOR = Color.YELLOW;
    static final Color ADMIN_DIV_1_COLOR = Color.GREEN;

    WorldExplorer application;
    GeoNamesParser gnp;
    
    static final short[] transitions = {Region.APPEAR, Region.APPEAR, Region.DISAPPEAR, Region.DISAPPEAR};

    int polygonID = 0;
    
    GeoToolsManager(WorldExplorer app, boolean queryGN, boolean loadAdminDiv1){
        this.application = app;
        Region region = application.sm.createRegion(0, 0, 84600, 43200, 0, 4, "BR0", "Boundaries",
                                                    1, transitions, Region.ORDERING_DISTANCE,
                                                    false, null, null);

        //loadShapes(new File("data/TM_WORLD_BORDERS-0.3.shp"), "Loading countries...", region, COUNTRY_COLOR);
        if (loadAdminDiv1){
            loadShapes(new File("data/shapefiles/ca_provinces/province.shp"), "Loading Canadian provinces...", region, ADMIN_DIV_1_COLOR);
            loadShapes(new File("data/shapefiles/us_states/statesp020.shp"), "Loading US states...", region, ADMIN_DIV_1_COLOR);
            loadShapes(new File("data/shapefiles/mx_states/mx_state.shp"), "Loading Mexican states...", region, ADMIN_DIV_1_COLOR);
            loadShapes(new File("data/shapefiles/russia/RUS1.shp"), "Loading Russian administrative divisions...", region, ADMIN_DIV_1_COLOR);
            loadShapes(new File("data/shapefiles/china/CHN1.shp"), "Loading Chinese administrative divisions...", region, ADMIN_DIV_1_COLOR);            
        }
        gnp = new GeoNamesParser(application);
        if (queryGN){
            loadEntities();            
        }
    }

    void loadShapes(File shapeFile, String msg, Region region, Color shapeColor){
        int progress = 0;
        application.gp.setValue(0);
        application.gp.setLabel(msg);
        Map connect = new HashMap();
        int newProgress = 0;
        try {
            connect.put("url", shapeFile.toURI().toURL());
            try {
                DataStore dataStore = DataStoreFinder.getDataStore(connect);
                String[] typeNames = dataStore.getTypeNames();
                String typeName = typeNames[0];
                FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = dataStore.getFeatureSource(typeName).getFeatures();
                FeatureIterator<SimpleFeature> fi = featureCollection.features();
                Vector<Polygon> awtPolygons = new Vector<Polygon>();
                Vector points = new Vector();
                Point2D.Double[] zvtmCoords;
                int i = 0;
                while(fi.hasNext()){
                    SimpleFeature f = fi.next();
                    Geometry geometry = (Geometry)f.getDefaultGeometry();                    
                    Object[] polygons = PolygonExtracter.getPolygons(geometry).toArray();
                    for (int k=0;k<polygons.length;k++){
                        Geometry simplifiedPolygon = DouglasPeuckerSimplifier.simplify((Geometry)polygons[k], 0.01);
                        PathIterator pi = (new LiteShape(simplifiedPolygon, null, false)).getPathIterator(null);
                        double[] coords = new double[6];
                        int type;
                        Vector shapes = new Vector();
                        while (!pi.isDone()){
                            type = pi.currentSegment(coords);
                            if (type == PathIterator.SEG_LINETO){
                                points.add(new Point2D.Double(coords[0]*CC, coords[1]*CC));
                            }
                            else if (type == PathIterator.SEG_MOVETO){
                                points.clear();
                            }
                            else if (type == PathIterator.SEG_CLOSE){
                                zvtmCoords = new Point2D.Double[points.size()];
                                for (int j=0;j<zvtmCoords.length;j++){
                                    zvtmCoords[j] = (Point2D.Double)points.elementAt(j);
                                }
                                VPolygon polygon = new VPolygon(zvtmCoords, 0, Color.BLACK, shapeColor, 1.0f);
                                polygon.setFilled(false);
                                application.sm.createClosedShapeDescription(polygon, "B"+Integer.toString(polygonID++),
                                    polygon.getZindex(),
                                    region, false);
                            }
                            else {
                                System.err.println("Error: GeoToolsManager.loadShape: Unsupported path iterator element type:" + type);
                            }
                            pi.next();
                        }
                    }
                    newProgress = (i++) * 100 / featureCollection.size();
                    if (newProgress > progress){
                        progress = newProgress;
                        application.gp.setValue(progress);
                    }
                }
                fi.close();
            }
            catch(MalformedURLException uex){
                uex.printStackTrace();
            }
        }
        catch (IOException ioex){
            ioex.printStackTrace();
        }
    }
    
    void toggleBoundaryDisplay(){
        application.bCamera.setEnabled(!application.bCamera.isEnabled());
    }
    
    void loadEntities(){
        application.gp.setValue(10);
        application.gp.setLabel("Loading GeoNames features");
        gnp.loadCities();
        application.gp.setValue(100);
    }

}
