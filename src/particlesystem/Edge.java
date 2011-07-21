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

/**
 * Represents an Edge in the network. Edges act like springs; they try to keep
 * the nodes they connect in a defined distance from each other.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
public class Edge {
	/**
	 * Defines the factor by which the edge strength that is divided for any new
	 * edge
	 */
	final float STRENGTH_FACTOR = 1.1f;

	/**
	 * The strength of an edge, i.e. how strong an edge tries to keep its
	 * rest-length
	 */
	float strength;

	/**
	 * How strong the edges are damped in their attempt to return to their
	 * rest-length after being stretched or compressed
	 */
	float damping;

	/**
	 * The length all edges should ideally have
	 */
	float restLength;

	/**
	 * The edge's end shape 0 = none, 1 = arrow, 2 = dash, 3 = circle
	 */
	int shape;

	/**
	 * The node that is the one end of the edge
	 */
	Node source;

	/**
	 * The node that is the other end of the edge
	 */

	Node target;
	/**
	 * The color that is used to draw the edge
	 */
	int color;

	/**
	 * Whether the edge is visible
	 */
	boolean visible;

	/**
	 * Whether the edge is highlighted
	 */
	boolean highlighted;

	/**
	 * The annotation of the edge, that is a label that can be displayed to
	 * provide additional information about the edge
	 */
	String annotation;

	/**
	 * The edge's weight
	 */
	float weight = 1.0f;

	/**
	 * The weight that is used to draw the edge. It is the same as the edge's
	 * weight if the edge's weight but bounded to a certain range
	 */
	float strokeWeight = 1.0f;

	/**
	 * Create an edge
	 * 
	 * @param a
	 *            The node at the one end of the edge
	 * @param b
	 *            The node at the other end of the edge
	 * @param weight
	 *            The weight of the edge
	 * @param annotation
	 *            The annotation of the edge
	 * @param shape
	 *            The shape of the edge
	 * @param directed
	 *            Whether the network is directed or not, used to determine the
	 *            standard shape if no shape information is given
	 * @param color
	 *            The color that is used to draw the edge
	 * @param strength
	 *            The strength with which the edge tries to keep its rest-length
	 * @param damping
	 *            The damping that is applied while the edge tries to keep its
	 *            rest-length
	 * @param restLength
	 *            The edge's rest-length
	 */
	public Edge (Node a, Node b, float weight, String annotation, String shape, boolean directed, String color, float strength, float damping, float restLength) {
		this.source = a;
		this.target = b;
		this.weight = weight;
		this.annotation = annotation;
		this.strength = strength;
		this.damping = damping;
		this.restLength = restLength;
		this.visible = true;

		if (shape == null) {
			if (!directed)
				this.shape = 0;
			else
				this.shape = 1;
		} else {
			if (shape.equals("none"))
				this.shape = 0;
			else if (shape.equalsIgnoreCase("arrow"))
				this.shape = 1;
			else if (shape.equalsIgnoreCase("dash"))
				this.shape = 2;
			else if (shape.equalsIgnoreCase("circle"))
				this.shape = 3;
			else {
				if (!directed)
					this.shape = 0;
				else
					this.shape = 1;
			}
		}

		setColor(color);

		if (weight < 2)
			strokeWeight = 2.0f;
		else if (weight > 10)
			strokeWeight = 10.0f;
		else
			strokeWeight = weight;
	}

	/**
	 * Apply the forces that act through the edge. The force that is carried out
	 * by the edge is computed and applied to the nodes at its ends
	 */
	void apply () {
		if (source.isFree() || target.isFree()) {
			float a2bX = source.getPosition().x - target.getPosition().x;
			float a2bY = source.getPosition().y - target.getPosition().y;

			float a2bDistance = (float) Math.sqrt(a2bX * a2bX + a2bY * a2bY);

			if (a2bDistance == 0) {
				a2bX = 0;
				a2bY = 0;
			} else {
				a2bX /= a2bDistance;
				a2bY /= a2bDistance;
			}

			// edge force is proportional to how much it stretched
			float edgeForce = -(a2bDistance - restLength) * strength;

			// want velocity along line b/w a & b, damping force is proportional
			// to this
			float Va2bX = source.getVelocity().x - target.getVelocity().x;
			float Va2bY = source.getVelocity().y - target.getVelocity().y;

			float dampingForce = -damping * (a2bX * Va2bX + a2bY * Va2bY);

			// forceB is same as forceA in opposite direction
			float r = edgeForce + dampingForce;

			a2bX *= r;
			a2bY *= r;

			if (source.isFree())
				source.getForce().add(a2bX, a2bY);
			if (target.isFree())
				target.getForce().add(-a2bX, -a2bY);
		}
	}

	/**
	 * Decrease the edge length by the given value
	 * 
	 * @param l
	 *            How much the edge length is decreased
	 */
	public void decreaseEdgeLength (Float l) {
		if (restLength > l)
			restLength -= l;
		else
			restLength = 1f;
	}

	/**
	 * Decrease the strength of the edge by one step
	 */
	public void decreaseEdgeStrength () {
		strength /= STRENGTH_FACTOR;
	}

	/**
	 * Remove highlighting of an edge
	 */
	public void dehighlight () {
		highlighted = false;
	}

	/**
	 * Whether the current edge is equal to another edge, i.e. has the same
	 * source- and target-nodes as the other edge
	 * 
	 * @param other
	 *            the other edge
	 * @param directed
	 *            whether edges are directed or not
	 * @return true if the edges are identical, false otherwise
	 */
	public boolean equals (Object other, boolean directed) {
		if (!this.getClass().equals(other.getClass()))
			return false;

		Edge otherEdge = (Edge) other;

		if (directed)
			return (otherEdge.getSource().equals(getSource()) && otherEdge.getTarget().equals(getTarget()));
		else
			return ((otherEdge.getSource().equals(getSource()) && otherEdge.getTarget().equals(getTarget())) || (otherEdge.getSource().equals(getTarget()) && otherEdge.getTarget().equals(getSource())));
	}

	/**
	 * Return the edge's annotation
	 * 
	 * @return The edge's annotation
	 */
	public String getAnnotation () {
		return annotation;
	}

	/**
	 * Return the edge's color
	 * 
	 * @return The edge's color
	 */
	public int getColor () {
		return color;
	}

	/**
	 * Return the edge's color as a hexadecimal string
	 * 
	 * @return The edge's color as a hexadecimal string
	 */
	public String getColorAsHexString () {
		return "#" + Integer.toHexString(color).substring(2).toUpperCase();
	}

	/**
	 * Return the blue value of the edge's color
	 * 
	 * @return The blue value of the edge's color
	 */
	public int getColorBlue () {
		return color & 0xFF;
	}

	/**
	 * Return the green value of the edge's color
	 * 
	 * @return The green value of the edge's color
	 */
	public int getColorGreen () {
		return color >> 8 & 0xFF;
	}

	/**
	 * Return the red value of the edge's color
	 * 
	 * @return The red value of the edge's color
	 */
	public int getColorRed () {
		return color >> 16 & 0xFF;
	}

	/**
	 * Return the current length of the edge
	 * 
	 * @return The edge's current length
	 */
	public float getCurrentLength () {
		return source.getPosition().getDistanceToPoint(target.getPosition());
	}

	/**
	 * Return the damping applied on the edge's forces
	 * 
	 * @return The damping
	 */
	public float getDamping () {
		return damping;
	}

	/**
	 * Return the nearest distance between the edge and the given point (for
	 * straight edges)
	 * 
	 * @param x
	 *            x-coordinate of the point
	 * @param y
	 *            y-coordinate of the point
	 * @return The minimal distance
	 */
	public float getDistanceToPoint (float x, float y) {
		float x1 = source.getPosition().getX();
		float y1 = source.getPosition().getY();

		float px = target.getPosition().getX() - x1;
		float py = target.getPosition().getY() - y1;

		float u = ((x - x1) * px + (y - y1) * py) / (px * px + py * py);

		if (u > 1)
			u = 1;
		else if (u < 0)
			u = 0;

		float dx = x1 + u * px - x;
		float dy = y1 + u * py - y;

		return dx * dx + dy * dy;
	}

	/**
	 * Return the nearest distance between the edge and the given point (for
	 * circle edges)
	 * 
	 * @param x
	 *            x-coordinate of the point
	 * @param y
	 *            y-coordinate of the point
	 * @param node_size
	 *            the node size (to calculate the center of the circle edge)
	 * @return The minimal distance
	 */
	public float getDistanceToPoint (float x, float y, float node_size) {
		float xc = source.getPosition().getX() - node_size / 2;
		float yc = source.getPosition().getY() - node_size / 2;

		float dist = ((xc - x) * (xc - x) + (yc - y) * (yc - y)) - (node_size * node_size);

		return dist;
	}

	/**
	 * Return the desired rest-length of the edge
	 * 
	 * @return The rest-length
	 */
	public float getRestLength () {
		return restLength;
	}

	/**
	 * Return an edge's shape
	 * 
	 * @return The edge's shape
	 */
	public int getShape () {
		return shape;
	}

	/**
	 * Return the node's shape as a string
	 * 
	 * @return The node's shape as a string (one of "none", "arrow", "dash" and
	 *         "circle")
	 */
	public String getShapeAsString () {
		switch (getShape()) {
			case 0:
				return "none";
			case 1:
				return "arrow";
			case 2:
				return "dash";
			case 3:
				return "circle";
			default:
				return "none";
		}
	}

	/**
	 * Return the node at one end of the edge
	 * 
	 * @return The node at one end of the edge
	 */
	public Node getSource () {
		return source;
	}

	/**
	 * Return the strength with which the edge tries to keep its rest length
	 * 
	 * @return The strength
	 */
	public float getStrength () {
		return strength;
	}

	/**
	 * Return the weight of the stroke to draw the edge
	 * 
	 * @return The weight of the stroke to draw the edge
	 */
	public float getStrokeWeight () {
		return strokeWeight;
	}

	/**
	 * Return the node at the other end of the edge
	 * 
	 * @return The noder at the other end of the edge
	 */
	public Node getTarget () {
		return target;
	}

	/**
	 * Return the edge's weight
	 * 
	 * @return The edge's weight
	 */
	public float getWeight () {
		return weight;
	}

	/**
	 * Set the edge to invisible
	 */
	public void hide () {
		visible = false;
	}

	/**
	 * Highlight the edge
	 */
	public void highlight () {
		highlighted = true;
	}

	/**
	 * Increade the edge length by the given value
	 * 
	 * @param l
	 *            How much the edge length is increased
	 */
	public void increaseEdgeLength (Float l) {
		restLength += l;
	}

	/**
	 * Increase the strength of the edge by one step
	 */
	public void increaseEdgeStrength () {
		strength *= STRENGTH_FACTOR;
	}

	/**
	 * Return true if the edge is highlighted
	 * 
	 * @return true if the edge is highlighted, otherwise false
	 */
	public boolean isHighlighted () {
		return highlighted;
	}

	/**
	 * Return true if the edge is visible
	 * 
	 * @return true if the edge is visible, otherwise false
	 */
	public boolean isVisible () {
		return visible;
	}

	/**
	 * Set the edge's annotation
	 * 
	 * @param a
	 *            The edge's annotation
	 */
	public void setAnnotation (String a) {
		annotation = a;
	}

	/**
	 * Set the edge's color
	 * 
	 * @param c
	 *            The edge's color as a hexadecimal string
	 */
	public void setColor (String c) {
		try {
			if (c.charAt(0) == '#')
				c = c.substring(1);
			this.color = (255 << 24) | Integer.parseInt(c, 16);
		} catch (Exception e) {
			this.color = (255 << 24) | (100 << 16) | (100 << 8) | 100;
		}
	}

	/**
	 * Set the damping that is applied on the edge's forces
	 * 
	 * @param d
	 *            The damping
	 */
	public void setDamping (float d) {
		damping = d;
	}

	/**
	 * Set the edge's rest-length
	 * 
	 * @param l
	 *            The edge's rest-length
	 */
	public void setRestLength (float l) {
		restLength = l;
	}

	/**
	 * Set the edge's shape
	 * 
	 * @param s
	 *            The edge's shape
	 */
	public void setShape (int s) {
		shape = s;
	}

	/**
	 * Set the strength witch which the edge tries to keep its rest-length
	 * 
	 * @param s
	 *            The strength
	 */
	public void setStrength (float s) {
		strength = s;
	}

	/**
	 * Set the edge's weight
	 * 
	 * @param w
	 *            The edge's weight
	 */
	public void setWeight (float w) {
		weight = w;

		if (w < 2)
			strokeWeight = 2.0f;
		else if (w > 10)
			strokeWeight = 10.0f;
		else
			strokeWeight = w;
	}

	/**
	 * Set the edge to visible
	 */
	public void show () {
		visible = true;
	}

	/**
	 * Return a GraphML representation of the edge
	 * 
	 * @return The GraphML representation of the edge
	 */
	public String toGraphML () {
		String graphml = "<edge source=\"" + getSource().getId() + "\" target=\"" + getTarget().getId() + "\">\n";

		graphml += "\t<data key=\"weight\">" + getWeight() + "</data>\n";

		graphml += "\t<data key=\"edgeshape\">" + getShapeAsString() + "</data>\n";

		graphml += "\t<data key=\"edgelabel\">" + getAnnotation() + "</data>\n";

		graphml += "\t<data key=\"edger\">" + getColorRed() + "</data>\n";
		graphml += "\t<data key=\"edgeg\">" + getColorGreen() + "</data>\n";
		graphml += "\t<data key=\"edgeb\">" + getColorBlue() + "</data>\n";

		graphml += "</edge>\n";

		return graphml;
	}

	/**
	 * Return a string-representation of the edge, that is the Ids of both nodes
	 * separated by a dash
	 */
	public String toString () {
		return source.getId() + " - " + target.getId();
	}

	/**
	 * Return an XGMML representation of the edge
	 * 
	 * @return The XGMML representation of the edge
	 */
	public String toXGMML () {
		String xgmml = "<edge label=\"" + getAnnotation() + "\" source=\"" + getSource().getId() + "\" target=\"" + getTarget().getId() + "\" weight=\"" + getWeight() + "\">\n";

		xgmml += "\t<att name=\"edge.shape\" value=\"" + getShapeAsString() + "\"/>\n";

		xgmml += "\t<att name=\"edge.color\" value=\"" + getColorRed() + "," + getColorGreen() + "," + getColorBlue() + "\"/>\n";

		xgmml += "</edge>\n";

		return xgmml;
	}

	/**
	 * Set the edge to visible, if both of its nodes are visible, otherwise set
	 * it to invisible
	 */
	public void updateVisibility () {
		if (source.isVisible() && target.isVisible())
			show();
		else
			hide();
	}

}