package fi.aalto.drumbeat;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;

public class GraphListener implements ViewerListener {
	final Graph graph;
	final View view;
	final Viewer viewer;

	public GraphListener(Graph graph,View view,Viewer viewer) {
		this.graph=graph;
		this.view=view;
		this.viewer=viewer;
	}

	public void viewClosed(String id) {	
	}

	int i=0;
	public void buttonPushed(String id) {
		
		
		//view.getCamera().resetView();
		//view.getCamera().setAutoFitView(true);
		
		/*for(Node n:graph.getNodeSet())
			 //if(!n.getId().equals(id))
				graph.removeNode(n);*/
			
			
			System.out.println("Button pushed on node "+id);
			String name=id+"."+i++;
			Node u = graph.addNode(name);
			u.addAttribute("ui.label", name);
			graph.addEdge("E"+i, id, name);

				
	}

	public void buttonReleased(String id) {
		System.out.println("Button released on node "+id);
	}
}