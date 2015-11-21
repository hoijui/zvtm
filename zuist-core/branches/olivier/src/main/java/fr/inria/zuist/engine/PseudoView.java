package fr.inria.zuist.engine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Camera;

public class PseudoView
{
	public final VirtualSpace vs;
	public final Camera c;
	private int width = 0;
	private int height = 0;
	private final List<PseudoViewListener> listeners = new CopyOnWriteArrayList<PseudoViewListener>();

	public final int layerIndex;
	public int previousLevel = -2;
    public int currentLevel = -1;
    public double prevAlt;
    public boolean regUpdaterEnabled ;
    public boolean updateLevel;

	public PseudoView(VirtualSpace vs, Camera c, int w, int h, int layerIndex){
		this.vs = vs; this.c = c;
		width = w; height = h;
		this.layerIndex = layerIndex; 
		regUpdaterEnabled = true;
    	updateLevel = true;
	}

	public PseudoView(VirtualSpace vs, Camera c, int w, int h){
		this(vs, c, w, h, 0);
	}
	
	public PseudoView(VirtualSpace vs, Camera c, int layerIndex){
		this(vs, c, 0, 0, layerIndex);
	}

	public PseudoView(VirtualSpace vs, Camera c){
		this(vs, c, 0, 0, 0);
	}

	public void addListener(PseudoViewListener listener){
		listeners.add(listener);
    }
    
    public void removeListener(PseudoViewListener listener){
		listeners.remove(listener);
    }

	public void sizeTo(int w, int h){
		width = w; height = h;
		for (PseudoViewListener l : listeners){
			l.pseudoViewSizeChanged(this, width, height);
		}
	}

	public int getWidth() { return width; }
	public int getHeight() { return height; }

	
	public double[] getVisibleRegion(){
		double[] res; 
		if ((width <= 0 || height <= 0) && c.getOwningView() == null){
			// System.out.println("PseudoView [getVisibleRegion] owning view is null");
		}
		else if (width <= 0 || height <= 0) {
			//System.out.println("PseudoView [getVisibleRegion] owning view is NOT null");
		}
		if ((width <= 0 || height <= 0) && c.getOwningView() != null){
			res = c.getOwningView().getVisibleRegion(c);
		}
		else{
			res = new double[4];
			double uncoef = (c.focal+c.altitude) / c.focal;
            res[0] = c.vx-(width/2)*uncoef;
            res[1] = c.vy+(height/2)*uncoef;
            res[2] = c.vx+(width/2)*uncoef;
            res[3] = c.vy-(height/2)*uncoef;
		}
		return res;
	}

	// fixme: center on region
}