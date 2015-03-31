/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;



import edu.jhu.pha.sdss.fits.FITSImage;
import java.net.URL;
import java.io.File;

//provide an accessor to scale parameters
public class ExFITSImage extends FITSImage{
    public ExFITSImage(URL imgUrl) throws Exception{
        super(imgUrl);
    }

    public ExFITSImage(File imgFile) throws Exception{
        super(imgFile);
    }

    public double[] getScaleParams(){
        return new double[]{ _min, _max };
    }

}