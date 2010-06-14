package fr.inria.zvtm.nodetrix;

import java.awt.Color;

/**Class that stores all color values applied in the project.
 * Arrays can be used to switch between color models.
 */
public class ProjectColors {
	
	//COLOR SCHEME DEFINITION
	public static int CS_SCREEN = 0;
    public static int CS_PRINT = 1;
    public static int COLOR_SCHEME = CS_SCREEN;
    
    public static final Color[] HIGHLIGHT = {Color.yellow, Color.orange};
    public static final Color[] HIGHLIGHT_RELATED = {Color.yellow.darker(), Color.orange.brighter()};
//    public static final Color[] EDGE_HIGHLIGHT_INCOMING = {Color.orange};
//    public static final Color[] EDGE_HIGHLIGHT_OUTGOING = {Color.orange.brighter()};
    
    //COLOR DEFINITIONS
    public static final Color MATRIX_BACKGROUND = Color.WHITE;	
	public static final Color[] MATRIX_SYMMETRY_FIELDS = {Color.LIGHT_GRAY, new Color(245,245,245)};	
	public static final Color[] MATRIX_GROUP_LABEL_BACKGROUND = {Color.DARK_GRAY, Color.GRAY}	;	
	public static final Color[] MATRIX_GROUP_LABEL_TEXT = {Color.LIGHT_GRAY, Color.BLACK};	
	public static final Color[] MATRIX_GRID = {Color.LIGHT_GRAY, new Color(235,235,235)};	
	public static final float MATRIX_GRID_TRANSLUCENCY = .5f;
	
	public static final Color[] EXTRA_COLOR_GRADIENT_END = {new Color(50,50,50), new Color(245,245,245)};
	public static final Color[] EXTRA_COLOR_GRADIENT_START = {Color.GRAY, Color.LIGHT_GRAY};
	public static final Color[] EXTRA_COLOR_FADE_OUT = {Color.GRAY.darker(), new Color(245,245,245)};
	
	public static final float INTRA_TRANSLUCENCY = .7f;
	public static final float INTRA_TRANSLUCENCY_DIMMFACTOR = .5f;
	public static final Color INTRA_FADE = Color.LIGHT_GRAY.brighter();
	public static final Color INTRA_COLOR_DEFAULT = Color.BLUE;
	
	public static final Color[] NODE_TEXT = {Color.LIGHT_GRAY, Color.BLACK};
	public static final Color[] NODE_BACKGROUND = {Color.DARK_GRAY.darker(), Color.LIGHT_GRAY};
	public static final float NODE_BACKGROUND_TRANSLUCENCY = .8f;
		
	
}
