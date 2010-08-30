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
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;

public class IntraEdgeAppearance extends EdgeAppearance{


	private VPolygon gMain, gSymmetry;
	private DPath gLeftFrameFragment, gRightFrameFragment;
	private DPath gLowerFrameFragment, gUpperFrameFragment;
	private VRectangle gSensitive;
	
	public IntraEdgeAppearance(NTEdge edge) {
		super(edge);
	}
	
	public void updateColor(){
		gMain.setColor(edge.edgeColor);
		if(gSymmetry != null)gSymmetry.setColor(edge.edgeColor);
	}
	
	@Override
	protected void clearGraphics() 
	{
		if(vs == null) return;
		vs.removeGlyph(gMain);
    	if(gSymmetry != null) vs.removeGlyph(gSymmetry);
     	vs.removeGlyph(gLeftFrameFragment);
     	vs.removeGlyph(gRightFrameFragment);
     	if(gUpperFrameFragment != null) vs.removeGlyph(gUpperFrameFragment);
     	if(gLowerFrameFragment != null) vs.removeGlyph(gLowerFrameFragment);
     	vs.removeGlyph(gSensitive);
   }
	
	@Override
	public void createGraphics(VirtualSpace vs){
		this.vs = vs;
		createGraphics();
	}
	
	@Override
//	public void createGraphics(long height, long y, long x, long index, VirtualSpace vs)  {
	public void createGraphics() {
		if(vs == null) return;
		double height = (NodeTrixViz.CELL_SIZE-3) / amount;
			
		this.animManager = VirtualSpaceManager.INSTANCE.getAnimationManager();
//    	this.offset = new Point2D.Double(x, y);
    	Point2D.Double mp = edge.tail.getMatrix().getPosition();
    	
    	double west = mp.x + edge.head.ndx - NodeTrixViz.CELL_SIZE_HALF +2;
		double north =  mp.y + edge.tail.wdy + NodeTrixViz.CELL_SIZE_HALF;
		double east = mp.x + edge.head.ndx + NodeTrixViz.CELL_SIZE_HALF -1;
		
		//non translucent glyph part
    	Point2D.Double[] p = new Point2D.Double[4];
    	p[0] = new Point2D.Double(east, (north-2) - index*height);
    	p[1] = new Point2D.Double(east, (north-2) - (index+1)*height);
    	p[2] = new Point2D.Double((west-1) + (index+1)*height, (north-2) - (index+1)*height );
    	p[3] = new Point2D.Double(west + index*height, (north-2) - index*height);
    	gMain = new VPolygon(p, 0, edge.edgeColor, edge.edgeColor);
    	gMain.setDrawBorder(false);
    	gMain.setSensitivity(false);
    	vs.addGlyph(gMain);
    	
    	//SYMMETRIC GLYPH
    	System.out.println("[INTAR_EDGE_APP] symmetric " + edge.symmetric);
    	if(edge.symmetric)
    	{
    		p = new Point2D.Double[4];
    		p[0] = new Point2D.Double(west, (north-2) - index*height );
    		p[1] = new Point2D.Double(west + index*height, (north-2) - index*height );
    		p[2] = new Point2D.Double((west-1) + (index+1)*height, (north-2) - (index+1)*height );
    		p[3] = new Point2D.Double(west, (north-2) - (index+1)*height);
    		gSymmetry = new VPolygon(p, 0, edge.edgeColor, edge.edgeColor);
    		gSymmetry.setDrawBorder(false);
    		gSymmetry.setSensitivity(false);
    		gMain.stick(gSymmetry);
    		vs.addGlyph(gSymmetry);
    	}
    	
    	//FRAMEGLYPHS
    	gLeftFrameFragment = new DPath(west-2, (long) (north - index*height)-2, 0, edge.edgeColor);
    	gLeftFrameFragment.addSegment(west-2, (long) (north - (index+1)*height)-2, true);
    	gMain.stick(gLeftFrameFragment);
    	vs.addGlyph(gLeftFrameFragment);
    	gRightFrameFragment = new DPath(east+1, (long) (north - index*height)-2, 0, edge.edgeColor);
    	gRightFrameFragment.addSegment(east+1, (long) (north - (index+1)*height)-2, true);
    	gMain.stick(gRightFrameFragment);
    	vs.addGlyph(gRightFrameFragment);

    	if(index == 0){
    		gUpperFrameFragment = new DPath(west-2, north-2, 0, edge.edgeColor);
        	gUpperFrameFragment.addSegment(west-2, north, true);
        	gUpperFrameFragment.addSegment(east+1, north, true);
        	gUpperFrameFragment.addSegment(east+1, north-2, true);
        	gMain.stick(gUpperFrameFragment);
        	vs.addGlyph(gUpperFrameFragment);
    	}
    	if(index == amount-1){
    		gLowerFrameFragment = new DPath(west-2, (long) ((north) - (index+1)*height), 0, edge.edgeColor);
        	gLowerFrameFragment.addSegment(west-2, (long) ((north-3) - (index+1)*height), true);
        	gLowerFrameFragment.addSegment(east+1, (long) ((north-3) - (index+1)*height), true);
        	gLowerFrameFragment.addSegment(east+1, (long) ((north) - (index+1)*height), true);
        	gMain.stick(gLowerFrameFragment);
        	vs.addGlyph(gLowerFrameFragment);
    	}
    	
    	//SENSITIE RECTANGLE
    	gSensitive = new VRectangle(mp.x + edge.head.ndx, (long)((north-2) - (index+.5)*height),0 ,NodeTrixViz.CELL_SIZE_HALF, (long) height/2 - 1, Color.black);
    	gSensitive.setTranslucencyValue(.2f);
    	gSensitive.setVisible(true);
    	gSensitive.setOwner(edge);
    	gMain.stick(gSensitive);
    	vs.addGlyph(gSensitive);
    	
    	onTop();
	}

	@Override
	public void fade() {
//		Animation a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
//				mainGlyph,
//				NodeTrixViz.INTRA_TRANSLUCENCY_DIMMFACTOR,
//				false, 
//				SlowInSlowOutInterpolator2.getInstance(), 
//				null);	
//		animManager.startAnimation(a, true);
//    	a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
//				symmetricGlyph,
//				NodeTrixViz.INTRA_TRANSLUCENCY * NodeTrixViz.INTRA_TRANSLUCENCY_DIMMFACTOR,
//				false, 
//				SlowInSlowOutInterpolator2.getInstance(), 
//				null);	
//		animManager.startAnimation(a, true);
	}

	

	@Override
	/**Updates the position according to the position of tail and head node.
	 * */
	public void updatePosition(){
		Point2D.Double mp = edge.tail.getMatrix().getPosition();
		clearGraphics();
		createGraphics();
//		mainGlyph.moveTo(mp.x + edge.head.ndx, mp.y + edge.tail.wdy);
//		if(symmetricGlyph != null) symmetricGlyph.moveTo(mp.x + edge.head.ndx + 3, mp.y + edge.tail.wdy + 3);
    }
	
	@Override
	/**moves the edge relativly
	 * */
	public void move(long x, long y) 
	{
		 gMain.move(x, y);
	}


	@Override
	public void onTop() 
	{
		if(vs == null) return;
		vs.onTop(this.gMain);
		if(gSymmetry != null) vs.onTop(this.gSymmetry);
		vs.onTop(this.gLeftFrameFragment);
		vs.onTop(this.gRightFrameFragment);
	 	if(gSymmetry != null) vs.onTop(gSymmetry);
     	if(gLeftFrameFragment != null) vs.onTop(gLeftFrameFragment);
     	if(gRightFrameFragment != null) vs.onTop(gRightFrameFragment);
     	if(gUpperFrameFragment != null) vs.onTop(gUpperFrameFragment);
     	if(gLowerFrameFragment != null) vs.onTop(gLowerFrameFragment);
     	vs.onTop(gSensitive);
   }

	@Override
	public void highlight(Color c) 
	{
//		gLeftFrameFragment.setColor(c);
//		gRightFrameFragment.setColor(c);
//     	if(gUpperFrameFragment != null) gUpperFrameFragment.setColor(c);
//     	if(gLowerFrameFragment != null) gLowerFrameFragment.setColor(c);
	}
	@Override
	public void reset() 
	{
//		gLeftFrameFragment.setColor(edge.edgeColor);
//		gRightFrameFragment.setColor(edge.edgeColor);
//     	if(gUpperFrameFragment != null) gUpperFrameFragment.setColor(edge.edgeColor);
//     	if(gLowerFrameFragment != null) gLowerFrameFragment.setColor(edge.edgeColor);

	}

	@Override
	public void select() {
		// TODO Auto-generated method stub
		
	}

}
