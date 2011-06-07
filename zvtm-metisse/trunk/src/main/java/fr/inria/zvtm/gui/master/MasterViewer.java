package fr.inria.zvtm.gui.master;

import javax.swing.ImageIcon;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.gui.MainEventHandler;
import fr.inria.zvtm.gui.Viewer;

public class MasterViewer extends Viewer{
	
	public MasterViewer() {
		init();
	}

	@Override
	public MainEventHandler makeEventHandler() {
		return new MasterMainEventHandler();		
	}

	@Override
	public void initNavigation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addBackground() {
		ImageIcon img = (new ImageIcon("src/main/java/fr/inria/zvtm/resources/bg.jpg"));
		Glyph g = new VImage(img.getImage());
		mSpace.addGlyph(g);
	}
	
}
