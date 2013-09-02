/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zuist.app.wm;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JSlider;

class Preferences extends JFrame {

    WorldExplorer application;

    JSlider slider;

    Preferences(WorldExplorer app){
        super();
        this.application = app;
        slider = new JSlider(JSlider.VERTICAL, 10, 1000, 40);
        this.add(slider);
        this.setSize(100,300);
        this.pack();
        this.setVisible(true);
        this.addWindowListener(
            new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    // probably want to do something at some point
                }
            }
        );
    }

}
