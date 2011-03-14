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
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

class GeoToolsManager {
    
    static final double CC = 21600 * 2 / 180.0;

    static final Color COUNTRY_COLOR = new Color(245,255,157);
    static final Color COUNTRY_FILL_HIGHLIGHT_COLOR = Color.GREEN;
    static final Color ADMIN_DIV_1_COLOR = new Color(151,255,151);
    
    /* type of administrative division (0=country, 1= state, province, ...) */
    static final String LAD0 = "AD0";
    static final String LAD1 = "AD1";
    static final String CTRY = "CTRY";
    static final String CITY = "CITY";

    WorldExplorer application;
    GeoNamesParser gnp;
    
    static final short[] transitions = {Region.APPEAR, Region.APPEAR, Region.DISAPPEAR, Region.DISAPPEAR};

    int polygonID = 0;
    
    boolean isShowing = false;
    
    GeoToolsManager(WorldExplorer app, boolean queryGN, short lad){
        this.application = app;
        gnp = new GeoNamesParser(application);
        if (lad >= 0){
            loadShapes(new File("data/TM_WORLD_BORDERS-0.3.shp"), "Loading countries...", COUNTRY_COLOR, LAD0);
            gnp.loadCountries();
        }
        if (lad >= 1){
            loadShapes(new File("data/shapefiles/ca_provinces/province.shp"), "Loading Canadian provinces...", ADMIN_DIV_1_COLOR, LAD1);
            loadShapes(new File("data/shapefiles/us_states/statesp020.shp"), "Loading US states...", ADMIN_DIV_1_COLOR, LAD1);
            loadShapes(new File("data/shapefiles/mx_states/mx_state.shp"), "Loading Mexican states...", ADMIN_DIV_1_COLOR, LAD1);
            loadShapes(new File("data/shapefiles/russia/RUS1.shp"), "Loading Russian administrative divisions...", ADMIN_DIV_1_COLOR, LAD1);
            loadShapes(new File("data/shapefiles/china/CHN1.shp"), "Loading Chinese administrative divisions...", ADMIN_DIV_1_COLOR, LAD1);
        }
        if (queryGN){
            loadEntities();            
        }
    }

    void loadShapes(File shapeFile, String msg, Color shapeColor, String bt){
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
                                VPolygon polygon = new VPolygon(zvtmCoords, 0, COUNTRY_FILL_HIGHLIGHT_COLOR, shapeColor, 1.0f);
                                polygon.setFilled(false);
                                polygon.setType(bt);
                                application.bSpace.addGlyph(polygon);
                                //application.sm.createClosedShapeDescription(polygon, "B"+Integer.toString(polygonID++),
                                //    polygon.getZindex(),
                                //    region, false);
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
        isShowing = true;
    }
    
    void toggleCountryDisplay(){
        showCountries(!isShowing);
    }
    
    void showCountries(boolean b){
        Vector<Glyph> boundaries = application.bSpace.getGlyphsOfType(LAD0);
        for (final Glyph g:boundaries){
            if (b){
                application.bSpace.show(g);
            }
            else {
                application.bSpace.hide(g);                
            }
        }
        boundaries = application.bSpace.getGlyphsOfType(LAD1);
        for (final Glyph g:boundaries){
            if (b){
                application.bSpace.show(g);
            }
            else {
                application.bSpace.hide(g);                
            }
        }
        Vector<Glyph> ctryNames = application.bSpace.getGlyphsOfType(CTRY);
        for (final Glyph g:ctryNames){
            if (b){
                application.bSpace.show(g);
                Animation a = application.vsm.getAnimationManager().getAnimationFactory().createTranslucencyAnim(NavigationManager.ANIM_MOVE_DURATION, g,
                    1f, false, IdentityInterpolator.getInstance(), null);
                application.vsm.getAnimationManager().startAnimation(a, false);
            }
            else {
                Animation a = application.vsm.getAnimationManager().getAnimationFactory().createTranslucencyAnim(NavigationManager.ANIM_MOVE_DURATION, g,
                    0, false, IdentityInterpolator.getInstance(),
                    new EndAction(){
                        public void	execute(Object subject, Animation.Dimension dimension){
                            application.bSpace.hide(g);
                        }
                    });
                application.vsm.getAnimationManager().startAnimation(a, false);
            }
        }
        isShowing = b;
    }
    
    void loadEntities(){
        application.gp.setValue(10);
        application.gp.setLabel("Loading GeoNames features");
        gnp.loadCities();
        application.gp.setValue(100);
    }

}
