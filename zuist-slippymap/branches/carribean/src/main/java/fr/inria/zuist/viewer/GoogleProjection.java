/*   Copyright (c) INRIA, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: GoogleProjection.java 15 2015-06-10 16:28:04Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.awt.geom.Point2D;

/* Inspired by the following code from osm2zuist.py derived from the Mapnik renderer */

// class GoogleProjection:
//     def __init__(self,levels=18):
//         self.Bc = []
//         self.Cc = []
//         self.zc = []
//         self.Ac = []
//         c = TS_I
//         for d in range(0,levels):
//             e = c/2;
//             self.Bc.append(c/360.0)
//             self.Cc.append(c/(2 * math.pi))
//             self.zc.append((e,e))
//             self.Ac.append(c)
//             c *= 2

//     def fromLLtoPixel(self,ll,zoom):
//          d = self.zc[zoom]
//          e = round(d[0] + ll[0] * self.Bc[zoom])
//          f = minmax(math.sin(DEG_TO_RAD * ll[1]),-0.9999,0.9999)
//          g = round(d[1] + 0.5*math.log((1+f)/(1-f))*-self.Cc[zoom])
//          return (e,g)

//     def fromPixelToLL(self,px,zoom):
//          e = self.zc[zoom]
//          f = (px[0] - e[0])/self.Bc[zoom]
//          g = (px[1] - e[1])/-self.Cc[zoom]
//          h = RAD_TO_DEG * ( 2 * math.atan(math.exp(g)) - 0.5 * math.pi)
//          return (f,h)

class GoogleProjection {

    static final double DEG2RAD = Math.PI / 180d;
    static final double RAD2DEG = 180 / Math.PI;

    static final double TS = 256d;
    double ts = TS;

    int maxLevel = 12;

    double[] Bc;
    double[] Cc;
    double[] zc;
    double[] Ac;

    /**
     *@param ts should be the tile size of the corresponding ZUIST scene
     *@param maxLevel how many levels in the ZUIST scene
     */
    GoogleProjection(int ts, int maxLevel){
        this.ts = ts;
        this.maxLevel = maxLevel;
        Bc = new double[maxLevel+1];
        Cc = new double[maxLevel+1];
        zc = new double[maxLevel+1];
        Ac = new double[maxLevel+1];
        double c = this.ts;
        for (int i=0;i<maxLevel+1;i++){
            Bc[i] = c / 360d;
            Cc[i] = c / (2*Math.PI);
            zc[i] = c / 2d;
            Ac[i] = c;
            c *= 2;
        }
    }

    Point2D.Double fromLLToPixel(double lon, double lat, Point2D.Double res){
        double d = zc[maxLevel];
        double e = d + lat * Bc[maxLevel];
        double f = minmax(Math.sin(DEG2RAD * lon), -0.9999, 0.9999);
        double g = d + 0.5 * Math.log((1+f) / (1-f)) * (-Cc[maxLevel]);
        res.setLocation(e, -g);
        return res;
    }

    Point2D.Double fromLLToPixel(double lon, double lat){
        return fromLLToPixel(lon, lat, new Point2D.Double());
    }

    Point2D.Double fromPixelToLL(double x, double y, Point2D.Double res){
        double e = zc[maxLevel];
        double f = (x - e) / Bc[maxLevel];
        double g = (-y - e) / (-Cc[maxLevel]);
        double h = RAD2DEG * (2 * Math.atan(Math.exp(g)) - 0.5 * Math.PI);
        res.setLocation(f, h);
        return res;
    }

    Point2D.Double fromPixelToLL(double x, double y){
        return fromPixelToLL(x, y, new Point2D.Double());
    }

    static double minmax(double a, double b, double c){
        double d = Math.max(a, b);
        return Math.min(d, c);
    }

}
