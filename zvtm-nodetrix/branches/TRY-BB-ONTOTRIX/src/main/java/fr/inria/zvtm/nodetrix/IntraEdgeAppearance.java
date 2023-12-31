/*   AUTHOR :           Benjamin Bach (bbach@lri.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

public class IntraEdgeAppearance extends EdgeAppearance{


	private VPolygon gPrimary, gSecondary;
	private DPath gLeftFrameFragment, gRightFrameFragment;
	private DPath gLowerFrameFragment, gUpperFrameFragment;
	private VRectangle gSensitive;
	private VCircle gHighlight;
	private Glyph[] allGlyphs = new Glyph[8];
	private Point2D.Double mp;

//	private VText gLabel;
	
	public IntraEdgeAppearance(NTEdge edge) {
		super(edge);
	}
	
	public void updateColor(){
		gPrimary.setColor(edge.getColor());
		gLeftFrameFragment.setColor(edge.getColor());
		gRightFrameFragment.setColor(edge.getColor());
		if(gLowerFrameFragment != null)gLowerFrameFragment.setColor(edge.getColor());
		if(gUpperFrameFragment != null)gUpperFrameFragment.setColor(edge.getColor());
		if(gSecondary != null) gSecondary.setColor(edge.isSymmetric() ? edge.getColor() : edge.getInverseColor());
	}
	
	
	@Override
	public void createGraphics(VirtualSpace vs){
		this.vs = vs;
		createGraphics();
	}
	

	@Override
	public void createGraphics() {
		if(vs == null) return;
		double height = (NodeTrixViz.CELL_SIZE-3) / amount;
			
		this.animManager = VirtualSpaceManager.INSTANCE.getAnimationManager();
    	mp = edge.tail.getMatrix().getPosition();
    	
    	double west = mp.x + edge.head.ndx - NodeTrixViz.CELL_SIZE_HALF + 2;
		double north =  mp.y + edge.tail.wdy + NodeTrixViz.CELL_SIZE_HALF;
		double east = mp.x + edge.head.ndx + NodeTrixViz.CELL_SIZE_HALF -2;
		
		//SENSITIE RECTANGLE
		int radius = (int) NodeTrixViz.CELL_SIZE_HALF + 5;
		//this glyph is never shown.
    	gHighlight = new VCircle(mp.x + edge.head.ndx, mp.y + edge.tail.wdy, 0, 2*radius, Color.red);
    	gHighlight.setDrawBorder(false);
    	gHighlight.setVisible(false);
    	vs.addGlyph(gHighlight);
    	allGlyphs[0] = gHighlight;
		
		//LABEL
//    	gLabel = new VText(mp.x + edge.head.ndx, south - 10, 0, Color.black, edge.owner));
    	
		//MAIN GLYPH
    	Point2D.Double[] p = new Point2D.Double[4];
    	p[0] = new Point2D.Double(east, (north-2) - index*height);
    	p[1] = new Point2D.Double(east, (north-2) - (index+1)*height);
    	p[2] = new Point2D.Double((west-1) + (index+1)*height, (north-2) - (index+1)*height );
    	p[3] = new Point2D.Double(west + index*height, (north-2) - index*height);
    	gPrimary = new VPolygon(p, 0, edge.getColor(), edge.getColor());
    	gPrimary.setDrawBorder(false);
    	gPrimary.setSensitivity(false);
//    	gPrimary.stick(gHighlight);
    	gHighlight.stick(gPrimary);
    	allGlyphs[1] = gPrimary;
    	gPrimary.setOwner(edge);
    	vs.addGlyph(gPrimary);
   	
    	//SYMMETRIC GLYPH
    	if(edge.isSymmetric() || edge.hasInverse())
    	{
    		p = new Point2D.Double[4];
    		p[0] = new Point2D.Double(west, (north-2) - index*height );
    		p[1] = new Point2D.Double(west + index*height, (north-2) - index*height );
    		p[2] = new Point2D.Double((west-1) + (index+1)*height, (north-2) - (index+1)*height );
    		p[3] = new Point2D.Double(west, (north-2) - (index+1)*height);
    		

    		Color co = edge.isSymmetric() ? edge.getColor() : edge.getInverseColor();
 
    		gSecondary = new VPolygon(p, 0, co, co);
    		gSecondary.setDrawBorder(false);
    		gSecondary.setSensitivity(false);
//    		gPrimary.stick(gSecondary);
    	   	gHighlight.stick(gSecondary);
        	allGlyphs[2] = gSecondary;
     		vs.addGlyph(gSecondary);
    	}
    	
    	//FRAMEGLYPHS
    	gLeftFrameFragment = new DPath(west-2, (north - index*height)-2, 0, edge.getColor());
    	gLeftFrameFragment.addSegment(west-2, (north - (index+1)*height)-2, true);
    	gLeftFrameFragment.setSensitivity(false);
    	vs.addGlyph(gLeftFrameFragment);
      	gHighlight.stick(gLeftFrameFragment);
    	allGlyphs[3] = gLeftFrameFragment;
    	
    	gRightFrameFragment = new DPath(east+1,  (north - index*height)-2, 0, edge.getColor());
    	gRightFrameFragment.addSegment(east+1,  (north - (index+1)*height)-2, true);
    	gRightFrameFragment.setSensitivity(false);
//    	gPrimary.stick(gRightFrameFragment);
    	vs.addGlyph(gRightFrameFragment);
       	gHighlight.stick(gRightFrameFragment);
    	allGlyphs[4] = gRightFrameFragment;
 

    	if(index == 0){
    		gUpperFrameFragment = new DPath(west-2, north-2, 0, edge.getColor());
        	gUpperFrameFragment.addSegment(west-2, north, true);
        	gUpperFrameFragment.addSegment(east+1, north, true);
        	gUpperFrameFragment.addSegment(east+1, north-2, true);
        	gUpperFrameFragment.setSensitivity(false);
//        	gPrimary.stick(gUpperFrameFragment);
        	vs.addGlyph(gUpperFrameFragment);
           	gHighlight.stick(gUpperFrameFragment);
        	allGlyphs[5] = gUpperFrameFragment;        	
    	}
    	if(index == amount-1){
    		gLowerFrameFragment = new DPath(west-2,  ((north) - (index+1)*height), 0, edge.getColor());
        	gLowerFrameFragment.addSegment(west-2,  ((north-3) - (index+1)*height), true);
        	gLowerFrameFragment.addSegment(east+1,  ((north-3) - (index+1)*height), true);
        	gLowerFrameFragment.addSegment(east+1,  ((north) - (index+1)*height), true);
        	gLowerFrameFragment.setSensitivity(false);
//        	gPrimary.stick(gLowerFrameFragment);
        	vs.addGlyph(gLowerFrameFragment);
        	gHighlight.stick(gLowerFrameFragment);
        	allGlyphs[6] = gLowerFrameFragment;        
        	
    	}
    	
    	//SENSITIE RECTANGLE
    	gSensitive = new VRectangle(mp.x + edge.head.ndx, ((north-2) - (index+.5)*height),0 ,NodeTrixViz.CELL_SIZE_HALF,  height/2 - 1, Color.black);
    	gSensitive.setTranslucencyValue(.2f);
    	gSensitive.setVisible(false);
    	gSensitive.setOwner(edge);
//    	gPrimary.stick(gSensitive);
    	vs.addGlyph(gSensitive);
    	gHighlight.stick(gSensitive);
    	allGlyphs[7] = gSensitive;        
    	
    	onTop();
	}

	@Override
	protected void clearGraphics() 
	{
		if(vs == null) return;
		for(Glyph g : allGlyphs){
			if(g == null) continue;
			vs.removeGlyph(g);
		}
	}
	
	@Override
	public void fade() {
//		for(Glyph g : allGlyphs){
//			if(g == null) continue;
//			g.setVisible(false);
//			g.setSensitivity(false);
//		}	
//		this.gHighlight.setVisible(false);
		this.gPrimary.setColor(ProjectColors.INTRA_FADE);
		if(gSecondary != null) this.gSecondary.setColor(ProjectColors.INTRA_FADE);
		this.gLeftFrameFragment.setColor(ProjectColors.INTRA_FADE);
		this.gRightFrameFragment.setColor(ProjectColors.INTRA_FADE);
		if(gUpperFrameFragment != null ) gUpperFrameFragment.setColor(ProjectColors.INTRA_FADE);
		if(gUpperFrameFragment != null ) gLowerFrameFragment.setColor(ProjectColors.INTRA_FADE);
	}
	
	@Override
	public void show(){
//		for(Glyph g : allGlyphs){
//			if(g == null) continue;
//			g.setVisible(true);
//			g.setSensitivity(true);
//		}	
//		gSensitive.setVisible(false);
	
		this.gPrimary.setColor(edge.getColor());
		if(gSecondary != null) this.gSecondary.setColor(edge.getColor());
		this.gLeftFrameFragment.setColor(edge.getColor());
		this.gRightFrameFragment.setColor(edge.getColor());
		if(gUpperFrameFragment != null ) gUpperFrameFragment.setColor(edge.getColor());
		if(gUpperFrameFragment != null ) gLowerFrameFragment.setColor(edge.getColor());
	}

	@Override
	/**Updates the position according to the position of tail and head node.
	 * */
	public void updatePosition(){
		mp = edge.tail.getMatrix().getPosition();
		double x = (mp.x + edge.head.ndx) - gHighlight.vx; 
 		double y = (mp.y + edge.tail.wdy) - gHighlight.vy;
 		move(x,y);	
    }
	
	@Override
	/**moves the edge relatively
	 * */
	public void move(double x, double y) 
	{
		 gHighlight.move(x, y);
	}


	@Override
	public void onTop() 
	{
		if(vs == null) return;
		vs.onTop(gHighlight);
		vs.onTop(this.gPrimary);
		if(gSecondary != null) vs.onTop(this.gSecondary);
		vs.onTop(this.gLeftFrameFragment);
		vs.onTop(this.gRightFrameFragment);
	 	if(gSecondary != null) vs.onTop(gSecondary);
     	if(gLeftFrameFragment != null) vs.onTop(gLeftFrameFragment);
     	if(gRightFrameFragment != null) vs.onTop(gRightFrameFragment);
     	if(gUpperFrameFragment != null) vs.onTop(gUpperFrameFragment);
     	if(gLowerFrameFragment != null) vs.onTop(gLowerFrameFragment);
     	vs.onTop(gSensitive);
   }

	@Override
	public void highlight(Color c) 
	{
//		gHighlight.setVisible(true);
//		gHighlight.setColor(c);
	}
	
	@Override
	public void reset() 
	{
//		gHighlight.setVisible(false);
	}
	

	@Override
	public void select() {
		// TODO Auto-generated method stub
		
	}

}
