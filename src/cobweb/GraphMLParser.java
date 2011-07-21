/*
	Cobweb
	Copyright (C) 2010 Joachim von Eichborn
 
    This file is part of Cobweb.

    Cobweb is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cobweb is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cobweb.  If not, see <http://www.gnu.org/licenses/>.
 */

package cobweb;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import particlesystem.Node;
import particlesystem.ParticleSystem;
import processing.core.PApplet;

/**
 * Parser to read GraphML-files and build the network.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
public class GraphMLParser {
	/**
	 * The parent applet
	 */
	PApplet parent;

	/**
	 * Stores the parameters like picture path and neighbourhoodscript that are
	 * given in the GraphML-file
	 */
	Parameters params = null;

	/**
	 * The root element of the document
	 */
	private Element docEle = null;

	/**
	 * The mapping of key-name and -for information to key-ids
	 */
	private HashMap<String, String> keyHash = null;

	/**
	 * Initializes the GraphML-parser object and makes it ready to parse the
	 * given GraphML-string
	 * 
	 * @param parent
	 *            The parent applet
	 * @param graphml
	 *            The network in GraphML format
	 */
	public GraphMLParser (PApplet parent, String graphml) {
		this.parent = parent;
		keyHash = new HashMap<String, String>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {

				public InputSource resolveEntity (String publicId, String systemId) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});

			dom = db.parse(new org.xml.sax.InputSource(new StringReader(graphml)));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "An error occured while reading the GraphML-data:\n\n" + e.getMessage(), "Error reading GraphML-File", 2);
			e.printStackTrace();
		}

		docEle = dom.getDocumentElement();

		params = new Parameters(parent);

		parseKeys();
	}

	/**
	 * Add spacers between the given node and every other node in the particle
	 * system, these spacers cause tow nodes to repulse each other.
	 * 
	 * @param ps
	 *            The particle system
	 * @param n
	 *            The node that has to get spacers to all other nodes
	 */
	void addSpacersToNode (ParticleSystem ps, Node n) {
		for (int i = 0; i < ps.numberOfNodes(); ++i) {
			Node q = ps.getNode(i);
			if (n != q)
				ps.makeRepulsion(n, q, Cobweb.repulsionStrength, 20);
		}
	}

	/**
	 * Return a value given in an "data" tag of an edge
	 * 
	 * @param ele
	 *            The parent edge whose child "data" tags are to be considered
	 * @param attributeName
	 *            The name of the key corresponding to the "data" tag whose
	 *            value is returned
	 * @return The value given in the "data" tag
	 */
	String getEdgeDataValue (Element ele, String attributeName) {
		String key = attributeName + "#edge";
		if (keyHash.containsKey(key)) {
			String id = keyHash.get(key);
			Element nEl = null;

			NodeList nl = ele.getElementsByTagName("data");
			if (nl != null && nl.getLength() > 0)
				for (int i = 0; i < nl.getLength(); i++) {
					nEl = (Element) nl.item(i);
					if (nEl.getAttribute("key").equals(id))
						return nEl.getTextContent();
				}
		}

		return null;
	}

	/**
	 * Return a value given in an "data" tag of a graph
	 * 
	 * @param ele
	 *            The parent graph whose child "data" tags are to be considered
	 * @param attributeName
	 *            The name of the key corresponding to the "data" tag whose
	 *            value is returned
	 * @return The value given in the "data" tag
	 */
	String getGraphDataValue (Element ele, String attributeName) {
		String key = attributeName + "#graph";
		if (keyHash.containsKey(key)) {
			String id = keyHash.get(key);
			Element nEl = null;

			NodeList nl = ele.getElementsByTagName("data");
			if (nl != null && nl.getLength() > 0)
				for (int i = 0; i < nl.getLength(); i++) {
					nEl = (Element) nl.item(i);
					if (nEl.getAttribute("key").equals(id))
						return nEl.getTextContent();
				}
		}

		return null;
	}

	/**
	 * Return a value given in an "data" tag of a node
	 * 
	 * @param ele
	 *            The parent node whose child "data" tags are to be considered
	 * @param attributeName
	 *            The name of the key corresponding to the "data" tag whose
	 *            value is returned
	 * @return The value given in the "data" tag
	 */
	String getNodeDataValue (Element ele, String attributeName) {
		String key = attributeName + "#node";
		if (keyHash.containsKey(key)) {
			String id = keyHash.get(key);
			Element nEl = null;

			NodeList nl = ele.getElementsByTagName("data");
			if (nl != null && nl.getLength() > 0)
				for (int i = 0; i < nl.getLength(); i++) {
					nEl = (Element) nl.item(i);
					if (nEl.getAttribute("key").equals(id))
						return nEl.getTextContent();
				}
		}

		return null;
	}

	/**
	 * Return an object containg all the parameters given in the GraphML-file,
	 * like picture path and neighbourhood-script
	 * 
	 * @return The parameters object
	 */
	Parameters getParameters () {
		return params;
	}

	/**
	 * Parse the GraphML-file for information about edges in the network
	 * 
	 * @param ps
	 *            The particle system
	 * @param edgeLength
	 *            The default edge-length
	 */
	void parseEdges (ParticleSystem ps, float edgeLength) {
		Element el = null;
		String sId = null;
		String tId = null;
		String weight = null;
		float fweight = 1.0f;
		String annotation = null;
		String shape = null;
		String r = null;
		String g = null;
		String b = null;
		String color = null;
		Node s = null;
		Node t = null;

		NodeList edgesList = docEle.getElementsByTagName("edge");
		if (edgesList != null) {
			for (int i = 0; i < edgesList.getLength(); i++) {
				el = (Element) edgesList.item(i);
				sId = el.getAttribute("source");
				tId = el.getAttribute("target");

				weight = getEdgeDataValue(el, "weight");
				annotation = getEdgeDataValue(el, "label");
				shape = getEdgeDataValue(el, "shape");
				r = getEdgeDataValue(el, "r");
				g = getEdgeDataValue(el, "g");
				b = getEdgeDataValue(el, "b");

				if (r != null && g != null && b != null) {
					int col = (255 << 24) | (Integer.parseInt(r) << 16) | (Integer.parseInt(g) << 8) | Integer.parseInt(b);
					color = "#" + Integer.toHexString(col).substring(2);
				}

				if (!weight.equals(""))
					fweight = Float.valueOf(weight);

				s = ps.getNodeById(sId);
				t = ps.getNodeById(tId);

				if (s != null && t != null)
					ps.makeEdge(s, t, fweight, annotation, shape, color, Cobweb.edgeStrength, Cobweb.edgeStrength, edgeLength);
			}
		}
	}

	/**
	 * Parse the mapping of key-name and -for information to key-ids
	 */
	private void parseKeys () {
		Element el = null;
		String id = null;
		String elem = null;
		String name = null;

		NodeList keysList = docEle.getElementsByTagName("key");
		if (keysList != null) {
			for (int i = 0; i < keysList.getLength(); i++) {
				el = (Element) keysList.item(i);
				id = el.getAttribute("id");
				elem = el.getAttribute("for");
				name = el.getAttribute("attr.name");
				keyHash.put(name + "#" + elem, id);
			}
		}
	}

	/**
	 * Parse the GraphML-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param x
	 *            The x-position where new nodes are to appear
	 * @param y
	 *            The y-position where new nodes are to appear
	 */
	void parseNodes (ParticleSystem ps, float x, float y) {
		parseNodes(ps, x, y, false);
	}

	/**
	 * Parse the GraphML-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param x
	 *            The x-position where new nodes are to appear
	 * @param y
	 *            The y-position where new nodes are to appear
	 * @param fix
	 *            Whether to fix new nodes or not
	 */
	void parseNodes (ParticleSystem ps, float x, float y, boolean fix) {
		Element el = null;
		String id = null;
		String name = null;
		String description = null;
		String picture = null;
		String xPos = null;
		String yPos = null;
		String shape = null;
		String r = null;
		String g = null;
		String b = null;
		String fillColor = null;
		Node n = null;
		Random random = new Random();
		ArrayList<Node> newNodes = new ArrayList<Node>();

		NodeList nodesList = docEle.getElementsByTagName("node");
		if (nodesList != null) {
			for (int i = 0; i < nodesList.getLength(); i++) {
				el = (Element) nodesList.item(i);

				id = el.getAttribute("id");

				name = getNodeDataValue(el, "label");
				description = getNodeDataValue(el, "description");
				picture = getNodeDataValue(el, "picture");
				xPos = getNodeDataValue(el, "x");
				yPos = getNodeDataValue(el, "y");
				shape = getNodeDataValue(el, "shape");
				r = getNodeDataValue(el, "r");
				g = getNodeDataValue(el, "g");
				b = getNodeDataValue(el, "b");

				if (r != null && g != null && b != null) {
					int color = (255 << 24) | (Integer.parseInt(r) << 16) | (Integer.parseInt(g) << 8) | Integer.parseInt(b);
					fillColor = "#" + Integer.toHexString(color).substring(2);
				}

				n = ps.makeNode(id, name, description, picture, params.getServerAdress() + params.getPicturePath(), shape, fillColor);

				if (n != null) {
					addSpacersToNode(ps, n);

					if (xPos == null || yPos == null) {
						n.getPosition().set(x + (random.nextFloat() * 2 - 1), y + (random.nextFloat() * 2 - 1));
					} else {
						n.getPosition().set(Float.parseFloat(xPos), Float.parseFloat(yPos));
						n.fix();
					}
				}
				newNodes.add(n);
			}

			if (fix) {
				for (int i = 0; i < 20; ++i)
					((Cobweb) parent).getParticleSystem().tick();

				for (int i = 0; i < newNodes.size(); ++i)
					newNodes.get(i).fix();
			}
		}
	}

	/**
	 * Parse the GraphML-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param width
	 *            The width of the visualisation applet
	 * @param height
	 *            The height of the visualisation applet
	 */
	void parseNodes (ParticleSystem ps, int width, int height) {
		parseNodes(ps, (float) width / 2, (float) height / 2);
	}

	/**
	 * Parse the GraphML-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param width
	 *            The width of the visualisation applet
	 * @param height
	 *            The height of the visualisation applet
	 * @param fix
	 *            Whether to fix new nodes or not
	 */
	void parseNodes (ParticleSystem ps, int width, int height, boolean fix) {
		parseNodes(ps, (float) width / 2, (float) height / 2, fix);
	}

	/**
	 * Parse the GraphML-file for parameters like the picture path and
	 * neighbourhood script
	 * 
	 * @param serverAddress
	 *            The address of the server from which the applet was loaded
	 */
	void parseParameters (ParticleSystem ps, String serverAddress) {
		params.setServerAddress(serverAddress);

		NodeList graphList = docEle.getElementsByTagName("graph");
		if (graphList != null) {
			Element el = (Element) graphList.item(0);

			String id = el.getAttribute("id");

			if (id != null)
				params.setLabel(id);

			String directed = el.getAttribute("edgedefault");

			if (directed != null) {
				if (directed.equals("directed"))
					ps.setDirected(true);
				else
					ps.setDirected(false);
			}

			params.setPicturePath(getGraphDataValue(el, "picturepath"));
			params.setNeighbourhoodScript(getGraphDataValue(el, "neighbourhoodscript"));
		}
	}

	/**
	 * Set the parameters
	 * 
	 * @param params
	 *            The object containing the parameters
	 */
	void setParameters (Parameters params) {
		this.params = params;
	}
}
