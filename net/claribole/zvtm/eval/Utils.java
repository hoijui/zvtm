/*   FILE: Utils.java
 *   DATE OF CREATION:  Mon Jan 23 09:21:27 2006
 *   AUTHOR :           Caroline Appert (appert@lri.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Utils.java,v 1.3 2006/04/05 06:26:01 epietrig Exp $
 */ 
 
package net.claribole.zvtm.eval;

import java.io.*;

/**
 * @author Caroline Appert
 */
public class Utils {

    /** computes a series of trials based on a given set of IDs and target widths
     *
     *@param ids the set of IDs expected
     *@param ws the set of target widths expected
     *@param repeat how many times each trial should appear in the series
     *@param center true if targets should be globally centered around coords (0,0)
     *@return a matrix in which each row corresponds to a trial; each row contains 4 values: ID, target distance, target width, absolute location of target
     */
    public static double[][] computeTrialsSeries(int[] ids, int[] ws, int repeat, boolean center){
	int nbTrials = ids.length*ws.length*repeat;
	double[][] trials = new double[nbTrials][4];
	int indexTrial = 0;
	nbTrials--;
	for(int i = 0; i < ids.length; i++) {
	    for(int j = 0; j < ws.length; j++) {
		for(int k = 0; k < repeat; k++) {
		    indexTrial = (int)(Math.random()*nbTrials);
		    nbTrials--;
		    int m = -1;
		    while(indexTrial>=0) {
			m++;
			if(trials[m][0] == 0) indexTrial--;
		    }
		    trials[m][0] = ids[i];
		    trials[m][1] = (Math.pow(2, ids[i]) - 1)*ws[j];
		    trials[m][2] = ws[j];
		}
	    }
	}
	if(center) centerTrials(trials);
	return trials;
    }

    /** centers the items of a trial series around (0,0)
     *
     *@param trials the result of method computeTrialsSeries() as input
     *@return a matrix in which each row corresponds to a trial; each row contains 4 values: ID, target distance, target width, absolute location of target
     */
    public static void centerTrials(double[][] trials) {
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;
	double y = 0;
	for(int i = 0; i < trials.length; i++) {
	    if(i%2 == 0) y += trials[i][1];
	    else y -= trials[i][1];
	    trials[i][3] = y;
	    min = Math.min(min, y);
	    max = Math.max(max, y);
	}
	double middle = (min+max)/2;
	for(int i = 0; i < trials.length; i++)
	    trials[i][3] = trials[i][3] - middle;
    }
    
    /** export a trial series to a file
     *
     */
    public static void exportTrials(double[][] trials, String fileDestination){
	DataOutputStream dosJavaReadable, dosHumanReadbale;
	try {
	    dosJavaReadable = new DataOutputStream(new FileOutputStream("java"+fileDestination));
	    dosHumanReadbale = new DataOutputStream(new FileOutputStream("human"+fileDestination));
	    dosHumanReadbale.writeChars("ID\tD\tW\tLocation\n");
	    for(int i = 0; i < trials.length; i++){
		for(int j = 0; j < trials[i].length; j++){
		    dosJavaReadable.writeDouble(trials[i][j]);
		    dosHumanReadbale.writeChars(trials[i][j]+"\t");
		}
		dosHumanReadbale.writeChars("\n");
	    }
	    dosJavaReadable.close();
	    dosHumanReadbale.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    /** import a trial series from a file
     *
     *@return a matrix in which each row corresponds to a trial; each row contains 4 values: ID, target distance, target width, absolute location of target
     */
    public static double[][] importTrials(int nbTrials, String fileSource){
	DataInputStream dis;
	double[][] trials = new double[nbTrials][4];
	try {
	    dis = new DataInputStream(new FileInputStream(fileSource));
	    for(int i = 0; i < nbTrials; i++)
		for(int j = 0; j < 4; j++)
		    trials[i][j] = dis.readDouble();
	    dis.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return trials;
    }
    
    public static boolean test(){
	int[] ids = {5, 10, 15, 20};
	int[] ws = {200, 400, 600};
	int repeat = 3;
	double[][] trialsComputed = computeTrialsSeries(ids, ws, repeat, true);
	exportTrials(trialsComputed, "test.txt");
	double[][] trialsLoaded = importTrials(ids.length*ws.length*repeat, "javatest.txt");
	boolean tabEqual = trialsLoaded.length == trialsComputed.length;
	for(int i = 0; i < trialsLoaded.length; i++) {
	    boolean lineEqual = true;
	    for(int j = 0; j <trialsLoaded[i].length; j++)
		lineEqual = lineEqual && (trialsLoaded[i][j] == trialsComputed[i][j]);
	    tabEqual = tabEqual && lineEqual;
	}
	return tabEqual;
    }
    
    static final String miscInfo = "Java VM Info: " + System.getProperty("java.vm.vendor")
	+ " " + System.getProperty("java.vm.name")
	+ " " + System.getProperty("java.vm.version")
	+"\nOS Info: " + System.getProperty("os.name")
	+ " " + System.getProperty("os.version")
	+ " / " + System.getProperty("os.arch")
	+ " " + System.getProperty("sun.cpu.isalist") + "\n";

    public static void main(String[] args){
	System.out.println("test : "+test());
    }
}
