package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.AgilePanelType;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


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

    public static void main(String[] args){
        SlaveOptions options = new SlaveOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try{
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }

        if(options.help){
            System.err.println("Usage: Agile2dSlaveApp [options] where options are: ");
            parser.printUsage(System.err);
            return;
        }

        SlaveApp app = new Agile2dSlaveApp(options);
        SlaveUpdater updater = new SlaveUpdater(options.appName,
                options.blockNumber);
        updater.setAppDelegate(app);
        updater.startOperation();
    }
}

