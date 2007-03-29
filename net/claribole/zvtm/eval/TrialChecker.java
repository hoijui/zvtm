/*   FILE: TrialChecker.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.eval;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Vector;

class TrialChecker {

    Vector dataSet = new Vector();

    TrialChecker(){
	try {
	    FileInputStream fis = new FileInputStream(new File("/Users/epietrig/projects/vtm/data/finalCitySet.csv"));
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    String line = br.readLine();
	    while (line != null){
		dataSet.add(line.split(";")[0]);
		line = br.readLine();
	    }
	    check("/Users/epietrig/projects/vtm/trials/all-cities-ich.csv", "ICH");
	    check("/Users/epietrig/projects/vtm/trials/all-cities-icm.csv", "ICM");
	    check("/Users/epietrig/projects/vtm/trials/all-cities-icl.csv", "ICL");
	}
	catch (Exception ex){ex.printStackTrace();}
    }
    
    void check(String file, String ic){
	try {
	    System.out.print(ic + ": ");
	    FileInputStream fis = new FileInputStream(new File(file));
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    String line = br.readLine();
	    String city;
	    while (line != null){
		city = line.split(";")[0];
		if (!dataSet.contains(city)){System.out.println(city);}
		else {System.out.print(".");}
		line = br.readLine();
	    }
	    System.out.println();
	}
	catch (Exception ex){ex.printStackTrace();}
    }
    
    public static void main(String[] args){
 	new TrialChecker();
    }

}