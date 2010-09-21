package fr.inria.zvtm.cluster;

import java.util.ArrayList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.fits.RangeSelection;

interface RangeStateObserver {
    //@param low in [0,1]
    //@param high in [0,1]
    //low <= high
    public void onStateChange(RangeManager source, double low, double high);
}

// a manager for a RangeSelection object.
// tracks dragging state etc.
class RangeManager {
    private final VirtualSpace parentSpace;
    private final RangeSelection rangeSel;
    private boolean leftDrag = false;
    private boolean rightDrag = false;
    private double prevMin = 0;
    private double prevMax = 0;

    private final ArrayList<RangeStateObserver> observers = new ArrayList();

    RangeManager(VirtualSpace parentSpace, double x, double y, double size){
        this.parentSpace = parentSpace;
        rangeSel = new RangeSelection();
        rangeSel.moveTo(x, y);
        parentSpace.addGlyph(rangeSel);
        rangeSel.sizeTo(size);
    }

    void dispose(){
        parentSpace.removeGlyph(rangeSel);
    }

    void setTicksVal(double lowCut, double highCut){
        rangeSel.setTicksVal(lowCut, highCut);
    }

    private void stateChanged(double low, double high){
        for(RangeStateObserver obs: observers){
            obs.onStateChange(this, low, high);
        }
    }

    //handler
    void onPress1(double x, double y){
        if(rangeSel.overLeftTick(x, y)){
            leftDrag = true;
        } else if(rangeSel.overRightTick(x, y)){
            rightDrag = true;
        }
    }

    //handler
    void onRelease1(){
        leftDrag = false;
        rightDrag = false;
        double min = rangeSel.getLeftValue();
        double max = rangeSel.getRightValue();
        if((prevMin != min) || (prevMax != max)){
            prevMin = min;
            prevMax = max;
            stateChanged(min, max);
        }
    }

    //handler
    void onDrag(double x, double y){
        if(leftDrag) {
            rangeSel.setLeftTickPos(x);
        } else if(rightDrag){
            rangeSel.setRightTickPos(x);
        }
    }

    void addObserver(RangeStateObserver obs){
        assert(obs != null);
        if(!observers.contains(obs)){
            observers.add(obs);
        }
    }

    void removeObserver(RangeStateObserver obs){
        observers.remove(obs);
    }
}

