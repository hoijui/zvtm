/*   Copyright (c) INRIA, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.lens;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LensMonitor extends JFrame {

    FixedSizeLens lens;
    LensPanel lp;

    public LensMonitor(FixedSizeLens l){
        super();
        Container c = this.getContentPane();
        lp = new LensPanel(this);
        c.setLayout(new BorderLayout());
        c.add(lp, BorderLayout.CENTER);
        this.setLens(l);
    }

    public void setLens(FixedSizeLens l){
        this.lens = l;
        updateDimensions();
    }

    void updateDimensions(){
        setSize(lens.mbw, lens.mbh);
    }

}

class LensPanel extends JPanel {

    LensMonitor lm;

    LensPanel(LensMonitor lm){
        super();
        this.lm = lm;
    }

    public void paint(Graphics g){
        g.drawImage(lm.lens.mbi, 0, 0, null);
    }

}
