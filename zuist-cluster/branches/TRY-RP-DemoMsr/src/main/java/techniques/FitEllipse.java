/*****************************************************************************
 * Copyright (C) 2008 Jean-Daniel Fekete and INRIA, France                  *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the X11 Software License    *
 * a copy of which has been included with this distribution in the           *
 * license.txt file.                                                         *
 *****************************************************************************/
package techniques;

import java.awt.geom.Point2D;

//import fr.aviz.motionrecorder.motion.Motion;

/**
 * <b>FitEllipse</b> define methods to fit an ellipse
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class FitEllipse {
    /**
     * <b>ImplicitParams</b> contains the implicit parameters of an ellipse of
     * the form:<br>
     * a*x^2 + b*x*y + c*y^2 + d*x + e*y + f = 0
     * 
     * @author Jean-Daniel Fekete
     * @version $Revision$
     */
    public static class ImplicitParams {
        /** a */
        public double a;
        /** b */
        public double b;
        /** c */
        public double c;
        /** d */
        public double d;
        /** e */
        public double e;
        /** f */
        public double f;

        /**
         * Creates an Ellipse implicit parameter set.
         * 
         * @param a
         *            a
         * @param b
         *            b
         * @param c
         *            c
         * @param d
         *            d
         * @param d
         *            d
         * @param e
         *            e
         * @param f
         *            f
         */
        public ImplicitParams(
                double a,
                double b,
                double c,
                double d,
                double e,
                double f) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
        }

        /**
         * Default constructor.
         */
        public ImplicitParams() {
        }

        /**
         * Compute the algebraic distance of the specified point to the curve.
         * 
         * @param x
         *            the x coordinate of the point
         * @param y
         *            the y coordinate of the point
         * @param params
         *            the parameters of the curve
         * @return the parametric distance
         */
        public double computeDistance(double x, double y) {
            double p = a * x * x + b * x * y + c * y * y + d * x + e * y + f;
            return p;
        }

        /**
         * Compute the algebraic distance of the specified point to the curve.
         * 
         * @param point
         *            the point
         * @param params
         *            the parameters of the curve
         * @return the parametric distance
         */
        public double computeDistance(Point2D point) {
            return computeDistance(point.getX(), point.getY());
        }
    }

    /**
     * <b>EllipseParams</b> contains the parametric description of an ellipse
     * of the form x(t),y(t): x'(t) = rX * cos(t)<br>
     * y'(t) = rY * sin(t)<br>
     * x(t) = x'(t)*cos(alpha)-y'(t)*sin(alpha) + cx<br>
     * y(t) = x'(t)*sin(alpha)+y'(t)*cos(alpha) + cy
     * 
     * @author Jean-Daniel Fekete
     * @version $Revision$
     */
    public static class EllipseParams {
        /** X coordinate of the Ellipse center. */
        public double cx;
        /** Y coordinate of the Ellipse center. */
        public double cy;
        /** rotation of the ellipse main axis. */
        public double angle;
        /** X radius */
        public double rX;
        /** Y radius */
        public double rY;

        /**
         * Constructor with parameters
         * 
         * @param cx
         *            X coordinate of the ellipse center
         * @param cy
         *            Y coordinate of the ellipse center
         * @param angle
         *            roation of the ellipse main axis
         * @param rX
         *            X radius
         * @param rY
         *            Y radius
         */
        public EllipseParams(
                double cx,
                double cy,
                double angle,
                double rX,
                double rY) {
            this.cx = cx;
            this.cy = cy;
            this.angle = angle;
            this.rX = rX;
            this.rY = rY;
        }

        /**
         * Default constructor
         */
        public EllipseParams() {
        }

        /**
         * Computes the position at the specified parameter t.
         * 
         * @param t
         *            the angle
         * @param p
         *            a point or null
         * @return the point or null
         */
        public Point2D computePosition(double t, Point2D p) {
            if (p == null) {
                p = new Point2D.Double();
            }
            double x = rX * Math.cos(t);
            double y = rY * Math.sin(t);
            double nx = x * Math.cos(angle) - y * Math.sin(angle) + cx;
            double ny = x * Math.sin(angle) + y * Math.cos(angle) + cy;
            p.setLocation(nx, ny);

            return p;
        }
        
        public double computeAngle(Point2D p) {
        	if (p == null) {
        		p = new Point2D.Double();
        	}
        	double angle = Math.atan( (p.getY()-cy)/(p.getX()-cx));//*180/Math.PI;
        	return angle;
        }
        

        /**
         * Computes the positions of the ellipse and store it in the specified
         * table.
         * 
         * @param points
         *            the points to store the results
         * @return the points
         */
        public Point2D[] computePosition(Point2D[] points) {
            int n = points.length;
            int n2 = n / 2;

            for (int i = 0; i < n2; i++) {
                double t = i * Math.PI / n2;
                double x = rX * Math.cos(t);
                double y = rY * Math.sin(t);
                double nx = x * Math.cos(angle) - y * Math.sin(angle);
                double ny = x * Math.sin(angle) + y * Math.cos(angle);
                points[i].setLocation(cx + nx, cy + ny);
                points[i + n2].setLocation(cx - nx, cy - ny);
            }
            return points;
        }
        
        /**
         * Compute the euclidian distance of the specified point to the ellipse.
         * 
         * @param x the X coordinate
         * @param y the Y coordinate
         * @param result the closest point on the ellipse
         * @return the distance
         */
        public double computeDistance(double x, double y, Point2D result) {
            x -= cx;
            y -= cy;
            double sin = 0;
            double cos = 0;
            if (angle != 0) {
                sin = Math.sin(-angle);
                cos = Math.cos(-angle);
                double nx = x * cos - y * sin;
                double ny = x * sin + y * cos;
                x = nx;
                y = ny;
            }
            double dist = DistancePointEllipse(x, y, rX, rY, result);
            if (result != null) {
                x = result.getX();
                y = result.getY();
                if (angle != 0) {
                    double nx = x * cos + y * sin;
                    double ny = -x * sin + y * cos;
                    x = nx;
                    y = ny;
                }
                x += cx;
                y += cy;
                result.setLocation(x, y);
            }
            return dist;
        }
        
        /**
         * Compute the euclidian distance of the specified point to the ellipse.
         * 
         * @param x the X coordinate
         * @param y the Y coordinate
         * @return the distance
         */
        public double computeDistance(double x, double y) {
            x -= cx;
            y -= cy;
            if (angle != 0) {
                double sin = Math.sin(-angle);
                double cos = Math.cos(-angle);
                double nx = x * cos - y * sin;
                double ny = x * sin + y * cos;
                x = nx;
                y = ny;
            }
            return DistancePointEllipse(x, y, rX, rY, null);
        }


        /**
         * Compute the euclidian distance of the specified point to the ellipse.
         * 
         * @param point
         *            the point
         * @return the distance
         */
        public double computeDistance(Point2D point) {
            return computeDistance(point.getX(), point.getY());
        }
        
        public String toString() {
        	return "ellipse: \ncx = "+this.cx+"\ncy = "+this.cy+"\nangle = "+this.angle+"\nrX = "+this.rX+"\nrX = "+this.rY;
        }
}

    /**
     * Fits an ellipse on the specified points, returning [cx,cy,angle,a,b]
     * where cx and cy are the center, angle is the angle, a is the largest
     * semi-axe length and b is the other semi-axe length.
     * 
     * @param points
     *            the vector of points
     * @return [cx, cy, angle, a, b] or null if the parameters cannot be
     *         computed.
     */
    public static EllipseParams fitParametric(Point2D[] points) {
        ImplicitParams params = fitImplicit(points);
        if (params == null)
            return null; // matrix is singular
        return implicitToParametric(params);
    }
    
    /**
     * Fits an ellipse on the specified points, returning [cx,cy,angle,a,b]
     * where cx and cy are the center, angle is the angle, a is the largest
     * semi-axe length and b is the other semi-axe length.
     * 
     * @param points the vector of points
     * @param off offset of the first point to consider
     * @param len number of points to consider after the offset            
     * @return [cx, cy, angle, a, b] or null if the parameters cannot be
     *         computed.
     */
    public static EllipseParams fitParametric(Point2D[] points, int off, int len) {
        ImplicitParams params = fitImplicit(points, off, len);
        if (params == null)
            return null; // matrix is singular
        return implicitToParametric(params);        
    }

    /**
     * Computes the parametric variance of the set of points to the specified
     * curve.
     * 
     * @param points
     *            the points
     * @param params
     *            the parameters of the curve
     * @return the sum of distances divided by the number of points
     */
    public static double computeParametricVariance(
            Point2D[] points,
            ImplicitParams params) {
        return computeAlgebraicVariance(points, 0, points.length, params);
    }
    
    /**
     * Computes the algebraic variance of the set of points to the specified
     * curve.
     * 
     * @param points
     *            the points
     * @param off
     *            the offset
     * @param len
     *            the length
     * @param params
     *            the parameters of the curve
     * @return the sum of distances divided by the number of points
     */
    public static double computeAlgebraicVariance(
            Point2D[] points,
            int off,
            int len,
            ImplicitParams params) {
        if (params == null) {
            params = fitImplicit(points, off, len);
        }
        if (params == null) {
            throw new FitException("Fit failed");
            //return Double.NaN; // singular matrix in fit
        }

        double sum = 0;
        for (int i = off; i < (off + len); i++) {
            double p = computeAlgebraicDistance(points[i], params);
            sum += p * p;
        }
        return sum / (len - 1);
    }

    /**
     * Compute the algebraic distance of the specified point to the curve.
     * 
     * @param x
     *            the x coordinate of the point
     * @param y
     *            the y coordinate of the point
     * @param params
     *            the parameters of the curve
     * @return the parametric distance
     */
    public static double computeAlgebraicDistance(
            double x,
            double y,
            ImplicitParams params) {
        return params.computeDistance(x, y);
    }

    /**
     * Compute the algebraic distance of the specified point to the curve.
     * 
     * @param point
     *            the point
     * @param params
     *            the parameters of the curve
     * @return the parametric distance
     */
    public static double computeAlgebraicDistance(
            Point2D point,
            ImplicitParams params) {
        return params.computeDistance(point);
    }

    /**
     * Compute the euclidian distance of the specified point to the curve.
     * 
     * @param point
     *            the point
     * @param ell
     *            the ellipse parameters [cx,cy,angle,a,b] or null
     * @return the euclidian distance
     */
    public static double computeDistance(
            Point2D point,
            EllipseParams ell) {
        return ell.computeDistance(point.getX(), point.getY());
    }
    
    static private double DistancePointEllipseSpecial(
            double x,
            double y,
            double a,
            double b,
            double epsilon,
            int maxIter,
            Point2D result) {
        double rdX = 0;
        double rdY = 0;

        // initial guess
        double dT = b * (y - b);
        // Newton’s method
        int i;
        for (i = 0; i < maxIter; i++) {
            double dTpASqr = dT + a * a;
            double dTpBSqr = dT + b * b;
            double dInvTpASqr = 1.0 / dTpASqr;
            double dInvTpBSqr = 1.0 / dTpBSqr;
            double dXDivA = a * x * dInvTpASqr;
            double dYDivB = b * y * dInvTpBSqr;
            double dXDivASqr = sqr(dXDivA);
            double dYDivBSqr = sqr(dYDivB);
            double dF = dXDivASqr + dYDivBSqr - 1.0;
            if (dF < epsilon) {
                // F(t0) is close enough to zero, terminate the iteration
                rdX = dXDivA * a;
                rdY = dYDivB * b;
                if (result != null) {
                    //riIFinal = i;
                    result.setLocation(rdX, rdY);
                }
                break;
            }
            double dFDer = 2.0 * (dXDivASqr*dInvTpASqr + dYDivBSqr*dInvTpBSqr);
            double dRatio = dF / dFDer;
            if (dRatio < epsilon) {
                // t1-t0 is close enough to zero, terminate the iteration
                rdX = dXDivA * a;
                rdY = dYDivB * b;
                if (result != null) {
                    //riIFinal = i;
                    result.setLocation(rdX, rdY);
                }
                break;
            }
            dT += dRatio;
        }
        if (i == maxIter) {
            // method failed to converge, let caller know
//            riIFinal = -1;
            throw new FitException("Distance iteration did not covnerge");
//            if (result != null) {
//                //result.setLocation(Double.NaN, Double.NaN);
//            }
//            return Double.NaN;
        }
//        else {
//            System.out.println("Performed "+i+" iterations");
//        }
        double dDelta0 = rdX - x;
        double dDelta1 = rdY - y;
        return Math.hypot(dDelta0, dDelta1);
    }
    
    /**
     * Computes the distance and intersection between a simple eclipse and
     * a point:<br>
     * x^2/a^2 + y^2/b^2 = 1
     * 
     * @param x x coordinate of point
     * @param y y coordinate of point
     * @param a a 
     * @param b b
     * @param epsilon precision
     * @param maxIter max number of iterations
     * @param result intersection point or null
     * @return
     */
    static public double DistancePointEllipse(
            double x,
            double y,
            double a,
            double b,
            Point2D result) {
        return DistancePointEllipse(x, y, a, b, 1e-6, 100, result);
    }

    /**
     * Computes the distance and intersection between a simple eclipse and
     * a point:<br>
     * x^2/a^2 + y^2/b^2 = 1
     * 
     * @param x x coordinate of point
     * @param y y coordinate of point
     * @param a a 
     * @param b b
     * @param epsilon precision
     * @param maxIter max number of iterations
     * @param result intersection point or null
     * @return
     */
    static public double DistancePointEllipse(
            double x,
            double y,
            double a,
            double b,
            double epsilon,
            int maxIter,
            Point2D result) {
        // special case of circle
        if (Math.abs(a - b) < epsilon) {
            double dLength = Math.hypot(x, y);
            if (result != null) {
                result.setLocation(x*a/dLength, y*a/dLength);
            }
            return Math.abs(dLength - a);
        }
        // reflect x = -x if necessary, clamp to zero if necessary
        boolean bXReflect;
        if (x > epsilon) {
            bXReflect = false;
        }
        else if (x < -epsilon) {
            bXReflect = true;
            x = -x;
        }
        else {
            bXReflect = false;
            x = 0.0;
        }
        // reflect y = -y if necessary, clamp to zero if necessary
        boolean bYReflect;
        if (y > epsilon) {
            bYReflect = false;
        }
        else if (y < -epsilon) {
            bYReflect = true;
            y = -y;
        }
        else {
            bYReflect = false;
            y = 0.0;
        }
        // transpose if necessary
        double dSave;
        boolean bTranspose;
        if (a >= b) {
            bTranspose = false;
        }
        else {
            bTranspose = true;
            dSave = a;
            a = b;
            b = dSave;
            
            dSave = x;
            x = y;
            y = dSave;
        }
        double dDistance;
        if (x != 0.0) {
            if (y != 0.0) {
                dDistance = DistancePointEllipseSpecial(
                        x,
                        y,
                        a,
                        b,
                        epsilon,
                        maxIter,
                        result);
            }
            else {
                double dBSqr = b * b;
                if (x < (a - dBSqr / a)) {
                    double dASqr = a * a;
                    double rdX = dASqr * x / (dASqr - dBSqr);
                    double dXDivA = rdX / a;
                    double rdY = b * Math.sqrt(Math.abs(1.0 - dXDivA * dXDivA));
                    double dXDelta = rdX - x;
                    dDistance = Math.hypot(dXDelta, rdY);
                    //riIFinal = 0;
                    if (result != null) {
                        result.setLocation(rdX, rdY);
                    }
                }
                else {
                    dDistance = Math.abs(x - a);
                    if (result != null) {
                        result.setLocation(a, 0.0);
                    }
                    //riIFinal = 0;
                }
            }
        }
        else {
            dDistance = Math.abs(y - b);
            if (result != null) {
                result.setLocation(0.0, b);
            }
//            riIFinal = 0;
        }
        if (bTranspose && result!=null) {
            result.setLocation(result.getY(), result.getX());
        }
        if (bYReflect && result != null) {
            result.setLocation(result.getX(), -result.getY());
        }

        if (bXReflect && result != null) {
            result.setLocation(-result.getX(), result.getY());
        }
        return dDistance;
    }

    /**
     * Computes the variance of the samples.
     * 
     * @param points
     *            the sample points
     * @param params
     *            the conic parameters or null if they have to be
     *            computed/fitted
     * @param ell
     *            the elliptical parameters of null if they have to be computed
     * @return the variance
     */
    public static double computeVariance(
            Point2D[] points,
            EllipseParams ell) {
        return computeVariance(points, 0, points.length, ell);
    }

    /**
     * Computes the variance of the samples starting at the specified offset and
     * with the specified length.
     * 
     * @param points
     *            the sample points
     * @param off
     *            the offset
     * @param len
     *            the length
     * @param params
     *            the conic parameters or null if they have to be
     *            computed/fitted
     * @param ell
     *            the elliptical parameters of null if they have to be computed
     * @return the variance
     */
    public static double computeVariance(
            Point2D[] points,
            int off,
            int len,
            EllipseParams ell) {
        if (ell == null) {
            ell = fitParametric(points);
            if (ell == null) 
                return Double.NaN;
        }

        double sum = 0;
        for (int i = off; i < (off + len); i++) {
            Point2D point = points[i];
            sum += sqr(ell.computeDistance(point));
        }
        return sum / (len - 1);
    }

    /**
     * Fits an ellipse on the specified points.
     * 
     * <p>
     * Fitting means computing Axx, Axy, Ayy, Ax, Ay and Ao such that Axx * x^2 +
     * Axy * x * y + Ayy * y^2 + Ax * x + Ay * y + A0 = 0
     * 
     * @param points
     *            the vector of points
     * @return a table of 7 values: Axx at index 0 Axy at index 1 Ayy at index 2
     *         Ax at index 3 Ay at index 4 Ao at index 5 or null if the
     *         parameters cannot be computed.
     */
    public static ImplicitParams fitImplicit(Point2D[] points) {
        return fitImplicit(points, 0, points.length);
    }
    
    static final double Const[][] = new double[7][7];
    static final double S[][] = new double[7][7];
    static final double L[][] = new double[7][7];
    static final double invL[][] = new double[7][7];
    static final double temp[][] = new double[7][7];
    static final double D2[] = new double[7];
    static final double V[][] = new double[7][7];
    static final double C[][] = new double[7][7];
    static final double sol[][] = new double[7][7];
    static double[][] D;

    static {
        Const[1][3] = -2;
        Const[2][2] = 1;
        Const[3][1] = -2;
    }

    /**
     * Fits an ellipse on the specified points.
     * 
     * <p>
     * Fitting means computing Axx, Axy, Ayy, Ax, Ay and Ao such that Axx * x^2 +
     * Axy * x * y + Ayy * y^2 + Ax * x + Ay * y + A0 = 0
     * 
     * @param points
     *            the vector of points
     * @param off
     *            the offset of the first point to consider
     * @param len
     *            the number of consecutive points to consider
     * @return a table of 7 values: Axx at index 0 Axy at index 1 Ayy at index 2
     *         Ax at index 3 Ay at index 4 Ao at index 5 or null if the
     *         parameters cannot be computed.
     */
    public static ImplicitParams fitImplicit(Point2D[] points, int off, int len) {
        if (len < 6)
            return null;
        
        // Now first fill design matrix
        if (D == null || D.length < len+1) {
            D = new double[len + 1][7];
        }
        
        //double D[][] = 
        for (int i = 0; i < len; i++) {
            Point2D p = points[i + off];
//            if (p == null) continue;
            double tx = p.getX();
            double ty = p.getY();
            D[i+1][1] = tx * tx;
            D[i+1][2] = tx * ty;
            D[i+1][3] = ty * ty;
            D[i+1][4] = tx;
            D[i+1][5] = ty;
            D[i+1][6] = 1.0;
        }
        
        // pm(Const,"Constraint");
        // Now compute scatter matrix S
        A_TperB(D, D, S, len, 6, len, 6);
        // pm(S,"Scatter");
        choldc(S, 6, L);
        // pm(L,"Cholesky");
        
        if (inverse(L, invL, 6) == -1) {
            return null; // matrix is singular
        }
        // pm(invL,"inverse");
        AperB_T(Const, invL, temp, 6, 6, 6, 6);
        AperB(invL, temp, C, 6, 6, 6, 6);
        // pm(C,"The C matrix");
        jacobi(C, 6, D2, V, 0);
        
        // pm(V,"The Eigenvectors"); /* OK */
        // pv(d,"The eigevalues");

        A_TperB(invL, V, sol, 6, 6, 6, 6);
        // pm(sol,"The GEV solution unnormalized"); /* SOl */

        // Now normalize them
        for (int j = 1; j <= 6; j++) /* Scan columns */
        {
            double mod = 0.0;
            for (int i = 1; i <= 6; i++)
                mod += sol[i][j] * sol[i][j];
            mod = Math.sqrt(mod);
            for (int i = 1; i <= 6; i++)
                sol[i][j] /= mod;
        }

        // pm(sol,"The GEV solution"); /* SOl */

        double zero = 10e-20;
        // double minev=10e+20;
        int solind = 0;
        for (int i = 1; i <= 6; i++)
            if (D2[i] < 0 && Math.abs(D2[i]) > zero)
                solind = i;

        return new ImplicitParams(
                sol[1][solind],
                sol[2][solind],
                sol[3][solind],
                sol[4][solind],
                sol[5][solind],
                sol[6][solind]);
    }

    static double sqr(double a) {
        return a * a;
    }

    /**
     * Converts a parametric description of an ellipse into a [cx,cy,angle,a,b]
     * where cx and cy are the center, angle is the angle, a is the largest
     * semi-axe length and b is the other semi-axe length.
     * 
     * @param par
     *            the parameters as returned by fit: a table of 7 values: Axx at
     *            index 0 Axy at index 1 Ayy at index 2 Ax at index 3 Ay at
     *            index 4 Ao at index 5
     * @return [cx, cy, angle, a, b]
     */
    public static EllipseParams implicitToParametric(ImplicitParams par) {
        if (par == null) 
            return null;
        double angle = 0.5 * Math.atan2(par.b, par.a - par.c);
        double cost = Math.cos(angle);
        double sint = Math.sin(angle);
        double sin_squared = sint * sint;
        double cos_squared = cost * cost;
        double cos_sin = sint * cost;

        double Ao = par.f;
        double Au = par.d * cost + par.e * sint;
        double Av = -par.d * sint + par.e * cost;
        double Auu = par.a * cos_squared + par.c * sin_squared + par.b
                * cos_sin;
        double Avv = par.a * sin_squared + par.c * cos_squared - par.b
                * cos_sin;

        double tuCentre = -Au / (2 * Auu);
        double tvCentre = -Av / (2 * Avv);
        double wCentre = Ao - Auu * tuCentre * tuCentre - Avv * tvCentre
                * tvCentre;

        double cx = tuCentre * cost - tvCentre * sint;
        double cy = tuCentre * sint + tvCentre * cost;

        double Ru = -wCentre / Auu;
        double Rv = -wCentre / Avv;

        Ru = Math.sqrt(Math.abs(Ru)) * Math.signum(Ru);
        Rv = Math.sqrt(Math.abs(Rv)) * Math.signum(Rv);
        return new EllipseParams(cx, cy, angle, Ru, Rv);
    }

    /**
     * Computes the position at the specified parameter t for a specified
     * parameters.
     * 
     * @param ell
     *            the parameters
     * @param t
     *            the angle
     * @param p
     *            a point or null
     * @return the point or null
     */
    public static Point2D computePosition(EllipseParams ell, double t, Point2D p) {
        if (ell == null)
            return null;
        return ell.computePosition(t, p);
        // if (p == null) {
        // p = new Point2D.Double();
        // }
        // double cx = ell.cx;
        // double cy = ell.cy;
        // double angle= ell.angle;
        // double ap = ell.rX;
        // double bp = ell.rY;
        //        
        // double x = ap * Math.cos(t);
        // double y = bp * Math.sin(t);
        // double nx = x*Math.cos(angle)-y*Math.sin(angle) + cx;
        // double ny = x*Math.sin(angle)+y*Math.cos(angle) + cy;
        // p.setLocation(nx, ny);
        //        
        // return p;
    }

    private static void ROTATE(
            double a[][],
            int i,
            int j,
            int k,
            int l,
            double tau,
            double s) {
        double g, h;
        g = a[i][j];
        h = a[k][l];
        a[i][j] = g - s * (h + g * tau);
        a[k][l] = h + s * (g - h * tau);
    }
    
    static double tmpA[];
    static double tmpB[];

    private static void jacobi(
            double a[][],
            int n,
            double d[],
            double v[][],
            int nrot) {
        int j, iq, ip, i;
        double tresh, theta, tau, t, sm, s, h, g, c;

        if (tmpA == null || tmpA.length < n+1) {
            tmpA = new double[n + 1];
        }
        if (tmpB == null || tmpB.length < n+1) {
            tmpB = new double[n + 1];
        }

        for (ip = 1; ip <= n; ip++) {
            for (iq = 1; iq <= n; iq++)
                v[ip][iq] = 0.0;
            v[ip][ip] = 1.0;
        }
        for (ip = 1; ip <= n; ip++) {
            tmpA[ip] = d[ip] = a[ip][ip];
            tmpB[ip] = 0.0;
        }
        nrot = 0;
        for (i = 1; i <= 50; i++) {
            sm = 0.0;
            for (ip = 1; ip <= n - 1; ip++) {
                for (iq = ip + 1; iq <= n; iq++)
                    sm += Math.abs(a[ip][iq]);
            }
            if (sm == 0.0) {
                /*
                 * free_vector(z,1,n); free_vector(b,1,n);
                 */
                return;
            }
            if (i < 4)
                tresh = 0.2 * sm / (n * n);
            else
                tresh = 0.0;
            for (ip = 1; ip <= n - 1; ip++) {
                for (iq = ip + 1; iq <= n; iq++) {
                    g = 100.0 * Math.abs(a[ip][iq]);
                    if (i > 4 && Math.abs(d[ip]) + g == Math.abs(d[ip])
                            && Math.abs(d[iq]) + g == Math.abs(d[iq]))
                        a[ip][iq] = 0.0;
                    else if (Math.abs(a[ip][iq]) > tresh) {
                        h = d[iq] - d[ip];
                        if (Math.abs(h) + g == Math.abs(h))
                            t = (a[ip][iq]) / h;
                        else {
                            theta = 0.5 * h / (a[ip][iq]);
                            t = 1.0 / (Math.abs(theta) + Math.sqrt(1.0 + theta
                                    * theta));
                            if (theta < 0.0)
                                t = -t;
                        }
                        c = 1.0 / Math.sqrt(1 + t * t);
                        s = t * c;
                        tau = s / (1.0 + c);
                        h = t * a[ip][iq];
                        tmpB[ip] -= h;
                        tmpB[iq] += h;
                        d[ip] -= h;
                        d[iq] += h;
                        a[ip][iq] = 0.0;
                        for (j = 1; j <= ip - 1; j++) {
                            ROTATE(a, j, ip, j, iq, tau, s);
                        }
                        for (j = ip + 1; j <= iq - 1; j++) {
                            ROTATE(a, ip, j, j, iq, tau, s);
                        }
                        for (j = iq + 1; j <= n; j++) {
                            ROTATE(a, ip, j, iq, j, tau, s);
                        }
                        for (j = 1; j <= n; j++) {
                            ROTATE(v, j, ip, j, iq, tau, s);
                        }
                        ++nrot;
                    }
                }
            }
            for (ip = 1; ip <= n; ip++) {
                tmpA[ip] += tmpB[ip];
                d[ip] = tmpA[ip];
                tmpB[ip] = 0.0;
            }
        }
        // printf("Too many iterations in routine JACOBI");
    }

    // Perform the Cholesky decomposition
    // Return the lower triangular L such that L*L'=A
    private static void choldc(double a[][], int n, double l[][]) {
        int i, j, k;
        double sum;
        
        if (tmpA == null || tmpA.length < n+1) {
            tmpA = new double[n + 1];
        }

        for (i = 1; i <= n; i++) {
            for (j = i; j <= n; j++) {
                for (sum = a[i][j], k = i - 1; k >= 1; k--)
                    sum -= a[i][k] * a[j][k];
                if (i == j) {
                    if (sum <= 0.0)
                    // printf("\nA is not poitive definite!");
                    {
                    }
                    else
                        tmpA[i] = Math.sqrt(sum);
                }
                else {
                    a[j][i] = sum / tmpA[i];
                }
            }
        }
        for (i = 1; i <= n; i++)
            for (j = i; j <= n; j++)
                if (i == j)
                    l[i][i] = tmpA[i];
                else {
                    l[j][i] = a[j][i];
                    l[i][j] = 0.0;
                }
    }
    
    static double B[][];
    static double A[][];


    /** ***************************************************************** */
    /** Calcola la inversa della matrice B mettendo il risultato * */
    /** in InvB . Il metodo usato per l'inversione e' quello di * */
    /** Gauss-Jordan. N e' l'ordine della matrice . * */
    /** ritorna 0 se l'inversione corretta altrimenti ritorna * */
    /** SINGULAR . * */
    /** ***************************************************************** */
    static int inverse(double TB[][], double InvB[][], int n) {
        int k, i, j, p, q;
        double mult;
        double D, temp;
        double maxpivot;
        int npivot;
        
        if (B == null || B.length < n+1) {
            B = new double[n + 1][n + 2];
            A = new double[n + 1][2 * n + 2];
        }
        // double C[][] = new double[N + 1][N + 1];
        double eps = 10e-20;

        for (k = 1; k <= n; k++)
            for (j = 1; j <= n; j++)
                B[k][j] = TB[k][j];

        for (k = 1; k <= n; k++) {
            for (j = 1; j <= n + 1; j++)
                A[k][j] = B[k][j];
            for (j = n + 2; j <= 2 * n + 1; j++)
                A[k][j] = (float) 0;
            A[k][k - 1 + n + 2] = (float) 1;
        }
        for (k = 1; k <= n; k++) {
            maxpivot = Math.abs((double) A[k][k]);
            npivot = k;
            for (i = k; i <= n; i++)
                if (maxpivot < Math.abs((double) A[i][k])) {
                    maxpivot = Math.abs((double) A[i][k]);
                    npivot = i;
                }
            if (maxpivot >= eps) {
                if (npivot != k)
                    for (j = k; j <= 2 * n + 1; j++) {
                        temp = A[npivot][j];
                        A[npivot][j] = A[k][j];
                        A[k][j] = temp;
                    }
                ;
                D = A[k][k];
                for (j = 2 * n + 1; j >= k; j--)
                    A[k][j] = A[k][j] / D;
                for (i = 1; i <= n; i++) {
                    if (i != k) {
                        mult = A[i][k];
                        for (j = 2 * n + 1; j >= k; j--)
                            A[i][j] = A[i][j] - mult * A[k][j];
                    }
                }
            }
            else { // printf("\n The matrix may be singular !!") ;
                return (-1);
            }
            ;
        }
        /** Copia il risultato nella matrice InvB ** */
        for (k = 1, p = 1; k <= n; k++, p++)
            for (j = n + 2, q = 1; j <= 2 * n + 1; j++, q++)
                InvB[p][q] = A[k][j];
        return (0);
    } /* End of INVERSE */

    private static void AperB(
            double _A[][],
            double _B[][],
            double _res[][],
            int _righA,
            int _colA,
            int _righB,
            int _colB) {
        int p, q, l;
        for (p = 1; p <= _righA; p++)
            for (q = 1; q <= _colB; q++) {
                _res[p][q] = 0.0;
                for (l = 1; l <= _colA; l++)
                    _res[p][q] = _res[p][q] + _A[p][l] * _B[l][q];
            }
    }

    private static void A_TperB(
            double _A[][],
            double _B[][],
            double _res[][],
            int _righA,
            int _colA,
            int _righB,
            int _colB) {
        int p, q, l;
        for (p = 1; p <= _colA; p++)
            for (q = 1; q <= _colB; q++) {
                _res[p][q] = 0.0;
                for (l = 1; l <= _righA; l++)
                    _res[p][q] = _res[p][q] + _A[l][p] * _B[l][q];
            }
    }

    private static void AperB_T(
            double _A[][],
            double _B[][],
            double _res[][],
            int _righA,
            int _colA,
            int _righB,
            int _colB) {
        int p, q, l;
        for (p = 1; p <= _colA; p++)
            for (q = 1; q <= _colB; q++) {
                _res[p][q] = 0.0;
                for (l = 1; l <= _righA; l++)
                    _res[p][q] = _res[p][q] + _A[p][l] * _B[q][l];
            }
    }

    /**
     * Main program to test.
     * 
     * @param args
     *            not used yet
     */
    public static void main(String[] args) {
//        generateAndFit(2, 25, 0.4, 60, true);
//        generateAndFit(2, 25, 0.4, 60, false);
//        generateAndFit(1, 25, 0.4, 60, true);
//        generateAndFit(1, 25, 0.6, 45, true);
    }

//    static void generateAndFit(
//            double freq,
//            double amp,
//            double ratio,
//            double angle,
//            boolean cw) {
//        // Generate an elliptical motion
//        Motion motion = new Motion(freq, amp, ratio, angle * Math.PI / 180);
//        motion.setClockwise(cw);
//        System.out.println("Motion freq = " + freq);
//        System.out.println("Motion angle = " + angle);
//        System.out.println("Motion radii = " + amp + ", " + (amp * ratio));
//        System.out.println("Motion orientation = " + (cw ? "CW" : "CCW"));
//
//        int n = 1000;
//        Point2D[] signal = new Point2D[n];
//        Random rand = new Random(10);
//        double variance = 2;
//        for (int i = 0; i < n; i++) {
//            Point2D p = motion.getPosition(i * 10.0 / n);
//            p.setLocation(p.getX() + (rand.nextDouble() - 0.5) * variance, p
//                    .getY()
//                    + (rand.nextDouble() - 0.5) * variance);
//            signal[i] = p;
//        }
//        fitPoints(signal);
//
//    }

    static void fitPoints(Point2D[] signal) {
        long time = System.currentTimeMillis();
        ImplicitParams params = null;
        EllipseParams ellipse = null;
        double angle = 0;
        for (int i = 0; i < 1000; i++) {
          params = FitEllipse.fitImplicit(signal);
          
            ellipse = FitEllipse.implicitToParametric(params);
            angle = 180 * ellipse.angle / Math.PI;
            while (angle < 0) {
           		angle += 360;
           	}
           	while (angle > 360) {
           		angle -= 360;
           	}
            
        }
        long dt = System.currentTimeMillis() - time;
        double v = computeVariance(signal, ellipse);
        System.out.println("Fit center = " + ellipse.cx + ", " + ellipse.cy);
        System.out.println("Fit angle = " + angle);
        System.out.println("Fit radii = " + ellipse.rX + ",\t" + ellipse.rY);
        System.out.println("Variance = " + v);
        
        System.out.println("Elipse Angle = " + ellipse.angle);
            
    }
    
    /**
     * <b>FitException</b> is used when an exception
     * occurs during a fit operation.
     */
    public static class FitException extends RuntimeException {
        /**
         * Construct an excpetion with a specified message.
         * @param msg the message 
         */
        public FitException(String msg) {
            super(msg);
        }
    }

}
