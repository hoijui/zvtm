package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.util.ArrayList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

interface ComboStateObserver {
    void onStateChange(ComboBox source, int activeIdx, String label);
}

//A manager for a combo-box style interaction.
//A Combo box is a number of horizontally aligned VRectangles, at most
//one of which is selected at a given time
class ComboBox {
    private VirtualSpace parentSpace;
    private final ArrayList<VRectangle> buttons = new ArrayList();
    private final ArrayList<VText> labels = new ArrayList();

    private final ArrayList<ComboStateObserver> observers = new ArrayList();

    private static final double LABEL_OFFSET = 15;

    /**
     * @param parentSpace parent VirtualSpace (new glyphs will be added to parentSpace)
     * @param labels combo box labels
     * @param x origin x-coordinate (center of the leftmost button)
     * @param y origin y-coordinate (center of the leftmost button)
     */
    ComboBox(VirtualSpace parentSpace, double x, double y, 
            String[] labels, Color[] colors, double itemSize){
        assert(labels.length == colors.length);
        for(int i=0; i<labels.length; ++i){
            VRectangle button = new VRectangle(x + i*(1.25*itemSize), y, 0, 
                    itemSize, itemSize, colors[i]);
            VText label = new VText(x + i*(1.25*itemSize), 
                    y-(itemSize * 0.5)-LABEL_OFFSET, 0,
                    colors[i], labels[i]);
            this.buttons.add(button);
            this.labels.add(label);
            parentSpace.addGlyph(button);
            parentSpace.addGlyph(label);
        }
    }

    void dispose(){
        for(VRectangle button: buttons){
            parentSpace.removeGlyph(button);
        }
        for(VText label: labels){
            parentSpace.removeGlyph(label);
        }
    }

    void addObserver(ComboStateObserver obs){
        assert(obs != null);
        if(!observers.contains(obs)){
            observers.add(obs);
        }
    }

    void removeObserver(ComboStateObserver obs){
        observers.remove(obs);
    }

    void onPress1(double x, double y){}
    void onRelease1(double x, double y){}
    //x and y in virtualspace coords
    void onClick1(double x, double y){
        int bIdx = buttonIndex(x, y);
        if(bIdx == -1){
            return;
        }
        stateChanged(bIdx, labels.get(bIdx).getText());
    }

    private void stateChanged(int activeIdx, String label){
        for(ComboStateObserver obs: observers){
            obs.onStateChange(this, activeIdx, label);
        }
    }

    /**
     * @return the index of the button at (x,y), or -1 if there is none.
     */
    private int buttonIndex(double x, double y){
        for(int i=0; i<buttons.size(); ++i){
           if(isInside(buttons.get(i), x, y)){
               return i;
           } 
        }
        return -1;
    }

    private static boolean isInside(Glyph glyph, double x, double y){
        double[] wnes = glyph.getBounds(); 
        return((x >= wnes[0]) && (x <= wnes[2]) &&
                (y >= wnes[3]) && (y <= wnes[1]));
    }
}

