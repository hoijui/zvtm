package fr.inria.zvtm.client.gui;

import java.awt.GraphicsEnvironment;

import javax.swing.ImageIcon;

import fr.inria.zvtm.common.compositor.InputForwarder;
import fr.inria.zvtm.common.gui.CursorHandler;
import fr.inria.zvtm.common.gui.MainEventHandler;
import fr.inria.zvtm.common.gui.NavigationManager;
import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.common.gui.menu.GlyphEventDispatcherForMenu;
import fr.inria.zvtm.common.gui.menu.PopMenu;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;

/**
 * The Zvtm viewer's specialization for a client use
 * @author Julien Altieri
 *
 */
public class ClientViewer extends Viewer{

	private InputForwarder infw;
	public PopMenu popmenu;
	

	public ClientViewer() {
		init();

		//centers the frame
		GraphicsEnvironment e =GraphicsEnvironment.getLocalGraphicsEnvironment();
		getView().getFrame().setLocation((int) (e.getCenterPoint().x-getView().getFrame().getWidth()*1./2), (int)(e.getCenterPoint().y-getView().getFrame().getHeight()*1./2));
		
		//cursor
		setCursor(new PCursor(this.cursorSpace,this.mSpace,this.getMenuSpace(),mCamera,getMenuCamera(),listenMultiplexer,1,20));
		setCursorHandler(new CursorHandler(getCursor(), this));
		infw = new InputForwarder(this);
		getMainEventListener().setViewer(this);
		ged = new GlyphEventDispatcherForMenu(this.getCursor(), this.getMenuSpace(),this);
		popmenu = new PopMenu(getMenuSpace(),this,ged,1);
		ged.setMenu(popmenu);
		ged.setPriorityOn(getMainEventListener());
		this.listenMultiplexer.addListerner(ged);
	}

	@Override
	public MainEventHandler makeEventHandler() {
		return new ClientMainEventHandler();
	}


	@Override
	public void initNavigation() {
		nm = new NavigationManager(this);
		nm.setCamera(mCamera);
	}
	
	
	@Override
	protected void addBackground() {
		ImageIcon img = (new ImageIcon("src/main/java/fr/inria/zvtm/resources/bg.jpg"));
		Glyph g = new VImage(img.getImage());
		mSpace.addGlyph(g);
	}

	/**
	 * 
	 * @return The {@link InputForwarder} attached to the viewer
	 * @see InputForwarder
	 */
	public InputForwarder getInputForwarder() {
		return infw;
	}

	/**
	 * This handles a message sent by the zvtm server which forces the virtual cursor to be called back. It happens when the server's view is zoomed, since the cursor can exit the visible region and be lost.
	 * The Cursor will be set to the center of the client's screen.
	 */
	public void handleResetCursor() {
		if(getCursorHandler().VirtualMode())getCursorHandler().resetCursorPos();
	}

}
