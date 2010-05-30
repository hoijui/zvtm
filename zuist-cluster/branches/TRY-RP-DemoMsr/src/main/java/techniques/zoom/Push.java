package techniques.zoom;

import java.awt.geom.Point2D;
import java.net.SocketException;
import java.util.Date;

import fr.inria.zuist.cluster.viewer.Viewer;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

/**
 * Initial Push technique, uses the Y position to compute the zoom factor. 
 * 
 * @author mathieunancel
 *
 */

public class Push extends AbstractZoomTechnique {

	public static final String IN_CMD_ZOOM_FIRSTVALUE_MEMORY = "zoomfvm"; 
	public static final String CMD_STOP = "stop";
	public static final String IN_CMD_STOP = CMD_STOP;
	
	protected OSCPortIn dataReceiver;
	protected OSCListener VICONListener;
	
	public Push(String id, ORDER o, int portIn) {
		
		super(id, o, false);
		
		try {
			this.dataReceiver = new OSCPortIn(portIn);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void initListeners() {
		VICONListener = new OSCListener() {
			public void acceptMessage(Date addr, OSCMessage msg) {
				
				Object[] parts = msg.getArguments();
				String cmd = (String)parts[0];
				
				if (cmd.equals(IN_CMD_ZOOM_FIRSTVALUE_MEMORY)){
					
					float xValue = ((Float) parts[1]).floatValue();
					float yValue = ((Float)parts[2]).floatValue();
					
					//if(!Viewer.getInstance().isViewering()) { 
						refPoint = new Point2D.Float(xValue, yValue);
					//}
					float zoomValue = (yValue - ((float)refPoint.getY()));
					
					if (order.equals(ORDER.FIRST)) {
						
						System.out.println("firstOrderViewerValue: " + zoomValue);
						//Viewer.getInstance().firstOrderViewer(zoomValue);
						
					} else if (order.equals(ORDER.ZERO)) {
						
						System.out.println("zeroOrderZoomValue: " + zoomValue);
						Viewer.getInstance().zeroOrderZoom(zoomValue);
						
						refPoint = new Point2D.Float(xValue, yValue);
					}

				}
				else if (cmd.equals(IN_CMD_STOP))
				{
					//Viewer.getInstance().firstOrderStop();
					System.out.println("stop");
				}
				
			}
		};
		
		dataReceiver.addListener(Viewer.MOVE_CAMERA, VICONListener);
	}
	
	@Override
	public void startListening() {
		
		dataReceiver.startListening();
		
	}
	
	@Override
	public void stopListening() {
		
		dataReceiver.stopListening();
		dataReceiver.close();
		
	}
	
	@Override
	public void close() {
		
		dataReceiver.close();
		
	}

}
