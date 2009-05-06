package net.claribole.zvtm.cluster;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpace; 
import com.xerox.VTM.engine.VirtualSpaceManager; 

//rectangular block of cameras
public class MetaCamera {
	//cameras ordered column-wise
	private Camera[] cameras;

	//*upper left-hand camera coordinates*
	//(differs from the rest of ZVTM, but it's
	//a prototype anyway)
	private long posX = 0;
	private long posY = 0;

	private int nbX;
	private int nbY;

	private int blockWidth;
	private int blockHeight;

	//XXX todo altitude

	//nbX: number of screens (horiz)
	//blockHeight: height of a screen (pixels)
	public MetaCamera(int nbX, int nbY, 
			int blockWidth, int blockHeight,
			VirtualSpace virtualSpace){
		assert(nbX > 0);
		assert(nbY > 0);
		assert(blockWidth > 0);
		assert(blockHeight > 0);

		this.nbX = nbX;
		this.nbY = nbY;
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;

		cameras = new Camera[nbX*nbY];
		for(int i=0; i<cameras.length; ++i){
			cameras[i] = VirtualSpaceManager.getInstance()
				.addCamera(virtualSpace);
			cameras[i].setAltitude(0f);
		}

		setSlaveCoordinates();
	}

	//note: slave cameras are ordered column-wise
	private void setSlaveCoordinates(){
		for(int i=0; i<cameras.length; ++i){
			int row = i % nbY;
			int col = i / nbY;

			cameras[i].moveTo(posX + col*blockWidth,
					posY - row*blockHeight);
		}
	}

	//relative movement
	public synchronized void move(long deltaX, long deltaY){
		moveTo(posX + deltaX, posY + deltaY);
	}

	//absolute movement
	public synchronized void moveTo(long x, long y){
		posX = x;
		posY = y;
		setSlaveCoordinates();
	}

	public Camera retrieveCamera(int index){
		return cameras[index];
	}
}

