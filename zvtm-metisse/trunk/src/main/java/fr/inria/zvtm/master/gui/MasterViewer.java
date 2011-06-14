package fr.inria.zvtm.master.gui;

import java.awt.GraphicsEnvironment;

import javax.swing.ImageIcon;

import fr.inria.zvtm.common.gui.MainEventHandler;
import fr.inria.zvtm.common.gui.NavigationManager;
import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;

public class MasterViewer extends Viewer{
	
	private CursorMultiplexer cursorMultiplexer;
	private Bouncer bouncer;

	public MasterViewer() {
		init();
		bouncer = new Bouncer();
		
		//centers the frame
		GraphicsEnvironment e =GraphicsEnvironment.getLocalGraphicsEnvironment();
		getView().getFrame().setLocation((int) (e.getCenterPoint().x-getView().getFrame().getWidth()*1./2), (int)(e.getCenterPoint().y-getView().getFrame().getHeight()*1./2));
		
		getMainEventListener().setViewer(this);
		cursorMultiplexer = new CursorMultiplexer(this);
		
		ged = new GEDMultiplexer();
		this.listenMultiplexer.addListerner(ged);
		
	}

	@Override
	public MainEventHandler makeEventHandler() {
		return new MasterMainEventHandler(null);		
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

	public CursorMultiplexer getCursorMultiplexer() {
		return cursorMultiplexer;
	}

	public VirtualSpace getCursorSpace() {
		return cursorSpace;
	}

	public Bouncer getBoucer() {
		return bouncer;
	}
	

	public void sendViewUpgrade() {
		PCursor.wallBounds = getView().getVisibleRegion(mCamera);
		bouncer.sendViewUpgrade(PCursor.wallBounds);
	}
	
}
