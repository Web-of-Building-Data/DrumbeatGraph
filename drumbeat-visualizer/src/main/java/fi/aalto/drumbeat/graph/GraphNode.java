package fi.aalto.drumbeat.graph;

import com.vaadin.graph.Node;
import com.vaadin.server.Resource;

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


public class GraphNode extends AbstractGraphElementImpl implements Node {

	private Resource icon;
	private boolean fetched = false;
	private String type = null;
	private boolean has_long_name = false;
	
	
	public GraphNode(String id) {
		this(id, id);
	}

	public GraphNode(String id, String label) {
		super(id, label);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	 public void setLongLabel(String label) {
			this.label = label;
			has_long_name=true;
	}

	
	public boolean isHas_long_name() {
		return has_long_name;
	}

	
	@Override
	public String getLabel() {
		if(type!=null)
		{
			System.out.println("Type was: "+type);
			return type+" "+label;
		}
		return label;
	}

	public String getPureLabel() {
		return label+":";
	}
	
	public Resource getIcon() {
		return icon;
	}

	public void setIcon(Resource icon) {
		this.icon = icon;
	}

	public boolean isFetched() {
		return fetched;
	}

	public void setFetched(boolean fetched) {
		this.fetched = fetched;
	}

}
