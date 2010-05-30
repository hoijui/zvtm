/**
 * 
 */
package techniques;

import java.awt.Dimension;
import java.util.Date;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

import fr.inria.zvtm.engine.LongPoint;


/**
 * @author mathieunancel
 *
 */
public class VICONLaserListener implements OSCListener {

	public static final int X = 1, Y = 2;
	protected static final int SCREEN = 0;
	protected static final int Z = 3;
	protected static final int DIST = 4;
	
	protected static final Dimension screenP = new Dimension(2760, 1840);
	
	protected double norm;
	protected String screenName;
	protected float screenX, screenY;
	protected double screenCoordsX, screenCoordsY;
	
	protected LongPoint currentCoords = new LongPoint(0, 0);
	protected LongPoint previousCoords = new LongPoint(0, 0);

	/* (non-Javadoc)
	 * @see pzwallzoom.techniques.AbstractTechnique#initListeners()
	 */
	/**
	 * TODO
	 */
	public void acceptMessage(Date time, OSCMessage message) {
				
		//System.out.println("time : " + time);
		
		if (message != null && message.getArguments() != null && message.getArguments().length > 2) {
			
			boolean quit = false;
			
			for (Object o : message.getArguments()) {
				if (o == null) {
					quit = true;
				}
			}
			
			if (!quit) {
			
				//norm = new Double(message.getArguments()[DIST].toString()).doubleValue();
				screenName = message.getArguments()[SCREEN].toString();
				
				try {
					
					screenX = ((Float)(message.getArguments()[X])).floatValue();
					screenY = ((Float)(message.getArguments()[Y])).floatValue();
					
				} catch (java.lang.ClassCastException e) {
					
					System.err.println("Warning : casting problem in VICONLaserListener");
					
					screenX = ((Integer)(message.getArguments()[X])).floatValue();
					screenY = ((Integer)(message.getArguments()[Y])).floatValue();
					
				}
					
				screenCoordsX = -1;
				screenCoordsY = -1;
				
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
				
				try {
					screenCoordsY = new Integer(""+screenName.charAt(1)).intValue();
				} catch (StringIndexOutOfBoundsException e) {
					System.err.println("Incorrect value form screenName : " + screenName);
				}
				
				previousCoords = currentCoords;
				
				currentCoords = new LongPoint(screenCoordsX * screenP.width + screenX, screenCoordsY * screenP.height + screenY);
				
			} else {
				System.out.println("No !");
			}
			
		} else {
			System.out.println("Incorrect message");
			System.out.println("Message to " + message.getAddress() + " : ");
			for (Object o : message.getArguments()) {
				
				System.out.print(o + ", ");
				
			}
			System.out.println("\n");
		}

	}
	
	public LongPoint getCurrentCoords() {
		return currentCoords;
	}

}
