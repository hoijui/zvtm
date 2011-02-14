/*   FILE: AnimationDemo.java
 *   DATE OF CREATION:   Thu Mar 22 17:20:34 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package fr.inria.zvtm.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.ConstantAccInterpolator;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.Transitions;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VShape;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;
import fr.inria.zvtm.glyphs.VImageOr;

public class AnimationDemo extends JApplet implements MouseListener, KeyListener {

    static final int DEFAULT_VIEW_WIDTH = 640;
    static final int DEFAULT_VIEW_HEIGHT = 480;
    int appletWindowWidth = DEFAULT_VIEW_WIDTH;
    int appletWindowHeight = DEFAULT_VIEW_HEIGHT;

    static final String APPLET_WIDTH_PARAM = "width";
    static final String APPLET_HEIGHT_PARAM = "height";

    static final String APPLET_TITLE = "ZVTM - Animations";
    static final Color APPLET_BKG_COLOR = new Color(221, 221, 221);

    static final short PACING_FUNCTION_LIN = 0;
    static final short PACING_FUNCTION_SIFO = 1;
    static final short PACING_FUNCTION_SISO = 2;

    static final short ANIMATE_ROT = 0;
    static final short ANIMATE_SIZ = 1;
    static final short ANIMATE_POS = 2;
    static final short ANIMATE_COL = 3;
    static final short ANIMATE_TRA = 4;

    JPanel viewPanel;
    CommandPanel cmdPanel;

    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    View mView;
    Camera mCam;
    
    AnimationDemoEventHandler eh;

    public AnimationDemo(){
	getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
    }

    public void init(){
	initGUI();
	initGlyphs();
    }

    void initGUI(){
	this.addKeyListener(this);
	this.addMouseListener(this);
	// get width and height of applet panel
	try {appletWindowWidth = Integer.parseInt(getParameter(APPLET_WIDTH_PARAM));}
	catch(NumberFormatException ex){appletWindowWidth = DEFAULT_VIEW_WIDTH;}
	try {appletWindowHeight = Integer.parseInt(getParameter(APPLET_HEIGHT_PARAM));}
	catch(NumberFormatException ex){appletWindowHeight = DEFAULT_VIEW_HEIGHT;}
	DemoUtils.initLookAndFeel();
	Container cpane = getContentPane();
	this.setSize(appletWindowWidth-10, appletWindowHeight-10);
	cpane.setSize(appletWindowWidth, appletWindowHeight);
	cpane.setBackground(APPLET_BKG_COLOR);
	    vsm = VirtualSpaceManager.INSTANCE;
	VText.setMainFont(new Font("Arial", Font.PLAIN, 24));
	mSpace = vsm.addVirtualSpace("demoSpace");
	mCam = mSpace.addCamera();
	Vector cameras = new Vector();
	cameras.add(mCam);
	viewPanel = vsm.addPanelView(cameras, APPLET_TITLE, appletWindowWidth, appletWindowHeight-40);
	mView = vsm.getView(APPLET_TITLE);
	mView.setBackgroundColor(APPLET_BKG_COLOR);
	eh = new AnimationDemoEventHandler(this);
	mView.setListener(eh);

 	viewPanel.setPreferredSize(new Dimension(appletWindowWidth-10, appletWindowHeight-40));
	mView.setAntialiasing(true);

	JPanel borderPanel = new JPanel();
	borderPanel.setLayout(new BorderLayout());
	borderPanel.add(viewPanel, BorderLayout.CENTER);
	borderPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black,2), APPLET_TITLE));
	borderPanel.setOpaque(false);

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	cpane.setLayout(gridBag);
	cmdPanel = new CommandPanel(this);
	buildConstraints(constraints,0,0,1,1,10,100);
	gridBag.setConstraints(cmdPanel, constraints);
	cpane.add(cmdPanel);
	buildConstraints(constraints,1,0,1,1,90,0);
	gridBag.setConstraints(borderPanel, constraints);
	cpane.add(borderPanel);
	vsm.repaint();
	mCam.setAltitude(150);
    }

    VRectangleOr rectangle;
    VShape irregularShape, star;
    VTextOr text;
    VImageOr image;
    VSegment segment;
    VShape triangle;

    static final double COL1_X = -500;
    static final double COL2_X = 0;
    static final double COL3_X = 500;
    static final double ROW1_Y = 400;
    static final double ROW2_Y = 0;
    static final double ROW3_Y = -400;
    static final Color GLYPH_BORDER_COLOR = Color.BLACK;
    static final Color ROW1_COLOR = new Color(110, 253, 55);
    static final Color ROW2_COLOR = new Color(82, 190, 40);
    static final Color ROW3_COLOR = new Color(54, 126, 26);
    static final double SIZE = 80;
    static double COMPOSITE_SIZE = 0;
    static double RECTANGLE_SIZE = 0;
    static double IMAGE_SIZE = 0;
    
    void initGlyphs(){
	// 1st row
	rectangle = new VRectangleOr(COL1_X, ROW1_Y, 0, 100, 50, ROW1_COLOR, GLYPH_BORDER_COLOR, 0, 1.0f);
	float[] starVertices = {1.0f, 0.5f, 1.0f, 0.5f, 1.0f, 0.5f, 1.0f, 0.5f};
	star = new VShape(COL2_X, ROW1_Y, 0, SIZE, starVertices, ROW1_COLOR, GLYPH_BORDER_COLOR, 0, 1.0f);
	float[] triangleEdges = {1f, 1f, 1f};
	triangle = new VShape(COL3_X, ROW1_Y, 0, SIZE, triangleEdges, ROW1_COLOR, GLYPH_BORDER_COLOR, 1.0f);
	// 2nd row
	text = new VTextOr(COL1_X, ROW2_Y, 0, ROW2_COLOR, "Text", 0.0f, VText.TEXT_ANCHOR_MIDDLE, 1.0f);
	text.setFont(new Font("Arial", Font.PLAIN, 48));
	image = new VImageOr(COL2_X, ROW2_Y, 0, (new ImageIcon(this.getClass().getResource("/images/logo-futurs-small.png"))).getImage(), 0, 1.0f);
	image.setBorderColor(GLYPH_BORDER_COLOR);
	float[] irregVertices = {1.0f, 0.2f, 0.7f, 0.3f, 0.5f, 0.1f, 0.8f, 1.0f, 0.4f, 0.4f, 0.3f, 0.6f};
	irregularShape = new VShape(COL3_X, ROW2_Y, 0, (long)SIZE, irregVertices, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 1.0f);
	// 3rd row
	//VRectangleOr c1m = new VRectangleOr(COL1_X, ROW3_Y, 0, 80, 30, ROW3_COLOR, GLYPH_BORDER_COLOR, 0, 1.0f);
	//SGlyph[] sgs1 = {
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), 80, 30, SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), 80, -30, SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), -80, 30, SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), -80, -30, SGlyph.FULL_ROTATION,SGlyph.RESIZE)
	//};
	//composite1 = new CGlyph(c1m, sgs1);
	//VRectangleOr c2m = new VRectangleOr(COL2_X, ROW3_Y, 0, 80, 30, ROW3_COLOR, GLYPH_BORDER_COLOR, 0, 1.0f);
	//SGlyph[] sgs2 = {
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), 80, 30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), 80, -30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), -80, 30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	//    new SGlyph(new VRectangleOr(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0, 0.8f), -80, -30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE)
	//};
	//composite2 = new CGlyph(c2m, sgs2);
	segment = new VSegment(COL3_X, ROW3_Y, 0, ROW3_COLOR, 1.0f, SIZE, 0.707f);
	mSpace.addGlyph(rectangle);
	mSpace.addGlyph(star);
	mSpace.addGlyph(triangle);
	mSpace.addGlyph(text);
	mSpace.addGlyph(image);
	mSpace.addGlyph(irregularShape);
	mSpace.addGlyph(new VText(COL1_X, ROW3_Y-100, 0, Color.BLACK, "Composite: full rotation", VText.TEXT_ANCHOR_MIDDLE));
	//for (int i=0;i<sgs1.length;i++){
	//    mSpace.addGlyph(sgs1[i].getGlyph());
	//}
	//mSpace.addGlyph(composite1);
	//mSpace.addGlyph(c2m);
	//for (int i=0;i<sgs2.length;i++){
	//    mSpace.addGlyph(sgs2[i].getGlyph());
	//}
	//mSpace.addGlyph(composite2);
	mSpace.addGlyph(new VText(COL2_X, ROW3_Y-100, 0, Color.BLACK, "Composite: position rotation", VText.TEXT_ANCHOR_MIDDLE));
	mSpace.addGlyph(segment);
	RECTANGLE_SIZE = rectangle.getSize();
	IMAGE_SIZE = image.getSize();
	//COMPOSITE_SIZE = composite1.getSize();
    }

    void reset(){
	// rectangle
	if (rectangle.vx != COL1_X || rectangle.vy != ROW1_Y){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(500, rectangle, new Point2D.Double(COL1_X, ROW1_Y-rectangle.vy), false,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	if (rectangle.getSize() != RECTANGLE_SIZE){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(500, rectangle, RECTANGLE_SIZE, false, 
				     IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	// star
	if (star.vx != COL2_X || star.vy != ROW1_Y){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(500, star, new Point2D.Double(COL2_X, ROW1_Y), false,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	if (star.getSize() != SIZE){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(500, star, SIZE, false, 
				     IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	// triangle
	if (triangle.vx != COL3_X || triangle.vy != ROW1_Y){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(500, triangle, new Point2D.Double(COL3_X, ROW1_Y), false,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	if (triangle.getSize() != SIZE){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(500, triangle, SIZE, false, 
				     IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	// text
	if (text.vx != COL1_X || text.vy != ROW2_Y){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(500, text, new Point2D.Double(COL1_X, ROW2_Y), false,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	// image
	if (image.vx != COL2_X || image.vy != ROW2_Y){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(500, image, new Point2D.Double(COL2_X, ROW2_Y), false,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	if (image.getSize() != IMAGE_SIZE){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(500, image, IMAGE_SIZE, false, 
				     IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	// shape
	if (irregularShape.vx != COL3_X || irregularShape.vy != ROW2_Y){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(500, irregularShape, new Point2D.Double(COL3_X, ROW2_Y), false,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	if (irregularShape.getSize() != SIZE){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(500, irregularShape, SIZE, false,
				     IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	// segment
	if (segment.vx != COL3_X || segment.vy != ROW3_Y){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(500, segment, new Point2D.Double(COL3_X, ROW3_Y), false,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
	if (segment.getSize() != SIZE){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(500, segment, SIZE, false,
				     IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	}
    }

    void animate(Glyph g){
	switch(cmdPanel.getAnimation()){
	case ANIMATE_ROT:{rotate(g, cmdPanel.getDuration());break;}
	case ANIMATE_SIZ:{resize(g, cmdPanel.getDuration());break;}
	case ANIMATE_POS:{translate(g, cmdPanel.getDuration());break;}
	case ANIMATE_COL:{colorize(g, cmdPanel.getDuration());break;}
	case ANIMATE_TRA:{translucent(g, cmdPanel.getDuration());break;}
	}
    }

    /* ------------------  ORIENTATON ------------------------ */

    static final float ROTATE_DATA = -4*(float)Math.PI;

    void rotate(Glyph g, int d){
	switch(cmdPanel.getPacingFunction()){
	case PACING_FUNCTION_LIN:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphOrientationAnim(d, g, ROTATE_DATA, true,
					    IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	case PACING_FUNCTION_SIFO:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphOrientationAnim(d, g, ROTATE_DATA, true,
					    ConstantAccInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	case PACING_FUNCTION_SISO:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphOrientationAnim(d, g, ROTATE_DATA, true,
					    SlowInSlowOutInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	}
    }

    static final float RESIZE_DATA = 1.5f;

    /* ------------------     SIZE    ------------------------ */

    void resize(Glyph g, int d){
	switch(cmdPanel.getPacingFunction()){
	case PACING_FUNCTION_LIN:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(d, g, RESIZE_DATA, true,
				     IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	case PACING_FUNCTION_SIFO:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(d, g, RESIZE_DATA, true,
				     ConstantAccInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	case PACING_FUNCTION_SISO:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(d, g, RESIZE_DATA, true,
				     SlowInSlowOutInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	}
    }

    /* ------------------   POSITION   ------------------------ */

    static final Point2D.Double TRANSLATE_DATA = new Point2D.Double(100, 50);

    void translate(Glyph g, int d){
	switch(cmdPanel.getPacingFunction()){
	case PACING_FUNCTION_LIN:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(d, g, TRANSLATE_DATA, true,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	case PACING_FUNCTION_SIFO:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(d, g, TRANSLATE_DATA, true,
					ConstantAccInterpolator.getInstance(), null);
	    break;
	}
	case PACING_FUNCTION_SISO:{
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(d, g, TRANSLATE_DATA, true,
					SlowInSlowOutInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, false);
	    break;
	}
	}
    }

    /* ------------------    COLOR     ------------------------ */

    void colorize(Glyph g, int d){
	float mainColorSaturation = g.getHSVColor()[1];
	float mainColorBrightness = g.getHSVColor()[2];
	// animate to black
	Animation anim1 = vsm.getAnimationManager().getAnimationFactory()
	    .createGlyphFillColorAnim(d/2, g, new float[]{0, -mainColorSaturation, -mainColorBrightness}, true,
				      IdentityInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim1, false);
	// animate back to original color
	Animation anim2 = vsm.getAnimationManager().getAnimationFactory()
	    .createGlyphFillColorAnim(d/2, g, new float[]{0, mainColorSaturation, mainColorBrightness}, true,
				      IdentityInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim2, false);
    }

    /* ------------------ TRANSLUCENCY ------------------------ */

    void translucent(Glyph g, int d){
	// animate to transparent
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createTranslucencyAnim(d/2, (Translucent)g, -1f, true, 
				    IdentityInterpolator.getInstance(), null);
	// animate back to opaque
	Animation anim2 = vsm.getAnimationManager().getAnimationFactory()
	    .createTranslucencyAnim(d/2, (Translucent)g, 1f, true,
				    IdentityInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, false);
	vsm.getAnimationManager().startAnimation(anim2, false);
    }


    /* ------------------ Fade IN/OUT  ------------------------ */

    void fade(boolean out){// true = fade out, false = fade in
	if (out){
	    Transitions.fadeOut(mView, cmdPanel.getDuration(), Color.BLACK);
	}
	else {
	    Transitions.fadeIn(mView, cmdPanel.getDuration());	    
	}
    }
    

    /* Key listener (keyboard events are not sent to ViewListener when View is a JPanel...) */
    
    public void keyPressed(KeyEvent e){
	mView.getGlobalView(mCam, 400);
    }

    public void keyReleased(KeyEvent e){}

    public void keyTyped(KeyEvent e){}

    public void mouseClicked(MouseEvent e){}

    public void mouseEntered(MouseEvent e){requestFocus();}

    public void mouseExited(MouseEvent e){}

    public void mousePressed(MouseEvent e){}

    public void mouseReleased(MouseEvent e){}

    static void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }

}

class CommandPanel extends JPanel implements ActionListener {
    
    AnimationDemo application;
    JRadioButton pacingLINBt, pacingSIFOBt, pacingSISOBt;
    ButtonGroup pacingGr;
    JRadioButton animROTBt, animSIZBt, animPOSBt, animCOLBt, animTRABt;
    ButtonGroup animGr;
    JButton resetBt;
    JButton fadeBt;
    JSpinner durationSp;
    
    CommandPanel(AnimationDemo app){
	super();
	this.application = app;
	initGUI();
    }
    
    void initGUI(){
	this.setOpaque(false);
	this.setLayout(new GridLayout(14, 1));

	JLabel animLb = new JLabel("<html><b>Animation</b></html>");
	this.add(animLb);
	animGr = new ButtonGroup();	
	animROTBt = new JRadioButton("Rotate", true);
	animROTBt.setOpaque(false);
	animGr.add(animROTBt);
	this.add(animROTBt);
	animSIZBt = new JRadioButton("Resize", false);
	animSIZBt.setOpaque(false);
	animGr.add(animSIZBt);
	this.add(animSIZBt);
	animPOSBt = new JRadioButton("Translate", false);
	animPOSBt.setOpaque(false);
	animGr.add(animPOSBt);
	this.add(animPOSBt);
	animCOLBt = new JRadioButton("Colorize", false);
	animCOLBt.setOpaque(false);
	animGr.add(animCOLBt);
	this.add(animCOLBt);
	animTRABt = new JRadioButton("Translucence", false);
	animTRABt.setOpaque(false);
	animGr.add(animTRABt);
	this.add(animTRABt);

	JLabel pacingLb = new JLabel("<html><b>Pacing function</b></html>");
	this.add(pacingLb);
	pacingGr = new ButtonGroup();
	pacingLINBt = new JRadioButton("Linear", false);
	pacingLINBt.setOpaque(false);
	pacingGr.add(pacingLINBt);
	this.add(pacingLINBt);
	pacingSIFOBt = new JRadioButton("Slow-in / Fast-out", false);
	pacingSIFOBt.setOpaque(false);
	pacingGr.add(pacingSIFOBt);
	this.add(pacingSIFOBt);
	pacingSISOBt = new JRadioButton("Slow-in / Slow-out", true);
	pacingSISOBt.setOpaque(false);
	pacingGr.add(pacingSISOBt);
	this.add(pacingSISOBt);

	JLabel durationLb = new JLabel("<html><b>Animation duration (ms)</b></html>");
	this.add(durationLb);
	durationSp = new JSpinner(new SpinnerNumberModel(2000, 100, 10000, 100));
	durationSp.setOpaque(false);
	this.add(durationSp);
	
	fadeBt = new JButton("Fade Out");
	fadeBt.setOpaque(false);
	fadeBt.addActionListener(this);
	this.add(fadeBt);

	resetBt = new JButton("Reset");
	resetBt.setOpaque(false);
	resetBt.addActionListener(this);
	this.add(resetBt);
    }

    void fade(){
	if (fadeBt.getText().equals("Fade Out")){application.fade(true);fadeBt.setText("Fade In");}
	else {application.fade(false);fadeBt.setText("Fade Out");}
    }

    short getPacingFunction(){
	if (pacingLINBt.isSelected()){return AnimationDemo.PACING_FUNCTION_LIN;}
	else if (pacingSIFOBt.isSelected()){return AnimationDemo.PACING_FUNCTION_SIFO;}
	else if (pacingSISOBt.isSelected()){return AnimationDemo.PACING_FUNCTION_SISO;}
	return AnimationDemo.PACING_FUNCTION_SISO;
    }

    short getAnimation(){
	if (animROTBt.isSelected()){return AnimationDemo.ANIMATE_ROT;}
	else if (animSIZBt.isSelected()){return AnimationDemo.ANIMATE_SIZ;}
	else if (animPOSBt.isSelected()){return AnimationDemo.ANIMATE_POS;}
	else if (animCOLBt.isSelected()){return AnimationDemo.ANIMATE_COL;}
	else if (animTRABt.isSelected()){return AnimationDemo.ANIMATE_TRA;}
	return AnimationDemo.ANIMATE_ROT;
    }

    int getDuration(){
	return ((Number)durationSp.getValue()).intValue();
    }
    
    public void actionPerformed(ActionEvent e){
	if (e.getSource() == fadeBt){fade();}
	else if (e.getSource() == resetBt){application.reset();}
    }
    
}

class AnimationDemoEventHandler implements ViewListener {

    AnimationDemo application;

    long lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    Glyph underCursor;

    AnimationDemoEventHandler(AnimationDemo app){
	this.application = app;
    }

	public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
		underCursor = getGlyph(v.getVCursor());
		if (underCursor != null){
			application.mSpace.onTop(underCursor);
			v.getVCursor().stickGlyph(underCursor);
		}
	}

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
		v.getVCursor().unstickLastGlyph();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (underCursor != null){
	    application.animate(underCursor);
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
	application.vsm.getActiveView().mouse.setSensitivity(false);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	v.cams[0].setXspeed(0);
	v.cams[0].setYspeed(0);
	v.cams[0].setZspeed(0);
	v.setDrawDrag(false);
	application.vsm.getActiveView().mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    /* Mapping from drag segment value to speed */
    static final float SPEED_FACTOR = 50.0f;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
	    Camera c = application.vsm.getActiveCamera();
	    double a = (c.focal+Math.abs(c.altitude))/c.focal;
	    if (mod == META_SHIFT_MOD) {
		v.cams[0].setXspeed(0);
		v.cams[0].setYspeed(0);
		v.cams[0].setZspeed((c.altitude>0) ? (lastJPY-jpy) * (a/SPEED_FACTOR) : (lastJPY-jpy) / (a*SPEED_FACTOR));
	    }
	    else {
		v.cams[0].setXspeed((c.altitude>0) ? (jpx-lastJPX) * (a/SPEED_FACTOR) : (jpx-lastJPX) / (a*SPEED_FACTOR));
		v.cams[0].setYspeed((c.altitude>0) ? (lastJPY-jpy) * (a/SPEED_FACTOR) : (lastJPY-jpy) / (a*SPEED_FACTOR));
		v.cams[0].setZspeed(0);
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c = application.vsm.getActiveCamera();
	double a = (c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	    application.vsm.repaint();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	    application.vsm.repaint();
	}
    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){}

    Glyph getGlyph(VCursor c){
        Glyph res = c.lastGlyphEntered;
        if (res != null){return res;}
        else {
            Vector<VSegment> v2 = c.getIntersectingSegments(application.mCam, 4);
            if (v2 != null){
                res = v2.firstElement();
            }
        }
        return res;
    }
    
}
