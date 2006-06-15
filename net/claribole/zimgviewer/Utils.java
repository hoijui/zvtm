/*   FILE: Utils.java
 *   DATE OF CREATION:   Thu Jan 09 14:14:35 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu May 29 16:47:16 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zimgviewer;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;

import javax.swing.UIManager;

import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class Utils {

    static Font smallFont=new Font("Dialog",0,10);
    static java.awt.Color pastelBlue=new java.awt.Color(156,154,206);
    static java.awt.Color darkerPastelBlue=new java.awt.Color(125,123,165);

//     private static final String mac="com.sun.java.swing.plaf.mac.MacLookAndFeel";
//     private static final String metal="javax.swing.plaf.metal.MetalLookAndFeel";
//     private static final String motif="com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
//     private static String currentLookAndFeel="com.sun.java.swing.plaf.motif.MotifLookAndFeel";

    public static void initLookAndFeel(){
// 	try {UIManager.setLookAndFeel(currentLookAndFeel);}
// 	catch(Exception ex){System.err.println("An error occured while trying to change the look and feel\n"+ex);}
	String key;
	for (Enumeration e=UIManager.getLookAndFeelDefaults().keys();e.hasMoreElements();){
	    key=(String)e.nextElement();
	    if (key.endsWith(".font") || key.endsWith("Font")){UIManager.put(key,smallFont);}
	}
	UIManager.put("ProgressBar.foreground",pastelBlue);
	UIManager.put("ProgressBar.background",java.awt.Color.lightGray);
	UIManager.put("Label.foreground",java.awt.Color.black);
    }

    /**
     * tells wheter the current JVM is version 1.4.0 and later (or not)
     */
    public static boolean javaVersionIs140OrLater(){
	String version=System.getProperty("java.vm.version");
	float numVer=(new Float(version.substring(0,3))).floatValue();
	if (numVer>=1.4f){return true;}
	else {return false;}
    }

    public static boolean hasImageExtension(File f){
	String s=f.toString();
	int index=s.lastIndexOf(".");
	if (index!=-1){
	    String ext=s.substring(index);
	    if (ext.length()>0){
		ext=ext.toLowerCase();
		if (ext.endsWith("png") || ext.endsWith("jpg") || ext.endsWith("gif")){return true;}
		else return false;
	    }
	}
	return false;
    }

    static void serialize(Document d,File f){
	if (f!=null && d!=null){
	    OutputFormat format=new OutputFormat(d,"UTF-8",true);
	    format.setLineSeparator(LineSeparator.Web);
	    try {
		OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		DOMSerializer serializer=(new XMLSerializer(osw,format)).asDOMSerializer();
		serializer.serialize(d);
	    }
	    catch (IOException e){
		e.printStackTrace();
	    }
	}
    }

}
