package fi.aalto.drumbeat.graph;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.graph.GraphElement;

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


abstract class AbstractGraphElementImpl implements GraphElement {

  protected final String id;
  protected  String label;
  private Map<String, Object> properties;

  public AbstractGraphElementImpl(String id) {
    this(id, id);
  }

  public AbstractGraphElementImpl(String id, String label) {
    super();
    this.id = id;
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
	this.label = label;
}

public Map<String, Object> getProperties() {
    if (properties == null) {
      properties = new HashMap<String, Object>();
    }
    return properties;
  }
  
  public void setStyle(String style) {
	getProperties().put(GraphElement.PROPERTY_NAME_STYLE, style);
  }

  public void removeStyles() {
	getProperties().clear();
  }

  public void setROOTStyle() {
	getProperties().put(GraphElement.PROPERTY_NAME_STYLE, "root");
  }

}

