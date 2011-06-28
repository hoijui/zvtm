package fr.inria.zvtm.common.gui.menu;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import javax.swing.ImageIcon;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.client.gui.ClientViewer;
import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.common.gui.GlyphEventDispatcher;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.master.gui.MasterViewer;
import fr.inria.zvtm.master.gui.PCursorPack;

public class PopMenu {
	public static String ressourcePath = "src/main/java/fr/inria/zvtm/resources/";
	public static double DEFAULTACTIVERADIUS = 60;
	private VirtualSpace virtualSpace;
	private LinkedList<Item> itemList;
	protected MetisseWindow parentFrame;
	private boolean invoked = false;
	double refVX;
	private double refVY;
	protected Viewer viewer;
	private double altFactor =1;
	public boolean pressed = false;
	public TeleportItem teleport;
	private VImage range;
	protected GlyphEventDispatcher ged;
	private DragItem pan;
	double factor = 1;
	private ShareItem share;
	private ScaleItem scale;
	private ResetItem reset;
	private PCursorPack owner;


	public PopMenu(VirtualSpace v,Viewer viewer, GlyphEventDispatcherForMenu ged,double factor) {
		this.factor  =factor;
		this.virtualSpace = v;
		this.ged = ged;
		this.viewer = viewer;
		this.itemList = new LinkedList<Item>();
		Image img = (new ImageIcon(ressourcePath+"range.png")).getImage();
		range = new VImage(img);
		range.setImage(img);
		range.reSize(factor);
		range.setSensitivity(false);
		range.setTranslucencyValue(0.3f);
		range.setDrawBorder(false);
		teleport = new TeleportItem(this);
		pan = new DragItem(this);
		share = new ShareItem(this);
		scale = new ScaleItem(this);
		reset = new ResetItem(this);
	}

	public void invoke(double vxx, double vyy, MetisseWindow frame) {
		refreshAltFactor();
		double vx = (vxx - viewer.getNavigationManager().getCamera().getLocation().vx)/getAltFactor();
		double vy = (vyy - viewer.getNavigationManager().getCamera().getLocation().vy)/getAltFactor();
		if(frame!=null){
			if(frame.isRoot())parentFrame=null;
			else {
				this.parentFrame = frame;
			}
		}
		else parentFrame = null;
		if(parentFrame==null){
			if(viewer instanceof ClientViewer)composeMenu1();
			if(viewer instanceof MasterViewer)composeMenu2();
		}
		else {
			if(viewer instanceof ClientViewer)composeMenu3();
			if(viewer instanceof MasterViewer)composeMenu4();
		}
		virtualSpace.addGlyph(range);
		for (Item it : itemList) {
			it.appear();
		}
		invoked = true;
		refreshAltFactor();
		refVX = vx*getAltFactor();
		refVY = vy*getAltFactor();
		repositionate(vx, vy);
	
		
	}

	/**
	 * Metisse frame / Master Version
	 */
	private void composeMenu4() {
		itemList.add(pan);		
		itemList.add(scale);	
		itemList.add(reset);	
		if(((MasterViewer)viewer).getBouncer().testOwnership(owner, parentFrame.getId()))
		itemList.add(share);
	}
	/**
	 * Metisse frame / Client Version
	 */
	private void composeMenu3() {
		itemList.add(pan);		
		itemList.add(scale);	
		itemList.add(reset);	
		itemList.add(teleport);
	}
	/**
	 * Root frame / Master Version
	 */
	private void composeMenu2() {		
		itemList.add(scale);	
		itemList.add(reset);	
	}
	/**
	 * Root frame / Client Version
	 */
	private void composeMenu1() {
		itemList.add(pan);		
		itemList.add(scale);	
		itemList.add(reset);		
	}

	public void banish() {
		virtualSpace.removeGlyph(range);
		for (Item it : itemList) {
			it.disappear();
		}
		itemList.removeAll(itemList);
		invoked = false;
	}

	public void refreshAltFactor(){
		setAltFactor(((viewer.getNavigationManager().getCamera().focal+(viewer.getNavigationManager().getCamera().altitude))/ viewer.getNavigationManager().getCamera().focal));
	}

	void repositionate(double mx, double my){
		if(itemList.size()<2)return;
		double offset = 0.;
		double a = offset ;
		double r = 30*factor;
		double pi = 2*Math.PI;
		range.moveTo(mx, my);
		for (Item it : itemList) {
			it.position(mx+r*Math.sin(a),my+r*Math.cos(a));
			a+=(pi-2*offset)/(itemList.size()-0);
		}
	}

	public VirtualSpace getVirtualSpace() {
		return virtualSpace;
	}

	public void toggle(double vx, double vy, MetisseWindow frame) {
		if(invoked)banish();
		else {
			invoke(vx, vy, frame);
		}

	}


	public boolean isInvoked(){
		return invoked;
	}

	public void mouseMove(double vxx, double vyy){
		refreshAltFactor();
		double vx = vxx - viewer.getNavigationManager().getCamera().getLocation().vx;
		double vy = vyy - viewer.getNavigationManager().getCamera().getLocation().vy;
		if(invoked && !validCoord(vx,vy)){
			banish();
		}
	}

	private boolean validCoord(double vxx, double vyy) {
		double vx = vxx/getAltFactor();
		double vy = vyy/getAltFactor();
		return ((vx-range.vx)*(vx-range.vx)+(vy-range.vy)*(vy-range.vy))<=0.25*range.vw*range.vw;
	}

	public void tryToBanish(){
		for (Item it : itemList) {
			if(it.isActive())return;
		}
		banish();
		return;
	}

	public LinkedList<Item> getItems() {
		return itemList;
	}



	public void move(double dx, double dy) {
		refreshAltFactor();
		refVX +=dx*1;
		refVY +=dy*1;
		repositionate(refVX/getAltFactor(),refVY/getAltFactor());
	}


	public double getAltFactor() {
		return altFactor;
	}

	public void setAltFactor(double d) {
		this.altFactor = d;
	}

	public void moveTo(double d, double e) {
		refVX = d;
		refVY = e;
		repositionate(refVX/getAltFactor(),refVY/getAltFactor());

	}

	public void animatedMove(DragableItem dragableItem, double d, double e) {
		refVX = range.vx + d;
		refVY = range.vy + e;
		
		
		for (Item it : itemList) {
			if(it==dragableItem){
				continue;
			}
			it.animatedMove(d, e);
		}
		refreshAltFactor();
		
		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createGlyphTranslation(200, (Glyph)range, new Point2D.Double(d, e), true, SlowInSlowOutInterpolator.getInstance(),null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
	}

	public void setOwner(PCursorPack pCursorPack) {
		owner = pCursorPack;
	}
}
