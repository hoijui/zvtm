package fr.inria.zvtm.fms;

import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Vector;

import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.animation.*;
import fr.inria.zvtm.animation.interpolation.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.lens.*;
import fr.inria.zvtm.widgets.*;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Test3 {

    VirtualSpaceManager vsm;
    static final String mSpaceStr = "mSpace";
    VirtualSpace mSpace;
    Camera mCamera;
    View mView;
    ViewEventHandler eh;
    SpeedCoupling speedCoupling;

    static int LENS_R1 = 125;
    static int LENS_R2 = 75;
    static final int LENS_ANIM_TIME = 300;
    static double MAG_FACTOR = 8.0;

    boolean DO_FLATENING = false;
    boolean FLATENING_VARIABLE = false;
    boolean INFINITE_FRICTION = true;
    boolean ELASTIC_BORDER = false;
    boolean SMOOTH = false;
    boolean SMOOTH_WARPING = true;
    boolean USE_LINEAR_SC = true;

    float MIN_MAG_FACTOR = (DO_FLATENING)? 1.0f : (float)MAG_FACTOR;
    Robot robot;

 
    Test3(){
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest();
    }

    public void initTest(){
        mSpace = vsm.addVirtualSpace(mSpaceStr);
        mCamera = vsm.addCamera(mSpace);
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace(mSpaceStr).getCamera(0));
        mView = vsm.addExternalView(cameras, "FMS", View.STD_VIEW, 1600, 800, false, true);
        mView.setBackgroundColor(Color.LIGHT_GRAY);
        eh = new EventHandlerTest3(this);
        mView.setEventHandler(eh);
        mView.setNotifyMouseMoved(true);
	int trans = 400;
        for (int i=-10;i<=10;i++){
            for (int j=-5;j<=5;j++){
		if (i == 0 && j == 0)
		{
		    vsm.addGlyph(new VRectangle(i*3 - trans,j*3,0,1,1,Color.RED), mSpace);
		    vsm.addGlyph(new VRectangle(i*3 + trans,j*3,0,1,1,Color.RED), mSpace);
		}
		else
		{
		    vsm.addGlyph(new VRectangle(i*3  - trans,j*3,0,1,1,Color.WHITE), mSpace);
		    vsm.addGlyph(new VRectangle(i*3   + trans,j*3,0,1,1,Color.WHITE), mSpace);
		    
		}
            }
        }
        vsm.repaintNow();

	robot = null;
	try {
	    robot = new Robot();
	} catch(AWTException e) { 
	    e.printStackTrace();
	}
    }
    
    //FixedSizeLens lens;
    SCFGaussianLens lens;
    //Lens lens;

    double _lensX, _lensY;
    double _lensLastContactX, _lensLastContactY;
    int _lensAttached = 0;  // not used

    static int NUM_LSTOCK = 256;
    double[] _lX = new double[NUM_LSTOCK];
    double[] _lY = new double[NUM_LSTOCK];
    long[] _lT = new long[NUM_LSTOCK];
    int[] _lA = new int[NUM_LSTOCK]; // not used
    int _lStockIndex = 0;

    double getRadius()
    {
	double amm = 1.0;
	amm = (double)lens.getActualMaximumMagnification();
	double Rad;
	Rad = (double)lens.getInnerRadius();
	if (DO_FLATENING && FLATENING_VARIABLE)
	{
	    Rad =  Rad*(amm/MAG_FACTOR);
	}
	return Rad;
    }

    void lstock(double x, double y)
    {
	for (int i = NUM_LSTOCK-1; i > 0; i--)
	{
	    _lX[i] = _lX[i-1];
	    _lY[i] = _lY[i-1];
	    _lT[i] = _lT[i-1];
	    _lA[i] = _lA[i-1];
	}
	_lX[0] = x;
	_lY[0] = y;
	_lT[0] = System.currentTimeMillis();
	_lA[0] = _lensAttached;
    }

    // slide para:
    int _slide_smooth = 5;
    double _slide_minFriction = 0.2;
    double _slide_smoothPower = 0.3;  // decrease for less friction close
                                      // to the radius
    int _slide_updateTime = 32;

    int indrag = 0;

    
    void doSlideLens(boolean fromCursorMoved)
    {


	int k = _slide_smooth;

	if (INFINITE_FRICTION || indrag >= 1 ||
	    _lT[0] == 0 || _lT[k] == 0 || _lT[k] == _lT[0])
	{
	    if (indrag >= 2)
	    {
		long l = System.currentTimeMillis();
		for (int i = NUM_LSTOCK-1; i >= 0; i--)
		{
		    _lX[i] = _lensX;
		    _lY[i] = _lensY;
		    _lT[i] = l;
		}
		indrag = 0;
	    }
	    return;
	}

	double norm = 0.0;
	double v = 0.0;
	int p_index = 0;

	if (false && _lA[0] == 1 && _lensAttached == 0)
	{
	    // this bugged ... should update the _l*
	    int max_index = 0;
	    int l = 0;
	    while (_lA[l] != 0 && l < NUM_LSTOCK-1)
		l++;

	    max_index = l-1;
	    if (max_index < k)
		max_index = k;
	    
	    double tv,vm = 0;
	    double nm = 0;
	    for (int i = 0; i <= max_index-k; i++)
	    {
		nm = Math.sqrt((_lX[i+k]-_lX[i])*(_lX[i+k]-_lX[i]) +
				Math.pow(_lY[i+k]-_lY[i],2.0));
		if (_lT[i] - _lT[k+i] == 0.0)
		{
		    continue;
		}
		tv = nm/(_lT[i] - _lT[k+i]);
		if (tv > v)
		{
		    p_index = i;
		    v = tv;
		    norm = nm;
		}
	    }
	    //System.out.println (
	    // "VM: "+ v + " (" + max_index + "," + p_index + ")");
	}
	
	if (v == 0.0)
	{
	    norm = Math.sqrt((_lX[k]-_lX[0])*(_lX[k]-_lX[0]) +
			     Math.pow(_lY[k]-_lY[0],2.0));
	    v = norm/(_lT[0] - _lT[k]);
	    //if (norm > 0.001 && v > 0.001)
	    //	System.out.println ("VM NORMAL: "+ v );
	}

	if (norm == 0.0 || v == 0.0)
	{
	    //System.out.println ("NORM ZERO");
	    return;
	}

	//System.out.println ( "Speed is " + v);

	long ctime  = System.currentTimeMillis();

	double cx = _lensLastContactX;
	double cy = _lensLastContactY;
	double d1 = Math.sqrt((cx - _lastX)*(cx - _lastX) +
			      (cy - _lastY)*(cy - _lastY));

	double Rad = LENS_R2;
	Rad = getRadius();
	double cr = (double)Rad + (double)Rad/10.0;
	double rat = (int)(10.0*(1.0 - Math.min(d1,cr)/cr))/10.0;

	// variante distance to the center
	//cx = _lX[0]; cy = _lY[0];
	//d1 = Math.sqrt((cx - _lastX)*(cx - _lastX) +
	//	       (cy - _lastY)*(cy - _lastY));
	//cr = (double)LENS_R2; 
	//rat = (int)(10.0*(Math.min(d1,cr)/cr))/10.0;

	// friction 
	double f = (1.0 - Math.pow(rat,_slide_smoothPower)) + _slide_minFriction;

	//System.out.println ( "v: " + v + ", rat: " + rat +", f: " +f);
	double tt = (double)(ctime - _lT[p_index]);
	v = (v - f*v);
	if (v < 0) v = 0;

	double ux =  tt*v*(_lX[p_index]-_lX[p_index+k])/norm;
	double uy =  tt*v*(_lY[p_index]-_lY[p_index+k])/norm;

	//System.out.println ( (int)(_lX[0]) + " " + (int)(_lY[0]) + " " +
	//			 (int)(_lX[0]+ux) + " " + (int)(_lY[0]+uy));
	double nx = _lX[p_index]+ux;
	double ny = _lY[p_index]+uy;
	double d = Math.sqrt((nx-_lastX)*(nx-_lastX) + (ny-_lastY)*(ny-_lastY));

	if (d > Rad+1)
	{
	    if (DO_FLATENING && FLATENING_VARIABLE)
	    {
		return;
	    }
	    lens_cursorMoved(_lastX, _lastY, true, null);
	}
	else
	{
	    
	    _lensLastContactX = _lensLastContactX + ux;
	    _lensLastContactY = _lensLastContactY + uy;
	    moveLens(_lX[0]+ux, _lY[0]+uy, System.currentTimeMillis());
	}
	//System.out.println ( "ux: "+ux+ ", uy: " + uy);
    }

    ActionListener taskPerformer = new ActionListener()
    {
	public void actionPerformed(ActionEvent evt)
	{
	    doSlideLens(false);
	    
	}
    };
    Timer _ltimer = new Timer(_slide_updateTime, taskPerformer);
    //_ltimer.restart();
    

    void toggleLens(double x, double y){
        if (lens != null){
            unsetLens();
        }
        else {
            setLens(x, y);
	    
        }
    }
 
    void setLens(double x, double y){
	_lensX = x; _lensY = y;
	lstock(x,y);
        //lens = (FixedSizeLens)mView.setLens(getLensDefinition((int)x, (int)y));
	lens = (SCFGaussianLens)mView.setLens(getLensDefinition((int)x, (int)y));
	lens.setCutoffFrequencyParameters(0.1,0.1);  // default 0.1 0.01
	lens.setMinMagFactor(MIN_MAG_FACTOR);
        lens.setBufferThreshold(1.5f);
        lens.setOuterRadiusColor(Color.BLACK);
        lens.setInnerRadiusColor(Color.BLACK);
	lens.setDrawMaxFlatTop(true);
	lens.setSpeedBlendRadii(false, false);
	if (USE_LINEAR_SC)
	{
	    speedCoupling = new SpeedCoupling();
	    lens.setSpeedCoupling(speedCoupling);
	}
        Animation a = vsm.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
	// caro diff
	lens.setAbsolutePosition((int)x, (int)y, System.currentTimeMillis());
	lens.setXfocusOffset(0);
	lens.setYfocusOffset(0);
    }
    
    void unsetLens(){
        Animation a = vsm.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(),
            new EndAction(){public void execute(Object subject, Animation.Dimension dimension){doUnsetLens();}});
        vsm.getAnimationManager().startAnimation(a, false);
    }
    
    void doUnsetLens(){
        lens.dispose();
        mView.setLens(null);
        lens = null;
    }

    SCFGaussianLens getLensDefinition(int x, int y)
    {
	SCFGaussianLens l = new SCFGaussianLens(
				    1.0f, LENS_R1, LENS_R2, x - 400, y - 300);
	
	return l;
	// return new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - 400, y - 300);
    }
    
    boolean lensCursorSync = true;
    boolean showLense = true;

    void setUpRadiiDrawing()
    {
	if (!DO_FLATENING)
	{
	    lens.setDrawMaxFlatTop(true);
	    lens.setSpeedBlendRadii(false, false);
	}
	else
	{
	    if (FLATENING_VARIABLE)
	    {
		lens.setDrawMaxFlatTop(false);
		lens.setSpeedBlendRadii(false, false);
	    }
	    else
	    {
		lens.setDrawMaxFlatTop(true);
		lens.setSpeedBlendRadii(true, false);
	    }
	}
    }
    void toggleLensCursorSync(){
        lensCursorSync = !lensCursorSync;
	
	if (!lensCursorSync)
	    _ltimer.restart();
	else
	    _ltimer.stop();
    }
    
    void toggleLensFlattening()
    {
	DO_FLATENING = !DO_FLATENING;
	if (!DO_FLATENING)
	{
	    lens.setMinMagFactor((float)MAG_FACTOR);
	}
	else
	{
	    lens.setMinMagFactor(1.0f);
	}
	setUpRadiiDrawing();
    }

    void toggleLensSlide()
    {
	INFINITE_FRICTION = !INFINITE_FRICTION;
	if (!INFINITE_FRICTION)
	{
	    if (lensCursorSync)
		toggleLensCursorSync();
	    _ltimer.restart();
	}
	else
	{
	    _ltimer.restart();
	}
    }
    void toggleLensVarFlatening()
    {
	FLATENING_VARIABLE = !FLATENING_VARIABLE;
	if (FLATENING_VARIABLE)
	{
	    if (lensCursorSync)
		toggleLensCursorSync();
	    if (!DO_FLATENING)
		toggleLensFlattening();
	    speedCoupling.setSpeedParameters(300);
	    speedCoupling.setCoefParameters(0.05f,0.0f);
	}
	else
	{
	    // ok
	    speedCoupling.setSpeedParameters(200);
	    speedCoupling.setCoefParameters(0.05f,0.0f);
	}
	setUpRadiiDrawing();
    }
    void toggleLensSmoothing()
    {
	SMOOTH = !SMOOTH;
    }

    //long _prevCurrentTime = System.currentTimeMillis();
    void moveLens(double x, double y, long currentTime){
        if (lens == null){return;}
	_lensX = x; _lensY = y;
	lstock(x,y);
        lens.setAbsolutePosition((int)x, (int)y, currentTime);
        vsm.repaintNow();
    }
    
    int _lastX,_lastY;
    static int NUM_PSTOCK = 1;
    double[] _pX = new double[NUM_PSTOCK];
    double[] _pY = new double[NUM_PSTOCK];

    double _motor_space = 0;
    double _motor_space_x = 0;
    double _motor_space_y = 0;

    void pstock(double x, double y)
    {
	for (int i = NUM_PSTOCK-1; i > 0; i--)
	{
	    _pX[i] = _pX[i-1];
	    _pY[i] = _pY[i-1];
	}
	_pX[0] = x;
	_pY[0] = y;
    }

    boolean _wait_robot = false;
    double _prev_radius = LENS_R2;
    void lens_cursorMoved(int x, int y, boolean force, MouseEvent e)
    {

	if (_wait_robot || (x == _lastX && y == _lastY && !force))
	{
	    _wait_robot = false;
	    //System.out.println("Robot Return "+ x + ", " + y);
	    _prev_radius = getRadius();
	    return;
	}
	
	boolean new_code = (ELASTIC_BORDER);
	double d = Math.sqrt((_lensX-x)*(_lensX-x) + (_lensY-y)*(_lensY-y));
	double Rad2 = LENS_R2;  double Rad1 = LENS_R1;
	Rad2 = getRadius();

	if (d < Rad2 || ((x == _lastX && y == _lastY) && !force))
	{
	    _lastX = x; _lastY = y;
	    pstock((double)x, (double)y);

	    if (!force && _lensAttached == 1)
	    {
		_lensAttached = 0;
		doSlideLens(true);
	    }
	    _lensAttached = 0;
	    _motor_space = 0;
	    _prev_radius = Rad2;
	    lens.setXfocusOffset(0);
	    lens.setYfocusOffset(0);
	    //System.out.println("Inside Return");
	    //lens.setInnerRadiusColor(Color.BLACK);
	    return;
	}

	//lens.setInnerRadiusColor(Color.WHITE);
	_lensAttached = 1;
	// FIXME for the new idea ...
	_lensLastContactX = x;
	_lensLastContactY = y;

	// We want to compute the new position of 
	// p1 = _lastX, _lastY  p2 = x,y = x2,y2  g =  _lensX,_lensY = xg,yg
	// R = lense radius LENS_R2
	// we work in a cartesian world (O,X,Y) with O = p1
	
	double x2 =   (double)x - _pX[NUM_PSTOCK-1]; //_lastX;
	double y2 = - (double)y + _pY[NUM_PSTOCK-1]; //_lastY;
	double xg =   (double)_lensX - _pX[NUM_PSTOCK-1]; //_lastX;
	double yg = - (double)_lensY + _pY[NUM_PSTOCK-1]; //_lastY;

	//System.out.println("C: " + x + "," + y + "   " + x2 + "," + y2);

	double Gx,Gy;

	// stupid method
	// moveLens(_lensX + (x-_lastX), _lensY + (y-_lastY));

	double fac = (Rad1 - Rad2)/2;

	if (new_code && _motor_space < fac*(Rad1 - Rad2)) // new code
	{
	    double coef =  Math.pow(((Rad2 +(_motor_space/fac) + 1.0)-Rad2)/(Rad1 - Rad2),1.0);

	    //System.out.println("F0: " + x2 + ", " + y2 + "; " + coef);
	    if (_motor_space == 0.0)
	    {
		_motor_space = _motor_space +
		    Math.sqrt((x2)*(x2) + (y2)*(y2));
		//x2 = y2 = 0.0;
		_lastX = x; _lastY = y;
		pstock((double)x, (double)y);
		return;
	    }
	    else
	    {
		double _pms = _motor_space;
		_motor_space = _motor_space +
		    Math.sqrt((x2)*(x2) + (y2)*(y2));
		x2 = coef*x2 + (_pX[0] - (int)_pX[0]);
		y2 = coef*y2 + (_pY[0] - (int)_pY[0]);

		//System.out.println("F1: " + x2 + ", " + y2 + "; "+_motor_space);
		if (false && robot != null && e != null)
		{
		    int xx = (int)(x2 + (double)_pX[NUM_PSTOCK-1]); //_lastX;
		    int yy = (int)(- y2 + (double)_pY[NUM_PSTOCK-1]);//_lastY; 
		    Point ptRobot = new Point(xx, yy);
		    SwingUtilities.convertPointToScreen(
						      ptRobot, e.getComponent()); 
		    
		    robot.mouseMove((int)ptRobot.getX(), (int)ptRobot.getY());
		    _wait_robot = true;
		    //System.out.println(
		    //		    "RR: " + xx + "(" + (int)ptRobot.getX() +
		    //		    "), " + yy + "(" + (int)ptRobot.getY() + ")");
		}
	    }
	}
	// intersection de la droite 2,g et du cercle 2,Rad2
	double norm = Math.sqrt((x2 - xg)*(x2 - xg) + (y2 -yg)*(y2 -yg));
	double r =  Math.sqrt((xg*xg) + (yg*yg));
	r = Rad2;
	Gx = x2 - r*((x2-xg)/norm);
	Gy = y2 - r*((y2-yg)/norm);

	
	//System.out.println("G: " + (int)Gx + "," + (int)Gy + "    " +
	//		   _lensX  + "," + _lensY);

	
	if (false && _prev_radius != Rad2 && robot != null && e != null)
	{
	    // intersection de la droite 2,g et du cercle g,Rad2
	    double normg = Math.sqrt((x2 - xg)*(x2 - xg) + (y2 -yg)*(y2 -yg));
	    r = Rad2;
	    Gx = xg - r*((xg-x2)/normg);
	    Gy = yg - r*((yg-y2)/normg);
	    Gx = Gx + (double)_pX[NUM_PSTOCK-1]; //_lastX;
	    Gy = - Gy + (double)_pY[NUM_PSTOCK-1];//_lastY;
	    if (robot != null && e != null)
	    {
		Point ptRobot = new Point((int)Gx, (int)Gy);
		SwingUtilities.convertPointToScreen(
						    ptRobot, e.getComponent()); 
		robot.mouseMove((int)ptRobot.getX(), (int)ptRobot.getY());
		_wait_robot = true;
		_lastX = (int)Gx; _lastY = (int)Gy;
		pstock(Gx,Gy);
		_lensLastContactX = Gx;
		_lensLastContactY = Gy;
		//System.out.println(
		//		    "RR: " + xx + "(" + (int)ptRobot.getX() +
		//		    "), " + yy + "(" + (int)ptRobot.getY() + ")");
		return;
	    }
	}

	if (SMOOTH) // && !(DO_FLATENING && FLATENING_VARIABLE))
	{
	    
	    double ra = 2*(Rad1 - Rad2);

	    if (_motor_space <= ra) 
	    {
		double coef =  Math.pow(
					(_motor_space/ra),4.0);

		_motor_space = _motor_space +
		    Math.sqrt((x2)*(x2) + (y2)*(y2));
		double magFactor = 1 + (1-coef) * (lens.getMaximumMagnification() - 1);
		

		int dx = lens.getXfocusOffset() + (int)(Gx - xg);
		int dy = lens.getYfocusOffset() + (int)(Gy - yg);

		Gx = xg + (double)(dx / (int)magFactor);
		Gy = yg + (double)(dy / (int)magFactor);

		int ox = dx % (int)magFactor;
		int oy = dy %  (int)magFactor;
		lens.setXfocusOffset(ox);
		lens.setYfocusOffset(oy);
		if (false) {
		    System.out.println("Offsets: " + ox + " " + oy + " " +
				       (int)(Gx + (double)_pX[NUM_PSTOCK-1]) +
				       " " +
				       (int)(- Gy + (double)_pY[NUM_PSTOCK-1])
				       + " " + coef);
		}
		if (SMOOTH_WARPING && false)
		{
		    // FIXME: does not work !!!
		    // intersection de la droite 2,g et du cercle g,Rad2
		    double normg = Math.sqrt((x2 - Gx)*(x2 - Gx) + (y2 -Gy)*(y2 - Gy));
		    r = Rad2;
		    double nx,ny;
		    nx = Gx - Rad2*((Gx-x2)/normg);
		    ny = Gy - Rad2*((Gy-y2)/normg);
		    nx = nx + (double)_pX[NUM_PSTOCK-1]; //_lastX;
		    ny = - ny + (double)_pY[NUM_PSTOCK-1];//_lastY;
		    if (robot != null && e != null)
		    {
			Point ptRobot = new Point((int)nx, (int)ny);
			SwingUtilities.convertPointToScreen(
						  ptRobot, e.getComponent()); 
			robot.mouseMove((int)ptRobot.getX(),
					(int)ptRobot.getY());
			_wait_robot = true;
			_lastX = (int)nx;
			_lastY = (int)ny;
			pstock(nx,ny);
			_lensLastContactX = nx;
			_lensLastContactY = ny;
			//System.out.println(
			//		    "RR: " + xx + "(" + (int)ptRobot.getX() +
			//		    "), " + yy + "(" + (int)ptRobot.getY() + ")");
		    }
		}
	    }
	    else
	    {
		lens.setXfocusOffset(0);
		lens.setYfocusOffset(0);
	    }
	}

	// convert back to screen coordinate:
	Gx = Gx + (double)_pX[NUM_PSTOCK-1]; //_lastX;
	Gy = - Gy + (double)_pY[NUM_PSTOCK-1];//_lastY;
	_lensLastContactX = _lensLastContactX + (Gx - _lensX);
	_lensLastContactY = _lensLastContactY + (Gy - _lensY);
	moveLens(Gx, Gy, System.currentTimeMillis());
	
	if (new_code)
	    {
		x2 = x2 + (double)_pX[NUM_PSTOCK-1]; //_lastX;
		y2 = - y2 + (double)_pY[NUM_PSTOCK-1];//_lastY;
		_lastX = (int)x2; _lastY = (int)y2;
		pstock(x2,y2);
		//pstock(_lastX,_lastY);
		//System.out.println("F3: " + _lastX + ", " + _lastY);
	    }
	else if (!SMOOTH_WARPING)
	    {
		_lastX = x; _lastY = y;
		pstock(x,y);
	    }


    }

    void incX(){
        lens.setXfocusOffset(lens.getXfocusOffset()+1);
    }

    void incY(){
        lens.setYfocusOffset(lens.getYfocusOffset()+1);
    }

    public static void main(String[] args){
        new Test3();
    }
    
}

class EventHandlerTest3 implements ViewEventHandler{

    Test3 application;
    boolean precisionEnabled = false;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    EventHandlerTest3(Test3 appli){
        application=appli;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //application.toggleLens(jpx, jpy);
        application.indrag = 1;
	
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.indrag = 2;
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){

    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawDrag(true);
        VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(false);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
        v.setDrawDrag(false);
        VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	if (application.lens == null && application.showLense)
	{
	    // FIXME: create the lense in the center of the window
	    application.setLens(300, 300);
	    application.moveLens(jpx, jpy,System.currentTimeMillis());
	}
        else if (application.lensCursorSync){
	    application.lens.setXfocusOffset(0);
	    application.lens.setYfocusOffset(0);
            application.moveLens(jpx, jpy, System.currentTimeMillis());
        }
	else
	{
	    //application.lens.setXfocusOffset(0);
	    //application.lens.setYfocusOffset(0);
	    application.lens_cursorMoved(jpx, jpy, false, e);
	}
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            Camera c=VirtualSpaceManager.INSTANCE.getActiveCamera();
            float a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
            }
            else {
                VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
            }
        }
	else if (buttonNumber == 1)
	{
	    mouseMoved(v, jpx,jpy, e);
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c=VirtualSpaceManager.INSTANCE.getActiveCamera();
        float a=(c.focal+Math.abs(c.altitude))/c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
        else {
            //wheelDirection == WHEEL_DOWN
            c.altitudeOffset(a*5);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
    }

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_SPACE){application.toggleLensCursorSync();}
	else if (c == 'f') 
	    {application.toggleLensFlattening();}
	else if (c == 's') 
	    {application.toggleLensSlide();}
	else if (c == 'v') 
	    {application.toggleLensVarFlatening();}
	else if(c == 'p') {
	    precisionEnabled = !precisionEnabled;
	    application.mView.setFocusControlled(precisionEnabled, FocusControlHandler.SPEED_DEPENDENT_LINEAR); }
	else if(c == 'o') {
	    application.toggleLensSmoothing();
	}
    }
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}
