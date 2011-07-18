package fr.inria.zvtm.common.gui.menu;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation.Dimension;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;

/**
 * A generic kind of {@link Item} which accepts 4 drawing states, useful for toggles, who require a "upstate" "downstate" for both their two states.
 * @author Julien Altieri
 *
 */
public abstract class ToggleItem extends Item {

	protected VImage shape3;
	protected VImage shape4;
	private ImageIcon img3;	
	private ImageIcon img4;	
	protected int status = 0;

	public ToggleItem(PopMenu parent) {
		super(parent);
		String imagePath3 = PopMenu.ressourcePath+getState3ImageName();
		String imagePath4 = PopMenu.ressourcePath+getState4ImageName();
		img3 = (new ImageIcon(imagePath3));
		img4 = (new ImageIcon(imagePath4));

		shape3 = new VImage(img3.getImage()){
			@Override
			public boolean coordInside(int jpx, int jpy, int camIndex, double cvx,double cvy) {
				return((cvx-vx)*(cvx-vx)+(cvy-vy)*(cvy-vy)<=vh*vw/4);
			}
		};
		shape4 = new VImage(img4.getImage()){
			@Override
			public boolean coordInside(int jpx, int jpy, int camIndex, double cvx,double cvy) {
				return((cvx-vx)*(cvx-vx)+(cvy-vy)*(cvy-vy)<=vh*vh/4);
			}
		};

		shape3.reSize(parent.factor);
		shape4.reSize(parent.factor);
		shape3.setDrawBorder(false);
		shape4.setDrawBorder(false);
		shape3.addCamera(0);
		shape4.addCamera(0);
		this.shape3.setSensitivity(true);
		this.shape4.setSensitivity(true);
		this.shape3.setTranslucencyValue(0);	
		this.shape4.setTranslucencyValue(0);	
		this.parent.ged.subscribe(this.shape3, this);
		this.parent.ged.subscribe(this.shape4, this);

	}

	/**
	 * @return The filename of the png image to draw in state 3.
	 */
	protected abstract String getState3ImageName();
	/**
	 * @return The filename of the png image to draw in state 4.
	 */
	protected abstract String getState4ImageName();




	public void appear() {
		super.appear();
		parent.getVirtualSpace().addGlyph(this.shape3);
		parent.getVirtualSpace().addGlyph(this.shape4);
		appearAnim(shape3, 0, MinTranslucency);
		appearAnim(shape4, 0, MinTranslucency);
	}

	public void disappear() {
		super.disappear();
		
		EndAction e1 = new EndAction() {
			
			@Override
			public void execute(Object subject, Dimension dimension) {
				parent.getVirtualSpace().removeGlyph(shape3);
				deactivate();
			}
		};
		EndAction e2 = new EndAction() {
			
			@Override
			public void execute(Object subject, Dimension dimension) {
				parent.getVirtualSpace().removeGlyph(shape4);
				deactivate();
			}
		};
		appearAnim(shape3,-1,0,e1);
		appearAnim(shape4,-1,0,e2);
	}


	protected void makeAppear(){
		super.makeAppear();
		if(parent.pressed)return;
		appearAnim(shape3, MinTranslucency, MaxTranslucency);
		appearAnim(shape4, MinTranslucency, MaxTranslucency);
	}

	protected void makeDisappear(){
		super.makeDisappear();
		if(parent.pressed)return;
		appearAnim(shape3, -1, MinTranslucency);
		appearAnim(shape4, -1, MinTranslucency);	
	}

	public void position(double vx, double vy) {
		super.position(vx, vy);
		shape3.moveTo(vx, vy);
		shape4.moveTo(vx, vy);
	}


	public void drawUp(){
		if(status==0){
			shape.setVisible(true);
			shape2.setVisible(false);
			if(shape3!=null)shape3.setVisible(false);
			if(shape4!=null)shape4.setVisible(false);
		}
		else if(status==1){
			shape.setVisible(false);
			shape2.setVisible(false);
			if(shape3!=null)shape3.setVisible(true);
			if(shape4!=null)shape4.setVisible(false);
		}
	}


	public void drawDown(){
		if(status==0){
			shape.setVisible(false);
			shape2.setVisible(true);
			shape3.setVisible(false);
			shape4.setVisible(false);
		}
		else if(status==1){
			shape.setVisible(false);
			shape2.setVisible(false);
			shape3.setVisible(false);
			shape4.setVisible(true);
		}
	}


	public void animatedMove(double offsetx,double offsety){
		parent.refreshAltFactor();
		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createGlyphTranslation(200, (Glyph)shape3, new Point2D.Double(offsetx, offsety), true, SlowInSlowOutInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);

		Animation trans2 = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createGlyphTranslation(200, (Glyph)shape4, new Point2D.Double(offsetx, offsety), true, SlowInSlowOutInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans2, false);


	}


	@Override
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press1(v, mod, jpxx, jpyy, e);
		drawDown();
	}

	@Override
	public void release1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.release1(v, mod, jpxx, jpyy, e);
		drawUp();

	}

	/**
	 * Set the drawing status of the {@link Item} (true for state 1 and 2, and false for state 3 and 4)
	 * @param status
	 */
	public void setState(int status){
		this.status = status;
		refresh();
	}

	private void refresh() {
		if(shape.isVisible()||shape3.isVisible())drawUp();
		else drawDown();
	}
}
