package fr.inria.zvtm.cluster.wis;

import fr.lri.insitu.FlowStates.events.IConEvent;

import fr.inria.zvtm.cluster.AstroRad;

public class TranslucencyEvent extends IConEvent {

    private double translucency;

    public void setSlotTranslucency(double a){
        this.translucency = a;
    }
    
    public double getSlotTranslucency(){
        return this.translucency;
    }

    public boolean occurs(){
        return true;
    }
    
}
