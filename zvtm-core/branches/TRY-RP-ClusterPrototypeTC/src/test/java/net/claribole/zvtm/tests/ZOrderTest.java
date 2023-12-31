/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 

package net.claribole.zvtm.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.VOctagon;

import net.claribole.zvtm.glyphs.CGlyph;
import net.claribole.zvtm.glyphs.SGlyph;

import java.awt.Color;

import java.util.ArrayList;
import java.util.List;

public class ZOrderTest extends TestCase {

    public ZOrderTest(String name){
	super(name);

	VirtualSpaceManager.setDebug(true);
    }

    public void setUp(){
	vsm = new VirtualSpaceManager();
	vs = vsm.addVirtualSpace("testVS");
    }

    public void testNullGlyphAbove(){
	Glyph g1 = new VCircle(2, 5, 42, 314, Color.WHITE);
	Glyph g2 = null;
	
	vsm.addGlyph(g1, vs, false);
	vsm.addGlyph(g2, vs, false);

	vs.above(g1, g2);
	
	//I simply expect that g1 will not change its z-index
	//(what about stack order?)
	assertEquals(42, g1.getZindex());
     }

    public void testMinZindex(){
	Glyph g1 = new VCircle();
	Glyph g2 = new VCircle();

	vsm.addGlyph(g1, vs, false); 
	vsm.addGlyph(g2, vs, false);

	vs.atBottom(g1, Integer.MIN_VALUE);
	vs.below(g2, g1);

	assertTrue(g2.getZindex() <= g1.getZindex());
	//test stack order?
    }

    public void testMaxZindex(){
	Glyph g1 = new VCircle();
	Glyph g2 = new VCircle();

	vsm.addGlyph(g1, vs, false); 
	vsm.addGlyph(g2, vs, false);

	vs.onTop(g1, Integer.MAX_VALUE);
	vs.above(g2, g1);

	assertTrue(g2.getZindex() >= g1.getZindex());
	//test stack order?
    }

    public void testAboveSameGlyph(){
	Glyph g = new VOctagon(10, 10, 3, 30, Color.BLACK);
	vsm.addGlyph(g, vs, false);
	vs.above(g, g);

	assertEquals(3, g.getZindex());
    }

    public void testBelowSameGlyph(){
	Glyph g = new VOctagon(10, 10, 3, 30, Color.BLACK);
	vsm.addGlyph(g, vs, false);
	vs.below(g, g);

	assertEquals(3, g.getZindex());
    }

    public void testReorderGlyphs(){
	List glyphs = new ArrayList();
	final int nbGlyphs = 100;

	for(int i=0; i<nbGlyphs; ++i){
	    Glyph g = new VOctagon(10, 10, i, 30, Color.BLACK);
	    glyphs.add(g);
	    vsm.addGlyph(g, vs, false);
	}

	for(int i=0; i<nbGlyphs; ++i){
	    vs.atBottom((Glyph)glyphs.get(i));
	}

	for(int i=0; i<nbGlyphs-1; ++i){
	    assertTrue(((Glyph)glyphs.get(i)).getZindex() >= 
		       ((Glyph)glyphs.get(i + 1)).getZindex());
	}
    }

    public void testRandomReorderTop(){
	List glyphs = new ArrayList();
	
	for(int i=0; i<pseudoRandomIndexes.length; ++i){
	    Glyph g = new VCircle(10, 10, pseudoRandomIndexes[i], 
				  30, Color.BLACK);
	    glyphs.add(g);
	    vsm.addGlyph(g, vs, false);
	}

	for(int i=0; i<pseudoRandomIndexes.length; ++i){
	    vs.onTop((Glyph)glyphs.get(i));
	}

	for(int i=0; i<pseudoRandomIndexes.length-1; ++i){
	    assertTrue(((Glyph)glyphs.get(i)).getZindex() <= 
		       ((Glyph)glyphs.get(i + 1)).getZindex());
	}
    }

   public void testRandomReorderBottom(){
	List glyphs = new ArrayList();
	
	for(int i=0; i<pseudoRandomIndexes.length; ++i){
	    Glyph g = new VCircle(10, 10, pseudoRandomIndexes[i], 
				  30, Color.BLACK);
	    glyphs.add(g);
	    vsm.addGlyph(g, vs, false);
	}

	for(int i=0; i<pseudoRandomIndexes.length; ++i){
	    vs.atBottom((Glyph)glyphs.get(i));
	}

	for(int i=0; i<pseudoRandomIndexes.length-1; ++i){
	    assertTrue(((Glyph)glyphs.get(i)).getZindex() >= 
		       ((Glyph)glyphs.get(i + 1)).getZindex());
	}
    }

    public void testUnrelatedGlyphs(){
 	//one glyph is part of the virtual space and the second one
 	//is unrelated; I expect this should have no effect on the z-index
 	//(and ordering) of either glyphs.

	Glyph g1 = new VCircle(10, 10, 42, 30, Color.BLACK);
	Glyph g2 = new VCircle(10, 10, 43, 30, Color.BLACK);
	
	vsm.addGlyph(g1, vs, false);
	vs.below(g2, g1);

	assertEquals(g1.getZindex(), 42);
	assertEquals(g2.getZindex(), 43);
    }

    public void testCompositeGlyphs(){
	Glyph g1 = new VCircle(10, 30, 42, 30, Color.BLACK);
	Glyph g2 = new VCircle(10, 10, 47, 30, Color.BLACK);

	Glyph g3 = new VCircle(10, 30, 40, 30, Color.BLACK);
	Glyph g4 = new VCircle(10, 10, 43, 30, Color.BLACK);
	
	CGlyph cg1 = new CGlyph(g1, new SGlyph[]{new SGlyph(g2, 10, 23)});
	CGlyph cg2 = new CGlyph(g3, new SGlyph[]{new SGlyph(g4, -42, 314)});
	
	vsm.addGlyph(g1, vs, false);
	vsm.addGlyph(g2, vs, false);
	vsm.addGlyph(g3, vs, false);
	vsm.addGlyph(g4, vs, false);
	vsm.addGlyph(cg1, vs, false);
	vsm.addGlyph(cg2, vs, false);

	vs.above(cg2, cg1);

	assertTrue(cg2.getZindex() >= cg1.getZindex());
    }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTestSuite(ZOrderTest.class);
	return suite;
    }

    private VirtualSpaceManager vsm;
    private VirtualSpace vs;

    //kindly provided by http://www.random.org
    private static final int[] pseudoRandomIndexes = new int[]{
	-78,	-393,	64,	319,	169,
	-6,	322,	164,	-400,	-80,
	234,	-245,	104,	-268,	403,
	462,	-205,	-183,	-340,	-250,
	-101,	174,	-108,	-172,	485,
	336,	-122,	-417,	384,	-285,
	-203,	-299,	-153,	20,	0,
	390,	-113,	74,	454,	382,
	170,	-352,	-311,	383,	46,
	10,	115,	331,	214,	220,
	177,	358,	-120,	448,	295,
	-190,	-290,	-260,	-215,	-255,
	-116,	-194,	65,	-161,	400,
	459,	336,	-208,	-68,	-324,
	190,	-340,	-355,	-246,	63,
	-219,	344,	463,	47,	98,
	147,	71,	243,	395,	381,
	309,	-209,	-60,	3,	-196,
	-276,	313,	100,	-398,	73,
	-144,	339,	-332,	-86,	73
    };

}
