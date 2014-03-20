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

    public static Point2D[] getLensPosition(GeneralPath route, Point2D C, double delta, int p, ArrayList<Point2D> attractionPoints, ArrayList<Point2D> attractionValues, Point2D overallAttraction) {
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
        Point2D L = C;
        Point2D Rc = null;
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

}

