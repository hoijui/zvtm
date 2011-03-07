package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.AgilePanelType;
import fr.inria.zvtm.engine.View;

public class Agile2dSlaveApp extends SlaveApp {
    public Agile2dSlaveApp(SlaveOptions options){
        super(options);
    }

    static {
        View.registerViewPanelType(AgilePanelType.AGILE_VIEW,
                new AgilePanelType());
    }

    @Override protected String getViewType(){
        return AgilePanelType.AGILE_VIEW;
    }
}

