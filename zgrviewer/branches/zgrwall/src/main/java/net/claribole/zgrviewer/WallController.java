package net.claribole.zgrviewer;

import java.util.Date;
import java.net.SocketException;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import fr.inria.zvtm.cluster.ClusterGeometry;

class WallController {
    private static final int X         = 0;
    private static final int Y         = 1;
    private static final int SCREEN    = 2;
    private static final int LEFT_BTN  = 3;
    private static final int RIGHT_BTN = 4;
    private static final int WHEEL     = 5;

    private final ClusterGeometry geom;
    private final OSCListener coordsListener;	
    private OSCPortIn dataReceiver;

    public static final int DEFAULT_OSC_PORT = 6789;

    private GraphicsManager listener = null;

    WallController(ClusterGeometry geom){
        this(geom, DEFAULT_OSC_PORT);
    }

    WallController(ClusterGeometry geom, int oscPort){
        this.geom = geom;

        coordsListener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {

                if (message != null && message.getArguments().length == 6) {
                    String screenName = message.getArguments()[SCREEN].toString();
                    int screenX = ((Integer)(message.getArguments()[X])).intValue();
                    int screenY = ((Integer)(message.getArguments()[Y])).intValue();
                    int screenCoordsX = 0;
                    int screenCoordsY = 0;

                    if (screenName.contains("A"))
                        screenCoordsX = 0;
                    else if (screenName.contains("B"))
                        screenCoordsX = 2;
                    else if (screenName.contains("C"))
                        screenCoordsX = 4;
                    else if (screenName.contains("D"))
                        screenCoordsX = 6;

                    if (screenName.contains("R"))
                        screenCoordsX ++;

                    screenCoordsY = new Integer(""+screenName.charAt(1)).intValue()-1;
                    if(screenCoordsX == -1 || screenCoordsY == -1){
                        System.err.println("incorrect screen indexes");
                        return;
                    }


                    int coordsX = screenCoordsX * WallController.this.geom.getBlockWidth() + screenX;
                    int coordsY = screenCoordsY * WallController.this.geom.getBlockHeight() + screenY;
                    //System.out.println("coordsX: " + coordsX + ", coordsY: " + coordsY);
                    boolean leftMouse = (Boolean)(message.getArguments()[LEFT_BTN]);
                    boolean rightMouse = (Boolean)(message.getArguments()[RIGHT_BTN]);
                    int wheel = ((Integer)(message.getArguments()[WHEEL])).intValue();

                    //signal listener
                    if(listener != null){
                        listener.onPointerCoordsUpdate(coordsX, coordsY, leftMouse, rightMouse, wheel);
                    }

                } else {
                    System.err.println("invalid OSC message");
                }
            }        
        };	
        
        try {
			this.dataReceiver = new OSCPortIn(oscPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		dataReceiver.addListener("/WildPointing/data", coordsListener);
        dataReceiver.startListening();
    }

    void setListener(GraphicsManager listener){
        this.listener = listener;
    }
}

