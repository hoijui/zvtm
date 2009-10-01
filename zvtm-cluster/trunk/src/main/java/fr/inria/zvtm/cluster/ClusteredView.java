package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.inria.zvtm.engine.Camera;

public class ClusteredView {
	//ObjId
	private final int origin; //bottom-left block number
	//width of a screen, in pixels, possibly including bezels
	private int blockWidth;
	private int blockHeight;
	private final int nbRows;
	private final int nbCols;
	private final ArrayList<Camera> cameras = new ArrayList<Camera>();

	public ClusteredView(int origin, 
			int blockWidth, int blockHeight,
			int nbRows, int nbCols,
			List<Camera> cameras){
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
		Collections.copy(this.cameras, cameras);
	}

	public void setBlockSize(int blockWidth, int blockHeight){
		if((blockWidth <=0) || (blockHeight <=0)){
			throw new IllegalArgumentException("Block dimensions should be greater than 0");
		}
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
		//propagate changes to the Cameras associated with this View
	}

	public void setBackgroundColor(Color color){
		//TODO implement
		throw new UnsupportedOperationException();
	}
}

