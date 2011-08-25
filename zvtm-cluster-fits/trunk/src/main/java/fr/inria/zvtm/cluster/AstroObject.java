package fr.inria.zvtm.cluster;

import jsky.science.Coordinates;

class AstroObject {
    private String identifier;
    private Coordinates coords;
    private AstroObject(){}

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

    Coordinates getCoords(){
        return coords;
    }

    /**
     * Returns the right ascension of the object, in degrees.
     */
    double getRa(){
        return coords.getRa();
    }

    /**
     * Returns the declination of the object, in degrees.
     */
    double getDec(){
        return coords.getDec();
    }

    String getIdentifier(){
        return identifier;
    }

    public String toString(){
        return identifier + " | " + coords.getRa() + " | " + coords.getDec();
    }
}

