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

import java.util.ArrayList;

/**
 * Fourth order Runge-Kutta-Integrator to compute the movement of the nodes in
 * the particle system.
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
public class Integrator {
	/**
	 * The nodes position before each step of the integrator
	 */
	ArrayList<Vector2D> originalPositions;

	/**
	 * The nodes velocities before each step of the integrator
	 */
	ArrayList<Vector2D> originalVelocities;

	/**
	 * First order forces
	 */
	ArrayList<Vector2D> k1Forces;

	/**
	 * First order velocities
	 */
	ArrayList<Vector2D> k1Velocities;

	/**
	 * Second order forces
	 */
	ArrayList<Vector2D> k2Forces;

	/**
	 * Second order velocities
	 */
	ArrayList<Vector2D> k2Velocities;

	/**
	 * Third order forces
	 */
	ArrayList<Vector2D> k3Forces;

	/**
	 * Third order velocities
	 */
	ArrayList<Vector2D> k3Velocities;

	/**
	 * Fourth order forces
	 */
	ArrayList<Vector2D> k4Forces;

	/**
	 * Fourth order velocities
	 */
	ArrayList<Vector2D> k4Velocities;

	/**
	 * The particle system
	 */
	ParticleSystem s;

	/**
	 * Initializes the integrator
	 * 
	 * @param s
	 *            The particle system
	 */
	Integrator (ParticleSystem s) {
		this.s = s;

		originalPositions = new ArrayList<Vector2D>();
		originalVelocities = new ArrayList<Vector2D>();
		k1Forces = new ArrayList<Vector2D>();
		k1Velocities = new ArrayList<Vector2D>();
		k2Forces = new ArrayList<Vector2D>();
		k2Velocities = new ArrayList<Vector2D>();
		k3Forces = new ArrayList<Vector2D>();
		k3Velocities = new ArrayList<Vector2D>();
		k4Forces = new ArrayList<Vector2D>();
		k4Velocities = new ArrayList<Vector2D>();
	}

	/**
	 * Allocates space to compute all forces and velocities for the fourth order
	 * integration
	 * 
	 * @param nodeSize
	 *            The number of nodes in the particle system
	 */
	void allocateNodes (int nodeSize) {
		while (nodeSize > originalPositions.size()) {
			originalPositions.add(new Vector2D());
			originalVelocities.add(new Vector2D());
			k1Forces.add(new Vector2D());
			k1Velocities.add(new Vector2D());
			k2Forces.add(new Vector2D());
			k2Velocities.add(new Vector2D());
			k3Forces.add(new Vector2D());
			k3Velocities.add(new Vector2D());
			k4Forces.add(new Vector2D());
			k4Velocities.add(new Vector2D());
		}
	}

	/**
	 * Perform one step of the integrater, new positions and velocities are
	 * computed for all nodes
	 */
	void step () {
		int nodeSize = s.nodes.size();
		allocateNodes(nodeSize);

		Node n = null;

		Vector2D originalPosition = null;
		Vector2D originalVelocity = null;
		Vector2D k1Force = null;
		Vector2D k1Velocity = null;
		Vector2D k2Force = null;
		Vector2D k2Velocity = null;
		Vector2D k3Force = null;
		Vector2D k3Velocity = null;
		Vector2D k4Force = null;
		Vector2D k4Velocity = null;

		// save original position and velocities
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				(originalPositions.get(i)).set(n.position);
				(originalVelocities.get(i)).set(n.velocity);
			}

			n.force.clear(); // and clear the forces
		}

		// get all the k1 values
		s.applyForces();

		// save the intermediate forces
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				(k1Forces.get(i)).set(n.force);
				(k1Velocities.get(i)).set(n.velocity);
			}

			n.force.clear();
		}

		// get k2 values
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				originalPosition = originalPositions.get(i);
				k1Velocity = k1Velocities.get(i);

				n.position.x = originalPosition.x + k1Velocity.x * 0.5f;
				n.position.y = originalPosition.y + k1Velocity.y * 0.5f;

				originalVelocity = originalVelocities.get(i);
				k1Force = k1Forces.get(i);

				n.velocity.x = originalVelocity.x + k1Force.x * 0.5f;
				n.velocity.y = originalVelocity.y + k1Force.y * 0.5f;
			}
		}

		s.applyForces();

		// save the intermediate forces
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				(k2Forces.get(i)).set(n.force);
				(k2Velocities.get(i)).set(n.velocity);
			}

			n.force.clear(); // and clear the forces now that we are done with
			// them
		}

		// get k3 values
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				originalPosition = originalPositions.get(i);
				k2Velocity = k2Velocities.get(i);

				n.position.x = originalPosition.x + k2Velocity.x * 0.5f;
				n.position.y = originalPosition.y + k2Velocity.y * 0.5f;

				originalVelocity = originalVelocities.get(i);
				k2Force = k2Forces.get(i);

				n.velocity.x = originalVelocity.x + k2Force.x * 0.5f;
				n.velocity.y = originalVelocity.y + k2Force.y * 0.5f;
			}
		}

		s.applyForces();

		// save the intermediate forces
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				(k3Forces.get(i)).set(n.force);
				(k3Velocities.get(i)).set(n.velocity);
			}

			n.force.clear(); // and clear the forces now that we are done with
			// them
		}

		// get k4 values
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				originalPosition = originalPositions.get(i);
				k3Velocity = k3Velocities.get(i);

				n.position.x = originalPosition.x + k3Velocity.x;
				n.position.y = originalPosition.y + k3Velocity.y;

				originalVelocity = originalVelocities.get(i);
				k3Force = k3Forces.get(i);

				n.velocity.x = originalVelocity.x + k3Force.x;
				n.velocity.y = originalVelocity.y + k3Force.y;

			}
		}

		s.applyForces();

		// save the intermediate forces
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				(k4Forces.get(i)).set(n.force);
				(k4Velocities.get(i)).set(n.velocity);
			}
		}

		// put them all together and what do you get?
		for (int i = nodeSize - 1; i >= 0; i--) {
			n = s.nodes.get(i);
			if (n.isFree()) {
				// update position
				originalPosition = originalPositions.get(i);
				k1Velocity = k1Velocities.get(i);
				k2Velocity = k2Velocities.get(i);
				k3Velocity = k3Velocities.get(i);
				k4Velocity = k4Velocities.get(i);

				n.position.x = originalPosition.x + 1.0f / 6.0f * (k1Velocity.x + 2.0f * k2Velocity.x + 2.0f * k3Velocity.x + k4Velocity.x);
				n.position.y = originalPosition.y + 1.0f / 6.0f * (k1Velocity.y + 2.0f * k2Velocity.y + 2.0f * k3Velocity.y + k4Velocity.y);

				// update velocity
				originalVelocity = originalVelocities.get(i);
				k1Force = k1Forces.get(i);
				k2Force = k2Forces.get(i);
				k3Force = k3Forces.get(i);
				k4Force = k4Forces.get(i);

				n.velocity.x = originalVelocity.x + 1.0f / (6.0f) * (k1Force.x + 2.0f * k2Force.x + 2.0f * k3Force.x + k4Force.x);
				n.velocity.y = originalVelocity.y + 1.0f / (6.0f) * (k1Force.y + 2.0f * k2Force.y + 2.0f * k3Force.y + k4Force.y);
			}
		}
	}
}
