/*   FILE: LNode.java
 *   DATE OF CREATION:   July 4 2007
 *   AUTHOR :            Boris Trofimov (trofimov@lri.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
package net.claribole.zvtm.layout;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.glyphs.DPath;
import net.claribole.zvtm.glyphs.VBText;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Node of the LTree tree.
 * @see LTree
 * @see TreeLayout
 */

public class LNode {

	// colors
	Color borderColor = Color.black;
	Color backgroundColor = Color.white;
	Color textColor = Color.blue;
	Color selectedBackgroundColor = Color.lightGray;
	Color selectedTextColor = Color.black;
	Color selectedBorderColor = Color.black;
	
	Object owner;
	VText vText;
	List<LNode> children;
	LNode parent;
	LTree tree;
	DPath inEdge; // we consider that there is only one incoming edge
	LNode collapseTo = null; // this is for animating disappearing after layouting
	boolean shouldExpand = false; // this is for animating appearing after layouting
	Hashtable clientProperties = new Hashtable();
	boolean isExpanded = true;
	public long x, y;
	String text;
	boolean drawBorder = true;

	/**
	 * Use addChild() method of LNode class or createRootNode() of LTree class to create new nodes.
	 * @param parentTree Tree where this node will be created.
	 * @param drawBorder Whether to draw border and background of node.
	 */
	LNode(LTree parentTree, boolean drawBorder) {
		tree = parentTree;
		children = new ArrayList<LNode>();
		this.drawBorder = drawBorder;
	}

	LNode(LTree parentTree, boolean drawBorder, Color textColor, Color borderColor, Color backgroundColor,
				 Color selectedTextColor, Color selectedBorderColor, Color selectedBackgroundColor){
		tree = parentTree;
		children = new ArrayList<LNode>();
		this.drawBorder = drawBorder;
		this.textColor = textColor;
		this.borderColor = borderColor;
		this.backgroundColor = backgroundColor;
		this.selectedTextColor = selectedTextColor;
		this.selectedBorderColor = selectedBorderColor;
		this.selectedBackgroundColor = selectedBackgroundColor;
	}

	public LTree getTree(){
		return tree;
	}

	/**
	 * Get text of the node.
	 * @return string text.
	 */
	public String getText(){
		return text;
	}

	/**
	 * Set text of the node.
	 * @param text String for this node.
	 */
	public void setText(String text){
		this.text = text;
	}

	/**
	 * Get the color that will be used for borders if drawBorder is true.
	 * @return border color.
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * Set the color that will be used for borders if drawBorder is true.
	 * @param borderColor new border color.
	 */
	public void setBorderColor(Color borderColor) {
		if (drawBorder && isExpanded())
			vText.setBorderColor(borderColor);
		this.borderColor = borderColor;
	}

	/**
	 * Get the color that will be used for background if drawBorder is true.
	 * @return background color.
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Set the color that will be used for background if drawBorder is true.
	 * @param backgroundColor new background color.
	 */
	public void setBackgroundColor(Color backgroundColor) {
		if (drawBorder && isExpanded()){
			((VBText)vText).setBackgroundFillColor(backgroundColor);
			this.backgroundColor = backgroundColor;
		}
	}

	/**
	 * Get the color that will be used for node's text.
	 * @return text color.
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * Set the color that will be used for node's text.
	 * @param textColor new text color.
	 */
	public void setTextColor(Color textColor) {
		if (isExpanded())
			vText.setColor(textColor);
		this.textColor = textColor;
	}

	public Color getSelectedBackgroundColor() {
		return selectedBackgroundColor;
	}

	public void setSelectedBackgroundColor(Color selectedBackgroundColor) {
		if (drawBorder && !isExpanded()){
			((VBText)vText).setBackgroundFillColor(selectedBackgroundColor);
		}
		this.selectedBackgroundColor = selectedBackgroundColor;
	}

	public Color getSelectedTextColor() {
		return selectedTextColor;
	}

	public void setSelectedTextColor(Color selectedTextColor) {
		if (!isExpanded()){
			vText.setColor(selectedTextColor);
		}
		this.selectedTextColor = selectedTextColor;
	}

	public Color getSelectedBorderColor() {
		return selectedBorderColor;
	}

	public void setSelectedBorderColor(Color selectedBorderColor) {
		if (drawBorder && !isExpanded()) {
			vText.setBorderColor(selectedBorderColor);
		}
		this.selectedBorderColor = selectedBorderColor;
	}

	public Color getEdgeColor() {
		return inEdge.getColor();
	}

	public void setEdgeColor(Color edgeColor) {
		inEdge.setColor(edgeColor);
	}

	/**
	 * Get bounds of the node. As far as we use VText or VBText for drawing nodes, we should provide camera index.
	 * @param camIndex Index of active camera.
	 * @return LongPoint that represent bounds of the node.
	 */
	public LongPoint getBounds(int camIndex) {
		return vText.getBounds(camIndex);
	}

	/**
	 * Get user object that was set for this key using method setClientProperty(Object key, Object value).
	 * @param key key to retrieve value
	 * @return corresponding value
	 */
	public Object getClientProperty(Object key) {
		if (clientProperties.containsKey(key)) {
			return clientProperties.get(key);
		}
		return null;
	}

	/**
	 * Set any key-value pair to the LNode instance.
	 * @param key key for the value
	 * @param value any user object
	 */
	public void setClientProperty(Object key, Object value) {
		if (key != null && value != null) {
			clientProperties.put(key, value);
		}
	}

	public LNode getParentNode(){
		return parent;
	}

	/**
	 * Add new child to this node.
	 * @param owner Any user data
	 * @param text Text representation of the node.
	 * @param visible Visibility of newly created node.
	 * @return Just created child.
	 */
	public LNode addChild(Object owner, String text, boolean visible){
		LNode child = new LNode(tree, drawBorder, textColor, borderColor, backgroundColor,
				selectedTextColor, selectedBorderColor, selectedBackgroundColor);
		child.parent = this;
		child.owner = owner;
		child.text = text;
		child.initLNode();
		children.add(child);
		child.inEdge.visible = visible;
		child.vText.visible = visible;
		return child;
	}

	public LNode addChild(Object owner, String text){
		return addChild(owner, text, true);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public LNode[] getChildren(){
		LNode[] array = new LNode[children.size()];
		children.toArray(array);
		return array;
	}

	public LNode getFirstChild() {
		LNode result = null;
		if (hasChildren()) {
			result = children.get(0);
		}
		return result;
	}

	public LNode getLastChild() {
		LNode result = null;
		if (hasChildren()) {
			result = children.get(children.size() - 1);
		}
		return result;
	}

	public LNode getPreviousSibling() {
		LNode result = null;
		if (parent != null) {
			int myIndex = parent.children.indexOf(this);
			if (myIndex > 0) {
				result = parent.children.get(myIndex - 1);
			}
		}
		return result;
	}

	public LNode getNextSibling() {
		LNode result = null;
		if (parent != null) {
			int myIndex = parent.children.indexOf(this);
			if (myIndex < parent.children.size() - 1) {
				result = parent.children.get(myIndex + 1);
			}
		}
		return result;
	}

	/**
	 * Get child of this node at specified index.
	 * @param i index of the desired child.
	 * @return child with index i.
	 */
	public LNode getAt(int i){
		if (i > -1 && i < children.size()){
			return children.get(i);
		}
		else
			return null;
	}

	public void removeChild(LNode child, boolean deleteSubTree){
		if (children.contains(child)){
			tree.vs.destroyGlyph(child.inEdge);
			tree.vs.destroyGlyph(child.vText);
			if (!deleteSubTree){ // set my parents as parents of my children.
				for (LNode node : child.children){
					if (!children.contains(node)){
						children.add(node);
						node.parent = this;
					}
				}
			}
			else{
				for (LNode node : child.getChildren()) {
					child.removeChild(node, deleteSubTree);
				}
			}
			children.remove(child);
		}
		if (!hasChildren()){ // if there is no more children
			isExpanded = true;
			switchToExpandedView();
		}
	}

	public void deleteFromTree(boolean deleteSubTree){
		parent.removeChild(this, deleteSubTree);
	}

	void initLNode() {
		if (drawBorder)
			vText = new VBText(0, 0, 0, Color.blue, text);
		else
			vText = new VText(0, 0, 0, Color.blue, text);
		vText.setOwner(this);
		inEdge = new DPath();
		inEdge.addCbCurve(0, 0, 0, 0, 0, 0, false);
		if (parent != null) {
			LongPoint[] cp = new LongPoint[2];
			cp[0] = new LongPoint(parent.vText.vx, parent.vText.vy);
			cp[1] = new LongPoint(parent.vText.vx, parent.vText.vy);
			inEdge.editElement(0, parent.vText.vx, parent.vText.vy, parent.vText.vx, parent.vText.vy, cp, true);
			vText.vx = parent.vText.vx;
			vText.vy = parent.vText.vy;
		}
		inEdge.setForcedDrawing(true);
		tree.vs.vsm.addGlyph(inEdge, tree.vs);
		tree.vs.vsm.addGlyph(vText, tree.vs);
		tree.vs.atBottom(inEdge);
		tree.vs.atBottom(vText);
		if (isExpanded()){
			if (drawBorder){
				((VBText)vText).setBackgroundFillColor(backgroundColor);
				vText.setBorderColor(borderColor);
			}
			vText.setColor(textColor);
		}
		else{
			if (drawBorder) {
				((VBText) vText).setBackgroundFillColor(selectedBackgroundColor);
				vText.setBorderColor(selectedBorderColor);
			}
			vText.setColor(selectedTextColor);
		}
	}

	/**
	 * This method can be called after the tree was layouted with layout algorithm
	 * to update position of this node and all children.
	 * @param treeOrientation One of the TreeOrientation constants.
	 * @param camIndex Index of active camera.
	 */
	public void updateNode(int treeOrientation, int camIndex) {
		long textX = 0;
		long textY = 0;
		LongPoint[] dpath = new LongPoint[4];
		dpath[0] = new LongPoint(); // start point
		dpath[1] = new LongPoint();
		dpath[2] = new LongPoint();
		dpath[3] = new LongPoint(); // end point (to the child)
		PostAnimationAction paa = null;

		if (collapseTo != null) {
			switch (treeOrientation){
			case TreeOrientation.LEFT_RIGHT:
				textX = collapseTo.x + collapseTo.getBounds(camIndex).x - getBounds(camIndex).x;
				textY = collapseTo.y;
				dpath[0].x = textX + getBounds(camIndex).x;
				dpath[0].y = textY + getBounds(camIndex).y / 2;
				dpath[3].x = textX;
				dpath[3].y = textY + getBounds(camIndex).y / 2;
				break;
			case TreeOrientation.RIGHT_LEFT:
				textX = collapseTo.x  - collapseTo.getBounds(camIndex).x;
				textY = collapseTo.y;
				dpath[0].x = textX;
				dpath[0].y = textY + getBounds(camIndex).y / 2;
				dpath[3].x = textX + getBounds(camIndex).x;
				dpath[3].y = textY + getBounds(camIndex).y / 2;
				break;
			case TreeOrientation.TOP_BOTTOM:
				textX = collapseTo.x - getBounds(camIndex).x / 2;
				textY = collapseTo.y;
				dpath[0].x = collapseTo.x;
				dpath[0].y = collapseTo.y;
				dpath[3].x = collapseTo.x;
				dpath[3].y = collapseTo.y + getBounds(camIndex).y;
				break;
			case TreeOrientation.BOTTOM_TOP:
				textX = collapseTo.x - getBounds(camIndex).x / 2;
				textY = collapseTo.y;
				dpath[0].x = collapseTo.x;
				dpath[0].y = collapseTo.y + getBounds(camIndex).y;
				dpath[3].x = collapseTo.x;
				dpath[3].y = collapseTo.y;
				break;
			}

			updateControlPoints(dpath);
			switchToCollapsedView();
			paa = new PostAnimationAction() {
				public void animationEnded(Object target, short type, String dimension) {
					vText.visible = false;
					inEdge.visible = false;
				}
			};
			collapseTo = null;
		}
		else if (shouldExpand){

			// first, move everything to the right place.
			switch (treeOrientation) {
			case TreeOrientation.LEFT_RIGHT:
				textX = parent.vText.vx + parent.getBounds(camIndex).x - getBounds(camIndex).x;
				textY = parent.vText.vy;
				dpath[0].x = textX + getBounds(camIndex).x;
				dpath[0].y = textY + getBounds(camIndex).y / 2;
				dpath[3].x = textX;
				dpath[3].y = textY + getBounds(camIndex).y / 2;
				break;
			case TreeOrientation.RIGHT_LEFT:
				textX = parent.vText.vx;
				textY = parent.vText.vy;
				dpath[0].x = textX;
				dpath[0].y = textY + getBounds(camIndex).y / 2;
				dpath[3].x = textX + getBounds(camIndex).x;
				dpath[3].y = textY + getBounds(camIndex).y / 2;
				break;
			case TreeOrientation.TOP_BOTTOM:
				textX = parent.vText.vx + parent.getBounds(camIndex).x / 2 - getBounds(camIndex).x / 2;
				textY = parent.vText.vy;
				dpath[0].x = textX + getBounds(camIndex).x / 2;
				dpath[0].y = textY;
				dpath[3].x = textX + getBounds(camIndex).x / 2;
				dpath[3].y = textY + getBounds(camIndex).y;
				break;
			case TreeOrientation.BOTTOM_TOP:
				textX = parent.vText.vx + parent.getBounds(camIndex).x / 2 - getBounds(camIndex).x / 2;
				textY = parent.vText.vy;
				dpath[0].x = textX + getBounds(camIndex).x / 2;
				dpath[0].y = textY + getBounds(camIndex).y;
				dpath[3].x = textX + getBounds(camIndex).x / 2;
				dpath[3].y = textY;
				break;
			}
			
			LongPoint[] cp = new LongPoint[]{dpath[0], dpath[3]};
			inEdge.editElement(0, dpath[0].x, dpath[0].y, dpath[3].x, dpath[3].y, cp, true);
			vText.vx = textX;
			vText.vy = textY;
			vText.visible = true;
			inEdge.visible = true;

			// Copmute positions to be animated
			switch (treeOrientation) {
			case TreeOrientation.LEFT_RIGHT:
				textX = x;
				textY = y;
				dpath[0].x = parent.x + parent.getBounds(camIndex).x;
				dpath[0].y = parent.y + parent.getBounds(camIndex).y / 2;
				dpath[3].x = x;
				dpath[3].y = y + getBounds(camIndex).y / 2;
				break;
			case TreeOrientation.RIGHT_LEFT:
				textX = x - getBounds(camIndex).x;
				textY = y;
				dpath[0].x = parent.x - parent.getBounds(camIndex).x;
				dpath[0].y = parent.y + parent.getBounds(camIndex).y / 2;
				dpath[3].x = x;
				dpath[3].y = y + getBounds(camIndex).y / 2;
				break;
			case TreeOrientation.TOP_BOTTOM:
				textX = x - getBounds(camIndex).x / 2;
				textY = y;
				dpath[0].x = parent.x;
				dpath[0].y = parent.y;
				dpath[3].x = x;
				dpath[3].y = y + getBounds(camIndex).y;
				break;
			case TreeOrientation.BOTTOM_TOP:
				textX = x - getBounds(camIndex).x / 2;
				textY = y;
				dpath[0].x = parent.x;
				dpath[0].y = parent.y + getBounds(camIndex).y;
				dpath[3].x = x;
				dpath[3].y = y;
				break;
			}
			updateControlPoints(dpath);
			shouldExpand = false;
		}
		else {
			if (isVisible()) {
				switch (treeOrientation) {
				case TreeOrientation.LEFT_RIGHT:
					textX = x;
					textY = y;
					if (parent != null){
						dpath[0].x = parent.x + parent.getBounds(camIndex).x;
						dpath[0].y = parent.y + parent.getBounds(camIndex).y / 2;
					}
					dpath[3].x = x;
					dpath[3].y = y + getBounds(camIndex).y / 2;
					break;
				case TreeOrientation.RIGHT_LEFT:
					textX = x - getBounds(camIndex).x;
					textY = y;
					if (parent != null){
						dpath[0].x = parent.x - parent.getBounds(camIndex).x;
						dpath[0].y = parent.y + parent.getBounds(camIndex).y / 2;
					}
					dpath[3].x = x;
					dpath[3].y = y + getBounds(camIndex).y / 2;
					break;
				case TreeOrientation.TOP_BOTTOM:
					textX = x - getBounds(camIndex).x / 2;
					textY = y;
					if (parent != null){
						dpath[0].x = parent.x;
						dpath[0].y = parent.y;
					}
					dpath[3].x = x;
					dpath[3].y = y + getBounds(camIndex).y;
					break;
				case TreeOrientation.BOTTOM_TOP:
					textX = x - getBounds(camIndex).x / 2;
					textY = y;
					if (parent != null){
						dpath[0].x = parent.x;
						dpath[0].y = parent.y + parent.getBounds(camIndex).y;
					}
					dpath[3].x = x;
					dpath[3].y = y;
					break;
				}
				if (parent != null){
					updateControlPoints(dpath);
				}				
			}
		}
		LongPoint data = new LongPoint(textX - vText.vx, textY - vText.vy);
		tree.vs.vsm.animator.createGlyphAnimation(500, AnimManager.GL_TRANS_SIG, data, vText.getID());
		if (parent != null){
			tree.vs.vsm.animator.createPathAnimation(500, AnimManager.DP_TRANS_SIG_ABS, dpath, inEdge.getID(), paa);
		}
		for (LNode child : children) {
			child.updateNode(treeOrientation, camIndex);
		}
	}

	private void updateControlPoints(LongPoint[] cp) {
		long sx = cp[0].x;
		long sy = cp[0].y;
		long ex = cp[3].x;
		long ey = cp[3].y;

		double dx = ex - sx;
		double dy = ey - sy;

		cp[1] = new LongPoint(sx + 2 * dx / 3, sy);
		cp[2] = new LongPoint(ex - dx / 8, ey - dy / 8);
	}

	private void setCollapseToRecursively(LNode colTo) {
		collapseTo = colTo;
		isExpanded = false;
		for (LNode child : children) {
			child.switchToCollapsedView();
			child.setCollapseToRecursively(colTo);
		}
	}

	/**
	 * Expand this node to show all children.
	 */
	public void expand() {
		if (hasChildren()){
			isExpanded = true;
			switchToExpandedView();
			for (LNode child : children) {
				child.shouldExpand = true;
				if (child.hasChildren()){
					child.isExpanded = false;
					child.switchToCollapsedView();
				}
				else{
					child.expand();
				}
			}
		}
		else{
			isExpanded = true;
		}
	}

	/**
	 * Collapse this node to hide all children.
	 */
	public void collapse() {
		if (hasChildren()){
			isExpanded = false;
			switchToCollapsedView();
			for (LNode child : children) {
				child.setCollapseToRecursively(this);
			}
		}
		else {
			isExpanded = true;
		}

	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public boolean isVisible() {
		if (parent != null)
			return parent.isExpanded() && parent.isVisible();
		else
			return true;
	}

	private void switchToExpandedView() {
		if (drawBorder){
			vText.setBorderColor(borderColor);
			((VBText) vText).setBackgroundFillColor(backgroundColor);
		}
		vText.setColor(textColor);
	}

	private void switchToCollapsedView() {
		if (hasChildren()){
			if (drawBorder){
				vText.setBorderColor(selectedBorderColor);
				((VBText) vText).setBackgroundFillColor(selectedBackgroundColor);
			}
			vText.setColor(selectedTextColor);
		}
	}

	/**
	 * Get user object that represent this node.
	 * @return user object.
	 */
	public Object getOwner() {
		return owner;
	}

	/**
	 * Set any user object that represent this node.
	 * @param owner user object.
	 */
	public void setOwner(Object owner) {
		this.owner = owner;
	}
}
