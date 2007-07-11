/*   FILE: TreeLayout.java
 *   DATE OF CREATION:   July 4 2007
 *   AUTHOR :            Boris Trofimov (trofimov@lri.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
package net.claribole.zvtm.layout;

import java.awt.geom.Point2D;
import java.util.Arrays;


/**
 * Layout algorithm to layout LTree objects.
 */
public class TreeLayout {

	private static final String PARAMS = "Node_PARAMS";

	private LTree tree;

	private Point2D anchor = null;
	private int orientation;	// the orientation of the tree
	private double bspace = 5;	// the spacing between sibling children
	private double tspace = 25;	// the spacing between subtrees
	private double dspace = 50;	// the spacing between depth levels
	
	private double[] depths = new double[10];
	private int maxDepth = 0;

	private double anchorX, anchorY; // for holding anchor co-ordinates
	private int camIndex;

	/**
	 * Creates instance of layout algorithm.
	 * @param tree LTree instance to be layouted.
	 */
	public TreeLayout(LTree tree) {
		this.tree = tree;
	}

	/**
	 * Creates instance of layout algorithm.
	 * @param tree LTree instance to be layouted.
	 * @param orientation Orientation is one of the TreeOrientation constants.
	 * @param dspace the spacing between depth levels
	 * @param bspace the spacing between sibling children
	 * @param tspace the spacing between subtrees
	 * @see TreeOrientation
	 */
	public TreeLayout(LTree tree, int orientation,
					  double dspace, double bspace, double tspace) {
		this.tree = tree;
		this.orientation = orientation;
		this.dspace = dspace;
		this.bspace = bspace;
		this.tspace = tspace;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setDepthSpacing(double d) {
		dspace = d;
	}

	public double getDepthSpacing() {
		return dspace;
	}

	public void setBreadthSpacing(double b) {
		bspace = b;
	}

	public double getBreadthSpacing() {
		return bspace;
	}

	public void setSubtreeSpacing(double s) {
		tspace = s;
	}

	public double getSubtreeSpacing() {
		return tspace;
	}

	/**
	 * Get anchor of the tree in virtual space (position of the root element)
	 * @return Layout anchor
	 */
	public Point2D getLayoutAnchor() {
		if (anchor == null){
			anchor = new Point2D.Double(0, 0);
		}
		return anchor;
	}

	/**
	 * Set layout anchor - position of the root element.
	 * @param a Point in virtual space
	 */
	public void setLayoutAnchor(Point2D a) {
		anchor = a;
	}

	private double spacing(LNode l, LNode r, boolean siblings) {
		boolean w = (orientation == TreeOrientation.TOP_BOTTOM ||
				orientation == TreeOrientation.BOTTOM_TOP);
		return (siblings ? bspace : tspace) + 0.5 *
				(w ? l.getBounds(camIndex).x + r.getBounds(camIndex).x
						: l.getBounds(camIndex).y + r.getBounds(camIndex).y);
	}

	private void updateDepths(int depth, LNode item) {
		boolean v = (orientation == TreeOrientation.TOP_BOTTOM ||
				orientation == TreeOrientation.BOTTOM_TOP);
		double d = (v ? item.getBounds(camIndex).y
				: item.getBounds(camIndex).x);
		if (depths.length <= depth) {
			double[] tmpArray = new double[3 * depth / 2];
			System.arraycopy(depths, 0, tmpArray, 0, depths.length);
			depths = tmpArray;
		}
		depths[depth] = Math.max(depths[depth], d);
		maxDepth = Math.max(maxDepth, depth);
	}

	private void determineDepths() {
		for (int i = 1; i < maxDepth; ++i)
			depths[i] += depths[i - 1] + dspace;
	}

	// ------------------------------------------------------------------------

	/**
	 * Layout all elements of the LTree. This method updates position of each LNode instance in LTree tree.
	 * @param cameraIndex index of active camera. Used to retrieve bounds of VText and VBText instances.
	 * @see LNode, LTree
	 */
	public void doLayout(int cameraIndex) {
		camIndex = cameraIndex;
		Arrays.fill(depths, 0);
		maxDepth = 0;

		Point2D a = getLayoutAnchor();
		anchorX = a.getX();
		anchorY = a.getY();

		LNode root = tree.rootLNode;
		Params rp = getParams(root);

		// do first pass - compute breadth information, collect depth info
		firstWalk(root, 0, 1);

		// sum up the depth info
		determineDepths();

		// do second pass - assign layout positions
		secondWalk(root, null, -rp.prelim, 0);

		tree.getRoot().updateNode(getOrientation(), camIndex);
	}

	private void firstWalk(LNode n, int num, int depth) {
		Params np = getParams(n);
		np.number = num;
		updateDepths(depth, n);

		boolean expanded = n.isExpanded();
		if (!n.hasChildren() || !expanded) // is leaf
		{
			LNode l = n.getPreviousSibling();
			if (l == null) {
				np.prelim = 0;
			}
			else {
				np.prelim = getParams(l).prelim + spacing(l, n, true);
			}
		}
		else {
			LNode leftMost = n.getFirstChild();
			LNode rightMost = n.getLastChild();
			LNode defaultAncestor = leftMost;
			LNode c = leftMost;
			for (int i = 0; c != null; ++i, c = c.getNextSibling()) {
				firstWalk(c, i, depth + 1);
				defaultAncestor = apportion(c, defaultAncestor);
			}

			executeShifts(n);

			double midpoint = 0.5 *
					(getParams(leftMost).prelim + getParams(rightMost).prelim);

			LNode left = n.getPreviousSibling();
			if (left != null) {
				np.prelim = getParams(left).prelim + spacing(left, n, true);
				np.mod = np.prelim - midpoint;
			}
			else {
				np.prelim = midpoint;
			}
		}
	}

	private LNode apportion(LNode v, LNode a) {
		LNode w = v.getPreviousSibling();
		if (w != null) {
			LNode vip, vim, vop, vom;
			double sip, sim, sop, som;

			vip = vop = v;
			vim = w;
			vom = vip.parent.getFirstChild();

			sip = getParams(vip).mod;
			sop = getParams(vop).mod;
			sim = getParams(vim).mod;
			som = getParams(vom).mod;

			LNode nr = nextRight(vim);
			LNode nl = nextLeft(vip);
			while (nr != null && nl != null) {
				vim = nr;
				vip = nl;
				vom = nextLeft(vom);
				vop = nextRight(vop);
				getParams(vop).ancestor = v;
				double shift = (getParams(vim).prelim + sim) -
						(getParams(vip).prelim + sip) + spacing(vim, vip, false);
				if (shift > 0) {
					moveSubtree(ancestor(vim, v, a), v, shift);
					sip += shift;
					sop += shift;
				}
				sim += getParams(vim).mod;
				sip += getParams(vip).mod;
				som += getParams(vom).mod;
				sop += getParams(vop).mod;

				nr = nextRight(vim);
				nl = nextLeft(vip);
			}
			if (nr != null && nextRight(vop) == null) {
				Params vopp = getParams(vop);
				vopp.thread = nr;
				vopp.mod += sim - sop;
			}
			if (nl != null && nextLeft(vom) == null) {
				Params vomp = getParams(vom);
				vomp.thread = nl;
				vomp.mod += sip - som;
				a = v;
			}
		}
		return a;
	}

	private LNode nextLeft(LNode n) {
		LNode c = null;
		if (n.isExpanded()) c = n.getFirstChild();
		return (c != null ? c : getParams(n).thread);
	}

	private LNode nextRight(LNode n) {
		LNode c = null;
		if (n.isExpanded()) c = n.getLastChild();
		return (c != null ? c : getParams(n).thread);
	}

	private void moveSubtree(LNode wm, LNode wp, double shift) {
		Params wmp = getParams(wm);
		Params wpp = getParams(wp);
		double subtrees = wpp.number - wmp.number;
		wpp.change -= shift / subtrees;
		wpp.shift += shift;
		wmp.change += shift / subtrees;
		wpp.prelim += shift;
		wpp.mod += shift;
	}

	private void executeShifts(LNode n) {
		double shift = 0, change = 0;
		for (LNode c = n.getLastChild(); c != null; c = c.getPreviousSibling()) {
			Params cp = getParams(c);
			cp.prelim += shift;
			cp.mod += shift;
			change += cp.change;
			shift += cp.shift + change;
		}
	}

	private LNode ancestor(LNode vim, LNode v, LNode a) {
		LNode p = v.parent;
		Params vimp = getParams(vim);
		if (vimp.ancestor.parent == p) {
			return vimp.ancestor;
		}
		else {
			return a;
		}
	}

	private void secondWalk(LNode n, LNode p, double m, int depth) {
		Params np = getParams(n);
		setBreadth(n, p, np.prelim + m);
		setDepth(n, p, depths[depth]);

		if (n.isExpanded()) {
			depth += 1;
			for (LNode c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
				secondWalk(c, n, m + np.mod, depth);
			}
		}

		np.clear();
	}

	private void setBreadth(LNode n, LNode p, double b) {
		switch (orientation) {
		case TreeOrientation.LEFT_RIGHT:
		case TreeOrientation.RIGHT_LEFT:
			setY(n, anchorY + b);
			break;
		case TreeOrientation.TOP_BOTTOM:
		case TreeOrientation.BOTTOM_TOP:
			setX(n, anchorX + b);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void setDepth(LNode n, LNode p, double d) {
		switch (orientation) {
		case TreeOrientation.LEFT_RIGHT:
			setX(n, anchorX + d);
			break;
		case TreeOrientation.RIGHT_LEFT:
			setX(n, anchorX - d);
			break;
		case TreeOrientation.TOP_BOTTOM:
			setY(n, anchorY - d);
			break;
		case TreeOrientation.BOTTOM_TOP:
			setY(n, anchorY + d);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void setX(LNode item, double x) {
		item.x = Math.round(x);
	}

	private void setY(LNode item, double y) {
		item.y = Math.round(y);
	}

	private Params getParams(LNode item) {
		Params rp = (Params) item.getClientProperty(PARAMS);
		if (rp == null) {
			rp = new Params();
			item.setClientProperty(PARAMS, rp);
		}
		if (rp.number == -2) {
			rp.init(item);
		}
		return rp;
	}

	private static class Params implements Cloneable {

		double prelim;
		double mod;
		double shift;
		double change;
		int number = -2;
		LNode ancestor = null;
		LNode thread = null;

		public void init(LNode item) {
			ancestor = item;
			number = -1;
		}

		public void clear() {
			number = -2;
			prelim = mod = shift = change = 0;
			ancestor = thread = null;
		}
	}

}
