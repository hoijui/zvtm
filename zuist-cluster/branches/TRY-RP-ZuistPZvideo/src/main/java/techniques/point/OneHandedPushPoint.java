/**
 * 
 */
package techniques.point;

import java.net.SocketException;
import java.util.Date;

import fr.inria.zuist.cluster.viewer.Viewer;
import techniques.zoom.Push2;
import utils.pointedscreen.PointedScreen;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import fr.inria.zvtm.engine.LongPoint;


/**
 * @author mathieunancel
 *
 */
public class OneHandedPushPoint extends AbstractPointTechnique {
	
	public static final int X = 0, Y = 1, Z = 2;
	public static final int XX = 0, XY = 1, XZ = 2;
	public static final int YX = 3, YY = 4, YZ = 5;
	
	/** 
	 * Amount of frames used to compute the pointing location
	 */
	public static final int WINDOW_SIZE = 10;
	
	protected OSCPortIn VICONPort;
	protected OSCListener positionListener, tiltListener;
	
	protected LongPoint objectLocation;
	protected LongPoint previousObjectLocation;
	
	// protected float sin_pitch, sin_roll;
	protected float previousX, previousY, previousZ = previousY = previousX = Integer.MAX_VALUE;
	
	protected float[][] smoothBox = new float[6][WINDOW_SIZE];
	protected int currentIndex = 0;
	
	protected PointedScreen pointedScreen;
	
	/**
	 * @param id
	 * @param o
	 */
	public OneHandedPushPoint(String id, ORDER o, int portIn) {
		super(id, o);
		
		try {
			VICONPort = new OSCPortIn(Zoom.DEFAULT_POINT_OSC_LISTENING_PORT); // TODO pas propre
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		pointedScreen = new PointedScreen("WILDDefault-borders.conf"); // TODO
		
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#close()
	 */
	@Override
	public void close() {
		
		VICONPort.close();
		
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#initListeners()
	 */
	@Override
	public void initListeners() {
		
		/*
		tiltListener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				
				if (msg != null) {
					
					Object[] parts = msg.getArguments();
					
					if (parts != null && parts.length == 3) {
						
						sin_pitch = ((Float)parts[0]).floatValue();
						sin_roll = ((Float)parts[1]).floatValue();
						
					}
					
				}
				
			}
			
		};
		*/
		
		positionListener = new OSCListener() {
			
			public void acceptMessage(Date time, OSCMessage msg) {
				
				if (msg != null) {
					
					// System.out.println("New message to " + msg.getAddress() + ", " + msg.getArguments().length + " arguments.");
					
					Object[] parts = msg.getArguments();
					
					if (parts != null && parts.length == 9) {
						
						float x = ((Float)parts[0]).floatValue();
						float y = ((Float)parts[1]).floatValue();
						float z = ((Float)parts[2]).floatValue();
						
						double distance = Math.sqrt( (x - previousX)*(x - previousX) + (y - previousY)*(y - previousY) + (z - previousZ)*(z - previousZ) );
						
						if (distance > Push2.ZOOM_MIN_MVT) {
							
							smoothBox[XX][currentIndex] = ((Float)parts[3]).floatValue();
							smoothBox[XY][currentIndex] = ((Float)parts[4]).floatValue();
							smoothBox[XZ][currentIndex] = ((Float)parts[5]).floatValue();
							smoothBox[YX][currentIndex] = ((Float)parts[6]).floatValue();
							smoothBox[YY][currentIndex] = ((Float)parts[7]).floatValue();
							smoothBox[YZ][currentIndex] = ((Float)parts[8]).floatValue();
							
//							smoothBox[X][currentIndex] = x - previousX;
//							smoothBox[Y][currentIndex] = y - previousY;
//							smoothBox[Z][currentIndex] = z - previousZ;
							
							if (previousX != x && previousY != y && previousZ != z) {
								previousX = x;
								previousY = y;
								previousZ = z;
							}
							
							float vx, vy, vz = vy = vx = 0; // vecteur direction
							float v2x, v2y, v2z = v2y = v2x = 0; // vecteur direction avec paume vers l'utilisateur
							
							for (int i = 0 ; i < WINDOW_SIZE ; i++) {
								
								vx += smoothBox[YX][i];
								vy += smoothBox[YY][i];
								vz += smoothBox[YZ][i];
								
								v2x += ( smoothBox[XX][i] );//  + smoothBox[YX][i] ) / 2;
								v2y += ( smoothBox[XY][i] );// + smoothBox[YY][i] ) / 2;
								v2z += ( smoothBox[XZ][i] );// + smoothBox[YZ][i] ) / 2;
								
							}
							
							vx /= WINDOW_SIZE;
							vy /= WINDOW_SIZE;
							vz /= WINDOW_SIZE;
							
							v2x /= WINDOW_SIZE;
							v2y /= WINDOW_SIZE;
							v2z /= WINDOW_SIZE;
							
							////////////////// TEST
							
//							vx = ((Float)parts[3]).floatValue();
//							vy = ((Float)parts[4]).floatValue();
//							vz = ((Float)parts[5]).floatValue();
								
							currentIndex = (currentIndex + 1 ) % WINDOW_SIZE;
	
							///////////////////
							
							double[] coordinates = null, coordinates1, coordinates2;
							
							coordinates1 = pointedScreen.computeProjectedCoordinates(x, y, z, vx, vy, vz);
							coordinates2 = pointedScreen.computeProjectedCoordinates(x, y, z, v2x, v2y, v2z);
							
							
							if ( coordinates1 != null && coordinates1[Z] < 0 ) { // VY.y
								coordinates = coordinates1;
							} else if ( coordinates2 != null && coordinates2[Z] > 0 ) {
								coordinates = coordinates2;
							}
							
							if (coordinates != null) {
								
								//coordinates[Y] -= 3500;
							
								// au sens de zvtm
								LongPoint cursorLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
										Zoom.getInstance().getCursorCamera(),
										(int)coordinates[X],
										(int)coordinates[Y]
								);
								
								LongPoint pointLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
										Zoom.getInstance().getMCamera(), 
										(int)coordinates[X],
										(int)coordinates[Y]
								); 
								
		//						LongPoint previousPointLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
		//								Zoom.getInstance().getMCamera(), 
		//								(int)previousCoords.x,
		//								(int)previousCoords.y
		//						); 
								
								// Zoom.getInstance().setCursorPosition(cursorLocation.x, cursorLocation.y);
								Zoom.getInstance().setZoomOrigin(pointLocation.x, pointLocation.y);
								
								//System.out.println("point : " + pointLocation.x + ", " + pointLocation.y);
							
							} else {
								System.out.println("Point Null coordinates" + ((Float)parts[7]).floatValue()
										   + " " + x + " " + y + " " + z + " "
										   + vx + " " + vy + " " + vz  + " "
										   + v2x + " " + v2y + " " + v2z);
							}
							
						} else {
							// System.out.println("Movement too small for pointing : " + distance);
						}
							
					}
					
				}
				
			}
			
		};
		
		VICONPort.addListener("/WildPointing/moveTo", positionListener);
		// VICONPort.addListener("/WildPointing/inclinaison", tiltListener);
		
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
	
	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#deleteStatLabels()
	 */
	@Override
	public void deleteStatLabels() {

	}

}
