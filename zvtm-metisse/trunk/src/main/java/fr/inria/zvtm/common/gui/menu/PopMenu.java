package fr.inria.zvtm.common.gui.menu;

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

public class PopMenu {
	public static String ressourcePath = "src/main/java/fr/inria/zvtm/resources/";
	public static double DEFAULTACTIVERADIUS = 60;
	private VirtualSpace virtualSpace;
	private LinkedList<Item> itemList;
	protected MetisseWindow parentFrame;
	private double activityRadius = DEFAULTACTIVERADIUS;
	private boolean invoked = false;
	double refVX;
	private double refVY;
	protected Viewer viewer;
	private double altFactor =1;
	public boolean pressed = false;
	public TeleportItem teleport;
	private VImage range;
	protected GlyphEventDispatcher ged;
	private DragItem drag;
	double factor = 1;


	public PopMenu(VirtualSpace v,Viewer viewer, GlyphEventDispatcherForMenu ged,double factor) {
		this.factor  =factor;
		this.virtualSpace = v;
		this.ged = ged;
		this.viewer = viewer;
		this.itemList = new LinkedList<Item>();
		range = new VImage((new ImageIcon(ressourcePath+"range.png")).getImage());
		range.scaleFactor = factor;
		range.setSensitivity(false);
		range.setTranslucencyValue(0.3f);
		range.setDrawBorder(false);
		teleport = new TeleportItem(this);
		drag = new DragItem(this);
		if (viewer instanceof ClientViewer)itemList.add(drag);
		itemList.add(new ScaleItem(this));
		itemList.add(new ResetItem(this));
		if (viewer instanceof ClientViewer)itemList.add(teleport);
		
	}

	private void invoke(double vxx, double vyy, MetisseWindow frame) {
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
			if(viewer instanceof ClientViewer && !itemList.contains(drag))itemList.add(drag);
			if(viewer instanceof MasterViewer && itemList.contains(drag))itemList.remove(drag);
			if(itemList.contains(teleport))itemList.remove(teleport);
		}
		else {
			if(!itemList.contains(drag))itemList.add(drag);
			if(viewer instanceof ClientViewer)if(!itemList.contains(teleport))itemList.add(teleport);
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

	
	public void banish() {
		virtualSpace.removeGlyph(range);
		for (Item it : itemList) {
			it.disappear();
		}
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

	public void toggle(double vx, double vy, MetisseWindow frame,double radius) {
		this.activityRadius = radius*factor;
		if(invoked)banish();
		else {
			invoke(vx, vy, frame,this.activityRadius);
		}

	}

	public void invoke(double vx, double vy, MetisseWindow frame,double radius) {
		if(invoked)return;
		this.activityRadius = radius*factor;
		invoke(vx, vy, frame);
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
		return ((vx-range.vx)*(vx-range.vx)+(vy-range.vy)*(vy-range.vy))<=activityRadius*activityRadius;
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
}
