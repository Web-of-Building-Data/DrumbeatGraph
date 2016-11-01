package fi.aalto.drumbeat;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;

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