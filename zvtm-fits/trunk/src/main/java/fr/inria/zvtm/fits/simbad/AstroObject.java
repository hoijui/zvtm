/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits.simbad;

import jsky.science.Coordinates;

public class AstroObject {

    private String identifier;
    private Coordinates coords;

    public AstroObject(){}

    /**
     * @param simRowStr - simbad row formatted as per the CatQuery
     * format.
     */
    static AstroObject fromSimbadRow(String simRowStr){
        AstroObject retval = new AstroObject();

        String[] elems = simRowStr.split("\\|");
        if(elems.length < 3){
            //this does not look like a valid row
            return null;
        }
        retval.identifier = elems[0];
        retval.coords = new Coordinates(Double.parseDouble(elems[1]),
                Double.parseDouble(elems[2]));
        return retval;
    }

    public Coordinates getCoords(){
        return coords;
    }

    /**
     * Returns the right ascension of the object, in degrees.
     */
    public double getRa(){
        return coords.getRa();
    }

    /**
     * Returns the declination of the object, in degrees.
     */
    public double getDec(){
        return coords.getDec();
    }

    public String getIdentifier(){
        return identifier;
    }

    public String toString(){
        return identifier + " | " + coords.getRa() + " | " + coords.getDec();
    }
}
