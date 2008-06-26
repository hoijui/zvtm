/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.eval;

import java.awt.Point;
import java.awt.geom.AffineTransform;

import java.util.Vector;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

public class DistractorGenerator {

	/** Amplitude */
	static int A;
	/** Width */
	static int W;
	/** Density */
	static float D;
	/** Interspace */
	static int InS;
	
	/** Translate objects by x,y */
	static Point translate = new Point(0, 0);
	/** Direction of target w.r.t start point. Impacts position of distractors. */
	static float direction = 0;
	
	static final float DEG2RAD = (float)(2*Math.PI/360.0);
	
	/** Angle of cone containing main series of distractors (in rad). */
	static float coneAngle = 20.0f * DEG2RAD;

	/** Set generator's parameters.
		*@param a amplitude (distance from start point to target's center)
		*@param w width (target width)
		*@param d density of distractors on the path to the target (in [0.0,1.0])
		*@param ins interspace distance (edge-to-edge distance between target and closest distractor)
		*/
	public static void setParameters(int a, int w, float d, int ins){
		A = a;
		W = w;
		D = d;
		InS = ins;
	}
	
	public static void setTranslation(int x, int y){
		translate.x = x;
		translate.y = y;
	}
	
	public static void setDirection(float angle){
		direction = angle;
	}
	
	public static void setConeAngleDeg(float a){
		coneAngle = a * DEG2RAD;
	}

	public static void setConeAngleRad(float a){
		coneAngle = a;
	}
	
	/** Generate a target and set of distractors.
		*@return the (x,y) coordinates of the target and distractors.
		*/
	public static Point[] generate(){
		Vector tres = new Vector();
		// target
		tres.add(new Point(A, 0));
		// 4 surrounding distractors
		// +/- 2 * W/2 - InS, actually
		tres.add(new Point(A-W-InS, 0));
		tres.add(new Point(A+W+InS, 0));
		tres.add(new Point(A, -W-InS));
		tres.add(new Point(A, W+InS));
		// distractors in 20deg cone
		// distance from start point to first distractor
		int startToFirstDistractor = A - W - InS;
		int maxNumberOfDistractors = (startToFirstDistractor-W) / W - 1;
		int numberOfDistractors = Math.round(maxNumberOfDistractors * D);
		float step = startToFirstDistractor / ((float)numberOfDistractors);
		int x;
		double maxY;
		for (int i=1;i<numberOfDistractors;i++){
			// start at 1 as [0] is the position of the start point
			x = Math.round(i*step);
			maxY = x * Math.tan(coneAngle/2.0);
			tres.add(new Point(x, (int)(2*maxY*Math.random()-maxY)));
		}
		// other distractors
		// follow same principle but in two adjacent cones (one on each side, slightly longer)
		step *= 1.2f;
		AffineTransform at = AffineTransform.getTranslateInstance(0, 2*W);
		at.concatenate(AffineTransform.getRotateInstance(coneAngle*1.2));
		for (int i=1;i<numberOfDistractors;i++){
			x = Math.round(i*step);
			maxY = x * Math.tan(coneAngle/2.0);
			Point p = new Point();
			at.transform(new Point(x, (int)(2*maxY*Math.random()-maxY)), p);
			while (checkForOverlap(p, W, (Point[])tres.toArray(new Point[tres.size()]))){
				at.transform(new Point(x, (int)(2*maxY*Math.random()-maxY)), p);
			}			
			tres.add(p);
		}
		at = AffineTransform.getTranslateInstance(0, -2*W);
		at.concatenate(AffineTransform.getRotateInstance(-coneAngle*1.2));
		for (int i=1;i<numberOfDistractors;i++){
			x = Math.round(i*step);
			maxY = x * Math.tan(coneAngle/2.0);
			Point p = new Point();
			at.transform(new Point(x, (int)(2*maxY*Math.random()-maxY)), p);
			while (checkForOverlap(p, W, (Point[])tres.toArray(new Point[tres.size()]))){
				at.transform(new Point(x, (int)(2*maxY*Math.random()-maxY)), p);
			}
			tres.add(p);
		}

		// build result array		
		if (direction != 0 || translate.x != 0 || translate.y != 0){
			// translate and rotate if necessary
			at = AffineTransform.getTranslateInstance(translate.x, translate.y);
			at.concatenate(AffineTransform.getRotateInstance(direction));
			Point[] res = new Point[tres.size()];
			for (int i=0;i<tres.size();i++){
				res[i] = (Point)at.transform((Point)tres.elementAt(i), new Point());
			}
			return res;			
		}
		else {
			return (Point[])tres.toArray(new Point[tres.size()]);			
		}
	}
	
	static boolean checkForOverlap(Point p, int w, Point[] existingObjects){
		for (int i=0;i<existingObjects.length;i++){
			if (Math.sqrt(Math.pow(p.x-existingObjects[i].x,2)+Math.pow(p.y-existingObjects[i].y,2)) < w){
				System.out.print(i+ " "+Math.sqrt(Math.pow(p.x-existingObjects[i].x,2)+Math.pow(p.y-existingObjects[i].y,2)));
				
				return true;
			}
		}
		return false;
	}

	/** Generate a target and set of distractors and save them to a file.
	    * Generates the (x,y) coordinates of the target and distractors. All values separated by commas: x1,y,x2,y2,x3,y3,... 
		*@param f file name
		*/	
	public static String generateAsString(){
		String res = "";
		Point[] pres = generate();
		for (int i=0;i<pres.length-1;i++){
			res += pres[i].x + "," + pres[i].y + ",";
		}
		res += pres[pres.length-1].x + "," + pres[pres.length-1].y;
		return res;
	}
	
	/** Generate a target and set of distractors and save them to a file.
	    * Generates the (x,y) coordinates of the target and distractors. All values separated by commas: x1,y,x2,y2,x3,y3,... 
		*@param f file name
		*/	
	public static void generate(File f){
		try {
			BufferedWriter bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			bwt.write(generateAsString());
			bwt.newLine();
			bwt.flush();
		}
		catch (IOException ex){ex.printStackTrace();}
	}
	
	public static void main(String[] args){
		if (args.length > 3){
			DistractorGenerator.setParameters(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
				                              Float.parseFloat(args[2]), Integer.parseInt(args[3]));
		}
		else {
			System.out.println("Usage:\n\tjava DistractorGenerator <amplitude> <width> <density> <interspace> [output_file]");
			System.exit(0);
		}
		if (args.length > 4){
			DistractorGenerator.generate(new File(args[4]));
		}
		else {
			System.out.println(DistractorGenerator.generateAsString());
		}
	}

}
