package techniques.zoom;

import java.net.SocketException;
import java.util.Date;

import fr.inria.zuist.cluster.viewer.Viewer;
import utils.transfer.SigmoidTF;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;


public class TurningWheel2 extends AbstractViewerTechnique {
	
	
	public static final double TWO_PI = 2 * Math.PI;
	
	
	/**
	 * Maximum difference between the previous D value and the current one.
	 * If this value is exceeded, the current glove coordinates are ignored.
	 */
	public static final float MAX_STEP = .5f;
	
	/**
	 * Minimum difference between the previous D value and the current one.
	 * If the current D is below this value, the current glove data are ignored.
	 * If this value is 0, there is a risk that the zooming movement never stops.
	 */
	public static final float MIN_STEP = 0.01f;
	
	/** 
	 * Maximum difference accepted between two consecutive steps with opposite directions.
	 * Compared to |previousStep| + |currentStep|
	 */
	public static final float MAX_OPPOSITE_DIFFERENCE = 2f;
	
	/** 
	 * Maximum difference accepted between two consecutive D values.
	 * If the absolute value of this difference is greater than MAX_D_DIFFERENCE, we compute the angle step as the opposite (see code).
	 */
	public static final float MAX_D_DIFFERENCE = (float)Math.PI;
	
	/**
	 * Requested min zoom factor.
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MIN_ZOOM_FACTOR = 0;
	
	/**
	 * Requested max zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MAX_ZOOM_FACTOR = 100;
	
	/**
	 * Minimum sine pitch value below which the yaw isn't taken into account.
	 * Can be used to create a constant neutral position
	 */
	public static final float MIN_SIN_PITCH = -1f;
	
	/**
	 * Number of samples used to compute the zoom factor.
	 */
	public static final int NB_SMOOTHING_SAMPLES = 3;
	
	
	
	protected OSCPortIn dataReceiver;
	protected OSCListener VICONListener;
	
	/**
	 * A pseudo-distance computed from the yaw value and the sine pitch.
	 * Represents roughly the distance of the actual glove position compared to the yaw angle zero on a circle which radius is one.
	 * Technically, should be between 0 and TWO_PI. The closer the glove is to the horizontal plan, the higher the D value gets.
	 */
	protected float d;
	
	/**
	 * Sine of the pitch value of the object :
	 * <ul>
	 * <li>If it points to the floor, the sine will be around -1.</li>
	 * <li>If it points horizontally, the sine will be around 0.</li>
	 * <li>If it points to the sky, the sine will be around 1.</li>
	 * </ul>
	 */
	protected float sinus_pitch = -2;

	/**
	 * Difference between two consecutive D values.
	 */
	protected float step = 0;
	
	protected float previous_d = 0;
	protected float previous_step = 0;
	
	protected double zoomFactorP;
	protected double zoomFactorN;
	protected double zoomFactor;
	protected int sens;
	
	/**
	 * Table containing step samples that are used to compute the zoom factor.
	 */
	protected double[] smoothBox = new double[NB_SMOOTHING_SAMPLES];
	protected int currentSmoothBoxIndex = -1;
	
	protected boolean alreadyMentionnedPitch = false;
	protected boolean alreadyMentionnedBounds = false;
	
	protected SigmoidTF transferFunction = new SigmoidTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	
	
	public TurningWheel2(String id, ORDER o, int portIn) {
		super(id, o, false);
		
		try {
			this.dataReceiver = new OSCPortIn(portIn);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		transferFunction.setLambda(2f);
		transferFunction.setXOffset(.2f);
		
	}
	
	/* -------------------turning wheel gesture ------------*/
	
	void turningWheel()
	{	
		
		if (sinus_pitch > MIN_SIN_PITCH) {
			
			alreadyMentionnedPitch = false;
			
			// Computing the D difference
			
			step = (d - previous_d);
			
			
			// Checking that the step value is realistic, i.e. in the bounds we expect it to be.
			// If it's gone "too far", we take the opposite angle
			
			if ( step > MAX_D_DIFFERENCE ) {
				step = -( (d % MAX_D_DIFFERENCE) + (previous_d % MAX_D_DIFFERENCE) );
			} else if ( step < -MAX_D_DIFFERENCE ) {
				step = (d % MAX_D_DIFFERENCE) + (previous_d % MAX_D_DIFFERENCE);
			}
			
			
			// In this case you can't take previous_d into account (it is updated lower in the code)
			
			if (currentSmoothBoxIndex == -1) {
				
				currentSmoothBoxIndex++;
				
				alreadyMentionnedBounds = false;
				
			} 
			
			// Checking that there hasn't be a too brutal change of direction
			
			else if ( 
					step * previous_step < 0 // i.e. opposite directions 
					&& Math.abs(previous_step) + Math.abs(step) > MAX_OPPOSITE_DIFFERENCE ) { // Too much difference
				
				// This step is not taken into account (the difference is probably due to missed steps)
				// (previous_d is updated lower in the code)
				
				System.out.println("Too much reverse difference : |" + previous_step + "| + |" + step + "| = " + (Math.abs(previous_step) + Math.abs(step)));
				
			} 
			
			// Checking that the steps are in the accepted bounds for the transfer function(s).
			
			else if (Math.abs(step) > MIN_STEP && Math.abs(step) < MAX_STEP) {
				
				alreadyMentionnedBounds = false;
				
				// Filling the smoothBox
				
				smoothBox[currentSmoothBoxIndex % NB_SMOOTHING_SAMPLES] = step;
				currentSmoothBoxIndex++;
				
				// No computation until the smoothBox is full
				
				if (currentSmoothBoxIndex > NB_SMOOTHING_SAMPLES) {
				
					// Computing the major direction and zoomFactors
					
					sens = 0;
					zoomFactorP = zoomFactorN = 0;
					
					for (double v : smoothBox) {
						if (v > 0) {
							sens ++;
							zoomFactorP += v;
						} else if (v < 0) {
							sens --;
							zoomFactorN -= v; // it's a MINUS, so the result will be POSITIVE
						}
					}
					
					// Computing the smoothed zoom factor
					
					if (sens != 0) {
					
						if (sens > 0) {
							zoomFactor = zoomFactorP;
							System.out.print("+");
						} else if (sens < 0) {
							zoomFactor = zoomFactorN;
							System.out.print("-");
						}
						
						zoomFactor /= sens; // Here the result will be either positive or negative, corresponding to the rotation direction.
						
						sens = (int)Math.signum(sens); // Mapped to {-1, 1}.
						
						// Applying the chosen transfer function
						
						zoomFactor = sens * transferFunction.compute( (float)zoomFactor );
						
						// Calling the zoom method
						
						Viewer.getInstance().zeroOrderViewer( (float)zoomFactor );
						
						// Stats stuff
						
						addAltitudeSample();
						addViewerSample( (float)zoomFactor );
						
						// Updating previous_step
						
						previous_step = step;
					
					} else {
						
						System.out.println("No direction available");
						
					}
				
				} else {
					
					// System.out.println("Not enough samples : " + currentSmoothBoxIndex);
					
				}
			
			} else {
				
				//if (!alreadyMentionnedBounds) {
					//System.out.println("Absolute value of step is outside its boundaries : " + Math.abs(step));
					System.out.print("!");
				//	alreadyMentionnedBounds = true;
				//}
					
			}
			
			// Updating previous_d
			
			previous_d = d;
			
		} else {
			
			if (!alreadyMentionnedPitch) {
				System.out.println("Not enough pitch : " + sinus_pitch);
				alreadyMentionnedPitch = true;
			}
			
			// In this case we ignore the values and reset the smoothBox
			
			currentSmoothBoxIndex = -1;
			
		}

	}
	
	@Override
	public void initListeners() {
		VICONListener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				
				Object[] parts = msg.getArguments();
				
				System.out.println("New message : ");
				System.out.println("\t sinus pitch : " + parts[0]);
				System.out.println("\t sinus roll : " + parts[1]);
				System.out.println("\t yaw : " + parts[2]);
				
				sinus_pitch = ((Float)parts[0]).floatValue();
				
				// d = yaw * (1 - sinus_pitch)
				d = ((Float)parts[2]).floatValue() * 1 - sinus_pitch;
				
				turningWheel();
				
			}
		};
		
		dataReceiver.addListener("/inclinaison", VICONListener);
		
		System.out.println("Listeners initialized");
	}
	
	@Override
	public void startListening() {
		
		dataReceiver.startListening();
		
		System.out.println("Listeners listening");
		
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
