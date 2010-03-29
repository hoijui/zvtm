package techniques.zoom;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import techniques.FitEllipse;
import fr.inria.zuist.cluster.viewer.Viewer;
import utils.transfer.SigmoidTF;
import utils.transfer.MultTF;

import com.illposed.osc.OSCListener;
import techniques.VICONLaserListener;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import fr.inria.zvtm.engine.LongPoint;


public class TurningWheel2Handed extends AbstractViewerTechnique {

	/*
	 * TODO
	 * 		- Question : can the turninCW values be equal to {-1, 1} instead of {0, 1} ? 
	 * 		This way we could use them directly in the computations of the zoom factor. 
	 */
	
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
	
	/**
	 * Minimum distance between the last glove coordinates and the current ones.
	 * If the actual distance is below this value, the glove coordinates are ignored.
	 */
	public static final int MIN_PASS_VALUE = 5;
	public static final int MIN_PASS_VALUE_ONEHANDED = 50;
	
	/**
	 * Maximum distance between the reference point and the new glove coordinates.
	 * If this distance is exceeded, a new reference point is created from the glove coordinates.
	 */
	public static final int MAX_DISTANCE = 500;
	public static final int MAX_DISTANCE_ONEHANDED = 5000;
	
	/**
	 * Maximum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If this value is exceeded, the current glove coordinates are ignored.
	 */
	public static final float MAX_STEP = .9f;//= .4f;
	
	/**
	 * Minimum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If the current angle is below this value, the current glove coordinates are ignored.
	 * If this value is 0, there is a risk that the zooming movement never stops.
	 */
	public static final float MIN_STEP = 0.005f;
	
	/**
	 * Requested min zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MIN_ZOOM_FACTOR = 0;
	
	/**
	 * Requested max zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MAX_ZOOM_FACTOR = 20;
	
	
	/**
	 * multiplier value for the MultiplierFunction. 
	 * used to change the zoom velocity in turningWheel.
	 */
	public static final float MULTIPLIER_VALUE = 30;
	
	
	public static final String IN_CMD_TURNING_WHEEL = "twheel";
	public static final String IN_CMD_TURNING_WHEELONSCREEN = "twheelOnScreen";
	
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
	
	protected boolean zoomMouseEvent=false;
	// private Point2D.Double centerPoint= new Point2D.Double(0,0);
	protected int max_distance=0;
	protected int min_pass_value=0;

	protected OSCPortIn dataReceiver;
	protected OSCListener VICONListener;
	protected MouseAdapter mouseListener;
	
	protected int portIn;
	
	
	protected SigmoidTF transferFunction = new SigmoidTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	protected MultTF MultiplierFunction = new MultTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	
	protected Date date;
	protected long timestamp;
	
	protected LongPoint cursorLocation;
	protected LongPoint pointLocation;
	
	public TurningWheel2Handed(String id, ORDER o, int portIn) {
		
		super(id, o, false);
		
		System.out.println("new TurningWheel2Handed()");
		
		this.portIn = portIn;
		
		try {
			this.dataReceiver = new OSCPortIn(portIn);
			System.out.println("dataReceiver created : " + dataReceiver + " " + portIn);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		params = null;
		ellipse = null;
		cur = null;
		
		Point2D.Double point = new Point2D.Double(0,0);
		Arrays.fill(pointBuffer, point);
		
		transferFunction.setLambda(2f);
		transferFunction.setXOffset(.2f);
		MultiplierFunction.setMultiplier(MULTIPLIER_VALUE);
		
		Date date = new Date();
		timestamp = date.getTime();
		
		
	}
	
	/* -------------------turning wheel gesture ------------*/
	void turningWheel(float x, float y)
	{	
		
		
		System.out.println("x "+ x +"\ny " + y);
		if (Math.abs(x - lastX) < min_pass_value && Math.abs(y - lastY) < min_pass_value) {
			System.out.println("TurningWheel canceled - 1 (Coordinates not far enough from previous position)");
			return;
		}
		//else System.out.println("Value passed with values"+ Math.abs(x - lastX) + " " +  Math.abs(y - lastY));
		
		/* users might move, or points might be added to the array 
		 * that are not supposed to be whithin the cyclic movement*/ 
		
		//max_distance is set by either VICONListener or VICONLaserListener.
		if(Math.abs(refPoint.getX() - x) > max_distance || Math.abs(refPoint.getY() - y) > max_distance){
			System.out.println("max distance of "+max_distance+" was reached. The Value x was "+ Math.abs(refPoint.getX() - x) + " y was "+Math.abs(refPoint.getY() - y));
			refPoint.setLocation(x,y);
			points.clear();
		}
		

		Point2D.Double point = new Point2D.Double(x,y);
		points.add(point);

//		if(points.size() > 20)
//		{
//			points.remove(0);
//			
//		}
		Point2D[] pointsArray = new Point2D[points.size()];

		if(!points.contains(new Point2D.Double(0,0)) && points.size()!=0) {
			
			params = FitEllipse.fitImplicit(points.toArray(pointsArray));
			
			double Angle=0.0;
			Point2D.Double closestPoint = new Point2D.Double(x,y);

			if (params  != null) {

				ellipse = FitEllipse.implicitToParametric(params);
				ellipse.computeDistance(x, y, closestPoint);
				//System.out.println(ellipse.toString());
				
				
	
				Angle = ellipse.computeAngle(closestPoint);

				Integer resultingDirection = new Integer(0);
				if (lastAngle - Angle > 0) {
					resultingDirection = new Integer(1);
				}
				else if (lastAngle - Angle < 0) {
					resultingDirection = new Integer(0);
				}

				
				double step = Math.abs(Angle - lastAngle);
				//System.out.println("step: "+ step);
				if(step > MAX_STEP){ //|| step < MIN_STEP) {
					lastAngle = Angle;
					System.out.println("TurningWheel canceled - 4 (Angle difference not in accepted boundaries [" + MIN_STEP + " ; " + MAX_STEP + "] : " + step + ")");
					//return;
				}
				else
				{
					smoothingStepsBuffer.add(new Double(step));
				}
				Double smoothedStepValue;
				
				
				double meanStep=0;
				for(int i=0; i<smoothingStepsBuffer.size(); i++)
				{
					meanStep = meanStep + smoothingStepsBuffer.elementAt(i);
				}
				meanStep = meanStep / smoothingStepsBuffer.size();
				smoothedStepValue = meanStep;
				
				//if(smoothingStepsBuffer.size() == SMOOTHING_FRAME_SIZE) {
					//Vector<Double> smoothingSteps = new Vector<Double>(smoothingStepsBuffer);
//					Collections.sort(smoothingSteps);
//					smoothedStepValue = smoothingSteps.elementAt((smoothingSteps.size()/2 + 1));//return object in the middle, median.
//					smoothingStepsBuffer.remove(0);
					
				//}
				//else { 
					//smoothedStepValue = new Double(step);
				//}
				
				lastAngle = Angle;
				lastX = x;
				lastY = y;
				float zoomValue;
				
				
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
				
				
				if (turningCW==1) {
					zoomValue = -MultiplierFunction.compute(smoothedStepValue.floatValue()); // * ZOOM_MULTIPLIER;
					Viewer.getInstance().zeroOrderViewer(zoomValue);
					//System.out.println("zoom in "+ zoomValue);
					
					addAltitudeSample();
						
				}
				else if (turningCW==0) {
					
					zoomValue = MultiplierFunction.compute(smoothedStepValue.floatValue()); // * ZOOM_MULTIPLIER;
					Viewer.getInstance().zeroOrderViewer(zoomValue);
					//System.out.println("zoom out "+ zoomValue);
					
					addAltitudeSample();
					
				} else {
					System.out.println("TurningWheel canceled - 5 (Unknown turning direction : " + turningCW + ")");
				}
				
			} else {
				System.out.println("TurningWheel canceled - 3 (params[] is null)");
			}
			
		} else {
			System.out.println("TurningWheel canceled - 2 (points is empty or contains a (0,0) point)");
		}

	}
	
	@Override
	public void initListeners() {
		
		
		

		VICONListener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				
				Object[] parts = msg.getArguments();
				String cmd = (String)parts[0];
				max_distance = MAX_DISTANCE;
				min_pass_value = MIN_PASS_VALUE;
				//System.out.println("message from zoom-glove with command " + cmd);
//				System.out.println("New message : ");
//				for (Object o : parts) {
//					System.out.print(o + " ");
//				}
//				System.out.println("\n(" + IN_CMD_TURNING_WHEEL + ")");
				
				if (cmd.equals(IN_CMD_TURNING_WHEEL)) {
					if(zoomMouseEvent)turningWheel(((Float)parts[1]).floatValue(), ((Float)parts[2]).floatValue());
					//else(panGesture)
				}
				
			}
		};
		
		mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3) 
				{
					zoomMouseEvent = true;
					Viewer.getInstance().startViewer();
				}
				
				/*
				else if(e.getButton() == MouseEvent.BUTTON1)
				{
					if(!points.isEmpty()) points.clear();
					directionObservationFrame.clear();
					//smoothingStepsBuffer.clear();
					turningCW = -1;
					zoomMouseEvent = false;
				}
				*/
					
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				//pressed = false;
				if(e.getButton() == MouseEvent.BUTTON3) 
					zoomMouseEvent = false;
					points.clear();
					directionObservationFrame.clear();
					turningCW = -1;
					Viewer.getInstance().stopViewer();
					//smoothingStepsBuffer.clear();
			}

		};
		
		System.out.println(dataReceiver + ", " + VICONListener);
		System.out.println(Viewer.MOVE_CAMERA);
		
		if (dataReceiver == null) {
			
			System.out.println("This shouldn't happen !");
			
			try {
				this.dataReceiver = new OSCPortIn(portIn);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		dataReceiver.addListener(Viewer.MOVE_CAMERA, VICONListener);
		
		System.out.println("Listeners initialized");
	}
	
	@Override
	public void startListening() {
		
		dataReceiver.startListening();
		
		Viewer.getInstance().getView().getPanel().addMouseListener(mouseListener);
		
		System.out.println("Listeners listening");
		
	}
	
	@Override
	public void stopListening() {
		
		dataReceiver.stopListening();
		Viewer.getInstance().getView().getPanel().removeMouseListener(mouseListener);
		// dataReceiver.close();
		
	}
	
	@Override
	public void close() {
		
		dataReceiver.close();
		System.out.println("TurningWheel dataReceiver closed");
		
	}
	
	
}
