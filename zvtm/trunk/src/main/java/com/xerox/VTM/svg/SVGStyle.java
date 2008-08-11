/*   FILE: SVGStyle.java
 *   DATE OF CREATION:   Jan 09 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */


package com.xerox.VTM.svg;

import java.awt.Color;
import java.awt.Font;
import java.util.StringTokenizer;

/**
 * A class to store style information relevant to the ZVTM
 */

public class SVGStyle {

    public static short CSS_FONT_WEIGHT_NORMAL=0;
    public static short CSS_FONT_WEIGHT_BOLD=1;
    public static short CSS_FONT_WEIGHT_BOLDER=2;
    public static short CSS_FONT_WEIGHT_LIGHTER=3;
    public static short CSS_FONT_WEIGHT_100=4;
    public static short CSS_FONT_WEIGHT_200=5;
    public static short CSS_FONT_WEIGHT_300=6;
    public static short CSS_FONT_WEIGHT_400=0;  //because according to the CSS spec normal=400
    public static short CSS_FONT_WEIGHT_500=7;
    public static short CSS_FONT_WEIGHT_600=8;
    public static short CSS_FONT_WEIGHT_700=1;  //because according to the CSS spec bold=700
    public static short CSS_FONT_WEIGHT_800=9;
    public static short CSS_FONT_WEIGHT_900=10;

    public static short CSS_FONT_STYLE_NORMAL=0;
    public static short CSS_FONT_STYLE_ITALIC=1;
    public static short CSS_FONT_STYLE_OBLIQUE=2;

    protected Color fillColor;   // fill color in ZVTM
    protected Color strokeColor; // border color in ZVTM
    protected boolean fillColorDefined = false;   // fillColor == null could have two meanings : it is not defined, or it is none
    protected boolean strokeColorDefined = false; // the boolean helps disambiguate these situations ; same thing for strokeColor
    protected Float strokeWidth;
    protected float[] strokeDashArray;
    protected Float alphaValue;  //transparency
    protected String font_family;
    protected String font_size;
    protected String font_weight;
    protected String font_style;
    
    public SVGStyle(){}

	/** Fill color, then stroke/border color. */
	public SVGStyle(Color c1, Color c2){
		fillColor=c1;
		strokeColor=c2;
		fillColorDefined = (fillColor != null);
		strokeColorDefined = (strokeColor != null);
	}

	/** Fill color, then stroke/border color, then transparency. */
	public SVGStyle(Color c1,Color c2,Float a){
		fillColor=c1;
		strokeColor=c2;
		alphaValue=a;
		fillColorDefined = (fillColor != null);
		strokeColorDefined = (strokeColor != null);
	}

    /**returns true if there is transparency information (the value does not matter)*/
    public boolean hasTransparencyInformation(){
	if (alphaValue==null){return false;}
	else {return true;}
    }

    /**set the fill (interior) color*/
    public void setFillColor(Color c){
	fillColor=c;
	fillColorDefined = true;
    }

    /**returns the fill (interior) color*/
    public Color getFillColor(){
	return fillColor;
    }

    /**Tells whether there is stroke color information or not.*/
    public boolean hasFillColorInformation(){
	return fillColorDefined;
    }

	/** Set the stroke (border) color. */
	public void setStrokeColor(Color c){
		strokeColor = c;
		strokeColorDefined = true;
	}

	/** Set the stroke (border) color.
		*@deprecated Since 0.9.7
		*@see #hasStrokeColorInformation
		*/
	public void setBorderColor(Color c){
		setStrokeColor(c);
	}

	/** Returns the stroke color. */
	public Color getStrokeColor(){
		return strokeColor;
	}

	/** Returns the stroke (border) color. 
		*@deprecated Since 0.9.7
		*@see #getStrokeColor()
	*/
	public Color getBorderColor(){
		return getStrokeColor();
	}

    /**Tells whether there is stroke color information or not.*/
    public boolean hasStrokeColorInformation(){
		return strokeColorDefined;
    }

    /**Tells whether there is stroke color information or not.
		*@deprecated Since 0.9.7
		*@see #hasStrokeColorInformation()
		*/
    public boolean hasBorderColorInformation(){
		return hasStrokeColorInformation();
    }

    /**set the transparency value (between 0 (fully transparent) and 1.0 (opaque))*/
    public void setAlphaTransparencyValue(Float f){
	alphaValue=f;
    }

    /**returns the alpha transparency value (1.0 if opaque, 0 is fully transparent)*/
    public float getAlphaTransparencyValue(){
	if (alphaValue!=null){return alphaValue.floatValue();}
	else return 1.0f;
    }

    /**
     *@see <a href="http://www.w3.org/TR/SVG11/painting.html#StrokeProperties">SVG stroke properties</a>
     */
    public void setStrokeWidth(String width){
	if (width != null && width.length() > 0){
	    if (width.endsWith("px")){
		width = width.substring(0, width.length()-2);
	    }
	    try {
		strokeWidth = new Float(width);
		if (strokeWidth.floatValue() == 1.0f){strokeWidth = null;}
	    }
	    catch (NumberFormatException ex){strokeWidth = null;}
	}
	else {
	    strokeWidth = null;
	}
    }

    public Float getStrokeWidth(){
	return strokeWidth;
    }

    /**
     *@see <a href="http://www.w3.org/TR/SVG11/painting.html#StrokeProperties">SVG stroke properties</a>
     */
    public void setStrokeDashArray(String dashArray){
	if (dashArray != null && !dashArray.equals("none")){
	    StringTokenizer st = new StringTokenizer(dashArray, ",");
	    strokeDashArray = new float[st.countTokens()];
	    int i = 0;
	    String s = null;
	    while (st.hasMoreTokens()){
		try {
		    s = st.nextToken();
		    if (s.endsWith("px")){s = s.substring(0, s.length()-2);}
		    strokeDashArray[i++] = Float.parseFloat(s);
		}
		catch (NumberFormatException ex){
		    strokeDashArray[i-1] = 1.0f;
		    System.err.println("Style: Error while parsing a stroke dash array: "+s+" is not a positive float value");
		}
	    }
	    //check the array
	    if (strokeDashArray.length == 0){strokeDashArray = null;}
	    else {
		if (strokeDashArray.length % 2 != 0){
		    // as stated in http://www.w3.org/TR/SVG11/painting.html#StrokeProperties :
		    // if an odd  number of values is provided, then the list of values is
		    // repeated to yield an even number of values
		    float[] tmpArray = new float[strokeDashArray.length * 2];
		    System.arraycopy(strokeDashArray,0,tmpArray,0,strokeDashArray.length);
		    System.arraycopy(strokeDashArray,0,tmpArray,strokeDashArray.length,strokeDashArray.length);
		    strokeDashArray = tmpArray;
		}
		boolean nonZero = false;
		for (int j=0;j<strokeDashArray.length;j++){
		    if (strokeDashArray[j] != 0.0f){
			nonZero = true;
			break;
		    }
		}
		if (!nonZero){strokeDashArray = null;}
	    }
	}
	else {
	    strokeDashArray = null;
	}
    }

    //null if not set
    public float[] getStrokeDashArray(){
	return strokeDashArray;
    }

    public boolean requiresSpecialStroke(){
	return (strokeWidth != null || strokeDashArray != null);
    }

    public void setFontFamily(String family){
	this.font_family = family;
    }

    public void setFontSize(String size){
	this.font_size = size;
	if (font_size != null && font_size.endsWith(SVGReader._pt)){font_size = font_size.substring(0, font_size.length()-2);}
    }

    public void setFontWeight(String weight){
	this.font_weight = weight;
    }

    public void setFontStyle(String style){
	this.font_style = style;
    }

    /*returns a Font object if there is enough information to create one, null if not*/
    Font getDefinedFont(Context ctx){
	String fam = (font_family!=null) ? font_family : ((ctx!=null) ? ctx.font_family : null);
	if (fam == null){
	    fam = "Default";
	}
	int size;
	if (font_size != null){
	    try {
		size = Math.round(Float.parseFloat(font_size));
	    }
	    catch (NumberFormatException ex){
		System.err.println("Warning: Font size value not supported (using default): " + font_size);
		size = 10;
	    }
	}
	else if (ctx != null && ctx.font_size != null){
	    try {
		size = Math.round(Float.parseFloat(ctx.font_size));
	    }
	    catch (NumberFormatException ex){
		System.err.println("Warning: Font size value not supported (using default): " + ctx.font_size);
		size = 10;
	    }
	}
	else {
	    size = 10;
	}
	String rfont_style = (font_style != null) ? font_style : ((ctx != null) ? ctx.font_style : null);
	String rfont_weight = (font_weight != null) ? font_weight : ((ctx != null) ? ctx.font_weight : null);
	int style;
	if (rfont_style != null && rfont_style.equals("italic")){
	    if (rfont_weight != null && rfont_weight.equals("bold")){style = Font.BOLD + Font.ITALIC;}
	    else {style = Font.ITALIC;}
	}
	else {
	    if (rfont_weight != null && rfont_weight.equals("bold")){style = Font.BOLD;}
	    else {style = Font.PLAIN;}
	}
	return SVGReader.getFont(fam, style, size);
    }


}
