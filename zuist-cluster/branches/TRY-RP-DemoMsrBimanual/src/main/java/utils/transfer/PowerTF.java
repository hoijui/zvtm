package utils.transfer;


public class PowerTF extends AbstractTransferFunction{
	
	
	public PowerTF(float minX, float maxX, float minY, float maxY) {
		
		super(minX, maxX, minY, maxY);
		
	}
	
	
	/**
	 * Simple power-based transfer function.
	 * @param param
	 * @return
	 */
	@Override
	public float compute(float... params) {
		
		// x is mapped into [0 ; 1] from [ MIN_X ; MAX_X ] 
		float mx = (params[0] - MIN_X) / (MAX_X - MIN_X);
		
		return MIN_Y + (float)Math.pow(MAX_Y - MIN_Y, mx);
		
	}
	
}
