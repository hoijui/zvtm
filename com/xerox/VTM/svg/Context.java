/*   FILE: Context.java
 *   DATE OF CREATION:   Thu Jan 16 17:24:56 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */

package com.xerox.VTM.svg;

import java.awt.Color;
import java.awt.Font;
import java.util.StringTokenizer;

/**context information that we might want to propagate while walking the SVG DOM tree*/

public class Context {

    String font_family;
    String font_size;
    String font_weight;
    String font_style;
    Color fill;
    Color stroke;
//     int stroke_width;
    Float fill_opacity;

    /*metadata associated with a group/link*/
    String url;
    String title;

    /*give it the value of a style attribute*/
    Context(){}

    /*give it the value of a style attribute*/
    Context(String s){
	if (s!=null){processStyleInfo(s);}
    }

    /*give it the value of a style attribute - any previously set value will be overwritten*/
    void add(String s){
	processStyleInfo(s);
    }

    void processStyleInfo(String styleInfo){
	String[] ar=null;
	if (styleInfo!=null){
	    StringTokenizer st=new StringTokenizer(styleInfo,";");
	    ar=new String[st.countTokens()];
	    int i=0;
	    while (st.hasMoreTokens()){
		ar[i++]=st.nextToken();
	    }
	}
	if (ar!=null){
	    for (int i=0;i<ar.length;i++){
		if (ar[i].startsWith(SVGReader._fill)){fill=SVGReader.getColor(ar[i].substring(SVGReader._fill.length(),ar[i].length()));}
		else if (ar[i].startsWith(SVGReader._stroke)){SVGReader.getColor(ar[i].substring(SVGReader._stroke.length(),ar[i].length()));}
		else if (ar[i].startsWith(SVGReader._fillopacity)){fill_opacity=new Float(ar[i].substring(SVGReader._fillopacity.length(),ar[i].length()));}
		else if (ar[i].startsWith(SVGReader._fontfamily)){font_family=ar[i].substring(SVGReader._fontfamily.length(),ar[i].length());}
		else if (ar[i].startsWith(SVGReader._fontsize)){font_size=ar[i].substring(SVGReader._fontsize.length(),ar[i].length());}
		else if (ar[i].startsWith(SVGReader._fontweight)){font_weight=ar[i].substring(SVGReader._fontweight.length(),ar[i].length());}
		else if (ar[i].startsWith(SVGReader._fontstyle)){font_style=ar[i].substring(SVGReader._fontstyle.length(),ar[i].length());}
	    }
	}
    }

    /**returns true if there is transparency information (the value does not matter)*/
    public boolean hasTransparencyInformation(){
	if (fill_opacity==null){return false;}
	else {return true;}
    }

    /**returns the fill (interior) color*/
    public Color getFillColor(){
	return fill;
    }

    /**returns the stroke (border) color*/
    public Color getBorderColor(){
	return stroke;
    }

    /**returns the alpha transparency value (1.0 if opaque, 0 is fully transparent)*/
    public float getAlphaTransparencyValue(){
	if (fill_opacity!=null){return fill_opacity.floatValue();}
	else return 1.0f;
    }

    /*returns a Font object if there is enough information to create one, null if not*/
    Font getDefinedFont(){
	if (font_family!=null || font_size!=null || font_style!=null || font_weight!=null){
	    String fam=(font_family!=null) ? font_family : "Default";
	    int size;
	    try {size=(font_size!=null) ? Math.round((new Float(font_size)).floatValue()) : 10;}
	    catch (NumberFormatException ex){System.err.println("Warning: Font size value not supported (using default): "+font_size);size=10;}
	    int style;
	    if (font_style!=null && font_style.equals("italic")){
		if (font_weight!=null && font_weight.equals("bold")){style=Font.BOLD+Font.ITALIC;}
		else {style=Font.ITALIC;}
	    }
	    else {
		if (font_weight!=null && font_weight.equals("bold")){style=Font.BOLD;}
		else {style=Font.PLAIN;}
	    }
	    return SVGReader.getFont(fam, style, size);
	}
	else {return null;}
    }

    public void setURL(String s){url=s;}

    /**null if none*/
    public String getURL(){return url;}

    public void setTitle(String s){title=s;}

    /**null if none*/
    public String getTitle(){return title;}

}
