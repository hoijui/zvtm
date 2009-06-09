/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ViewerEventHandler.java 2008 2009-06-09 07:53:35Z epietrig $
 */

package fr.inria.wild.zuist;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.OSCMessage;

public class Controller extends JFrame implements ActionListener {
    
    JButton[] translateBts;
    
    OSCPortOut sender;

    static final String MOVE_CAMERA = "/moveCam";
    
    static final short TRANSLATE_WEST = 0;
    static final short TRANSLATE_NORTH = 1;
    static final short TRANSLATE_EAST = 2;
    static final short TRANSLATE_SOUTH = 3;
    
    static final String[] TRANSLATE_STR = {"West", "North", "East", "South"};
    
    Float TRANSLATE_VALUE = new Float(1000);
    
    public Controller(){
        super();
        Container c = this.getContentPane();
        c.setLayout(new FlowLayout());
        for (int i=0;i<TRANSLATE_STR.length;i++){
            translateBts[i] = new JButton(TRANSLATE_STR[i]);
            c.add(translateBts[i]);
            translateBts[i].addActionListener(this);
        }
        this.pack();
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){System.exit(0);}});
        initOSC();
    }
    
    void initOSC(){
        try {
            sender = new OSCPortOut();            
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e){
        Object src = e.getSource();
        for (int i=0;i<translateBts.length;i++){
            if (src == translateBts[i]){
                translate(i);
                break;
            }
        }
    }
    
    void translate(int direction){
        sendMsg(MOVE_CAMERA, TRANSLATE_STR[direction], TRANSLATE_VALUE);
    }

    void sendMsg(String listener, String cmd, Float value){
        Object args[] = new Object[2];
    	args[0] = cmd;
    	args[1] = value;
    	OSCMessage msg = new OSCMessage(listener, args);
    	System.out.println("Sending "+msg);
    	 try {
    		sender.send(msg);
    	 } catch (Exception e) {
    		 System.out.println("Couldn't send");
    	 }
    }
    
    public static void main(String[] args){
        new Controller();
    }
    
}
