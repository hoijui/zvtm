package techniques.zoom;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.SocketException;
import java.util.Date;

import fr.inria.zuist.cluster.viewer.Viewer;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

/**
 * Push technique using the orientation of the hand (front or back) to decide the direction of the zooming. 
 * Requires a Y axis that is perpendiculary to the palm.
 * Still using the Y position. 
 * 
 * @author mathieunancel
 *
 */

public class Push2 extends AbstractViewerTechnique {

	public static final String IN_CMD_ZOOM_FIRSTVALUE_MEMORY = "zoomfvm"; 
	public static final String CMD_STOP = "stop";
	public static final String IN_CMD_STOP = CMD_STOP;
	
	public static final float ZOOM_MIN_SIN_PITCH = .5f;
	
	public static final float ZOOMIN_MIN_Y_DIR = .2f; 
	public static final float ZOOMIN_MAX_Y_DIR = 1f; 
	
	public static final float ZOOMOUT_MIN_Y_DIR = -1f; 
	public static final float ZOOMOUT_MAX_Y_DIR = -.2f; 
	
	public static final float ZOOM_MIN_MVT = 1; // TODO
	
	public static final float ZOOM_MAX_MVT = 50;
	
	protected OSCPortIn tiltReceiver, positionReceiver;
	protected OSCListener positionListener;
	protected OSCListener tiltListener;
	
	protected MouseAdapter mouseListener;
	protected boolean pressed = false;
	
	// protected float sin_pitch;
	// protected float sin_roll;
	
	protected float previousX, previousY, previousZ = previousY = previousX = Integer.MAX_VALUE;
	
	protected int direction;
	
	public Push2(String id, ORDER o, int portIn, boolean markersOnBack) {
		
		super(id, o, false);
		
		this.direction = markersOnBack ? 1 : -1; // TODO to be tested
		
		try {
			
			// this.tiltReceiver = new OSCPortIn(57110); 
			this.positionReceiver = new OSCPortIn(Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT);
			
		} catch (SocketException e) { 
			e.printStackTrace() ;
		}
		
	}

	@Override
	public void initListeners() {
		
		/*
		tiltListener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				
				if (msg != null && msg.getAddress().equals(Viewer.TILT_OBJECT)) {
					
					// System.out.println("New message to " + msg.getAddress() + ", " + msg.getArguments().length + " arguments.");
					
					Object[] parts = msg.getArguments();
					
					if (parts != null && parts.length == 3) {
						
						sin_pitch = ((Float)parts[0]).floatValue();
						sin_roll = ((Float)parts[1]).floatValue();
						
						// System.out.println("pitch : " + sin_pitch + "\nroll : " + sin_roll + "\n------");
						
					}
					
				}
				
			}
			
		};
		*/
		
		positionListener = new OSCListener() {
			public void acceptMessage(Date date, OSCMessage msg) {
				
				if (msg != null && msg.getAddress().equals(Viewer.MOVE_OBJECT)) {
					
					//System.out.println("New message to " + msg.getAddress() + ", " + msg.getArguments().length + " arguments.");
					
					Object[] parts = msg.getArguments();
					
					if (parts != null && parts.length == 6) {
						
						float x = ((Float)parts[0]).floatValue();
						float y = ((Float)parts[1]).floatValue();
						float z = ((Float)parts[2]).floatValue();
						
						float dx = ((Float)parts[3]).floatValue();
						float dy = ((Float)parts[4]).floatValue();
						float dz = ((Float)parts[5]).floatValue();
						
						// No previous values. This message just sets the previous values
						if (previousX == Integer.MAX_VALUE && previousY == Integer.MAX_VALUE && previousZ == Integer.MAX_VALUE) {
							
							System.out.println("No previous values");
							
							previousX = x;
							previousY = y;
							previousZ = z;
							
						} else {
							
							if (pressed) {
							
								float movementY = y - previousY;
									
								// Right yaw value for zooming in
								if ( dy > ZOOMIN_MIN_Y_DIR && dy < ZOOMIN_MAX_Y_DIR ) {
									
									// Right movement amplitude and direction
									if (movementY > ZOOM_MIN_MVT) { // TODO to be tested
										
										Viewer.getInstance().zeroOrderZoom(direction * movementY); // That too
										
									} else {
										// System.out.println("Movement shoud be higher : " + movementY);
									}
									
									// Right yaw value for zooming out
								} else if ( dy > ZOOMOUT_MIN_Y_DIR && dy < ZOOMOUT_MAX_Y_DIR ) {
									
									// Right movement amplitude and direction
									if (movementY < -ZOOM_MIN_MVT) { // TODO to be tested
										
										Viewer.getInstance().zeroOrderZoom(direction * movementY); // That too
										
									} else {
										// System.out.println("Movement shoud be lower : " + movementY);
									}
									
								} else {
									// System.out.println("dir.y doesn't correspond to a zoom orientation : " + dy);
								}
							
							}
							
							if (previousX != x && previousY != y && previousZ != z) {
									
								previousX = x;
								previousY = y;
								previousZ = z;
								
							}
							
						}
						
					}
				}
				
			}
		};
		
		mouseListener = new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					pressed = true;
					Viewer.getInstance().startViewer();
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					pressed = false;
					Viewer.getInstance().stopViewer();
				}
			}
			
		};
		
		OSCListener debugListener = new OSCListener() {
			
			public void acceptMessage(Date arg0, OSCMessage msg) {
				
				System.out.println("New message to " + msg.getAddress() + ", " + msg.getArguments().length + " arguments.");
				
			}
			
		};
		
		positionReceiver.addListener(Viewer.MOVE_OBJECT, positionListener);
		// tiltReceiver.addListener(Viewer.TILT_OBJECT, tiltListener);
		
		// tiltReceiver.addListener(".*", debugListener);
		
		System.out.println("Listeners added !");
	}
	
	@Override
	public void startListening() {
		
		// tiltReceiver.startListening();
		positionReceiver.startListening();
		Viewer.getInstance().getView().getPanel().addMouseListener(mouseListener);
		
	}
	
	@Override
	public void stopListening() {
		
		// tiltReceiver.stopListening();
		positionReceiver.stopListening();
		Viewer.getInstance().getView().getPanel().removeMouseListener(mouseListener);
		
	}
	
	@Override
	public void close() {
		
		// tiltReceiver.close();
		positionReceiver.close();
		
	}

}
