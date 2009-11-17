package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.CameraPortal;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.LongPoint;
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

	/**
	 * Constructs a new ClusteredView.
	 * @param origin origin (bottom-left) block number
	 * @param blockWidth width of a block, in pixels. Every block
	 * of a ClusteredView is assumed to have the same width.
	 * @param blockHeight height of a block, in pixels. Every block 
	 * of a ClusteredView is assumed to have the same height.
	 * @param nbRows number of rows in the cluster
	 * @param nbCols number of columns in the cluster
	 * @param viewRows number of rows in the view (viewRows <= nbRows)
	 * @param viewCols number of columns in the view (viewCols <= nbCols)
	 * @param cameas a list of cameras observed by this ClusteredView.
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

	/**
	 * Sets the dimensions of a block. This is useful e.g. to toggle
	 * screen borders on/off
	 * @param blockWidth new block width
	 * @param blockHeight new block height
	 * @throws IllegalArgumentException if blockWidth <= 0 or
	 *                                     blockHeight <= 0
	 */
	public void setBlockSize(int blockWidth, int blockHeight){
		if((blockWidth <=0) || (blockHeight <=0)){
			throw new IllegalArgumentException("Block dimensions should be greater than 0");
		}
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
	}

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
		return new Dimension(blockWidth*viewCols, blockHeight*viewRows);
	}

	public void addPortal(CameraPortal portal){
		Delta delta = new PortalCreateDelta(this.getObjId(), portal);
		VirtualSpaceManager.INSTANCE.sendDelta(delta);
	}

	private static class PortalCreateDelta implements Delta {
		//xOrig and yOrig are the supplied origin relative
		//to the ClusteredView, need translation relative to 
		//block views (ZVTM views)
		private final int xOrig;
		private final int yOrig;
		private final int width;
		private final int height;
		private final ObjId viewId;
		private final ObjId camId;
		private final ObjId portalId;

		PortalCreateDelta(ObjId viewId, CameraPortal portal){
			this.xOrig = portal.x;
			this.yOrig = portal.y;
			this.width = portal.w;
			this.height = portal.h;
			this.viewId = viewId;
			this.camId = portal.getCamera().getObjId();
			this.portalId = portal.getObjId();
		}

	public void apply(SlaveUpdater updater){
		Camera cam = (Camera)(updater.getSlaveObject(camId));
		assert(cam != null); //not production ready
		CameraPortal portal = new CameraPortal(xOrig, yOrig,
				width, height, cam);
        portal.setBorder(Color.RED);
		ClusteredView cv = (ClusteredView)(updater.getSlaveObject(viewId));
		assert(cv != null);
		updater.setPortalLocation(cv, portal, xOrig, yOrig);
		updater.addPortal(cv, portal);
		updater.putSlaveObject(portalId, portal);
		}
	}

	public void destroyPortal(CameraPortal portal){
		Delta delta = new PortalDestroyDelta(this.getObjId(), portal.getObjId());
		VirtualSpaceManager.INSTANCE.sendDelta(delta);
	}

	private static class PortalDestroyDelta implements Delta {
		private final ObjId viewId;
		private final ObjId portalId;

		PortalDestroyDelta(ObjId viewId, ObjId portalId){
			this.viewId = viewId;
			this.portalId = portalId;
		}

		public void apply(SlaveUpdater updater){
			CameraPortal portal = (CameraPortal)(updater.getSlaveObject(portalId));
			assert(portal != null);
			ClusteredView cv = (ClusteredView)(updater.getSlaveObject(viewId));
			assert(cv != null);
			updater.removeSlaveObject(portalId);
			updater.destroyPortal(cv, portal);
		}
	}

    public void movePortalTo(CameraPortal portal, int masterX, int masterY){
        portal.x = masterX;
        portal.y = masterY;
        Delta delta = new PortalMoveDelta(this, portal);
        VirtualSpaceManager.INSTANCE.sendDelta(delta);
    }

   private static class PortalMoveDelta implements Delta {
        private final int masterX;
        private final int masterY;
        private final ObjId viewId;
        private final ObjId portalId;
        PortalMoveDelta(ClusteredView clusteredView, CameraPortal portal){
            masterX = portal.x;
            masterY = portal.y;
            viewId = clusteredView.getObjId();
            portalId = portal.getObjId();
        }

        public void apply(SlaveUpdater su){
            CameraPortal portal = (CameraPortal)(su.getSlaveObject(portalId));
            assert(portal != null);
            ClusteredView view = (ClusteredView)(su.getSlaveObject(viewId));
            assert(view != null);
            portal.x = masterX;
            portal.y = masterY;
            su.setPortalLocation(view, portal, masterX, masterY);
        }
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

    private boolean owns(Camera cam){
        return cameras.contains(cam);
    }
}

