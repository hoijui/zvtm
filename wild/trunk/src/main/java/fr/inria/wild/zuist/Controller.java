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
    
    JButton upBt, downBt, leftBt, rightBt;
    
    OSCPortOut sender;
    
    Long TRANSLATION = new Long(1000);
    
    public Controller(){
        super();
        Container c = this.getContentPane();
        c.setLayout(new FlowLayout());
        upBt = new JButton("Up");
        downBt = new JButton("Down");
        leftBt = new JButton("Left");
        rightBt = new JButton("Right");
        c.add(upBt);
        c.add(downBt);
        c.add(leftBt);
        c.add(rightBt);
        upBt.addActionListener(this);
        downBt.addActionListener(this);
        leftBt.addActionListener(this);
        rightBt.addActionListener(this);
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
        if (src == upBt){
            sendMsg("UP");
        }
        else if (src == downBt){
            sendMsg("DOWN");
        }
        else if (src == leftBt){
            sendMsg("LEFT");
        }
        else if (src == rightBt){
            sendMsg("RIGHT");
        }
    }

    void sendMsg(String cmd){
        Object args[] = new Object[2];
    	args[0] = cmd;
    	args[1] = TRANSLATION;
    	OSCMessage msg = new OSCMessage("/translate", args);
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
