package fr.inria.zvtm.cluster.wis;

import fr.lri.insitu.wild.wildinputserver.plugins.FlowStatesPlugin;
import fr.lri.insitu.wild.wildinputserver.plugins.AbstractPlugin;
import fr.lri.insitu.FlowStates.sm.IConStateMachine;

import java.net.URL;
import fr.inria.zvtm.cluster.AstroRad;
import fr.inria.zvtm.cluster.AstroServer;

public class AstroRadWISPlugin extends AbstractPlugin implements FlowStatesPlugin {
    
    AstroRad ar;
    
    public AstroRadWISPlugin(){}
    
    public void init(){
        try {
            ar = new AstroRad(new URL("file:///Users/epietrig/tools/FITS/ngc4587.fits"), null);
            new AstroServer(ar, 8000);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        
    }
    
    public IConStateMachine[] getStateMachines(){
        return null;
    }
    
    public boolean isSupported(){
        return true;
    }
    
}
