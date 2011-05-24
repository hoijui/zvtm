package fr.inria.zvtm.cluster;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;


public class ViconInput {
	
	
    static final int DEFAULT_OSC_LISTENING_PORT = 57109;
    
    static final String PREFIX = "/WildMetissePan";
    static final int WALL_WIDTH = 21880;
    static final int WALL_HEIGHT = 7100;
    static final double MOVE_TRESHOLD = 5;
    private double lastxx;
    private double lastyy;
    private double lastdd;
    
    
    OSCPortIn receiver;
	
	
	void initOSC(int in){

        // OSC receiver (control of meta camera)
        try {
            System.out.println("Initializing OSC receiver");
            receiver = new OSCPortIn(in);
            OSCListener listener = new OSCListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message){
                    processIncomingMessage(message);
                }
            };
            System.out.println("OSC receiver ready");
            receiver.addListener(PREFIX, listener);
            receiver.startListening();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }        
        
        
    }
    
    public void processIncomingMessage(OSCMessage msg){
  
        Object[] params = msg.getArguments();
        int x = (Integer) params[0];
        int y = (Integer) params[1];
        float d = ((Float) params[2]);
     
       
//        double appearentW = ((MetisseViewer) MetisseMain.viewer).getVisibleRegionWidth()/20;
//        double appearentH = ((MetisseViewer) MetisseMain.viewer).getVisibleRegionHeight()/20;
        double appearentW = 1000;
        double appearentH = 1000;
        double zoomSensibility = 1;
        
        
        double xx = (x-0.5*WALL_WIDTH)*2./WALL_WIDTH;
        double yy = (0.5*WALL_HEIGHT- y)*2./WALL_HEIGHT;
        double dd = (d-1300);
        
        if(x==0)xx = lastxx;
        if(y==0)yy = lastyy;
        if(d==0)dd = lastdd;
        
        
        double dx = (xx-lastxx)*appearentW;
        double dy = (yy-lastyy)*appearentH;
        double Dd = (dd-lastdd)*zoomSensibility;
		if(Math.abs(dx)< MOVE_TRESHOLD){
			dx = 0;
		}
		else{
			lastxx = xx;
		}
		if(Math.abs(dy)<MOVE_TRESHOLD ){
			dy = 0;
		}
		else{
			lastyy = yy;			
		}
		if(Math.abs(Dd)<MOVE_TRESHOLD ){
			Dd = 0;
		}
		else{
			lastdd = dd;			
		}
        if(dx!=0||dy!=0)MetisseMain.clientViewer.moveViewOf(dx, dy);
        else if(Dd!=0)MetisseMain.clientViewer.zoomOf(Dd);
    }


}
