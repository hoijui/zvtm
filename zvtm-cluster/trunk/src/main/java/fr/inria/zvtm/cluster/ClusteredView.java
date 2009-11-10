package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ClusteredView implements Identifiable {
	private final ObjId objId = ObjIdFactory.next();
	private final int origin; //bottom-left block number
	//width of a screen, in pixels, possibly including bezels
	private int blockWidth;
	private int blockHeight;
	private final int nbRows;
	private final int nbCols;
	private final int viewRows;
	private final int viewCols;
	private final ArrayList<Camera> cameras; 
    private Color bgColor;

	/**
	 * @param origin
	 * @param blockWidth
	 * @param blockHeight
	 */
	public ClusteredView(int origin, 
			int blockWidth, int blockHeight,
			int nbRows, int nbCols,
			int viewRows, int viewCols,
			List<Camera> cameras){
		//clustered view is replicated on the slaves (provides cluster
		//geometry)
		//
		//We might want to group all these parameters into a 
		//'cluster geometry' object and a 'view geometry' object.
		//Also, we might want to move the cluster geometry into its own
		//class (separate from ClusterView).
		this.origin = origin;
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
		this.nbRows = nbRows;
		this.nbCols = nbCols;
		this.viewRows = viewRows;
		this.viewCols = viewCols;
		this.cameras = new ArrayList<Camera>(cameras);
        this.bgColor = Color.BLACK;

		if(origin < 0){
			throw new IllegalArgumentException("Blocks are 0-based naturals");
		}
		if((blockWidth <=0) || (blockHeight <=0)){
			throw new IllegalArgumentException("Block dimensions should be greater than 0");
		}
		if((nbRows <= 0) || (nbCols <= 0)){
			throw new IllegalArgumentException("Row and Column counts should be greater than 0");
		}
		if((viewRows <= 0) || (viewCols <= 0)){
			throw new IllegalArgumentException("View row and column counts should be greater than 0");
		}
		if(viewRows > nbRows){ //XXX
			throw new IllegalArgumentException("View row(s) outside of cluster");
		}
		if(viewCols + colNum(origin) > nbCols){
			throw new IllegalArgumentException("View column(s) outside of cluster");
		}

	}

	public ObjId getObjId(){ return objId; }

	public void setBlockSize(int blockWidth, int blockHeight){
		if((blockWidth <=0) || (blockHeight <=0)){
			throw new IllegalArgumentException("Block dimensions should be greater than 0");
		}
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
	}

    public void setBackgroundColor(Color color){
        this.bgColor = color;
        if(VirtualSpaceManager.INSTANCE.isMaster()){
            VirtualSpaceManager.INSTANCE.sendDelta(
                    new ViewBackgroundColorDelta(getObjId(), bgColor)
                    );
        }
    }
    private static class ViewBackgroundColorDelta implements Delta {
        private final ObjId viewId; //clustered view ID
        private final Color bgColor;

        ViewBackgroundColorDelta(ObjId viewId, Color bgColor){
            this.viewId = viewId;
            this.bgColor = bgColor;
        }   

        public void apply(SlaveUpdater su){
            ClusteredView cv = (ClusteredView)(su.getSlaveObject(viewId));
            su.setViewBackground(cv, bgColor);
        }
    }

    public Color getBackgroundColor(){
        return bgColor;
    }

	int getOrigin() { return origin; }

	int getBlockWidth(){ return blockWidth; }

	int getBlockHeight(){ return blockHeight; }

	int getNbRows() { return nbRows; }

	int getNbCols() { return nbCols; }

	int getViewRows() { return viewRows; }

	int getViewCols() { return viewCols; }

	//vector for compatibility with zvtm views 
	Vector<Camera> getCameras(){
		return new Vector(cameras);
	}

	/**
	 * Tests whether the block 'blockNumber' belongs
	 * to this ClusteredView.
	 */
	boolean ownsBlock(int blockNumber){
		int col = colNum(blockNumber);
		int row = rowNum(blockNumber);
		int origCol = colNum(origin);
		int origRow = rowNum(origin);
		return 
			(row <= origRow) &&
			(col >= origCol) &&
			(row > (origRow - viewRows)) &&
			(col < (origCol + viewCols));
	}

	//returns the column number associated with a block number.
	//blocks are ordered column-wise
	int colNum(int blockNum){
		return blockNum / nbRows;
	}

	//returns the row number associated with a block number
	//blocks are ordered column-wise
	int rowNum(int blockNum){
		return blockNum % nbRows;
	}
}

