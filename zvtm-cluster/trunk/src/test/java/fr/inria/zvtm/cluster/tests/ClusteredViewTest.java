/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster.tests;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.cluster.ClusteredView;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ClusteredViewTest extends TestCase {
	public ClusteredViewTest(String name){
		super(name);
	}

	public void testZeroViewRows(){
		try{
			ClusteredView cv = new ClusteredView(
					2,
					400, 300,
					3, 2, 
					0, 1,
					new ArrayList<Camera>());
		fail();
		} catch(IllegalArgumentException expected){}
	}

	public void testZeroViewCols(){
		try{
			ClusteredView cv = new ClusteredView(
					2,
					400, 300,
					3, 2, 
					1, 0,
					new ArrayList<Camera>());
		fail();
		} catch(IllegalArgumentException expected){}
	}

	public void testExcessViewRows(){
		try{
			ClusteredView cv = new ClusteredView(
					2,
					400, 300,
					3, 2, 
					4, 1,
					new ArrayList<Camera>());
			fail();
		} catch(IllegalArgumentException expected){}
	}

	public void testExcessViewCols(){
		try{
			ClusteredView cv = new ClusteredView(
					2,
					400, 300,
					3, 2, 
					4, 1,
					new ArrayList<Camera>());
			fail();
		} catch(IllegalArgumentException expected){}
	}
}

