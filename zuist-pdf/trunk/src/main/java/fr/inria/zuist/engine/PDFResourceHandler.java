/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;

/** Interface implemented by handlers of the various resource types.
 *@author Emmanuel Pietriga
 */

public class PDFResourceHandler implements ResourceHandler {
    
    public PDFResourceHandler(){
        
    }
    
    public PDFPageDescription createResourceDescription(long x, long y, long w, long h, String id, int zindex, Region region, 
                                                        String imagePath, boolean sensitivity, Color stroke, Object im){
        
        PDFPageDescription pdfd = new PDFPageDescription(id, x, y, zindex, w, h, imagePath, stroke, im, region);
        pdfd.setSensitive(sensitivity);
        region.addObject(pdfd);
        return pdfd;
    }
    
}
