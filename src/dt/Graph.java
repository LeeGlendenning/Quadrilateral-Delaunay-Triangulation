package dt;

import java.util.*;

/**
 * This class models a simple, undirected graph using an 
 * incidence list representation. Vertices are identified 
 * uniquely by their labels, and only unique vertices are allowed.
 * At most one Edge per vertex pair is allowed in this Graph.
 * 
 * Note that the Graph is designed to manage the Edges. You
 * should not attempt to manually add Edges yourself.
 * 
 * @author Michael Levet
 * @date June 09, 2015
 */
public class Graph {
    
    private List<Vertex> vertices;
    private List<Edge> edges;
    
    public Graph(){
        this.vertices = Collections.synchronizedList(new ArrayList());
        this.edges = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * This constructor accepts an ArrayList<Vertex> and populates
     * this.vertices. If multiple Vertex objects have the same label,
     * then the last Vertex with the given label is used. 
     * 
     * @param vertices The initial Vertices to populate this Graph
     */
    public Graph(ArrayList<Vertex> vertices){
        this.vertices = Collections.synchronizedList(new ArrayList());
        this.edges = Collections.synchronizedList(new ArrayList());
        
        for(Vertex v: vertices){
            this.vertices.add(v);
        }
        
    }
    
    
    /**
     * Accepts two vertices and a weight, and adds the edge 
     * ({one, two}, weight) iff no Edge relating one and two 
     * exists in the Graph.
     * 
     * @param v1 The first Vertex of the Edge
     * @param v2 The second Vertex of the Edge
     * @return true iff no Edge already exists in the Graph
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
        
        //ensures the Edge is not in the Graph
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
     * 
     * @param e The Edge to look up
     * @return true iff this Graph contains the Edge e
     */
    /*public boolean containsEdge(Edge e){
        if(e.getVertices()[0] == null || e.getVertices()[1] == null){
            return false;
        }
        
        return this.edges.containsKey(e.hashCode());
    }*/
    
    
    /**
     * This method removes the specified Edge from the Graph,
     * including as each vertex's incidence neighborhood.
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
     * @param vertex The Vertex to look up
     * @return true iff this Graph contains vertex
     */
    /*public boolean containsVertex(Vertex vertex){
        for (Vertex v : this.vertices) {
            if (v.equals(vertex)) {
                return true;
            }
        }
        return false;
    }*/
    
    /**
     * 
     * @return Vertex The Vertex with the specified label
     */
    /*public Vertex getVertex(String label){
        return vertices.get(label);
    }*/
    
    public List<Vertex> getVertices() {
        return new ArrayList(this.vertices);
    }
    
    /**
     * This method adds a Vertex to the graph. If a Vertex with the same label
     * as the parameter exists in the Graph, the existing Vertex is overwritten
     * only if overwriteExisting is true. If the existing Vertex is overwritten,
     * the Edges incident to it are all removed from the Graph.
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
    
    /*public Vertex getVertex(double x, double y) {
        double tolerance = 0.0001;
        for (Vertex v : this.vertices) {
            if (Math.abs(v.x - x) < tolerance && Math.abs(v.y - y) < tolerance) {
                return v;
            }
        }
    }*/
    
    /**
     * 
     * @return Set<String> The unique labels of the Graph's Vertex objects
     */
    /*public Set<String> vertexKeys(){
        return this.vertices.keySet();
    }*/
    
    /**
     * 
     * @return List<Edge> The Edges of this graph
     */
    public List<Edge> getEdges(){
        return new ArrayList(this.edges);
    }
    
}
