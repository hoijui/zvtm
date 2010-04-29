package fr.inria.zuist.engine;

import java.net.URL;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * Describes a FITS images and creates / releases the corresponding
 * ZVTM glyph on demand.
 */
public class FitsImageDescription extends ResourceDescription {
    private float scaleFactor = 1;
    private FitsImage.ScaleMethod scaleMethod;

    transient FitsImage glyph; //the actual FITS image

    FitsImageDescription(String id, long x, long y, int z, URL location, 
            Region parentRegion){
    }

    public String getType(){
        return FitsResourceHandler.RESOURCE_TYPE_FITS;
    }

    public void createObject(final VirtualSpace vs, final boolean fadeIn){
        
    }

    public void destroyObject(final VirtualSpace vs, boolean fadeOut){
        glyph = null;
    }

    public Glyph getGlyph(){
        return glyph;
    }

}

