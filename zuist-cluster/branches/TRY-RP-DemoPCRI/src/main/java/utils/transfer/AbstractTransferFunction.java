package utils.transfer;


public abstract class AbstractTransferFunction {
	
	protected float MIN_X, MAX_X, MIN_Y, MAX_Y;
	
	
	public AbstractTransferFunction(float minX, float maxX, float minY, float maxY) {
		
		setRanges(minX, maxX, minY, maxY);
		
	}
	
	public void setRanges(float minX, float maxX, float minY, float maxY) {
		
		MIN_X = minX;
		MIN_Y = minY;
		
		MAX_X = maxX;
		MAX_Y = maxY;
		
	}
	
	public abstract float compute(float... params);
	
}
