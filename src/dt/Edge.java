package dt;

import java.util.Objects;

/**
 * Edge data structure used by the Graph class
 * 
 * @author Lee Glendenning
 * Adapted from Michael Levet, June 09, 2015
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
     * @param current An end vertex of the edge
     * @return The neighbor of current along this Edge
     */
    public Vertex getNeighbor(Vertex current){
        if(!(current.equals(v1) || current.equals(v2))){
            return null;
        }
        
        return (current.equals(v1)) ? v2 : v1;
    }
    
    /**
     * 
     * @return Deep copy of the end vertices of this edge
     */
    public Vertex[] getVertices() {
        return Utility.deepCopyVertexArray(new Vertex[]{this.v1, this.v2});
    }
    
    /**
     * 
     * @return The length of this Edge
     */
    public double getWeight(){
        return this.weight;
    }
    
    /**
     * 
     * @return A String representation of this Edge
     */
    @Override
    public String toString(){
        return "({" + this.v1 + ", " + this.v2 + "}, " + this.weight + ")";
    }
    
    /**
     * 
     * @param other The Object to compare against this
     * @return True iff other is an Edge with the same Vertices as this
     */
    @Override
    public boolean equals(Object other){
        if(!(other instanceof Edge)){
            return false;
        }
        
        Edge e = (Edge)other;
        
        return e.v1.equals(this.v1) && e.v2.equals(this.v2);
    }
    
}
