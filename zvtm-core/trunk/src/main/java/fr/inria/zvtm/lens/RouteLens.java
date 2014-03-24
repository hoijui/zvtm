/*   AUTHOR : Caroline Appert (appert@lri.fr)
 *   Copyright (c) CNRS, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.lens;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Point;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.lens.Lens;
import fr.inria.zvtm.glyphs.DPath;

/** A utility class implementing the RouteLens motor behavior.
 * The RouteLens motor behavior is described in <strong>J. Alvina, C. Appert, O. Chapuis, E. Pietriga, RouteLens: Easy Route Following for Map Applications, AVI '14: Proceedings of the 12th working conference on Advanced visual interfaces</strong>.
 * It can be coupled with any type of ZVTM lens.
 * When moving a lens, simply call RouteLens.moveLens(x, y) instead of Lens.setAbsolutePosition(x, y);
<p>Example of use, specifying that Lens l's position should stick to the route who's geometry is encoded by a DPath:</p>
<pre>
DPath route1 = ...;
DPath route2 = ...;
Lens l = ...;
Camera c = ...;
RouteLens rl = new RouteLens(l, c);
rl.addRoute(route1);
rl.addRoute(route2);
...

public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
    rl.moveLens(jpx, jpy);
}
</pre>
 *@author Caroline Appert
 *@since 0.11.2
 */

public class RouteLens {

    public static ArrayList<GeneralPath> cutPath(GeneralPath path) {
        ArrayList<GeneralPath> paths = new ArrayList<GeneralPath>();
        PathIterator pathIterator = path.getPathIterator(null);
        Point2D lastPoint = null;
        double[] coords = new double[6];
        GeneralPath subGP = null;
        while(!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                lastPoint = new Point2D.Double(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                subGP = new GeneralPath();
                subGP.moveTo(lastPoint.getX(), lastPoint.getY());
                subGP.lineTo(coords[0], coords[1]);
                lastPoint = new Point2D.Double(coords[0], coords[1]);
                paths.add(subGP);
                break;
            case PathIterator.SEG_QUADTO:
                subGP = new GeneralPath();
                subGP.moveTo(lastPoint.getX(), lastPoint.getY());
                subGP.quadTo(coords[0], coords[1], coords[2], coords[3]);
                lastPoint = new Point2D.Double(coords[2], coords[3]);
                paths.add(subGP);
                break;
            case PathIterator.SEG_CUBICTO:
                subGP = new GeneralPath();
                subGP.moveTo(lastPoint.getX(), lastPoint.getY());
                subGP.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                lastPoint = new Point2D.Double(coords[4], coords[5]);
                paths.add(subGP);
                break;
            default:
                System.out.println("unexpected segment type: "+type);
            }
            pathIterator.next();
        }
        return paths;
    }
    
    public static GeneralPath concatPath(GeneralPath path1, GeneralPath path2) {
    	GeneralPath path = (GeneralPath)path1.clone();
        PathIterator pathIterator = path2.getPathIterator(null);
        double[] coords = new double[6];
        while(!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                path.moveTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO:
            	path.lineTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                break;
            default:
                System.out.println("unexpected segment type: "+type);
            }
            pathIterator.next();
        }
        return path;
    }

    public static Point2D getClosestPoint(Point2D pt, GeneralPath path, double maxSegmentLength) {
        ArrayList<Point2D> points = getPoints(path, maxSegmentLength);
        Point2D closest = null;
        double minDis = Double.MAX_VALUE;
        for (Iterator<Point2D> iterator = points.iterator(); iterator.hasNext();) {
            Point2D p = iterator.next();
            double d = p.distance(pt);
            if(d < minDis) {
                minDis = d;
                closest = p;
            }
        }
        return closest;
    }

    public static ArrayList<Point2D> getPoints(GeneralPath path, double maxSegmentLength) {
        ArrayList<Point2D> points = new ArrayList<Point2D>();
        PathIterator pathIterator = path.getPathIterator(null, 0.1);
        double[] coords = new double[6];
        while(!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                points.add(new Point2D.Double(coords[0], coords[1]));
                break;
            case PathIterator.SEG_LINETO:
                points.addAll(resample(points.get(points.size()-1), new Point2D.Double(coords[0], coords[1]), maxSegmentLength));
                break;
            default:
                System.out.println("unexpected segment type: "+type);
            }
            pathIterator.next();
        }
        return points;
    }

    /**
     * For debug purpose
     */
    static Point2D[] getLensPosition(GeneralPath route, Point2D C, double delta, int p, ArrayList<Point2D> attractionPoints, ArrayList<Point2D> attractionValues, Point2D overallAttraction) {
        Point2D L = C;
        Point2D Rc = null;
        Point2D closest = null;
        double dmin = Double.MAX_VALUE;
        double dc;
        int n = 0;
        Point2D A = new Point2D.Double(0, 0);
        ArrayList<GeneralPath> routeSegments = cutPath(route);
        double sumWeights = 0.0;
        for (Iterator<GeneralPath> iterator = routeSegments.iterator(); iterator.hasNext();) {
            GeneralPath routeSegment = iterator.next();
            Point2D closestPointOnSegment = getClosestPoint(C, routeSegment, 5);
            double distanceToSegment = closestPointOnSegment.distance(C);
            if(distanceToSegment < delta) {
                // segment attracts the cursor
                Rc = closestPointOnSegment;
                dc = distanceToSegment;
                double alpha = 1.0 - Math.pow(dc/delta, p);
                Point2D Ai = new Point2D.Double(
                        alpha * (Rc.getX() - C.getX())/dc,
                        alpha * (Rc.getY() - C.getY())/dc);
                double weight = delta - dc;
                sumWeights += weight;
                A.setLocation(
                        A.getX() + weight*Ai.getX(),
                        A.getY() + weight*Ai.getY());
                attractionPoints.add(Rc);
                attractionValues.add(Ai);
                if(dc < dmin) {
                    dmin = dc;
                    closest = closestPointOnSegment;
                }
                n++;
            }
        }
        Point2D[] res = new Point2D[2];
        if(n != 0) {
            L = new Point2D.Double(
                    C.getX() + dmin * A.getX() / sumWeights,
                    C.getY() + dmin * A.getY() / sumWeights);
            overallAttraction.setLocation(
                    dmin * A.getX() / sumWeights,
                    dmin * A.getY() / sumWeights);
        }
        res[0] = L;
        res[1] = closest;
        return res;
    }

    public static Point2D getLensPosition(GeneralPath route, Point2D C, double delta, int p) {
        ArrayList<GeneralPath> routeSegments = cutPath(route);
        return getLensPosition(routeSegments, C, delta, p);
    }
    
    public static Point2D getLensPosition(ArrayList<GeneralPath> routeSegments, Point2D C, double delta, int p) {
        Point2D L = C;
        Point2D Rc = null;
        double dmin = Double.MAX_VALUE;
        double dc;
        int n = 0;
        Point2D A = new Point2D.Double(0, 0);
        double sumWeights = 0.0;
        for (Iterator<GeneralPath> iterator = routeSegments.iterator(); iterator.hasNext();) {
            GeneralPath routeSegment = iterator.next();
            Point2D closestPointOnSegment = getClosestPoint(C, routeSegment, 5);
            double distanceToSegment = closestPointOnSegment.distance(C);
            if(distanceToSegment < delta) {
                // segment attracts the cursor
                Rc = closestPointOnSegment;
                dc = distanceToSegment;
                double alpha = 1.0 - Math.pow(dc/delta, p);
                Point2D Ai = new Point2D.Double(
                        alpha * (Rc.getX() - C.getX())/dc,
                        alpha * (Rc.getY() - C.getY())/dc);
                double weight = delta - dc;
                sumWeights += weight;
                A.setLocation(
                        A.getX() + weight*Ai.getX(),
                        A.getY() + weight*Ai.getY());
                if(dc < dmin) {
                    dmin = dc;
                }
                n++;
            }
        }
        if(n != 0) {
            L = new Point2D.Double(
                    C.getX() + dmin * A.getX() / sumWeights,
                    C.getY() + dmin * A.getY() / sumWeights);
        }
        return L;
    }


    public static ArrayList<Point2D> resample(Point2D pt1, Point2D pt2, double maxSegmentLength) {
        ArrayList<Point2D> newPoints = new ArrayList<Point2D>();
        double d = pt1.distance(pt2);
        if(d < maxSegmentLength) {
            newPoints.add(pt2);
            return newPoints;
        } else {
            int nPoints = (int)(d / maxSegmentLength);
            for(int i = 1; i < nPoints; i++) {
                double x = pt1.getX() + (i*maxSegmentLength/d) * (pt2.getX() - pt1.getX());
                double y = pt1.getY() + (i*maxSegmentLength/d) * (pt2.getY() - pt1.getY());
                newPoints.add(new Point2D.Double(x, y));
            }
            if((nPoints-1)*maxSegmentLength < d) {
                newPoints.add(pt2);
            }
        }

        return newPoints;
    }

    /*----------------------------------------*/

    Lens lens;
    View view;
    Camera camera;
    ArrayList<DPath> routes;
    ArrayList<GeneralPath> routeSegments;
    double delta;
    static final int DEFAULT_P = 2;
    int param_p = DEFAULT_P;

    boolean enabled = true;

    Point2D.Double cursorInVS = new Point2D.Double();
    Point lensCenterInPanel = new Point();

    /**
     *@param l the lens whose position will be influenced by route dp.
     *@param c the camera observing the route in the View that holds the lens.
     *@param mad the maximum attraction distance, beyond which a route segment will not exert any influence on the lens.
     *@param p power parameter used to fine-tune the attraction effect. Default is 2, typically in range [2,6].
     */
    public RouteLens(Lens l, Camera c, double mad, int p){
        this.lens = l;
        this.camera = c;
        this.view = c.getOwningView();
        this.delta = mad;
        this.param_p = p;
        this.routeSegments = new ArrayList<GeneralPath>();
        this.routes = new ArrayList<DPath>();
    }

    /**
     *@param l the lens whose position will be influenced by route dp.
     *@param c the camera observing the route in the View that holds the lens.
     */
    public RouteLens(Lens l, Camera c){
        this.lens = l;
        this.camera = c;
        this.view = c.getOwningView();
        this.delta = l.getRadius();
        this.param_p = 2;
        this.routeSegments = new ArrayList<GeneralPath>();
        this.routes = new ArrayList<DPath>();
    }

    /** Move the lens to coordinates x,y, possibly adjusting its actual position depending on attraction forces exterted by the route.
     *@param x x-coord of the default lens center position. Usually the mouse cursor's x-coord in screen space (JPanel coord sys).
     *@param y y-coord of the default lens center position. Usually the mouse cursor's y-coord in screen space (JPanel coord sys).
     */
    public void moveLens(int x, int y){
        if (enabled){
            view.fromPanelToVSCoordinates(x, y, camera, cursorInVS);
            Point2D lensCenterInVS = RouteLens.getLensPosition(routeSegments, cursorInVS,
                                                               delta * (camera.altitude + camera.focal) / camera.focal,
                                                               param_p);
            view.fromVSToPanelCoordinates(lensCenterInVS.getX(), lensCenterInVS.getY(), camera, lensCenterInPanel);
            lens.setAbsolutePosition(lensCenterInPanel.x, lensCenterInPanel.y);
        }
        else {
            lens.setAbsolutePosition(x, y);
        }
        view.repaint();
    }
    
    protected void updateGeneralPath() {
    	GeneralPath route = this.routes.get(0).getJava2DGeneralPath();
    	for (int i = 1; i < this.routes.size(); i++) {
    		DPath path = this.routes.get(i);
    		route = RouteLens.concatPath(route, path.getJava2DGeneralPath());
		}
    	routeSegments = cutPath(route);
    }
    
    /**
     * *@param route a route that should influence the lens' position.
     */
    public void addRoute(DPath route) {
    	this.routes.add(route);
    	updateGeneralPath();
    }
    
    /**
     * @param route a route that does not influence anymore the lens' position.
     */
    public void removeRoute(DPath route) {
    	this.routes.remove(route);
    	updateGeneralPath();
    }

    /**Enabled/disable RouteLens attraction.*/
    public void setEnabled(boolean b){
        this.enabled = b;
    }

    /**Is RouteLens attraction enabled or disabled.*/
    public boolean isEnabled(){
        return this.enabled;
    }

	/**
	 * @return the maximum attraction distance, beyond which a route segment will not exert any influence on the lens.
	 */
	public double getMad() {
		return delta;
	}

	/**
	 * @param mad the maximum attraction distance, beyond which a route segment will not exert any influence on the lens.
	 */
	public void setMad(double mad) {
		this.delta = mad;
	}

	/**
	 * @return power parameter used to fine-tune the attraction effect. Default is 2, typically in range [2,6].
	 */
	public int getP() {
		return param_p;
	}

	/**
	 * @param p power parameter used to fine-tune the attraction effect. Default is 2, typically in range [2,6].
	 */
	public void setP(int p) {
		this.param_p = p;
	}

}
