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
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.master.gui.MasterViewer;
import fr.inria.zvtm.master.gui.PCursorPack;

/**
 * The {@link PopMenu} is aimed at concentrating all the zvtm interaction.
 * It pops around the cursor and anchors at the invocation cursor's position and disappear when the cursor goes out of range, or when the user voluntarily banishes it.
 * @author Julien Altieri
 *
 */
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

	/**
	 * 
	 * @param v the {@link VirtualSpace} where the Items will be drawn
	 * @param viewer The {@link Viewer} who owns the {@link PopMenu}
	 * @param ged The {@link GlyphEventDispatcherForMenu} for interaction purpose
	 * @param factor A global scale factor (on the wall, it is set to 4 instead of 1)
	 */
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

	/**
	 * Makes the {@link PopMenu} appear at the specified virtual coordinates.
	 * @param vxx
	 * @param vyy
	 * @param frame the frame with whom the menu deals with
	 */
	public void invoke(double vxx, double vyy, MetisseWindow frame) {
		if(invoked)return;
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

	/**
	 * Makes the menu disappear.
	 */
	public void banish() {
		virtualSpace.removeGlyph(range);
		for (Item it : itemList) {
			it.disappear();
		}
		itemList.removeAll(itemList);
		invoked = false;
	}

	/**
	 * Since the menu {@link Camera} can move, we may need to recalculate the altfactor.
	 */
	public void refreshAltFactor(){
		setAltFactor(((viewer.getNavigationManager().getCamera().focal+(viewer.getNavigationManager().getCamera().altitude))/ viewer.getNavigationManager().getCamera().focal));
	}

	/**
	 * Effectively places the items circle aroung the cursor
	 * @param mx the x center of the circle
	 * @param my the y center of the circle
	 */
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

	/**
	 * 
	 * @return The {@link VirtualSpace} where the items are drawn
	 */
	public VirtualSpace getVirtualSpace() {
		return virtualSpace;
	}


	/**
	 * Makes the menu appears if not visible, disappear otherwise.
	 * @param vx virtual x coordinate where the menu should be invoked
	 * @param vy virtual y coordinate where the menu should be invoked
	 * @param frame the frame whom the menu deals with
	 */
	public void toggle(double vx, double vy, MetisseWindow frame) {
		if(invoked)banish();
		else {
			invoke(vx, vy, frame);
		}
	}

	/**
	 * 
	 * @return The current invocation state.
	 */
	public boolean isInvoked(){
		return invoked;
	}

	/**
	 * Test if the mouse is still in the authorized range and banishes the menu if it isnot the case.
	 * @param vxx virtual x coordinates of the mouse
	 * @param vyy virtual y coordinates of the mouse
	 */
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

	/**
	 * Banishes the menu if no {@link Item} is active.
	 */
	public void tryToBanish(){
		for (Item it : itemList) {
			if(it.isActive())return;
		}
		banish();
		return;
	}

	/**
	 * 
	 * @return The list of the Items contained in the menu.
	 */
	public LinkedList<Item> getItems() {
		return itemList;
	}


	/**
	 * Relatively moves the menu.
	 * @param dx relative x move
	 * @param dy relative y move
	 */
	public void move(double dx, double dy) {
		refreshAltFactor();
		refVX +=dx*1;
		refVY +=dy*1;
		repositionate(refVX/getAltFactor(),refVY/getAltFactor());
	}

	/**
	 * 
	 * @return The altFactor of the menu's {@link Camera}
	 */
	public double getAltFactor() {
		return altFactor;
	}

	/**
	 * Sets the altFactor
	 * @param d
	 */
	public void setAltFactor(double d) {
		this.altFactor = d;
	}

	/**
	 * Absolutely moves the menu
	 * @param d x virtual coordinate
	 * @param e y virtual coordinate
	 */
	public void moveTo(double d, double e) {
		refVX = d;
		refVY = e;
		repositionate(refVX/getAltFactor(),refVY/getAltFactor());

	}

	/**
	 * Relatively moves the menu with an animation.
	 * @param dragableItem the {@link DragableItem} which is responsible for this request
	 * @param d relative x move
	 * @param e relative y move
	 */
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

	/**
	 * Used in the master version, set the {@link PCursorPack} owing the menu.
	 * @param pCursorPack
	 */
	public void setOwner(PCursorPack pCursorPack) {
		owner = pCursorPack;
	}
}
