package dt;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Constructs a DT of a point set and a quadrilateral by constructing a VoronoiDiagram then computing its dual
 * 
 * @author Lee Glendenning
 */
public class DelaunayTriangulation {
    
    private final ArrayList<Point> points;
    private ArrayList<DelaunayNode> nodes;
    
    /**
     * Create Delaunay Triangulation from dual of Voronoi diagram
     * 
     * @param q Quadrilateral
     * @param p Point set
     */
    public DelaunayTriangulation(Quadrilateral q, ArrayList<Point> p) {
        this.points = p;
        
        System.out.println("Creating DT");
        constructDT(new VoronoiDiagram(q, points));
    }
    
    /**
     * Construct DT by computing the dual of the Voronoi diagram
     * 
     * @param v Voronoi diagram to construct DT from
     */
    private void constructDT(VoronoiDiagram v) {
        initNodes();
        computeDual(v);
    }
    
    /**
     * Create a DelaunayNode object for each point in the point set
     */
    private void initNodes() {
        
    }
    
    /**
     * Define all DelaunayNode neighbours by computing the dual of the VoronoiDiagram
     * 
     * @param v Voronoi diagram to compute dual of
     */
    private void computeDual(VoronoiDiagram v) {
        
    }
    
    /**
     * Compute the shortest path in the DT between two points
     * 
     * @param p1 A point in the DT
     * @param p2 A point in the DT
     * @return List of DeulaunayNode objects representing the shortest path
     */
    public ArrayList<DelaunayNode> computeShortestPath(Point p1, Point p2) {
        ArrayList<DelaunayNode> shortestPath = new ArrayList();
        
        return shortestPath;
    }
    
    /**
     * Compute the length of the path
     * 
     * @param path List of DelaunayNodes defining a path in the DT
     * @return Length of the path
     */
    public double computeLengthOfPath(ArrayList<DelaunayNode> path) {
        double length = 0.0;
        
        return length;
    }
    
    /**
     * Compute the stretch factor of the DT
     * 
     * @return Stretch factor of the DT
     */
    public double computeStretchFactorOfDT() {
        double stretchFactor = 0.0;
        
        return stretchFactor;
    }
    
    /**
     * Compute the stretch factor of the path
     * 
     * @param path List of DelaunayNodes defining a path in the DT
     * @return Stretch factor of the path
     */
    public double computeStretchFactorOfPath(ArrayList<DelaunayNode> path) {
        double stretchFactor = 0.0;
        
        return stretchFactor;
    }
    
    /**
     * Draw the Delaunay Triangulation to the screen
     */
    public void drawDT() {
        System.out.println("Drawing DT");
    }
    
}
