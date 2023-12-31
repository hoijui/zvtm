
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.Slider;

/**
 * Add methods that should be replay by the generic Delta here.
 * See the AbstractAutoReplay aspect in ZVTM-cluster for more details.
 * @see fr.inria.zvtm.AbstractAutoReplay
 */
 
aspect FitsAutoReplay extends AbstractAutoReplay {
    public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        if(replayTarget.isReplicated()) &&
        (
         execution(public void FitsImage.rescale(double, double, double)) ||
         execution(public void FitsImage.zRescale()) ||
         execution(public void FitsImage.setColorFilter(FitsImage.ColorFilter)) ||
         execution(public void JSkyFitsImage.setColorLookupTable(String)) ||
         execution(public void JSkyFitsImage.setCutLevels(double, double)) ||
         execution(public void JSkyFitsImage.setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm)) ||
         execution(public void JSkyFitsImage.setTranslucencyValue(float) ) ||
         execution(public void JSkyFitsImage.setVisible(boolean) ) ||
         execution(public void JSkyFitsImage.moveTo(double, double) ) ||
         execution(public void JSkyFitsImage.orientTo(double) ) ||
         execution(public void RangeSelection.setTicksVal(double, double)) ||
         execution(public void RangeSelection.setLeftTickPos(double)) ||
         execution(public void RangeSelection.setRightTickPos(double)) ||
         execution(public void Slider.setTickVal(double)) ||
         execution(public void Slider.setTickPos(double)) 
        );
}
