package cmsc420_s22;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class EMSTree<LPoint extends LabeledPoint2D> {
	
	private ArrayList<LPoint> pointList;
	private HashSet<LPoint> inEMST;
	private ArrayList<Pair<LPoint>> edgeList;
	private HBkdTree<LPoint> kdTree;
	private QuakeHeap<Double, Pair<LPoint>> heap;
	private HashMap<LPoint, ArrayList<LPoint>> dependents;
	
	public EMSTree(Rectangle2D bbox) {
		edgeList = new ArrayList<Pair<LPoint>>();
		inEMST = new HashSet<LPoint>();
		kdTree = new HBkdTree<LPoint>(bbox);
		heap = new QuakeHeap<Double, Pair<LPoint>>(10);
		pointList = new ArrayList<LPoint>();
		dependents = new HashMap<LPoint, ArrayList<LPoint>>();
	}
	public void addPoint(LPoint pt) {
		pointList.add(pt);
		dependents.put(pt, new ArrayList<LPoint>());
	}
	public void clear() { 
		edgeList.clear();
		inEMST.clear();
		kdTree.clear();
		heap.clear();
		pointList.clear();
		dependents.clear();
	}
	public int size() {
		return pointList.size(); 	
	}
	private String addEdge(Pair<LPoint> edge) throws Exception {
		ArrayList<String> resultArray = new ArrayList<String>();
		LPoint pt2 = edge.getSecond(); // endpoint to add to EMST
		edgeList.add(edge); // add edge to the EMST
		String result = "add: " + edge + " new-nn:";
		inEMST.add(pt2); // add pt2 to the EMST
		kdTree.delete(pt2.getPoint2D()); // remove pt2 from the kd-tree
		ArrayList<LPoint> dep2 = dependents.get(pt2); // get pt2’s dependent points
		dep2.add(pt2); // include pt2 as well
		for (int i = 0; i < dep2.size(); i++) { // compute new nearest neighbors
			LPoint pt3 = dep2.get(i);
			LPoint nn3 = kdTree.root != null && kdTree.rootPoint() != null ? 
					kdTree.nearestNeighbor(pt3, kdTree.root, kdTree.bbox, kdTree.rootPoint()) : null; // pt3’s nearest neighbor
			if (nn3 == null) break; // out of points? -- we’re done
			resultArray.add("(" + pt3.getLabel() + "->" + nn3.getLabel() + ")");
			Collections.sort(resultArray);
			addNearNeighbor(pt3, nn3); // add this near-neighbor pair
		}
		for (String r : resultArray) 
			result += " " + r;
		return result;
	}
	private void addNearNeighbor(LPoint pt, LPoint nn) {
		double dist = pt.getPoint2D().distanceSq(nn.getPoint2D()); // squared distance to nearest neighbor
		Pair<LPoint> pair = new Pair<LPoint>(pt, nn); // new nearest-neighbor pair
		heap.insert(dist, pair); // add to priority queue
		dependents.get(nn).add(pt); // add to nn’s dependents list
	}
	private void initializeEMST(LPoint start) throws Exception {
		edgeList.clear(); // clear the edge list
		inEMST.clear(); // clear the EMST set
		heap.clear(); // clear the heap
		for (ArrayList<LPoint> dep : dependents.values()) { // clear all the dependents
			dep.clear();
		}
		kdTree.clear(); // clear the kd-tree
		for (int i = 0; i < pointList.size(); i++) {// add all but start
			LPoint pt = pointList.get(i);
			if (pt != start) {
				try{ 
					kdTree.insert(pt);
				}catch (Exception error){
					throw error;
				}
			}
		}
		inEMST.add(start);
	}
	public ArrayList<String> buildEMST(LPoint start) throws Exception { 
		ArrayList<String> result = new ArrayList<String>();
		initializeEMST(start); // initialize
		LPoint nn = kdTree.root != null && kdTree.rootPoint() != null ? 
				kdTree.nearestNeighbor(start, kdTree.root, kdTree.bbox, kdTree.rootPoint()) : null; // get start’s nearest
		if (nn == null) return result; // no more points -- done
		addNearNeighbor(start, nn); // add nearest neighbor pair
		result.add("new-nn: (" + start.getLabel() + "->" + nn.getLabel() + ")");
		while (kdTree.size() != 0) {
			Pair<LPoint> edge = heap.extractMin(); // extract next edge
			LPoint pt2 = edge.getSecond(); // get destination end point
			if (!inEMST.contains(pt2)) { // not redundant?
				result.add(addEdge(edge)); // add the edge to the EMST
			}
		}
		return result;
	}
	public ArrayList<String> listEMST() {
		ArrayList<String> result = new ArrayList<String>();
		if (inEMST.size() != 0) {
			for (int i = 0; i < edgeList.size(); i++) {
				result.add("(" + edgeList.get(i).getFirst().getLabel() + "," + edgeList.get(i).getSecond().getLabel() + ")");
			}
		}
		return result; 
	}
}
