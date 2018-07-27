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
    
    private HashMap<String, Vertex> vertices;
    private HashMap<Integer, Edge> edges;
    
    public Graph(){
        this.vertices = new HashMap<String, Vertex>();
        this.edges = new HashMap<Integer, Edge>();
    }
    
    /**
     * This constructor accepts an ArrayList<Vertex> and populates
     * this.vertices. If multiple Vertex objects have the same label,
     * then the last Vertex with the given label is used. 
     * 
     * @param vertices The initial Vertices to populate this Graph
     */
    public Graph(ArrayList<Vertex> vertices){
        this.vertices = new HashMap<String, Vertex>();
        this.edges = new HashMap<Integer, Edge>();
        
        for(Vertex v: vertices){
            this.vertices.put(v.getLabel(), v);
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
        if(v1.equals(v2)){
            return false;   
        }
       
        //ensures the Edge is not in the Graph
        Edge e = new Edge(v1, v2);
        if(edges.containsKey(e.hashCode())){
            return false;
        }
       
        //and that the Edge isn't already incident to one of the vertices
        else if(v1.containsNeighbor(e) || v2.containsNeighbor(e)){
            return false;
        }
            
        edges.put(e.hashCode(), e);
        v1.addNeighbor(e);
        v2.addNeighbor(e);
        return true;
    }
    
    /**
     * 
     * @param e The Edge to look up
     * @return true iff this Graph contains the Edge e
     */
    public boolean containsEdge(Edge e){
        if(e.getVertices()[0] == null || e.getVertices()[1] == null){
            return false;
        }
        
        return this.edges.containsKey(e.hashCode());
    }
    
    
    /**
     * This method removes the specified Edge from the Graph,
     * including as each vertex's incidence neighborhood.
     * 
     * @param e The Edge to remove from the Graph
     * @return Edge The Edge removed from the Graph
     */
    public Edge removeEdge(Edge e){
       e.getVertices()[0].removeNeighbor(e);
       e.getVertices()[1].removeNeighbor(e);
       return this.edges.remove(e.hashCode());
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
     * @param label The specified Vertex label
     * @return Vertex The Vertex with the specified label
     */
    public Vertex getVertex(String label){
        return vertices.get(label);
    }
    
    /**
     * This method adds a Vertex to the graph. If a Vertex with the same label
     * as the parameter exists in the Graph, the existing Vertex is overwritten
     * only if overwriteExisting is true. If the existing Vertex is overwritten,
     * the Edges incident to it are all removed from the Graph.
     * 
     * @param vertex
     * @param overwriteExisting
     * @return true iff vertex was added to the Graph
     */
    public boolean addVertex(Vertex vertex, boolean overwriteExisting){
        Vertex current = this.vertices.get(vertex.getLabel());
        if(current != null){
            if(!overwriteExisting){
                return false;
            }
            
            while(current.getNeighborCount() > 0){
                this.removeEdge(current.getNeighbor(0));
            }
        }
        
        
        vertices.put(vertex.getLabel(), vertex);
        return true;
    }
    
    /**
     * 
     * @param label The label of the Vertex to remove
     * @return Vertex The removed Vertex object
     */
    public Vertex removeVertex(String label){
        Vertex v = vertices.remove(label);
        
        while(v.getNeighborCount() > 0){
            this.removeEdge(v.getNeighbor((0)));
        }
        
        return v;
    }
    
    /**
     * 
     * @return Set<String> The unique labels of the Graph's Vertex objects
     */
    public Set<String> vertexKeys(){
        return this.vertices.keySet();
    }
    
    /**
     * 
     * @return Set<Edge> The Edges of this graph
     */
    public Set<Edge> getEdges(){
        return new HashSet<Edge>(this.edges.values());
    }
    
}
