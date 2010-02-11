/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

import java.awt.Image;

public class FITSImage extends VImage {
    
    public FITSImage(Image img){
        super(img);
    }
    
    public FITSImage(long x, long y, int z, Image img){
        super(x, y, z, img);
    }
    
    public FITSImage(long x, long y, int z, Image img, double scale){
        super(x, y, z, img, scale);
    }
    
    public FITSImage(long x, long y, int z, Image img, double scale, float alpha){
        super(x, y, z, img, scale, alpha);
    }
    
}
