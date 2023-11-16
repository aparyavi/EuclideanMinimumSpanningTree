package cmsc420_s22;

import java.util.ArrayList;

import cmsc420_s22.QuakeHeap.Locator;
import cmsc420_s22.QuakeHeap.Node;

public class QuakeHeap<Key extends Comparable<Key>, Value> {
	ArrayList<Node>[] roots;
	int nodeCt[];
	private int nLevels;
	double ratio = 0.75;
	
	class Node { 
		Key key;
		Value value;
		Node leftChild;
		Node rightChild;
		Node parent;
		int level;
		// Constructor when making leaf node
		public Node(Key x, Value v) {
			this.key = x;
			this.value = v;
			this.leftChild = null;
			this.rightChild = null;
			this.parent = null;
			this.level = 0;
		}
		// Constructor when making parent node
		public Node(Key x, Value v, Node left, Node right, int level) {
			this.key = x;
			this.value = v;
			this.leftChild = left;
			this.rightChild = right;
			this.parent = null;
			this.level = level;
		}
		public Node(Key key2, Value val, int lev, QuakeHeap<Key, Value>.Node u, QuakeHeap<Key, Value>.Node v) {
			this.key = key2;
			this.value = val;
			this.level = lev;
			this.leftChild = u;
			this.rightChild = v;
			this.parent = null;
		}
	}
	
	public class Locator { 
		private Node u; // the node
		private Locator(Node u) { this.u = u; } // constructor
		private Node get() { return u; }
	}
	
	public QuakeHeap(int nLevels) { 
		// Initialize roots, nodeCt and nLevel
		this.roots = (ArrayList<Node>[]) new ArrayList[nLevels];
		for (int i = 0; i < nLevels; i++) {
			this.roots[i] = new ArrayList<Node>();
		}
		this.nodeCt = new int[nLevels];
		this.nLevels = nLevels;
	}
	
	public void clear() {
		// set every element in roots and nodeCt to empty (null or 0)
		for (int i = 0; i < this.nLevels; i++) {
			this.roots[i].clear();
			this.nodeCt[i] = 0;
		}
	}
	
	public Locator insert(Key x, Value v) {
		// Create new node and add to roots and increment nodeCt at level 0
		Node u = new Node(x, v);
		this.roots[0].add(u);
		this.nodeCt[0]++;
		return new Locator(u);
	}
	
	// Helper function to check if heap is empty
	private boolean isEmpty() {
		// Iterate through every level and check the number of nodes in each level,
		// if there is a node in a level then return false
		for (int i = 0; i < this.nLevels; i++) {
			if (nodeCt[i] != 0)
				return false;
		}
		return true;
	}
	
	// Helper function to get smallest key
	private Key smallestKey() {
		// Go through every level, get the smallest in each level, then compare
		// smallest key found to find the minimum key.
		Key smallest = null;
		for (int i = 0; i < this.nLevels; i++) {
			Key smallTemp = null;
			if (this.roots[i].size() != 0)
				smallTemp = this.roots[i].get(0).key;
			for (int j = 0; j < this.roots[i].size(); j++) {
				if (this.roots[i].get(j).key.compareTo(smallTemp) < 0)
					smallTemp = this.roots[i].get(j).key;
			}
			if (smallTemp != null && smallest != null && smallTemp.compareTo(smallest) < 0) {
					smallest = smallTemp;
			}
			else if (smallTemp != null && smallest == null) {
				smallest = smallTemp;
			}
		}
		return smallest;
	}
	
	// Helper function to combine the nodes into a tree
	private void rearrangeTree() {
		// For every level, sort roots
		for (int k = 0; k < this.nLevels - 1; k++) {
			int n = this.roots[k].size();
			  
	        // One by one move boundary of unsorted subarray
	        for (int i = 0; i < n-1; i++)
	        {
	            // Find the minimum element in unsorted array
	            int min_idx = i;
	            for (int j = i+1; j < n; j++)
	                if (this.roots[k].get(j).key.compareTo(this.roots[k].get(min_idx).key) < 0)
	                    min_idx = j;
	  
	            // Swap the found minimum element with the first
	            // element
	            Node temp = this.roots[k].get(min_idx);
	            this.roots[k].set(min_idx, this.roots[k].get(i));
	            this.roots[k].set(i, temp);
	        }
	        // Remove roots 0 and 1 for this level and put it under a parent
	        // node we create at level, k+1
	        if (this.roots[k].size() >= 2) {
	        	int size = this.roots[k].size();
	        	for (int z = 0; z < size - 1; z = z + 2) {
		        	System.out.println(z);
		        	Node u = this.roots[k].get(0);
		        	Node v = this.roots[k].get(1);
		        	Node w = new Node(u.key, u.value, u, v, k+1);
		        	nodeCt[k+1]++;
		        	u.parent = w;
		        	v.parent = w;
		        	this.roots[k].remove(0);
		        	this.roots[k].remove(0);
		        	this.roots[k+1].add(w);
	        	}
	        }
		}
	}
	
	public Key getMinKey() throws Exception {
		// Call all three helper functions (isEmpty(),
		// smallestKey(), and rearrangeTree()
		if (isEmpty())
			throw new Exception("Empty heap");

		Key minKey = smallestKey();
		rearrangeTree();
		return minKey;
	}
	
	// Helper function to get the max level
	private int getHeight(Node r) {
		// Do a recursive call on getHeight() and add 1 each time to it until
		// we hit a null or parent doesn't have the same key
		if (r == null)
			return 0;
		if (r.parent != null && r.key.compareTo(r.parent.key) == 0)
			return getHeight(r.parent) + 1;
		return 1;
	}
	
	public int getMaxLevel(Locator r) {
		// call helper getHeight() add subtract 1 from it
		int result = getHeight(r.get()) - 1;
		return result;
	}
	
	// Helper to get each node from tree
	private ArrayList<String> listHeapHelper(Node root, ArrayList<String> result) {
		// Does a recursive call on listHeapHelper() and adds the correct string
		// to the result arraylist in preorder traversal by calling root.left first
		// then root.right
		if (root == null)
			return result;
		if (root.rightChild == null && root.leftChild == null)
			result.add("[" + root.key + " " + root.value + "]");
		else
			result.add("(" + root.key + ")");
		listHeapHelper(root.leftChild, result);
		listHeapHelper(root.rightChild, result);
		if (root.rightChild == null && root.leftChild != null)
			result.add("[" + null + "]");
		return result;
	}
	
	public ArrayList<String> listHeap() {
		// For every level, sort the roots
		ArrayList<String> result = new ArrayList<String>();
		for (int k = 0; k < this.nLevels; k++) {
			int n = this.roots[k].size();
			  
	        // One by one move boundary of unsorted subarray
	        for (int i = 0; i < n-1; i++)
	        {
	            // Find the minimum element in unsorted array
	            int min_idx = i;
	            for (int j = i+1; j < n; j++)
	                if (this.roots[k].get(j).key.compareTo(this.roots[k].get(min_idx).key) < 0)
	                    min_idx = j;
	  
	            // Swap the found minimum element with the first
	            // element
	            Node temp = this.roots[k].get(min_idx);
	            this.roots[k].set(min_idx, this.roots[k].get(i));
	            this.roots[k].set(i, temp);
	        }
	        // If we have a node at level add the appropriate string to result and
	        // call listHeapHelper() to get the string for the other nodes of the root
	        if (this.nodeCt[k] != 0) {
	        	result.add("{lev: " + k + " nodeCt: " + this.nodeCt[k] + "}");
	        	for (int y = 0; y < this.roots[k].size(); y++) {
		        	ArrayList<String> listTemp = listHeapHelper(this.roots[k].get(y), new ArrayList<String>());
		        	for (int z = 0; z < listTemp.size(); z++)
		        		result.add(listTemp.get(z));
	        	}
	        }
		}
		return result;
	}

	// New functions
	private void cut(Node w) {
		Node v = w.rightChild;
		if (v != null) {
			w.rightChild = null; // cut off v
			makeRoot(v); // ... and make it a root
		}
	}
	private void makeRoot(Node u) { // make u a root node
		u.parent = null; // null out parent link
		this.roots[u.level].add(u); // add it to the list of roots
	}

	public void decreaseKey(Locator r, Key newKey) throws Exception {
		Node u = r.get(); // leaf node to be changed
		if (u.key.compareTo(newKey) < 0)
			throw new Exception("Invalid key for decrease-key");
		Node uChild = null; // u’s child on path
		do {
			u.key = newKey; // update key value
			uChild = u; 
			u = u.parent; // move up a level
		} while (u != null && uChild == u.leftChild); // until end of left path
		if (u != null) {
			cut(u);
		}
	}
	
	public Value extractMin() throws Exception { 
		if (this.nodeCt[0] == 0)
			throw new Exception("Empty heap");
		
		Node u = null; // find the min root (exercise)
		Key smallest = null;
		for (int i = 0; i < this.nLevels; i++) {
			Key smallTemp = null;
			Node uTemp = null;
			if (this.roots[i].size() != 0) {
				smallTemp = this.roots[i].get(0).key;
				uTemp = this.roots[i].get(0);
			}
			for (int j = 0; j < this.roots[i].size(); j++) {
				if (this.roots[i].get(j).key.compareTo(smallTemp) < 0) {
					uTemp = this.roots[i].get(j);
					smallTemp = this.roots[i].get(j).key;
				}
			}
			if (smallTemp != null && smallest != null && smallTemp.compareTo(smallest) < 0) {
					smallest = smallTemp;
					u = uTemp;
			}
			else if (smallTemp != null && smallest == null) {
				smallest = smallTemp;
				u = uTemp;
			}
		}
		
		
		Value result = u.value; // final return result
		deleteLeftPath(u); // delete entire left path
		for (int i = 0; i < this.nLevels; i++) {
			for (int j = 0; j < this.roots[i].size(); j++) {
				if (this.roots[i].get(j).key.compareTo(smallest) == 0)
					this.roots[i].remove(j);
			}
		}
		mergeTrees(); // merge tree pairs
		quake(); // perform the quake operation
		return result;
	}
	
	private void deleteLeftPath(Node u) { // delete left path to leaf
		while (u != null) { // repeat all the way down
			cut(u); // cut off u’s right child
			nodeCt[u.level] -= 1; // one less node on this level
			u = u.leftChild; // go to the left child
		}
	}
	private Node minNodeRemove(int lev) {
		Node u = null;
		Key smallest = null;
		
		Key smallTemp = null;
		int index = 0;
		if (this.roots[lev].size() != 0) 
			smallTemp = this.roots[lev].get(0).key;
		for (int j = 0; j < this.roots[lev].size(); j++) {
			if (this.roots[lev].get(j).key.compareTo(smallTemp) < 0) {
				smallTemp = this.roots[lev].get(j).key;
				index = j;
			}
		}
		u = this.roots[lev].get(index);
		this.roots[lev].remove(index);
		return u;
	}
	private void mergeTrees() { // merge trees bottom-up in pairs
		for (int lev = 0; lev < nLevels-1; lev++) { // process levels bottom-up
			while (roots[lev].size() >= 2) { // at least two trees?
				Node u = minNodeRemove(lev); // remove any two
				Node v = minNodeRemove(lev);
				Node w = link(u, v); // ... and merge them
				makeRoot(w); // ... and make this a root
			}
		}
	}
	private Node link(Node u, Node v) { // link u and v into new tree
		int lev = u.level + 1; // new node’s level
		Node w;
		if (u.key.compareTo(v.key) <= 0) // u’s key is smaller?
			w = new Node(u.key, u.value, lev, u, v); // new root with u’s key
		else
			w = new Node(v.key, v.value, lev, v, u); // new root with v’s key
		nodeCt[lev] += 1; // increment node count
		u.parent = v.parent = w; // w is the new parent
		return w;
	}
	private void quake() { // flatten if needed
		for (int lev = 0; lev < nLevels-1; lev++) { // process levels bottom-up
			if (nodeCt[lev+1] > this.ratio * nodeCt[lev]) { // too many?
				for (int i = lev; i < this.nLevels; i++) {
					for (int j = 0; j < this.roots[i].size(); j++) {
						cutNLevel(lev + 1, this.roots[i].get(j).leftChild, this.roots[i].get(j).rightChild);
					}
				}
				clearAllAboveLevel(lev); // clear all nodes above level lev
			}
		}
	}
	private void clearAllAboveLevel(int lev) {
		for (int i = 0; i < this.roots[lev].size(); i++) {
			this.roots[lev].get(i).parent = null;
		}
		for (int i = lev + 1; i < this.nLevels; i++){
			this.roots[i] = new ArrayList<Node>();
			this.nodeCt[i] = 0;
		}
	}
	
	public int size() { 
		return this.nodeCt[0];
	}
	public void setQuakeRatio(double newRatio) throws Exception {
		if (newRatio > 1 || newRatio < 0.5)
			throw new Exception("Quake ratio is outside valid bounds");
		this.ratio = newRatio;
	}
	private void cutNLevel(int nl, Node left, Node right) {
		if (left != null) {
			if (left.level < nl) {
				boolean okay = true;
				for (int k = 0; k < this.roots[nl-1].size(); k++) {
					if (this.roots[nl-1].get(k).key.compareTo(left.key) == 0)
						okay = false;
				}
				if (okay)
					this.roots[nl-1].add(left);
			} else {
				cutNLevel(nl, left.leftChild, right);
				cutNLevel(nl, left.rightChild, right);
			}
		}
		if (right != null) {
			if (right.level < nl) {
				boolean okay = true;
				for (int k = 0; k < this.roots[nl-1].size(); k++) {
					if (this.roots[nl-1].get(k).key.compareTo(right.key) == 0)
						okay = false;
				}
				if (okay)
					this.roots[nl-1].add(right);
			} else {
				cutNLevel(nl, left, right.leftChild);
				cutNLevel(nl, left, right.rightChild);
			}
		}
	}
	public void setNLevels(int nl) throws Exception { 
		if (nl == 0)
			throw new Exception("Attempt to set an invalid number of levels");
		
		ArrayList<Node>[] rootsTemp = (ArrayList<Node>[]) new ArrayList[nl];
		int nodeCtTemp[] = new int[nl];
		if (nl > this.nLevels) {
			for (int i = 0; i < this.nLevels; i++) {
				nodeCtTemp[i] = this.nodeCt[i];
				rootsTemp[i] = this.roots[i];
			}
			for (int i = this.nLevels; i < nl; i++) {
				rootsTemp[i] = new ArrayList<Node>();
			}
		} else if (nl < this.nLevels) {
			for (int i = nl; i < this.nLevels; i++) {
				for (int j = 0; j < this.roots[i].size(); j++) {
					cutNLevel(nl, this.roots[i].get(j).leftChild, this.roots[i].get(j).rightChild);
				}
			}
			for (int i = 0; i < nl; i++) {
				nodeCtTemp[i] = this.nodeCt[i];
				rootsTemp[i] = this.roots[i];
			}
			clearAllAboveLevel(nl);
		}
		this.roots = rootsTemp;
		this.nodeCt = nodeCtTemp;
		this.nLevels = nl;
	}
}
