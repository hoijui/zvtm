package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;


public class NTIntraEdgeSet {

	private Vector<NTIntraEdge> intraEdges = new Vector<NTIntraEdge>();
	private LongPoint mp;
	Glyph gTriangle, gTriangleInverse;
	LongPoint offset;
	Glyph[] triangles;
	Glyph[] relationGlyphsInverse;
	
	public NTIntraEdgeSet()
	{
	}
	
    void createGraphics(long height, long y, long x, long noMeaning, VirtualSpace vs, Matrix m)
    {
    	this.offset = new LongPoint(x, y);
    	long h = (NodeTrixViz.CELL_SIZE) / this.intraEdges.size();
		triangles = new Glyph[intraEdges.size()];
		relationGlyphsInverse = new Glyph[intraEdges.size()];
//		long csHalf = NodeTrixViz.CELL_SIZE/2;
		
		triangles = new Glyph[2];
		int i = 0;
		for(NTIntraEdge ie : this.intraEdges)
		{ 
			long y1 = (long) (y - NodeTrixViz.CELL_SIZE/2 + i*h + h/2); 
			ie.createGraphics(h, y1,  x, 0, vs);
			i++;
		}
		
		//Triangle
		LongPoint[] p = new LongPoint[3];
		long cs = NodeTrixViz.CELL_SIZE/2;
		p[0] = new LongPoint(m.getPosition().x + x - cs, m.getPosition().y + y + cs);
		p[1] = new LongPoint(m.getPosition().x + x - cs, m.getPosition().y + y - cs);
		p[2] = new LongPoint(m.getPosition().x + x + cs, m.getPosition().y + y - cs);
		gTriangle = new VPolygon(p, 0, Color.white, Color.white, NodeTrixViz.RELATION_ARROW_ALPHA);
		vs.addGlyph(gTriangle);
		gTriangle.setSensitivity(false);
		this.triangles[0]= gTriangle;
		m.bkg.stick(gTriangle);
		
//		//inverse Triangle
//		p = new LongPoint[3];
//		p[0] = new LongPoint(m.getPosition().x - y + cs, m.getPosition().y - x - cs);
//		p[1] = new LongPoint(m.getPosition().x - y + cs, m.getPosition().y - x + cs);
//		p[2] = new LongPoint(m.getPosition().x - y - cs, m.getPosition().y - x + cs);
//    	gTriangleInverse = new VPolygon(p, 0, Color.white, Color.white, NodeTrixViz.RELATION_ARROW_ALPHA);
//		vs.addGlyph(gTriangleInverse);
//		gTriangleInverse.setSensitivity(false);		
//		this.relationGlyphs[1]= gTriangleInverse;
		
//		long west = mp.x + x - csHalf;
//		long north =  mp.y + y + csHalf;
//		long east = mp.x + x + csHalf;
//		long south = mp.y + y - csHalf;
//		int d = (int) (NodeTrixViz.CELL_SIZE *2 / intraEdges.size());
//		int n = intraEdges.size();
//		int nHalf = intraEdges.size() /2;
//		LongPoint[] p;
//		
//		if(n == 1)
//		{
//			p = new LongPoint[3];
//			p[0] = new LongPoint(west, north);
//			p[1] = new LongPoint(east, north);
//			p[2] = new LongPoint(east, south);
//	    	relationGlyphs[0] = new VPolygon(p, 0, intraEdges.firstElement().edgeColor);
//	    	relationGlyphs[0].setStrokeWidth(0);
//	    	vs.addGlyph(relationGlyphs[0]);
//	    	relationGlyphs[0].setOwner(intraEdges.get(0));
//	 
//////	    	p = new LongPoint[3];
//////	    	p[0] = new LongPoint(m.getPosition().x - y + cs, m.getPosition().y - x - cs);
//////			p[1] = new LongPoint(m.getPosition().x - y + cs, m.getPosition().y - x + cs);
//////			p[2] = new LongPoint(m.getPosition().x - y - cs, m.getPosition().y - x + cs);
//////	    	relationGlyphsInverse[0] = new VPolygon(p, 0, intraEdges.firstElement().edgeColor, Color.white, 1f);
//////	    	vs.addGlyph(relationGlyphsInverse[0]);
//////	    	relationGlyphsInverse[0].setOwner(relationGlyphsInverse[0]);
//   		}
//		else 
//		if(n % 2 == 0)// quads having an even number of properties
//		{	
//			for(int i = 0 ; i < n ; i++)
//			{
//				if(i == 0){	//upper triangle
//					p = new LongPoint[3];
//					p[0] = new LongPoint(west, north);
//					p[1] = new LongPoint(west + d, north);
//					p[2] = new LongPoint(west + d/2 ,north - d/2);
//				}else if(i == n-1){	//lower triangle
//					p = new LongPoint[3];
//					p[0] = new LongPoint(east, south);
//					p[1] = new LongPoint(east, south +d);
//					p[2] = new LongPoint(east - d/2 ,south + d/2);
//				}else if(i <= nHalf){	// in between stripes left side
//					p = new LongPoint[4];
//					p[0] = new LongPoint(west + i*d, north);
//					p[1] = new LongPoint(west + (i+1)*d, north);
//					p[2] = new LongPoint(west + (i+1)*d/2, north-(i+1)*d/2);
//					p[3] = new LongPoint(west + i*d/2, north + i*d/2);
//				}else{	// in between stripes right side
//					p = new LongPoint[4];
//					p[0] = new LongPoint(east, south + (n-i)*d);
//					p[1] = new LongPoint(east, south + ((n-i)-1)*d);
//					p[2] = new LongPoint(west + (i+1)*d/2, north-(i+1)*d/2);
//					p[3] = new LongPoint(west + i*d/2, north + i*d/2);
//				}
//				relationGlyphs[i] = new VPolygon(p, 0, intraEdges.get(i).edgeColor);
//		    	vs.addGlyph(relationGlyphs[i]);
//		    	relationGlyphs[i].setStrokeWidth(0);
//		    	relationGlyphs[i].setOwner(intraEdges.get(i));
//			}	
//		}
//		else{	//quads with an odd number of properties
//			for(int i = 0 ; i< n; i++)
//			{
//				if(i == 0){	//upper triangle
//					p = new LongPoint[3];
//					p[0] = new LongPoint(west, north);
//					p[1] = new LongPoint(west + d, north);
//					p[2] = new LongPoint(west + d/2 ,north - d/2);
//				}else if(i == n-1){	//lower triangle
//					p = new LongPoint[3];
//					p[0] = new LongPoint(east, south);
//					p[1] = new LongPoint(east, south +d);
//					p[2] = new LongPoint(west + i*d/2, north - i*d/2);
//				}else if(i == nHalf){	// center stripe
//					p = new LongPoint[5];
//					p[0] = new LongPoint(east - d/2, north);
//					p[1] = new LongPoint(east, north);
//					p[2] = new LongPoint(east ,north - d/2);
//					p[3] = new LongPoint(west + (i+1)*d/2 ,north-(i+1)*d/2);
//					p[4] = new LongPoint(west + i*d/2, north - i*d/2);
//				}else if(i < nHalf){	//in between stripes
//					p = new LongPoint[4];
//					p[0] = new LongPoint(west + i*d, north);
//					p[1] = new LongPoint(west + (i+1)*d, north);
//					p[2] = new LongPoint(west + (i+1)*d/2, north-(i+1)*d/2);
//					p[3] = new LongPoint(west + i*d/2, north + i*d/2);
//				}else{	//in between stripes
//					p = new LongPoint[4];
//					p[0] = new LongPoint(east, south + (n-i)*d);
//					p[1] = new LongPoint(east, south + ((n-i)-1)*d);
//					p[2] = new LongPoint(west + (i+1)*d/2, north-(i+1)*d/2);
//					p[3] = new LongPoint(west + i*d/2, north + i*d/2);
//				}
//				relationGlyphs[i] = new VPolygon(p, 0, intraEdges.get(i).edgeColor);
//		    	vs.addGlyph(relationGlyphs[i]);
//		    	relationGlyphs[i].setStrokeWidth(0);
//		    	relationGlyphs[i].setOwner(intraEdges.get(i));
//			}
//		}
//		
	}
    
    public void move(long x, long y)
    {
    	for(NTIntraEdge ie: this.intraEdges){ ie.move(x, y);}
    }
    
    public void moveTo(long x, long y)
    {
     	for(NTIntraEdge ie: this.intraEdges){ ie.moveTo(x, y);}
    }
 
   
	public void addIntraEdge(NTIntraEdge e)
	{
		this.intraEdges.add(e);
	}
}