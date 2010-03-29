/**
 * 
 */
package techniques.pan;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.SocketException;
import java.util.Date;

import fr.inria.zuist.cluster.viewer.Viewer;
import package techniques.VICONLaserListener;
import package techniques.zoom.Push2;
import utils.pointedscreen.PointedScreen;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import fr.inria.zvtm.engine.LongPoint;


/**
 * @author mathieunancel
 *
 */
public class MouseButtonLaserPanWithoutWIS extends AbstractPanTechnique {
	
	/** 
	 * Amount of frames used to compute the pointing location
	 */
	public static final int WINDOW_SIZE = 10;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int DX = 3;
	public static final int DY = 4;
	public static final int DZ = 5;

    	public static final int XX = 0, XY = 1, XZ = 2;
    	public static final int YX = 3, YY = 4, YZ = 5;

	protected OSCPortIn VICONPort;
	protected OSCListener positionListener;
	protected MouseAdapter mouseListener;
	
	protected boolean pressed = false;
	
	protected LongPoint cursorLocation;
	protected LongPoint pointLocation;
	protected LongPoint previousPointLocation;
	protected LongPoint previousCoords = new LongPoint(0, 0);
	
	protected PointedScreen pointedScreen;
	
	protected float previousX, previousY, previousZ = previousY = previousX = Integer.MAX_VALUE;
	
	protected float[][] smoothBox = new float[6][WINDOW_SIZE];
	protected int currentIndex = 0;
	
	/**
	 * @param id
	 * @param o
	 */
	public MouseButtonLaserPanWithoutWIS(String id, ORDER o) {
		
		super(id, o);
		
		try {
			VICONPort = new OSCPortIn(Zoom.DEFAULT_PAN_OSC_LISTENING_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		pointedScreen = new PointedScreen("WILDDefault-borders.conf");
		
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#close()
	 */
	@Override
	public void close() {
		
		Zoom.getInstance().getView().getPanel().removeMouseListener(mouseListener);
		VICONPort.close();
		
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#initListeners()
	 */
	@Override
	public void initListeners() {
		
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
								
								previousPointLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
									Zoom.getInstance().getCursorCamera(), 
									(int)previousCoords.x,
									(int)previousCoords.y
								    ); 
							
								Zoom.getInstance().setCursorPosition(cursorLocation.x, cursorLocation.y);
							
								if (pressed) {
								    Zoom.getInstance().zeroOrderTranslate((int)(previousPointLocation.x - cursorLocation.x), (int)(previousPointLocation.y - cursorLocation.y));
								}
							
								previousCoords = new LongPoint(coordinates[X], coordinates[Y]);
								//Zoom.getInstance().setCursorPosition(cursorLocation.x, cursorLocation.y);
								//Zoom.getInstance().setZoomOrigin(pointLocation.x, pointLocation.y);
								
								//System.out.println("point : " + pointLocation.x + ", " + pointLocation.y);
							
							} else {
								System.out.println("Pan Null coordinates" + ((Float)parts[7]).floatValue()
										   + " " + x + " " + y + " " + z + " "
										   + vx + " " + vy + " " + vz  + " "
										   + v2x + " " + v2y + " " + v2z);
							}
							
						} else {
							// System.out.println("Movement too small for pointing : " + distance);
						}
							
					}
					else if (parts != null && parts.length == 6) {
						
						// double distance = Math.sqrt( (x - previousX)*(x - previousX) + (y - previousY)*(y - previousY) + (z - previousZ)*(z - previousZ) );
						
						smoothBox[X][currentIndex] = ((Float)parts[0]).floatValue();
						smoothBox[Y][currentIndex] = ((Float)parts[1]).floatValue();
						smoothBox[Z][currentIndex] = ((Float)parts[2]).floatValue();
						smoothBox[DX][currentIndex] = ((Float)parts[3]).floatValue();
						smoothBox[DY][currentIndex] = ((Float)parts[4]).floatValue();
						smoothBox[DZ][currentIndex] = ((Float)parts[5]).floatValue();
						
//							smoothBox[X][currentIndex] = x - previousX;
//							smoothBox[Y][currentIndex] = y - previousY;
//							smoothBox[Z][currentIndex] = z - previousZ;
						
//						if (previousX != x && previousY != y && previousZ != z) {
//							previousX = x;
//							previousY = y;
//							previousZ = z;
//						}
						
						float x, y, z = y = x = 0; // position
						
						for (int i = 0 ; i < WINDOW_SIZE ; i++) {
							
							x += smoothBox[X][i];
							y += smoothBox[Y][i];
							z += smoothBox[Z][i];
							
						}
						
						x /= WINDOW_SIZE;
						y /= WINDOW_SIZE;
						z /= WINDOW_SIZE;
						
						float vx, vy, vz = vy = vx = 0; // vecteur direction
						
						for (int i = 0 ; i < WINDOW_SIZE ; i++) {
							
							vx += smoothBox[DX][i];
							vy += smoothBox[DY][i];
							vz += smoothBox[DZ][i];
							
						}
						
						vx /= WINDOW_SIZE;
						vy /= WINDOW_SIZE;
						vz /= WINDOW_SIZE;
						
						////////////////// TEST
						
//							vx = ((Float)parts[3]).floatValue();
//							vy = ((Float)parts[4]).floatValue();
//							vz = ((Float)parts[5]).floatValue();
							
						currentIndex = (currentIndex + 1 ) % WINDOW_SIZE;

						///////////////////
						
						double[] coordinates = pointedScreen.computeProjectedCoordinates(x, y, z, vx, vy, vz);
						
						if (coordinates != null) {
						
							// au sens de zvtm
							cursorLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
									Zoom.getInstance().getCursorCamera(),
									(int)coordinates[X],
									(int)coordinates[Y]
							); 
							
							pointLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
									Zoom.getInstance().getMCamera(), 
									(int)coordinates[X],
									(int)coordinates[Y]
							); 
							
							previousPointLocation = Zoom.getInstance().getClusteredView().viewToSpaceCoords(
									Zoom.getInstance().getCursorCamera(), 
									(int)previousCoords.x,
									(int)previousCoords.y
							); 
							
							Zoom.getInstance().setCursorPosition(cursorLocation.x, cursorLocation.y);
							
							if (pressed) {
								Zoom.getInstance().zeroOrderTranslate((int)(previousPointLocation.x - cursorLocation.x), (int)(previousPointLocation.y - cursorLocation.y));
							}
							
							previousCoords = new LongPoint(coordinates[X], coordinates[Y]);
							
						}
							
					}
					
				}
				
			}
			
		};
		
		VICONPort.addListener("/WildPointing/moveTo", positionListener);
		
		mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				//pressed = true;
				
				if(e.getButton() == MouseEvent.BUTTON1)
				{ 
					pressed = true;
					Zoom.getInstance().startPan();
				}
			
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				//pressed = false;
				
				if(e.getButton() == MouseEvent.BUTTON1)
				{ 
					pressed = false;
					Zoom.getInstance().stopPan();
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
		Zoom.getInstance().getView().getPanel().addMouseListener(mouseListener);

	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#stopListening()
	 */
	@Override
	public void stopListening() {
		
		VICONPort.stopListening();
		Zoom.getInstance().getView().getPanel().removeMouseListener(mouseListener);

	}

}
