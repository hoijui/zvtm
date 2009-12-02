package fr.inria.zvtm.dazibao;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCPortIn;

/*
 * Handles input from a laser pointer type device
 * (vicon tracking + blind mouse for button press events) 
 * ICON config: dazibao2
 */
class LaserEventSource {
    private final DazBoard app;
    private OSCPortIn receiver; //receives messages from the Vicon tracker
    public static final int LASER_OSC_PORT = 5678;

    private static final int BLOCK_WIDTH = 2760; //code tangling, but hard to avoid
    private static final int BLOCK_HEIGHT = 1740;

	private static final int 
        X = 0,
        Y = 1,
		SCREEN = 2,
		NAME = 3,
		MOUSEL = 4,
		MOUSER = 5,
		MOUSEW = 6,
        FIELD_NUMBER = 7;//#fields in OSC message (==max_index+1)

    private enum Event {
		MOVE(){
            @Override void sendEvent(DazBoard app, int xpos, int ypos, int wheel){
                app.onLaserMove(xpos, ypos);
            }
        },
		L_PRESS(){
            @Override void sendEvent(DazBoard app, int xpos, int ypos, int wheel){
                app.onLeftPress();
            }
        },
		R_PRESS(){ 
            @Override void sendEvent(DazBoard app, int xpos, int ypos, int wheel){
                app.onRightPress();
            }
        },
		L_RELEASE(){
            @Override void sendEvent(DazBoard app, int xpos, int ypos, int wheel){
                app.onLeftRelease();
            }
        },
        R_RELEASE(){
            @Override void sendEvent(DazBoard app, int xpos, int ypos, int wheel){
                app.onRightRelease();
            }
        },
		WHEEL(){
            @Override void sendEvent(DazBoard app, int xpos, int ypos, int wheel){
                app.onWheel(wheel);
            }
        };
        abstract void sendEvent(DazBoard app, int xpos, int ypos, int wheel);
	}

    private static int LEFT = 0, RIGHT = 1;
    private boolean previouslyPressed[] = new boolean[2];

    public LaserEventSource(DazBoard theApp){
        this.app = theApp;
        try {
            receiver = new OSCPortIn(LASER_OSC_PORT);
        } catch(java.net.SocketException ex){
            throw new Error("could not start listening for OSC messages: " + ex);
        }
        OSCListener listener = new OSCListener(){
            public void acceptMessage(java.util.Date date, OSCMessage message){
                if(message == null){
                    System.err.println("null message");
                    return;
                }
                if(message.getArguments().length < FIELD_NUMBER){
                    System.err.println("malformed message");
                    return;
                }
                boolean pressedL = ((Integer)message.getArguments()[MOUSEL]).intValue() == 1;
                boolean pressedR = ((Integer)message.getArguments()[MOUSER]).intValue() == 1;
                int wheel = ((Integer)message.getArguments()[MOUSEW]).intValue();

                Event event = Event.MOVE; 					
				if (pressedL && !previouslyPressed[LEFT]) {
					event = Event.L_PRESS;		
				} else if (!pressedL && previouslyPressed[LEFT]) {
					event = Event.L_RELEASE;		
				} else if (pressedR && !previouslyPressed[RIGHT]) {
					event = Event.R_PRESS;			
				} else if (!pressedR && previouslyPressed[RIGHT]) {
                    event = Event.R_RELEASE;
				} else if (wheel != 0) {
					event = Event.WHEEL;
				}
				
				previouslyPressed[LEFT] = pressedL;
				previouslyPressed[RIGHT] = pressedR;

                String screenName = message.getArguments()[2].toString();
				int screenX = ((Integer)(message.getArguments()[X])).intValue();
				int screenY = ((Integer)(message.getArguments()[Y])).intValue();
				if (screenName != null && screenName.length() > 0) {
					int screenCoordsX = -1;
					
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
					
					int screenCoordsY = new Integer(""+screenName.charAt(1)).intValue()-1;
					int xpos = screenCoordsX * BLOCK_WIDTH + screenX;
					int ypos = screenCoordsY * BLOCK_WIDTH + screenY;

                    event.sendEvent(app, xpos, ypos, wheel);
            }
            }
        };
        receiver.addListener("/osclaser", listener);
    }

    public void start(){
        receiver.startListening();
    }

}

