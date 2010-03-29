package techniques.zoom;

import java.awt.geom.Point2D;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import techniques.FitEllipse;
import fr.inria.zuist.cluster.viewer.Viewer;
import techniques.pan.IPodPressLaserPan;
import utils.dispatchOSC.OSCDispatcher;
import utils.transfer.SigmoidTF;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;


public class TurningIPod extends AbstractViewerTechnique {
	
	public static final int ID = 0, X = 1, Y = 2, PRESS = 3;
	
	/**
	 * Max size of the directionObservationFrame.
	 * Amount of the last computed directions (clockwise or counter-clockwise) that have to be equal.
	 * If they are not, the previous direction is used. 
	 */
	public static final int DIRECTION_OBSERVATION_FRAME_SIZE = 10;
	
	/**
	 * Size of the steps (angle differences) buffer. 
	 * When full, the final zoom value is the median of this buffer's values.
	 */
	public static final int SMOOTHING_FRAME_SIZE = 5; //should be odd. TODO: add a odd/even test and change median calculation
	
	public static final int NB_ELLIPSE_POINTS = 20;
	
	/**
	 * Minimum distance between the last glove coordinates and the current ones.
	 * If the actual distance is below this value, the glove coordinates are ignored.
	 */
	public static final int MIN_PASS_VALUE = 1;
	
	/**
	 * Maximum distance between the reference point and the new glove coordinates.
	 * If this distance is exceeded, a new reference point is created from the glove coordinates.
	 */
	public static final int MAX_DISTANCE = 500;
	
	/**
	 * Maximum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If this value is exceeded, the current glove coordinates are ignored.
	 */
	public static final float MAX_STEP = .8f;
	
	/**
	 * Minimum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If the current angle is below this value, the current glove coordinates are ignored.
	 * If this value is 0, there is a risk that the zooming movement never stops.
	 */
	public static final float MIN_STEP = 0.00f;
	
	/**
	 * Requested min zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MIN_ZOOM_FACTOR = 0;
	
	/**
	 * Requested max zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MAX_ZOOM_FACTOR = 30;
	
	
	
	public static final String TOUCHPAD_ADDRESS = "/zoompan/xy1";
	
	public static final int MIN_MVT = 0;
	
	/* turning Wheel gesture */
	protected ArrayList<Point2D> points = new ArrayList<Point2D>();
	protected Vector<Double> smoothingStepsBuffer = new Vector<Double>(SMOOTHING_FRAME_SIZE);
	protected ArrayList <Integer> directionObservationFrame = new ArrayList<Integer>();
	protected FitEllipse.ImplicitParams params;
	protected FitEllipse.EllipseParams ellipse;
	protected Point2D[] tmpPoints;
	protected Point2D cur;
	protected double lastAngle=0.0;
	protected float lastX=0,lastY=0;		
	protected Point2D pointBuffer [] = new Point2D [6];
	protected int pointBufferIndex=0;
	protected int turningCW;
	// private Point2D.Double centerPoint= new Point2D.Double(0,0);

	protected double lastStep = 0;

	protected OSCPortIn dataReceiver;
	protected OSCListener iPodListener;
	
	protected SigmoidTF transferFunction = new SigmoidTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	// protected MultTF transferFunction = new MultTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	
	protected OSCDispatcher dispatcher = new OSCDispatcher(
			Viewer.IPOD_DEFAULT_OSC_LISTENING_PORT, 
			new int[] {
					Viewer.IPOD_ZOOM_OSC_LISTENING_PORT, 
					Viewer.IPOD_POINT_OSC_LISTENING_PORT, 
					Viewer.IPOD_PAN_OSC_LISTENING_PORT
			}
	);
	
	
	public TurningIPod(String id, ORDER o) {
		super(id, o, false);
		
		try {
			this.dataReceiver = new OSCPortIn(Viewer.IPOD_ZOOM_OSC_LISTENING_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		params = null;
		ellipse = null;
		cur = null;
		
		Point2D.Double point = new Point2D.Double(0,0);
		Arrays.fill(pointBuffer, point);
		
		transferFunction.setLambda(1f);
		transferFunction.setXOffset(.2f);
		// transferFunction.setMultiplier(2);
		
		System.out.println("Turning iPod");
		
	}
	
	/* -------------------turning wheel gesture ------------*/
	void turningWheel(float x, float y)
	{	
		
		// System.out.println("TurningWheel("+ x +", " + y + ")");
		
		if (Math.abs(x - lastX) < MIN_PASS_VALUE && Math.abs(y - lastY) < MIN_PASS_VALUE) {
			// System.out.println("TurningWheel canceled - 1 (Coordinates not far enough from previous position)");
			return;
		}

		Point2D.Double point = new Point2D.Double(x,y);
		points.add(point);

		/* users might move, or points might be added to the array 
		 * that are not supposed to be whithin the cyclic movement*/ 
		if(Math.abs(refPoint.getX() - x) > MAX_DISTANCE || Math.abs(refPoint.getY() - y) > MAX_DISTANCE){
			refPoint.setLocation(x,y);
			points.clear();
		}
		
//		if (points.size() > NB_ELLIPSE_POINTS) {
//			points.remove(0);
//		}
		
		Point2D[] pointsArray = new Point2D[points.size()];
		
		if(!points.contains(new Point2D.Double(0,0)) && points.size()!=0) {
			
			params = FitEllipse.fitImplicit(points.toArray(pointsArray));
			double Angle=0.0;
			Point2D.Double closestPoint = new Point2D.Double(x,y);

			if (params  != null) {

				ellipse = FitEllipse.implicitToParametric(params);
				ellipse.computeDistance(x, y, closestPoint);
				Angle = ellipse.computeAngle(closestPoint);

				Integer resultingDirection = new Integer(0);
				if (lastAngle - Angle > 0) {
					resultingDirection = new Integer(1);
				}
				else if (lastAngle - Angle < 0) {
					resultingDirection = new Integer(0);
				}

				/*
    			  the directionObservationFrame observes the direction value over DIRECTION_OBSERVATION_FRAME_SIZE values and 
    			  limits noise direction changes.
				 */
				directionObservationFrame.add(resultingDirection);
				
				if (directionObservationFrame.size() == DIRECTION_OBSERVATION_FRAME_SIZE) {
					
					/*
					 * If directionObservationFrame contains only zeros or ones, the first direction value is used and then removed from the List.
					 * When directionObservationFrame contains different values, the turningCW value doesn't change until there's only equal values.
					 */
					if( !( directionObservationFrame.contains(0) && directionObservationFrame.contains(1) ) ) {
						
						turningCW = directionObservationFrame.get(0).intValue();
						
					}
					
					directionObservationFrame.remove(0);
				}
				
				double step = Math.abs(Angle - lastAngle);

				if(step > MAX_STEP || step < MIN_STEP) {
					lastAngle = Angle;
					
					// System.out.println("TurningWheel canceled - 4 (Angle difference not in accepted boundaries [" + MIN_STEP + " ; " + MAX_STEP + "] : " + step + ")");
					
					// return;
					
					// TODO ˆ tester ###############
					if (step > MAX_STEP) {
						step = lastStep; 
					}
					
				} else {
					
					lastStep = step;
					
				}
				
				// #################################

				Double smoothedStepValue;
				smoothingStepsBuffer.add(new Double(step));
				
//				if(smoothingStepsBuffer.size() == SMOOTHING_FRAME_SIZE) {
//					
//					Vector<Double> smoothingSteps = new Vector<Double>(smoothingStepsBuffer);
//					Collections.sort(smoothingSteps);
//					smoothedStepValue = smoothingSteps.elementAt((smoothingSteps.size()/2 + 1));//return object in the middle, median.
//					smoothingStepsBuffer.remove(0);
//					
//				}
//				else { 
//					smoothedStepValue = new Double(step);
//				}
				
				double meanStep=0;
				for(int i=0; i<smoothingStepsBuffer.size(); i++)
				{
					meanStep = meanStep + smoothingStepsBuffer.elementAt(i);
				}
				meanStep = meanStep / smoothingStepsBuffer.size();
				smoothedStepValue = meanStep;

				lastAngle = Angle;
				float zoomValue;
				
				if (!IPodPressLaserPan.isPanning()) {
				
					if (turningCW == 1) {
						
						zoomValue = transferFunction.compute(smoothedStepValue.floatValue()); // * ZOOM_MULTIPLIER;
						Viewer.getInstance().zeroOrderViewer(zoomValue);
						// System.out.println("zoom in "+ zoomValue);
						
						addAltitudeSample();
							
					}
					else if (turningCW == 0) {
						
						zoomValue = -transferFunction.compute(smoothedStepValue.floatValue()); // * ZOOM_MULTIPLIER;
						Viewer.getInstance().zeroOrderViewer(zoomValue);
						// System.out.println("zoom out "+ zoomValue);
						
						addAltitudeSample();
						
					} else {
						// System.out.println("TurningWheel canceled - 5 (Unknown turning direction : " + turningCW + ")");
					}
					
				} else {
					// System.out.println("TurningWheel canceled - 6 (Panning)");
				}
				
			} else {
				// System.out.println("TurningWheel canceled - 3 (params[] is null)");
			}
			
		} else {
			// System.out.println("TurningWheel canceled - 2 (points is empty or contains a (0,0) point)");
		}

	}
	
	@Override
	public void initListeners() {
		iPodListener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				
				System.out.println("Message OSC " + msg.getAddress() + ", " + msg.getArguments().length + " args");
				
				String[] parts = msg.getArguments()[0].toString().split(" ");
				
				if (parts.length == 4) {
					
					if ( new Integer(parts[PRESS]).intValue() == 1 ) {
					
						int x = new Integer(parts[X]).intValue();
						int y = new Integer(parts[Y]).intValue();
						
						turningWheel(x, y);
					
					} else {
						
						System.out.println("iPod release");
						
						points.clear();
						directionObservationFrame.clear();
						turningCW = -1;
						
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
		
		System.out.println("Listeners listening");
		
		dispatcher.dispatch();
		
	}
	
	@Override
	public void stopListening() {
		
		dataReceiver.stopListening();
		dataReceiver.close();
		
		dispatcher.stop();
		
	}
	
	@Override
	public void close() {
		
		dataReceiver.close();
		dispatcher.close();
		
	}
	
	
}
