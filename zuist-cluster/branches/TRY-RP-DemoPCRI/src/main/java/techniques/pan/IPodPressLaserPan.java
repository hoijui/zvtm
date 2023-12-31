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
import techniques.VICONLaserListener;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import java.awt.geom.Point2D;


/**
 * @author mathieunancel
 *
 */
public class IPodPressLaserPan extends AbstractPanTechnique {
	
	public static final int ID = 0, X = 1, Y = 2, PRESS = 3;
	
	public static final String PRESS_ADDRESS = "/zoompan/xy2";
	
	protected OSCPortIn VICONPort;
	protected OSCPortIn IPodPort;
	protected VICONLaserListener laserListener;
	protected OSCListener iPodListener;
	
	protected static boolean panning = false;
	
	protected Point2D.Double cursorLocation;
	protected Point2D.Double pointLocation;
	protected Point2D.Double previousPointLocation;
	
	/**
	 * @param id
	 * @param o
	 */
	public IPodPressLaserPan(String id, ORDER o) {
		
		super(id, o);
		
		try {
			VICONPort = new OSCPortIn(Viewer.DEFAULT_PAN_OSC_LISTENING_PORT);
			IPodPort = new OSCPortIn(Viewer.IPOD_PAN_OSC_LISTENING_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
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
					cursorLocation = Viewer.getInstance().getClusteredView().viewToSpaceCoords(
							Viewer.getInstance().getCursorCamera(),
							(int)currentCoords.x,
							(int)currentCoords.y
					); 
					
					pointLocation = Viewer.getInstance().getClusteredView().viewToSpaceCoords(
							Viewer.getInstance().getMCamera(), 
							(int)currentCoords.x,
							(int)currentCoords.y
					); 
					
					previousPointLocation = Viewer.getInstance().getClusteredView().viewToSpaceCoords(
							Viewer.getInstance().getCursorCamera(), 
							(int)previousCoords.x,
							(int)previousCoords.y
					); 
					
					Viewer.getInstance().setCursorPosition(cursorLocation.x, cursorLocation.y);
					// System.out.println("cursor : " + cursorLocation.x + ", " + cursorLocation.y);
					
					if (panning) {
						
						Viewer.getInstance().zeroOrderTranslate((int)(previousPointLocation.x - cursorLocation.x), (int)(previousPointLocation.y - cursorLocation.y));
						
					}
					
				}
			}
		};
		
		VICONPort.addListener("/WildPointing/moveTo", laserListener);
		
		iPodListener = new OSCListener() {
			
			public void acceptMessage(Date time, OSCMessage msg) {
				
				String[] parts = msg.getArguments()[0].toString().split(" ");
				
				if (parts.length == 4) {
					
					if ( new Integer(parts[PRESS]).intValue() == 1 ) {
						
						panning = true;
						Viewer.getInstance().startPan();
						
						System.out.println("Panning");
						
						
					} else {
						
						panning = false;
						Viewer.getInstance().stopPan();
						
						System.out.println("Not Panning");
						
						
					}
					
				}
				
			}
			
		};
		
		IPodPort.addListener(PRESS_ADDRESS, iPodListener);

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
