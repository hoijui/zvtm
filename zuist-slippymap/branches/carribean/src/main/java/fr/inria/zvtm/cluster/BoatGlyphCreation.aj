/*   Copyright (c) INRIA, 2015. All Rights Reserved
 * $Id:  $
 */


package fr.inria.zvtm.cluster;

import java.awt.Color;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.BoatInfoG;

public aspect BoatGlyphCreation {

    //overrides for various Glyph subclasses
    @Override public GlyphReplicator BoatInfoG.getReplicator(){
        return new BoatInfoGReplicator(this);
    }

    private static class BoatInfoGReplicator extends GlyphCreation.AbstractGlyphReplicator {

        private String boatType, boatNameAndCallsign, boatCrewAndPassengers, boatTimeToImpact;
        private boolean exposed;

        BoatInfoGReplicator(BoatInfoG source){
            super(source);
            boatType = source.getBoatType();
            boatNameAndCallsign = source.getBoatNameAndCallsign();
            boatCrewAndPassengers = source.getBoatCrewAndPassengers();
            boatTimeToImpact = source.getBoatTimeToImpact();
            exposed = source.isExposed();
        }

        public Glyph doCreateGlyph(){
            BoatInfoG retval = new BoatInfoG(0d, 0d, 0, boatType, boatNameAndCallsign,
                                             boatCrewAndPassengers, boatTimeToImpact, exposed);
            return retval;
        }
    }

}
