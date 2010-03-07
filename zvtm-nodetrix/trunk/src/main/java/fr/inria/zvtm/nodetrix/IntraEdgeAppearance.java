package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VPolygon;

public class IntraEdgeAppearance extends EdgeAppearance{

	public IntraEdgeAppearance(NTEdge edge) {
		super(edge);
	}

	Glyph mainGlyph, symmetricGlyph;
//	LongPoint offset;
	
	public void updateColor(){
		mainGlyph.setColor(edge.edgeColor);
		symmetricGlyph.setColor(edge.edgeColor);
	}
	
	@Override
	protected void clearGraphics() 
	{
		if(vs == null) return;
		vs.removeGlyph(mainGlyph);
    	vs.removeGlyph(symmetricGlyph);
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
		double height = NodeTrixViz.CELL_SIZE / amount;
			
		this.animManager = VirtualSpaceManager.INSTANCE.getAnimationManager();
//    	this.offset = new LongPoint(x, y);
    	LongPoint mp = edge.tail.getMatrix().getPosition();
    	
    	long west = mp.x + edge.head.ndx - NodeTrixViz.CELL_SIZE_HALF;
		long north =  mp.y + edge.tail.wdy + NodeTrixViz.CELL_SIZE_HALF;
		long east = mp.x + edge.head.ndx + NodeTrixViz.CELL_SIZE_HALF;
		
		//non translucent glyph part
    	LongPoint[] p = new LongPoint[4];
    	p[0] = new LongPoint(east, north - index*height);
    	p[1] = new LongPoint(east, north - (index+1)*height );
    	p[2] = new LongPoint(west + (index+1)*height, north - (index+1)*height );
    	p[3] = new LongPoint(west + index*height, north - index*height );
    	mainGlyph = new VPolygon(p, 0, edge.edgeColor, edge.edgeColor);
    	mainGlyph.setStrokeWidth(0);
    	mainGlyph.setSensitivity(false);
    	vs.addGlyph(mainGlyph);
    	
    	//translucent glyph part
    	p = new LongPoint[4];
    	p[0] = new LongPoint(west, north - index*height);
    	p[1] = new LongPoint(west + index*height, north - index*height);
    	p[2] = new LongPoint(west + (index+1)*height, north - (index+1)*height);
    	p[3] = new LongPoint(west, north - (index+1)*height );
    	symmetricGlyph = new VPolygon(p, 0, edge.edgeColor, edge.edgeColor, NodeTrixViz.INTRA_TRANSLUCENCY);
    	symmetricGlyph.setStrokeWidth(0);
    	symmetricGlyph.setSensitivity(false);
    	mainGlyph.stick(symmetricGlyph);
    	vs.addGlyph(symmetricGlyph);
    	this.vs = vs;
    	onTop();
	}

	@Override
	public void fade() {
		Animation a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
				mainGlyph,
				NodeTrixViz.INTRA_TRANSLUCENCY_DIMMFACTOR,
				false, 
				SlowInSlowOutInterpolator2.getInstance(), 
				null);	
		animManager.startAnimation(a, true);
    	a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
				symmetricGlyph,
				NodeTrixViz.INTRA_TRANSLUCENCY * NodeTrixViz.INTRA_TRANSLUCENCY_DIMMFACTOR,
				false, 
				SlowInSlowOutInterpolator2.getInstance(), 
				null);	
		animManager.startAnimation(a, true);
	}

	
	@Override
	public void highlight(Color c) 
	{
		symmetricGlyph.setColor(c);
	}

	@Override
	/**Updates the position according to the position of tail and head node.
	 * */
	public void updatePosition(){
		LongPoint mp = edge.tail.getMatrix().getPosition();
		mainGlyph.moveTo(mp.x + edge.head.ndx + 3, mp.y + edge.tail.wdy + 3);
    }
	
	@Override
	/**moves the edge relativly
	 * */
	public void move(long x, long y) 
	{
		 mainGlyph.move(x, y);
	}



	@Override
	public void onTop() 
	{
		if(vs == null) return;
		vs.onTop(this.mainGlyph);
		vs.onTop(this.symmetricGlyph);
	}

	@Override
	public void reset() 
	{
		symmetricGlyph.setColor(edge.edgeColor);
	}

	@Override
	public void select() {
		// TODO Auto-generated method stub
		
	}

}
