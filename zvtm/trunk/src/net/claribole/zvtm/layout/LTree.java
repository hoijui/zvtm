/*   FILE: LTree.java
 *   DATE OF CREATION:   July 4 2007
 *   AUTHOR :            Boris Trofimov (trofimov@lri.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
package net.claribole.zvtm.layout;

import com.xerox.VTM.engine.VirtualSpace;

import java.awt.Color;

/**
 * Representation of abstract tree.
 * This tree can be laid out with TreeLayout instance.
 * @author Boris Trofimov
 * @see LNode
 * @see TreeLayout
 */

public class LTree {

	// colors
	Color borderColor = Color.black;
	Color backgroundColor = Color.white;
	Color textColor = Color.blue;
	Color selectedBackgroundColor = Color.lightGray;
	Color selectedTextColor = Color.blue;
	Color selectedBorderColor = Color.black;

	VirtualSpace vs;
	LNode rootLNode;
	Object owner;
	boolean drawBorders;

	/**
	 * Create instance of LTree in virtual space.
	 * @param vs virtual space where this tree will be created.
	 */
	public LTree(VirtualSpace vs) {
		this(vs, true);
	}

	/**
	 * Create instance of LTree in virtual space.
	 * @param vs virtual space where this tree will be created.
	 * @param drawBorders Indicates whethere to draw borders and backgrounds of tree nodes.
	 * If drawBorders is false, then VText class will be used to represent nodes.
	 * If drawBorders is true, then VBText class will be used instead of VText.
	 */
	public LTree(VirtualSpace vs, boolean drawBorders){
		this.vs = vs;
		this.drawBorders = drawBorders;
	}

	/**
	 * Any user object that represent the whole tree.
	 * @return owner of this tree
	 */
	public Object getOwner() {
		return owner;
	}

	/**
	 * Set object that holds any data for this tree.
	 * @param owner any object
	 */
	public void setOwner(Object owner) {
		this.owner = owner;
	}

	/**
	 * Get default border color of the nodes.
	 * @return default border color.
	 */
	public Color getDefaultBorderColor() {
		return borderColor;
	}

	/**
	 * Set color that will be used as border color for newly created nodes.
	 * @param borderColor default border color to be used.
	 */
	public void setDefaultBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * Get default background color of the nodes.
	 * @return default background color.
	 */
	public Color getDefaultBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Set color that will be used as background color for newly created nodes.
	 * @param backgroundColor default background color to be used.
	 */
	public void setDefaultBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * Get default text color of the nodes.
	 * @return default text color.
	 */
	public Color getDefaultTextColor() {
		return textColor;
	}

	/**
	 * Set color that will be used as text color for newly created nodes.
	 * @param textColor default text color to be used.
	 */
	public void setDefaultTextColor(Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * Get default text color of the nodes.
	 * @return default text color.
	 */
	public Color getDefaultSelectedBackgroundColor() {
		return selectedBackgroundColor;
	}

	public void setDefaultSelectedBackgroundColor(Color selectedBackgroundColor) {
		this.selectedBackgroundColor = selectedBackgroundColor;
	}

	public Color getDefaultSelectedTextColor() {
		return selectedTextColor;
	}

	public void setDefaultSelectedTextColor(Color selectedTextColor) {
		this.selectedTextColor = selectedTextColor;
	}

	public Color getDefaultSelectedBorderColor() {
		return selectedBorderColor;
	}

	public void setDefaultSelectedBorderColor(Color selectedBorderColor) {
		this.selectedBorderColor = selectedBorderColor;
	}

	/**
	 * Get root node of the tree
	 * @return LNode instance that represent root node
	 */
	public LNode getRoot(){
		return rootLNode;
	}

	/**
	 * Create new root node. If there is already root node, then it will be lost.
 	 * @param owner Any user object that represent root node.
	 * @param text Text of the root node.
	 * @return LNode instance that represent root node.
	 */
	public LNode createRootNode(Object owner, String text){
		rootLNode = new LNode(this, drawBorders, textColor, borderColor, backgroundColor,
				selectedTextColor, selectedBorderColor, selectedBackgroundColor);
		rootLNode.owner = owner;
		rootLNode.text = text;
		rootLNode.initLNode();
		return rootLNode;
	}

}
