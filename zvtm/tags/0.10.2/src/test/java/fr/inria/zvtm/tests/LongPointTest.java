/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */ 
package fr.inria.zvtm.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import fr.inria.zvtm.engine.LongPoint;

public class LongPointTest extends TestCase {

    public LongPointTest(String name){
        super(name);
    }

    public void setUp(){}

    public void testDistancePoint(){
        LongPoint p1 = new LongPoint(3,4);
        LongPoint p2 = new LongPoint(4,5);
        
        assertEquals(1.4142, p1.distance(p2), 0.01);
    }

    public void testDistanceSqPoint(){
        LongPoint p1 = new LongPoint(-20, 0); 
        LongPoint p2 = new LongPoint(20, 20); 

        assertEquals(2000, p2.distanceSq(p1), 0.001);
    }

    public void testDistanceDoubles(){
        LongPoint p1 = new LongPoint(10, 40);
        assertEquals(53.852 ,p1.distance(-10, -10), 0.01);
    }

    public void testDistanceSqDoubles(){
        LongPoint p1 = new LongPoint(10, 40);
        assertEquals(2900,p1.distanceSq(-10, -10), 0.001);
    }

    public void testCommutative(){
        LongPoint p1 = new LongPoint(100, -40);
        LongPoint p2 = new LongPoint(-100, -200);
        assertEquals(p1.distance(p2), p2.distance(p1), 0.0001);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(LongPointTest.class);
        return suite;
    }
}

