package techniques.zoom;

import java.awt.event.MouseWheelEvent;

import fr.inria.zuist.cluster.viewer.Viewer;
import utils.transfer.SigmoidTF;
import fr.inria.zvtm.engine.DefaultEventHandler;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;


public class Knob extends AbstractViewerTechnique {
	
	/**
	 * Maximum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If this value is exceeded, the current glove coordinates are ignored.
	 */
	public static final float MAX_STEP = 5f;
	
	/**
	 * Minimum difference between the previous glove 'angle' and the current one. Angles are computed from glove coordinates.
	 * If the current angle is below this value, the current glove coordinates are ignored.
	 * If this value is 0, there is a risk that the zooming movement never stops.
	 */
	public static final float MIN_STEP = 1f;
	
	/**
	 * Requested min zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MIN_ZOOM_FACTOR = 8;
	
	/**
	 * Requested max zoom factor. 
	 * Used to set the parameters for some of the transfer function(s).
	 */
	public static final float MAX_ZOOM_FACTOR = 30;


	protected ViewEventHandler eventHandler;
	
	protected SigmoidTF transferFunction = new SigmoidTF(MIN_STEP, MAX_STEP, MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR);
	
	
	public Knob(String id, ORDER o) {
		super(id, o, false);
		
		transferFunction.setLambda(2f);
		transferFunction.setXOffset(.2f);
		
	}
	
	
	@Override
	public void initListeners() {
		
		this.eventHandler = new DefaultEventHandler() {
			@Override
			public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e) {

				int clicks = e.getWheelRotation();
				int sens = -(int)Math.signum(clicks);
				
				Viewer.getInstance().zeroOrderZoom( sens * transferFunction.compute(clicks) );
				
			}
		};
		
		Viewer.getInstance().getView().setEventHandler(eventHandler);
		
		
		System.out.println("Listeners initialized");
	}
	
	@Override
	public void startListening() {
		
	}
	
	@Override
	public void stopListening() {
		
	}
	
	@Override
	public void close() {
		
		Viewer.getInstance().getView().setEventHandler(null);
		
	}
	
	
}
