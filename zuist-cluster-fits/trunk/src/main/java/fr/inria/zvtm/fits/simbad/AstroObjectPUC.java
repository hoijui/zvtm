/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AstroObjectPUC.java $
 */

package fr.inria.zvtm.fits.simbad;


import fr.inria.zvtm.fits.simbad.AstroObject;
import jsky.science.Coordinates;

import org.json.JSONException;
import org.json.JSONObject;

public class AstroObjectPUC extends AstroObject{


	private boolean nucleated;

	/**
     * @param obj - JSONObject with atributes ra, dec and identifier
     */
    public static AstroObjectPUC fromJSON(JSONObject obj) throws JSONException{
        AstroObjectPUC retval = new AstroObjectPUC();
        double ra = obj.getDouble("ra");
        double dec = obj.getDouble("dec");
        retval.setIdentifier(obj.getString("identifier"));
        retval.setNucleated(obj.getBoolean("nucleated"));
        retval.setCoords(new Coordinates(ra, dec));
        return retval;
    }

    public void setNucleated(boolean nucleated){
    	this.nucleated = nucleated;
    }

    public boolean isNucleated(){
    	return nucleated;
    }

    @Override
    public String toString(){
        return this.getIdentifier() + " | " + this.getRa() + " | " + this.getDec() + " | " + nucleated;
    }
}