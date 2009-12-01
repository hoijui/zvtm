package fr.inria.zvtm.dazibao;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCPortIn;

/*
 * Handles input from a laser pointer type device
 * (vicon tracking + blind mouse for button press events) 
 */
class LaserEventSource {
    private DazBoard app;
    private OSCPortIn receiver; //receives messages from the Vicon tracker
    public static final int LASER_OSC_PORT = 5678;

	private static final int 
		SCREEN = 2,
		NAME = 3,
		MOUSEL = 4,
		MOUSER = 5,
		MOUSEW = 6,
        FIELD_NUMBER = 7;//#fields in OSC message (==max_index+1)

    private enum Event {
		MOVE,
		L_PRESS,
		R_PRESS,
		L_RELEASE,
		R_RELEASE,
		WHEEL
	}

    private static int LEFT = 0, RIGHT = 1;
    private boolean previouslyPressed[] = new boolean[2];

    public LaserEventSource(DazBoard app){
        this.app = app;
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
            }
        };
        receiver.addListener("/osclaser", listener);
    }

    public void start(){
        receiver.startListening();
    }

}

