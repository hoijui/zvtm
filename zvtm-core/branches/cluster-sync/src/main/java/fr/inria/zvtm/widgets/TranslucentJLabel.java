/*   Copyright (c) INRIA, 2010-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.widgets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JLabel;

import fr.inria.zvtm.widgets.TranslucentWidget;

public class TranslucentJLabel extends JLabel implements TranslucentWidget {

    AlphaComposite bgAC = AB_08;
    AlphaComposite fgAC = AB_10;

    boolean drawBorder = true;

    public TranslucentJLabel() {
        super();
        init();
    }

    public TranslucentJLabel(Icon image) {
        super();
        init();
    }

    // Creates a JLabel instance with the specified image.
    public TranslucentJLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        init();
    }

    // Creates a JLabel instance with the specified image and horizontal
    // alignment.
    public TranslucentJLabel(String text) {
        super(text);
        init();
    }

    // Creates a JLabel instance with the specified text.
    public TranslucentJLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        init();

    }

    // Creates a JLabel instance with the specified text, image, and horizontal
    // alignment.
    public TranslucentJLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        init();
    }

    // Creates a JLabel instance with the specified text and horizontal
    // alignment.

    void init() {
        setOpaque(false);
        initColors();
    }

    void initColors() {
        setForeground(Color.WHITE);
        setBackground(Color.BLACK);
    }

    /**
     * Set the translucence value of this text area's background.
     *
     * @param alpha
     *            blending value, in [0.0,1.0]. Default is 0.8
     */
    public void setBackgroundTranslucence(float alpha) {
        this.bgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    /**
     * Set the translucence value of this text area's foreground.
     *
     * @param alpha
     *            blending value, in [0.0,1.0]. Default is 1.0
     */
    public void setForegroundTranslucence(float alpha) {
        this.fgAC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    /**
     * Should the widget's border be drawn or not.
     */
    public void setDrawBorder(boolean b){
        drawBorder = b;
    }

    /**
     * Is the widget's border drawn or not.
     */
    public boolean getDrawBorder(){
        return drawBorder;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(bgAC);
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(fgAC);
        if (drawBorder){
            g2d.setColor(getForeground());
            g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
        super.paint(g2d);
    }

}
