package cmsc420_s22;

public class HBkdTree<LPoint extends LabeledPoint2D> {
	
	private class KDNode {

		private LPoint point; // the associated point
		private int cutDim; // cutting dimension (0 == x, 1 == y)
		private KDNode left, right; // children

		public KDNode(LPoint point, int cutDim) { // leaf constructor
			this.point = point;
			this.cutDim = cutDim;
			left = right = null;
		}

		boolean onLeft(LPoint pt) { // in the left subtree? (for Labeled points)
			return pt.get(cutDim) < point.get(cutDim);
		}

		boolean onLeft(Point2D pt) { // in the left subtree? (for points)
			return pt.get(cutDim) < point.get(cutDim);
		}

		public String toString() { // string representation
			String cut = (cutDim == 0 ? "x" : "y");
			return "(" + cut + "=" + point.get(cutDim) + point.toString();
		}
	}

	// -----------------------------------------------------------------
	// Recursive helpers for main functions
	// -----------------------------------------------------------------

	/**
	 * Finds a point in the node's subtree.
	 */
	LPoint find(KDNode p, Point2D pt) { // find point in subtree
		if (p == null) {
			return null;
		} else if (p.point.getPoint2D().equals(pt)) {
			return p.point;
		} else if (p.onLeft(pt)) {
			return find(p.left, pt);
		} else {
			return find(p.right, pt);
		}
	}

	/**
	 * Insert a point in the node's subtree. Uses the standard alternating
	 * cutting dimension rule.
	 */
	KDNode insert(LPoint pt, KDNode p, int cd) throws Exception {
		if (p == null) {
			return new KDNode(pt, cd);
		} else if (pt.getPoint2D().equals(p.point.getPoint2D())) {
			throw new Exception("Attempt to insert a duplicate point");
		} else if (p.onLeft(pt)) { // insert on appropriate side
			p.left = insert(pt, p.left, 1-cd);
		} else {
			p.right = insert(pt, p.right, 1-cd);
		}
		return p;
	}

	/**
	 * Delete a point from node's subtree.
	 */
	KDNode delete(Point2D pt, KDNode p) throws Exception {
		if (p == null) { // fell out of tree?
			throw new Exception("Attempt to delete a nonexistent point");
		} else if (pt.equals(p.point.getPoint2D())) { // found it
			if (p.right != null) { // can replace from right
				p.point = findMin(p.right, p.cutDim); // find and copy replacement
				p.right = delete(p.point.getPoint2D(), p.right); // delete from right
			} else if (p.left != null) { // can replace from left
				p.point = findMin(p.left, p.cutDim); // find and copy replacement
				p.right = delete(p.point.getPoint2D(), p.left); // delete left but move to right!!
				p.left = null; // left subtree is now empty
			} else { // deleted point in leaf
				p = null; // remove this leaf
			}
		} else if (p.onLeft(pt)) {
			p.left = delete(pt, p.left); // delete from left subtree
		} else { // delete from right subtree
			p.right = delete(pt, p.right);
		}
		return p;
	}

	/**
	 * Find min node in subtree along coordinate i.
	 */
	LPoint findMin(KDNode p, int i) {
		if (p == null) { // fell out of tree?
			return null;
		} else if (p.cutDim == i) { // cutting dimension matches i?
			if (p.left == null) { // no left child?
				return p.point; // use this point
			} else {
				return findMin(p.left, i); // get min from left subtree
			}
		} else { // check both sides and this point as well
			return min(i, p.point, min(i, findMin(p.left, i), findMin(p.right, i)));
		}
	}

	/**
	 * Return the minimum non-null point w.r.t. coordinate i.
	 */
	LPoint min(int i, LPoint pt1, LPoint pt2) {
		if (pt1 == null) {
			return pt2;
		} else if (pt2 == null) {
			return pt1;
		} else if (pt1.get(i) < pt2.get(i)) {
			return pt1;
		} else {
			return pt2;
		}
	}

	// -----------------------------------------------------------------
	// Private data
	// -----------------------------------------------------------------

	public KDNode root; // root of the tree
	public Rectangle2D bbox; // the bounding box
	private int nPoints; // number of points in the tree

	// -----------------------------------------------------------------
	// Public members
	// -----------------------------------------------------------------

	/**
	 * Creates an empty tree.
	 */
	public HBkdTree(Rectangle2D bbox) {
		root = null;
		nPoints = 0;
		this.bbox = new Rectangle2D(bbox);
	}

	/**
	 * Number of entries in the dictionary.
	 */
	public int size() {
		return nPoints;
	}

	/**
	 * Find an point in the tree.
	 */
	public LPoint find(Point2D pt) {
		return find(root, pt);
	}

	/**
	 * Insert a point
	 */
	public void insert(LPoint pt) throws Exception {
		if (!bbox.contains(pt.getPoint2D())) {
			throw new Exception("Attempt to insert a point outside bounding box");
		} else {
			root = insert(pt, root, 0); // insert the point
		}
		nPoints += 1; // one more point
	}

	/**
	 * Delete a point. Note that the point being deleted does not need to match
	 * fully. It suffices that it has enough information to satisfy the comparator.
	 */
	public void delete(Point2D pt) throws Exception {
		root = delete(pt, root); // delete the point
		nPoints -= 1; // one fewer point
	}

	/**
	 * Remove all items, resulting in an empty tree
	 */
	public void clear() {
		root = null;
		nPoints = 0;
	}
	
	public LPoint rootPoint() {
		return root.point;
	}
	
	public LPoint nearestNeighbor(LPoint q, KDNode p, Rectangle2D cell, LPoint best) {
		if (p != null) {
			if (q.getPoint2D().distance(p.point.getPoint2D()) < q.getPoint2D().distance(best.getPoint2D())) // p.point is closer?
				best = p.point; // p.point is new best
			int cd = p.cutDim; // cutting dimension
			Rectangle2D leftCell = cell.leftPart(cd, p.point.get(cd)); // left child’s cell
			Rectangle2D rightCell = cell.rightPart(cd, p.point.get(cd)); // right child’s cell
			if (q.get(cd) < p.point.get(cd)) { // q is closer to left
				best = nearestNeighbor(q, p.left, leftCell, best);
				if (rightCell.distanceSq(q.getPoint2D()) < q.getPoint2D().distanceSq(best.getPoint2D())) { // is right viable?
					best = nearestNeighbor(q, p.right, rightCell, best);
				}
			} else { // q is closer to right
				best = nearestNeighbor(q, p.right, rightCell, best);
				if (leftCell.distanceSq(q.getPoint2D()) < q.getPoint2D().distanceSq(best.getPoint2D())) { // is left viable?
					best = nearestNeighbor(q, p.left, leftCell, best);
				}
			}
		}
		return best;	
	}
}
