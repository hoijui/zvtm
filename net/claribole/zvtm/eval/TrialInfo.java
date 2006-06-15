/*   FILE: TrialInfo.java
 *   DATE OF CREATION:  Fri Apr 21 16:00:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TrialInfo.java,v 1.4 2006/05/23 14:36:00 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

class TrialInfo {

    static final int NB_DEC = 1;
    static final String DOT = ".";

    // ID
    short id;
    // original position of camera
    long startLatitude, startLongitude;
    float startAltitude;
    // location of target city
    long targetLatitude, targetLongitude;
    // targetRegion is null if empty
    String targetName, targetRegion, targetCountry;
    // City and IC info (index of confidence) and associated region (all kept as Strings as we just dump them in the log file)
    String cityInfo;
    
    TrialInfo(String s){
	String[] items = s.split(GeoDataStore.CSV_SEP);
	// items[0] is the subject's name
	// items[1] is the subject's ID
	// items[2] is the block
	// items[3] is the trial number
	id = Short.parseShort(items[4]);
	startLatitude = Long.parseLong(items[5]);
	startLongitude = Long.parseLong(items[6]);
	startAltitude = Float.parseFloat(items[7]);
	targetName = items[8];
	targetRegion = (items[9].length() > 0) ? items[9] : null;
	targetCountry = items[10];
	targetLatitude = Long.parseLong(items[11]);
	targetLongitude = Long.parseLong(items[12]);
	cityInfo = targetName + LogManager.OUTPUT_CSV_SEP + // target name (city)
	    ((targetRegion != null) ? targetRegion : "") + LogManager.OUTPUT_CSV_SEP + // target region
	    targetCountry + LogManager.OUTPUT_CSV_SEP + // target country
	    targetLatitude + LogManager.OUTPUT_CSV_SEP + // target latitude
	    targetLongitude + LogManager.OUTPUT_CSV_SEP + // target longitude
	    items[13] + LogManager.OUTPUT_CSV_SEP + // IC
	    items[14] + LogManager.OUTPUT_CSV_SEP + // Region Area
	    items[15] + LogManager.OUTPUT_CSV_SEP + // Region NW latitude
	    items[16] + LogManager.OUTPUT_CSV_SEP + // Region NW longitude
	    items[17] + LogManager.OUTPUT_CSV_SEP + // Region SE latitude
	    items[18];                              // Region SE longitude
    }

    public String toString(){
	return "ID " + id +
	    "\nStart Latitude/Longitude/Altitude " + startLatitude + " / " + startLongitude + " / " + startAltitude +
	    "\nTarget " + cityInfo + "\n";
    }

    static String doubleFormatter(double number){
	String res = Double.toString(number);
	int dotindex = res.indexOf(DOT);
	if (dotindex != -1){
	    int declen = res.length() - dotindex;
	    if (declen > NB_DEC){
		return res.substring(0,dotindex+NB_DEC+1);
	    }
	}
	return res;
    }

    static String floatFormatter(float number){
	String res = Float.toString(number);
	int dotindex = res.indexOf(DOT);
	if (dotindex != -1){
	    int declen = res.length() - dotindex;
	    if (declen > NB_DEC){
		return res.substring(0,dotindex+NB_DEC+1);
	    }
	}
	return res;
    }
}