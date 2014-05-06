package fr.inria.zvtm.cluster.wis;

import fr.lri.insitu.FlowStates.sm.IConStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Event;

import fr.inria.zvtm.cluster.AstroRad;

public class TranslucencySM extends IConStateMachine {

    AstroRad application;

    public TranslucencySM(){}
    
    public TranslucencySM(String name, Canvas canvas, AstroRad app){
        super(name, canvas);
        this.application = app;
    }
    
    State idle = new State(){
        Transition recEvent = new Event(TranslucencyEvent.class){
            public void action(){
                TranslucencyEvent evt = (TranslucencyEvent)getEvent();
                //System.out.println("Translucency action..."+evt.getSlotTranslucency());
            }
        };
    };
    
    
}
