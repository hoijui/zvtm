/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.widgets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import javax.swing.JTextArea;
import javax.swing.JLayeredPane;
import javax.swing.OverlayLayout;

/** Translucent text area, typically used in an overlay pane.
<p>Example of use, setting a white text on a dark translucent background:</p>
<pre>
JFrame f = ...;
JLayeredPane lp = f.getRootPane().getLayeredPane();
lp.setLayout(new OverlayLayout(lp));
JTextArea t = new TranslucentTextArea(...);
t.setForeground(Color.WHITE);
t.setBackground(Color.BLACK);
lp.add(t, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
t.setBounds(x, y, w, h);
</pre>
*@author Emmanuel Pietriga
*@since 0.9.7
*/

public class TranslucentTextArea extends JTextArea {
		
	AlphaComposite bgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f);
	AlphaComposite fgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
	
	TranslucentTextArea(){
		super();
	}
	
	TranslucentTextArea(javax.swing.text.Document doc){
		super(doc);
		setOpaque(false);
	}
	
	TranslucentTextArea(javax.swing.text.Document doc, String text, int rows, int columns){
		super(doc, text, rows, columns);
		setOpaque(false);
	}
	
	TranslucentTextArea(int rows, int columns){
		super(rows, columns);
		setOpaque(false);
	}
	
	TranslucentTextArea(String text){
		super(text);
		setOpaque(false);
	}

	TranslucentTextArea(String text, int rows, int columns){
		super(text, rows, columns);
		setOpaque(false);
	}
	
	/**Set the translucence value of this text area's background.
	 *@param alpha blending value, in [0.0,1.0]. Default is 0.8
	 */
	public void setBackgroundTranslucence(float alpha){
		this.bgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	/**Set the translucence value of this text area's background.
	 *@param alpha blending value, in [0.0,1.0]. Default is 1.0
	 */
	public void setForegroundTranslucence(float alpha){
		this.fgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}
	
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setComposite(bgAC);
		g2d.setColor(getBackground());
		g2d.fillRect(0,0,getWidth(),getHeight());
		g2d.setComposite(fgAC);
		super.paint(g2d);
	}
	
}
