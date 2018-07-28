package dt;

import java.awt.Color;
import java.util.ArrayList;

/**
 * This class models a vertex in a graph. For ease of 
 * the reader, a label for this vertex is required. 
 * Note that the Graph object only accepts one Vertex per label,
 * so uniqueness of labels is important. This vertex's neighborhood
 * is described by the Edges incident to it. 
 * 
 * @author Michael Levet
 * @date June 09, 2015
 */
public class Vertex {

    private final ArrayList<Edge> neighborhood;
    private String label;
    public double x, y;
    private final Color colour;
    
    /**
     * 
     * @param x
     * @param y
     */
    public Vertex(double x, double y){
        this.x = x;
        this.y = y;
        this.colour = Color.black;
        this.neighborhood = new ArrayList();
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
     * the edge is not already present. 
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
     * @return true iff other is contained in this.neighborhood
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
     * @param index The index of the edge to remove from this.neighborhood
     * @return Edge The removed Edge
     */
    Edge removeNeighbor(int index){
        return this.neighborhood.remove(index);
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
     * @return int The number of neighbors of this Vertex
     */
    public int getNeighborCount(){
        return this.neighborhood.size();
    }
    
    
    /**
     * 
     * @return String The label of this Vertex
     */
    public String getLabel(){
        return this.label;
    }
    
    /**
     * 
     * @param label Label for the vertex
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    
    /**
     * 
     * @return String A String representation of this Vertex
     */
    @Override
    public String toString(){
        return "(" + this.x + ", " + this.y + ")";
    }
    
    /**
     * 
     * @param other The object to compare
     * @return true iff other instanceof Vertex and the two Vertex objects have the same x and y values within the tolerance
     */
    @Override
    public boolean equals(Object other){
        double tolerance = 0.01;
        if (other.getClass() == this.getClass() && Math.abs(this.x - ((Vertex) other).x) < tolerance && Math.abs(this.y - ((Vertex) other).y) < tolerance) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @return The hash code of this Vertex's x and y coordinates
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
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
     * @return ArrayList<Edge> A copy of this.neighborhood. Modifying the returned
     * ArrayList will not affect the neighborhood of this Vertex
     */
    /*public ArrayList<Edge> getNeighbors(){
        return new ArrayList<Edge>(this.neighborhood);
    }*/
    
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
