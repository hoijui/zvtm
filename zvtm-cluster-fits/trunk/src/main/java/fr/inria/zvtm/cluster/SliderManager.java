package fr.inria.zvtm.cluster;

import java.util.ArrayList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.fits.Slider;

interface SliderStateObserver {
    //@param value in [0,1]
    //@param high in [0,1]
    public void onStateChange(SliderManager source, double value);
}

// a manager for a Slider object.
// tracks dragging state etc.
class SliderManager {
    private final VirtualSpace parentSpace;
    private final Slider slider;
    private boolean drag = false;
    private double prevValue = 0;

    private final ArrayList<SliderStateObserver> observers = new ArrayList();

    SliderManager(VirtualSpace parentSpace, double x, double y, double size){
        this.parentSpace = parentSpace;
        slider = new Slider();
        slider.moveTo(x, y);
        parentSpace.addGlyph(slider);
        slider.sizeTo(size);
    }

    void dispose(){
        parentSpace.removeGlyph(slider);
    }

    void setTickVal(double val){
        slider.setTickVal(val);
    }

    private void stateChanged(double val){
        for(SliderStateObserver obs: observers){
            obs.onStateChange(this, val);
        }
    }

    //handler
    void onPress1(double x, double y){
        if(slider.overTick(x, y)){
            drag = true;
        } 
    }

    //handler
    void onRelease1(){
        drag = false;
        double currValue = slider.getValue();
        if(prevValue != currValue){
            prevValue = currValue;
            stateChanged(currValue);
        }
    }

    //handler
    void onDrag(double x, double y){
        if(drag) {
            slider.setTickPos(x);
        } 
    }

    void addObserver(SliderStateObserver obs){
        assert(obs != null);
        if(!observers.contains(obs)){
            observers.add(obs);
        }
    }

    void removeObserver(SliderStateObserver obs){
        observers.remove(obs);
    }
}

