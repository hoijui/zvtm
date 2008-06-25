/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.eval;

import java.awt.Point;

import java.util.Vector;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

public class DistractorGenerator {

	// amplitude
	static int A;
	// width
	static int W;
	// density
	static float D;
	// interspace
	static int InS;

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
		// distance from start point edge to first distractor edge
		// - 4 * W/2 - InS, actually
		int distractorInterval = A - 2*W - InS;
		float maxNumberOfDistractors = distractorInterval / ((float)W);
		int numberOfDistractors = Math.round(maxNumberOfDistractors * D);
		float step = distractorInterval / ((float)numberOfDistractors);
		for (int i=0;i<numberOfDistractors;i++){
			tres.add(new Point(Math.round(W+i*step), 0));
		}
		// other distractors
		
		return (Point[])tres.toArray(new Point[tres.size()]);
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
