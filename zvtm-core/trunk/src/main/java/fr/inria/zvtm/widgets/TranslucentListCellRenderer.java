/*   AUTHOR :           Julien Husson (jhusson@lri.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Identifies components that can be used as "rubber stamps" to paint the cells
 * in a JList
 * 
 * @author julien-h
 * 
 */

public class TranslucentListCellRenderer extends JLabel implements
		ListCellRenderer {

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	private AttributedString as;
	private int startColor = 0;
	private int endColor = 1;

	public Component getListCellRendererComponent(JList list, Object value, // value
			// to
			// display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY), BorderFactory
				.createEmptyBorder(1, 6, 1, 3)));

		setText(value.toString());
		setBackground(isSelected ? Color.BLACK : Color.DARK_GRAY);
		setForeground(isSelected ? list.getSelectionForeground() : list
				.getForeground());
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		/*
		 * if (value.toString().length() > 0) { as = new
		 * AttributedString(value.toString());
		 * as.addAttribute(TextAttribute.FOREGROUND, Color.RED, startColor,
		 * endColor); }
		 */

		return this;
	}

	/*
	 * public void paint(Graphics g) { g.drawString(as.getIterator(), 4, 12); }
	 * 
	 * public void setInterval(int start, int end) { startColor = start;
	 * endColor = end; }
	 */
}
