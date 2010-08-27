/*   FILE: GLEView.java
 *   DATE OF CREATION:   Tue Oct 12 09:10:24 2004
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import fr.inria.zvtm.engine.ViewEventHandler;

  /**
   * An external view is a window and can be composed of one or several cameras superimposed (uses a standard JFrame)<br>
   * Uses OpenGL acceletation provided by J2SE 5.0<br>
   * The use of GLEView requires the following Java property: -Dsun.java2d.opengl=true
   * @author Emmanuel Pietriga
   **/

public class GLEView extends View implements KeyListener{

    JFrame frame;
    JMenuBar jmb;

    /**
     *@param v list of cameras
     *@param t view name
     *@param panelWidth width of window in pixels
     *@param panelHeight height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param decorated should the view be decorated with the underlying window manager's window frame or not
     */
    protected GLEView(Vector<Camera> v,String t,int panelWidth,int panelHeight,boolean bar,boolean visible, boolean decorated){
	    this(v, t, panelWidth, panelHeight, bar, visible, decorated, null);
    }

    /**
     *@param v list of cameras
     *@param t view name
     *@param panelWidth width of window in pixels
     *@param panelHeight height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param mnb a menu bar, already configured with actionListeners already attached to items (it is just added to the view)
     *@param decorated should the view be decorated with the underlying window manager's window frame or not
     */
    protected GLEView(Vector<Camera> v,String t,int panelWidth,int panelHeight,boolean bar,boolean visible, boolean decorated, JMenuBar mnb){
	    frame=new JFrame();
        if (!decorated){frame.setUndecorated(true);}
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setJMenuBar(mnb);
        this.jmb=mnb;
        mouse=new VCursor(this);
        name=t;
        initCameras(v);
        GridBagLayout gridBag=new GridBagLayout();
        GridBagConstraints constraints=new GridBagConstraints();
        Container cpane=frame.getContentPane();
        cpane.setLayout(gridBag);
        if (bar){
            buildConstraints(constraints,0,0,1,1,100,90);
            constraints.fill=GridBagConstraints.BOTH;
            constraints.anchor=GridBagConstraints.CENTER;
            panel=new GLViewPanel(v,this, false);
            panel.setSize(panelWidth,panelHeight);
            gridBag.setConstraints(panel,constraints);
            cpane.add(panel);
            buildConstraints(constraints,0,1,1,1,0,0);
            constraints.anchor=GridBagConstraints.WEST;
            statusBar=new JLabel(" ");
            gridBag.setConstraints(statusBar,constraints);
            cpane.add(statusBar);
        }
        else {
            buildConstraints(constraints,0,0,1,1,100,90);
            constraints.fill=GridBagConstraints.BOTH;
            constraints.anchor=GridBagConstraints.CENTER;
            panel=new GLViewPanel(v,this, false);
            panel.setSize(panelWidth,panelHeight);
            gridBag.setConstraints(panel,constraints);
            cpane.add(panel);
        }
        frame.setTitle(t);
        WindowListener l=new WindowAdapter(){
            public void windowClosing(WindowEvent e){close();}
            public void windowActivated(WindowEvent e){activate();}
            public void windowDeactivated(WindowEvent e){deactivate();}
            public void windowIconified(WindowEvent e){iconify();}
            public void windowDeiconified(WindowEvent e){deiconify();}
        };
        frame.addWindowListener(l);
        frame.addKeyListener(this);
        frame.pack();
        frame.setSize(panelWidth,panelHeight);
        if (visible){frame.setVisible(true);}
    }

    @Override
    public Container getFrame(){return frame;}

    @Override
    public boolean isSelected(){
	    return (this.frame==VirtualSpaceManager.INSTANCE.activeJFrame);
    }

    @Override
    public void setLocation(int x,int y){frame.setLocation(x,y);}

    @Override
    public void setTitle(String t){frame.setTitle(t);}

    @Override
    public void setSize(int width, int height){frame.setSize(width, height);}

    @Override
    public void setResizable(boolean resizable){frame.setResizable(resizable);}

    @Override
    public void setVisible(boolean b){
        frame.setVisible(b);
        if (b){this.activate();}
        else {this.deactivate();}
    }
    
    @Override
    public void destroyView(){
        panel.stop();
        VirtualSpaceManager.INSTANCE.destroyView(this.name);
        frame.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e){
        if (e.isShiftDown()) {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
            else {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
        }
        else {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
            else {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
        }
    }

    @Override
    public void keyPressed(KeyEvent e){
        if (e.isShiftDown()) {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
            else {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
        }
        else {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
            else {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
        }
    }

    @Override
    public void keyReleased(KeyEvent e){
        if (e.isShiftDown()) {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
            else {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
        }
        else {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
            else {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
        }
    }

}
