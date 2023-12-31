package fr.inria.zvtm.compositor;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.jdesktop.animation.timing.interpolation.Interpolator;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.DefaultTimingHandler;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VImage;

public class MetisseWindow extends VImage{

	private int id;
	public static Point.Double rootDimension;
	public static Point.Double rootPosition;
	private static MetisseWindow rezisingFrame;
	private Point.Double rootSize;//size of the root frame
	private Point.Double rootLocation;//pos of the root frame
	private int[] lastFrameBuffer;
	private boolean alwaysRefresh = true;
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
	private static int encoding = BufferedImage.TYPE_INT_ARGB_PRE;
	private MetisseWindow master;
	private boolean isResizing= false;
	public boolean isRescaling = false;
	private HashMap<Integer, MetisseWindow> children;
	private boolean onWall;
	//	private boolean isMoving = false;



	public MetisseWindow(boolean isroot,int window, int x, int y, int w, int h) {
		super(new BufferedImage(w, h, encoding));
		this.children = new HashMap<Integer, MetisseWindow>();
		this.id= window;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.isroot = isroot;
		this.onWall = false;
		if (isroot){
			rootDimension = new Point.Double(w, h);
			rootPosition = new Point.Double(x, y);
		}
		else if (rootDimension!=null){
			this.rootSize = rootDimension;
			this.rootLocation = rootPosition;
			this.vx = (x-rootSize.x*1./2+w*1./2);
			this.vy =(-y+rootSize.y*1./2-h*1./2);
		}
	}

	public void fbUpdate(byte[] img, int x, int y, int w, int h) {
		//	System.err.print("x");
		if(isroot)return;
		int[] imgint = new int[w*h];
		for (int i = 0; i < img.length; i=i+4) {
			imgint[i/4] = byteToRGB(img[i],img[i+1],img[i+2],img[i+3]);
		}

		if(!isResizing){
			//	System.out.print("mcx:"+x+" "+y+" "+w+" "+h);

			((BufferedImage)image).setRGB(x, y, w, h, imgint, 0, w);
			//	System.out.print("AFTER");
		}
		else {
			if((currentW==w)&&(currentH==h))
				lastFrameBuffer = imgint;
		}
		if(alwaysRefresh){
			if (isResizing && lastFrameBuffer!= null && currentH*currentW== lastFrameBuffer.length) {
				this.width = currentW;
				this.height = currentH;
				this.vw = this.width*scaleFactor;
				this.vh = this.height*scaleFactor;
				this.vx = (this.x-rootSize.x*1./2+this.width*1./2);//*scaleFactor;
				this.vy =(-this.y+rootSize.y*1./2-this.height*1./2);//*scaleFactor;
				this.image = new BufferedImage((int)currentW, (int)currentH, encoding);		
		//		System.out.print("complet");
				((BufferedImage)image).setRGB((int)0, (int)0,(int) width, (int)height, lastFrameBuffer, 0, (int)width);

			}
		}
		//	System.out.println("Glyph id: " + getObjId());
		VirtualSpaceManager.INSTANCE.repaint();
	}

	private int byteToRGB(byte c,byte b,byte a,byte d) {
		return ( ( d<< 24 ) & 0xff000000 ) 
		|  ( ( c << 16 ) & 0x00ff0000 ) 
		|  ( ( b<< 8  ) & 0x0000ff00 ) 
		|  (   a        & 0x000000ff );

	}

	public void configure(int x, int y, int w, int h) {
		if(isroot)return;
		if(isResizing || this.width!= w|| this.height!=h){
			isResizing = true;
			MetisseWindow.rezisingFrame = this;
			currentH = h;
			currentW = w;

		}
		else{
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
			this.move(dx,dy);//moves the glyph
		}

		this.x = x;
		this.y = y;

	}
	
	private void fatherOf(MetisseWindow m){
		if(children.containsKey(m))return;
		children.put(m.getId(), m);
	}

	public void endResize(){
		if(!isResizing)return;
		this.width = currentW;
		this.height = currentH;
		this.vw = this.width*scaleFactor;
		this.vh = this.height*scaleFactor;
		this.vx = (x-rootSize.x*1./2+this.width*1./2);//*scaleFactor;
		this.vy =(-y+rootSize.y*1./2-this.height*1./2);//*scaleFactor;
		this.image = new BufferedImage((int)currentW, (int)currentH,encoding);		
		isResizing = false;
		MetisseWindow.rezisingFrame= null;
		if (width*height == lastFrameBuffer.length) ((BufferedImage)image).setRGB((int)0, (int)0,(int) width, (int)height, lastFrameBuffer, 0, (int)width);
	}


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

	public double getX_offset(){
		return x_offset;
	}
	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public double getY_offset(){
		return y_offset;
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public static MetisseWindow getRezisingFrame() {
		return rezisingFrame;
	}

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

		VirtualSpaceManager.INSTANCE.repaint();
	}

	public MetisseWindow getMaster(){
		return master;
	}

	public double getYparentOffset() {
		return yParentOffset;
	}

	public double getXparentOffset() {
		return xParentOffset;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

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

	public void setX_offset(double xOffset) {
		x_offset = xOffset;
	}

	public void setY_offset(double yOffset) {
		y_offset = yOffset;
	}

	public void setXparentOffset(double xParentOffset) {
		this.xParentOffset = xParentOffset;
	}

	public void setYparentOffset(double yParentOffset) {
		this.yParentOffset = yParentOffset;
	}

	public void setParentScaleFactor(double parentScaleFactor) {
		this.parentScaleFactor = parentScaleFactor;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public void setIsroot(boolean isroot) {
		this.isroot = isroot;
	}

	public void setResizing(boolean isResizing) {
		this.isResizing = isResizing;
	}

	public void setRescaling(boolean isRescaling) {
		this.isRescaling = isRescaling;
	}

	public double getParentScaleFactor() {
		return parentScaleFactor;
	}

	public HashMap<Integer, MetisseWindow> getChildren() {
		return children;
	}

	public boolean isRoot() {
		return isroot;
	}

	public boolean isResizing() {
		return isResizing;
	}

	public boolean isRescaling() {
		return isRescaling;
	}

	public void setRootSize(Point.Double rootSize) {
		this.rootSize = rootSize;
	}

	public void setRootLocation(Point.Double rootLocation) {
		this.rootLocation = rootLocation;
	}

	public Point.Double getRootSize() {
		return rootSize;
	}

	public Point.Double getRootLocation() {
		return rootLocation;
	}
	
	@Override
	public boolean coordInside(int jpx, int jpy, int camIndex, double cvx,double cvy) {
 		return((cvx-vx)*(cvx-vx)<=vw*vw/4		&&      (cvy-vy)*(cvy-vy)<=vh*vh/4);
	}
	
	public void setOnWall(boolean b){
		for (MetisseWindow c : children.values()) {
			c.setOnWall(b);
		}
		onWall = b;
	}
	
	public boolean isOnWall(){
		return onWall;
	}
}
