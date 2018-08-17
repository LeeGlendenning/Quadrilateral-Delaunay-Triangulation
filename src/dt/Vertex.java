package dt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Vertex data structure used by Graph and Quadrilateral classes
 * 
 * @author Lee Glendenning
 * Adapted from Michael Levet, June 09, 2015
 */
public class Vertex {

    private final List<Edge> neighborhood;
    public double x, y;
    private final Color colour;
    
    /**
     * 
     * @param x X coordinate of this Vertex
     * @param y Y coordinate of this Vertex
     */
    public Vertex(double x, double y){
        this.x = x;
        this.y = y;
        this.colour = Color.black;
        this.neighborhood = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * Empty constructor initializes the vertex at (0,0)
     */
    public Vertex() {
        this.x = 0;
        this.y = 0;
        this.colour = Color.black;
        this.neighborhood = new ArrayList();
    }
    
    /**
     * 
     * @return Colour vertex should be drawn using
     */
    public Color getColour() {
        return this.colour;
    }
    
    
    /**
     * This method adds an Edge to the incidence neighborhood of this graph iff
     * the edge does not exist
     * 
     * @param edge The edge to add
     */
    public void addNeighbor(Edge edge){
        if(this.neighborhood.contains(edge)){
            return;
        }
        this.neighborhood.add(edge);
    }
    
    /**
     * 
     * @param other The edge for which to search
     * @return True iff other is contained in this.neighborhood
     */
    public boolean containsNeighbor(Edge other){
        return this.neighborhood.contains(other);
    }
    
    /**
     * 
     * @param index The index of the Edge to retrieve
     * @return Edge The Edge at the specified index in this.neighborhood
     */
    public Edge getNeighbor(int index){
        return this.neighborhood.get(index);
    }
    
    /**
     * 
     * @param e The Edge to remove from this.neighborhood
     */
    public void removeNeighbor(Edge e){
        this.neighborhood.remove(e);
    }
    
    /**
     * 
     * @return The number of neighbors of this Vertex
     */
    public int getNeighborCount(){
        return this.neighborhood.size();
    }
    
    /**
     * 
     * @return A String representation of this Vertex
     */
    @Override
    public String toString(){
        return "(" + this.x + ", " + this.y + ")";
    }
    
    /**
     * 
     * @param other The object to compare
     * @return True iff other instanceof Vertex and the two Vertex objects have the same x and y values within the tolerance
     */
    @Override
    public boolean equals(Object other){
        double tolerance = 0.01;
        return other.getClass() == this.getClass() && Math.abs(this.x - ((Vertex) other).x) < tolerance && Math.abs(this.y - ((Vertex) other).y) < tolerance;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        //Utility.debugPrintln("inside hashCode. returning " + hash);
        return hash;
    }
    
    /**
     * 
     * @return Deep copy of this Vertex
     */
    public Vertex deepCopy() {
        return new Vertex(this.x, this.y);
    }
    
    /**
     * 
     * @return ArrayList of neighbouring Vertex objects
     */
    public ArrayList<Vertex> getNeighbours() {
        ArrayList<Vertex> neighbours = new ArrayList();
        for (Edge e : this.neighborhood) {
            neighbours.add(e.getNeighbor(this));
        }
        return neighbours;
    }
    
}
