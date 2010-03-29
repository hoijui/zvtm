package techniques.point;

import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import fr.inria.zuist.cluster.viewer.Viewer;
import fr.inria.zvtm.engine.ViewEventHandler;


public class DesktopMouse extends AbstractPointTechnique {

    	
    protected Robot robot = null;
    protected ViewEventHandler handler;
    protected MouseAdapter mouseAdapter;
    protected MouseMotionAdapter mouseMotionAdapter;

    public DesktopMouse(String id, ORDER o) {
	super(id, o);
	// TODO Auto-generated constructor stub
    }

    @Override
	public void close() {
		// TODO Auto-generated method stub

	}

    private int lastJPX = -1;
    private int lastJPY = -1;

	@Override
	public void initListeners() {

	    mouseAdapter = new MouseAdapter() {

		    public void mousePressed(MouseEvent evt) {
		    }

		    public void mouseReleased (MouseEvent evt){
			//System.out.println("MOUSE Released");
		    }
		};
		
	    mouseMotionAdapter = new MouseMotionAdapter() {
		    public void mouseMoved(MouseEvent evt) {
			long x = (long)(evt.getX());
			long y = (long)(evt.getY());
			// should convert in zvtm coordinate !
			// Zoom.getInstance().setZoomOriginByMasterScreenCoor(x, y);
			//System.out.println("MOUSE Moved motion");
		    }
		    public void mouseDragged(MouseEvent evt) {
		    }
		};


	}

	@Override
	public void startListening() {
	    Zoom.getInstance().getZVTMPanel().addMouseListener(mouseAdapter);
	    Zoom.getInstance().getZVTMPanel().addMouseMotionListener(mouseMotionAdapter);
	}

	@Override
	public void stopListening() {
		// TODO Auto-generated method stub
	    Zoom.getInstance().getZVTMPanel().removeMouseListener(mouseAdapter);
	    Zoom.getInstance().getZVTMPanel().removeMouseMotionListener(mouseMotionAdapter);

	}

	@Override
	public void deleteStatLabels() {
		// Nothing
	}    
}
