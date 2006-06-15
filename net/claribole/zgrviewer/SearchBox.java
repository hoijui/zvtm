/*   FILE: SearchBox.java
 *   DATE OF CREATION:   Thu Jan 09 15:47:07 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *   $Id: SearchBox.java,v 1.6 2006/01/13 10:36:00 epietrig Exp $
 */

package net.claribole.zgrviewer;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
// import java.awt.event.FocusListener;
// import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;

class SearchBox extends JFrame implements ActionListener, KeyListener {

    static int FRAME_WIDTH = 300;
    static int FRAME_HEIGHT = 110;

    static Color FIND_COLOR = Color.red;

    ZGRViewer application;
    
    JButton prevBt, nextBt;
    JTextField searchText;

    /*search variables*/
    int searchIndex = 0;
    String lastSearchedString = "";
    Vector matchingList = new Vector();
//     Glyph lastMatchingEntity = null;  //remember it so that its color can be reset after the search ends
//     Color lastMatchingColor = null;

    SearchBox(ZGRViewer app){
	super();
	this.application = app;
	Container cp = this.getContentPane();
	cp.setLayout(new GridLayout(2,1));
	JPanel p1 = new JPanel();
	JPanel p2 = new JPanel();
	cp.add(p1);
	cp.add(p2);
	p1.add(new JLabel("Find:"));
	searchText = new JTextField(32);
	p1.add(searchText);
	searchText.addKeyListener(this);
	prevBt = new JButton("Previous");
	p2.add(prevBt);
	prevBt.addActionListener(this);
	nextBt = new JButton("Next");
	p2.add(nextBt);
	nextBt.addActionListener(this);
	//window
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){
// 		    resetLastMatchingEntity();
		    dispose();
		}
	    };
	this.addWindowListener(w0);
// 	this.addFocusListener(this);
	this.setTitle("Find");
	this.pack();
	this.setResizable(false);
    }

    public void actionPerformed(ActionEvent e){
	if (e.getSource() == prevBt){search(searchText.getText(), -1);}
	else {search(searchText.getText(), 1);}
   }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    search(searchText.getText(), 1);
	}
    }

    public void keyReleased(KeyEvent e){}

    public void keyTyped(KeyEvent e){}

    /*given a string, centers on a VText with this string in it*/
    void search(String s, int direction){
	if (s.length()>0){
	    if (!s.toLowerCase().equals(lastSearchedString)){//searching a new string - reinitialize everything
		resetSearch(s);
		Glyph[] gl = application.mSpace.getVisibleGlyphList();
		for (int i=0;i<gl.length;i++){
		    if (gl[i] instanceof VText){
			if ((((VText)gl[i]).getText() != null) &&
			    (((VText)gl[i]).getText().toLowerCase().indexOf(lastSearchedString)!=-1)){
			    matchingList.add(gl[i]);
			}
		    }
		}
	    }
	    int matchSize = matchingList.size();

	    if (matchSize > 0){
		//get prev/next entry in the list of matching elements
		searchIndex = searchIndex + direction;
		if (searchIndex < 0){// if reached start/end of list, go to end/start (loop)
		    searchIndex = matchSize - 1;
		}
		else if (searchIndex >= matchSize){
		    searchIndex = 0;
		}
		if (matchSize > 1){
		    application.mainView.setStatusBarText(Utils.rankString(searchIndex+1) + " of " + matchSize + " matches");
		}
		else {
		    application.mainView.setStatusBarText(matchSize + " match");
		}
		//center on the entity
		Glyph g = (Glyph)matchingList.elementAt(searchIndex);
// 		resetLastMatchingEntity();
// 		lastMatchingEntity = g;
// 		lastMatchingColor = g.getColor();
// 		lastMatchingEntity.setColor(FIND_COLOR);
		application.vsm.centerOnGlyph(g /*lastMatchingEntity*/, application.mSpace.getCamera(0), 400);
	    }
	}
    }

    /*reset the search variables after it is finished*/
    void resetSearch(String s){
	searchIndex = -1;
	lastSearchedString = s.toLowerCase();
	matchingList.removeAllElements();
    }

//     /*revert last found entity to its original color*/
//     void resetLastMatchingEntity(){
// 	if (lastMatchingEntity != null){lastMatchingEntity.setColor(lastMatchingColor);}
//     }

//     public void focusGained(FocusEvent e){}

//     public void focusLost(FocusEvent e){
// 	resetLastMatchingEntity();
//     }
}
