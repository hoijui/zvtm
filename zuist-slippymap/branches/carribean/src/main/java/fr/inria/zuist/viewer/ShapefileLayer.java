/*   Copyright (c) INRIA, 2015. All Rights Reserved
 * $Id:  $
 */

package fr.inria.zuist.viewer;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
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
// import org.geotools.data.shapefile.shp.JTSUtilities;
// import org.geotools.geometry.jts.LiteShape;
import com.vividsolutions.jts.geom.Point;
// import com.vividsolutions.jts.geom.Geometry;
// import com.vividsolutions.jts.geom.Coordinate;
// import com.vividsolutions.jts.geom.Polygon;
// import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
// import com.vividsolutions.jts.geom.util.PolygonExtracter;
//
// import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.SICircle;
import fr.inria.zvtm.glyphs.BoatInfoG;
import fr.inria.zvtm.engine.VirtualSpace;


public class ShapefileLayer {

    static final double MIN_DISTANCE_BETWEEN_BOATS = 50;

    Point2D.Double NWcorner, SEcorner;

    double AX, BX, AY, BY;

    Vector<Boat> allBoats = new Vector();

    // sceneBounds as wnes
    public ShapefileLayer(double minLon, double maxLon, double minLat, double maxLat,
                          double[] sceneBounds){
        NWcorner = new Point2D.Double(minLon, maxLat);
        SEcorner = new Point2D.Double(maxLon, minLat);
        AX = (sceneBounds[2]-sceneBounds[0]) / ((double)(SEcorner.getX()-NWcorner.getX()));
        BX = (SEcorner.getX()*sceneBounds[0]-NWcorner.getX()*sceneBounds[2]) / ((double)(SEcorner.getX()-NWcorner.getX()));
        AY = (sceneBounds[3]-sceneBounds[1]) / ((double)(SEcorner.getY()-NWcorner.getY()));
        BY = (SEcorner.getY()*sceneBounds[1]-NWcorner.getY()*sceneBounds[3]) / ((double)(SEcorner.getY()-NWcorner.getY()));
    }

    void loadShapes(File shapeFile, VirtualSpace vs){
        Map connect = new HashMap();
        try {
            connect.put("url", shapeFile.toURI().toURL());
            try {
                DataStore dataStore = DataStoreFinder.getDataStore(connect);
                String[] typeNames = dataStore.getTypeNames();
                String typeName = typeNames[0];
                FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = dataStore.getFeatureSource(typeName).getFeatures();
                FeatureIterator<SimpleFeature> fi = featureCollection.features();
                while(fi.hasNext()){
                    SimpleFeature f = fi.next();
                    Point p = (Point)f.getAttribute(0);
                    double vx = AX * p.getX() + BX;
                    double vy = AY * p.getY() + BY;
                    boolean tooClose = false;
                    for (Boat boat:allBoats){
                        if (boat.distanceTo(vx,vy) < MIN_DISTANCE_BETWEEN_BOATS){
                            tooClose = true;
                            break;
                        }
                    }
                    if (!tooClose){
                        Boat boat = new Boat(vx, vy, f.getAttributes());
                        vs.addGlyph(boat.glyph);
                        allBoats.add(boat);
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

}

class Boat {

    // com.vividsolutions.jts.geom.Point lon/lat as a Point
    // java.lang.Double ID
    // java.lang.String Nom
    // java.lang.Double IMO
    // java.lang.Double MMSI
    // java.lang.String Callsign
    // java.lang.Double lat/Y
    // java.lang.Double lon/X
    // java.lang.String Type
    // java.lang.String Statut
    // java.lang.String Longueur
    // java.lang.Double nombre membres d'equipage
    // java.lang.Double nombre passagers
    // java.lang.String Exposition ("oui" / "non") presence dans la zone a evacuer
    // java.lang.Integer TTT_Min temps
    // java.lang.Integer TOT_HUM1
    // java.lang.Double GreenL Amplitude tsunami
    // java.lang.Double SS_GL Amplitude tsuname sans loi de Green

    String name, callsign;
    double lat, lon;
    String type, status;
    double length;
    int nb_crew, nb_passengers;
    boolean exposed;
    int tti;

    // coords in zvtm virtual space for overlay on the map
    double vx, vy;

    BoatInfoG glyph;

    Boat(double vx, double vy, List<Object> attribs){
        this.vx = vx;
        this.vy = vy;
        name = (String)attribs.get(2);
        callsign = (String)attribs.get(5);
        lat = ((Double)attribs.get(6)).doubleValue();
        lon = ((Double)attribs.get(7)).doubleValue();
        type = (String)attribs.get(8);
        status = (String)attribs.get(9);
        nb_crew = ((Double)attribs.get(11)).intValue();
        nb_passengers = ((Double)attribs.get(12)).intValue();
        exposed = ((String)attribs.get(13)).toLowerCase().equals("oui");
        tti = ((Integer)attribs.get(14)).intValue();
        glyph = new BoatInfoG(vx, vy, 10, type, name + " (" + callsign + ")",
                              nb_crew + " / " + nb_passengers, tti + " mn", exposed);
        glyph.setOwner(this);
    }

    public double distanceTo(double x, double y){
        return Math.sqrt((x-vx)*(x-vx)+(y-vy)*(y-vy));
    }

    public String toString(){
        String res = name + " ("+ callsign + ")\n";
        res += "Type: " + type + "\n";
        res += "Status: " + status  + "\n";
        res += "Crew/Passengers: " + nb_crew + " / " + nb_passengers + "\n";
        res += "Position: " + lon + " " + lat + "\n";
        res += "Time to impact (min): " + tti + "\n";
        res += "Exposed: " + exposed + "\n";
        return res;
    }

}
