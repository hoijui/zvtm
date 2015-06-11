/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZuistPicker.java 5510 2015-04-27 07:58:17Z epietrig $
 */

package fr.inria.zuist.engine;

import java.awt.geom.Point2D;

public abstract class ZuistPicker {

    double vx, vy;
    int topLevel;
    int bottomLevel;

    SceneManager sm;

    /** Set picker's coordinates (virtual space coordinates system).
     *@param x x-coordinate, in virtual space coordinates system
     *@param y y-coordinate, in virtual space coordinates system
     */
    public void setVSCoordinates(double x, double y){
        vx = x;
        vy = y;
    }

    /** Get picker's coordinates.
     *@return coordinates in the virtual space coordinates system.
     */
    public Point2D.Double getVSCoordinates(Point2D.Double res){
        res.setLocation(vx, vy);
        return res;
    }

    /** Get the picker's level range, i.e., the levels at which the picker is looking for candidates.
     * A higher level has a lower index. Both levels should bl >= 0. tl should be equal to or less than bl (but this is not checked).
     *@param tl top level
     *@param bl bottom level
     */
    public void setLevelRange(int tl, int bl){
        this.topLevel = (tl > 0) ? tl : 0;
        this.bottomLevel = (bl > 0) ? bl : 0;
    }

    /** Get the picker's level range, i.e., the levels at which the picker is looking for candidates.
     *@return {top level, bottom level}. A higher level has a lower index.
     */
    public int[] getLevelRange(){
        return new int[]{topLevel, bottomLevel};
    }

}
