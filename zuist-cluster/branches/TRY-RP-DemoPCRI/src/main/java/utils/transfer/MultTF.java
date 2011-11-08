package utils.transfer;


public class MultTF extends AbstractTransferFunction{
	
	public float multiplier = 1;
	
	
	public MultTF(float minX, float maxX, float minY, float maxY) {
		
		super(minX, maxX, minY, maxY);
		
	}
	
	
	
	/**
	 * Simple multiplier function
	 * @param param
	 * @return
	 */
	@Override
	public float compute(float... params) {

		return params[0] * multiplier;
		
	}
	
	
	public void setMultiplier(float m) {
		this.multiplier = m;
	}
	
}
