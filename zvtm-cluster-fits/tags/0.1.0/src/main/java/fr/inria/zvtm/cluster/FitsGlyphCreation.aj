package fr.inria.zvtm.cluster;

import java.net.URL;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;

public aspect FitsGlyphCreation {
    @Override GlyphReplicator FitsImage.getReplicator(){
        return new FitsImageReplicator(this);
    }

    private static class FitsImageReplicator extends GlyphCreation.ClosedShapeReplicator {
        private final URL imageLocation;
        private final double scaleFactor;

        FitsImageReplicator(FitsImage source){
            super(source);
            this.scaleFactor = source.scaleFactor;
            this.imageLocation = source.getImageLocation();
        }

        Glyph doCreateGlyph(){
            try{
                return new FitsImage(0.,0.,0,imageLocation, scaleFactor);
            } catch(Exception e){
                //XXX error handling
                throw new Error(e);
            }
        }
    }
}

