package fr.inria.zvtm.fits;

import nom.tam.fits.FitsException;
import edu.jhu.pha.sdss.fits.FITSImage;

public class DefaultSampler implements Sampler {
    private final FITSImage image;

    public DefaultSampler(FITSImage image){
        this.image = image;
    }

    public double[] getSample(int nmax){
        double retval[];
        int nbpix = image.getWidth() * image.getHeight();
        int stride = (int)Math.max(1., nbpix / nmax);
        retval = new double[(int)Math.min(nbpix, nmax)];
        int j = 0;

        for(int i=0; i<retval.length; ++i){
            try{
                retval[i] = image.getOriginalValue(j%image.getWidth(), 
                        j/image.getWidth());
            } catch(FitsException ex){
                throw new Error(ex);
            }
            j += stride;
        }

        return retval;
    }

    /**
     * Prints an image sample to the standard output
     */
    public static void main(String[] args) throws Exception{
        double[] sample = new DefaultSampler(new FITSImage(args[0])).getSample(2000);
        for(int i=0; i<sample.length; ++i){
            System.out.println(i + "\t" + sample[i]);
        }
    }
}

