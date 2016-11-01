package fi.aalto.drumbeat.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.vaadin.graph.Arc.Direction;
import com.vaadin.graph.GraphRepository;

/*
* 
Jyrki Oraskari, Aalto University, 2016 

This research has partly been carried out at Aalto University in DRUMBEAT 
“Web-Enabled Construction Lifecycle” (2014-2017) —funded by Tekes, 
Aalto University, and the participating companies.

The MIT License (MIT)
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/


public class DrumbeatGraphRepository implements GraphRepository<GraphNode, GraphEdge>, Serializable {

	private static final long serialVersionUID = 1L;
	private String homeNodeId;

	final private Map<String, GraphNode> nodeMap = new HashMap<String, GraphNode>();
	final private Map<String, GraphEdge> edgeMap = new HashMap<String, GraphEdge>();
	final private Map<String, String> headMap = new HashMap<String, String>();
	final private Map<String, String> tailMap = new HashMap<String, String>();
	final private Map<String, Set<String>> incomingMap = new HashMap<String, Set<String>>();
	final private Map<String, Set<String>> outgoingMap = new HashMap<String, Set<String>>();

	final private DrumbeatDataModel data_model;
	
	public DrumbeatGraphRepository(String url) {
		 data_model=new DrumbeatDataModel(url, this);
		 
	}

	public DrumbeatGraphRepository(String url, Stack<String> url_stack) {
		data_model=new DrumbeatDataModel(url, this,url_stack);
	}

	@Override
	public GraphNode getTail(GraphEdge arc) {
		return nodeMap.get(tailMap.get(arc.getId()));
	}

	@Override
	public GraphNode getHead(GraphEdge arc) {
		return nodeMap.get(headMap.get(arc.getId()));
	}

	@Override
	public Iterable<String> getArcLabels() {
		List<String> ret = new ArrayList<String>(edgeMap.size());
		for (GraphEdge e : edgeMap.values()) {
			ret.add(e.getLabel());
		}
		return ret;
	}

	@Override
	public Collection<GraphEdge> getArcs(GraphNode node, String label, Direction dir) {
		Set<String> idset;
		if (Direction.INCOMING == dir) {
			idset = incomingMap.get(node.getId());
		} else {
			idset = outgoingMap.get(node.getId());
		}
		List<GraphEdge> result = new ArrayList<GraphEdge>();
		if (idset != null) {
			for (String eid : idset) {
				GraphEdge arc = edgeMap.get(eid);
				if (arc.getLabel().equals(label)) {
					result.add(arc);
				}
			}
		}
		return result;
	}

	@Override
	public GraphNode getHomeNode() {
		return nodeMap.get(homeNodeId);
	}

	@Override
	public GraphNode getOpposite(GraphNode node, GraphEdge arc) {
		String hnid = headMap.get(arc.getId());
		String tnid = tailMap.get(arc.getId());

		if (hnid != null && tnid != null) {
			if (hnid.equals(node.getId())) {
				// given node is head so return tail as an opposite
				return nodeMap.get(tnid);
			} else if (tnid.equals(node.getId())) {
				// given node is tail so return head as an opposite
				return nodeMap.get(hnid);
			} else {
				// what is this edge ?
				return null;
			}
		} else {
			// not a node of the graph
			return null;
		}
	}

	@Override
	public GraphNode getNodeById(String id) {
		GraphNode gn=nodeMap.get(id);
		System.out.println("get Graph Node:"+id);
		if(gn!=null)
		{
		  if(!gn.isFetched())
		  {
			  
			  System.out.println("READ Graph Node:"+id);
			  data_model.appendGraph(id, this);		  
			  gn.setFetched(true);
		  }
		}
		return gn;
	}

	// Internals
	//--------------------------------------------------

	public GraphNode createNode(String id, String label) {
		GraphNode n = new GraphNode(id, label);
		nodeMap.put(id, n);
		return n;
	}

	public GraphEdge createEdge(String nid1, String nid2, String eid, String label) {
		GraphEdge e = new GraphEdge(eid, label);
		edgeMap.put(eid, e);
		headMap.put(eid, nid1);
		tailMap.put(eid, nid2);

		addToOutgoing(nid1, eid);
		addToIncomming(nid2, eid);
		return e;
	}

	public void setHomeNodeId(String homeNodeId) {
		if(this.homeNodeId!=null)
		{
			GraphNode gn=nodeMap.get(homeNodeId);
			gn.removeStyles();
		}
		this.homeNodeId = homeNodeId;
		{
		  GraphNode gn=nodeMap.get(homeNodeId);
		  gn.setROOTStyle();
		}

	}


	private void addToOutgoing(String nid, String eid) {
		Set<String> s = outgoingMap.get(nid);
		if (s == null) {
			s = new HashSet<String>();
			outgoingMap.put(nid, s);
		}
		s.add(eid);
	}

	private void addToIncomming(String nid, String eid) {
		Set<String> s = incomingMap.get(nid);
		if (s == null) {
			s = new HashSet<String>();
			incomingMap.put(nid, s);
		}
		s.add(eid);
	}

	public void clear() {
		homeNodeId = null;
		nodeMap.clear();
		edgeMap.clear();
		headMap.clear();
		tailMap.clear();
		incomingMap.clear();
		outgoingMap.clear();
	}

	public DrumbeatDataModel getData_model() {
		return data_model;
	}

	
}