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
 * Represents the repulsion between two nodes in the network. Repulsions
 * connect, like edges, two nodes with the difference, that they drive the nodes
 * apart from each other. The force that drives the nodes apart is the stronger
 * the closer the nodes are to each other.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
public class Repulsion {
	/**
	 * The maximal distance for which repulsive forces are taken into account
	 */
	final float DISTANCE_THRESHOLD = 300;

	/**
	 * The node that is one end of the repulsion.
	 */
	Node a;

	/**
	 * The node that is the other end of the repulsion.
	 */
	Node b;

	/**
	 * The strength of the repulsion
	 */
	float strength;

	/**
	 * The square of the minimal distance that is assumed between two nodes
	 */
	float distanceMinSquared;

	/**
	 * Create a new repulsion
	 * 
	 * @param a
	 *            The node at one end of the repulsion
	 * @param b
	 *            The node at the other end of the repulsion
	 * @param strength
	 *            The strength of the repulsion
	 * @param distanceMin
	 *            The minimal distance that is assumed between two nodes
	 */
	public Repulsion (Node a, Node b, float strength, float distanceMin) {
		this.a = a;
		this.b = b;
		this.strength = strength;
		this.distanceMinSquared = distanceMin * distanceMin;
	}

	/**
	 * Apply the forces that act through the repulsion. The force that is
	 * carried out by the repulsion is computed and applied to the nodes at its
	 * ends
	 */
	void apply () {
		if (a.isFree() || b.isFree()) {
			float a2bX = a.getPosition().getX() - b.getPosition().getX();
			float a2bY = a.getPosition().getY() - b.getPosition().getY();

			float a2bDistanceSquared = a2bX * a2bX + a2bY * a2bY;

			if (a2bDistanceSquared < distanceMinSquared)
				a2bDistanceSquared = distanceMinSquared;

			float length = (float) Math.sqrt(a2bDistanceSquared);

			if (length > DISTANCE_THRESHOLD)
				return;

			float force = -strength / a2bDistanceSquared;

			// make unit vector
			a2bX /= length;
			a2bY /= length;

			// multiply by force
			a2bX *= force;
			a2bY *= force;

			// apply
			if (a.isFree())
				a.getForce().add(-a2bX, -a2bY);
			if (b.isFree())
				b.getForce().add(a2bX, a2bY);
		}
	}

	/**
	 * Whether the current repulsion is equal to another repulsion, i.e. has the
	 * same start- and end-nodes as the other repulsion irrespective of its
	 * direction
	 * 
	 * @param other
	 *            the other repulsion
	 * @return true if the repulsions are identical, false otherwise
	 */
	public boolean equals (Object other) {
		if (!this.getClass().equals(other.getClass()))
			return false;

		Repulsion otherRepulsion = (Repulsion) other;
		return ((otherRepulsion.getOneEnd().equals(getOneEnd()) && otherRepulsion.getTheOtherEnd().equals(getTheOtherEnd())) || (otherRepulsion.getOneEnd().equals(getTheOtherEnd()) && otherRepulsion
				.getTheOtherEnd().equals(getOneEnd())));
	}

	/**
	 * Return the node at one end of the repulsion
	 * 
	 * @return The node at one end of the repulsion
	 */
	public Node getOneEnd () {
		return a;
	}

	/**
	 * Return the strength of the repulsion
	 * 
	 * @return The strength of the repulsion
	 */
	public float getStrength () {
		return strength;
	}

	/**
	 * Return the node at the other end of the repulsion
	 * 
	 * @return The node at the other end of the repulsion
	 */
	public Node getTheOtherEnd () {
		return b;
	}

	/**
	 * Set the strength of the repulsion
	 * 
	 * @param strength
	 *            The repulsion's strength
	 */
	public void setStrength (float strength) {
		this.strength = strength;
	}
}
