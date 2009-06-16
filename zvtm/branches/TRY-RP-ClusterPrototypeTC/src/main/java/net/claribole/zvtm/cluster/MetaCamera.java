package net.claribole.zvtm.cluster;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpace; 
import com.xerox.VTM.engine.VirtualSpaceManager; 

//rectangular block of cameras
public class MetaCamera {
	//cameras ordered column-wise
	private Camera[] cameras;

	//*upper left-hand camera (center) coordinates*
	//(differs from the rest of ZVTM, but it's
	//a prototype anyway)
	private long posX = 0;
	private long posY = 0;

	private int nbX;
	private int nbY;

	private int blockWidth;
	private int blockHeight;

	private int bezelWidth = 0; //bezel width, in pixels
	private int bezelHeight = 0; //bezel height, in pixels
	private boolean bezelEnabled = false; //true if bezels are taken
	//into account, i.e. image under the bezels is not shown (a circle looks like a 
	//circle, not like an oval)

	//XXX todo altitude

	//nbX: number of screens (horiz)
	//blockHeight: height of a screen (pixels)
	public MetaCamera(int nbX, int nbY, 
			int blockWidth, int blockHeight,
			VirtualSpace virtualSpace){
		this(nbX, nbY, blockWidth, 
				blockHeight, virtualSpace,
				0, 0);
		bezelEnabled = false;
	}

	public MetaCamera(int nbX, int nbY, 
			int blockWidth, int blockHeight,
			VirtualSpace virtualSpace,
			int bezelWidth, int bezelHeight){
		assert(nbX > 0);
		assert(nbY > 0);
		assert(blockWidth > 0);
		assert(blockHeight > 0);
		assert(bezelWidth >= 0);
		assert(bezelHeight >= 0);

		this.nbX = nbX;
		this.nbY = nbY;
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
		this.bezelWidth = bezelWidth;
		bezelEnabled = true;

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

			long newX = posX + col*blockWidth;
			long newY = posY - row*blockHeight;
			if(bezelEnabled){
				newX += (2*col*bezelWidth);	
				newY -= (2*row*bezelHeight);
			}

			cameras[i].moveTo(newX,
					newY);
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

	public void enableBezel(boolean enable){
		bezelEnabled = enable;
	}

	public Camera retrieveCamera(int index){
		return cameras[index];
	}
}

