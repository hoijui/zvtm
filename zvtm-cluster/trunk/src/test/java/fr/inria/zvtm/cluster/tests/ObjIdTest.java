package fr.inria.zvtm.cluster.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.awt.Color;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;

import fr.inria.zvtm.cluster.ObjId;

public class ObjIdTest extends TestCase {
	public ObjIdTest(String name){
		super(name);
	}

	public void testObjIdValidity(){
		//create a random Glyph (VCircle), make sure created ID 
		//is valid
		Glyph glyph = new VCircle(2,5,42,314,Color.WHITE);
		ObjId id = glyph.getObjId();
		assertTrue(id.isValid());
	}
}
