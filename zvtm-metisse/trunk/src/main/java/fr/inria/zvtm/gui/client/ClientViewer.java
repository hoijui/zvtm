package fr.inria.zvtm.gui.client;

import java.awt.GraphicsEnvironment;

import javax.swing.ImageIcon;

import fr.inria.zvtm.compositor.InputForwarder;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.gui.CursorHandler;
import fr.inria.zvtm.gui.MainEventHandler;
import fr.inria.zvtm.gui.NavigationManager;
import fr.inria.zvtm.gui.PCursor;
import fr.inria.zvtm.gui.Viewer;
import fr.inria.zvtm.gui.menu.GlyphEventDispatcherForMenu;
import fr.inria.zvtm.gui.menu.PopMenu;

public class ClientViewer extends Viewer{

	private InputForwarder infw;
	public PopMenu popmenu;
	

	public ClientViewer() {
		init();

		//centers the frame
		GraphicsEnvironment e =GraphicsEnvironment.getLocalGraphicsEnvironment();
		getView().getFrame().setLocation((int) (e.getCenterPoint().x-getView().getFrame().getWidth()*1./2), (int)(e.getCenterPoint().y-getView().getFrame().getHeight()*1./2));
		
		//cursor
		setCursor(new PCursor(this.cursorSpace,this.mSpace,this.menuSpace,mCamera,getMenuCamera(),listenMultiplexer,1,20));
		setCursorHandler(new CursorHandler(getCursor(), this));
		infw = new InputForwarder(this);
		eh.setViewer(this);
		ged = new GlyphEventDispatcherForMenu(this.getCursor().getPicker2(), this.menuSpace,this);
		popmenu = new PopMenu(menuSpace,this);
		ged.setMenu(popmenu);
		ged.setPriorityOn(eh);
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

	public InputForwarder getInputForwarder() {
		return infw;
	}

}
