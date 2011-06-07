package fr.inria.zvtm.gui.menu;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import javax.swing.ImageIcon;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.gui.GlyphListener;

public abstract class Item implements GlyphListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float MinTranslucency = 0.3f;
	private static final float MaxTranslucency = 1f;
	protected VImage shape;
	protected PopMenu parent;
	private ImageIcon img;	
	private ImageIcon img2;	
	private boolean active;
	private LinkedList<ItemActivationListener> listeners;
	protected boolean pressed;



	public Item(PopMenu parent) {
		listeners = new LinkedList<ItemActivationListener>();
		String imagePath = PopMenu.ressourcePath+getState1ImageName();
		String imagePath2 = PopMenu.ressourcePath+getState2ImageName();
		this.parent = parent;
		img = (new ImageIcon(imagePath));
		img2 = (new ImageIcon(imagePath2));
		shape = new VImage(img.getImage());
		shape.setDrawBorder(false);
		shape.addCamera(0);
		this.shape.setSensitivity(true);
		this.shape.setTranslucencyValue(0);	
		GlyphEventDispatcherForMenu.INSTANCE.subscribe(this.shape, this);
	}

	protected abstract String getState1ImageName();
	protected abstract String getState2ImageName();


	public void appear() {
		parent.getVirtualSpace().addGlyph(this.shape);
		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createTranslucencyAnim(100, this.shape, MinTranslucency, false, SlowInSlowOutInterpolator.getInstance(), null);
		if(this.shape.getTranslucencyValue()==0)
			VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);

	}

	public void disappear() {
		deactivate();
		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createTranslucencyAnim(100, this.shape, 0, false, SlowInSlowOutInterpolator.getInstance(), new EndAction() {

				@Override
				public void execute(Object subject,
						fr.inria.zvtm.animation.Animation.Dimension dimension) {
					parent.getVirtualSpace().removeGlyph(shape);
					deactivate();

				}
			});
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);

	}

	private void makeAppear(){
		if(parent.pressed)return;
		activate();
		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createTranslucencyAnim(100, this.shape, MaxTranslucency, false, SlowInSlowOutInterpolator.getInstance(), null);
		if(this.shape.getTranslucencyValue()==MinTranslucency)
			VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
	}

	private void makeDisappear(){
		if(parent.pressed)return;
		deactivate();
		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createTranslucencyAnim(100, this.shape, MinTranslucency, false, SlowInSlowOutInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
	}

	public void addListener(ItemActivationListener ial){
		listeners.add(ial);
	}

	public void removeListener(ItemActivationListener ial){
		listeners.remove(ial);
	}

	private void activate() {
		active = true;
		for (ItemActivationListener ial : listeners) {
			ial.activated();
		}
	}

	private void deactivate() {
		active = false;
		for (ItemActivationListener ial : listeners) {
			ial.deactivated();
		}
	}

	public void position(double vx, double vy) {
		shape.moveTo(vx, vy);
	}

	@Override
	public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e) {}

	@Override
	public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e) {}

	@Override
	public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {}

	@Override
	public void click1(ViewPanel v, int mod, int jpxx, int jpyy,
			int clickNumber, MouseEvent e) {}

	@Override
	public void click2(ViewPanel v, int mod, int jpxx, int jpyy,
			int clickNumber, MouseEvent e) {}

	@Override
	public void click3(ViewPanel v, int mod, int jpxx, int jpyy,
			int clickNumber, MouseEvent e) {}

	@Override
	public void glyphEntered() {
		makeAppear();
	}

	@Override
	public void glyphExited() {
		makeDisappear();
	}

	@Override
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,
			int jpyy, MouseEvent e) {}

	@Override
	public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {}

	@Override
	public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpxx,
			int jpyy, MouseWheelEvent e) {}

	@Override
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		parent.pressed = true;
		parent.viewer.getGlyphEventDispatcher().setAlwaysRepick(false);
	}

	@Override
	public void press2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		parent.pressed = true;
		parent.viewer.getGlyphEventDispatcher().setAlwaysRepick(false);
	}

	@Override
	public void press3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {	
		parent.pressed = true;
		parent.viewer.getGlyphEventDispatcher().setAlwaysRepick(false);
	}

	@Override
	public void release1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		parent.pressed = false;
		parent.viewer.getGlyphEventDispatcher().setAlwaysRepick(true);
	}

	@Override
	public void release2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		parent.pressed = false;
		parent.viewer.getGlyphEventDispatcher().setAlwaysRepick(true);

	}

	@Override
	public void release3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		parent.pressed = false;
		parent.viewer.getGlyphEventDispatcher().setAlwaysRepick(true);
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void drawUp(){
		shape.setImage(img.getImage());
	}

	public void drawDown(){
		shape.setImage(img2.getImage());
	}

	public void animatedMove(double offsetx,double offsety){
		parent.refreshAltFactor();
		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createGlyphTranslation(200, (Glyph)shape, new Point2D.Double(offsetx, offsety), true, SlowInSlowOutInterpolator.getInstance(), null);
			VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
			
	}
}
