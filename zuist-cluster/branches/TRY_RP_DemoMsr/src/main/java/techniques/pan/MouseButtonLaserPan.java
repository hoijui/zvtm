/**
 * 
 */
package techniques.pan;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
public class MouseButtonLaserPan extends AbstractPanTechnique {
	
	protected OSCPortIn VICONPort;
	protected VICONLaserListener laserListener;
	protected MouseAdapter mouseListener;
	
	protected boolean pressed = false;
	
	protected LongPoint cursorLocation;
	protected LongPoint pointLocation;
	protected LongPoint previousPointLocation;
	
	/**
	 * @param id
	 * @param o
	 */
	public MouseButtonLaserPan(String id, ORDER o, int portIn) {
		
		super(id, o);
		
		try {
			VICONPort = new OSCPortIn(portIn);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#close()
	 */
	@Override
	public void close() {
		
		Viewer.getInstance().getView().getPanel().removeMouseListener(mouseListener);
		VICONPort.close();
		
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#initListeners()
	 */
	@Override
	public void initListeners() {
		
		laserListener = new VICONLaserListener() {
			@Override
			public void acceptMessage(Date time, OSCMessage msg) {
				
				super.acceptMessage(time, msg);
				
				//System.out.println("New message to " + msg.getAddress() + ", " + msg.getArguments().length + " arguments.");
				
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
				//System.out.println("cursor : " + cursorLocation.x + ", " + cursorLocation.y);
				
				if (pressed) {
					Viewer.getInstance().zeroOrderTranslate((int)(previousPointLocation.x - cursorLocation.x), (int)(previousPointLocation.y - cursorLocation.y));
				}
			}
		};
		
		VICONPort.addListener("/WildPointing/moveTo", laserListener);
		
		mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				//pressed = true;
				
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					pressed = true;
					Viewer.getInstance().startPan();
				}
			
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				//pressed = false;
				
				if(e.getButton() == MouseEvent.BUTTON1)
				{ 
					pressed = false;
					Viewer.getInstance().stopPan();
				}
			}

		};

	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#startListening()
	 */
	@Override
	public void startListening() {
		
		VICONPort.startListening();
		Viewer.getInstance().getView().getPanel().addMouseListener(mouseListener);

	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#stopListening()
	 */
	@Override
	public void stopListening() {
		
		VICONPort.stopListening();
		Viewer.getInstance().getView().getPanel().removeMouseListener(mouseListener);

	}

}
