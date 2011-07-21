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

import java.util.Random;

import particlesystem.Node;
import particlesystem.ParticleSystem;
import processing.core.PApplet;

/**
 * Parser to read sif-files and build the network.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
public class SIFParser {
	/**
	 * The parent applet
	 */
	PApplet parent;

	String sif;

	/**
	 * Stores the parameters like picture path and neighbourhoodscript
	 */
	Parameters params = null;

	/**
	 * Initializes the sif-parser object and makes it ready to parse the given
	 * sif-string
	 * 
	 * @param parent
	 *            The parent applet
	 * @param sif
	 *            The network in sif format
	 */
	public SIFParser (PApplet parent, String sif) {
		this.parent = parent;
		this.sif = sif;

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
	 * Return an object containg all the parameters like picture path and
	 * neighbourhood-script
	 * 
	 * @return The parameters object
	 */
	Parameters getParameters () {
		return params;
	}

	/**
	 * Parse the sif-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param x
	 *            The x-position where new nodes are to appear
	 * @param y
	 *            The y-position where new nodes are to appear
	 */
	void parseNetwork (ParticleSystem ps, float x, float y, float edgeLength) {
		Random random = new Random();

		String delimiter = "\t";
		if (sif.indexOf('\t') == -1)
			delimiter = " ";

		String[] lines = sif.split("\n");

		Node n = null;
		Node s = null;
		Node t = null;

		for (int i = 0; i < lines.length; i++) {
			String[] elems = lines[i].split(delimiter);

			n = ps.makeNode(elems[0], elems[0], null, null, params.getServerAdress() + params.getPicturePath(), null, null);
			if (n != null) {
				addSpacersToNode(ps, n);
				n.getPosition().set(x + (random.nextFloat() * 2 - 1), y + (random.nextFloat() * 2 - 1));
			}

			s = ps.getNodeById(elems[0]);

			for (int j = 2; j < elems.length; j++) {
				n = ps.makeNode(elems[j], elems[j], null, null, params.getServerAdress() + params.getPicturePath(), null, null);
				if (n != null) {
					addSpacersToNode(ps, n);
					n.getPosition().set(x + (random.nextFloat() * 2 - 1), y + (random.nextFloat() * 2 - 1));
				}

				t = ps.getNodeById(elems[j]);

				ps.makeEdge(s, t, 1, elems[1], null, null, Cobweb.edgeStrength, Cobweb.edgeStrength, edgeLength);

			}
		}
	}

	/**
	 * Parse the sif-file for information about nodes in the network
	 * 
	 * @param ps
	 *            The particle System
	 * @param width
	 *            The width of the visualisation applet
	 * @param height
	 *            The height of the visualisation applet
	 */
	void parseNetwork (ParticleSystem ps, int width, int height, float edgeLength) {
		parseNetwork(ps, (float) width / 2, (float) height / 2, edgeLength);
	}

	/**
	 * Parse the sif file for parameters and set the server Address
	 * 
	 * @param serverAddress
	 *            The address of the server from which the applet was loaded
	 */
	void parseParameters (ParticleSystem ps, String serverAddress) {
		ps.setDirected(true);
		
		params.setServerAddress(serverAddress);
	}

}
