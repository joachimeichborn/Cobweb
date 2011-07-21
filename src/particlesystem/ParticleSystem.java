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
import java.util.Iterator;

/**
 * Makes up the particle system that contains all edges, nodes and repulsions
 * 
 * @author Joachim von Eichborn
 * @author http://bioinformatics.charite.de/cobweb
 */
public class ParticleSystem {
	/**
	 * Defines the default drag
	 */
	final static float DEFAULT_DRAG = 0.001f;

	/**
	 * The list of all nodes in the particle system
	 */
	ArrayList<Node> nodes;

	/**
	 * The list of all edges in the particle system
	 */
	ArrayList<Edge> edges;

	/**
	 * The list of all nodes that are currently highlighted
	 */
	ArrayList<Node> selectedNodes;

	/**
	 * The list of all repulsions in the particle system
	 */
	ArrayList<Repulsion> repulsions;

	/**
	 * The integrator used to compute the movements of the particles
	 */
	Integrator integrator;

	/**
	 * The drag
	 */
	float drag;

	/**
	 * Whether the edges are directed or not
	 */
	boolean directed;

	/**
	 * Initializes the particle system with the default drag value
	 */
	public ParticleSystem () {
		this(DEFAULT_DRAG);
	}

	/**
	 * Initializes the particle system with a specific drag value
	 * 
	 * @param drag
	 *            The drag value
	 */
	public ParticleSystem (float drag) {
		this.integrator = new Integrator(this);
		this.nodes = new ArrayList<Node>();
		this.selectedNodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		this.repulsions = new ArrayList<Repulsion>();
		this.drag = drag;
		this.directed = false;
	}

	/**
	 * Apply the forces that act on the elements in the particle system
	 */
	void applyForces () {

		if (isDirected()) {
			for (int i = 0; i < nodes.size(); ++i) {
				Node n = (Node) nodes.get(i);
				n.force.add(n.gravity);
			}
		}

		for (int i = 0; i < nodes.size(); ++i) {
			Node n = nodes.get(i);
			n.force.add(n.velocity.getX() * -drag, n.velocity.getY() * -drag);
		}

		for (int i = 0; i < edges.size(); i++) {
			try {
				Edge e = edges.get(i);
				e.apply();
			} catch (NullPointerException e) {
				break;
			}
		}

		for (int i = 0; i < repulsions.size(); i++) {
			try {
				Repulsion r = repulsions.get(i);
				r.apply();
			} catch (NullPointerException e) {
				break;
			}
		}
	}

	/**
	 * Delete the forces that act on all nodes
	 */
	void clearForces () {
		Iterator<Node> i = nodes.iterator();
		while (i.hasNext())
			i.next().force.clear();
	}

	/**
	 * Return true if an edge with the same source- and target-nodes is present
	 * in the particle system
	 * 
	 * @param e
	 *            the edge
	 * @return true if an edge with the given source- and target-nodes is
	 *         present in the particle system, otherwise false
	 */
	public boolean containsEdge (Edge e) {
		for (int i = 0; i < edges.size(); ++i)
			if (getEdge(i).equals(e, isDirected()))
				return true;

		return false;
	}

	/**
	 * Return true if a node with the given id is present in the particle system
	 * 
	 * @param id
	 *            The id of the node
	 * @return true if a node with the given id is present in the particle
	 *         system, otherwise false
	 */
	public boolean containsNode (String id) {
		for (int i = 0; i < nodes.size(); ++i)
			if (getNode(i).getId().equals(id))
				return true;

		return false;
	}

	/**
	 * Decreases the strength of all edges of the given node by one step
	 * 
	 * @param n
	 *            the nodes whose edges are weakend
	 * @return The number of times that edge-strengths were decreased
	 */
	public int decreaseEdgeStrength (Node n) {
		int c = 0;
		for (int i = 0; i < edges.size(); ++i)
			if ((getEdge(i).getSource() == n) || (getEdge(i).getTarget() == n)) {
				getEdge(i).decreaseEdgeStrength();
				c++;
			}

		return c;
	}

	/**
	 * Deselect all Nodes
	 */
	public void deselectAllNodes () {
		for (int i = 0; i < numberOfSelectedNodes(); ++i)
			getSelectedNode(i).dehighlight();

		selectedNodes.clear();
	}

	/**
	 * Deselect the given node
	 * 
	 * @param n
	 *            The node that is deselected
	 */
	public void deselectNode (Node n) {
		n.dehighlight();
		selectedNodes.remove(n);
	}

	/**
	 * Return the i-th edge in the particle system
	 * 
	 * @param i
	 *            The number of the edge that is to be returned
	 * @return The i-th edge in the particle system
	 */
	public Edge getEdge (int i) {
		return edges.get(i);
	}

	/**
	 * Return the i-th node in the particle system
	 * 
	 * @param i
	 *            The number of the node that is to be returned
	 * @return The i-th node in the particle system
	 */
	public Node getNode (int i) {
		return nodes.get(i);
	}

	/**
	 * Return the node with the given id
	 * 
	 * @param id
	 *            The id of the node that is to be returned
	 * @return The node with the given id if present, otherwise null
	 */
	public Node getNodeById (String id) {
		for (int i = 0; i < numberOfNodes(); i++) {
			if (getNode(i).getId().equals(id))
				return getNode(i);
		}

		return null;
	}

	/**
	 * Return the i-th repulsion in the particle system
	 * 
	 * @param i
	 *            The number of the repulsion that is to be returned
	 * @return The i-th repulsion in the particle system
	 */
	public Repulsion getRepulsion (int i) {
		return repulsions.get(i);
	}

	/**
	 * Return the i-th selected node in the particle system
	 * 
	 * @param i
	 *            The number of the selected node that is to be returned
	 * @return The i-th selected node in the particle system
	 */
	public Node getSelectedNode (int i) {
		return selectedNodes.get(i);
	}

	/**
	 * Increases the strength of all edges of the given node by one step
	 * 
	 * @param n
	 *            the nodes whose edges are strengthend
	 */
	public void increaseEdgeStrength (Node n) {
		for (int i = 0; i < edges.size(); ++i)
			if ((getEdge(i).getSource() == n) || (getEdge(i).getTarget() == n))
				getEdge(i).increaseEdgeStrength();
	}

	/**
	 * Return true if the edges in the particle system are directed
	 * 
	 * @return true if the edges in the particle system are directed, otherwise
	 *         false
	 */
	public boolean isDirected () {
		return this.directed;
	}

	/**
	 * Create a new edge
	 * 
	 * @param source
	 *            The node at one end of the edge
	 * @param target
	 *            The node at the other end of the edge
	 * @param weight
	 *            The eight of the edge
	 * @param annotation
	 *            A label for the edge
	 * @param color
	 *            The color of the edge
	 * @param strength
	 *            The strength of the edge
	 * @param damping
	 *            The damping that is applied to the edge's strength
	 * @param restLength
	 *            The edge's rest length
	 * @return The new edge if it was not already present in the particle
	 *         system, otherwise null
	 */
	public Edge makeEdge (Node source, Node target, float weight, String annotation, String shape, String color, float strength, float damping, float restLength) {
		Edge e = new Edge(source, target, weight, annotation, shape, isDirected(), color, strength, damping, restLength);

		if (!containsEdge(e)) {
			source.decreaseGravity();
			target.increaseGravity();
			int edgeCount = decreaseEdgeStrength(source);

			if (!source.equals(target)) // for self-edges the edge is not
				// counted twice
				edgeCount += decreaseEdgeStrength(target);

			for (int i = 0; i < edgeCount; ++i)
				e.decreaseEdgeStrength();

			edges.add(e);
			return e;
		} else
			return null;
	}

	/**
	 * Create a new Node
	 * 
	 * @param id
	 *            The node's Id
	 * @param name
	 *            The node's name
	 * @param description
	 *            A longer description of the node
	 * @param picture
	 *            The picture that is used to represent the node
	 * @param picturePath
	 *            The path to the picture-directory on the server
	 * @param color
	 *            The node's color
	 * @return The new node if it was not already present in the particle
	 *         system, otherwise null
	 */
	public Node makeNode (String id, String name, String description, String picture, String picturePath, String shape, String color) {
		Node n = new Node(id, name, description, picture, picturePath, shape, color);
		if (!containsNode(n.getId())) {
			nodes.add(n);
			return n;
		} else
			return null;
	}

	/**
	 * Create a new repulsion
	 * 
	 * @param a
	 *            The node at one end of the repulsion
	 * @param b
	 *            The node at the other end of the repulsion
	 * @param strength
	 *            The strength of the repulsion
	 * @param minDistance
	 *            The minimal distance that is assumed between the nodes
	 * @return The new repulsion
	 */
	public Repulsion makeRepulsion (Node a, Node b, float strength, float minDistance) {
		Repulsion r = new Repulsion(a, b, strength, minDistance);

		if (!repulsions.contains(r))
			repulsions.add(r);

		return r;
	}

	/**
	 * Return the number of edges in the particle system
	 * 
	 * @return The number of edges in the particle system
	 */
	public int numberOfEdges () {
		return edges.size();
	}

	/**
	 * Return the number of nodes in the particle system
	 * 
	 * @return The number of nodes in the particle system
	 */
	public int numberOfNodes () {
		return nodes.size();
	}

	/**
	 * Return the number of repulsions in the particle system
	 * 
	 * @return The number of repulsions in the particle system
	 */
	public int numberOfRepulsions () {
		return repulsions.size();
	}

	/**
	 * Return the number of selected nodes in the particle system
	 * 
	 * @return The number of selected nodes in the particle system
	 */
	public int numberOfSelectedNodes () {
		return selectedNodes.size();
	}

	/**
	 * Remove the given edge from the particle system
	 * 
	 * @param e
	 *            The edge that is to be removed
	 */
	public void removeEdge (Edge e) {
		e.getSource().increaseGravity();
		e.getTarget().decreaseGravity();
		increaseEdgeStrength(e.getSource());
		increaseEdgeStrength(e.getTarget());
		edges.remove(e);
	}

	/**
	 * Remove the i-th edge from the particle system
	 * 
	 * @param i
	 *            The number of the edge that is to be removed from the particle
	 *            system
	 */
	public void removeEdge (int i) {
		Edge e = edges.get(i);
		removeEdge(e);
	}

	/**
	 * Remove the i-th node with all it's edges and repulsions from the particle
	 * system
	 * 
	 * @param i
	 *            The number of the node that is to be removed from the particle
	 *            system
	 */
	public void removeNode (int i) {
		removeNode(getNode(i));
	}

	/**
	 * Remove the given node with all it's edges and repulsions from the
	 * particle system
	 * 
	 * @param n
	 *            The node that is to be removed
	 */
	public void removeNode (Node n) {
		ArrayList<Repulsion> removeRepulsions = new ArrayList<Repulsion>();

		for (int i = 0; i < numberOfRepulsions(); ++i) {
			Repulsion r = getRepulsion(i);
			if (r.getOneEnd().equals(n) || r.getTheOtherEnd().equals(n))
				removeRepulsions.add(r);
		}

		for (Iterator<Repulsion> repulsionsIt = removeRepulsions.iterator(); repulsionsIt.hasNext();) {
			removeRepulsion(repulsionsIt.next());
		}

		ArrayList<Edge> removeEdges = new ArrayList<Edge>();

		for (int i = 0; i < numberOfEdges(); ++i) {
			Edge e = getEdge(i);
			if (e.getSource().equals(n) || e.getTarget().equals(n))
				removeEdges.add(e);
		}

		for (Iterator<Edge> edgesIt = removeEdges.iterator(); edgesIt.hasNext();)
			removeEdge(edgesIt.next());

		if (selectedNodes.contains(n))
			selectedNodes.remove(n);

		nodes.remove(n);
	}

	/**
	 * Remove the i-th repulsion from the particle system
	 * 
	 * @param i
	 *            The number of the repulsion that is to be removed
	 */
	public void removeRepulsion (int i) {
		repulsions.remove(i);
	}

	/**
	 * Remove the given repulsion from the particle system
	 * 
	 * @param r
	 *            The repulsion that is to be removed
	 */
	public void removeRepulsion (Repulsion r) {
		repulsions.remove(r);
	}

	/**
	 * Select the given node
	 * 
	 * @param n
	 *            The node that is selected
	 */
	public void selectNode (Node n) {
		n.highlight();
		selectedNodes.add(n);
	}

	/**
	 * Set whether the edges in the graph are directed
	 * 
	 * @param d
	 *            true if the edges should be directed
	 */
	public void setDirected (boolean d) {
		directed = d;
	}

	/**
	 * Perform one step of the iterator
	 */
	public void tick () {
		try {
			integrator.step();
		} catch (IndexOutOfBoundsException e) {
			System.err.println(e.getMessage());
		}
	}

}