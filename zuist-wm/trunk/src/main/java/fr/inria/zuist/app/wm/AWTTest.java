package fr.inria.zuist.app.wm;

import java.awt.*;
import javax.swing.*;

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
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

public class AWTTest extends JFrame {
    
    static final double CC = 1280 / 360.0;
    
    GeoPanel p;
    
    Polygon[] countryBoundaries;
    
    boolean paint = false;
    
    public AWTTest(String shapeFilePath){
        super();
        p = new GeoPanel(this);
        this.add(p);
        this.setSize(1280,1280/2);
        this.setVisible(true);        
        load(new File(shapeFilePath));
    }

    void load(File shapeFile){
        Map connect = new HashMap();
        try {
            connect.put("url", shapeFile.toURI().toURL());
            try {
                DataStore dataStore = DataStoreFinder.getDataStore(connect);
                String[] typeNames = dataStore.getTypeNames();
                String typeName = typeNames[0];
                FeatureCollection collection = dataStore.getFeatureSource(typeName).getFeatures();
                Feature[] features = (Feature[])collection.toArray();

                Vector awtPolygons = new Vector();
                // for each feature
                for (int i=0;i<features.length;i++){
                    Feature feature = features[i];
                    Geometry geometry = feature.getDefaultGeometry();
                    Object[] polygons = PolygonExtracter.getPolygons(geometry).toArray();
                    // for each polygon in the MultiPolygon
                    for (int j=0;j<polygons.length;j++){
                        Geometry simplifiedPolygon = (Geometry)polygons[j];
                        Coordinate[] coords = simplifiedPolygon.getCoordinates();
                        Vector points = new Vector();
                        // add each x,y coord pair to a list of pairs that will be used to create an AWT polygon
                        for (int k=0;k<coords.length;k+=1){
                            points.add(new Point((int)Math.round(coords[k].x*CC), (int)Math.round(coords[k].y*CC)));
                        }
                        int[] x = new int[points.size()];
                        int[] y = new int[points.size()];
                        for (int k=0;k<points.size();k++){
                            x[k] = 640+((Point)points.elementAt(k)).x;
                            y[k] = 300-((Point)points.elementAt(k)).y;
                        }
                        awtPolygons.add(new Polygon(x, y, points.size()));
                    }
                }
                countryBoundaries = new Polygon[awtPolygons.size()];
                for (int h=0;h<awtPolygons.size();h++){
                    countryBoundaries[h] = (Polygon)awtPolygons.elementAt(h);
                }
                p.cb = countryBoundaries;
                repaint();
            }
            catch (IOException ioex){
                ioex.printStackTrace();
            }
        }
        catch(MalformedURLException uex){
            uex.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        System.out.println("Using GeoTools v" + GeoTools.getVersion());
        new AWTTest(args[0]);
    }

}

class GeoPanel extends JPanel {
    
    AWTTest application;
    Polygon[] cb;
    
    GeoPanel(AWTTest app){
        this.application = app;
    }
    
    public void paintComponent(Graphics g){
        if (cb != null){
            g.setColor(Color.BLACK);
            for (int i=0;i<cb.length;i++){
                g.drawPolygon(cb[i]);
            }
        }
    }
    
}