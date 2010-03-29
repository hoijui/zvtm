package techniques.pan;

import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import fr.inria.zuist.cluster.viewer.Viewer;
import fr.inria.zvtm.engine.ViewEventHandler;


public class DesktopMouse extends AbstractPanTechnique {

    	
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
			//System.out.println("MOUSE PRESSED");
			lastJPX = evt.getX();
			lastJPY = evt.getY();
			Viewer.getInstance().startPan();
		    }

		    public void mouseReleased (MouseEvent evt){
			//System.out.println("MOUSE Released");
			Viewer.getInstance().stopPan();
		    }
		};
		
	    mouseMotionAdapter = new MouseMotionAdapter() {
		    public void mouseMoved(MouseEvent evt) {
			//System.out.println("MOUSE Moved motion");
			// Viewer.getInstance().setCursorPositionByMasterScreenCoor(evt.getX(), evt.getY());
			
		    }
		    public void mouseDragged(MouseEvent evt) {
			//System.out.println("MOUSE draged motion");
			// Viewer.getInstance().setCursorPositionByMasterScreenCoor(evt.getX(), evt.getY());
			int jpx = evt.getX();
			int jpy = evt.getY();
			Viewer.getInstance().zeroOrderTranslate((long)(-(jpx-lastJPX)*(long)Viewer.getInstance().getMasterToWildWidthFact()), (long)(-(lastJPY-jpy))*(long)Viewer.getInstance().getMasterToWildHeightFact());
			lastJPX = jpx;
			lastJPY = jpy;
		    }
		};
	    //Viewer.getInstance().getZVTMPanel().addMouseListener(mouseAdapter);

	}

	@Override
	public void startListening() {
	    Viewer.getInstance().getZVTMPanel().addMouseListener(mouseAdapter);
	    Viewer.getInstance().getZVTMPanel().addMouseMotionListener(mouseMotionAdapter);
	}

	@Override
	public void stopListening() {
		// TODO Auto-generated method stub
	    Viewer.getInstance().getZVTMPanel().removeMouseListener(mouseAdapter);
	    Viewer.getInstance().getZVTMPanel().removeMouseMotionListener(mouseMotionAdapter);

	}

	@Override
	public void deleteStatLabels() {
		// Nothing
	}
}
