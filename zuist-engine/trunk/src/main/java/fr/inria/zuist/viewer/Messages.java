/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.awt.geom.Point2D;

import java.util.Scanner;

class Messages {
		
	static String VERSION;

	static {
	    Scanner sc = new Scanner(Messages.class.getResourceAsStream("/properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()){
            String token = sc.next();
            if (token.equals("version")){
                Messages.VERSION = sc.next();
            }
        }
	}

    static final String OPEN = "Open...";
    static final String EXIT = "Exit";
    static final String RELOAD = "Reload";
    static final String OVERVIEW = "Overview";
    static final String ABOUT = "About...";
    static final String FILE = "File";
    static final String VIEW = "View";
    static final String HELP = "Help"; 

	static final String PM_ENTRY = "mpmE";

    static final String PM_BACK = "Back";
    static final String PM_GLOBALVIEW = "Global View";
    static final String PM_OPEN = "Open...";
    static final String PM_RELOAD = "Reload";
    
    static final String INFO_HIDE = "Hide Info";
    static final String INFO_SHOW = "Show Info";
    static final String CONSOLE_HIDE = "Hide Console";
    static final String CONSOLE_SHOW = "Show Console";
    
    static final String ALTITUDE = "Altitude: ";
    static final String LEVEL = "Level: ";
    
    static final String COORD_SEP = ", ";

    static final String[] mainMenuLabels = {PM_GLOBALVIEW, PM_OPEN, PM_BACK, PM_RELOAD};
    static final Point2D.Double[] mainMenuLabelOffsets = {new Point2D.Double(10, 0), new Point2D.Double(0, 0),
						     new Point2D.Double(-10, 0), new Point2D.Double(0, -10)};
						     
	static final String ZON = "Zero-Order Navigation";
	static final String FON = "First-Order Navigation";
	
	static final String SCB = "Speed-coupled Blending Lens";
	static final String FISHEYE = "Fisheye Lens";

}
