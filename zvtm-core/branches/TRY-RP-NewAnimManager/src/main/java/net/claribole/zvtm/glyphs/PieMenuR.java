/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
*   Copyright (c) INRIA, 2008-2009. All Rights Reserved
*   Licensed under the GNU LGPL. For full terms see the file COPYING.
*
* $Id$
*/

package net.claribole.zvtm.glyphs;

import java.awt.Color;
import java.awt.Font;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VCircle;
import net.claribole.zvtm.glyphs.VRing;
import net.claribole.zvtm.glyphs.VRingST;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTextOr;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.interpolation.IdentityInterpolator;

public class PieMenuR extends PieMenu {

	public static final int animStartSize = 5;
	
    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param irr Inner ring boundary radius as a percentage of outer ring boundary radius
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColor menu items' fill color
        *@param borderColor menu items' border color
        *@param fillSColor menu items' fill color, when selected<br>can be null if color should not change
        *@param borderSColor menu items' border color, when selected<br>can be null if color should not change
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param animDuration duration in ms of animation creating the menu (expansion) - 0 for instantaneous display
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        */
    public PieMenuR(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, float irr, double startAngle,
                    Color fillColor, Color borderColor, Color fillSColor, Color borderSColor, Color labelColor, float alphaT,
                    int animDuration, double sensitRadius, Font font){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        items = new VRing[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            angle += angleDelta;
            if (alphaT >= 1.0f){
                items[i] = new VRing(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColor, borderColor);
            }
            else {
                items[i] = new VRingST(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColor, borderColor, alphaT);
            }
            items[i].setMouseInsideFillColor(fillSColor);
            items[i].setMouseInsideHighlightColor(borderSColor);
            vsm.addGlyph(items[i], vs, false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle ;
                    if (angle > Utilities.HALF_PI){
                        if (angle >= Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                            //else {textAngle +=Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2),
                        0, labelColor, stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2),
                        0, labelColor, stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);
                }
                labels[i].setSpecialFont(font);
                labels[i].setSensitivity(false);
                vsm.addGlyph(labels[i], vs);
            }
        }
        if (animDuration > 0){
            for (int i=0;i<items.length;i++){
              	Animation sizeAnim = vsm.getAnimationManager().getAnimationFactory()
		    .createGlyphSizeAnim(animDuration, items[i],
					 (float)pieMenuRadius,
					 false,
					 IdentityInterpolator.getInstance(),
					 null);
		vsm.getAnimationManager().startAnimation(sizeAnim, false);
            }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vsm.addGlyph(boundary, vs);
        vs.atBottom(boundary);
    }

    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param irr Inner ring boundary radius as a percentage of outer ring boundary radius
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColors menu items' fill colors (this array should have the same length as the stringLabels array)
        *@param borderColors menu items' border colors (this array should have the same length as the stringLabels array)
        *@param fillSColors menu items' fill colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param borderSColors menu items' border colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param animDuration duration in ms of animation creating the menu (expansion) - 0 for instantaneous display
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        */
    public PieMenuR(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, float irr, double startAngle,
                    Color[] fillColors, Color[] borderColors, Color[] fillSColors, Color[] borderSColors, Color[] labelColors, float alphaT,
                    int animDuration, double sensitRadius, Font font){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        items = new VRing[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            angle += angleDelta;
            if (alphaT >= 1.0f){
                items[i] = new VRing(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColors[i], borderColors[i]);
            }
            else {
                items[i] = new VRingST(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColors[i], borderColors[i], alphaT);
            }
            items[i].setMouseInsideFillColor(fillSColors[i]);
            items[i].setMouseInsideHighlightColor(borderSColors[i]);
            vsm.addGlyph(items[i], vs, false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle ;
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2),
                        0, labelColors[i], stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2),
                        0, labelColors[i], stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);
                }
                labels[i].setBorderColor(borderColors[i]);
                labels[i].setSpecialFont(font);
                labels[i].setSensitivity(false);
                vsm.addGlyph(labels[i], vs);
            }
        }
        if (animDuration > 0){
            for (int i=0;i<items.length;i++){
		Animation sizeAnim = vsm.getAnimationManager().getAnimationFactory()
		    .createGlyphSizeAnim(animDuration, items[i],
					 (float)pieMenuRadius,
					 false,
					 IdentityInterpolator.getInstance(),
					 null);
		vsm.getAnimationManager().startAnimation(sizeAnim, false);
	    }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vsm.addGlyph(boundary, vs);
        vs.atBottom(boundary);
    }

    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param irr Inner ring boundary radius as a percentage of outer ring boundary radius
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColor menu items' fill color
        *@param borderColor menu items' border color
        *@param fillSColor menu items' fill color, when selected<br>can be null if color should not change
        *@param borderSColor menu items' border color, when selected<br>can be null if color should not change
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param animDuration duration in ms of animation creating the menu (expansion) - 0 for instantaneous display
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
        */
    public PieMenuR(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, float irr, double startAngle,
                    Color fillColor, Color borderColor, Color fillSColor, Color borderSColor, Color labelColor, float alphaT,
                    int animDuration, double sensitRadius, Font font, LongPoint[] labelOffsets){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        items = new VRing[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            angle += angleDelta;
            if (alphaT >= 1.0f){
                items[i] = new VRing(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColor, borderColor);
            }
            else {
                items[i] = new VRingST(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColor, borderColor, alphaT);
            }
            items[i].setMouseInsideFillColor(fillSColor);
            items[i].setMouseInsideHighlightColor(borderSColor);
            vsm.addGlyph(items[i], vs, false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle ;
                    if (angle > Utilities.HALF_PI){
                        if (angle >= Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                            //else {textAngle +=Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2 + labelOffsets[i].x),
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2 + labelOffsets[i].y),
                        0, labelColor, stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2 + labelOffsets[i].x),
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2 + labelOffsets[i].y),
                        0, labelColor, stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);                    
                }
                labels[i].setSpecialFont(font);
                labels[i].setSensitivity(false);
                vsm.addGlyph(labels[i], vs);
            }
        }
        if (animDuration > 0){
            for (int i=0;i<items.length;i++){
		Animation sizeAnim = vsm.getAnimationManager().getAnimationFactory()
		    .createGlyphSizeAnim(animDuration, items[i],
					 (float)pieMenuRadius,
					 false,
					 IdentityInterpolator.getInstance(),
					 null);
		vsm.getAnimationManager().startAnimation(sizeAnim, false);
	    }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vsm.addGlyph(boundary, vs);
        vs.atBottom(boundary);
    }

    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param irr Inner ring boundary radius as a percentage of outer ring boundary radius
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColors menu items' fill colors (this array should have the same length as the stringLabels array)
        *@param borderColors menu items' border colors (this array should have the same length as the stringLabels array)
        *@param fillSColors menu items' fill colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param borderSColors menu items' border colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param animDuration duration in ms of animation creating the menu (expansion) - 0 for instantaneous display
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
        */
    public PieMenuR(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, float irr, double startAngle,
                    Color[] fillColors, Color[] borderColors, Color[] fillSColors, Color[] borderSColors, Color[] labelColors, float alphaT,
                    int animDuration, double sensitRadius, Font font, LongPoint[] labelOffsets){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        items = new VRing[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            angle += angleDelta;
            if (alphaT >= 1.0f){
                items[i] = new VRing(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColors[i], borderColors[i]);
            }
            else {
                items[i] = new VRingST(vx, vy, 0, (animDuration > 0) ? animStartSize : pieMenuRadius, angleDelta, irr, angle, fillColors[i], borderColors[i], alphaT);
            }
            items[i].setMouseInsideFillColor(fillSColors[i]);
            items[i].setMouseInsideHighlightColor(borderSColors[i]);
            vsm.addGlyph(items[i], vs, false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle ;
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2) + labelOffsets[i].x,
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2) + labelOffsets[i].y,
                        0, labelColors[i], stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle)*pieMenuRadius/2) + labelOffsets[i].x,
                        Math.round(vy+Math.sin(angle)*pieMenuRadius/2) + labelOffsets[i].y,
                        0, labelColors[i], stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);
                }
                labels[i].setBorderColor(borderColors[i]);
                labels[i].setSpecialFont(font);
                labels[i].setSensitivity(false);
                vsm.addGlyph(labels[i], vs);
            }
        }
        if (animDuration > 0){
            for (int i=0;i<items.length;i++){
		Animation sizeAnim = vsm.getAnimationManager().getAnimationFactory()
		    .createGlyphSizeAnim(animDuration, items[i],
					 (float)pieMenuRadius,
					 false,
					 IdentityInterpolator.getInstance(),
					 null);
		vsm.getAnimationManager().startAnimation(sizeAnim, false);
            }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vsm.addGlyph(boundary, vs);
        vs.atBottom(boundary);
    }

}
