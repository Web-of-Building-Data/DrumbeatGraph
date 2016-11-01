package fi.aalto.drumbeat;

import java.io.File;
import java.net.URL;
import java.util.Stack;

import javax.servlet.http.Cookie;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.graph.DrumbeatClickedNode;
import com.vaadin.graph.GraphExplorer;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fi.aalto.drumbeat.graph.DrumbeatGraphRepository;
import fi.aalto.drumbeat.graph.GraphEdge;
import fi.aalto.drumbeat.graph.GraphNode;

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


@Theme("drumbeat")
public class DrumbeatVisualizer extends UI implements ClickListener, DrumbeatClickedNode, ValueChangeListener {
	private static final long serialVersionUID = 1L;
	private DrumbeatGraphRepository graph_repo;
	private GraphExplorer<?, ?> graph_explorer;
	VerticalLayout selection_area;
	private CssLayout layout;

	private TextField rest_url;
	private Button back_button;
	private Button home_button;
	private Button fetch_button;
	private Button save_button;

	//private String default_url =  "http://localhost/drumbeat/collections";
    private String default_url = "http://architectural.drb.cs.hut.fi/drumbeat/collections";

	//private String url = "http://localhost/drumbeat/collections";
	private String url = "http://architectural.drb.cs.hut.fi/drumbeat/collections";

	final private Stack<String> url_stack = new Stack<String>();

	@Override
	public void init(VaadinRequest request) {
		String req_url=request.getParameter("url");
		if(req_url!=null)
		{
			try {
				// validity check
				URL u=new URL(req_url);		  
				if(u.getPath()!=null)
				{
					default_url=req_url;
					url=req_url;					
				}
			} catch (Exception e) {
				
			}
		}
		//Fetch all cookies
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		for(Cookie c:cookies)
		{
			if(c.getName().equals("DRUMBEAT_URL"))
			{
				url=c.getValue();
				default_url=url;
			}
		}
		
		String basepath = VaadinService.getCurrent().getBaseDirectory()
				.getAbsolutePath();
		
		// Image as a file resource
		FileResource resource = new FileResource(new File(basepath
				+ "/WEB-INF/images/drumbeat_banner_small2.jpg"));

		// Show the image in the application
		Image drumbeat_logo = new Image(
				"Aalto University", resource);
		
		graph_repo = new DrumbeatGraphRepository(url);
		VerticalLayout content = new VerticalLayout();
		content.addComponent(drumbeat_logo);
		content.setStyleName("drumbeat");
		content.setMargin(true);
		content.setSpacing(true);
		
		selection_area = new VerticalLayout();
		rest_url = new TextField("DRUMBEAT URL");
		rest_url.addValueChangeListener(this);
		rest_url.setWidth("600px");
		
		selection_area.addComponent(rest_url);
		HorizontalLayout button_area = new HorizontalLayout();
		back_button = new Button("<<==");
		back_button.addClickListener(this);

		String uri_presentation = "";
		try {
			URL url = new URL(default_url);
			uri_presentation=url.getFile().toString();
		} catch (Exception e) {
			uri_presentation  = default_url;
		}
		
		home_button = new Button("HOME: <"+uri_presentation+">");
		home_button.addClickListener(this);

		fetch_button = new Button("GO");
		fetch_button.addClickListener(this);

		save_button = new Button("SAVE URL");
		save_button.setDescription("Saves the url into a cookie. If set, that is used as default.");
		save_button.addClickListener(this);

		
		button_area.addComponent(back_button);
		button_area.addComponent(home_button);
		button_area.addComponent(fetch_button);
		button_area.addComponent(save_button);
		selection_area.addComponent(button_area);

		content.addComponent(selection_area);

		layout = new CssLayout();

		layout.setSizeFull();

		content.addComponent(layout);
		content.setExpandRatio(layout, 1);
		content.setSizeFull();
		if(graph_repo==null)
			return;
		try
		{
			graph_explorer = new GraphExplorer<GraphNode, GraphEdge>(this, graph_repo);		
			graph_explorer.setSizeFull();
			layout.addComponent(graph_explorer);			
		}catch (Exception e) {
			e.printStackTrace();
		}
		setContent(content);
		Page.getCurrent().setTitle("Drumbeat Visualizer");
		rest_url.setValue(url);
	}

	private void recreateGraphArea() {
		graph_repo.clear();
		graph_repo = new DrumbeatGraphRepository(url);
		layout.removeAllComponents();
		if (graph_repo == null)
			return;
		try {
			graph_explorer = new GraphExplorer<GraphNode, GraphEdge>(this, graph_repo);
			graph_explorer.setSizeFull();
			layout.addComponent(graph_explorer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void recreateGraphArea(Stack<String> url_stack) {
		graph_repo.clear();
		graph_repo = new DrumbeatGraphRepository(url,url_stack);
		layout.removeAllComponents();
		if (graph_repo == null)
			return;
		try {
			graph_explorer = new GraphExplorer<GraphNode, GraphEdge>(this, graph_repo);
			graph_explorer.setSizeFull();
			layout.addComponent(graph_explorer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void clicked(String nodeId) {
		String url = graph_repo.getData_model().getURL(nodeId);
		if (url != null) {
			// Ainoastaan, jos yhden stepin päästä??
			if (!url_stack.contains(url))
				url_stack.push(url);
			if(url.contains("/objects/"))
			{
				// Too huge to be handled otherwise
				rest_url.setValue(url);
				recreateGraphArea(url_stack);
				return;
			}
			rest_url.setValue(url);
			this.url = url;
		}

		// No reaction!!
		//graph_repo.setHomeNodeId(nodeId);

	}

	@Override
	// TODO tää ei nyt toimi, vaatii datamodellin uudelleen boottaamisen jne.
	public void buttonClick(ClickEvent event) {
		if (event.getButton().equals(back_button)) {
			if (!url_stack.isEmpty()) {
				String top_url = (String) url_stack.pop();
				if (!top_url.equals(url)) {
					url = top_url;
				} else if (!url_stack.isEmpty())
					url = (String) url_stack.pop();
				else
					url = default_url;
			}
			else
				url = default_url;

			rest_url.setValue(url);
			recreateGraphArea(url_stack);
		} 
		
		if (event.getButton().equals(home_button)) {
			url_stack.clear();
			if (!url_stack.contains(url))
				url_stack.push(url);
			url = default_url;
			rest_url.setValue(url);
			recreateGraphArea();
		} 
		
		if(event.getButton().equals(fetch_button))
		{
			// Get data
			url = rest_url.getValue();
			if (!url_stack.contains(url))
				url_stack.push(url);
			recreateGraphArea();
		}
		if(event.getButton().equals(save_button))
		{
			try {
				default_url=rest_url.getValue();
				String uri_presentation = "";
				try {
					URL url = new URL(default_url);
					uri_presentation=url.getFile().toString();
				} catch (Exception e) {
					uri_presentation  = default_url;
				}
				
				home_button.setCaption("HOME: <"+uri_presentation+">");
				Cookie myCookie = new Cookie("DRUMBEAT_URL", rest_url.getValue());
				myCookie.setMaxAge(120);
				myCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
				VaadinService.getCurrentResponse().addCookie(myCookie);				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

	}

	@Override
	// TODO tää ei nyt toimi, vaatii datamodellin uudelleen boottaamisen jne.
	public void valueChange(ValueChangeEvent event) {
		url = rest_url.getValue();
		//if (!url_stack.contains(url))
		//	url_stack.push(url);
		rest_url.setValue(url);
		// TODO Tämä laittaa graafin uudelleenpiirtoon joka klikkauksella!
		//recreateGraphArea();
	}

}
