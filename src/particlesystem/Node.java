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

package particlesystem;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a node in the network.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 * @version 1.1.0
 */
public class Node {
	/**
	 * Defines gravity strength that is added/subsracted from nodes for
	 * incoming/outgoing edges
	 */
	final Vector2D GRAVITY_STEP = new Vector2D(0f, 1f);

	/**
	 * The node's Id, this is the node's identifier and has to be unique for
	 * each node
	 */
	String id;

	/**
	 * The node's name
	 */
	String name;

	/**
	 * A longer description of the node
	 */
	String description;

	/**
	 * The name of the picture used to represent the node
	 */
	String pictureName;

	/**
	 * The color that is used to fill the node
	 */
	int fillColor;

	/**
	 * The node's shape 0 = circle, 1 = triangle, 2 = box, 3 = rectangle, 4 =
	 * rhombus, 5 = hexagon, 6 = octagon, 7 = horizontal ellipsis, 8 = vertical
	 * ellipsis
	 */
	int shape;

	/**
	 * The position of the node
	 */
	Vector2D position;

	/**
	 * The node's velocity
	 */
	Vector2D velocity;

	/**
	 * The forces acting on the node
	 */
	Vector2D force;

	/**
	 * The gravity acting on the node (depends on the number of
	 * incoming/outgoing edges)
	 */
	Vector2D gravity;

	/**
	 * Whether the node is visible
	 */
	boolean visible;

	/**
	 * Whether the node is highlighted
	 */
	boolean highlighted;

	/**
	 * Whether the node is fixed or can be moved by the integrator
	 */
	boolean fixed;

	/**
	 * The picture used to represent the node
	 */
	PImage img;

	/**
	 * Create a new node
	 * 
	 * @param id
	 *            The node's Id
	 * @param name
	 *            The node's name
	 * @param description
	 *            A longer description of the node
	 * @param pictureName
	 *            The name of the picture that is used to represent the node
	 * @param picturePath
	 *            The path were the pictures are stored on the server
	 * @param shape
	 *            The node's shape
	 * @param fillColor
	 *            The color that is used to fill the node
	 */
	public Node (String id, String name, String description, String pictureName, String picturePath, String shape, String fillColor) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.pictureName = pictureName;

		if (shape == null)
			this.shape = 0;
		else {
			if (shape.equals("circle"))
				this.shape = 0;
			else if (shape.equalsIgnoreCase("triangle"))
				this.shape = 1;
			else if (shape.equalsIgnoreCase("box"))
				this.shape = 2;
			else if (shape.equalsIgnoreCase("rectangle"))
				this.shape = 3;
			else if (shape.equalsIgnoreCase("rhombus"))
				this.shape = 4;
			else if (shape.equalsIgnoreCase("hexagon"))
				this.shape = 5;
			else if (shape.equalsIgnoreCase("octagon"))
				this.shape = 6;
			else if (shape.equalsIgnoreCase("hor_ellipsis"))
				this.shape = 7;
			else if (shape.equalsIgnoreCase("ver_ellipsis"))
				this.shape = 8;
			else
				this.shape = 0;
		}

		setFillColor(fillColor);

		this.position = new Vector2D();
		this.velocity = new Vector2D();
		this.force = new Vector2D();
		this.gravity = new Vector2D();
		this.fixed = false;
		this.img = null;
		this.visible = true;

		if (this.name.equals(""))
			this.name = id;
		// if (this.description == null)
		// this.description = this.name;
		try {
			if (pictureName != null)
				img = (new PApplet()).loadImage(picturePath + pictureName);
		} catch (Exception e) {
			img = null;
		}
	}

	/**
	 * Decreases a node's gravity by one step
	 */
	public void decreaseGravity () {
		gravity.subtract(GRAVITY_STEP);
	}

	/**
	 * Remove highlighting of a node
	 */
	void dehighlight () {
		highlighted = false;
	}

	/**
	 * Whether the node is equal to another node, i.e. whether the nodes have
	 * the same Ids
	 */
	public boolean equals (Object other) {
		if (!this.getClass().equals(other.getClass()))
			return false;

		return ((Node) other).getId().equals(id);
	}

	/**
	 * Fixes a node, i.e. the integrator can not move the node any more
	 */
	public void fix () {
		fixed = true;
		velocity.clear();
	}

	/**
	 * Unfix a node
	 */
	public void free () {
		fixed = false;
	}

	/**
	 * Return the node's description
	 * 
	 * @return The node's description
	 */
	public String getDescription () {
		return description;
	}

	/**
	 * Return the color that is used to fill the node
	 * 
	 * @return The color that is used to fill the node
	 */
	public int getFillColor () {
		return fillColor;
	}

	/**
	 * Return the color that is used to fill the node as a hexidecimal string
	 * 
	 * @return The color that is used to fill the node as a hexidecimal string
	 */
	public String getFillColorAsHexString () {
		return "#" + Integer.toHexString(fillColor).substring(2).toUpperCase();
	}

	/**
	 * Return the blue value of the color that is used to fill the node
	 * 
	 * @return The blue value of the color that is used to fill the node
	 */
	public int getFillColorBlue () {
		return fillColor & 0xFF;
	}

	/**
	 * Return the green value of the color that is used to fill the node
	 * 
	 * @return The green value of the color that is used to fill the node
	 */
	public int getFillColorGreen () {
		return fillColor >> 8 & 0xFF;
	}

	/**
	 * Return the red value of the color that is used to fill the node
	 * 
	 * @return The red value of the color that is used to fill the node
	 */
	public int getFillColorRed () {
		return fillColor >> 16 & 0xFF;
	}

	/**
	 * Return the forces acting on the node
	 * 
	 * @return The forces acting on the node
	 */
	public Vector2D getForce () {
		return force;
	}

	/**
	 * Return the node's identifier
	 * 
	 * @return The node's identifier
	 */
	public String getId () {
		return id;
	}

	/**
	 * Return the image used to represent the node
	 * 
	 * @return The image used to represent the node
	 */
	public PImage getImage () {
		return img;
	}

	/**
	 * Return the node's name
	 * 
	 * @return The node's name
	 */
	public String getName () {
		return name;
	}

	/**
	 * Return the name of the picture used to represent the node
	 * 
	 * @return The name of the picture used to represent the node
	 */
	public String getPictureName () {
		return pictureName;
	}

	/**
	 * Return the position of the node
	 * 
	 * @return The position of the node
	 */
	public Vector2D getPosition () {
		return position;
	}

	/**
	 * Return a node's shape
	 * 
	 * @return The node's shape
	 */
	public int getShape () {
		return shape;
	}

	/**
	 * Return the node's shape as a string
	 * 
	 * @return The node's shape as a string (one of "circle", "triangle", "box",
	 *         "rectangle", "rhombus", "hexagon", "octagon", "hor_ellipsis" and
	 *         "ver_ellipsis")
	 */
	public String getShapeAsString () {
		switch (getShape()) {
			case 0:
				return "circle";
			case 1:
				return "triangle";
			case 2:
				return "box";
			case 3:
				return "rectangle";
			case 4:
				return "rhombus";
			case 5:
				return "hexagon";
			case 6:
				return "octagon";
			case 7:
				return "hor_ellipsis";
			case 8:
				return "ver_ellipsis";
			default:
				return "circle";
		}
	}

	/**
	 * Return the node's velocity
	 * 
	 * @return The node's velocity
	 */
	public Vector2D getVelocity () {
		return velocity;
	}

	/**
	 * Return true if an image is available to represent the node
	 * 
	 * @return true if an image is available to represent the node, otherwise
	 *         false
	 */
	public boolean hasImage () {
		return img != null;
	}

	/**
	 * Set the node to invisible
	 */
	public void hide () {
		visible = false;
	}

	/**
	 * Highlight the node
	 */
	void highlight () {
		highlighted = true;
	}

	/**
	 * Increases a node's gravity by one step
	 */
	public void increaseGravity () {
		gravity.add(GRAVITY_STEP);
	}

	/**
	 * Return true if the node is fixed
	 * 
	 * @return true if the node is fixed, otherwise false
	 */
	public boolean isFixed () {
		return fixed;
	}

	/**
	 * Return true if the node is not fixed
	 * 
	 * @return true if the node is not fixed, otherwise false
	 */
	public boolean isFree () {
		return !fixed;
	}

	/**
	 * Return true if the node is highlighted
	 * 
	 * @return true if the node is highlighted, otherwise false
	 */
	public boolean isHighlighted () {
		return highlighted;
	}

	/**
	 * Return true if the node is visible
	 * 
	 * @return true if the node is visible, otherwise false
	 */
	public boolean isVisible () {
		return visible;
	}

	/**
	 * Sets the node's description
	 * 
	 * @param d
	 *            The node's description
	 */
	public void setDescription (String d) {
		description = d;
	}

	/**
	 * Sets the color that is used to fill the node
	 * 
	 * @param c
	 *            The color that is used to fill the node as a hexidecimal
	 *            string
	 */
	public void setFillColor (String c) {
		try {
			if (c.charAt(0) == '#')
				c = c.substring(1);
			this.fillColor = (255 << 24) | Integer.parseInt(c, 16);
		} catch (Exception e) {
			this.fillColor = (255 << 24) | (140 << 16) | (140 << 8) | 214;
		}
	}

	/**
	 * Set the node's name
	 * 
	 * @param n
	 *            The node's name
	 */
	public void setName (String n) {
		name = n;
	}

	/**
	 * Set the picture that is used to represent the node
	 * 
	 * @param p
	 *            The picture name
	 * @param picturePath
	 *            The directory where the pictures are stored on the server
	 */
	public void setPicture (String p, String picturePath) {
		pictureName = p;
		try {
			if (pictureName != null)
				img = (new PApplet()).loadImage(picturePath + pictureName);
			else
				img = null;
		} catch (Exception e) {
			img = null;
		}
	}

	/**
	 * Sets the node's position
	 * 
	 * @param p
	 *            The node's position
	 */
	public void setPosition (Vector2D p) {
		position = p;
	}

	/**
	 * Set the node's shape
	 * 
	 * @param s
	 *            The node's shape
	 */
	public void setShape (int s) {
		shape = s;
	}

	/**
	 * Set the node to visible
	 */
	public void show () {
		visible = true;
	}

	/**
	 * Return a GraphML representation of the node
	 * 
	 * @return The GraphML representation of the node
	 */
	public String toGraphML () {
		String graphml = "";

		graphml += "<node id=\"" + getId() + "\">\n";

		graphml += "\t<data key=\"name\">" + getName() + "</data>\n";

		graphml += "\t<data key=\"shape\">" + getShapeAsString() + "</data>\n";

		graphml += "\t<data key=\"r\">" + getFillColorRed() + "</data>\n";
		graphml += "\t<data key=\"g\">" + getFillColorGreen() + "</data>\n";
		graphml += "\t<data key=\"b\">" + getFillColorBlue() + "</data>\n";

		if (getDescription() != null)
			graphml += "\t<data key=\"description\">" + getDescription() + "</data>\n";

		if (getPictureName() != null)
			graphml += "\t<data key=\"picture\">" + getPictureName() + "</data>\n";

		graphml += "\t<data key=\"x\">" + getPosition().getX() + "</data>\n";
		graphml += "\t<data key=\"y\">" + getPosition().getY() + "</data>\n";

		graphml += "</node>\n";

		return graphml;
	}

	/**
	 * Return a XGMML representation of the node
	 * 
	 * @return The XGMML representation of the node
	 */
	public String toXGMML () {
		String xgmml = "";

		xgmml += "<node id=\"" + getId() + "\" label=\"" + getName() + "\">\n";

		xgmml += "\t<graphics type=\"" + getShapeAsString() + "\" fill=\"" + getFillColorAsHexString() + "\"/>\n";

		if (getDescription() != null)
			xgmml += "\t<att name=\"description\" value=\"" + getDescription() + "\"/>\n";

		if (getPictureName() != null)
			xgmml += "\t<att name=\"picture\" value=\"" + getPictureName() + "\"/>\n";

		xgmml += "\t<att name=\"position\" value=\"" + (int) getPosition().getX() + ";" + (int) getPosition().getY() + "\"/>\n";

		xgmml += "</node>\n";

		return xgmml;
	}
}
