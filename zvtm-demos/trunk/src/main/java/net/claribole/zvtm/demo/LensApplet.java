/*   FILE: LensApplet.java
 *   DATE OF CREATION:  Tue Nov 16 15:39:23 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.interpolation.IdentityInterpolator;
import net.claribole.zvtm.lens.FixedSizeLens;
import net.claribole.zvtm.lens.FSLinearLens;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VSegment;

public class LensApplet extends JApplet {

    static VirtualSpaceManager vsm;
    static LensAppletEvtHdlr evt;

    static Color backgroundColor = new Color(221, 221, 221);

    int viewWidth=400;
    int viewHeight=400;

    static String demoVS="demovs";
    static String zvtmView="Lens Applet Demo";
    
    VirtualSpace vs;
    View view;

    Lens lens;

    JCheckBox lensCB;
    
    public LensApplet() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",Boolean.TRUE);
    }

    public void init() {
	Container cp = getContentPane();
	try {
	    int w=Integer.parseInt(getParameter("width"));
	    int h=Integer.parseInt(getParameter("height"));
	    if (w>0){viewWidth=w;}
	    if (h>0){viewHeight=h;}
	}
	catch (Exception ex){}
	cp.setBackground(backgroundColor);
	cp.setLayout(new FlowLayout());
	((JPanel)cp).setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black,2)," Lens demo "));
	vsm = VirtualSpaceManager.INSTANCE;
	vs = vsm.addVirtualSpace(demoVS);
	vsm.addCamera(demoVS);
	vsm.getVirtualSpace(demoVS).getCamera(0).setZoomFloor(0);
	Vector cams=new Vector();
	cams.add(vsm.getVirtualSpace(demoVS).getCamera(0));
	this.setSize(viewWidth-10,viewHeight-10);
	cp.setSize(viewWidth,viewHeight);
	JPanel zvtmV=vsm.addPanelView(cams,zvtmView,viewWidth-10,viewHeight-10);
 	zvtmV.setPreferredSize(new Dimension(viewWidth-10,viewHeight-80));
	view = vsm.getView(zvtmView);
	evt = new LensAppletEvtHdlr(this);
	view.setEventHandler(evt);
	view.setBackgroundColor(backgroundColor);
	cp.add(zvtmV);
	ActionListener a0 = new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (lensCB.isSelected()){
			LensApplet.this.view.repaintNow();
			LensApplet.this.lens = view.setLens(new FSLinearLens(1.0f,100,20));
			LensApplet.this.lens.setBufferThreshold(1.5f);
			
			Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
			    .createLensMagAnim(1000, (FixedSizeLens)LensApplet.this.lens, 
					       1f, true, 
					       IdentityInterpolator.getInstance(), null);
			vsm.getAnimationManager().startAnimation(lensAnim, true);
		    }
		    else {
			//XXX remove lens from view? (view.setLens(null))
			Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
			    .createLensMagAnim(1000, (FixedSizeLens)LensApplet.this.lens, 
					       -1f, true, 
					       IdentityInterpolator.getInstance(), null);
			vsm.getAnimationManager().startAnimation(lensAnim, true);

			LensApplet.this.lens = null;
		    }
		}
	    };
	lensCB = new JCheckBox("Linear lens");
	lensCB.setBackground(backgroundColor);
	lensCB.addActionListener(a0);
	cp.add(lensCB);
	for (int i=-200;i<=200;i+=40){
	    VSegment s = new VSegment(i,0,0,0,200,Color.black);
	    VSegment s2 = new VSegment(0,i,0,200,0,Color.black);
	    vsm.addGlyph(s,demoVS);vsm.addGlyph(s2,demoVS);
	}
	VImage i1=new VImage(0,0,0,(new ImageIcon(this.getClass().getResource("/images/logo-futurs-small.png"))).getImage());
	i1.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(i1,demoVS);
	vsm.repaintNow();
	vsm.getGlobalView(vs.getCamera(0),500);
	vsm.repaintNow();
    }

}
