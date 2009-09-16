/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
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
		this.location = loc;
		if(slaveCam != null){
			float focal = slaveCam.getFocal();
			float altCoef = (focal + loc.alt) / focal;
			//screen width in virtualspace coords
			long virtBlockWidth = (long)(blockWidth * altCoef);
			//screen height in virtualspace coords
			long virtBlockHeight = (long)(blockHeight * altCoef);

			int row = slaveIndex % numRows;
			int col = slaveIndex / numRows;

			long newX = location.vx + col*virtBlockWidth;
			long newY = location.vy - row*virtBlockHeight;

			slaveCam.setLocation(new Location(newX, newY, loc.alt));
		}
	}

	/* called by a slave application */
	public void offerCamera(Camera cam, int slaveIndex,
			int numRows, int numCols, 
			int blockWidth, int blockHeight){
		this.slaveCam = cam;
		this.slaveCam.setZoomFloor(0f);
		this.slaveIndex = slaveIndex;
		this.numRows = numRows;
		this.numCols = numCols;
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
	}

}

