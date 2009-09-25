package fr.inria.zvtm.cluster;

public class ClusteredView {
	private final int origin; //bottom-left block number
	//width of a screen, in pixels, possibly including bezels
	private final int blockWidth;
	private final int blockHeight;
	private final int nbRows;
	private final int nbCols;

	public ClusteredView(int origin, 
			int blockWidth, int blockHeight,
			int nbRows, int nbCols){
		if(origin < 0){
		   throw new IllegalArgumentException("Blocks are 0-based naturals");
		}
 		if((blockWidth <=0) || (blockHeight <=0)){
			throw new IllegalArgumentException("Block dimensions should be greater than 0");
		}
		if((nbRows <= 0) || (nbCols <= 0)){
			throw new IllegalArgumentException("Row and Column counts should be greater than 0");
		}
		this.origin = origin;
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
		this.nbRows = nbRows;
		this.nbCols = nbCols;
	}

}
