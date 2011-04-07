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

import processing.core.PApplet;

/**
 * Stores the parameters given in the network.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 * @version 1.1.0
 */
class Parameters {
	/**
	 * The parent Applet
	 */
	PApplet parent = null;

	/**
	 * The networks label
	 */
	String label = null;

	/**
	 * The server address, used to fetch pictures or to load neighbouring nodes
	 */
	String serverAddress = null;

	/**
	 * The directory on the server, where the pictures, that are used to
	 * represent nodes, are stored
	 */
	String picturePath = null;

	/**
	 * The path to the script that is used to load neighbouring nodes
	 */
	String neighbourhoodScript = null;

	/**
	 * Initializes the
	 * 
	 * @param parent
	 */
	Parameters (PApplet parent) {
		this.parent = parent;
	}

	/**
	 * Returns the networks label or an empty string if the label is not set
	 * 
	 * @return the networks label or an empty string if the label is not set
	 */
	String getLabel () {
		if (label != null)
			return label;
		else
			return "";
	}

	/**
	 * Return the path to the script used to load neighbouring nodes
	 * 
	 * @return The path to the script used to load neighbouring nodes
	 */
	String getNeighbourhoodScript () {
		return neighbourhoodScript;
	}

	/**
	 * Return the directory in which the pictures, that are used to represent
	 * nodes, are stored
	 * 
	 * @return The directory
	 */
	String getPicturePath () {
		return picturePath;
	}

	/**
	 * Return the address of the server, from which the applet was loaded.
	 * 
	 * @return The server address
	 */
	String getServerAdress () {
		return serverAddress;
	}

	/**
	 * Set the networks label
	 * 
	 * @param l
	 *            the label
	 */
	void setLabel (String l) {
		label = l;
	}

	/**
	 * Set the path to the script used to load neighbouring nodes
	 * 
	 * @param n
	 *            The path
	 */
	void setNeighbourhoodScript (String n) {
		neighbourhoodScript = n;
	}

	/**
	 * Set the directory in which the pictures, that are used to represent
	 * nodes, are stored
	 * 
	 * @param p
	 *            The directory
	 */
	void setPicturePath (String p) {
		if (p == null)
			picturePath = "thmubnails/";
		else
			picturePath = p;
	}

	/**
	 * Set the address of the server, from which the applet was loaded.
	 * 
	 * @param s
	 *            The server address
	 */
	void setServerAddress (String s) {
		serverAddress = s;
	}

	/**
	 * Return a GraphML representation of the parameters
	 */
	public String toGraphML () {
		String graphml = "";

		if (getPicturePath() != null)
			graphml += "<data key=\"picturepath\">" + getPicturePath() + "</data>\n";

		if (getNeighbourhoodScript() != null)
			graphml += "<data key=\"neighbourhoodscript\">" + getNeighbourhoodScript() + "</data>\n";

		return graphml;
	}

	/**
	 * Return a XGMML representation of the parameters
	 */
	public String toXGMML () {
		String xgmml = "";

		if (getPicturePath() != null)
			xgmml += "<att name=\"PICTURE_PATH\" value=\"" + getPicturePath() + "\"/>\n";

		if (getNeighbourhoodScript() != null)
			xgmml += "<att name=\"NEIGHBOURHOOD_SCRIPT\" value=\"" + getNeighbourhoodScript() + "\"/>\n";

		return xgmml;
	}

}
