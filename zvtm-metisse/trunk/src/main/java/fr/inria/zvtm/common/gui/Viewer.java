package fr.inria.zvtm.common.gui;

import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.SwingUtilities;

import fr.inria.zvtm.common.compositor.FrameManager;
import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.common.gui.menu.GlyphEventDispatcherForMenu;
import fr.inria.zvtm.common.gui.menu.PopMenu;
import fr.inria.zvtm.common.kernel.Temporizer;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * The minimal structure for building a zvtm {@link Viewer} related to this project.
 * @author Julien Altieri
 *
 */
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
	private VirtualSpace menuSpace;
	protected Camera menuCamera;
	
	private MainEventHandler eh;
	protected NavigationManager nm;
	private FrameManager fm;
	protected ViewListenerMultiplexer listenMultiplexer;
	private PCursor cursor;
	private CursorHandler cursorHandler;
	protected GlyphEventDispatcherForMenu ged;
	protected Vector<Camera> cameras;

	/**
	 * Need to be called right after instantiation.
	 */
	public void init() {
		vsm = VirtualSpaceManager.INSTANCE;
		
		preInitHook();
		
		this.mSpace = vsm.addVirtualSpace(Messages.mSpaceName);
		this.mCamera = this.mSpace.addCamera();
		this.mCamera.setZoomFloor(-99);
		this.cursorSpace = vsm.addVirtualSpace("cursorSpace");
		this.cursorCamera = this.cursorSpace.addCamera();
		this.cursorCamera.setZoomFloor(-99);
		this.mCamera.stick(cursorCamera,true);
		this.menuSpace = vsm.addVirtualSpace("menuSpace");
		this.menuCamera = this.getMenuSpace().addCamera();
		
		windowLayout();
		
		initNavigation();
		
		cameras = new Vector<Camera>();
		cameras.add(mCamera);
		cameras.add(getMenuCamera());
		cameras.add(cursorCamera);
		mView = (EView)vsm.addFrameView(cameras, Messages.mViewName, View.STD_VIEW, VIEW_W, VIEW_H,false, false, true, null);
		mView.getFrame().setFocusTraversalKeysEnabled(false);
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

	/**
	 * Hook called just at the beginning to initialization.
	 */
	protected void preInitHook(){}
	
	/**
	 * Hook called at the end of initialization.
	 */
	protected void viewCreatedHook(){}

	/**
	 * Hook to add a background to the {@link VirtualSpace}.
	 */
	protected void addBackground() {}

	/**
	 * Must set a {@link NavigationManager}.
	 */
	public abstract void initNavigation();

	/**
	 * Must return the desired type of {@link MainEventHandler}
	 * @return The {@link MainEventHandler} that will be attached to this {@link Viewer}
	 */
	public abstract MainEventHandler makeEventHandler();

	/**
	 * @return the drawing list of the main {@link VirtualSpace} (containing Metisse windows)
	 */
	public Glyph[] getDrawingStack(){
		return mSpace.getDrawingList();
	}
	
	/**
	 * Adds a {@link MetisseWindow} to the main {@link VirtualSpace}.
	 * @param f a {@link MetisseWindow} object
	 */
	public void addFrame(final MetisseWindow f){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mSpace.addGlyph(f);
			}
		});
		refresh();
	}

	/**
	 * Removes the specified {@link MetisseWindow} from the main {@link VirtualSpace}. Does not test weather or not this window already exists (must be done before). 
	 * @param f a {@link MetisseWindow} object
	 */
	public void remFrame(final MetisseWindow f){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mSpace.removeGlyph(f);
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

	/**
	 * Forces refreshing the zvtm view.
	 */
	public void refresh() {
		if(System.currentTimeMillis()-lastRepaintTime>Temporizer.repaintMinDelay){
			mView.repaint();
			VirtualSpaceManager.INSTANCE.repaint();
			lastRepaintTime = System.currentTimeMillis();
		}
	}

	/**
	 * 
	 * @return the {@link FrameManager} attached to this {@link Viewer}
	 */
	public FrameManager getFrameManager(){
		return fm;
	}

	/**
	 * 
	 * @return the {@link NavigationManager} attached to this {@link Viewer}
	 */
	public NavigationManager getNavigationManager() {
		return nm;
	}

	/**
	 * Set the specified {@link FrameManager} as attached to this {@link Viewer}
	 * @param framemanager
	 */
	public void setFrameManager(FrameManager framemanager) {
		this.fm = framemanager;
	}
	
	/**
	 * 
	 * @return the {@link VirtualSpace} where Metisse windows are drawn
	 */
	public VirtualSpace getVirtualSpace() {
		return mSpace;
	}

	/**
	 * Sets the {@link CursorHandler} attached to this {@link Viewer}.
	 * @param cursorHandler
	 */
	public void setCursorHandler(CursorHandler cursorHandler) {
		this.cursorHandler = cursorHandler;
	}

	/**
	 * 
	 * @return the {@link CursorHandler} which handles this {@link Viewer} main {@link PCursor}
	 */
	public CursorHandler getCursorHandler() {
		return cursorHandler;
	}

	/**
	 * Set the {@link PCursor} attached to this {@link Viewer}
	 * @param cursor a {@link PCursor} object.
	 */
	public void setCursor(PCursor cursor) {
		this.cursor = cursor;
	}

	/**
	 * 
	 * @return The {@link PCursor} attached to this {@link Viewer}
	 */
	public PCursor getCursor() {
		return cursor;
	}

	/**
	 * 
	 * @return The {@link GlyphEventDispatcher} attached to this {@link Viewer}
	 */
	public GlyphEventDispatcher getGlyphEventDispatcher() {
		return ged;
	}

	/**
	 * 
	 * @return The {@link EView} attached to this {@link Viewer}
	 */
	public EView getView() {
		return mView;
	}

	/**
	 * 
	 * @return The {@link Camera} observing {@link PopMenu}'s {@link VirtualSpace}
	 */
	public Camera getMenuCamera() {
		return menuCamera;
	}

	/**
	 * 
	 * @return The {@link VirtualSpace} which contains the {@link PopMenu}
	 */
	public VirtualSpace getMenuSpace() {
		return menuSpace;
	}
	
	/**
	 * 
	 * @return The {@link MainEventHandler} attached to this {@link Viewer}
	 */
	public MainEventHandler getMainEventListener() {
		return eh;
	}




	
}
