package dt;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Creates a node consisting of x-y coordinates and neighbours which will define Delaunay edges in the DT
 * 
 * @author Lee Glendenning
 */
public class DelaunayNode {
    
    Point coordinates;
    ArrayList<DelaunayNode> neighbours; // Nodes for which a Delaunay edge will be drawn
    
    /**
     * Create a DelaunayNode having x-y coordinates and an empty list of neighbours
     * 
     * @param coords x-y coordinate of the node
     */
    public DelaunayNode(Point coords) {
        this.coordinates = coords;
        neighbours = new ArrayList();
    }
    
    /**
     * Add neighbour node to this DelaunayNode
     * 
     * @param neighbour Node to be added to list of neighbours
     */
    public void addNeighbour(DelaunayNode neighbour) {
        neighbours.add(neighbour);
    }
    
}
