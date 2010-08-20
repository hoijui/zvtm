package fr.inria.zvtm.fits;

public interface Sampler {
    /**
     * returns up to 'nmax' samples.
     */
    public double[] getSample(int nmax);
}

