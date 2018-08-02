package dt;

import java.util.*;

/**
 * Graph class used to store the Delaunay triangulation
 * @author Lee Glendenning
 * Adapted from Michael Levet, June 09, 2015
 */
public class Graph {
    
    private final List<Vertex> vertices;
    private final List<Edge> edges;
    
    public Graph(){
        this.vertices = Collections.synchronizedList(new ArrayList());
        this.edges = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * 
     * @param vertices Vertex List for the graph
     */
    public Graph(ArrayList<Vertex> vertices){
        this.vertices = Collections.synchronizedList(new ArrayList());
        this.edges = Collections.synchronizedList(new ArrayList());
        
        for(Vertex v: vertices){
            this.vertices.add(v);
        }
        
    }
    
    
    /**
     * @param v1 The first Vertex of the Edge
     * @param v2 The second Vertex of the Edge
     * @return true iff the Edge does not already exists in the Graph
     */
    public boolean addEdge(Vertex v1, Vertex v2){
        boolean v1Exists = false, v2Exists = false;
        double tolerance = 0.0001;
        for (Vertex v : this.vertices) {
            if (Math.abs(v.x - v1.x) < tolerance && Math.abs(v.y - v1.y) < tolerance) {
                v1 = v;
                v1Exists = true;
            }
            if (Math.abs(v.x - v2.x) < tolerance && Math.abs(v.y - v2.y) < tolerance) {
                v2 = v;
                v2Exists = true;
            }
        }
        
        if (!v1Exists || !v2Exists) {
            Utility.debugPrintln("Edge not added because vertices do not exist.");
            return false;
        }
        
        // Ensure the Edge is not in the Graph
        Edge e = new Edge(v1, v2);
        if(edges.contains(e)){
            Utility.debugPrintln("Edge already exists. Not added.");
            return false;
        }
            
        edges.add(e);
        v1.addNeighbor(e);
        v2.addNeighbor(e);
        return true;
    }
    
    /**
     * Removes the specified Edge from the Graph,
     * including each vertex's incidence neighborhood
     * 
     * @param e The Edge to remove from the Graph
     */
    public void removeEdge(Edge e){
       e.getVertices()[0].removeNeighbor(e);
       e.getVertices()[1].removeNeighbor(e);
       this.edges.remove(e);
    }
    
    /**
     * 
     * @return Deep copy of Vertex List
     */
    public List<Vertex> getVertices() {
        return new ArrayList(this.vertices);
    }
    
    /**
     * 
     * @param vertex Vertex to add
     */
    public void addVertex(Vertex vertex){
        this.vertices.add(vertex);
    }
    
    /**
     * 
     * @param v The vertex to remove
     */
    public void removeVertex(Vertex v){
        this.vertices.remove(v);
        
        while(v.getNeighborCount() > 0){
            this.removeEdge(v.getNeighbor((0)));
        }
        
    }
    /**
     * 
     * @return List<Edge> The Edges of the Graph
     */
    public List<Edge> getEdges(){
        return new ArrayList(this.edges);
    }
    
}
