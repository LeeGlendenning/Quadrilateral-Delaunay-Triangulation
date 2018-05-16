package dt;

import java.awt.Graphics2D;
import java.awt.Point;

/**
 * Maintains a quadrilateral given 4 vertices
 * 
 * @author Lee Glendenning
 */
public class Quadrilateral {
    
    private Point[] vertices = new Point[4];
    private double[] slopeToCenter = new double[4]; // Slope of each point to center
    private Point[] distToCenter = new Point[4];
    //private int[] yIntercepts = new int[4];
    private Point center;

    /**
     * Create quad using array of vertices
     * 
     * @param vertices array of Point objects defining vertices
     */
    public Quadrilateral(Point[] vertices) {
        this.vertices = vertices;
        center = new Point();
        computeCenter();
        computeSlopes();
        computeDistToCenter();
        //computeYIntercepts();
    }
    
    /**
     * Load quad vertices from a file
     * 
     * @param filename name of file to load vertices from
     */
    public Quadrilateral(String filename) {
        
    }
    
    /**
     * Compute and store center of quad
     */
    private void computeCenter() {
        int x = (vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x) / 4;
        int y = (vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y) / 4;
        center = new Point(x, y);
        System.out.println("Center of quad: (" + center.x + ", " + center.y + ")");
    }
    
    /**
     * Compute and store the slope of each line defined by a vertex and the center of the quad
     */
    private void computeSlopes() {
        for (int i = 0; i < 4; i ++) {
            slopeToCenter[i] = (vertices[i].y - center.y) / (vertices[i].x - center.x);
        }
    }
    
    /**
     * Compute and store the distance of each vertex to the center of the quad
     */
    private void computeDistToCenter() {
        for (int i = 0; i < 4; i ++) {
            distToCenter[i] = new Point(vertices[i].x - center.x, vertices[i].y - center.y);
        }
    }
    
    
    
    /**
     * Determine whether Quadrilateral q intersects this quad
     * 
     * @param q reference quad
     * @return true if q intersects the quad, false otherwise
     */
    public boolean isIntersection(Quadrilateral q) {
        return false;
    }
    
    /**
     * Scale quad such that it intersects with another quad
     * 
     * @param q2 Quad to intersect with
     */
    public void scaleQuad(Quadrilateral q2) {
        
    }
    
    /**
     * Draw quad around a point
     * 
     * @param g2d Graphics 2D object used to draw to the screen
     * @param p Point to draw quad around
     * @param scaleFactor Factor to scale pixels by
     */
    public void drawQuad(Graphics2D g2d, Point p, int scaleFactor) {
        System.out.println("---Drawing quad---");
        System.out.println("Center: (" + p.x + ", " + p.y + ")");
        int j = 1;
        for (int i = 0; i < 4; i ++) {
            j = (j==3) ? 0 : i+1; // Wrap around to draw edge from vertices[3] to vertices[0]
            g2d.drawLine((p.x + distToCenter[i].x)*scaleFactor, (p.y + distToCenter[i].y)*scaleFactor, 
                    (p.x + distToCenter[j].x)*scaleFactor, (p.y + distToCenter[j].y)*scaleFactor); // x1, y1, x2, y2
            System.out.print("(" + (p.x + distToCenter[i].x) + ", " + (p.y + distToCenter[i].y) + ") ");
            System.out.println("(" + (p.x + distToCenter[j].x) + ", " + (p.y + distToCenter[j].y) + ")");
        }
        System.out.println();
    }
    
}
