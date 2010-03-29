/**
 * 
 */
package techniques.point;

import java.net.SocketException;
import java.util.Date;

import fr.inria.zuist.cluster.viewer.Viewer;
import techniques.VICONLaserListener;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import fr.inria.zvtm.engine.LongPoint;


/**
 * @author mathieunancel
 *
 */
public class DefaultLaserPoint extends AbstractPointTechnique {
	
	protected OSCPortIn VICONPort;
	protected VICONLaserListener laserListener;
	
	protected LongPoint cursorLocation;
	protected LongPoint pointLocation;
	protected LongPoint previousPointLocation;
	
	/**
	 * @param id
	 * @param o
	 */
	public DefaultLaserPoint(String id, ORDER o, int portIn) {
		super(id, o);
		
		try {
			VICONPort = new OSCPortIn(portIn);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#close()
	 */
	@Override
	public void close() {
		
		VICONPort.close();
		
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#deleteStatLabels()
	 */
	@Override
	public void deleteStatLabels() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#initListeners()
	 */
	@Override
	public void initListeners() {
		
		laserListener = new VICONLaserListener() {
			
			@Override
			public void acceptMessage(Date time, OSCMessage message) {
				super.acceptMessage(time, message);
				//System.out.println("message");
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
						Viewer.getInstance().getMCamera(), 
						(int)previousCoords.x,
						(int)previousCoords.y
				); 
				
				Viewer.getInstance().setCursorPosition(cursorLocation.x, cursorLocation.y);
				Viewer.getInstance().setZoomOrigin(pointLocation.x, pointLocation.y);
				
				// System.out.println("point : " + pointLocation.x + ", " + pointLocation.y);
				
			}
			
		};
		
		VICONPort.addListener("/WildPointing/moveTo", laserListener);
		
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#startListening()
	 */
	@Override
	public void startListening() {
		
		VICONPort.startListening();

	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#stopListening()
	 */
	@Override
	public void stopListening() {
		
		VICONPort.stopListening();

	}

}
