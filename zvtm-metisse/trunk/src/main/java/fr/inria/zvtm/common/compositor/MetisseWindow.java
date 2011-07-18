package fr.inria.zvtm.common.compositor;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;

import org.jdesktop.animation.timing.interpolation.Interpolator;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.DefaultTimingHandler;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VImage;

/**
 * This is the main structure for handling Metisse windows in ZVTM. 
 * @author Julien Altieri
 *
 */
public class MetisseWindow extends VImage{

	private int id;
	/**
	 * This static field is used for calibration of resizing in Metisse relatively to the upper left corner of a window, because we need to know the dimensions of the root frame.
	 */
	public static Point.Double rootDimension;
	/**
	 * This static field is used for calibration of resizing in Metisse relatively to the upper left corner of a window, because we need to know the dimensions of the root frame.
	 */
	public static Point.Double rootPosition;
	private static MetisseWindow rezisingFrame;
	private Point.Double rootSize;//size of the root frame
	private byte[] lastFrameBuffer= new byte[1];
	private boolean alwaysRefresh = true;//refresh during resizing
	private double x_offset;//ZVTM position
	private double y_offset;
	private double xParentOffset;//ZVTM position
	private double yParentOffset;
	private double parentScaleFactor = 1;
	private double x;//coordinates in the x server
	private double y;
	private double height;//size in the x server
	private double width;
	private boolean isroot = false;
	private double currentW;
	private double currentH;
	private static int encoding = BufferedImage.TYPE_4BYTE_ABGR;
	private MetisseWindow master;
	private boolean isResizing= false;
	private boolean isRescaling = false;//used by the 
	private HashMap<Integer, MetisseWindow> children;
	private boolean published = false;//on wall
	private boolean shared = true;//should we transmit events from this window on the wall?



	/**
	 * 
	 * @param isroot
	 * @param window the id of the window
	 * @param x x position in the Metisse server
	 * @param y y position in the Metisse server
	 * @param w width in the Metisse server (also the size of the raster)
	 * @param h height in the Metisse server (also the size of the raster)
	 */
	public MetisseWindow(boolean isroot,int window, int x, int y, int w, int h) {
		super(new BufferedImage(w, h, encoding));
		this.children = new HashMap<Integer, MetisseWindow>();
		this.id= window;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.isroot = isroot;
		if (isroot){
			shared = false;
			rootDimension = new Point.Double(w, h);
			rootPosition = new Point.Double(x, y);
		}
		else if (rootDimension!=null){
			this.rootSize = rootDimension;
			this.vx = (x-rootSize.x*1./2+w*1./2);
			this.vy =(-y+rootSize.y*1./2-h*1./2);
		}
	}

	/**
	 * Updates the raster of the window
	 * @param img the byte array containing graphic instructions (4 bytes encoding)
	 * @param x the x position where the update rectangle should start
	 * @param y the y position where the update rectangle should start
	 * @param w the width of the updated rectangle 
	 * @param h the height of the updated rectangle 
	 */
	public void fbUpdate(byte[] img, int x, int y, int w, int h) {
		if(isroot)return;
		if(!isResizing){
			patch(img, x, y, w, h);
		}
		else {
			if((currentW==w)&&(currentH==h)){
				lastFrameBuffer = img;
			}
			if(alwaysRefresh){
				this.width = currentW;
				this.height = currentH;
				this.vw = this.width*scaleFactor;
				this.vh = this.height*scaleFactor;
				this.vx = (this.x-rootSize.x*1./2+this.width*1./2);
				this.vy =(-this.y+rootSize.y*1./2-this.height*1./2);
				this.image = new BufferedImage((int)currentW, (int)currentH, encoding);		
				patch(img, x, y, w, h);
			}
		}

		VirtualSpaceManager.INSTANCE.repaint();
	}

	private void patch(byte[] img, int x, int y, int w, int h){
		byte[] raster = ((DataBufferByte)((BufferedImage) image).getRaster().getDataBuffer()).getData();
		int W = ((BufferedImage) image).getWidth();
		for (int i = 0; i < img.length; i++) {
			if(4*(i/4)+(3-i%4)+4*(i/(4*w))*(W-w)+4*(W*y+x)<raster.length)
				raster[4*(i/4)+(3-i%4)+4*(i/(4*w))*(W-w)+4*(W*y+x)] = img[i];
		}
	}


	/**
	 * Updates the size and the position of the current frame (in metisse, but also acts on the zvtm object)
	 * @param x the new window's x
	 * @param y the new window's y
	 * @param w the new width
	 * @param h the new height
	 */
	public void configure(int x, int y, int w, int h) {
		if(isroot)return;
		if(this.width!= w|| this.height!=h){
			isResizing = true;
			MetisseWindow.rezisingFrame = this;
			currentH = h;
			currentW = w;
		}
		else{
			if(isRescaling)endResize();
			for (MetisseWindow m : children.values()) {
				m.move((x-this.x)*scaleFactor, -(y-this.y)*scaleFactor);
			}
		}
		double sf= 1;
		if (master!=null){
			sf = parentScaleFactor;
		}
		else{
			double dx = (x-this.x)*sf;
			double dy = -(y-this.y)*sf;
			this.move(dx,dy);
		}

		this.x = x;
		this.y = y;

	}

	private void fatherOf(MetisseWindow m){
		if(children.containsKey(m))return;
		children.put(m.getId(), m);
	}

	/**
	 * This method should be called as soon as the resize process (in Metisse, so implies the raster) ends.
	 */
	public void endResize(){
		if(!isResizing)return;
		this.width = currentW;
		this.height = currentH;
		this.vw = this.width*scaleFactor;
		this.vh = this.height*scaleFactor;
		this.vx = (x-rootSize.x*1./2+this.width*1./2);
		this.vy =(-y+rootSize.y*1./2-this.height*1./2);
		this.image = new BufferedImage((int)currentW, (int)currentH,encoding);		
		isResizing = false;
		MetisseWindow.rezisingFrame= null;
		if (4*width*height == lastFrameBuffer.length) 
			patch(lastFrameBuffer, 0, 0, (int)width,(int)height);
	}

	/**
	 * Only used for zvtm purposes 
	 */
	public void endRescale(){
		if(isroot)return;
		this.isRescaling = false;
		VirtualSpaceManager.INSTANCE.repaint();
	}


	private void drawRect(Graphics2D g,int i, int dx, int dy,double wFactor, double hFactor){
		g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,(int)((2*pc[i].cw-1)*wFactor),(int)((2*pc[i].ch-1)*hFactor));
	}

	private void drawImg(Graphics2D g,double wFactor, double hFactor){
		g.drawImage(image,AffineTransform.getScaleInstance(trueCoef*wFactor,trueCoef*hFactor),null);
	}

	/**
	 * Deals with the zvtm factor only. The size of raster of the {@link MetisseWindow} will not change, but the way it is drawn will.
	 * @param d the new scale factor
	 */
	public void setScaleFactor(double d) {
		if(isroot)return;
		if (d ==0)return;
		vw = d*image.getWidth(null);
		vh = d*image.getHeight(null);


		if (master!=null){
			this.vx = master.vx-master.width*parentScaleFactor/2+parentScaleFactor*(x- master.x)+width*scaleFactor/2 + x_offset;
			this.vy = master.vy-(-master.height*parentScaleFactor/2+parentScaleFactor*(y- master.y)+height*scaleFactor/2)+y_offset;
		}

		for (MetisseWindow m : children.values()) {
			m.setScaleFactor(m.getScaleFactor()*d/scaleFactor);

		}
		scaleFactor = d;
		VirtualSpaceManager.INSTANCE.repaint();
	}

	/**
	 * Relatively moves the {@link MetisseWindow} of (dx,dy) (zvtm position only) 
	 * @param dx
	 * @param dy
	 */
	public void moveGlyphOf(double dx, double dy) {
		if(isroot)return;
		x_offset+=dx;
		y_offset+=dy;
		this.move(dx, dy);
	}

	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){

		boolean paintBorder = true;//this variable is a private one inherited from ClosestShape. 
		//	One should remove this Line as soon as the file MetisseWindow.java is placed in the package glyphs
		double wFactor = 1;
		double hFactor = 1;
		//		interpolationMethod = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

		if(isResizing){
			wFactor = currentW/this.width;
			hFactor = currentH/this.height;
		}

		if (alphaC != null && alphaC.getAlpha()==0){return;}
		if ((pc[i].cw>=1) || (pc[i].ch>=1)){

			if (zoomSensitive){
				trueCoef = scaleFactor*coef;
			}
			else{
				trueCoef = scaleFactor;
			}
			//a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
			if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
			// translate
			at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
			g.setTransform(at);
			// rescale and draw
			if (alphaC != null){
				// translucent
				g.setComposite(alphaC);
				if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
					g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
					drawImg(g, wFactor, hFactor);
					g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				}
				else {
					drawImg(g, wFactor, hFactor);
				}
				g.setTransform(stdT);

				if (paintBorder){
					g.setColor(borderColor);
					if (stroke!=null) {
						g.setStroke(stroke);
						drawRect(g, i, dx, dy, wFactor, hFactor) ;
						g.setStroke(stdS);
					}
					else {
						drawRect(g, i, dx, dy, wFactor, hFactor) ; 
					}
				}
				g.setComposite(acO);
			}
			else {
				// opaque
				if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
					g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
					drawImg(g, wFactor, hFactor);
					g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				}
				else {
					drawImg(g, wFactor, hFactor);
				}
				g.setTransform(stdT);
				if (paintBorder){
					g.setColor(borderColor);
					if (stroke!=null) {
						g.setStroke(stroke);
						drawRect(g, i, dx, dy, wFactor, hFactor) ;                
						g.setStroke(stdS);
					}
					else {
						drawRect(g, i, dx, dy, wFactor, hFactor) ;
					}
				}
			}
		}
		else {
			g.setColor(this.borderColor);
			g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		}
	}

	public int getId() {
		return id;
	}

	/**
	 * @return the difference between zvtm x position and metisse x position
	 */
	public double getX_offset(){
		return x_offset;
	}
	
	/**
	 * @return the Metisse height (height of the raster)
	 */
	public int getH() {
		return (int) height;
	}


	/**
	 * @return the Metisse width (width of the raster)
	 */
	public int getW() {
		return (int) width;
	}


	/**
	 * @return the difference between zvtm x position and metisse x position
	 */
	public double getY_offset(){
		return y_offset;
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * @return The last resized frame (or the current one if there is one)
	 */
	public static MetisseWindow getRezisingFrame() {
		return rezisingFrame;
	}

	/**
	 * This method handles the inheritance of the properties from the parent if there is one. For example, it includes the relative calculus of scale factor from the parent and the handling of relative positioning (menus). It must be called as soon as we get the information of who is the parent.
	 * @param w the parent {@link MetisseWindow}
	 */
	public void refreshMaster(MetisseWindow w){
		if(w== null)return;
		if(isroot)return;
		if(w==this)return;
		master = w;
		master.fatherOf(this);
		x_offset*= w.getScaleFactor()/parentScaleFactor;
		y_offset*= w.getScaleFactor()/parentScaleFactor;
		double d = (scaleFactor*w.getScaleFactor()/parentScaleFactor);

		if(isroot)return;
		if (d ==0)return;
		vw = d*image.getWidth(null);
		vh = d*image.getHeight(null);
		scaleFactor = d;

		parentScaleFactor = w.getScaleFactor();

		this.vx = w.vx-w.width*parentScaleFactor/2+parentScaleFactor*(x- w.x)+width*scaleFactor/2 + x_offset;
		this.vy = w.vy-(-w.height*parentScaleFactor/2+parentScaleFactor*(y- w.y)+height*scaleFactor/2)+y_offset;

		xParentOffset = w.x_offset;
		yParentOffset = w.y_offset;

		published = master.published;
		VirtualSpaceManager.INSTANCE.repaint();
	}

	/**
	 * @return The parent frame of this window if it exists, null otherwise.
	 */
	public MetisseWindow getMaster(){
		return master;
	}

	/**
	 * @return the result of this.getMaster().getY_offset()
	 * @see {@link MetisseWindow#getMaster()}, {@link MetisseWindow#getY_offset()}
	 */
	public double getYparentOffset() {
		return yParentOffset;
	}

	/**
	 * @return the result of this.getMaster().getX_offset()
	 * @see {@link MetisseWindow#getMaster()}, {@link MetisseWindow#getX_offset()}
	 */
	public double getXparentOffset() {
		return xParentOffset;
	}

	/**
	 * @return The x position (in the Metisse server)
	 */
	public int getX() {
		return (int)this.x;
	}

	/**
	 * @return The y position (in the Metisse server)
	 */
	public int getY() {
		return (int)this.y;
	}

	/**
	 * This animates and reset all the zvtm offsets and scale factor. The result is exactly the replication of the Metisse server's rendered window. Resetting will also trigger the same for all the children.
	 */
	public void resetTransform(){
		double dx = vx-x_offset;
		double dy = vy-y_offset;
		if(master!=null)parentScaleFactor = master.scaleFactor;
		if (isroot)return;
		if (master!=null&&!master.isroot){
			dx = master.vx-master.width*parentScaleFactor/2+parentScaleFactor*(x- master.x)+width*parentScaleFactor/2;
			dy = master.vy-(-master.height*parentScaleFactor/2+parentScaleFactor*(y- master.y)+height*parentScaleFactor/2);
		}


		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createGlyphTranslation(200, this, new Point2D.Double(dx,dy), false, SlowInSlowOutInterpolator.getInstance(),null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);


		Animation scale = 
			createGlyphScaleAnim(200, this, parentScaleFactor, false, SlowInSlowOutInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(scale, false);

		for (MetisseWindow m : children.values()) {
			m.resetTransform(dx,dy,1);
		}


		x_offset = 0;
		y_offset = 0;

	}

	private void resetTransform(double masterX, double masterY, double sf) {
		double dx = vx-x_offset;
		double dy = vy-y_offset;
		if (isroot)return;
		if (master!=null&&!master.isroot){
			dx = masterX-master.width*sf/2+sf*(x- master.x)+width*sf/2;
			dy = masterY-(-master.height*sf/2+sf*(y- master.y)+height*sf/2);
		}

		Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
			createGlyphTranslation(200, this, new Point2D.Double(dx,dy), false, SlowInSlowOutInterpolator.getInstance(),null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);

		x_offset = 0;
		y_offset = 0;

		Animation scale = 
			createGlyphScaleAnim(200, this, sf, false, SlowInSlowOutInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(scale, false);

	}

	private Animation createGlyphScaleAnim(final int duration, final MetisseWindow mw,
			final double data, final boolean relative,
			final Interpolator interpolator,
			final EndAction endAction){
		return VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createAnimation(duration, 1f, Animation.RepeatBehavior.LOOP,
				mw,
				Animation.Dimension.SIZE,
				new DefaultTimingHandler(){
			private double startScale = java.lang.Double.MIN_VALUE;
			private double endScale = java.lang.Double.MIN_VALUE;

			@Override
			public void begin(Object subject, Animation.Dimension dim){
				startScale = mw.getScaleFactor();
				endScale = relative? mw.getScaleFactor() + data : data;

				//throw if this animation would cause the size to become negative
				//XXX it might be better to silently clip size to zero 
				if(endScale < 0f){
					throw new IllegalStateException("Cannot animate a Glyph scale to a negative value");
				}
			}

			@Override
			public void end(Object subject, Animation.Dimension dim){
				if(null != endAction){
					endAction.execute(subject, dim);
				}
			}

			@Override
			public void timingEvent(float fraction, 
					Object subject, Animation.Dimension dim){
				double d = (startScale + fraction*(endScale - startScale));

				if(mw.isroot)return;
				if (d ==0)return;
				mw.vw = d*mw.image.getWidth(null);
				mw.vh = d*mw.image.getHeight(null);
				mw.scaleFactor = d;

			}
		},
		interpolator);
	}

	
	/**
	 * Use this method to set whether or not this window should be on the wall (as well as it's children).
	 * @param published
	 */
	public void setPublished(boolean published) {
		for (MetisseWindow m : children.values()) {
			m.setPublished(published);
		}
		this.published = published;
	}

	/**
	 * @return True if the window is published on the wall, false in other case.
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * 
	 * @return the list of the attached {@link MetisseWindow} (menus for example)
	 */
	public HashMap<Integer, MetisseWindow> getChildren() {
		return children;
	}

	public boolean isRoot() {
		return isroot;
	}

	/**
	 * If the resizing is true, not all the updates are taken into account.
	 * @return the current resizing state
	 */
	public boolean isResizing() {
		return isResizing;
	}

	/**
	 * Used for calibration, not crucial.
	 * @param rootSize
	 */
	public void setRootSize(Point.Double rootSize) {
		this.rootSize = rootSize;
	}


	//it is necessary we redefine it, since the test is usually made on projected coordinates whereas we need to make it on virtual coordinates here.
	@Override
	public boolean coordInside(int jpx, int jpy, int camIndex, double cvx,double cvy) {
		return((cvx-vx)*(cvx-vx)<=vw*vw/4		&&      (cvy-vy)*(cvy-vy)<=vh*vh/4);
	}

	/**
	 * 
	 * @return The current complete raster of the {@link MetisseWindow}
	 */
	public byte[] getRaster() {
		byte[] ori =  ((DataBufferByte)((BufferedImage) image).getRaster().getDataBuffer()).getData();
		byte[] res = new byte[ori.length];
		for (int i = 0; i < res.length; i=i+4) {
			res[i] = ori[i+3];
			res[i+1] = ori[i+2];
			res[i+2] = ori[i+1];
			res[i+3] = ori[i+0];
		}
		return res;
	}

	/**
	 * Set this to true to allow external events to bounce to the owner of this frame.
	 * @param b
	 */
	public void setShared(boolean b) {
		if(isroot)return;
		shared   = b;
	}

	/**
	 * 
	 * @return whether or not external events will bounce to the owner of this frame.
	 */
	public boolean isShared() {
		return shared;
	}
	
	/**
	 * Does nothing :)
	 * @param isResizing
	 */
	public void setResizing(boolean isResizing){
		
	}
	
	public boolean isRescaling(){
		return this.isRescaling;
	}
	
	public Double getRootLocation(){
		return MetisseWindow.rootPosition;
	}
	
	public Double getRootSize(){
		return MetisseWindow.rootDimension;
	}
	
	public void setX_offset(double x){
		this.x_offset = x;
	}
	
	public void setY_offset(double y){
		this.y_offset = y;
	}
	
	public void setXparentOffset(double x){
		this.xParentOffset = x;
	}
	
	public void setYparentOffset(double y){
		this.yParentOffset = y;
	}
	
	public void setParentScaleFactor(double sf){
		this.parentScaleFactor = sf;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public void setIsroot(boolean b){
		this.isroot = b;
	}
	
	public void setRescaling(boolean b){
		this.isRescaling = b;
	}
	
	public void setRootLocation(Point2D.Double p){
		MetisseWindow.rootPosition = p;
	}
	
	public double getParentScaleFactor(){
		return parentScaleFactor;
	}
	
	
}
