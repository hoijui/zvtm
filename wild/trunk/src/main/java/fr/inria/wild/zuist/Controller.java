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

import java.util.Vector;

import java.io.File;
import java.net.InetAddress;

import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.OSCMessage;

import fr.inria.zuist.engine.SceneManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class Controller extends JFrame implements ActionListener {
    
    static final String MOVE_CAMERA = "/moveCam";
    
    static final short TRANSLATE_WEST = 0;
    static final short TRANSLATE_NORTH = 1;
    static final short TRANSLATE_EAST = 2;
    static final short TRANSLATE_SOUTH = 3;
        
    // sequence should match above values
    static final String[] MOVE_STR = {"West", "North", "East", "South", "Higher", "Lower", "Global"};
    
    Integer TRANSLATE_VALUE = new Integer(1000);

    JButton[] translateBts;

    // following arrays have same length, viewport at index i is associated with sender at index i
    ViewPort[] viewports;
    OSCPortOut[] senders;
    
    WallConfiguration wc;
        
    public Controller(File configFile){
        super();
        Container c = this.getContentPane();
        c.setLayout(new FlowLayout());
        translateBts = new JButton[MOVE_STR.length];
        for (int i=0;i<MOVE_STR.length;i++){
            translateBts[i] = new JButton(MOVE_STR[i]);
            c.add(translateBts[i]);
            translateBts[i].addActionListener(this);
        }
        this.pack();
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){System.exit(0);}});
        wc = new WallConfiguration(configFile);
        initOSC();
    }
    
    void initOSC(){
        try {
            Vector vp = new Vector();
            Vector sd = new Vector();
            ClusterNode[] nodes = wc.getNodes();
            for (int i=0;i<nodes.length;i++){
                for (int j=0;j<nodes[i].getViewPorts().length;j++){
                    ViewPort p = nodes[i].getViewPorts()[j];
                    System.out.println("Creating for "+p.getPort()+" for "+p.getNode().getHostName());
                    vp.add(p);
                    sd.add(new OSCPortOut(InetAddress.getByName(p.getNode().getHostName()), p.getPort()));
                }
            }
            if (vp.size() != sd.size()){
                System.out.println("Error while initializing OSC senders: viewport count does not match osc sender count");
            }
            viewports = (ViewPort[])vp.toArray(new ViewPort[vp.size()]);
            senders = (OSCPortOut[])sd.toArray(new OSCPortOut[sd.size()]);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e){
        Object src = e.getSource();
        for (int i=0;i<translateBts.length;i++){
            if (src == translateBts[i]){
                move(i);
                break;
            }
        }
    }
    
    void move(int direction){
        for (int i=0;i<senders.length;i++){
            sendMsg(senders[i], MOVE_CAMERA, MOVE_STR[direction], TRANSLATE_VALUE);            
        }
    }

    void sendMsg(OSCPortOut sender, String listener, String cmd, Integer value){
        Object args[] = new Object[2];
        args[0] = cmd;
        args[1] = value;
        OSCMessage msg = new OSCMessage(listener, args);
        try {
            sender.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        new Controller(new File(args[0]));
    }
    
}
