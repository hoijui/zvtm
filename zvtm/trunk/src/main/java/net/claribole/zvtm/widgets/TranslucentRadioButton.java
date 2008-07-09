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
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JRadioButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.BorderFactory;

/** Translucent text area, typically used in an overlay pane.
<p>Example of use, setting a white text on a dark translucent background:</p>
<pre>
JFrame f = ...;
JLayeredPane lp = f.getRootPane().getLayeredPane();
JButton bt = new TranslucentRadioButton(...);
lp.add(bt, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
bt.setBounds(x, y, w, h);
</pre>
*@author Emmanuel Pietriga
*@since 0.9.7
*/

public class TranslucentRadioButton extends JRadioButton implements TranslucentWidget {
	
	AlphaComposite bgAC = AB_08;
	AlphaComposite bgACro = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f);
	AlphaComposite fgAC = AB_10;
	
	/** Background rollover color. */
	Color bgColro = Color.GRAY;
	/** Foreground selected/pressed color. */
	Color fgColsel = Color.LIGHT_GRAY;
	/** Foreground color. */
	Color fgCol = Color.WHITE;
	/** Background color. */
	Color bgCol = Color.BLACK;
	
	boolean cursorInside = false;
	boolean buttonPressed = false;
	
	public JRadioButton(){
		super();
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
	
	public JRadioButton(Action a){
		super(a);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
	
	public JRadioButton(Icon icon){
		super(icon);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
	
	public JRadioButton(Icon icon, boolean selected){
		super(icon, selected);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
	
	public JRadioButton(String text){
		super(text);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
	
	public JRadioButton(String text, boolean selected){
		super(text, selected);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
	
	public JRadioButton(String text, Icon icon){
		super(text, icon);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
	
	public JRadioButton(String text, Icon icon, boolean selected){
		super(text, icon, selected);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		initColors();
		initListeners();
	}
		
	void initColors(){
        setForeground(fgCol);
        setBackground(bgCol);
	}

	void initListeners(){
		addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e){
				cursorInside = true;
				setBackground(bgColro);
				repaint();
			}
			public void mouseExited(MouseEvent e){
				cursorInside = false;
				setBackground(bgCol);
				repaint();
			}
			public void mousePressed(MouseEvent e){
				buttonPressed = true;
				setForeground(fgColsel);
				repaint();
			}
			public void mouseReleased(MouseEvent e){
				buttonPressed = false;
				setForeground(fgCol);	
				repaint();			
			}
		});
	}
	
	/**Set the translucence value of this button's background.
	 *@param alpha blending value, in [0.0,1.0]. Default is 0.8
	 */
	public void setBackgroundTranslucence(float alpha){
		this.bgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	/**Set the translucence value of this button's foreground.
	 *@param alpha blending value, in [0.0,1.0]. Default is 1.0
	 */
	public void setForegroundTranslucence(float alpha){
		this.fgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}
	
	/**Set the translucence value of this button's background when the cursor is hover it.
	 *@param alpha blending value, in [0.0,1.0]. Default is 1.0
	 */	
	public void setRolloverBackgroundTranslucence(float alpha){
		this.bgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}
	
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		if (cursorInside){
			g2d.setComposite(bgACro);
		}
		else {
			g2d.setComposite(bgAC);
		}
		g2d.setColor(getBackground());
		g2d.fillRect(0,0,getWidth(),getHeight());
		g2d.setComposite(fgAC);
		g2d.setColor(getForeground());
		g2d.drawRect(0,0,getWidth()-1,getHeight()-1);
		super.paint(g2d);
	}
	
}
