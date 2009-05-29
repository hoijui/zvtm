/*   FILE: SVGWriter.java
 *   DATE OF CREATION:   Nov 19 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.BooleanOps;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VBoolShape;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.VDiamond;
import com.xerox.VTM.glyphs.VEllipse;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VPoint;
import com.xerox.VTM.glyphs.VPolygon;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VShape;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTriangle;
import net.claribole.zvtm.glyphs.DPath;

    /** A class to export the content of a virtual space as an SVG document. Support for all glyph classes defined in package com.xerox.VTM.glyphs, including transparency.
     *@author Emmanuel Pietriga
     */

public class SVGWriter {

    //serif
    private static String _serifff="serif";
    private static String _timesff="times";
    private static String _garamondff="garamond";
    private static String _minionff="minion";
    private static String _cyberbitff="cyberbit";
    private static String _georgiaff="georgia";
    //sans-serif
    private static String _sansff="sans";
    private static String _arialff="arial";
    private static String _trebuchetff="trebuchet";
    private static String _verdanaff="verdana";
    private static String _universff="univers";
    private static String _helveticaff="helvetica";
    private static String _tahomaff="tahoma";
    private static String _lucidaff="lucida";
    //monospace
    private static String _courierff="courier";
    private static String _monoff="mono";
    //cursive
    private static String _cursiveff="cursive";
    private static String _caflischff="caflisch";
    private static String _poeticaff="poetica";
    private static String _sanvitoff="sanvito";
    private static String _corsivaff="corsiva";
    //fantasy
    private static String _critterff="critter";
    private static String _fantasyff="fantasy";
    
    //stroke properties
    public static String _strokewidth="stroke-width";
    public static String _strokelinecap="stroke-linecap";
    public static String _strokelinejoin="stroke-linejoin";
    public static String _strokemiterlimit="stroke-miterlimit";
    public static String _strokedasharray="stroke-dasharray";
    public static String _strokedashoffset="stroke-dashoffset";

    public static String _strokecapbutt="butt";
    public static String _strokecapround="round";
    public static String _strokecapsquare="square";
    public static String _strokejoinbevel="bevel";
    public static String _strokejoinmiter="miter";
    public static String _strokejoinround="round";

    public static String _class = "class";

    public static float DEFAULT_MITER_LIMIT=4.0f;
    public static float DEFAULT_DASH_OFFSET=0.0f;

    /**the SVG namespace URI*/
    public static final String svgURI="http://www.w3.org/2000/svg";

    /**the xlink namespace URI*/
    public static final String xlinkURI="http://www.w3.org/1999/xlink";

    /**
     * returns all but last 2 characters of a string (All But Last 2 Chars) representing a float
     */
    static String abl2c(float f){
	String s=String.valueOf(f);
	return s.substring(0,s.length()-2);
    }

    long farWest=0;  //used to compute the translation of all glyphs so that they have positive coordinates
    long farNorth=0; //used to compute the translation of all glyphs so that they have positive coordinates

    Document svgDoc; //exported document
    File destination;
    File img_subdir; //subdirectory in which images will be stored

    Hashtable bitmapImages;  //used to remember bitmap images that have already been exported, so that they get exported once, and not as many times as they appear in the SVG

    public SVGWriter(){}

    /**Export the content of a virtual space as a DOM object (that can then be serialized as an XML document using the parser of your choice (returns null if any error occurs)
     *@param vs virtual space to be exported
     *@param di a DOMImplementation, like org.apache.xerces.dom.DOMImplementationImpl()
     *@param dest destination file to which the SVG is exported (SVGWriter does not serialize the SVG document, but needs this info in case there are bitmap images associated with your SVG document, so that they get output in a subdirectory whose name is based on the main SVG file name) - This is necessary ONLY if the exported VirtualSpace contains one or more instances of VImage (or any subclass). Otherwise, you can pass <i>null</i>. VImages get exported as separate PNG files referenced using <i>relative</i> URIs in the main SVG file. The scheme is as follows: for a main SVG file named abcd.svg, a directory named abcd_files will be created, and PNG images will be placed there, named zvtmXXX.png where XXX is a positive integer
     */
    public Document exportVirtualSpace(VirtualSpace vs,DOMImplementation di,File dest){
	destination=dest;
	img_subdir=null;
	bitmapImages=new Hashtable();
	if (di!=null){
	    svgDoc=di.createDocument(svgURI,"svg",null);
	    Element root=svgDoc.getDocumentElement();
	    svgDoc.appendChild(svgDoc.createComment(" Generated by ZVTM (Zoomable Visual Transformation Machine) v0.9.8 http://zvtm.sourceforge.net"));
	    long[] lurd=VirtualSpaceManager.findFarmostGlyphCoords(vs);
	    farWest=-lurd[0];
	    farNorth=lurd[1];
	    root.setAttribute("xmlns",svgURI);
	    root.setAttribute("xmlns:xlink",xlinkURI);
	    root.setAttribute(SVGReader._width,"800");
	    root.setAttribute(SVGReader._height,"600");
	    root.setAttribute("viewBox","0 0 "+String.valueOf(lurd[2]-lurd[0])+" "+String.valueOf(lurd[1]-lurd[3]));
	    Element mainGroup=svgDoc.createElementNS(svgURI,SVGReader._g);
	    mainGroup.setAttribute(SVGReader._style,createFontInformation(VText.getMainFont()));
	    root.appendChild(mainGroup);
	    Glyph[] visibleGlyphs=vs.getDrawingList();
	    Element el;
	    for (int i=0;i<visibleGlyphs.length;i++){
		el=processGlyph(visibleGlyphs[i]);
		if (el!=null){mainGroup.appendChild(el);}
	    }
	    bitmapImages.clear();
	    bitmapImages=null;
	    return svgDoc;
	}
	else return null;
    }

    /**Export the content of a virtual space as a DOM object (that can then be serialized as an XML document using the parser of your choice (returns null if any error occurs)
     *@param vs virtual space to be exported
     *@param di a DOMImplementation, like org.apache.xerces.dom.DOMImplementationImpl()
     *@param dest destination file to which the SVG is exported (SVGWriter does not serialize the SVG document, but needs this info in case there are bitmap images associated with your SVG document, so that they get output in a subdirectory whose name is based on the main SVG file name) - This is necessary ONLY if the exported VirtualSpace contains one or more instances of VImage (or any subclass). Otherwise, you can pass <i>null</i>. VImages get exported as separate PNG files referenced using <i>relative</i> URIs in the main SVG file. The scheme is as follows: for a main SVG file named abcd.svg, a directory named abcd_files will be created, and PNG images will be placed there, named zvtmXXX.png where XXX is a positive integer
     *@param epp object implementing the SVGWriterPostProcessing interface
     */
    public Document exportVirtualSpace(VirtualSpace vs,DOMImplementation di,File dest, SVGWriterPostProcessor epp){
	destination=dest;
	img_subdir=null;
	bitmapImages=new Hashtable();
	if (di!=null){
	    svgDoc=di.createDocument(svgURI,"svg",null);
	    Element root=svgDoc.getDocumentElement();
	    svgDoc.appendChild(svgDoc.createComment(" Generated by ZVTM (Zoomable Visual Transformation Machine) v0.9.1 http://zvtm.sourceforge.net"));
	    long[] lurd=VirtualSpaceManager.findFarmostGlyphCoords(vs);
	    farWest=-lurd[0];
	    farNorth=lurd[1];
	    root.setAttribute("xmlns",svgURI);
	    root.setAttribute("xmlns:xlink",xlinkURI);
	    root.setAttribute(SVGReader._width,"800");
	    root.setAttribute(SVGReader._height,"600");
	    root.setAttribute("viewBox","0 0 "+String.valueOf(lurd[2]-lurd[0])+" "+String.valueOf(lurd[1]-lurd[3]));
	    Element mainGroup=svgDoc.createElementNS(svgURI,SVGReader._g);
	    mainGroup.setAttribute(SVGReader._style,createFontInformation(VText.getMainFont()));
	    root.appendChild(mainGroup);
	    Glyph[] visibleGlyphs=vs.getDrawingList();
	    Element el;
	    for (int i=0;i<visibleGlyphs.length;i++){
		el=processGlyph(visibleGlyphs[i]);
		if (el!=null){
		    mainGroup.appendChild(el);
		    epp.newElementCreated(el, visibleGlyphs[i], svgDoc);
		}
	    }
	    bitmapImages.clear();
	    bitmapImages=null;
	    return svgDoc;
	}
	else return null;
    }

    private Element processGlyph(Glyph o){
	if (o.isVisible()){
	    if (o instanceof VEllipse){return createEllipse((VEllipse)o);}
	    else if (o instanceof VRectangle){return createRect((VRectangle)o);}
	    else if (o instanceof VPath){return createPath((VPath)o);}
	    else if (o instanceof DPath){return createPath((DPath)o);}
	    else if (o instanceof VText){return createText((VText)o);}
	    else if (o instanceof VTriangle){return createPolygon((VTriangle)o);}
	    else if (o instanceof VCircle){return createCircle((VCircle)o);}
	    else if (o instanceof VPolygon){return createPolygon((VPolygon)o);}
	    else if (o instanceof VDiamond){return createPolygon((VDiamond)o);}
	    else if (o instanceof VPoint){return createPoint((VPoint)o);}
	    else if (o instanceof VSegment){return createLine((VSegment)o);}
	    else if (o instanceof VShape){return createPolygon((VShape)o);}
	    else if (o instanceof VImage){return createImage((VImage)o);}
	    else if (o instanceof VBoolShape){return createPath((VBoolShape)o);}
	    else {System.err.println("There is currently no support for outputing this glyph as SVG: "+o);return null;}
	}
	else return null;
    }

    public static String getGenericFontFamily(Font f){
	String family=f.getFamily().toLowerCase();
	if (family.indexOf(_timesff)!=-1 || family.indexOf(_garamondff)!=-1 || family.indexOf(_minionff)!=-1 || family.indexOf(_cyberbitff)!=-1 || family.indexOf(_georgiaff)!=-1 || (family.indexOf(_serifff)!=-1 && family.indexOf(_sansff)==-1)){
	    return "serif";
	}
	else if (family.indexOf(_sansff)!=-1 || family.indexOf(_arialff)!=-1 || family.indexOf(_trebuchetff)!=-1 || family.indexOf(_verdanaff)!=-1 || family.indexOf(_universff)!=-1 || family.indexOf(_helveticaff)!=-1 || family.indexOf(_tahomaff)!=-1 || family.indexOf(_lucidaff)!=-1){
	    return "sans-serif";
	}
	else if (family.indexOf(_courierff)!=-1 || family.indexOf(_monoff)!=-1){
	    return "monospace";
	}
	else if (family.indexOf(_cursiveff)!=-1 || family.indexOf(_caflischff)!=-1 || family.indexOf(_poeticaff)!=-1 || family.indexOf(_sanvitoff)!=-1 || family.indexOf(_corsivaff)!=-1){
	    return "cursive";
	}
	else if (family.indexOf(_fantasyff)!=-1 || family.indexOf(_critterff)!=-1){
	    return "fantasy";
	}
	else {
	    return "serif";
	}
    }

    private String createFontInformation(Font f){
	return "font-family:"+f.getFamily()+","+getGenericFontFamily(f)+";font-style:"+((f.getStyle()==Font.ITALIC || f.getStyle()==Font.BOLD+Font.ITALIC) ? "italic" : "normal")+";font-weight:"+((f.getStyle()==Font.BOLD || f.getStyle()==Font.BOLD+Font.ITALIC) ? "bold" : "normal")+";font-size:"+Integer.toString(f.getSize());
    }

    //except for stroke color which is dealt with in shapeColors
    private void createStrokeInformation(Glyph g,Element e){
	if (g.getStroke()!=null){
	    BasicStroke bs=g.getStroke();
	    if (bs.getLineWidth()!=Glyph.DEFAULT_STROKE_WIDTH){
		e.setAttribute(SVGWriter._strokewidth,Float.toString(bs.getLineWidth()));
	    }
	    if (bs.getEndCap()!=BasicStroke.CAP_BUTT){
		e.setAttribute(SVGWriter._strokelinecap,(bs.getEndCap()==BasicStroke.CAP_SQUARE) ? _strokecapsquare : _strokecapround);
	    }
	    if (bs.getLineJoin()!=BasicStroke.JOIN_MITER){
		e.setAttribute(SVGWriter._strokelinejoin,(bs.getLineJoin()==BasicStroke.JOIN_BEVEL) ? _strokejoinbevel : _strokejoinround);
	    }
	    if (bs.getMiterLimit()!=SVGWriter.DEFAULT_MITER_LIMIT){
		e.setAttribute(SVGWriter._strokemiterlimit,Float.toString(bs.getMiterLimit()));
	    }
	    if (bs.getDashArray()!=null){
		e.setAttribute(SVGWriter._strokedasharray,SVGWriter.arrayOffloatAsCSStrings(bs.getDashArray()));
	    }
	    if (bs.getDashPhase()!=SVGWriter.DEFAULT_DASH_OFFSET){
		e.setAttribute(SVGWriter._strokedashoffset,Float.toString(bs.getDashPhase()));
	    }
	}
    }

    private String shapeColors(Glyph g){//returns style attributes for fill color and stroke color
	String res;
	Color fill=g.getColor();
	Color border = g.getBorderColor();
	if (g.isFilled()){res="fill:rgb("+fill.getRed()+","+fill.getGreen()+","+fill.getBlue()+")";}
	else {res="fill:none";}
	if (g.isBorderDrawn()){res+=";stroke:rgb("+border.getRed()+","+border.getGreen()+","+border.getBlue()+")";}
	else {res+=";stroke:none";}
	if (g instanceof Translucent){res+=";fill-opacity:"+String.valueOf(((Translucent)g).getTranslucencyValue());}
	return res;
    }

	private void createClassInforation(Glyph g, Element e){
		if (g.getType() != null){
			e.setAttribute(SVGWriter._class, g.getType());
		}
	}

//     private Element shapeText(Glyph g){
// 	Element text=svgDoc.createElementNS(svgURI,SVGReader._text);
// 	text.setAttribute(SVGReader._x,String.valueOf(g.vx+farWest));
// 	text.setAttribute(SVGReader._y,String.valueOf(-g.vy+farNorth));
// 	text.appendChild(svgDoc.createTextNode(g.getText()));
// 	Color c=g.getColorb();
// 	String color="stroke:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+");fill:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
// 	text.setAttribute("style","font-family:"+g.getFont().getFamily()+","+getGenericFontFamily(g.getFont())+";font-size:"+g.getFont().getSize()+";"+color);
// 	return text;
//     }

    private Element createGroup(){
	Element res=svgDoc.createElementNS(svgURI,SVGReader._g);
	return res;
    }

	private Element createEllipse(VEllipse e){
		Element shape=svgDoc.createElementNS(svgURI,SVGReader._ellipse);
		shape.setAttribute(SVGReader._cx,String.valueOf(e.vx+farWest));
		shape.setAttribute(SVGReader._cy,String.valueOf(-e.vy+farNorth));
		shape.setAttribute(SVGReader._rx,String.valueOf(e.getWidth()));
		shape.setAttribute(SVGReader._ry,String.valueOf(e.getHeight()));
		shape.setAttribute(SVGReader._style,shapeColors(e));
		if (e.getStroke()!=null){
			createStrokeInformation(e,shape);
		}
		createClassInforation(e, shape);
		return shape;
	}

	private Element createCircle(VCircle c){
		Element shape=svgDoc.createElementNS(svgURI,SVGReader._circle);
		shape.setAttribute(SVGReader._cx,String.valueOf(c.vx+farWest));
		shape.setAttribute(SVGReader._cy,String.valueOf(-c.vy+farNorth));
		shape.setAttribute(SVGReader._r,String.valueOf(Math.round(c.getSize())));
		shape.setAttribute(SVGReader._style,shapeColors(c));
		if (c.getStroke()!=null){
			createStrokeInformation(c,shape);
		}
		createClassInforation(c, shape);
		return shape;
	}

	private Element createRect(VRectangle r){
		Element shape;
		if (r.getOrient()==0){
			shape = svgDoc.createElementNS(svgURI,SVGReader._rect);
			shape.setAttribute(SVGReader._x,String.valueOf(r.vx-r.getWidth()+farWest));
			shape.setAttribute(SVGReader._y,String.valueOf(-r.vy-r.getHeight()+farNorth));
			shape.setAttribute(SVGReader._width,String.valueOf(2*r.getWidth()));
			shape.setAttribute(SVGReader._height,String.valueOf(2*r.getHeight()));
			shape.setAttribute(SVGReader._style,shapeColors(r));
		}
		else {
			shape = svgDoc.createElementNS(svgURI,SVGReader._polygon);
			double x1=-r.getWidth();
			double y1=-r.getHeight();
			double x2=r.getWidth();
			double y2=r.getHeight();
			long[] xcoords=new long[4];
			long[] ycoords=new long[4];
			xcoords[0]=Math.round((x2*Math.cos(Math.PI-r.getOrient())+y1*Math.sin(Math.PI-r.getOrient()))+r.vx)+farWest;
			xcoords[1]=Math.round((x1*Math.cos(Math.PI-r.getOrient())+y1*Math.sin(Math.PI-r.getOrient()))+r.vx)+farWest;
			xcoords[2]=Math.round((x1*Math.cos(Math.PI-r.getOrient())+y2*Math.sin(Math.PI-r.getOrient()))+r.vx)+farWest;
			xcoords[3]=Math.round((x2*Math.cos(Math.PI-r.getOrient())+y2*Math.sin(Math.PI-r.getOrient()))+r.vx)+farWest;
			ycoords[0]=-Math.round((y1*Math.cos(Math.PI-r.getOrient())-x2*Math.sin(Math.PI-r.getOrient()))+r.vy)+farNorth;
			ycoords[1]=-Math.round((y1*Math.cos(Math.PI-r.getOrient())-x1*Math.sin(Math.PI-r.getOrient()))+r.vy)+farNorth;
			ycoords[2]=-Math.round((y2*Math.cos(Math.PI-r.getOrient())-x1*Math.sin(Math.PI-r.getOrient()))+r.vy)+farNorth;
			ycoords[3]=-Math.round((y2*Math.cos(Math.PI-r.getOrient())-x2*Math.sin(Math.PI-r.getOrient()))+r.vy)+farNorth;
			shape.setAttribute(SVGReader._points,String.valueOf(xcoords[0])+","+String.valueOf(ycoords[0])+" "+String.valueOf(xcoords[1])+","+String.valueOf(ycoords[1])+" "+String.valueOf(xcoords[2])+","+String.valueOf(ycoords[2])+" "+String.valueOf(xcoords[3])+","+String.valueOf(ycoords[3]));
			shape.setAttribute(SVGReader._style,shapeColors(r));
		}
		if (r.getStroke() != null){
			createStrokeInformation(r, shape);
		}
		createClassInforation(r, shape);
		return shape;
	}

    public String getSVGPathCoordinates(VPath p){
		return getSVGPathCoordinates(p.getJava2DPathIterator());
	}

    public String getSVGPathCoordinates(DPath p){
		return getSVGPathCoordinates(p.getSVGPathIterator());
	}

	public String getSVGPathCoordinates(PathIterator pi){
		StringBuffer coords=new StringBuffer();
		float[] seg=new float[6];
		int type;
		//anything but M, L, Q, C since we want the first command to explicitely appear in any case
		char lastOp='Z';
		while (!pi.isDone()){
			//save the path as a sequence of instructions following the SVG model for "d" attributes
			type=pi.currentSegment(seg);
			switch (type){
				case java.awt.geom.PathIterator.SEG_MOVETO:{
					if (lastOp!='M'){coords.append('M');} else {coords.append(' ');}
					lastOp='M';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth));
					break;
				}
				case java.awt.geom.PathIterator.SEG_LINETO:{
					if (lastOp!='L'){coords.append('L');} else {coords.append(' ');}
					lastOp='L';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth));
					break;
				}
				case java.awt.geom.PathIterator.SEG_QUADTO:{
					if (lastOp!='Q'){coords.append('Q');} else {coords.append(' ');}
					lastOp='Q';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth)+" "+abl2c(seg[2]+farWest)+" "+abl2c(seg[3]+farNorth));
					break;
				}
				case java.awt.geom.PathIterator.SEG_CUBICTO:{
					if (lastOp!='C'){coords.append('C');} else {coords.append(' ');}
					lastOp='C';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth)+" "+abl2c(seg[2]+farWest)+" "+abl2c(seg[3]+farNorth)+" "+abl2c(seg[4]+farWest)+" "+abl2c(seg[5]+farNorth));
					break;
				}
			}
			pi.next();
		}
		return coords.toString();
	}

	private Element createPath(VPath p){
		Element path=svgDoc.createElementNS(svgURI,SVGReader._path);
		path.setAttribute(SVGReader._d,getSVGPathCoordinates(p));
		Color c=p.getColor();
		String color="stroke:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
		path.setAttribute(SVGReader._style,"fill:none;"+color);
		if (p.getStroke()!=null){
			createStrokeInformation(p,path);
		}
		createClassInforation(p, path);
		return path;
	}

	private Element createPath(DPath p){
		Element path = svgDoc.createElementNS(svgURI, SVGReader._path);
		path.setAttribute(SVGReader._d, getSVGPathCoordinates(p));
		Color c = p.getColor();
		String color = "stroke:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
		path.setAttribute(SVGReader._style, "fill:none;"+color);
		if (p.getStroke() != null){
			createStrokeInformation(p, path);
		}
		createClassInforation(p, path);
		return path;
	}

	private Element createText(VText t){
		Element text=svgDoc.createElementNS(svgURI,SVGReader._text);
		text.setAttribute(SVGReader._x,String.valueOf(t.vx+farWest));
		text.setAttribute(SVGReader._y,String.valueOf(-t.vy+farNorth));
		if (t.getTextAnchor()==VText.TEXT_ANCHOR_START){text.setAttribute(SVGReader._textanchor,"start");}
		else if (t.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){text.setAttribute(SVGReader._textanchor,"middle");}
		else if (t.getTextAnchor()==VText.TEXT_ANCHOR_END){text.setAttribute(SVGReader._textanchor,"end");}
		text.appendChild(svgDoc.createTextNode(t.getText()));
		Color c=t.getColor();
		//only fill, do not add stroke:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+") as it creates a wide stroke
		String style="fill:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
		if (t.usesSpecialFont()){style=createFontInformation(t.getFont())+";"+style;}
		text.setAttribute(SVGReader._style,style);
		createClassInforation(t, text);
		return text;
	}

	private Element createPolygon(VTriangle t){
		Element shape=svgDoc.createElementNS(svgURI,SVGReader._polygon);
		long[] xcoords=new long[3];
		long[] ycoords=new long[3];
		long halfEdge=Math.round(0.866f*t.getSize());
		long thirdHeight=Math.round(0.5f*t.getSize());
		xcoords[0]=Math.round(t.vx-t.getSize()*Math.sin(Math.PI-t.getOrient()))+farWest;
		xcoords[1]=Math.round(t.vx-halfEdge*Math.cos(Math.PI-t.getOrient())+thirdHeight*Math.sin(Math.PI-t.getOrient()))+farWest;
		xcoords[2]=Math.round(t.vx+halfEdge*Math.cos(Math.PI-t.getOrient())+thirdHeight*Math.sin(Math.PI-t.getOrient()))+farWest;
		ycoords[0]=-Math.round(t.vy-t.getSize()*Math.cos(Math.PI-t.getOrient()))+farNorth;
		ycoords[1]=-Math.round(t.vy+thirdHeight*Math.cos(Math.PI-t.getOrient())+halfEdge*Math.sin(Math.PI-t.getOrient()))+farNorth;
		ycoords[2]=-Math.round(t.vy+thirdHeight*Math.cos(Math.PI-t.getOrient())-halfEdge*Math.sin(Math.PI-t.getOrient()))+farNorth;
		shape.setAttribute(SVGReader._points,String.valueOf(xcoords[0])+","+String.valueOf(ycoords[0])+" "+String.valueOf(xcoords[1])+","+String.valueOf(ycoords[1])+" "+String.valueOf(xcoords[2])+","+String.valueOf(ycoords[2]));
		shape.setAttribute(SVGReader._style,shapeColors(t));
		if (t.getStroke()!=null){
			createStrokeInformation(t,shape);
		}
		createClassInforation(t, shape);
		return shape;
	}

	private Element createPolygon(VDiamond d){
		Element shape=svgDoc.createElementNS(svgURI,SVGReader._polygon);
		long[] xcoords=new long[4];
		long[] ycoords=new long[4];
		xcoords[0]=Math.round(d.vx+d.getSize()*Math.cos(Math.PI-d.getOrient()))+farWest;
		xcoords[1]=Math.round(d.vx-d.getSize()*Math.sin(Math.PI-d.getOrient()))+farWest;
		xcoords[2]=Math.round(d.vx-d.getSize()*Math.cos(Math.PI-d.getOrient()))+farWest;
		xcoords[3]=Math.round(d.vx+d.getSize()*Math.sin(Math.PI-d.getOrient()))+farWest;
		ycoords[0]=-Math.round(d.vy-d.getSize()*Math.sin(Math.PI-d.getOrient()))+farNorth;
		ycoords[1]=-Math.round(d.vy-d.getSize()*Math.cos(Math.PI-d.getOrient()))+farNorth;
		ycoords[2]=-Math.round(d.vy+d.getSize()*Math.sin(Math.PI-d.getOrient()))+farNorth;
		ycoords[3]=-Math.round(d.vy+d.getSize()*Math.cos(Math.PI-d.getOrient()))+farNorth;
		shape.setAttribute(SVGReader._points,String.valueOf(xcoords[0])+","+String.valueOf(ycoords[0])+" "+String.valueOf(xcoords[1])+","+String.valueOf(ycoords[1])+" "+String.valueOf(xcoords[2])+","+String.valueOf(ycoords[2])+" "+String.valueOf(xcoords[3])+","+String.valueOf(ycoords[3]));
		shape.setAttribute(SVGReader._style,shapeColors(d));
		if (d.getStroke()!=null){
			createStrokeInformation(d,shape);
		}
		createClassInforation(d, shape);
		return shape;
	}

	private Element createPoint(VPoint p){
		Element shape=svgDoc.createElementNS(svgURI,SVGReader._rect);
		shape.setAttribute(SVGReader._x,String.valueOf(p.vx+farWest));
		shape.setAttribute(SVGReader._y,String.valueOf(-p.vy+farNorth));
		shape.setAttribute(SVGReader._width,"1");
		shape.setAttribute(SVGReader._height,"1");
		Color c=p.getColor();
		shape.setAttribute(SVGReader._style,"stroke:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")");
		createClassInforation(p, shape);
		return shape;
	}

	private Element createLine(VSegment s){
		Element shape=svgDoc.createElementNS(svgURI,SVGReader._line);
		LongPoint[] endPoints = s.getEndPoints();
		shape.setAttribute("x1", String.valueOf(endPoints[0].x+farWest));
		shape.setAttribute("y1", String.valueOf(-endPoints[0].y+farNorth));
		shape.setAttribute("x2", String.valueOf(endPoints[1].x+farWest));
		shape.setAttribute("y2", String.valueOf(-endPoints[1].y+farNorth));
		Color c=s.getColor();
		shape.setAttribute(SVGReader._style,"stroke:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")");
		if (s.getStroke()!=null){
			createStrokeInformation(s,shape);
		}
		createClassInforation(s, shape);
		return shape;
	}

	private Element createPolygon(VShape s){
		Element shape=svgDoc.createElementNS(svgURI,SVGReader._polygon);
		float[] vertices=s.getVertices();
		double vertexAngle=-s.getOrient();
		long[] xcoords=new long[vertices.length];
		long[] ycoords=new long[vertices.length];
		for (int j=0;j<vertices.length;j++){
			xcoords[j]=Math.round(s.vx+s.getSize()*Math.cos(vertexAngle)*vertices[j])+farWest;
			ycoords[j]=-Math.round(s.vy-s.getSize()*Math.sin(vertexAngle)*vertices[j])+farNorth;
			vertexAngle-=2*Math.PI/vertices.length;
		}
		String coords="";
		for (int j=0;j<vertices.length-1;j++){
			coords+=String.valueOf(xcoords[j])+","+String.valueOf(ycoords[j])+" ";
		}//last point outside loop just to avoid white space char at end of attrib value
		coords+=String.valueOf(xcoords[vertices.length-1])+","+String.valueOf(ycoords[vertices.length-1]);
		shape.setAttribute(SVGReader._points,coords);
		shape.setAttribute(SVGReader._style,shapeColors(s));
		if (s.getStroke()!=null){
			createStrokeInformation(s,shape);
		}
		createClassInforation(s, shape);
		return shape;
	}

	private Element createPolygon(VPolygon p){
		Element polygon=svgDoc.createElementNS(svgURI,SVGReader._polygon);
		LongPoint[] vertices=p.getVertices();
		long[] xcoords=new long[vertices.length];
		long[] ycoords=new long[vertices.length];
		for (int j=0;j<vertices.length;j++){
			xcoords[j]=Math.round(p.vx+vertices[j].x)+farWest;
			ycoords[j]=-Math.round(p.vy+vertices[j].y)+farNorth;
		}
		String coords="";
		for (int j=0;j<vertices.length-1;j++){
			coords+=String.valueOf(xcoords[j])+","+String.valueOf(ycoords[j])+" ";
		}//last point outside loop just to avoid white space char at end of attrib value
		coords+=String.valueOf(xcoords[vertices.length-1])+","+String.valueOf(ycoords[vertices.length-1]);
		polygon.setAttribute(SVGReader._points,coords);
		polygon.setAttribute(SVGReader._style,shapeColors(p));
		if (p.getStroke()!=null){
			createStrokeInformation(p,polygon);
		}
		createClassInforation(p, polygon);
		return polygon;
	}

	/*
		We need to dump the image itself as an external PNG or JPEG
		file and reference it using the xlink:href attribute
		*/
	private Element createImage(VImage i){
		Element shape;
		try {
			shape=svgDoc.createElementNS(svgURI,SVGReader._image);
			shape.setAttribute(SVGReader._x,String.valueOf(i.vx-i.getWidth()+farWest));
			shape.setAttribute(SVGReader._y,String.valueOf(-i.vy-i.getHeight()+farNorth));
			shape.setAttribute(SVGReader._width,String.valueOf(2*i.getWidth()));
			shape.setAttribute(SVGReader._height,String.valueOf(2*i.getHeight()));
			Image im=i.getImage();
			ImageWriter writer=(ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
			File f=null;
			//create a subdirectory based on the main SVG file name, removing extension (probably .svg) and appending "_files"
			if (img_subdir==null || !img_subdir.exists() || !img_subdir.isDirectory()){
				String dirName=destination.getName();
				int lio;
				if ((lio=dirName.lastIndexOf("."))>0){dirName=dirName.substring(0,lio);}
				dirName+="_files";
				img_subdir=new File(destination.getParentFile(),dirName);
				img_subdir.mkdirs();
			}
			if (bitmapImages.containsKey(im)){
				f=(File)bitmapImages.get(im);
			}
			else {
				f=File.createTempFile("zvtm",".png",img_subdir);
				writer.setOutput(ImageIO.createImageOutputStream(f));
				BufferedImage bi=new BufferedImage(im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_ARGB);
				(bi.createGraphics()).drawImage(im,null,null);
				writer.write(bi);
				bitmapImages.put(im,f);
			}
			shape.setAttributeNS(xlinkURI,"xlink:href",img_subdir.getName()+"/"+f.getName());  //relative URI as the png files are supposed
			//to be in img_subdir w.r.t the SVG file
		}
		catch (Exception ex){
			shape=svgDoc.createElementNS(svgURI,SVGReader._rect);
			shape.setAttribute(SVGReader._x,String.valueOf(i.vx-i.getWidth()+farWest));
			shape.setAttribute(SVGReader._y,String.valueOf(-i.vy-i.getHeight()+farNorth));
			shape.setAttribute(SVGReader._width,String.valueOf(2*i.getWidth()));
			shape.setAttribute(SVGReader._height,String.valueOf(2*i.getHeight()));
			System.err.println("SVGWriter:An error occured while exporting "+i.toString()+" to PNG.\n"+ex);
			if (!Utilities.javaVersionIs140OrLater()){
				System.err.println("ZVTM/SVGWriter:Error: the Java Virtual Machine in use seems to be older than version 1.4.0 ; package javax.imageio is probably missing, which prevents generating bitmap files for representing VImage objects. Install a JVM version 1.4.0 or later if you want to use this functionality.");
			}
			ex.printStackTrace();
		}
		createClassInforation(i, shape);
		return shape;
	}

	//boolean shapes are saved as closed general paths.
	private Element createPath(VBoolShape s){
		Element path=svgDoc.createElementNS(svgURI,SVGReader._path);
		Area area;
		switch (s.getMainShapeType()) {
			case 1:{
				//ellipse
				area=new Area(new Ellipse2D.Float(s.vx-s.getWidth()/2,-s.vy-s.getHeight()/2,s.getWidth(),s.getHeight()));
				break;
			}
			case 2:{
				//rectangle
				area=new Area(new Rectangle2D.Float(s.vx-s.getWidth()/2,-s.vy-s.getHeight()/2,s.getWidth(),s.getHeight()));
				break;
			}
			default:{
				//ellipse as default
				area=new Area(new Ellipse2D.Float(s.vx-s.getWidth()/2,-s.vy-s.getHeight()/2,s.getWidth(),s.getHeight()));
			}
		}
		BooleanOps[] ops=s.getOperations();
		for (int j=0;j<ops.length;j++){
			switch (ops[j].opType) {
				case 1:{
					area.add(getJ2DShape(ops[j],s.vx,-s.vy));
					break;
				}
				case 2:{
					area.subtract(getJ2DShape(ops[j],s.vx,-s.vy));
					break;
				}
				case 3:{
					area.intersect(getJ2DShape(ops[j],s.vx,-s.vy));
					break;
				}
				case 4:{
					area.exclusiveOr(getJ2DShape(ops[j],s.vx,-s.vy));
					break;
				}
			}
		}
		StringBuffer coords=new StringBuffer();
		PathIterator pi=area.getPathIterator(null);
		float[] seg=new float[6];
		int type;
		char lastOp='Z';
		//anything but M, L, Q, C since we want the first command to explicitely appear in any case
		while (!pi.isDone()){
			//save the path as a sequence of instructions following the SVG model for "d" attributes
			type=pi.currentSegment(seg);
			switch (type){
				case java.awt.geom.PathIterator.SEG_MOVETO:{
					if (lastOp!='M'){coords.append('M');} else {coords.append(' ');}
					lastOp='M';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth));
					break;
				}
				case java.awt.geom.PathIterator.SEG_LINETO:{
					if (lastOp!='L'){coords.append('L');} else {coords.append(' ');}
					lastOp='L';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth));
					break;
				}
				case java.awt.geom.PathIterator.SEG_QUADTO:{
					if (lastOp!='Q'){coords.append('Q');} else {coords.append(' ');}
					lastOp='Q';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth)+" "+abl2c(seg[2]+farWest)+" "+abl2c(seg[3]+farNorth));
					break;
				}
				case java.awt.geom.PathIterator.SEG_CUBICTO:{
					if (lastOp!='C'){coords.append('C');} else {coords.append(' ');}
					lastOp='C';
					coords.append(abl2c(seg[0]+farWest)+" "+abl2c(seg[1]+farNorth)+" "+abl2c(seg[2]+farWest)+" "+abl2c(seg[3]+farNorth)+" "+abl2c(seg[4]+farWest)+" "+abl2c(seg[5]+farNorth));
					break;
				}
				case java.awt.geom.PathIterator.SEG_CLOSE:{
					//should only happen as the last instruction
					coords.append('Z');
					break;
				}
			}
			pi.next();
		}
		path.setAttribute(SVGReader._d,coords.toString());
		path.setAttribute(SVGReader._style,shapeColors(s));
		if (s.getStroke()!=null){
			createStrokeInformation(s,path);
		}
		createClassInforation(s, path);
		return path;
	}

	private Area getJ2DShape(BooleanOps ops,long x,long y){
	switch (ops.shapeType) {
	case 1:{//ellipse
	    return new Area(new Ellipse2D.Float(x+(ops.ox-ops.szx/2),y-(ops.oy+ops.szy/2),ops.szx,ops.szy));
	}
	case 2:{//rectangle
	    return new Area(new Rectangle2D.Float(x+(ops.ox-ops.szx/2),y-(ops.oy+ops.szy/2),ops.szx,ops.szy));
	}
	default:{//ellipse as default
	    return new Area(new Ellipse2D.Float(x+(ops.ox-ops.szx/2),y-(ops.oy+ops.szy/2),ops.szx,ops.szy));
	}
	}
    }

    /*takes an array of floats and returns a single string containing all values separated by commas*/
    public static String arrayOffloatAsCSStrings(float[] ar){
	String res="";
	for (int i=0;i<ar.length-1;i++){
	    res+=Float.toString(ar[i])+",";
	}
	res+=Float.toString(ar[ar.length-1]);
	return res;
    }

}




