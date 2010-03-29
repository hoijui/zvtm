/**
 * 
 */
package techniques.pan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;
import java.util.Date;

import javax.swing.Timer;

import fr.inria.zuist.cluster.viewer.Viewer;
import package techniques.VICONLaserListener;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import fr.inria.zvtm.engine.LongPoint;


/**
 * @author mathieunancel
 *
 */
public class IPodZoneLaserPan extends AbstractPanTechnique {
	
	public static final String X_ADDRESS = "/mrmr/tactilezoneX/0/iPod-touch";
	public static final String Y_ADDRESS = "/mrmr/tactilezoneY/0/iPod-touch";
	public static final String TAP_ADDRESS = "/mrmr/tactilezoneTouchDown/0/iPod-touch";
	
	public static final int Y_LIMIT = 666;
	public static final int TIME_LAP = 100;
	
	protected OSCPortIn VICONPort;
	protected OSCPortIn IPodPort;
	protected VICONLaserListener laserListener;
	protected OSCListener iPodListener;
	
	protected static boolean panning = false;
	protected boolean pressed = false;
	
	protected int prevX = Integer.MAX_VALUE, prevY = Integer.MAX_VALUE;
	protected long lastRelease = -1;
	
	protected LongPoint cursorLocation;
	protected LongPoint pointLocation;
	protected LongPoint previousPointLocation;
	
	protected Timer releaseTimer;
	
	/**
	 * @param id
	 * @param o
	 */
	public IPodZoneLaserPan(String id, ORDER o) {
		
		super(id, o);
		
		try {
			VICONPort = new OSCPortIn(Zoom.DEFAULT_PAN_OSC_LISTENING_PORT);
			IPodPort = new OSCPortIn(Zoom.IPOD_PAN_OSC_LISTENING_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		releaseTimer = new Timer(TIME_LAP, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Zoom.getInstance().stopPan();
				panning = false;
				
				System.out.println("Not panning");
				
			}
		});
		
		releaseTimer.setRepeats(false);
		
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#close()
	 */
	@Override
	public void close() {
		
		IPodPort.close();
		VICONPort.close();
		
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#initListeners()
	 */
	@Override
	public void initListeners() {
		
		laserListener = new VICONLaserListener() {
			@Override
			public void acceptMessage(Date time, OSCMessage message) {
				
				super.acceptMessage(time, message);
				
				if (panning) {
					
					// au sens de zvtm
					cursorLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
							Zoom.getInstance().getCursorCamera(),
							(int)currentCoords.x,
							(int)currentCoords.y
					); 
					
					pointLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
							Zoom.getInstance().getMCamera(), 
							(int)currentCoords.x,
							(int)currentCoords.y
					); 
					
					previousPointLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
							Zoom.getInstance().getCursorCamera(), 
							(int)previousCoords.x,
							(int)previousCoords.y
					); 
					
					Zoom.getInstance().setCursorPosition(cursorLocation.x, cursorLocation.y);
					// System.out.println("cursor : " + cursorLocation.x + ", " + cursorLocation.y);
					
					if (pressed) {
						
						Zoom.getInstance().zeroOrderTranslate((int)(previousPointLocation.x - cursorLocation.x), (int)(previousPointLocation.y - cursorLocation.y));
						
					}
					
				}
			}
		};
		
		VICONPort.addListener("/WildPointing/moveTo", laserListener);
		
		// Criterion : 
		// No movement greater than MAX_MVT during TIME_LIMIT ms
		iPodListener = new OSCListener() {
			
			public void acceptMessage(Date time, OSCMessage msg) {
				
				Object[] parts = msg.getArguments();
				
				if (msg.getAddress().equals(TAP_ADDRESS)) {
					
					if ( ((Integer)parts[0]).intValue() == 1 ) {
						
						pressed = true;
						
						// System.out.println("Press");
						
					} else if ( ((Integer)parts[0]).intValue() == 0 ) {
						
						pressed = false;
						
						releaseTimer.start();
						
						// System.out.println("Release");
						
						prevX = prevY = Integer.MAX_VALUE;
						
						lastRelease = System.currentTimeMillis();
						
					} else {
						System.err.println("Strange : no value associated to " + TAP_ADDRESS);
					}
					
				} else if (prevY == Integer.MAX_VALUE && msg.getAddress().equals(Y_ADDRESS) && Math.abs(System.currentTimeMillis() - lastRelease) > TIME_LAP) {
					
					int y = ((Integer)parts[0]).intValue();
					
					prevY = y;
					
					// System.out.println("Received y = " + y + ", " + Math.abs(System.currentTimeMillis() - lastRelease) );
					
					if (y > Y_LIMIT) {
						Zoom.getInstance().startPan();
						panning = true;
						
						System.out.println("Panning");
						
					} else {
						panning = false;
						
						System.out.println("Not panning");
						
					}
					
				} else if (prevY == Integer.MAX_VALUE && msg.getAddress().equals(Y_ADDRESS)) {
					// System.out.println( Math.abs(System.currentTimeMillis() - lastRelease) );
				}
				
			}
			
		};
		
		IPodPort.addListener(X_ADDRESS, iPodListener);
		IPodPort.addListener(Y_ADDRESS, iPodListener);
		IPodPort.addListener(TAP_ADDRESS, iPodListener);

	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#startListening()
	 */
	@Override
	public void startListening() {
		
		VICONPort.startListening();
		IPodPort.startListening();

	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#stopListening()
	 */
	@Override
	public void stopListening() {
		
		VICONPort.stopListening();
		IPodPort.stopListening();

	}
	
	public static boolean isPanning() {
		return panning;
	}

}
