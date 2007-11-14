/*   FILE: CityInfo.java
 *   DATE OF CREATION:  Tue May 23 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

class CityInfo {

    // location of  city
    long latitude, longitude;
    // Region is null if empty
    String name, region, country;

    CityInfo(String s){
	String[] items = s.split(GeoDataStore.CSV_SEP);
	name = items[0];
	region = (items[1].length() > 0) ? items[1] : null;
	country = items[2];
	latitude = Long.parseLong(items[3]);
	longitude = Long.parseLong(items[4]);
    }

    public String toString(){
	return "\n Latitude/Longitude " + latitude + " / " + longitude +
	    "\n Information (City/Region/Country) " + name + " / " + region + " / " + country + "\n";
    }

}