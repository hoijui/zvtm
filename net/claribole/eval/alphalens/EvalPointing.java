
package net.claribole.eval.alphalens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Graphics2D;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.lens.*;

public abstract class EvalPointing implements Java2DPainter {

    /* techniques */
    static final short TECHNIQUE_FL = 0; // fading lens
    static final short TECHNIQUE_ML = 1; // melting lens
    static final short TECHNIQUE_DL = 2; // distortion lens
    static final short TECHNIQUE_HL = 3; // manhattan lens
    static final String[] TECHNIQUE_NAMES = {"Fading Lens", "Melting Lens", "Distortion Lens", "Manhattan Lens"}; 
    short technique = TECHNIQUE_FL;

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1600;
    static int VIEW_MAX_H = 1200;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    /* ZVTM components */
    static final Color BACKGROUND_COLOR = Color.WHITE;
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Evaluation";
    Camera mCamera;

    BaseEventHandlerPointing eh;

    /* padding for lenses */
    static final int[] vispad = {100, 100, 100, 100};
    static final Color PADDING_COLOR = Color.BLACK;

    /* lens */
    static final Color LENS_BOUNDARY_COLOR = Color.RED;
    static final Color LENS_OBSERVED_REGION_COLOR = Color.RED;
    float MAGNIFICATION_FACTOR = 4.0f;
    static final int INNER_RADIUS = 50;
    static final int OUTER_RADIUS = 100;
    Lens lens;
    TFadingLens flens;

    void initGUI(){
	windowLayout();
	vsm = new VirtualSpaceManager();
	mSpace = vsm.addVirtualSpace(mSpaceName);
	mCamera = vsm.addCamera(mSpaceName);
	Vector v = new Vector();
	v.add(mCamera);
	mView = vsm.addExternalView(v, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, true);
	mView.setVisibilityPadding(vispad);
	mView.getPanel().addComponentListener(eh);
	mView.setNotifyMouseMoved(true);
	mView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	updatePanelSize();
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	    SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void setLens(int x, int y){
	switch(technique){
	case TECHNIQUE_FL:{
	    flens = new TFadingLens(MAGNIFICATION_FACTOR, 0.0f, 0.95f, OUTER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    flens.setBoundaryColor(LENS_BOUNDARY_COLOR);
	    flens.setObservedRegionColor(LENS_OBSERVED_REGION_COLOR);
	    lens = flens;
	    break;
	}
	case TECHNIQUE_ML:{
	    lens = new TLinearLens(MAGNIFICATION_FACTOR, 0.0f, 0.90f, OUTER_RADIUS, INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    flens = null;
	    break;
	}
	case TECHNIQUE_DL:{
	    lens = new FSGaussianLens(MAGNIFICATION_FACTOR, OUTER_RADIUS, INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    flens = null;
	    break;
	}
	case TECHNIQUE_HL:{
	    lens = new FSManhattanLens(MAGNIFICATION_FACTOR, OUTER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    ((FSManhattanLens)lens).setBoundaryColor(LENS_BOUNDARY_COLOR);
	    flens = null;
	    break;
	}
	}
	mView.setLens(lens);
    }

    void unsetLens(){
	mView.setLens(null);
	lens.dispose();
    }

    void moveLens(int x, int y, long absTime){
	if (flens != null){// dealing with a fading lens
	    flens.setAbsolutePosition(x, y, absTime);
	}
	else {// dealing with a probing lens
	    lens.setAbsolutePosition(x, y);
	}
	vsm.repaintNow();
    }

    /* ------------ TRIAL MANAGEMENT ------------- */

    void startTrial(){
	
    }

    void selectTarget(Glyph g){
	
    }

    /* ------------ LOW-LEVEL GRAPHICS ------------- */

    void updatePanelSize(){
	Dimension d = mView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	drawVisibilityPadding(g2d, viewWidth, viewHeight);
    }

    void drawVisibilityPadding(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(PADDING_COLOR);
	g2d.fillRect(0, 0, viewWidth, vispad[1]);
	g2d.fillRect(0, vispad[1], vispad[0], viewHeight-vispad[1]-vispad[3]-1);
	g2d.fillRect(viewWidth-vispad[2], vispad[1], vispad[2], viewHeight-vispad[1]-vispad[3]-1);
	g2d.fillRect(0, viewHeight-vispad[3]-1, viewWidth, vispad[3]+1);
    }

    void exit(){
	System.exit(0);
    }

}
