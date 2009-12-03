package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ClusteredView implements Identifiable {
	private final ObjId objId = ObjIdFactory.next();
	private final int origin; //bottom-left block number
	private final int viewRows;
	private final int viewCols;
    private ClusterGeometry clGeom;
	private final ArrayList<Camera> cameras; 

	/**
	 * Constructs a new ClusteredView.
     * @param clGeom cluster geometry
	 * @param origin origin (bottom-left) block number
	 * @param viewRows number of rows in the view (viewRows <= nbRows)
	 * @param viewCols number of columns in the view (viewCols <= nbCols)
	 * @param cameas a list of cameras observed by this ClusteredView.
	 */
	public ClusteredView(ClusterGeometry clGeom,
            int origin, 
            int viewRows, int viewCols,
			List<Camera> cameras){
        this.clGeom = clGeom;
        this.origin = origin;
		this.viewRows = viewRows;
		this.viewCols = viewCols;
		this.cameras = new ArrayList<Camera>(cameras);

		if(origin < 0){
			throw new IllegalArgumentException("Blocks are 0-based naturals");
		}
        if((viewRows <= 0) || (viewCols <= 0)){
			throw new IllegalArgumentException("View row and column counts should be greater than 0");
		}
		if(viewRows > clGeom.getRows()){ //XXX
			throw new IllegalArgumentException("View row(s) outside of cluster");
		}
		if(viewCols + colNum(origin) > clGeom.getColumns()){
			throw new IllegalArgumentException("View column(s) outside of cluster");
		}
	}

	public ObjId getObjId(){ return objId; }

	/** 
	 * Sets the background color for this ClusteredView.
	 * Not implemented yet.
	 * @param color new background color
	 */
	public void setBackgroundColor(Color color){
	//XXX implement	
	}

    /**
     * Returns the size of this ClusteredView, in pixels.
     * The width of this view is equal to blockWidth*viewCols
     * The height of this view is equal to blockHeight*viewRows
     */
	public Dimension getSize(){
		return new Dimension(clGeom.getBlockWidth()*viewCols, 
                clGeom.getBlockHeight()*viewRows);
	}

	/**
	 * @throw IllegalArgumentException If cam does not belong to this
	 *                                 ClusteredView
	 * @param xPos point x-coordinate, in VirtualSpace coords
	 * @param yPos point y-coordinate, in VirtualSpace coords
	 * @return the coordinates of a point at (xPos, yPos), in view
	 *         coordinates (ie (0,0) top-left, x increases to the
	 *         right, y increases downwards)
	 */
	public Point spaceToViewCoords(Camera cam, long xPos, long yPos){  
		if(!this.owns(cam)){
			throw new IllegalArgumentException("this view does not own Camera 'cam'");
		} 

		Location camLoc = cam.getLocation();

		float focal = cam.getFocal();
		float altCoef = (focal + camLoc.alt) / focal;
		Dimension viewSize = getSize();

		return new Point(viewSize.width/2+Math.round((xPos-camLoc.vx)/altCoef), viewSize.height/2-Math.round((yPos-camLoc.vy)/altCoef));
	}

	/**
	 * Converts the coordinates of a point given in ClusteredView 
	 * coordinates into VirtualSpace coordinates.
	 * @throw IllegalArgumentException If cam does not belong to this
	 *                                 ClusteredView
	 * @param xPos point x-coodinate, in View coords
	 * @param yPos point y-coordinate, in View coords
	 */
	public LongPoint viewToSpaceCoords(Camera cam, int xPos, int yPos){
		if(!this.owns(cam)){
			throw new IllegalArgumentException("this view does not own Camera 'cam'");
		}

		Location camLoc = cam.getLocation();
		float focal = cam.getFocal();
		float altCoef = (focal + camLoc.alt) / focal;
		Dimension viewSize = getSize();

		//find coords of view origin in the virtual space
		long viewOrigX = camLoc.vx - (long)(0.5*viewSize.width*altCoef);
		long viewOrigY = camLoc.vy + (long)(0.5*viewSize.height*altCoef);

		return new LongPoint(
				viewOrigX + (long)(altCoef*xPos),
				viewOrigY - (long)(altCoef*yPos));
	}

	/**
	 * Gets the origin (bottom-left) block number for
	 * this ClusteredView. Note that blocks are ordered column-wise,
	 * and block numbers start at zero.
	 */
	int getOrigin() { return origin; }

    ClusterGeometry getClusterGeometry(){ return clGeom; }

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
    //column 0 is the leftmost column
    //XXX move to ClusterGeometry?
	int colNum(int blockNum){
		return blockNum / clGeom.getRows();
	}

	//returns the row number associated with a block number
	//blocks are ordered column-wise
    //row 0 is the topmost one
    //XXX move to ClusterGeometry?
	int rowNum(int blockNum){
		return blockNum % clGeom.getRows();
	}

	/**
	 * @param x1 first point x-coordinate, in VirtualSpace coords
	 * @param y1 first point y-coordinate, in VirtualSpace coords	 
	 * @param x2 second point x-coordinate, in VirtualSpace coords
	 * @param y2 second point y-coordinate, in VirtualSpace coords

	 */
    public Location centerOnRegion(Camera cam, long x1, long y1, long x2, long y2){
       if(!this.owns(cam)){
           throw new IllegalArgumentException("this view does not own Camera 'cam'");
       } 

       long west = Math.min(x1,x2);
       long north = Math.max(y1,y2);
       long east = Math.max(x1,x2);
       long south = Math.min(y1,y2);

       long newX = (west + east) / 2;
       long newY = (north + south) / 2;

       Dimension viewSize = getSize();
       //new altitude to fit horizontally
       float nah = (east-newX)*2*cam.getFocal() / viewSize.width - cam.getFocal();
       //new altitude to fit vertically
       float nav = (north-newY)*2*cam.getFocal()/ viewSize.height - cam.getFocal();
       float newAlt = Math.max(nah, nav);

       return new Location(newX, newY, newAlt);
    }

   // public Location centerOnGlyph(Camera cam, Glyph glyph){
   // }

    boolean owns(Camera cam){
        return cameras.contains(cam);
    }
}

