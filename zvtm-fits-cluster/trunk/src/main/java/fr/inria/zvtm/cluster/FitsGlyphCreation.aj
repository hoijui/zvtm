package fr.inria.zvtm.cluster;

import java.net.URL;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;

public aspect FitsGlyphCreation {
    @Override Delta FitsImage.getCreateDelta(){
        return new FitsImageCreateDelta(this,
                this.getParentSpace().getObjId());
    }

    private static class FitsImageCreateDelta extends GlyphCreation.ClosedShapeCreateDelta {
        private final URL imageLocation;
        private final float scaleFactor;

        FitsImageCreateDelta(FitsImage source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
            this.scaleFactor = source.scaleFactor;
            this.imageLocation = source.getImageLocation();
        }

        Glyph createGlyph(){
            try{
                return new FitsImage(0,0,0,imageLocation);
                //XXX scale factor
            } catch(Exception e){
                throw new Error(e);
            }
        }
    }
}

