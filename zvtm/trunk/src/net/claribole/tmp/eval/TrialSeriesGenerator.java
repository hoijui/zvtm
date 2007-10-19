/*   FILE: LocateTask.java
 *   DATE OF CREATION:  Thu May 04 10:05:06 2006
 *   AUTHOR :           Caroline Appert (appert@lri.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class TrialSeriesGenerator {
    
    /*
     * Input files
     * 1: id
     * 2, 3, 4: city, province/state, country
     * 5, 6: lat, long [city]
     */
	
    /*
     * Output files
     * 1: id
     * 2, 3, 4: lat, long, alt [starting point of the camera]
     * 5, 6, 7: city name, province/state, country
     * 8, 9: lat, long [city]
     */
	
    static BufferedWriter bw;
    static final int MAIN_MAP_WIDTH = 8000;
    static final int MAIN_MAP_HEIGHT = 4000;
    static final int MAP_WIDTH = (int)Math.round(MAIN_MAP_WIDTH * MapData.MN000factor.doubleValue());
    static final int MAP_HEIGHT = (int)Math.round(MAIN_MAP_HEIGHT * MapData.MN000factor.doubleValue());
	
    public static void startingPoint(int id, String nameCity, String state, String country, int latCity, int longCity, int widthTarget) {
	Point p = getStartLocation(id, latCity, longCity, widthTarget);
	try {
	    if(p != null) {
		bw.write(id+
			 ";"+p.y+";"+p.x+";0"+
			 ";"+nameCity+";"+state+";"+country+
			 ";"+latCity+";"+longCity+"\n");
// 		System.out.println(id+
// 				   ";"+latInZVTM+";"+longInZVTM+";0"+
// 				   ";"+nameCity+";"+state+";"+country+
// 				   ";"+latCity+";"+longCity);
// 		System.out.println("ID: "+Math.log(1+(new Point2D.Double(p.x, p.y).distance(longCity, latCity))/widthTarget)/Math.log(2));
	    } else {
		bw.write(id+
			 ";NaN;NaN;NaN"+
			 ";"+nameCity+";"+state+";"+country+
			 ";"+latCity+";"+longCity+"\n");
// 		System.out.println(id+
// 				   ";"+nameCity+";"+state+";"+country+
// 				   ";"+nameCity+
// 				   ";"+latCity+";"+longCity);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    static Point getStartLocation(int id, int latCity, int longCity, int widthTarget){
	int x = -1;
	int y = -1;
		
	// compute the distance to fit the specific id
	double distanceTarget = (Math.pow(2, id) - 1) * widthTarget;
		
	int latInGraphics = MAP_HEIGHT/2  - latCity;
	int longInGraphics = longCity + MAP_WIDTH/2;
		
// 	System.out.println(
// 			   "("+latCity
// 			   +","+longCity+") - "
// 			   +"("+latInGraphics
// 			   +","+longInGraphics+")"
// 			   );
		
	// just take the outline of the circle around the target city
	Area circle = new Area(new Ellipse2D.Double(longInGraphics - distanceTarget, latInGraphics - distanceTarget, 2*distanceTarget, 2*distanceTarget));
	circle.exclusiveOr(new Area(new Ellipse2D.Double(longInGraphics - distanceTarget + 1, latInGraphics - distanceTarget + 1, 2*distanceTarget-2, 2*distanceTarget-2)));
		
	// just take the intersection of the interior of the map
	Area map = new Area(new Rectangle2D.Double(1024/2, 768/2, MAP_WIDTH - 1024, MAP_HEIGHT - 768));
	map.intersect(circle);
		
	double[] coords = new double[6];
		
	// count number of segments on the outline of the resulting shape
	PathIterator pi = map.getPathIterator(new AffineTransform());
	int seg = 0;
	while(!pi.isDone()) {
	    if(pi.currentSegment(coords) != PathIterator.SEG_MOVETO)
		seg++;
	    pi.next();
			
	}
		
	seg = (int)(Math.random()*(seg - 1));
		
	int s = 0;
		
	// selecting an extremity of one of the segment belonging to the resulting shape to obtain the starting point
	pi = map.getPathIterator(new AffineTransform());
		
	boolean hasFound = false;
	while(!pi.isDone()) {
	    pi.currentSegment(coords);
	    if(pi.currentSegment(coords) != PathIterator.SEG_MOVETO)
		s++;
	    //			System.out.println("coords: "+coords[0]+", "+coords[1]);
	    if(s==seg) {
		hasFound = true;
		x = (int)coords[0];
		y = (int)coords[1];
	    }
	    pi.next();
			
	}
		
// 	int longInZVTM = x - MAP_WIDTH/2;
// 	int latInZVTM = -(y - MAP_HEIGHT/2);
	if (hasFound){
	    return new Point(x - MAP_WIDTH/2, -(y - MAP_HEIGHT/2));
	}
	else {
	    return null;
	}
    }
	
    public static void parseFileInput(String fileInput, int widthTarget) {
	try {
	    BufferedReader br = new BufferedReader(
						   new InputStreamReader(
									 new FileInputStream(fileInput),"UTF-8"));
	    String line = br.readLine();
	    String[] lineBroken;
	    while (line != null) {
		lineBroken = line.split(";");
		line = br.readLine();
		startingPoint(
			      Integer.parseInt(lineBroken[0]), 
			      lineBroken[1],
			      lineBroken[2],
			      lineBroken[3],
			      Integer.parseInt(lineBroken[4]),
			      Integer.parseInt(lineBroken[5]),
			      widthTarget); 
	    }
	    br.close();
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
				
    }

    public static void main(String[] args) {
	if(args.length != 2)
	    System.err.println("usage: TrialSeriesGenerator inputFile outputFile");
	try {
	    bw = new BufferedWriter(new OutputStreamWriter(
							   new FileOutputStream(args[1]), "UTF8"));
	    parseFileInput(args[0], (int)GeoDataStore.TARGET_WIDTH);
	    bw.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
