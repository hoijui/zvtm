package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.LinearGradientPaint;

import java.util.ArrayList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.LGRectangle;
import fr.inria.zvtm.glyphs.VText;

interface RadioStateObserver {
    void onStateChange(RadioGroup source, int activeIdx, String label);
}

//A Radio Group is a number of horizontally aligned VRectangles, at most
//one of which is selected at a given time
class RadioGroup {
    private final VirtualSpace parentSpace;
    private final ArrayList<LGRectangle> buttons = new ArrayList();
    private final ArrayList<VText> labels = new ArrayList();

    private final ArrayList<RadioStateObserver> observers = new ArrayList();

    private static final double LABEL_OFFSET = 15;

    /**
     * @param parentSpace parent VirtualSpace (new glyphs will be added to parentSpace)
     * @param labels radio group labels
     * @param x origin x-coordinate (center of the leftmost button)
     * @param y origin y-coordinate (center of the leftmost button)
     */
    RadioGroup(VirtualSpace parentSpace, double x, double y, 
            String[] labels, LinearGradientPaint[] gradients, double itemSize){
        assert(labels.length == gradients.length);
        this.parentSpace = parentSpace;
        for(int i=0; i<labels.length; ++i){
            LGRectangle button = new LGRectangle(x + i*(1.25*itemSize), y, 0, 
                    itemSize, itemSize/4, gradients[i], Color.WHITE);
            VText label = new VText(x + i*(1.25*itemSize), 
                    y-(itemSize * 0.5)-LABEL_OFFSET, 0,
                    Color.WHITE, labels[i]);
            this.buttons.add(button);
            this.labels.add(label);
            parentSpace.addGlyph(button);
            parentSpace.addGlyph(label);
        }
    }

    void dispose(){
        for(LGRectangle button: buttons){
            parentSpace.removeGlyph(button);
        }
        for(VText label: labels){
            parentSpace.removeGlyph(label);
        }
    }

    void addObserver(RadioStateObserver obs){
        assert(obs != null);
        if(!observers.contains(obs)){
            observers.add(obs);
        }
    }

    void removeObserver(RadioStateObserver obs){
        observers.remove(obs);
    }

    //x and y in virtualspace coords
    void onClick1(double x, double y){
        int bIdx = buttonIndex(x, y);
        if(bIdx == -1){
            return;
        }
        stateChanged(bIdx, labels.get(bIdx).getText());
    }

    private void stateChanged(int activeIdx, String label){
        for(RadioStateObserver obs: observers){
            obs.onStateChange(this, activeIdx, label);
        }
    }

    /**
     * @return the index of the button at (x,y), or -1 if there is none.
     */
    private int buttonIndex(double x, double y){
        for(int i=0; i<buttons.size(); ++i){
           if(AstroUtil.isInside(buttons.get(i), x, y)){
               return i;
           } 
        }
        return -1;
    }

}

