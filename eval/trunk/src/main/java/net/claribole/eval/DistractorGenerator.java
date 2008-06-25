/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.eval;

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
		*@return the (x,y) coordinates of the target and distractors. All values separated by commas: x1,y,x2,y2,x3,y3,... 
		*/
	public static String generate(){
		String res = "";
		
		return res;
	}
	
	/** Generate a target and set of distractors and save them to a file.
	    * Generates the (x,y) coordinates of the target and distractors. All values separated by commas: x1,y,x2,y2,x3,y3,... 
		*@param f file name
		*/	
	public static void generate(File f){
		try {
			BufferedWriter bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			bwt.write(generate());
			bwt.newLine();
			bwt.flush();
		}
		catch (IOException ex){ex.printStackTrace();}
	}

}
