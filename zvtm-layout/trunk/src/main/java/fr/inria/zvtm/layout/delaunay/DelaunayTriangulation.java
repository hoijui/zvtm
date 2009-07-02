/* Code taken from http://www.cs.cornell.edu/Info/People/chew/Delaunay05.html, and adapted to work with ZVTM graphical entities */

/*
 * Copyright (c) 2005 by L. Paul Chew.
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, subject to the following 
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

/*  --------------------------------------------------------- UGLY FIRST EXAMPLE OF USE
* int INIT_SIZE = 10000;
* Simplex<Pnt> tri = new Simplex<Pnt>(new Pnt(-INIT_SIZE,INIT_SIZE), new Pnt(INIT_SIZE,INIT_SIZE), new Pnt(0,-INIT_SIZE));
* System.out.println("Triangle created: " + tri);
* DelaunayTriangulation dt = new DelaunayTriangulation(tri);
* System.out.println("DelaunayTriangulation created: " + dt);
* dt.delaunayPlace(new Pnt(0,0));
* dt.delaunayPlace(new Pnt(100,0));
* dt.delaunayPlace(new Pnt(0,100));
* dt.delaunayPlace(new Pnt(100,100));
* dt.delaunayPlace(new Pnt(100,200));
* dt.delaunayPlace(new Pnt(200,200));
* System.out.println("After adding 3 points, the DelaunayTriangulation is a " + dt);
* dt.printStuff();
*
*	Vector drawn = new Vector();
*	boolean exists = false;
*	for (Simplex<Pnt> triangle: dt){
*		for (Pnt site: triangle) {
*			exists = false;
*			for (int i=0;i<drawn.size();i++){
*				if (drawn.elementAt(i).equals(site)){
*					exists = true;
*					break;
*				}
*			}
*			if (!exists){
*				vsm.addGlyph(new VCircle((long)site.coord(0), (long)site.coord(1), 0, 2, Color.BLACK), mainSpaceName);
*				drawn.add(site);
*			}
*		}
*	}
*	drawn.clear();
*	for (Simplex<Pnt> triangle: dt){
*		for (Simplex<Pnt> other: dt.neighbors(triangle)) {
*			Pnt p = Pnt.circumcenter(triangle.toArray(new Pnt[0]));
*			Pnt q = Pnt.circumcenter(other.toArray(new Pnt[0]));
*			
*			exists = false;
*			for (int i=0;i<drawn.size();i+=2){
*				if (drawn.elementAt(i).equals(p) && drawn.elementAt(i+1).equals(q)
*				|| drawn.elementAt(i).equals(q) && drawn.elementAt(i+1).equals(p)){
*					exists = true;
*					break;
*				}
*			}
*			if (!exists){
*				vsm.addGlyph(new VSegment((long)p.coord(0), (long)p.coord(1), 0, Color.BLACK, (long)q.coord(0), (long)q.coord(1)), mainSpaceName);
*				drawn.add(p);
*				drawn.add(q);
*			}
*		}
*	}
*
*
* // Loop through all sites of the DT
* // Each is done several times, about 6 times each on average; this
* // can be proved via Euler's formula.
* for (Simplex<Pnt> triangle: dt)
* for (Pnt site: triangle) 
*     draw(site);
* 
* // Loop through all the edges of the DT (each is done twice)
* for (Simplex<Pnt> triangle: dt)
* for (Simplex<Pnt> other: dt.neighbors(triangle)) {
*     Pnt p = Pnt.circumcenter(triangle.toArray(new Pnt[0]));
*     Pnt q = Pnt.circumcenter(other.toArray(new Pnt[0]));
*     draw(p,q);
* }
* 
* // DELAUNAY TRIANGLES
* for (Simplex<Pnt> triangle: dt)
* for (Set<Pnt> edge: triangle.facets()) {
*     Pnt[] endpoint = edge.toArray(new Pnt[2]);
*     draw(endpoint[0], endpoint[1]);
* }
* 
* 
* // CIRCLES
* loop: for (Simplex<Pnt> triangle: dt) {
*     for (Pnt p: initialTriangle)  // Skip circles invoving initialTriangle
*         if (triangle.contains(p)) continue loop;
*     Pnt c = Pnt.circumcenter(triangle.toArray(new Pnt[0]));
*     double radius = c.subtract(triangle.iterator().next()).magnitude();
*     draw(c, radius, null);
* }
* 
---------------------------------------------*/

package fr.inria.zvtm.layout.delaunay;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

/**
 * A 2D Delaunay Triangulation (DT) with incremental site insertion.
 * This is not the fastest way to build a DT, but it's a reasonable way
 * to build the DT incrementally and it makes a nice interactive display.
 * There are several O(n log n) methods, but they require that either (1) 
 * the sites are all known initially or (2) the sites are inserted in random
 * order.
 * 
 * @author Paul Chew
 * 
 * Created July 2005.  Derived from an earlier, messier version.
 */
public class DelaunayTriangulation extends Triangulation<Pnt> {
    
    private Simplex<Pnt> mostRecent = null;  // Most recently inserted triangle
    public boolean debug = false;            // Used for debugging
    
    /**
     * Constructor.
     * All sites must fall within the initial triangle.
     * @param triangle the initial triangle
     */
    public DelaunayTriangulation (Simplex<Pnt> triangle) {
        super(triangle);
        mostRecent = triangle;
    }
    
    /**
     * Locate the triangle with point (a Pnt) inside (or on) it.
     * @param point the Pnt to locate
     * @return triangle (Simplex<Pnt>) that holds the point; null if no such triangle
     */
    public Simplex<Pnt> locate (Pnt point) {
        Simplex<Pnt> triangle = mostRecent;
        if (!this.contains(triangle)) triangle = null;
        
        // Try a directed walk (this works fine in 2D, but can fail in 3D)
        Set<Simplex<Pnt>> visited = new HashSet<Simplex<Pnt>>();
        while (triangle != null) {
            if (visited.contains(triangle)) { // This should never happen
                System.out.println("Warning: Caught in a locate loop");
                break;
            }
            visited.add(triangle);
            // Corner opposite point
            Pnt corner = point.isOutside(triangle.toArray(new Pnt[0]));
            if (corner == null) return triangle;
            triangle = this.neighborOpposite(corner, triangle);
        }
        // No luck; try brute force
        System.out.println("Warning: Checking all triangles for " + point);
        for (Simplex<Pnt> tri: this) {
            if (point.isOutside(tri.toArray(new Pnt[0])) == null) return tri;
        }
        // No such triangle
        System.out.println("Warning: No triangle holds " + point);
        return null;
    }
    
    /**
     * Place a new point site into the DT.
     * @param site the new Pnt
     * @return set of all new triangles created
     */
    public Set<Simplex<Pnt>> delaunayPlace (Pnt site) {
        Set<Simplex<Pnt>> newTriangles = new HashSet<Simplex<Pnt>>();
        Set<Simplex<Pnt>> oldTriangles = new HashSet<Simplex<Pnt>>();
        Set<Simplex<Pnt>> doneSet = new HashSet<Simplex<Pnt>>();
        Queue<Simplex<Pnt>> waitingQ = new LinkedList<Simplex<Pnt>>();
        
        // Locate containing triangle
        if (debug) System.out.println("Locate");
        Simplex<Pnt> triangle = locate(site);
        
        // Give up if no containing triangle or if site is already in DT
        if (triangle == null || triangle.contains(site)) return newTriangles;
        
        // Find Delaunay cavity (those triangles with site in their circumcircles)
        if (debug) System.out.println("Cavity");
        waitingQ.add(triangle);
        while (!waitingQ.isEmpty()) {
            triangle = waitingQ.remove();
            if (site.vsCircumcircle(triangle.toArray(new Pnt[0])) == 1) continue;
            oldTriangles.add(triangle);
            for (Simplex<Pnt> tri: this.neighbors(triangle)) {
                if (doneSet.contains(tri)) continue;
                doneSet.add(tri);
                waitingQ.add(tri);
            }
        }
        // Create the new triangles
        if (debug) System.out.println("Create");
        for (Set<Pnt> facet: Simplex.boundary(oldTriangles)) {
            facet.add(site);
            newTriangles.add(new Simplex<Pnt>(facet));
        }
        // Replace old triangles with new triangles
        if (debug) System.out.println("Update");
        this.update(oldTriangles, newTriangles);
        
        // Update mostRecent triangle
        if (!newTriangles.isEmpty()) mostRecent = newTriangles.iterator().next();
        return newTriangles;
    }
    
    /**
     * Main program; used for testing.
     */
    public static void main (String[] args) {
        Simplex<Pnt> tri = new Simplex<Pnt>(new Pnt(-10,10), new Pnt(10,10), new Pnt(0,-10));
        System.out.println("Triangle created: " + tri);
        DelaunayTriangulation dt = new DelaunayTriangulation(tri);
        System.out.println("DelaunayTriangulation created: " + dt);
        dt.delaunayPlace(new Pnt(0,0));
        dt.delaunayPlace(new Pnt(1,0));
        dt.delaunayPlace(new Pnt(0,1));
        System.out.println("After adding 3 points, the DelaunayTriangulation is a " + dt);
        dt.printStuff();
    }
}