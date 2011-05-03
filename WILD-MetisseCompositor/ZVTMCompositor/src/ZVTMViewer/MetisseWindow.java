package ZVTMViewer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import Protocol.rfbAgent;

import kernel.Main;

import fr.inria.zvtm.glyphs.VImage;

public class MetisseWindow extends VImage{

	public int windowNumber;
	public static MetisseWindow rootFrame;
	public static MetisseWindow beingResized;
	private int[] lastFrameBuffer;
	private boolean alwaysRefresh = true;
	boolean beingRescaled = false;
	double last_vw;
	double last_vh;

	public double getY() {
		return y;
	}

	public double getX() {
		return x;
	}




	private double x;//coordinates in the x server
	private double y;
	private double height;//size in the x server
	private double width;
	private boolean isroot;
	private boolean resizing= false;
	private double currentW;
	private double currentH;



	public MetisseWindow(boolean isroot,int window, int x, int y, int w, int h) {
		super(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
		this.windowNumber = window;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
 		this.last_vh = this.vh;
 		this.last_vw = this.vw;
		this.isroot = isroot;
		if (isroot)MetisseWindow.rootFrame = this;
		else{
			this.vx = (x-MetisseWindow.rootFrame.width*1./2+w*1./2);//*scaleFactor;
			this.vy =(-y+MetisseWindow.rootFrame.height*1./2-h*1./2);//*scaleFactor;
		}
	}

	public void fbUpdate(byte[] img, int x, int y, int w, int h) {
		if(isroot)return;
		int[] imgint = new int[w*h];
		for (int i = 0; i < img.length; i=i+4) {
			imgint[i/4] = byteToRGB(img[i],img[i+1],img[i+2],img[i+3]);
		}

		if(!resizing)((BufferedImage)image).setRGB(x, y, w, h, imgint, 0, w);
		else {
			if((currentW==w)&&(currentH==h))
			lastFrameBuffer = imgint;
		}
		if(alwaysRefresh){
		if (resizing && lastFrameBuffer!= null && currentH*currentW== lastFrameBuffer.length) {
			this.width = currentW;
			this.height = currentH;
	 		this.vw = this.width*scaleFactor;
	 		this.vh = this.height*scaleFactor;
	 		this.last_vh = this.vh;
	 		this.last_vw = this.vw;
			this.vx = (this.x-MetisseWindow.rootFrame.width*1./2+this.width*1./2);//*scaleFactor;
			this.vy =(-this.y+MetisseWindow.rootFrame.height*1./2-this.height*1./2);//*scaleFactor;
			this.image = new BufferedImage((int)currentW, (int)currentH, BufferedImage.TYPE_INT_ARGB);		
			
			((BufferedImage)image).setRGB((int)0, (int)0,(int) width, (int)height, lastFrameBuffer, 0, (int)width);
		}
		}

	}

	private int byteToRGB(byte a,byte b,byte c,byte d) {
		return ( ( d<< 24 ) & 0xff000000 ) 
		|  ( ( c << 16 ) & 0x00ff0000 ) 
		|  ( ( b<< 8  ) & 0x0000ff00 ) 
		|  (   a        & 0x000000ff );

	}

	public void configure(int x, int y, int w, int h) {
		if(isroot)return;
		if(resizing || this.width!= w|| this.height!=h){
			resizing = true;
			MetisseWindow.beingResized = this;
			currentH = h;
			currentW = w;
		
		}
//		System.out.println(x);
		double dx = (x-this.x);
		double dy = -(y-this.y);
		double sf = scaleFactor;
		double dw = this.width-w;
		double dh = this.height-h;
		this.move(dx,dy);//moves the glyph
	//	this.move(0.5*dw*(1-sf),0.5*dh*(1-sf));
		this.x = x;//+ 0.5*dw*(1-sf);
		this.y = y;//+ 0.5*dh*(1-sf);

	}

	public void endResize(){
		if(!resizing)return;
		this.width = currentW;
		this.height = currentH;
		this.vw = this.width*scaleFactor;
		this.vh = this.height*scaleFactor;
		this.last_vh = this.vh;
 		this.last_vw = this.vw;
		this.vx = (x-MetisseWindow.rootFrame.width*1./2+this.width*1./2);//*scaleFactor;
		this.vy =(-y+MetisseWindow.rootFrame.height*1./2-this.height*1./2);//*scaleFactor;
		this.image = new BufferedImage((int)currentW, (int)currentH, BufferedImage.TYPE_INT_ARGB);		
		resizing = false;
		MetisseWindow.beingResized = null;
		if (width*height == lastFrameBuffer.length) ((BufferedImage)image).setRGB((int)0, (int)0,(int) width, (int)height, lastFrameBuffer, 0, (int)width);
	}

	
	public void endRescale(){
		if(isroot)return;
		this.last_vh = this.vh;
 		this.last_vw = this.vw;
 		this.beingRescaled = false;
 		
	}

	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){

		boolean paintBorder = true;//this variable is a private one inherited from ClosestShape. 
		//	One should remove this Line as soon as the file MetisseWindow.java is placed in the package glyphs

		double wFactor = 1;
		double hFactor = 1;

		if(resizing){
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


	private void drawRect(Graphics2D g,int i, int dx, int dy,double wFactor, double hFactor){
		g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,(int)((2*pc[i].cw-1)*wFactor),(int)((2*pc[i].ch-1)*hFactor));
	}

	private void drawImg(Graphics2D g,double wFactor, double hFactor){
		g.drawImage(image,AffineTransform.getScaleInstance(trueCoef*wFactor,trueCoef*hFactor),null);
	}

	public void setScaleFactor(double d) {
		if(isroot)return;
		if (d ==0)return;
		scaleFactor = d;
		vw = scaleFactor*image.getWidth(null);
		vh = scaleFactor*image.getHeight(null);
	}
}
