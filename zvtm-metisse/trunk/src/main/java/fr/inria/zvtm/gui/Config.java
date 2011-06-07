/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Config.java 2769 2010-01-15 10:17:58Z epietrig $
 */

package fr.inria.zvtm.gui;

import java.awt.Color;
import java.awt.Font;

public class Config {
    
    /* Fonts */
	public static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
	public static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);
	public static final Font SAY_MSG_FONT = new Font("Arial", Font.PLAIN, 24);
	public static final Font MONOSPACE_ABOUT_FONT = new Font("Courier", Font.PLAIN, 8);
    
    /* Other colors */
    public static final Color SAY_MSG_COLOR = Color.LIGHT_GRAY;
    public static Color BACKGROUND_COLOR  = Color.LIGHT_GRAY;
    public static final Color FADE_REGION_FILL = Color.BLACK;
    public static final Color FADE_REGION_STROKE = Color.WHITE;
    
    /* Overview */
    public static final int OVERVIEW_WIDTH = 200;
    public static final int OVERVIEW_HEIGHT = 200;
    public static final Color OBSERVED_REGION_COLOR = Color.GREEN;
    public static final float OBSERVED_REGION_ALPHA = 0.5f;
    public static final Color OV_BORDER_COLOR = Color.WHITE;
	public static final Color OV_INSIDE_BORDER_COLOR = Color.WHITE;
    
    /* Durations/Animations */
    public static final int ANIM_MOVE_LENGTH = 300;
    public static final int SAY_DURATION = 500;

    /* External resources */
    public static final String INSITU_LOGO_PATH = "/images/insitu.png";
    public static final String INRIA_LOGO_PATH = "/images/inria.png";
 
}
