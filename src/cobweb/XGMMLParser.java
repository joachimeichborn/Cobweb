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
 * Parser to read XGMML-files and build the network.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 * @version 1.1.0
 */
public class XGMMLParser {
	/**
	 * The parent applet
	 */
	PApplet parent;

	/**
	 * Stores the parameters like picture path and neighbourhoodscript that are
	 * given in the XGMML-file
	 */
	Parameters params = null;

	/**
	 * The root element of the document
	 */
	private Element docEle = null;

	/**
	 * Initializes the XGMML-parser object and makes it ready to parse the given
	 * XGMML-string
	 * 
	 * @param parent
	 *            The parent applet
	 * @param xgmml
	 *            The network in XGMML format
	 */
	public XGMMLParser (PApplet parent, String xgmml) {
		this.parent = parent;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {

				public InputSource resolveEntity (String publicId, String systemId) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});

			dom = db.parse(new org.xml.sax.InputSource(new StringReader(xgmml)));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "An error occured while reading the XGMML-data:\n\n" + e.getMessage(), "Error reading XGMML-File", 2);
			e.printStackTrace();
		}

		docEle = dom.getDocumentElement();

		params = new Parameters(parent);
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
	 * Return a value given in an "att" tag
	 * 
	 * @param ele
	 *            The parent element whose child "att" tags are to be considered
	 * @param attributeName
	 *            The name of the "att" tag
	 * @return The value given in the "att" tag
	 */
	String getAttValue (Element ele, String attributeName) {
		NodeList nl = ele.getElementsByTagName("att");
		if (nl != null && nl.getLength() > 0)
			for (int i = 0; i < nl.getLength(); i++) {
				Element nEl = (Element) nl.item(i);
				if (nEl.getAttribute("name").equals(attributeName))
					return nEl.getAttribute("value");
			}

		return null;
	}

	/**
	 * Return the fill value for the graphics tag of a given node
	 * 
	 * @param ele
	 *            The node whose graphics tag is parsed
	 * @return The fill value of the graphics tag
	 */
	String getGraphicsFill (Element ele) {
		NodeList nl = ele.getElementsByTagName("graphics");
		if (nl != null && nl.getLength() > 0) {
			String c = ((Element) nl.item(0)).getAttribute("fill");
			if (c.equals(""))
				return null;
			else
				return c;
		}

		return null;
	}

	/**
	 * Return the type value for the graphics tag of a given node
	 * 
	 * @param ele
	 *            The node whose graphics tag is parsed
	 * @return The type value of the graphics tag
	 */
	String getGraphicsType (Element ele) {
		NodeList nl = ele.getElementsByTagName("graphics");
		if (nl != null && nl.getLength() > 0) {
			String t = ((Element) nl.item(0)).getAttribute("type");
			if (t.equals(""))
				return null;
			else
				return t;
		}

		return null;
	}

	/**
	 * Return an object containg all the parameters given in the XGMML-file,
	 * like picture path and neighbourhood-script
	 * 
	 * @return The parameters object
	 */
	Parameters getParameters () {
		return params;
	}

	/**
	 * Parse the XGMML-file for information about edges in the network
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
		String color = null;
		Node s = null;
		Node t = null;

		NodeList edgesList = docEle.getElementsByTagName("edge");
		if (edgesList != null) {
			for (int i = 0; i < edgesList.getLength(); i++) {
				el = (Element) edgesList.item(i);
				sId = el.getAttribute("source");
				tId = el.getAttribute("target");
				weight = el.getAttribute("weight");
				annotation = el.getAttribute("label");
				shape = getAttValue(el, "edge.shape");
				color = getAttValue(el, "edge.color");

				if (color != null) {
					try {
						String[] colorArray = color.split(",");
						int col = (255 << 24) | (Integer.parseInt(colorArray[0]) << 16) | (Integer.parseInt(colorArray[1]) << 8) | Integer.parseInt(colorArray[2]);
						color = "#" + Integer.toHexString(col).substring(2);
					} catch (Exception e) {
						System.err.println("Wrong color format");
					}
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
	 * Parse the XGMML-file for information about nodes in the network
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
	 * Parse the XGMML-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param x
	 *            The x-position where new nodes are to appear
	 * @param y
	 *            The y-position where new nodes are to appear
	 * @param fix
	 *            Whether to fix the new nodes or not
	 */
	void parseNodes (ParticleSystem ps, float x, float y, boolean fix) {
		Element el = null;
		String id = null;
		String name = null;
		String description = null;
		String picture = null;
		String position = null;
		String shape = null;
		String fillColor = null;
		Node n = null;
		Random random = new Random();
		ArrayList<Node> newNodes = new ArrayList<Node>();

		NodeList nodesList = docEle.getElementsByTagName("node");
		if (nodesList != null) {
			for (int i = 0; i < nodesList.getLength(); i++) {
				el = (Element) nodesList.item(i);

				id = el.getAttribute("id");
				name = el.getAttribute("label");
				description = getAttValue(el, "description");
				picture = getAttValue(el, "picture");
				position = getAttValue(el, "position");
				shape = getGraphicsType(el);
				fillColor = getGraphicsFill(el);

				n = ps.makeNode(id, name, description, picture, params.getServerAdress() + params.getPicturePath(), shape, fillColor);

				if (n != null) {
					addSpacersToNode(ps, n);

					if (position == null) {
						n.getPosition().set(x + (random.nextFloat() * 2 - 1), y + (random.nextFloat() * 2 - 1));
					} else {
						String[] positions = position.split(";");
						n.getPosition().set(Float.parseFloat(positions[0]), Float.parseFloat(positions[1]));
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
	 * Parse the XGMML-file for information about nodes in the network
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
	 * Parse the XGMML-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param width
	 *            The width of the visualisation applet
	 * @param height
	 *            The height of the visualisation applet
	 * @param fix
	 *            Whether to fix the new nodes or not
	 */
	void parseNodes (ParticleSystem ps, int width, int height, boolean fix) {
		parseNodes(ps, (float) width / 2, (float) height / 2, fix);
	}

	/**
	 * Parse the XGMML-file for parameters like the picture path and
	 * neighbourhood script
	 * 
	 * @param serverAddress
	 *            The address of the server from which the applet was loaded
	 */
	void parseParameters (ParticleSystem ps, String serverAddress) {
		params.setServerAddress(serverAddress);

		try {
			String directed = docEle.getAttribute("directed");

			if (directed.equals("1"))
				ps.setDirected(true);
			else
				ps.setDirected(false);
		} catch (Exception e) {
			ps.setDirected(false);
		}

		try {
			params.setLabel(docEle.getAttribute("label"));
		} catch (Exception e) {
		}

		params.setPicturePath(getAttValue(docEle, "PICTURE_PATH"));
		params.setNeighbourhoodScript(getAttValue(docEle, "NEIGHBOURHOOD_SCRIPT"));
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
