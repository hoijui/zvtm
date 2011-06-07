package fr.inria.zvtm.gui;

import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.SwingUtilities;

import fr.inria.zvtm.compositor.FrameManager;
import fr.inria.zvtm.compositor.MetisseWindow;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.gui.menu.GlyphEventDispatcherForMenu;
import fr.inria.zvtm.kernel.Temporizer;

public abstract class Viewer {

	/* screen dimensions, actual dimensions of windows */
	private int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
	private int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
	private int VIEW_MAX_W = 1024;  // 1400
	private int VIEW_MAX_H = 768;   // 1050
	protected int VIEW_W;
	protected int VIEW_H;
	@SuppressWarnings("unused")
	private int VIEW_X, VIEW_Y;
	private long lastRepaintTime ;

	private EView mView;
	protected VirtualSpaceManager vsm;
	protected VirtualSpace mSpace;
	protected Camera mCamera;
	protected VirtualSpace cursorSpace;
	protected Camera cursorCamera;
	protected VirtualSpace menuSpace;
	private Camera menuCamera;
	
	protected MainEventHandler eh;
	protected NavigationManager nm;
	private FrameManager fm;
	protected ViewListenerMultiplexer listenMultiplexer;
	private PCursor cursor;
	private CursorHandler cursorHandler;
	protected GlyphEventDispatcherForMenu ged;

	public void init() {
		preInitHook();
		
		vsm = VirtualSpaceManager.INSTANCE;
		
		this.mSpace = vsm.addVirtualSpace(Messages.mSpaceName);
		this.mCamera = this.mSpace.addCamera();
		this.mCamera.setZoomFloor(-99);
		this.cursorSpace = vsm.addVirtualSpace("cursorSpace");
		this.cursorCamera = this.cursorSpace.addCamera();
		this.cursorCamera.setZoomFloor(-99);
		this.mCamera.stick(cursorCamera,true);
		this.menuSpace = vsm.addVirtualSpace("menuSpace");
		this.menuCamera = this.menuSpace.addCamera();
		
		windowLayout();
		
		initNavigation();
		
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(mCamera);
		cameras.add(getMenuCamera());
		cameras.add(cursorCamera);
		mView = (EView)vsm.addFrameView(cameras, Messages.mViewName, View.STD_VIEW, VIEW_W, VIEW_H,false, false, true, null);
		mView.getCursor().setVisibility(false);
		mView.mouse.setSensitivity(false);
		mView.setVisible(true);	
		
		this.eh = makeEventHandler();	
		this.listenMultiplexer = new ViewListenerMultiplexer();
		listenMultiplexer.addListerner(eh);
		mView.setListener(listenMultiplexer);
		
		mView.setAntialiasing(true);
		mView.setBackgroundColor(Config.BACKGROUND_COLOR);

		viewCreatedHook();
		addBackground();
	}

	protected void preInitHook(){}
	
	protected void viewCreatedHook(){}

	protected void addBackground() {}

	public abstract void initNavigation();

	public abstract MainEventHandler makeEventHandler();

	public Glyph[] getDrawingStack(){
		return mSpace.getDrawingList();
	}
	
	public void addFrame(final MetisseWindow f){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mSpace.addGlyph(f);
				f.setDisplayed(true);
			}
		});
		refresh();
	}

	public void remFrame(final MetisseWindow f){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mSpace.removeGlyph(f);
				f.setDisplayed(false);
			}
		});
		refresh();
	}
	
	private void windowLayout(){
		if (Utils.osIsWindows()){
			VIEW_X = VIEW_Y = 0;
		}
		else if (Utils.osIsMacOS()){
			VIEW_X = 80;
			SCREEN_WIDTH -= 80;
		}
		VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
		VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
	}

	public void refresh() {
		if(System.currentTimeMillis()-lastRepaintTime>Temporizer.repaintMinDelay){
			mView.repaint();
			VirtualSpaceManager.INSTANCE.repaint();
			lastRepaintTime = System.currentTimeMillis();
		}
	}

	
	public FrameManager getFrameManager(){
		return fm;
	}

	public NavigationManager getNavigationManager() {
		return nm;
	}

	public void setFrameManager(FrameManager framemanager) {
		this.fm = framemanager;
	}
	
	public VirtualSpace getVirtualSpace() {
		return mSpace;
	}

	public void setCursorHandler(CursorHandler cursorHandler) {
		this.cursorHandler = cursorHandler;
	}

	public CursorHandler getCursorHandler() {
		return cursorHandler;
	}

	public void setCursor(PCursor cursor) {
		this.cursor = cursor;
	}

	public PCursor getCursor() {
		return cursor;
	}

	public GlyphEventDispatcher getGlyphEventDispatcher() {
		return ged;
	}

	public EView getView() {
		return mView;
	}

	public Camera getMenuCamera() {
		return menuCamera;
	}
	
}
