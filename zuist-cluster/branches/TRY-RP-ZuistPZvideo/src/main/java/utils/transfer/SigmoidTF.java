package utils.transfer;


public class SigmoidTF extends AbstractTransferFunction{
	
	/**
	 * Identifier of the lambda value for the sigmoidTF() function.
	 * Describes the slope of the inflexion point.
	 * The higher it gets, the more violent the transition becomes.
	 */
	protected float lambda = 3;
	
	/** 
	 * X offset for the sigmoid function. 
	 * If not zero, the sigmoid function is not centered on (MAX_X + MIN_X) / 2.
	 * The Y values of the sigmoid function are still MAX_Y around MAX_X and MIN_Y around MIN_X
	 * (depending on the lambda value. The higher it gets, the closer it becomes)
	 */
	protected float xOffset = 0;
	
	/**
	 * Min bound to the theoretical range used for the x values in the sigmoid computation. 
	 * If the offset is equal to zero, minXBound = MIN_X
	 */
	protected float minXBound = MIN_X;
	
	/**
	 * Max bound to the theoretical range used for the x values in the sigmoid computation. 
	 * If the offset is equal to zero, maxXBound = MAX_X
	 */
	protected float maxXBound = MAX_X;
	
	
	
	
	public SigmoidTF(float minX, float maxX, float minY, float maxY) {
		
		super(minX, maxX, minY, maxY);
		
	}
	
	
	
	
	
	/**
	 * Sigmoid-based transfer function.
	 * @return
	 */
	@Override
	public float compute(float... params) {

		// |x| is mapped into [-1 ; 1] from [ minXBound ; maxXBound ] 
		float mx = 2f * (Math.abs(params[0]) - minXBound ) / (maxXBound - minXBound ) - 1f;
		
		return MIN_Y + (MAX_Y - MIN_Y) * ( 1f / ( 1f + (float)Math.exp( -lambda * mx) ) );
		
	}
	
	public void setLambda(float l) {
		this.lambda = l;
	}
	
	public void setXOffset(float offset) {
		xOffset = offset;
		
		minXBound = (xOffset > 0) ? MIN_X + 2f * xOffset * (MAX_X - MIN_X) : MIN_X;
		maxXBound = (xOffset < 0) ? MAX_X - 2f * xOffset * (MAX_X - MIN_X) : MAX_X;
	}

	
}
