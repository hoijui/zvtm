/**
 * 
 */
package techniques.zoom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;
import java.util.Date;

import javax.swing.Timer;

import fr.inria.zuist.cluster.viewer.Viewer;
import techniques.pan.IPodZoneLaserPan;
import utils.dispatchOSC.OSCDispatcher;
import utils.transfer.SigmoidTF;
import utils.transfer.MultTF;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;


/**
 * @author mathieunancel
 *
 */
public class LinearIPod extends AbstractViewerTechnique {
	
	public static final int ID = 0, X = 1, Y = 2, PRESS = 3;
	
	public static final String TOUCHPAD_ADDRESS = "/zoompan/xy1";
	
	/**
	 * Maximum difference between the previous D value and the current one.
	 * If this value is exceeded, the current glove coordinates are ignored.
	 */
	public static final float MAX_STEP = 20;
	
	/**
	 * Minimum difference between the previous D value and the current one.
	 * If the current D is below this value, the current glove data are ignored.
	 * If this value is 0, there is a risk that the zooming movement never stops.
	 */
	public static final float MIN_STEP = 0;
	
	/**
	 * Requested min zoom factor.
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MIN_ZOOM_FACTOR = 0;
	
	/**
	 * Requested max zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MAX_ZOOM_FACTOR = 40;
	
	
	
	
	protected OSCPortIn dataReceiver;
	protected OSCListener iPodListener;
	
	protected int currentSens = -2, ponctualSens = -2;
	protected int currentY, previousY = Integer.MAX_VALUE;
	
	protected SigmoidTF transferFunction = new SigmoidTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	 //protected MultTF transferFunction = new MultTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	
	
	protected OSCDispatcher dispatcher = new OSCDispatcher(
			Viewer.IPOD_DEFAULT_OSC_LISTENING_PORT, 
			new int[] {
					Viewer.IPOD_ZOOM_OSC_LISTENING_PORT, 
					Viewer.IPOD_POINT_OSC_LISTENING_PORT, 
					Viewer.IPOD_PAN_OSC_LISTENING_PORT
			}
	);
	
	
	/**
	 * @param id
	 * @param o
	 */
	public LinearIPod(String id, ORDER o, boolean c) {
		
		super(id, o, c);
		
		try {
			this.dataReceiver = new OSCPortIn(Viewer.IPOD_ZOOM_OSC_LISTENING_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		 transferFunction.setLambda(2f);
		 transferFunction.setXOffset(0);
		 //transferFunction.setMultiplier(2);
		
		cyclic = false;
		
	}
	
	public void linearMovement() {
		
		if (previousY != -Integer.MAX_VALUE && !IPodZoneLaserPan.isPanning()) {
				
			ponctualSens = (int)Math.signum(currentY - previousY);
			
			if (currentSens == -2 || !cyclic) {
				
				currentSens = ponctualSens;
				
				// System.out.println(currentSens);
				
			}
			
			// System.out.println("\t" + ponctualSens);
			
			Viewer.getInstance().zeroOrderViewer( currentSens * transferFunction.compute(currentY - previousY) );
			
		}
		
		previousY = currentY;
		
	}
	
	@Override
	public void initListeners() {
		iPodListener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				
				// System.out.print(".");
				
				String[] parts = msg.getArguments()[0].toString().split(" ");
				
				if (parts.length == 4) {
					
					if ( new Integer(parts[PRESS]).intValue() == 1 ) {
						
						currentY = new Integer(parts[Y]).intValue();
						
						linearMovement();
						
					} else {
						
						previousY = -Integer.MAX_VALUE;
						
					}
					
				}
				
			}
		};
		
		dataReceiver.addListener(TOUCHPAD_ADDRESS, iPodListener);
		
		System.out.println("Listeners initialized");
	}

	@Override
	public void startListening() {
		
		dataReceiver.startListening();
		dispatcher.dispatch();
		
		System.out.println("Listeners listening");
		
	}
	
	@Override
	public void stopListening() {
		
		dataReceiver.stopListening();
		// dataReceiver.close();
		dispatcher.stop();
		
	}
	
	@Override
	public void close() {
		
		dataReceiver.close();
		dispatcher.close();
		
	}

}
