package fi.aalto.drumbeat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class DrumbeatSimple extends Application {
	private Application application;

	@Override
	public void start(Stage stage) throws Exception {
		final Graph graph = new MultiGraph(UUID.randomUUID().toString());
		graph.addAttribute("ui.antialias");
		graph.setAttribute("stylesheet",
				"node { " + "     shape: rounded-box; " + "     padding: 5px; " + "     fill-color: white; "
						+ "     stroke-mode: plain; " + "     size-mode: fit; " + "} " + "edge { arrow-shape: arrow; arrow-size: 10px, 10px;"
						+ "     shape: freeplane; " + "}");

		List<String> new_uris=appendGraph("http://architectural.drb.cs.hut.fi/drumbeat/collections", graph);
		if(new_uris==null)
			return;
		for(String uri:new_uris)
		{
			List<String> new_uris2=appendGraph(uri, graph);
			/*for(String uri2:new_uris2)
			{
				appendGraph(uri2, graph);
			}*/
		}
			
		final Viewer viewer = graph.display();
		View view = viewer.getDefaultView();

		ViewerPipe fromViewer = viewer.newViewerPipe();
		final GraphListener gl = new GraphListener(graph, view, viewer);
		fromViewer.addViewerListener(gl);
		fromViewer.addSink(graph);

		final Runnable worker_pump = () -> {
			while (true)
				fromViewer.pump(); // or fromViewer.blockingPump(); in the
									// nightly builds
		};
		final Thread thread_pump = new Thread(worker_pump);
		thread_pump.setDaemon(true);
		thread_pump.start();

		viewer.enableAutoLayout();
	}

	Map<Resource, Node> node_map = new HashMap<Resource, Node>();
	Set<Statement> statements = new HashSet<Statement>();
	public List<String> appendGraph(String url, Graph graph) {
		List<String> list_of_new_uris=new ArrayList<String>();
		if(url.contains(".ttl"))
			return list_of_new_uris;
		OntModel model = getModel(url);
		if(model==null)
			return null;
		StmtIterator iter1 = model.listStatements();

		while (iter1.hasNext()) {			
			Statement stmt = iter1.nextStatement(); // get next statement
			//Handled already
			if(!statements.add(stmt))
				continue;
			Resource rs = stmt.getSubject();
			Node ns = node_map.get(rs);
			if (ns == null) {
				ns = graph.addNode(rs.getURI());
				//ns.addAttribute("ui.label", rs.getLocalName());
				ns.addAttribute("ui.label", rs.getURI());
				node_map.put(rs, ns);
				list_of_new_uris.add(rs.getURI());
			}

			RDFNode ro = stmt.getObject();
			/*if (ro.isLiteral()) {
				Node ln = graph.addNode(System.currentTimeMillis() + "");
				ln.addAttribute("ui.label", ro.asLiteral().getLexicalForm());
				Edge snln = graph.addEdge(stmt.toString(), ns, ln,true);
				snln.addAttribute("ui.label", stmt.getPredicate().getLocalName());
			} else */{
				if (ro.isResource()) {
					Node no = node_map.get(ro);
					if (no == null) {
						no = graph.addNode(ro.asResource().getURI());
						//no.addAttribute("ui.label", ro.asResource().getLocalName());
						no.addAttribute("ui.label", ro.asResource().getURI());
						node_map.put(ro.asResource(), no);
						list_of_new_uris.add(ro.asResource().getURI());
					}
					Edge snln = graph.addEdge(stmt.toString(), ns, no,true);
					if(stmt.getPredicate().getLocalName().equals("type"))
					{
						no.setAttribute("ui.style","shape:circle; ");
					}
					snln.addAttribute("ui.label", stmt.getPredicate().getLocalName());
				}
			}
		}
		return list_of_new_uris;
	}

	OntModel model;

	private OntModel getModel(String url) {
		String json = getResponse(url);
		
		System.out.println("URL:"+url);
		System.out.println("json.....>");
		System.out.println(json);
		if(json==null)
		{
			System.err.println(" No response from th server!");
			return null;
		}
		InputStream is = new ByteArrayInputStream(json.getBytes());

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);

		model.read(is, null, "JSON-LD");

		return model;
	}

	private String getResponse(String url) {

		int timeout = Integer.MAX_VALUE;

		HttpURLConnection c = null;
		try {
			URL u = new URL(url);
			c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-length", "0");
			c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			c.setRequestProperty("Accept", "application/ld+json");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.setConnectTimeout(timeout);
			c.setReadTimeout(timeout);

			c.connect();
			int status = c.getResponseCode();

			switch (status) {
			case 200:
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				return sb.toString();
			}

		} catch (MalformedURLException ex) {

		} catch (IOException ex) {
		} finally {
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex) {
				}
			}
		}
		return null;
	}

	public void stop() throws Exception {
		this.application.stop();
		this.application = null;
		Platform.exit();
	}

	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		launch(args);
	}

}
