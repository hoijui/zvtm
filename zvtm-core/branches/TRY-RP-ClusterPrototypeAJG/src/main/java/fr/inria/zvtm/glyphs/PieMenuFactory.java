/*   FILE: PieMenuFactory.java
 *   DATE OF CREATION:  Thu Aug 25 14:14:17 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Font;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VText;

public abstract class PieMenuFactory {
    
    static double ANGLE = 0.0;
    static long RADIUS = 100;
    static Color ITEM_FILLCOLOR = Color.white;
    static Color ITEM_BORDERCOLOR = Color.gray;
    static Color ITEM_SFILLCOLOR = Color.gray;
    static Color ITEM_SBORDERCOLOR = Color.black;
    static Color LABEL_COLOR = Color.black;
    static float TRANSLUCENCY = 1.0f;
    static double SENSIT_BOUNDING_RADIUS = 1.0f;
    static Font FONT = VText.getMainFont();
	static float RING_INNER_RATIO = 0.1f;

    /**Standard pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param animLength duration in ms of menu expansion animation
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     */
    public static PieMenu createPieMenu(String[] labels, int animLength, View v, VirtualSpaceManager vsm){
	return new PieMenuR(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, RING_INNER_RATIO, ANGLE,
			    ITEM_FILLCOLOR, ITEM_BORDERCOLOR, ITEM_SFILLCOLOR, ITEM_SBORDERCOLOR, LABEL_COLOR, TRANSLUCENCY,
			    animLength, SENSIT_BOUNDING_RADIUS, FONT);
    }

    /**Standard pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param animLength duration in ms of menu expansion animation
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     *@param itemColors color of each menu item (this array should have the same length as the labels array)
     *@param itembColors border color of each menu item (this array should have the same length as the labels array)
     *@param itemSColors color of each menu item when selected (this array should have the same length as the labels array)
     *@param itembSColors border color of each menu item when selected (this array should have the same length as the labels array)
     *@param labelColors  color of each menu item label (this array should have the same length as the labels array)
     */
    public static PieMenu createPieMenu(String[] labels, int animLength, View v, VirtualSpaceManager vsm,
					Color[] itemColors, Color[] itembColors, Color[] itemSColors,
					Color[] itembSColors, Color[] labelColors){
	return new PieMenuR(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, RING_INNER_RATIO, ANGLE,
			    itemColors, itembColors, itemSColors, itembSColors, labelColors, TRANSLUCENCY,
			    animLength, SENSIT_BOUNDING_RADIUS, FONT);
    }

    /**Standard pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
     *@param animLength duration in ms of menu expansion animation
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     */
    public static PieMenu createPieMenu(String[] labels, LongPoint[] labelOffsets, int animLength, View v, VirtualSpaceManager vsm){
	return new PieMenuR(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, RING_INNER_RATIO, ANGLE,
			    ITEM_FILLCOLOR, ITEM_BORDERCOLOR, ITEM_SFILLCOLOR, ITEM_SBORDERCOLOR, LABEL_COLOR, TRANSLUCENCY,
			    animLength, SENSIT_BOUNDING_RADIUS, FONT, labelOffsets);
    }

    /**Standard pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
     *@param animLength duration in ms of menu expansion animation
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     *@param itemColors color of each menu item (this array should have the same length as the labels array)
     *@param itembColors border color of each menu item (this array should have the same length as the labels array)
     *@param itemSColors color of each menu item when selected (this array should have the same length as the labels array)
     *@param itembSColors border color of each menu item when selected (this array should have the same length as the labels array)
     *@param labelColors  color of each menu item label (this array should have the same length as the labels array)
     */
    public static PieMenu createPieMenu(String[] labels, LongPoint[] labelOffsets, int animLength, View v, VirtualSpaceManager vsm,
					Color[] itemColors, Color[] itembColors, Color[] itemSColors,
					Color[] itembSColors, Color[] labelColors){
	return new PieMenuR(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, RING_INNER_RATIO, ANGLE,
			    itemColors, itembColors, itemSColors, itembSColors,labelColors, TRANSLUCENCY,
			    animLength, SENSIT_BOUNDING_RADIUS, FONT, labelOffsets);
    }


    /**Polygonal pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     */
    public static PieMenu createPolygonalPieMenu(String[] labels, View v, VirtualSpaceManager vsm){
	return new PieMenuP(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, ANGLE,
			    ITEM_FILLCOLOR, ITEM_BORDERCOLOR, ITEM_SFILLCOLOR, ITEM_SBORDERCOLOR, LABEL_COLOR, TRANSLUCENCY,
			    SENSIT_BOUNDING_RADIUS, FONT);
    }

    /**Polygonal pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     *@param itemColors color of each menu item (this array should have the same length as the labels array)
     *@param itembColors border color of each menu item (this array should have the same length as the labels array)
     *@param itemSColors color of each menu item when selected (this array should have the same length as the labels array)
     *@param itembSColors border color of each menu item when selected (this array should have the same length as the labels array)
     *@param labelColors  color of each menu item label (this array should have the same length as the labels array)
     */
    public static PieMenu createPolygonalPieMenu(String[] labels, View v, VirtualSpaceManager vsm,
						 Color[] itemColors, Color[] itembColors, Color[] itemSColors,
						 Color[] itembSColors, Color[] labelColors){
	return new PieMenuP(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, ANGLE,
			    itemColors, itembColors, itemSColors, itembSColors, labelColors, TRANSLUCENCY,
			    SENSIT_BOUNDING_RADIUS, FONT);
    }

    /**Polygonal pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     */
    public static PieMenu createPolygonalPieMenu(String[] labels, LongPoint[] labelOffsets, View v, VirtualSpaceManager vsm){
	return new PieMenuP(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, ANGLE,
			    ITEM_FILLCOLOR, ITEM_BORDERCOLOR, ITEM_SFILLCOLOR, ITEM_SBORDERCOLOR, LABEL_COLOR, TRANSLUCENCY,
			    SENSIT_BOUNDING_RADIUS, FONT, labelOffsets);
    }

    /**Polygonal pie menu creation method.
     *
     * When calling this method, the layer associated with the
     * camera showing the pie menu in the view must already have been made active.
     *@param labels text label of each menu item
     *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
     *@param v View in which the pie menu will appear
     *@param vsm instance of VirtualSpaceManager
     *@param itemColors color of each menu item (this array should have the same length as the labels array)
     *@param itembColors border color of each menu item (this array should have the same length as the labels array)
     *@param itemSColors color of each menu item when selected (this array should have the same length as the labels array)
     *@param itembSColors border color of each menu item when selected (this array should have the same length as the labels array)
     *@param labelColors  color of each menu item label (this array should have the same length as the labels array)
     */
    public static PieMenu createPolygonalPieMenu(String[] labels, LongPoint[] labelOffsets, View v, VirtualSpaceManager vsm,
						 Color[] itemColors, Color[] itembColors, Color[] itemSColors,
						 Color[] itembSColors, Color[] labelColors){
	return new PieMenuP(labels, new LongPoint(v.mouse.vx, v.mouse.vy),
			    v.getActiveCamera().getOwningSpace().getName(), vsm, RADIUS, ANGLE,
			    itemColors, itembColors, itemSColors, itembSColors, labelColors, TRANSLUCENCY,
			    SENSIT_BOUNDING_RADIUS, FONT, labelOffsets);
    }

    /**set the radius (in virtual space units) of pie menus that will be created by calls to createPieMenu()*/
    public static void setRadius(long r){
	RADIUS = r;
    }

    /**set the angle offset of the first menu item of pie menus that will be created by calls to createPieMenu()
     *@param a the angle offset, interpreted relative to the X horizontal axis (counter clockwise)
     */
    public static void setAngle(double a){
	ANGLE = a;
    }

    /**set the fill color of pie menu items that will be created by calls to createPieMenu()*/
    public static void setItemFillColor(Color c){
	ITEM_FILLCOLOR = c;
    }

    /**set the border color of pie menu items that will be created by calls to createPieMenu()*/
    public static void setItemBorderColor(Color c){
	ITEM_BORDERCOLOR = c;
    }

    /**set the fill color of pie menu items that will be created by calls to createPieMenu() - null if same as unselected*/
    public static void setSelectedItemFillColor(Color c){
	ITEM_SFILLCOLOR = c;
    }

    /**set the border color of pie menu items that will be created by calls to createPieMenu() - null if same as unselected*/
    public static void setSelectedItemBorderColor(Color c){
	ITEM_SBORDERCOLOR = c;
    }

    /**set the color of pie menu labels that will be created by calls to createPieMenu()*/
    public static void setLabelColor(Color c){
	LABEL_COLOR = c;
    }

    /**set the translucency of pie menus that will be created by calls to createPieMenu()*/
    public static void setTranslucency(float a){
	TRANSLUCENCY = a;
    }

    /**set the bounding shape's radius (as a percentage of the menu's actual radius)*/
    public static void setSensitivityRadius(double p){
	SENSIT_BOUNDING_RADIUS = p;
    }

    /**set font used for menu labels*/
    public static void setFont(Font f){
	FONT = f;
    }

    public static long getRadius(){
	return RADIUS;
    }

    public static double getAngle(){
	return ANGLE;
    }

    public static Color getItemFillColor(){
	return ITEM_FILLCOLOR;
    }

    public static Color getItemBorderColor(){
	return ITEM_BORDERCOLOR;
    }

    public static Color getSelectedItemFillColor(){
	return ITEM_SFILLCOLOR;
    }

    public static Color getSelectedItemBorderColor(){
	return ITEM_SBORDERCOLOR;
    }

    public static Color getLabelColor(){
	return LABEL_COLOR;
    }

    public static float getTranslucency(){
	return TRANSLUCENCY;
    }

    public static double getSensitivityRadius(){
	return SENSIT_BOUNDING_RADIUS;
    }

    public static Font getFont(){
	return FONT;
    }
    
}
