package dt;

import java.util.*;

/**
 * Graph class used to store the Delaunay triangulation
 * @author Lee Glendenning
 * Adapted from Michael Levet, June 09, 2015
 */
public class Graph {
    
    private Vertex[] boundaryTriangle;
    private List<Vertex> vertices;
    private List<Edge> edges;
    private int xmax, ymax;
    private final int BOUNDARY_SIZE = 800;
    
    /**
     * Empty constructor initializes instance variables
     * @param xmax Max x pixel value for JFrame
     * @param ymax Max y pixel value for JFrame
     */
    public Graph(int xmax, int ymax){
        initInstances(xmax, ymax);
    }
    
    /**
     * 
     * @param vertices Vertex List for the graph
     * @param xmax Max x pixel value for JFrame
     * @param ymax Max y pixel value for JFrame
     */
    public Graph(ArrayList<Vertex> vertices, int xmax, int ymax){
        initInstances(xmax, ymax);
        
        for(Vertex v: vertices){
            this.vertices.add(v);
        }
        
    }
    
    /**
     * Initialize all instance variables and the boundary triangle
     */
    private void initInstances(int xmax, int ymax) {
        this.xmax = xmax;
        this.ymax = ymax;
        this.vertices = Collections.synchronizedList(new ArrayList());
        this.edges = Collections.synchronizedList(new ArrayList());
        constructBoundaryTriangle();
    }
    
    /**
     * Define boundary triangle vertices and edges and add them to the graph
     */
    private void constructBoundaryTriangle() {
        this.boundaryTriangle = new Vertex[]{new Vertex(this.xmax/2, this.ymax/2+2*BOUNDARY_SIZE),
            new Vertex(this.xmax/2-3*BOUNDARY_SIZE, this.ymax/2-BOUNDARY_SIZE), new Vertex(this.xmax/2+3*BOUNDARY_SIZE, this.ymax/2-BOUNDARY_SIZE)};
        
        for (Vertex v : this.boundaryTriangle) {
            addVertex(v);
        }
        
        addEdge(this.boundaryTriangle[0], this.boundaryTriangle[1]);
        addEdge(this.boundaryTriangle[1], this.boundaryTriangle[2]);
        addEdge(this.boundaryTriangle[2], this.boundaryTriangle[0]);
    }
    
    /**
     * 
     * @return Deep copy of boundary triangle vertices as List
     */
    public List<Vertex> getBoundaryTriangle() {
        return Arrays.asList(new Vertex[]{this.boundaryTriangle[0].deepCopy(), this.boundaryTriangle[1].deepCopy(), this.boundaryTriangle[2].deepCopy()});
    }
    
    /**
     * Uses slab method
     * @param v Vertex to locate
     * @return Array of vertices containing v
     */
    public Vertex[] locateTriangle(Vertex v) {
        Utility.debugPrintln("Locating triangle:");
        // Copy vertices List
        List<Vertex> slabs = new ArrayList(this.vertices);
        
        // Sort vertices by x value to create slabs
        Collections.sort(slabs, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex v1, Vertex v2) {
                if (v1.x > v2.x || (v1.x == v2.x && v1.y > v2.y)) {
                    return +1;
                } else if (v1.x <= v2.x) {
                     return -1;
                } else {
                    // This shouldn't happen. Vertices cannot be equal
                    return 0;
                }
            }
        });
        //Utility.debugPrintln(slabs.toString());
        
        // Find which horizontal slab v is in
        int i;
        for (i = 0; i < slabs.size()-1; i ++) {
            double tolerance = 0.0001;
            // if v on left slab line or between left and right slab lines it is considered in the slab
            // if we are considering the last slab and v is on the right slab line, v is condsidered in the last slab
            if ((v.x > slabs.get(i).x || Math.abs(v.x - slabs.get(i).x) < tolerance)
                    && (v.x < slabs.get(i+1).x || i+1 == slabs.size()-1)) {
                // v between slab i and slab i+1
                break;
            }
        }
        Utility.debugPrintln("v in slab " + i + " between " + slabs.get(i) + " and " + slabs.get(i+1));

        // Holds line segments represnting the intersection of edges with slab i,i+1, keyed by the edge in the DT
        List<EdgeSegment> slabSegs = findSlabSegments(slabs.get(i).x, slabs.get(i+1).x);

        // Sort y-monotonic slabSegs vertically
        Collections.sort(slabSegs, new Comparator<EdgeSegment>() {
            @Override
            public int compare(EdgeSegment e1, EdgeSegment e2) {
                // Determine left and right vertices of e1 and e2
                Vertex e1Left = new Vertex(), e1Right = new Vertex();
                Utility.setLeftAndRightVertex(e1.valEdge.getVertices()[0], e1.valEdge.getVertices()[1], e1Left, e1Right, 0);
                Vertex e2Left = new Vertex(), e2Right = new Vertex();
                Utility.setLeftAndRightVertex(e2.valEdge.getVertices()[0], e2.valEdge.getVertices()[1], e2Left, e2Right, 0);

                double tolerance = 0.0001;
                if ((e1Left.y > e2Left.y || Math.abs(e1Left.y - e2Left.y) < tolerance) && 
                        (e1Right.y > e2Right.y || Math.abs(e1Right.y - e2Right.y) < tolerance)) {
                    return +1;
                } else if (e1Left.y < e2Left.y || e1Right.y < e2Right.y) {
                     return -1;
                } else {
                    // This shouldn't happen. 2 Edges cannot be equal
                    return 0;
                }
            }
        });

        if (slabSegs.isEmpty() ||
                Utility.isLeftOfSegment(slabSegs.get(0).valEdge.getVertices()[0], slabSegs.get(0).valEdge.getVertices()[1], v, 0.1) == -1 || 
                Utility.isLeftOfSegment(slabSegs.get(slabSegs.size()-1).valEdge.getVertices()[0], slabSegs.get(slabSegs.size()-1).valEdge.getVertices()[1], v, 0.1) == 1) {
            // v outside all regions
            Utility.debugPrintln("v outside regions. ");
            return new Vertex[]{};
        } 

        // Determine which vertical region v is in
        // If v on a line, it is considered to be in the region having the line as its top boundary
        int j;
        for (j = 0; j < slabSegs.size()-1; j ++) {
            /*Utility.debugPrintln("is v left of " + slabSegs.get(j).valEdge.getVertices()[0] + ", " + slabSegs.get(j).valEdge.getVertices()[1] + ": " +
                    (Utility.isLeftOfSegment(slabSegs.get(j).valEdge.getVertices()[0], slabSegs.get(j).valEdge.getVertices()[1], v, 0.1) == 1));
            Utility.debugPrintln("is v right of " + slabSegs.get(j+1).valEdge.getVertices()[0] + ", " + slabSegs.get(j+1).valEdge.getVertices()[1] + ": " +
                    (Utility.isLeftOfSegment(slabSegs.get(j).valEdge.getVertices()[0], slabSegs.get(j).valEdge.getVertices()[1], v, 0.1) == -1));*/
            if (Utility.isLeftOfSegment(slabSegs.get(j).valEdge.getVertices()[0], slabSegs.get(j).valEdge.getVertices()[1], v, 0.1) == 1 &&
                    Utility.isLeftOfSegment(slabSegs.get(j+1).valEdge.getVertices()[0], slabSegs.get(j+1).valEdge.getVertices()[1], v, 0.1) <= 0) {
                // v between slabSeg j and slabSeg j+1
                //Utility.debugPrintln("v left of " + slabSegs.get(j).valEdge + " and right of " + slabSegs.get(j+1).valEdge);
                break;
            }
        }
        Utility.debugPrintln("v in region " + j + " between " + slabSegs.get(j).valEdge + " and " + slabSegs.get(j+1).valEdge);
        
        List<Vertex> triangle = new ArrayList();
        
        Vertex tempV = slabSegs.get(j).keyEdge.getVertices()[0];
        triangle.add(getVertex(tempV.x, tempV.y));
        
        tempV = slabSegs.get(j).keyEdge.getVertices()[1];
        triangle.add(getVertex(tempV.x, tempV.y));
        
        if (!triangle.contains(slabSegs.get(j+1).keyEdge.getVertices()[0])) {
            tempV = slabSegs.get(j+1).keyEdge.getVertices()[0];
            triangle.add(getVertex(tempV.x, tempV.y));
        }
        if (!triangle.contains(slabSegs.get(j+1).keyEdge.getVertices()[1])) {
            tempV = slabSegs.get(j+1).keyEdge.getVertices()[1];
            triangle.add(getVertex(tempV.x, tempV.y));
        }
        return triangle.toArray(new Vertex[triangle.size()]);
    }
    
    /**
     * EdgeSegment class used for slab decomposition.
     * Object is keyed by an edge in the DT, having 
     * value of a line segment of the edge that intersects a slab
     */
    private class EdgeSegment {
        protected Edge keyEdge, valEdge;
        protected EdgeSegment(Edge keyEdge, Edge valEdge) {
            this.keyEdge = keyEdge;
            this.valEdge = valEdge;
        }
    }
    
    /**
     * 
     * @param leftSlabX X coordinate of left slab line
     * @param rightSlabX X coordinate of right slab line
     * @return List of EdgeSegments keyed by edge in the DT, having a value of the edge segment that overlaps the slab
     */
    private List<EdgeSegment> findSlabSegments(double leftSlabX, double rightSlabX) {
        // TODO: consider case where vertical edge has both vertices on same slab edge? Idk if this is a problem or not
        List<EdgeSegment> slabSegs = new ArrayList();
        double tolerance = 0.0001;
        
        // Take copy of edge list
        List<Edge> allEdges = new ArrayList(this.edges);
        // Consider edges of containing triangle as well
        addEdge(this.boundaryTriangle[0], this.boundaryTriangle[1]);
        addEdge(this.boundaryTriangle[1], this.boundaryTriangle[2]);
        addEdge(this.boundaryTriangle[2], this.boundaryTriangle[0]);
        
        // Find the edges that cross slab i,i+1
        for (Edge e : allEdges) {
            Vertex left, right;
            // If a vertex is on the left slab line
            if (Math.abs(e.getVertices()[0].x - leftSlabX) < tolerance) {
                //Utility.debugPrintln("edge vertex on slab line i");
                left = e.getVertices()[0];
            } else if (Math.abs(e.getVertices()[1].x - leftSlabX) < tolerance) {
                //Utility.debugPrintln("edge vertex on slab line i");
                left = e.getVertices()[1];
            } else {
                // Neither edge vertex is on the slab line so find where it intersects
                left = Utility.doLineSegmentsIntersect(new Vertex(leftSlabX, -Utility.RAY_SIZE), new Vertex(leftSlabX, Utility.RAY_SIZE), 
                        e.getVertices()[0], e.getVertices()[1]);
            }

            // If a vertex is on the right slab line
            if (Math.abs(e.getVertices()[0].x - rightSlabX) < tolerance) {
                //Utility.debugPrintln("edge vertex on slab line i+1");
                right = e.getVertices()[0];
            } else if (Math.abs(e.getVertices()[1].x - rightSlabX) < tolerance) {
                //Utility.debugPrintln("edge vertex on slab line i+1");
                right = e.getVertices()[1];
            } else {
                // Neither edge vertex is on the slab line so find where it intersects
                right = Utility.doLineSegmentsIntersect(new Vertex(rightSlabX, -Utility.RAY_SIZE), new Vertex(rightSlabX, Utility.RAY_SIZE), 
                        e.getVertices()[0], e.getVertices()[1]);
            }

            if (left != null && right != null) {
                slabSegs.add(new EdgeSegment(e, new Edge(left, right)));
                //Utility.debugPrintln("Adding slabSeg at " + left + ", " + right);
            }
        }
        return slabSegs;
    }
    
    /**
     * 
     * @param vertices Vertex array of expected size 3
     * @return True if the given vertices all have edges between them in this Graph, false otherwise
     */
    public boolean isTriangleFace(Vertex[] vertices) {
        if (vertices.length == 3 && 
                this.edges.contains(new Edge(vertices[0], vertices[1])) &&
                this.edges.contains(new Edge(vertices[1], vertices[2])) &&
                this.edges.contains(new Edge(vertices[2], vertices[0]))) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param x X coordinate of desired vertex
     * @param y Y coordinate of desired vertex
     * @return Actual vertex in this Graph having the given coordinates
     */
    public Vertex getVertex(double x, double y) {
        for (Vertex v : this.vertices) {
            double tolerance = 0.0001;
            if (Math.abs(v.x - x) < tolerance && Math.abs(v.y - y) < tolerance) {
                return v;
            }
        }
        return null;
    }
    
    /**
     * @param v1 The first Vertex of the Edge
     * @param v2 The second Vertex of the Edge
     * @return true iff the Edge does not already exists in the Graph
     */
    public boolean addEdge(Vertex v1, Vertex v2){
        v1 = getVertex(v1.x, v1.y);
        v2 = getVertex(v2.x, v2.y);
        if (v1 == null || v2 == null) {
            Utility.debugPrintln("Edge not added because at least one vertex does not exist.");
            return false;
        }
        
        // Ensure the Edge is not in the Graph
        Edge e = new Edge(v1, v2);
        if(this.edges.contains(e)){
            //Utility.debugPrintln("Edge already exists. Not added.");
            return false;
        }
        
        this.edges.add(e);
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
       Utility.debugPrintln("Removing edge: " + e);
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
     * @return List of vertices without the boundary ones
     */
    public List<Vertex> getDisplayVertices () {
        List<Vertex> displayVerts = new ArrayList();
        List<Vertex> boundaryVList = Arrays.asList(this.boundaryTriangle);
        for (int i = 0; i < this.vertices.size(); i ++) {
            if (!boundaryVList.contains(this.vertices.get(i))) {
                displayVerts.add(this.vertices.get(i));
            }
        }
        return displayVerts;
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
     * @return List of edges removed from the graph
     */
    public void removeVertex(Vertex v){
        Utility.debugPrintln("Removing vertex " + v);
        this.vertices.remove(v);
        
        for (int i = v.getNeighborCount()-1; i >= 0; i --) {
            Utility.debugPrintln("Removing neighbor ");
            this.removeEdge(v.getNeighbor(i));
        }
    }
    
    /**
     * 
     * @return List<Edge> The Edges of the Graph
     */
    public List<Edge> getEdges(){
        return new ArrayList(this.edges);
    }
    
    /**
     * 
     * @return 
     */
    public List<Edge> getDisplayEdges() {
        List<Edge> displayEdges = new ArrayList();
        List<Vertex> boundaryVList = Arrays.asList(this.boundaryTriangle);
        for (int i = 0; i < this.edges.size(); i ++) {
            if (!boundaryVList.contains(this.edges.get(i).getVertices()[0]) &&
                    !boundaryVList.contains(this.edges.get(i).getVertices()[1])) {
                displayEdges.add(this.edges.get(i));
            }
        }
        return displayEdges;
    }
}
