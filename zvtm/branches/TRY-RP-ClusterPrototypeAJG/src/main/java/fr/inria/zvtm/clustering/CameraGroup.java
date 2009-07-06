package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpace;

public class CameraGroup {
	private final VirtualSpace owner;
	private Location location = new Location(0L, 0L, 0F);
	private Camera slaveCam = null;
	private int slaveIndex = 0;
	private int numRows = 0;
	private int numCols = 0; 
	private int blockWidth = 0; //width of a screen
	private int blockHeight = 0;

	public CameraGroup(VirtualSpace owner){
		this.owner = owner;
	}

	public VirtualSpace getOwner(){
		return owner;
	}

	//note: blocks ("screens") are ordered column-wise

	public Location getLocation(){
		return location;
	}

	/* called by the master application */
	public void setLocation(Location loc){
		this.location = location;
		if(slaveCam != null){
			int row = slaveIndex % numCols;
			int col = slaveIndex / numCols;

			long newX = location.vx + col*blockWidth;
			long newY = location.vy - row*blockHeight;

			slaveCam.moveTo(newX, newY);
		}
	}

	/* called by a slave application */
	public void offerCamera(Camera cam, int slaveIndex,
			int numRows, int numCols, 
			int blockWidth, int blockHeight){
		this.slaveCam = cam;
		this.slaveIndex = slaveIndex;
		this.numRows = numRows;
		this.numCols = numCols;
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
	}

}

