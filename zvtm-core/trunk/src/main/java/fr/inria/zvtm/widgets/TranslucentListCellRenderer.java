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

	static final String ELLIPSIS = "...";
	static final int MAX_BOUND = 27;

	private AttributedString as;

	public TranslucentListCellRenderer(JList list) {
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY), BorderFactory
				.createEmptyBorder(1, 6, 1, 3)));

		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setBackground(Color.GRAY);
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value, // value
			// to
			// display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		if (list.getName() != null) {
			String str = ((String) value).substring(0, ((String) value)
					.length() - 4);
			if (str.length() > MAX_BOUND) {
				String cardinality = ((String) value).substring(
						((String) value).length() - 4, ((String) value)
								.length());
				value = value.toString().substring(0, MAX_BOUND) + ELLIPSIS
						+ cardinality;
			}
		}
		setText(value.toString());
		setBackground(isSelected ? Color.BLACK : Color.DARK_GRAY);
		setForeground(isSelected ? list.getSelectionForeground() : list
				.getForeground());
		if (value.toString().length() > 0) {
			as = new AttributedString(value.toString());
			as.addAttribute(TextAttribute.FOREGROUND, Color.RED, 0, 1);
		}

		return this;
	}

	protected void paintComponent(Graphics g) {
		g.drawString(as.getIterator(), 4, 12);
	}

}
