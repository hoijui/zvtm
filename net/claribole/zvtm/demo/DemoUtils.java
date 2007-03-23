/*   FILE: Utils.java
 *   DATE OF CREATION:   Thu Jan 09 14:14:35 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.awt.Font;

import java.util.Enumeration;

import javax.swing.UIManager;

public class DemoUtils {

    static final Font smallFont = new Font("Dialog", 0, 10);
    static Color pastelBlue = new Color(156, 154, 206);
    static Color darkerPastelBlue = new Color(125, 123, 165);

    public static void initLookAndFeel(){
	String key;
	Object okey;
	for (Enumeration e = UIManager.getLookAndFeelDefaults().keys();e.hasMoreElements();){
	    okey = e.nextElement(); // depending on JVM (1.5.x and earlier, or 1.6.x or later) and OS,
	    key = okey.toString();  // keys are respectively String or StringBuffer objects
	    if (key.endsWith(".font") || key.endsWith("Font")){UIManager.put(okey, smallFont);}
	}
	UIManager.put("ProgressBar.foreground", pastelBlue);
	UIManager.put("ProgressBar.background", Color.lightGray);
	UIManager.put("Label.foreground", Color.black);
    }

}
