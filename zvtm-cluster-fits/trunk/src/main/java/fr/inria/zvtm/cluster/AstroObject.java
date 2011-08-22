package fr.inria.zvtm.cluster;

import jsky.science.Coordinates;

class AstroObject {
    private String identifier;
    private Coordinates coords;
    private AstroObject(){}

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

    public String toString(){
        return identifier + " | " + coords.getRa() + " | " + coords.getDec();
    }
}

