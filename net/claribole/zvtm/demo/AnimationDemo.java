/*   FILE: AnimationDemo.java
 *   DATE OF CREATION:   Thu Mar 22 17:20:34 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.util.Vector;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangleOrST;
import com.xerox.VTM.glyphs.VTriangleOrST;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTextOr;
import com.xerox.VTM.glyphs.VShapeST;
import com.xerox.VTM.glyphs.VSegmentST;
import net.claribole.zvtm.glyphs.CGlyph;
import net.claribole.zvtm.glyphs.SGlyph;
import net.claribole.zvtm.glyphs.VImageOrST;
import net.claribole.zvtm.glyphs.VTextOrST;
import net.claribole.zvtm.engine.TransitionManager;

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

	vsm = new VirtualSpaceManager(true);
	vsm.setMainFont(new Font("Arial", Font.PLAIN, 24));
	mSpace = vsm.addVirtualSpace("demoSpace");
	mCam = vsm.addCamera(mSpace);
	Vector cameras = new Vector();
	cameras.add(mCam);
	viewPanel = vsm.addPanelView(cameras, APPLET_TITLE, appletWindowWidth, appletWindowHeight-40);
	mView = vsm.getView(APPLET_TITLE);
	mView.setBackgroundColor(APPLET_BKG_COLOR);
	eh = new AnimationDemoEventHandler(this);
	mView.setEventHandler(eh);

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
	vsm.repaintNow();
	mCam.setAltitude(150);
    }

    VRectangleOrST rectangle;
    VShapeST irregularShape, star;
    VTextOrST text;
    VImageOrST image;
    CGlyph composite1, composite2;
    VSegmentST segment;
    VTriangleOrST triangle;

    static final long COL1_X = -500;
    static final long COL2_X = 0;
    static final long COL3_X = 500;
    static final long ROW1_Y = 400;
    static final long ROW2_Y = 0;
    static final long ROW3_Y = -400;
    static final Color GLYPH_BORDER_COLOR = Color.BLACK;
    static final Color ROW1_COLOR = new Color(110, 253, 55);
    static final Color ROW2_COLOR = new Color(82, 190, 40);
    static final Color ROW3_COLOR = new Color(54, 126, 26);
    
    void initGlyphs(){
	// 1st row
	rectangle = new VRectangleOrST(COL1_X, ROW1_Y, 0, 100, 50, ROW1_COLOR, GLYPH_BORDER_COLOR, 1.0f, 0);
	float[] starVertices = {1.0f, 0.5f, 1.0f, 0.5f, 1.0f, 0.5f, 1.0f, 0.5f};
	star = new VShapeST(COL2_X, ROW1_Y, 0, 80, starVertices, ROW1_COLOR, GLYPH_BORDER_COLOR, 1.0f, 0);
	triangle = new VTriangleOrST(COL3_X, ROW1_Y, 0, 80, ROW1_COLOR, GLYPH_BORDER_COLOR, 1.0f, 0);
	// 2nd row
	text = new VTextOrST(COL1_X, ROW2_Y, 0, ROW2_COLOR, "Text", 0.0f, VText.TEXT_ANCHOR_MIDDLE, 1.0f);
	text.setSpecialFont(new Font("Arial", Font.PLAIN, 48));
	image = new VImageOrST(COL2_X, ROW2_Y, 0, (new ImageIcon(this.getClass().getResource("/images/logo-futurs-small.png"))).getImage(), 0, 1.0f);
	image.setBorderColor(GLYPH_BORDER_COLOR);
	image.setDrawBorderPolicy(VImageOrST.DRAW_BORDER_MOUSE_INSIDE);
	float[] irregVertices = {1.0f, 0.2f, 0.7f, 0.3f, 0.5f, 0.1f, 0.8f, 1.0f, 0.4f, 0.4f, 0.3f, 0.6f};
	irregularShape = new VShapeST(COL3_X, ROW2_Y, 0, 80, irregVertices, ROW2_COLOR, GLYPH_BORDER_COLOR, 1.0f, 0);
	// 3rd row
	VRectangleOrST c1m = new VRectangleOrST(COL1_X, ROW3_Y, 0, 80, 30, ROW3_COLOR, GLYPH_BORDER_COLOR, 1.0f, 0);
	SGlyph[] sgs1 = {
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), 80, 30, SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), 80, -30, SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), -80, 30, SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), -80, -30, SGlyph.FULL_ROTATION,SGlyph.RESIZE)
	};
	composite1 = new CGlyph(c1m, sgs1);
	VRectangleOrST c2m = new VRectangleOrST(COL2_X, ROW3_Y, 0, 80, 30, ROW3_COLOR, GLYPH_BORDER_COLOR, 1.0f, 0);
	SGlyph[] sgs2 = {
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), 80, 30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), 80, -30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), -80, 30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	    new SGlyph(new VRectangleOrST(0, 0, 0, 10, 10, ROW2_COLOR, GLYPH_BORDER_COLOR, 0.8f, 0), -80, -30, SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE)
	};
	composite2 = new CGlyph(c2m, sgs2);
	segment = new VSegmentST(COL3_X, ROW3_Y, 0, 80, 0.707f, ROW3_COLOR, 1.0f);
	vsm.addGlyph(rectangle, mSpace);
	vsm.addGlyph(star, mSpace);
	vsm.addGlyph(triangle, mSpace);
	vsm.addGlyph(text, mSpace);
	vsm.addGlyph(image, mSpace);
	vsm.addGlyph(irregularShape, mSpace);
	vsm.addGlyph(c1m, mSpace);
	vsm.addGlyph(new VText(COL1_X, ROW3_Y-100, 0, Color.BLACK, "Composite: full rotation", VText.TEXT_ANCHOR_MIDDLE), mSpace);
	for (int i=0;i<sgs1.length;i++){
	    vsm.addGlyph(sgs1[i].getGlyph(), mSpace);
	}
	vsm.addGlyph(composite1, mSpace);
	vsm.addGlyph(c2m, mSpace);
	for (int i=0;i<sgs2.length;i++){
	    vsm.addGlyph(sgs2[i].getGlyph(), mSpace);
	}
	vsm.addGlyph(composite2, mSpace);
	vsm.addGlyph(new VText(COL2_X, ROW3_Y-100, 0, Color.BLACK, "Composite: position rotation", VText.TEXT_ANCHOR_MIDDLE), mSpace);
	vsm.addGlyph(segment, mSpace);
    }

    void reset(){

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

    static final Float ROTATE_DATA = new Float(-4*(float)Math.PI);

    void rotate(Glyph g, int d){
	if (g.getCGlyph() != null){g = g.getCGlyph();}
	switch(cmdPanel.getPacingFunction()){
	case PACING_FUNCTION_LIN:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_ROT_LIN, ROTATE_DATA, g.getID());break;}
	case PACING_FUNCTION_SIFO:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_ROT_PAR, ROTATE_DATA, g.getID());break;}
	case PACING_FUNCTION_SISO:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_ROT_SIG, ROTATE_DATA, g.getID());break;}
	}
    }

    static final Float RESIZE_DATA = new Float(1.5f);

    /* ------------------     SIZE    ------------------------ */

    void resize(Glyph g, int d){
	if (g.getCGlyph() != null){g = g.getCGlyph();}
	switch(cmdPanel.getPacingFunction()){
	case PACING_FUNCTION_LIN:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_SZ_LIN, RESIZE_DATA, g.getID());break;}
	case PACING_FUNCTION_SIFO:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_SZ_PAR, RESIZE_DATA, g.getID());break;}
	case PACING_FUNCTION_SISO:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_SZ_SIG, RESIZE_DATA, g.getID());break;}
	}
    }

    /* ------------------   POSITION   ------------------------ */

    static final LongPoint TRANSLATE_DATA = new LongPoint(100, 50);

    void translate(Glyph g, int d){
	if (g.getCGlyph() != null){g = g.getCGlyph();}
	switch(cmdPanel.getPacingFunction()){
	case PACING_FUNCTION_LIN:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_TRANS_LIN, TRANSLATE_DATA, g.getID());break;}
	case PACING_FUNCTION_SIFO:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_TRANS_PAR, TRANSLATE_DATA, g.getID());break;}
	case PACING_FUNCTION_SISO:{vsm.animator.createGlyphAnimation(d, AnimManager.GL_TRANS_SIG, TRANSLATE_DATA, g.getID());break;}
	}
    }

    /* ------------------    COLOR     ------------------------ */

    void colorize(Glyph g, int d){
	Vector COLOR_DATA = new Vector();
	float mainColorSaturation = g.getHSVColor()[1];
	float mainColorBrightness = g.getHSVColor()[2];
	// animate to black
	COLOR_DATA.add(new Float(0));
 	COLOR_DATA.add(new Float(-mainColorSaturation));
 	COLOR_DATA.add(new Float(-mainColorBrightness));
	COLOR_DATA.add(new Float(0));
	COLOR_DATA.add(new Float(0));
	COLOR_DATA.add(new Float(0));
	vsm.animator.createGlyphAnimation(d/2, AnimManager.GL_COLOR_LIN, COLOR_DATA, g.getID());
	// animate back to original color
	COLOR_DATA.clear();
	COLOR_DATA.add(new Float(0));
	COLOR_DATA.add(new Float(mainColorSaturation));
	COLOR_DATA.add(new Float(mainColorBrightness));
	COLOR_DATA.add(new Float(0));
	COLOR_DATA.add(new Float(0));
	COLOR_DATA.add(new Float(0));
	vsm.animator.createGlyphAnimation(d/2, AnimManager.GL_COLOR_LIN, COLOR_DATA, g.getID());
    }

    /* ------------------ TRANSLUCENCY ------------------------ */

    static Vector TRANSLUCENCY_DATA_1 = new Vector();
    static {
	TRANSLUCENCY_DATA_1.add(new Float(0));
	TRANSLUCENCY_DATA_1.add(new Float(0));
	TRANSLUCENCY_DATA_1.add(new Float(0));
	TRANSLUCENCY_DATA_1.add(new Float(0));
	TRANSLUCENCY_DATA_1.add(new Float(0));
	TRANSLUCENCY_DATA_1.add(new Float(0));
	TRANSLUCENCY_DATA_1.add(new Float(-1));
    }

    static Vector TRANSLUCENCY_DATA_2 = new Vector();
    static {
	TRANSLUCENCY_DATA_2.add(new Float(0));
	TRANSLUCENCY_DATA_2.add(new Float(0));
	TRANSLUCENCY_DATA_2.add(new Float(0));
	TRANSLUCENCY_DATA_2.add(new Float(0));
	TRANSLUCENCY_DATA_2.add(new Float(0));
	TRANSLUCENCY_DATA_2.add(new Float(0));
	TRANSLUCENCY_DATA_2.add(new Float(1));
    }

    void translucent(Glyph g, int d){
	// animate to transparent
	vsm.animator.createGlyphAnimation(d/2, AnimManager.GL_COLOR_LIN, TRANSLUCENCY_DATA_1, g.getID());
	// animate back to opaque
	vsm.animator.createGlyphAnimation(d/2, AnimManager.GL_COLOR_LIN, TRANSLUCENCY_DATA_2, g.getID());
    }


    /* ------------------ Fade IN/OUT  ------------------------ */

    void fade(boolean out){// true = fade out, false = fade in
	if (out){
	    TransitionManager.fadeOut(mView, cmdPanel.getDuration(), Color.BLACK, vsm);
	}
	else {
	    TransitionManager.fadeIn(mView, cmdPanel.getDuration(), vsm);	    
	}
    }
    

    /* Key listener (keyboard events are not sent to ViewEventHandler when View is a JPanel...) */
    
    public void keyPressed(KeyEvent e){
	vsm.getGlobalView(mCam, 400);
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
	
	fadeBt = new JButton("Fade out");
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
