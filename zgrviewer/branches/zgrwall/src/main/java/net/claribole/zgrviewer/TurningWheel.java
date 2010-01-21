package net.claribole.zgrviewer;

import java.net.SocketException;
import java.util.Date;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

/*
 * TODO
 * Redo the javadocs and constant names for yaw -> d
 */

public class TurningWheel{
	
	
	public static final double TWO_PI = 2 * Math.PI;
	
	
	/**
	 * Maximum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If this value is exceeded, the current glove coordinates are ignored.
	 */
	public static final float MAX_STEP = .5f;
	
	/** 
	 * Maximum difference accepted between two consecutive steps with opposite directions.
	 * Compared to |previousStep| + |currentStep|
	 */
	public static final float MAX_OPPOSITE_DIFFERENCE = 2f;
	
	/** 
	 * Maximum difference accepted between two consecutive yaw values. 
	 * If the absolute value of this difference is greater than MAX_YAW_DIFFERENCE, we compute the angle step as the opposite (see code).
	 */
	public static final float MAX_YAW_DIFFERENCE = (float)Math.PI;
	
	/**
	 * Minimum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If the current angle is below this value, the current glove coordinates are ignored.
	 * If this value is 0, there is a risk that the zooming movement never stops.
	 */
	public static final float MIN_STEP = 0.01f;
	
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
	 */
	public static final float MIN_SIN_PITCH = -1f;
	
	/**
	 * Number of samples used to compute the zoom factor.
	 */
	public static final int NB_SMOOTHING_SAMPLES = 5;
	
	
	
	protected OSCPortIn dataReceiver;
	protected OSCListener VICONListener;
	
	/**
	 * The angle we're interested about. Values between 0 and 2*PI;
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
	protected float previous_d = 0;
	protected float step = 0;
	protected float previous_step = 0;
	
	protected int sens;
	
	protected double[] smoothBox = new double[NB_SMOOTHING_SAMPLES];
	protected int currentSmoothBoxIndex = -1;
	
	protected boolean alreadyMentionnedPitch = false;
	protected boolean alreadyMentionnedBounds = false;

    public static final int DEFAULT_OSC_PORT = 6789;
	
	
	public TurningWheel(int portIn) {
		try {
			this.dataReceiver = new OSCPortIn(portIn);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

    public TurningWheel(){
        this(DEFAULT_OSC_PORT);
    }
	
	/* -------------------turning wheel gesture ------------*/
	void turningWheel()
	{	
		
		if (sinus_pitch > MIN_SIN_PITCH) {
			
			alreadyMentionnedPitch = false;
			
			// Computing the angle difference
			
			step = (d - previous_d);
			
//			System.out.println("yaw : " + yaw);
//			System.out.println("step1 : " + step);
			
			if ( step > MAX_YAW_DIFFERENCE ) {
				step = -( (d % MAX_YAW_DIFFERENCE) + (previous_d % MAX_YAW_DIFFERENCE) );
			} else if ( step < -MAX_YAW_DIFFERENCE ) {
				step = (d % MAX_YAW_DIFFERENCE) + (previous_d % MAX_YAW_DIFFERENCE);
			}
			
			// System.out.println("step2 : " + step);
			

			
			if (currentSmoothBoxIndex == -1) {
				
				// In this case you can't take previous_d into account (it is updated lower in the code)
				currentSmoothBoxIndex++;
				
				alreadyMentionnedBounds = false;
				
			} else if ( 
					step * previous_step < 0 // i.e. opposite directions 
					&& Math.abs(previous_step) + Math.abs(step) > MAX_OPPOSITE_DIFFERENCE ) { // Too much difference
				
				// This step is not taken into account (the difference is probably due to missed steps)
				// (previous_d is updated lower in the code)
				
				System.out.println("Too much reverse difference : |" + previous_step + "| + |" + step + "| = " + (Math.abs(previous_step) + Math.abs(step)));
				
			} else if (Math.abs(step) > MIN_STEP && Math.abs(step) < MAX_STEP) {
				
				alreadyMentionnedBounds = false;
				
				smoothBox[currentSmoothBoxIndex % NB_SMOOTHING_SAMPLES] = step;
				
				currentSmoothBoxIndex++;
				
				if (currentSmoothBoxIndex > NB_SMOOTHING_SAMPLES) {
				
					// Computing the major direction and zoomFactors
					
					sens = 0;
					
					for (double v : smoothBox) {
						if (v > 0) {
							sens ++;
						} else if (v < 0) {
							sens --;
						}
					}
					
                    sens = (int)Math.signum(sens); // Mapped to {-1, 1}.
				
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
			
			previous_d = d;
			
			// System.out.println();
			
		} else {
			
			if (!alreadyMentionnedPitch) {
				System.out.println("Not enough pitch : " + sinus_pitch);
				alreadyMentionnedPitch = true;
			}
			
			// In this case we ignore the values
			currentSmoothBoxIndex = -1;
		}
	}
	
	public void initListeners() {
		VICONListener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				Object[] parts = msg.getArguments();
				
//				System.out.println("New message : ");
//				System.out.println("\t sinus pitch : " + parts[0]);
//				System.out.println("\t sinus roll : " + parts[1]);
//				System.out.println("\t yaw : " + parts[2]);
				
				sinus_pitch = ((Float)parts[0]).floatValue();
				
				// d = yaw * (1 - sinus_pitch)
				d = ((Float)parts[2]).floatValue() * 1 - sinus_pitch;
				
				turningWheel();
				
			}
		};
		
		dataReceiver.addListener("/inclinaison", VICONListener);
		
		System.out.println("Listeners initialized");
	}
	
	public void startListening() {
		dataReceiver.startListening();
		
		System.out.println("Listeners listening");
    }
	
	public void stopListening() {
		dataReceiver.stopListening();
		dataReceiver.close();
	}
	
	public void close() {
		dataReceiver.close();
	}
	
	/**
	 * Power-based transfer function.
	 * @param param
	 * @return
	 */
	protected float powTF(float param) {
		// x is mapped into [0 ; 1] from [ MIN_STEP ; MAX_STEP ] 
		float mx = (param - MIN_STEP) / (MAX_STEP - MIN_STEP);
		
		return MIN_ZOOM_FACTOR + (float)Math.pow(MAX_ZOOM_FACTOR - MIN_ZOOM_FACTOR, mx);
	}
	
	/**
	 * Lambda value for the sigmoidTF() function.
	 * Describes the slope of the inflexion point.
	 * The higher it gets, the more violent the transition becomes.
	 */
	protected static final float SIGMOID_LAMBDA = 2;
	
	/** 
	 * X offset for the sigmoid function. 
	 * If not zero, the sigmoid function is not centered on (MAX_STEP + MIN_STEP) / 2.
	 * The Y values of the sigmoid function are still MAX_ZOOM_FACTOR around MAX_STEP and MIN_ZOOM_FACTOR around MIN_STEP
	 * (depending on the lambda value. The higher it gets, the closer it becomes)
	 */
	protected static final float SIGMOID_X_OFFSET = .2f;
	
	/**
	 * Min bound to the theoretical range used for the x values in the sigmoid computation. 
	 * If the offset is equal to zero, SIGMOID_MIN_X_BOUND = MIN_STEP
	 */
	protected static final float SIGMOID_MIN_X_BOUND = (SIGMOID_X_OFFSET > 0) ? MIN_STEP + 2 * SIGMOID_X_OFFSET * (MAX_STEP - MIN_STEP) : MIN_STEP;
	
	/**
	 * Max bound to the theoretical range used for the x values in the sigmoid computation. 
	 * If the offset is equal to zero, SIGMOID_MAX_X_BOUND = MAX_STEP
	 */
	protected static final float SIGMOID_MAX_X_BOUND = (SIGMOID_X_OFFSET < 0) ? MAX_STEP - 2 * SIGMOID_X_OFFSET * (MAX_STEP - MIN_STEP) : MAX_STEP;
	
	/**
	 * Sigmoid-based transfer function.
	 * @return
	 */
	protected float sigmoidTF(float param) {
		
		// |x| is mapped into [-1 ; 1] from [ SIGMOID_MIN_BOUND ; SIGMOID_MAX_BOUND ] 
		float mx = 2 * (Math.abs(param) - SIGMOID_MIN_X_BOUND ) / (SIGMOID_MAX_X_BOUND - SIGMOID_MIN_X_BOUND ) - 1;
		
		return MIN_ZOOM_FACTOR + (MAX_ZOOM_FACTOR - MIN_ZOOM_FACTOR) * ( 1f / ( 1f + (float)Math.exp( -SIGMOID_LAMBDA * mx) ) );
		
	}
	
	
	
	/**
	 * Multiplier value for the multTF() function
	 */
	protected static final float ZOOM_MULTIPLIER = 100;
	
	/**
	 * Simple multiplier function
	 * @param param
	 * @return
	 */
	protected float multTF(float param) {
		
		return param * ZOOM_MULTIPLIER;
		
	}
	
}
