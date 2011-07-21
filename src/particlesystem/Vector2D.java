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
 * Represents a vector in two dimensional space. Used to store the positions of
 * the nodes as well as forces and velocities that act in the particle system.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
public class Vector2D {
	/**
	 * x-value of the vector
	 */
	float x;

	/**
	 * y-value of the vector
	 */
	float y;

	/**
	 * Initializes a vector with (0, 0)
	 */
	public Vector2D () {
		x = 0;
		y = 0;
	}

	/**
	 * Initializes a vector with the given values
	 * 
	 * @param x
	 *            The vector's new x-value
	 * @param y
	 *            The vector's new y-value
	 */
	public Vector2D (float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Initializes a vector with another vector
	 * 
	 * @param p
	 *            The vector whose values are to be used for the new vector
	 */
	public Vector2D (Vector2D p) {
		x = p.x;
		y = p.y;
	}

	/**
	 * Add x- and y-values to the vector's values
	 * 
	 * @param x
	 *            The value to add to the x-value
	 * @param y
	 *            The value to add to the y-value
	 */
	public void add (float x, float y) {
		this.x += x;
		this.y += y;
	}

	/**
	 * Add another vector to the vector
	 * 
	 * @param p
	 *            The vecor whose values are to be added to the vector's values
	 */
	public void add (Vector2D p) {
		x += p.x;
		y += p.y;
	}

	/**
	 * Set the vector's values to (0, 0)
	 */
	public void clear () {
		x = 0;
		y = 0;
	}

	/**
	 * Return the square of the distance to the given vector
	 * 
	 * @param p
	 *            The vector to whome the distance is to be computed
	 * @return The square of the distance between the vectors
	 */
	public float getDistanceSquaredTo (Vector2D p) {
		float dx = x - p.x;
		float dy = y - p.y;
		return dx * dx + dy * dy;
	}

	/**
	 * Return the distance to a point given by the two parameters
	 * 
	 * @param x
	 *            The x-value of the given point
	 * @param y
	 *            The y-value to the given point
	 * @return The distance between the vector and the fiven point
	 */
	public float getDistanceToPoint (float x, float y) {
		float dx = this.x - x;
		float dy = this.y - y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Return the distance to the given vector
	 * 
	 * @param p
	 *            The vector to whome the distance is to be computed
	 * @return The distance between the vectors
	 */
	public float getDistanceToPoint (Vector2D p) {
		return (float) Math.sqrt(getDistanceSquaredTo(p));
	}

	/**
	 * Return the x-value of the vector
	 * 
	 * @return The x-value of the vector
	 */
	public float getX () {
		return x;
	}

	/**
	 * Return the y-value of the vector
	 * 
	 * @return The y-value of the vector
	 */
	public float getY () {
		return y;
	}

	/**
	 * Return true if the vector's values are (0, 0)
	 * 
	 * @return true if the vector's values are (0, 0), otherwise false
	 */
	public boolean isZero () {
		return x == 0 && y == 0;
	}

	/**
	 * Multiply the vector's values with a scalar
	 * 
	 * @param f
	 *            The scalar with which the vector's values are multiplied
	 * @return A reference to this vector having the values multiplied
	 */
	public Vector2D multiplyBy (float f) {
		x *= f;
		y *= f;
		return this;
	}

	/**
	 * Set the vector's values to the values given
	 * 
	 * @param x
	 *            The vector's new x-value
	 * @param y
	 *            The vector's new y-value
	 */
	public void set (float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Set the vector's values to the values of another vector
	 * 
	 * @param p
	 *            The vector whose values are to be adopted
	 */
	public void set (Vector2D p) {
		x = p.x;
		y = p.y;
	}

	/**
	 * Set the vector's x-value
	 * 
	 * @param x
	 *            The vector's new x-value
	 */
	public void setX (float x) {
		this.x = x;
	}

	/**
	 * Set the vector's y-value
	 * 
	 * @param y
	 *            The vector's new y-value
	 */
	public void setY (float y) {
		this.y = y;
	}

	/**
	 * Substract the given values from the vectors values
	 * 
	 * @param x
	 *            The value to substract from the vector's x-value
	 * @param y
	 *            The value to substract from the vector's y-value
	 */
	public void subtract (float x, float y) {
		this.x -= x;
		this.y -= y;
	}

	/**
	 * Substract the given vector from the vector
	 * 
	 * @param p
	 *            The vector whose values are to be substracted from the
	 *            vector's values
	 */
	public void subtract (Vector2D p) {
		x -= p.x;
		y -= p.y;
	}
}
