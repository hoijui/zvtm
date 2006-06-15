/*   FILE: Console.java
 *   DATE OF CREATION:  Thu Mar 09 16:55:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Console.java,v 1.8 2006/04/07 14:32:49 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;

/*A simple text viewer that displays the content of a stringbuffer. Can be set to automatically update its content periodically. Can be used for showing error logs, raw source files,...*/

public class Console extends JFrame {

    static Style BLACK_STYLE, GRAY_STYLE;

    JTextPane ar;
    StyledDocument doc;
    JScrollBar sb;

    public Console(String frameTitle, int x, int y, int width, int height, boolean visible){
	ar = new JTextPane(new DefaultStyledDocument());
	doc = (StyledDocument)ar.getDocument();
	BLACK_STYLE = ar.addStyle("blackText", null);
	StyleConstants.setForeground(BLACK_STYLE, Color.black);
	GRAY_STYLE = ar.addStyle("grayText", null);
	StyleConstants.setForeground(GRAY_STYLE, Color.gray);
	ar.setEditable(false);
	JScrollPane sp=new JScrollPane(ar);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	Container cpane=this.getContentPane();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	cpane.setLayout(gridBag);
	buildConstraints(constraints,0,0,1,1,100,100);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);
	sb = sp.getVerticalScrollBar();
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){dispose();}
	    };
	this.addWindowListener(w0);
	this.setTitle(frameTitle);
	this.pack();
	this.setLocation(x,y);
	this.setSize(width,height);
	this.setVisible(visible);
    }

    public void append(final String s, final Style st){
	Runnable updater = new Runnable(){
		public void run(){doAppend(s, st);}
	    };
	SwingUtilities.invokeLater(updater);
    }

    public void append(String s){
	append(s, BLACK_STYLE);
    }

    public void doAppend(String s, Style st){
	try {
	    int len = doc.getLength();
	    if (len == 0){doc.insertString(len, s, st);}
	    else {doc.insertString(len, s, st);}
	}
	catch (BadLocationException ex){
	    ex.printStackTrace();
	}
    }

    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }

}
