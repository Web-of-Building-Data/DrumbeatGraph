package fi.aalto.drumbeat.graph;

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
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.vaadin.server.ThemeResource;

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


public class DrumbeatDataModel {
	// URL -> Node
	Map<String, GraphNode> url_node_map = new HashMap<String, GraphNode>();
	// TODO test if statement IDs are the same
	private final Set<Statement> statements = new HashSet<Statement>();

	private long literal = 0;
	private long statement = 0;

	// nodeID -> URL Nodes have URL as ID, literals not
	final private Map<String, String> nodeid_url = new HashMap<String, String>();

	public DrumbeatDataModel(String url_str, DrumbeatGraphRepository graph) {
		try {
			URL url = new URL(url_str);
			home_name = url.getFile();
		} catch (Exception e) {
			home_name = url_str;
		}

		List<String> new_uris = handleGraph(url_str, graph);
		if (new_uris == null)
			return;
		if (new_uris.size() < 30) // Restrict fetches that are too huge
			for (String uri : new_uris) {
				@SuppressWarnings("unused")
				List<String> new_uris2 = handleGraph(uri, graph);
			}
	}

	Stack<String> url_stack;
	String home_name = "Home";

	public DrumbeatDataModel(String url_str, DrumbeatGraphRepository graph, Stack<String> url_stack) {
		try {
			URL url = new URL(url_str);
			home_name = url.getFile();
		} catch (Exception e) {
			home_name = url_str;
		}
		this.url_stack = url_stack;
		List<String> new_uris = handleGraph(url_str, graph);
		if (new_uris == null)
			return;
		if (new_uris.size() < 30) // Restrict fetches that are too huge
			for (String uri : new_uris) {
				@SuppressWarnings("unused")
				List<String> new_uris2 = handleGraph(uri, graph);
			}
	}

	// repository update
	public void appendGraph(String id, DrumbeatGraphRepository graph) {
		String url = nodeid_url.get(id);
		if (url != null) {
			List<String> new_uris = handleGraph(url, graph);
			if (new_uris.size() < 30) // Restrict fetches that are too huge
				for (String uri : new_uris) {
					@SuppressWarnings("unused")
					List<String> new_uris2 = handleGraph(uri, graph);
				}
		}
	}

	public String getURL(String nodeID) {
		return nodeid_url.get(nodeID);
	}

	static Map<String, String> subject_classes = new HashMap<String, String>();

	// URL->Model
	static final Map<String, Model> cache_for_huge_models = new HashMap<String, Model>();
	static long cache_born_time = 0;

	private List<String> handleGraph(String url, DrumbeatGraphRepository graph) {

		List<String> list_of_new_uris = new ArrayList<String>();
		if (url.contains(".ttl"))
			return list_of_new_uris;
		Model model = null;
		if(cache_born_time-System.currentTimeMillis() > 60000 * 60)
		{
			cache_for_huge_models.clear();
			cache_born_time=System.currentTimeMillis();
		}
		Model cm = cache_for_huge_models.get(url);
		if (cm != null) {
			System.out.println("USE CACHED MODEL!!");
			model = cm;
		} else {
			model = getModel(url);
			if (model == null)
				return null;			
			if (model.size() > 500) {
				// Only one				 
				System.out.println("SAVE MODEL into CACHE!!");
				System.out.println("MODEL size was:"+model.size());
				cache_for_huge_models.clear();
				cache_born_time=System.currentTimeMillis();
				cache_for_huge_models.put(url, model);
				
			}
			else 
			{
				if(cache_for_huge_models.size()<200)
				  cache_for_huge_models.put(url, model);
				System.out.println("MODEL size was:"+model.size());
			}
		}

		StmtIterator statement_iterator = model.listStatements();

		Map<Resource, Map<String, String>> subjects_having_literals = new HashMap<Resource, Map<String, String>>();
		Set<GraphNode> subjects = new HashSet<GraphNode>();

		// All statements ====>>>
		while (statement_iterator.hasNext()) {
			Statement stmt = statement_iterator.nextStatement(); // get next
																	// statement
			// System.out.println("STM: " + stmt.asTriple().toString());
			// Handled already
			if (!statements.add(stmt))
				continue;
			Resource rs = stmt.getSubject();
			GraphNode ns = url_node_map.get(rs.getURI());
			if (ns == null) {
				// ns = graph.addNode("node" + (node++), rs.getLocalName());
				ns = graph.createNode(rs.getURI(), rs.getLocalName());
				subjects.add(ns);
				url_node_map.put(rs.getURI(), ns);
				if (url_stack != null) {
					if (url_stack.contains(rs.getURI()))
						ns.setStyle("root");
				}
				list_of_new_uris.add(rs.getURI());
				nodeid_url.put(ns.getId(), rs.getURI());
				//System.out.println("PUT NS: " + ns.getId());
				Map<String, String> subject_literals = subjects_having_literals.get(rs);
				if (subject_literals == null) {
					subject_literals = new HashMap<String, String>();
					subjects_having_literals.put(rs, subject_literals);
				}
				subject_literals.put("DRUMBEAT", "<a href=\"" + ns.getId() + "\" target=\"_blank\">link</a>");

			}

			RDFNode ro = stmt.getObject();

			if (ro.isResource()) {

				if (!stmt.getPredicate().getLocalName().equals("type")) {
					GraphNode no = url_node_map.get(ro.asResource().getURI());
					if (no == null) {

						// no = graph.addNode("node" + (node++),
						// ro.asResource().getLocalName());
						no = graph.createNode(ro.asResource().getURI(), ro.asResource().getLocalName());
						url_node_map.put(ro.asResource().getURI(), no);

						if (url_stack != null) {
							if (url_stack.contains(ro.asResource().getURI()))
								no.setStyle("root");
						}

						list_of_new_uris.add(ro.asResource().getURI());
						nodeid_url.put(no.getId(), ro.asResource().getURI());
						//System.out.println("PUT NO: " + no.getId());
						Map<String, String> subject_literals = subjects_having_literals.get(ro.asResource());
						if (subject_literals == null) {
							subject_literals = new HashMap<String, String>();
							subjects_having_literals.put(ro.asResource(), subject_literals);
						}

						subject_literals.put("DRUMBEAT", "<a href=\"" + no.getId() + "\" target=\"_blank\">link</a>");

					}
					graph.createEdge(no.getId(), ns.getId(), "statement" + (statement++),
							stmt.getPredicate().getLocalName());
				} else {
					// --TYPE--

					GraphNode no = url_node_map.get(ro.asResource().getURI());
					if (no == null) {

						// no = graph.addNode("node" + (node++),
						// ro.asResource().getLocalName());
						no = graph.createNode(ro.asResource().getURI(), ro.asResource().getLocalName());
						no.setStyle("type");
						url_node_map.put(ro.asResource().getURI(), no);

						list_of_new_uris.add(ro.asResource().getURI());
						nodeid_url.put(no.getId(), ro.asResource().getURI());
						//System.out.println("PUT NO: " + no.getId());
						Map<String, String> subject_literals = subjects_having_literals.get(ro.asResource());
						if (subject_literals == null) {
							subject_literals = new HashMap<String, String>();
							subjects_having_literals.put(ro.asResource(), subject_literals);
						}

						subject_literals.put("DRUMBEAT", "<a href=\"" + no.getId() + "\" target=\"_blank\">link</a>");

					}
					graph.createEdge(no.getId(), ns.getId(), "statement" + (statement++),
							stmt.getPredicate().getLocalName());

					// Org type
					{

						// Jotta kirjautuu
						// Subjectin tyyppi
						subject_classes.put(ns.getId(), ro.asResource().getLocalName());
                        ns.setType(ro.asResource().getLocalName());
						// Handled as literal to make it shown separately when
						// needed
						Map<String, String> subject_literals = subjects_having_literals.get(rs);
						if (subject_literals == null) {
							subject_literals = new HashMap<String, String>();
							subjects_having_literals.put(rs, subject_literals);
						}
						// TODO In theory, there could be many literals of the
						// same predicate
						subject_literals.put(stmt.getPredicate().getLocalName(), ro.asResource().getLocalName());
					}
				}
			} else {
				Map<String, String> subject_literals = subjects_having_literals.get(rs);
				if (subject_literals == null) {
					subject_literals = new HashMap<String, String>();
					subjects_having_literals.put(rs, subject_literals);
				}
				// TODO In theory, there could be many literals of the same
				// predicate
				subject_literals.put(stmt.getPredicate().getLocalName(), ro.asLiteral().getString());

			}

		} // <<<==== All statements

		

		for (Map.Entry<Resource, Map<String, String>> rs_entry : subjects_having_literals.entrySet()) {
			Resource rs = rs_entry.getKey();
			GraphNode ns = url_node_map.get(rs.getURI());
			if (ns != null) {
				StringBuffer sb = new StringBuffer();
				sb.append(ns.getPureLabel()); // Short since appended only for graph!
				sb.append("<TABLE>");
				String subject_class = subject_classes.get(ns.getId());

				if (rs_entry.getValue().entrySet().size() > 3) {
					for (Map.Entry<String, String> literal_entry : rs_entry.getValue().entrySet()) {
						if (literal_entry.getKey().equals("type"))
							continue;
						if (literal_entry.getKey().endsWith("Uri")) {
							if (literal_entry.getKey().endsWith("graphBaseUri")) {
								boolean is_url = true;
								try {
									URL url_test = new URL(literal_entry.getValue());
									url_test.getFile();

								} catch (Exception e) {
									is_url = false;
								}
								String literal_id = literal_entry.getValue();
								GraphNode no = graph.createNode(literal_id,
										"-DataSource Objects-<BR>(may take some time)<HR>DRUMBEAT <a href=\"" + literal_entry.getValue()
												+ "\" target=\"_blank\">link</a>");
								no.setStyle("white");
								graph.createEdge(literal_id, ns.getId(), "statement" + (statement++),
										literal_entry.getKey());
								nodeid_url.put(literal_id, literal_id);

							} else {
								String literal_id = literal_entry.getKey() + " : " + literal_entry.getValue();
								GraphNode no = graph.createNode(literal_id,
										"<a href=\"" + literal_entry.getValue() + "\" target=\"_blank\">link</a>");
								no.setStyle("white");
								graph.createEdge(literal_id, ns.getId(), "statement" + (statement++),
										literal_entry.getKey());
							}
						} else {
							String literal_id = "literal" + (literal++);
							GraphNode no = graph.createNode(literal_id, literal_entry.getValue());
							no.setStyle("white");
							graph.createEdge(literal_id, ns.getId(), "statement" + (statement++),
									literal_entry.getKey());
						}
					}
					sb.append("</TABLE>");
					if(!ns.isHas_long_name())
					  ns.setLongLabel(sb.toString());
					continue;
				}
				
				// Else
				// Set already!
				if(ns.isHas_long_name())
					  continue;


				for (Map.Entry<String, String> literal_entry : rs_entry.getValue().entrySet()) {
					sb.append("<TR>");
					sb.append("<TD>" + literal_entry.getKey() + "</TD>");
					sb.append("<TD>" + literal_entry.getValue() + "</TD>");
					sb.append("</TR>");
				}
				sb.append("</TABLE>");
				ns.setLongLabel(sb.toString());

				// TODO väärässä paikassa
				if (subject_class != null) {
					if (subject_class.equals("Collection"))
						ns.setIcon(new ThemeResource("icons/48x48/1474989318_HP-Pictures-Folder-Dock-48JO.gif"));

					/*if (subject_class.equals("DataSource")) {
						String uri = ns.getId();
						String new_uri = uri.replaceFirst("/datasources/", "/objects/");
						GraphNode no = graph.createNode(new_uri, "-DataSource Objects-");
						nodeid_url.put(new_uri, new_uri);
						// GraphNode no = graph.createNode(new_uri, new_uri);
						no.setStyle("white");
						graph.createEdge(new_uri, ns.getId(), "statement" + (statement++), "-virtual link objects-");
					}*/

				}
			}

		}

		if (subjects.size() > 1) {
			GraphNode home = graph.createNode("node1", home_name);
			home.setStyle("root");
			home.setIcon(new ThemeResource("icons/48x48/1474988619_Home.png"));
			graph.setHomeNodeId("node1");

			for (GraphNode ns : subjects) {
				graph.createEdge(ns.getId(), home.getId(), "statement" + (statement++), "has");
			}

		} else if (subjects.size() == 1) {
			for (GraphNode ns : subjects) {
				ns.setStyle("root");
				ns.setIcon(new ThemeResource("icons/48x48/1474988619_Home.png"));
				graph.setHomeNodeId(ns.getId());
			}

		}

		return list_of_new_uris;
	}

	private Model getModel(String url) {
		String json = getResponse(url);

		// System.out.println("URL:" + url);
		// System.out.println("json.....>");
		// System.out.println(json);
		if (json == null) {
			System.err.println(" No response from th server!");
			return null;
		}
		InputStream is = new ByteArrayInputStream(json.getBytes());

		Model model = ModelFactory.createDefaultModel();// createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);

		try {
			model.read(is, null, "JSON-LD");

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

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

}
