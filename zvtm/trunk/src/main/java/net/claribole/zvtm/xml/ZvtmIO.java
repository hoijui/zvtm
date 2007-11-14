/*   FILE: ZvtmIO.java
 *   DATE OF CREATION:   Fri Nov 29 15:23:05 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Tue Dec 03 12:05:17 2002 by Emmanuel Pietriga
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zvtm.xml;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.ClosedShape;
import com.xerox.VTM.glyphs.RectangularShape;
import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VCirImage;
import com.xerox.VTM.glyphs.VCirShape;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.VCircleST;
import com.xerox.VTM.glyphs.VDiamond;
import com.xerox.VTM.glyphs.VDiamondOr;
import com.xerox.VTM.glyphs.VDiamondOrST;
import com.xerox.VTM.glyphs.VDiamondST;
import com.xerox.VTM.glyphs.VEllipse;
import com.xerox.VTM.glyphs.VEllipseST;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VImageOr;
import com.xerox.VTM.glyphs.VOctagon;
import com.xerox.VTM.glyphs.VOctagonOr;
import com.xerox.VTM.glyphs.VOctagonOrST;
import com.xerox.VTM.glyphs.VOctagonST;
import com.xerox.VTM.glyphs.VPoint;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRectangleOr;
import com.xerox.VTM.glyphs.VRectangleOrST;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VShape;
import com.xerox.VTM.glyphs.VShapeST;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTextOr;
import com.xerox.VTM.glyphs.VTriangle;
import com.xerox.VTM.glyphs.VTriangleOr;
import com.xerox.VTM.glyphs.VTriangleOrST;
import com.xerox.VTM.glyphs.VTriangleST;

/**Methods to read/write VTM files (using its own XML vocabulary), which can be used to store entire ZVTM environments (cameras, glyphs, virtual spaces, views) or just some specific entities <br> no support for curves yet (coming soon)*/

public class ZvtmIO {

    public static String zvtmURI="http://www.claribole.net/zvtm";
    public static String prefix="zvtm";

    private static File imageIconDir=new File(".");

    public static String _alpha="alpha";
    public static String _orient="or";
    public static String _vertices="vtx";
    public static String _x="vx";
    public static String _y="vy";
    public static String _id="id";
    public static String _sz="sz";
    public static String _width="width";
    public static String _height="height";
    public static String _color="color";
    public static String _filled="filled";
    public static String _bcolor="bcolor";
    public static String _border="border";
    public static String _selected="selected";
    public static String _sensitive="sensitive";
    public static String _visible="visible";
    public static String _label="text";
    public static String _type="type";
    public static String _cid="cid";
    public static String _zoom="zoom";
    public static String _font="font";
    public static String _path="path";
    public static String _shpcolor="shpcolor";
    public static String _shpfilled="shpfilled";
    public static String _shpbcolor="shpbcolor";

    public static String _shape="shape";
    public static String _rectangle="rectangle";
    public static String _circle="circle";
    public static String _ellipse="ellipse";
    public static String _triangle="triangle";
    public static String _text="text";
    public static String _segment="segment";
    public static String _diamond="diamond";
    public static String _octagon="octagon";
    public static String _point="point";
    public static String _image="image";
    public static String _cirimage="cirimage";
    public static String _cirshape="cirshape";

    /**
     * set the directory containing the bitmap image files used by VImage (right now the format is PNG and cannot be changed)
     *@param f should be a directory (no effect if it is not)
     */
    public static void setImageIconDirectory(File f){
	if (f!=null && f.isDirectory()){
	    imageIconDir=f;
	}
    }

    public static Document parse(String xmlFile,boolean validation) {
	try {
	    DOMParser parser = new DOMParser();
	    parser.setFeature("http://xml.org/sax/features/validation",validation);
	    parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",validation);  //if true, the external DTD will be loaded even if validation was set to false
	    parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace",false);
	    try {
		parser.parse(xmlFile);
	    }
	    catch (SAXException se) {
		//se.printStackTrace();
	    }
	    catch (IOException ioe) {
		//ioe.printStackTrace();
	    }
	    Document document = parser.getDocument();
	    document.normalize();
	    return document;
	}
	catch (Exception e){
	    return null;
	}
    }

    public static void serialize(Document d,File f){
	if (f!=null && d!=null){
	    //serialize a DOM instance 
	    org.apache.xml.serialize.XMLSerializer ser=new org.apache.xml.serialize.XMLSerializer();   
	    try {
		ser.setOutputCharStream(new java.io.FileWriter(f.toString()));
		ser.setOutputFormat(new org.apache.xml.serialize.OutputFormat(d,"UTF-8",true));
		ser.serialize(d);
	    }
	    catch (java.io.IOException ioe){ioe.printStackTrace();}
	}
    }

    /**create all virtual spaces, glyphs, cameras, and views defined in file f
     *@param f ZVTM/XML file containing the definitions
     *@param vsm VirtualSpaceManager that will handle all the created entities
     */
    public static void load(File f,VirtualSpaceManager vsm){
	
    }

    /**save all virtual spaces, glyphs, cameras, and views managed by vsm in file f
     *@param f ZVTM/XML file in which the definitions will be stored
     *@param vsm VirtualSpaceManager that handles the entities to be saved
     */
    public static void save(File f,VirtualSpaceManager vsm){
// 	DOMImplementation di=new DOMImplementationImpl();
// 	Document prj=di.createDocument(Bidule.biduleURI,"bd:project",null);
// 	Element rt=prj.getDocumentElement();
// 	rt.setAttribute("xmlns:bd",Bidule.biduleURI);
// 	Element defs=prj.createElementNS(Bidule.biduleURI,"bd:definitions");
//  	rt.appendChild(defs);
// 	serialize(prj,f);
    }

    /**
     *Returns a string representation of the list of a VShape's vertices (comma separated values)
     *@param vts float array of vertices defining a VShape (between 0 and 1.0)
     */
    public static String verticesAsString(float[] vts){
	String res="";
	if (vts!=null){
	    StringBuffer sb=new StringBuffer();
	    for (int i=0;i<vts.length-1;i++){
		sb.append(vts[i]);
		sb.append(",");
	    }
	    sb.append(vts[vts.length-1]);
	    res=sb.toString();
	}
	return res;
    }

    /**Get a DOM representation of a given glyph using a ZVTM specific XML vocabulary
     *@param gl glyph to be represented as a DOM element
     *@param d XML document represented as a DOM in which the element representing the glyph will be inserted (note: you still have to call Node.appendChild() to actually attach the newly created element to the document)
     */
    public static Element getGlyphAsDOMElement(Glyph gl,Document d){
	Element res=null;
	if (gl instanceof VShape){
	    res=d.createElementNS(zvtmURI,prefix+":"+_shape);
	    res.setAttribute(_orient,Float.toString(gl.getOrient()));
	    res.setAttribute(_vertices,ZvtmIO.verticesAsString(((VShape)gl).getVertices()));
	}
	else if (gl instanceof VRectangle){
	    res=d.createElementNS(zvtmURI,prefix+":"+_rectangle);
	    if (gl instanceof VRectangleOr){res.setAttribute(_orient,Float.toString(gl.getOrient()));}
	}
	else if (gl instanceof VCircle){
	    res=d.createElementNS(zvtmURI,prefix+":"+_circle);
	}
	else if (gl instanceof VEllipse){
	    res=d.createElementNS(zvtmURI,prefix+":"+_ellipse);
	}
	else if (gl instanceof VTriangle){
	    res=d.createElementNS(zvtmURI,prefix+":"+_triangle);
	    if (gl instanceof VTriangleOr){res.setAttribute(_orient,Float.toString(gl.getOrient()));}
	}
	else if (gl instanceof VText){
	    res=d.createElementNS(zvtmURI,prefix+":"+_text);
	    if (gl instanceof VTextOr){res.setAttribute(_orient,Float.toString(gl.getOrient()));}
	    res.setAttribute(_zoom,Boolean.toString(((VText)gl).isZoomSensitive()));
	}
	else if (gl instanceof VSegment){
	    res=d.createElementNS(zvtmURI,prefix+":"+_segment);
	    //res.setAttribute(_orient,Float.toString(gl.getOrient()));
	}
	else if (gl instanceof VDiamond){
	    res=d.createElementNS(zvtmURI,prefix+":"+_diamond);
	    if (gl instanceof VDiamondOr){res.setAttribute(_orient,Float.toString(gl.getOrient()));}
	}
	else if (gl instanceof VOctagon){
	    res=d.createElementNS(zvtmURI,prefix+":"+_octagon);
	    if (gl instanceof VOctagonOr){res.setAttribute(_orient,Float.toString(gl.getOrient()));}
	}
	else if (gl instanceof VPoint){
	    res=d.createElementNS(zvtmURI,prefix+":"+_point);
	}
	else if (gl instanceof VImage){
	    res=d.createElementNS(zvtmURI,prefix+":"+_image);
	    if (gl instanceof VImageOr){res.setAttribute(_orient,Float.toString(gl.getOrient()));}
	    res.setAttribute(_zoom,Boolean.toString(((VImage)gl).isZoomSensitive()));
	    Image im=((VImage)gl).getImage();
	    ImageWriter writer=(ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
	    File f=null;
	    try {
		f=File.createTempFile("zvtm",".png",imageIconDir);
		writer.setOutput(ImageIO.createImageOutputStream(f));
		BufferedImage bi=new BufferedImage(im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_ARGB);
		(bi.createGraphics()).drawImage(im,null,null);
		writer.write(bi);
		res.setAttribute(_path,f.getAbsolutePath());
	    }
	    catch (Exception ex){
		System.err.println("ZVTM-I/O: An error occured while exporting "+gl.toString()+"to PNG format.\n"+ex);
	    }
	}
	else if (gl instanceof VCirImage){
	    res=d.createElementNS(zvtmURI,prefix+":"+_cirimage);
	    res.setAttribute(_orient,Float.toString(gl.getOrient()));
	    Image im=((VCirImage)gl).getImage();
	    ImageWriter writer=(ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
	    File f=null;
	    try {
		f=File.createTempFile("zvtm",".png",imageIconDir);
		writer.setOutput(ImageIO.createImageOutputStream(f));
		BufferedImage bi=new BufferedImage(im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_ARGB);
		(bi.createGraphics()).drawImage(im,null,null);
		writer.write(bi);
		res.setAttribute(_path,f.getAbsolutePath());
	    }
	    catch (Exception ex){
		System.err.println("ZVTM-I/O: An error occured while exporting "+gl.toString()+"to PNG format.\n"+ex);
	    }
	}
	else if (gl instanceof VCirShape){
	    VCirShape c=(VCirShape)gl;
	    res=d.createElementNS(zvtmURI,prefix+":"+_cirshape);
	    res.setAttribute(_orient,Float.toString(gl.getOrient()));
	    res.setAttribute(_vertices,ZvtmIO.verticesAsString(c.getVertices()));
	    res.setAttribute(_shpcolor,Integer.toString(c.getShapeColor().getRGB()));
	    res.setAttribute(_shpfilled,Boolean.toString(c.isShapeFilled()));
	    res.setAttribute(_shpbcolor,Integer.toString(c.getShapeBorderColor().getRGB()));
	}
	else {System.err.println("ZVTM-I/O: unsupported glyph type (no DOM representation defined): "+gl.toString());return res;}
	//common attributes
	res.setAttribute(_id,gl.getID().toString());
	res.setAttribute(_x,Long.toString(gl.vx));
	res.setAttribute(_y,Long.toString(gl.vy));
	res.setAttribute(_sz,Float.toString(gl.getSize()));
	if (gl instanceof RectangularShape){
	    res.setAttribute(_width,Long.toString(((RectangularShape)gl).getWidth()));
	    res.setAttribute(_height,Long.toString(((RectangularShape)gl).getHeight()));
	}
	res.setAttribute(_color,Integer.toString(gl.getColor().getRGB()));
	res.setAttribute(_filled, Boolean.toString(gl.isFilled()));
	res.setAttribute(_bcolor, Integer.toString(gl.getBorderColor().getRGB()));
	res.setAttribute(_border, Boolean.toString(gl.isBorderDrawn()));
	res.setAttribute(_selected,Boolean.toString(gl.isSelected()));
	res.setAttribute(_sensitive,Boolean.toString(gl.isSensitive()));
	res.setAttribute(_visible,Boolean.toString(gl.isVisible()));
	if (gl instanceof VText){
	    VText tx = (VText)gl;
	    if (tx.getText()!=null && tx.getText().length()>0){res.setAttribute(_label,tx.getText());}
	    //note: can probably use java.awt.Font.decode(String) to decode what is saved here
	    if (tx.usesSpecialFont()){res.setAttribute(_font,tx.getFont().toString());}
	}
	if (gl.getType()!=null && gl.getType().length()>0){res.setAttribute(_type,gl.getType());}
	if (gl instanceof Translucent){res.setAttribute(_alpha,Float.toString(((Translucent)gl).getTranslucencyValue()));}
	//ref to composite glyph
	if (gl.getCGlyph()!=null){res.setAttribute(_cid,gl.getCGlyph().getID().toString());}	
	return res;
    }
    
    /**Get the glyph corresponding to the provided DOM representation using a ZVTM specific XML vocabulary
     */
    public static Glyph getGlyphFromDOMElement(Element el){
	Glyph res=null;
	if (el.getNamespaceURI()!=null && el.getNamespaceURI().equals(zvtmURI)){
	    try {
		String name=el.getLocalName();
		long vx=(new Long(el.getAttribute(_x))).longValue();
		long vy=(new Long(el.getAttribute(_y))).longValue();
		float sz=(new Float(el.getAttribute(_sz))).floatValue();
		Color fc=new Color((new Integer(el.getAttribute(_color))).intValue());
		if (name.equals(_shape)){
		    if (el.hasAttribute(_alpha)){
			res=new VShapeST(vx,vy,0,(long)sz,floatTokenizer(el.getAttribute(_vertices)),fc,(new Float(el.getAttribute(_orient))).floatValue());
			((Translucent)res).setTranslucencyValue((new Float(el.getAttribute(_alpha))).floatValue());
		    }
		    else {res=new VShape(vx,vy,0,(long)sz,floatTokenizer(el.getAttribute(_vertices)),fc,(new Float(el.getAttribute(_orient))).floatValue());}
		}
		else if (name.equals(_rectangle)){
		    if (el.hasAttribute(_alpha)){
			if (el.hasAttribute(_orient)){
			    res=new VRectangleOrST(vx,vy,0,(new Long(el.getAttribute(_width))).longValue(),(new Long(el.getAttribute(_height))).longValue(),fc,(new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res=new VRectangleST(vx,vy,0,(new Long(el.getAttribute(_width))).longValue(),(new Long(el.getAttribute(_height))).longValue(),fc);}
			((Translucent)res).setTranslucencyValue((new Float(el.getAttribute(_alpha))).floatValue());
		    }
		    else {
			if (el.hasAttribute(_orient)){
			    res=new VRectangleOr(vx,vy,0,(new Long(el.getAttribute(_width))).longValue(),(new Long(el.getAttribute(_height))).longValue(),fc,(new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res=new VRectangle(vx,vy,0,(new Long(el.getAttribute(_width))).longValue(),(new Long(el.getAttribute(_height))).longValue(),fc);}
		    }
		}
		else if (name.equals(_circle)){
		    if (el.hasAttribute(_alpha)){
			res=new VCircleST(vx,vy,0,(long)sz,fc);
			((Translucent)res).setTranslucencyValue((new Float(el.getAttribute(_alpha))).floatValue());
		    }
		    else {res=new VCircle(vx,vy,0,(long)sz,fc);}
		}
		else if (name.equals(_ellipse)){
		    if (el.hasAttribute(_alpha)){
			res=new VEllipseST(vx,vy,0,(new Long(el.getAttribute(_width))).longValue(),(new Long(el.getAttribute(_height))).longValue(),fc);
			((Translucent)res).setTranslucencyValue((new Float(el.getAttribute(_alpha))).floatValue());
		    }
		    else {res=new VEllipse(vx,vy,0,(new Long(el.getAttribute(_width))).longValue(),(new Long(el.getAttribute(_height))).longValue(),fc);}
		}
		else if (name.equals(_triangle)){
		    if (el.hasAttribute(_alpha)){
			if (el.hasAttribute(_orient)){
			    res = new VTriangleOrST(vx, vy, 0, (long)sz, fc, Color.BLACK, (new Float(el.getAttribute(_alpha))).floatValue(), (new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res = new VTriangleST(vx,vy,0,(long)sz,fc, Color.BLACK, (new Float(el.getAttribute(_alpha))).floatValue());}
		    }
		    else {
			if (el.hasAttribute(_orient)){
			    res=new VTriangleOr(vx,vy,0,(long)sz,fc,(new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res=new VTriangle(vx,vy,0,(long)sz,fc);}
		    }
		}
		else if (name.equals(_text)){
		    if (el.hasAttribute(_orient)){
			res=new VTextOr(vx,vy,0,fc,el.getAttribute(_label),(new Float(el.getAttribute(_orient))).floatValue());
		    }
		    else {res=new VText(vx,vy,0,fc,el.getAttribute(_label));}
		    ((VText)res).setZoomSensitive((new Boolean(el.getAttribute(_zoom))).booleanValue());
		    if (el.hasAttribute(_font)){((VText)res).setSpecialFont(Font.decode(el.getAttribute(_font)));}
		}
		else if (name.equals(_segment)){
		    res=new VSegment(vx,vy,0,(new Long(el.getAttribute(_width))).longValue(),(new Long(el.getAttribute(_height))).longValue(),fc);
		}
		else if (name.equals(_diamond)){
		    if (el.hasAttribute(_alpha)){
			if (el.hasAttribute(_orient)){
			    res = new VDiamondOrST(vx, vy, 0, (long)sz, fc, Color.BLACK, (new Float(el.getAttribute(_alpha))).floatValue(), (new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res = new VDiamondST(vx, vy, 0, (long)sz, fc, Color.BLACK, (new Float(el.getAttribute(_alpha))).floatValue());}
		    }
		    else {
			if (el.hasAttribute(_orient)){
			    res=new VDiamondOr(vx,vy,0,(long)sz,fc,(new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res=new VDiamond(vx,vy,0,(long)sz,fc);}
		    }
		}
		else if (name.equals(_octagon)){
		    if (el.hasAttribute(_alpha)){
			if (el.hasAttribute(_orient)){
			    res = new VOctagonOrST(vx, vy, 0, (long)sz, fc, Color.BLACK, (new Float(el.getAttribute(_alpha))).floatValue(), (new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res = new VOctagonST(vx, vy, 0, (long)sz, fc, Color.BLACK, (new Float(el.getAttribute(_alpha))).floatValue());}
		    }
		    else {
			if (el.hasAttribute(_orient)){
			    res = new VOctagonOr(vx, vy, 0, (long)sz, fc, Color.BLACK, (new Float(el.getAttribute(_orient))).floatValue());
			}
			else {res=new VOctagon(vx,vy,0,(long)sz,fc);}
		    }
		}
		else if (name.equals(_point)){
		    res=new VPoint(vx,vy,fc);
		}
		else if (name.equals(_image)){
		    
		}
		else if (name.equals(_cirimage)){
		    
		}
		else if (name.equals(_cirshape)){
		    
		}
		if (res instanceof ClosedShape){
		    ClosedShape cs = (ClosedShape)res;
		    cs.setFilled((new Boolean(el.getAttribute(_filled))).booleanValue());
		    float[] hsvb=new float[3];
		    Color bc=new Color((new Integer(el.getAttribute(_bcolor))).intValue());
		    Color.RGBtoHSB(bc.getRed(),bc.getGreen(),bc.getBlue(),hsvb);
		    cs.setHSVbColor(hsvb[0],hsvb[1],hsvb[2]);
		    cs.setDrawBorder((new Boolean(el.getAttribute(_border))).booleanValue());
		}
		res.select((new Boolean(el.getAttribute(_selected))).booleanValue());
		res.setSensitivity((new Boolean(el.getAttribute(_sensitive))).booleanValue());
		res.setVisible((new Boolean(el.getAttribute(_visible))).booleanValue());
		if (el.hasAttribute(_type)){res.setType(el.getAttribute(_type));}
	    }
	    catch (Exception ex) {
		System.err.println("ZVTM_I/O : Error: getGlyphFromDOMElement: unknown type of glyph (or bad delcaration) "+el.toString()+" "+ex);
	    }
	}
	else {
	    System.err.println("ZVTM_I/O : Error: getGlyphFromDOMElement: provided element does not belong to the zvtm namespace and does not describe a Glyph "+el.toString());
	}
	return res;
    }

    public static float[] floatTokenizer(String s){
	StringTokenizer tks=new StringTokenizer(s,",");
	float[] res=new float[tks.countTokens()];
	for (int i=0;i<res.length;i++){
	    res[i]=(new Float(tks.nextToken())).floatValue();
	}
	return res;
    }

}
