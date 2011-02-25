/*   Copyright (c) INRIA, 2010-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */
 
package fr.inria.zvtm.cluster;

import java.awt.Color;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.GPath;

aspect NodeTrixGlyphCreation {
    GlyphReplicator GPath.getReplicator(){
        return new GPathReplicator(this);
    }

    private static class GPathReplicator extends GlyphCreation.DPathReplicator{
        private final Color[] gradientColors;
        private final float[] gradientSteps;

        GPathReplicator(GPath source){
            super(source);
            gradientColors = source.peekGradientColors();
            gradientSteps = source.peekGradientSteps();
        }

        public Glyph doCreateGlyph(){
            GPath retval = new GPath(path.getPathIterator(null), 0, Color.BLACK);
            retval.setGradient(gradientColors, gradientSteps);
            return retval;
        }
    }
}

