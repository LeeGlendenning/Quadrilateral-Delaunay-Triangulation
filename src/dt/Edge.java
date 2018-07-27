package dt;

import java.util.Objects;

/**
 * This class models an undirected Edge in the Graph implementation.
 * An Edge contains two vertices and a weight. If no weight is
 * specified, the default is a weight of 1. This is so traversing
 * edges is assumed to be of greater distance or cost than staying
 * at the given vertex.
 * 
 * This class also deviates from the expectations of the Comparable interface
 * in that a return value of 0 does not indicate that this.equals(other). The
 * equals() method only compares the vertices, while the compareTo() method 
 * compares the edge weights. This provides more efficient implementation for
 * checking uniqueness of edges, as well as the fact that two edges of equal weight
 * should be considered equitably in a pathfinding or spanning tree algorithm.
 * 
 * @author Michael Levet
 * @date June 09, 2015
 */
public class Edge {

    private final Vertex v1, v2;
    private final double weight;
    
    /**
     * 
     * @param v1 The first vertex in the Edge
     * @param v2 The second vertex in the Edge
     */
    public Edge(Vertex v1, Vertex v2){
        this.v1 = v1;
        this.v2 = v2;
        this.weight = Utility.euclideanDistance(v1, v2);
    }
    
    /**
     * 
     * @param current
     * @return The neighbor of current along this Edge
     */
    /*public Vertex getNeighbor(Vertex current){
        if(!(current.equals(one) || current.equals(two))){
            return null;
        }
        
        return (current.equals(one)) ? two : one;
    }*/
    
    
    public Vertex[] getVertices() {
        return Utility.deepCopyVertexArray(new Vertex[]{this.v1, this.v2});
    }
    
    /**
     * 
     * @return int The weight of this Edge
     */
    public double getWeight(){
        return this.weight;
    }
    
    
    /**
     * Note that the compareTo() method deviates from 
     * the specifications in the Comparable interface. A 
     * return value of 0 does not indicate that this.equals(other).
     * The equals() method checks the Vertex endpoints, while the 
     * compareTo() is used to compare Edge weights
     * 
     * @param other The Edge to compare against this
     * @return int this.weight - other.weight
     */
    /*@Override
    public int compareTo(Edge other){
        return this.weight - other.weight;
    }*/
    
    /**
     * 
     * @return String A String representation of this Edge
     */
    @Override
    public String toString(){
        return "({" + this.v1 + ", " + this.v2 + "}, " + this.weight + ")";
    }
    
    /**
     * 
     * @param other The Object to compare against this
     * @return ture iff other is an Edge with the same Vertices as this
     */
    @Override
    public boolean equals(Object other){
        if(!(other instanceof Edge)){
            return false;
        }
        
        Edge e = (Edge)other;
        
        return e.v1.equals(this.v1) && e.v2.equals(this.v2);
    }   
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.v1);
        hash = 59 * hash + Objects.hashCode(this.v2);
        return hash;
    }
}
