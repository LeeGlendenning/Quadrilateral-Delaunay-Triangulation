package dt;

import java.awt.Point;

/**
 * Maintains a quadrilateral given 4 vertices
 * 
 * @author Lee Glendenning
 */
public class Quadrilateral {
    
    private Point[] vertices = new Point[4];
    private Point center;

    /**
     * Create quad using array of vertices
     * 
     * @param vertices array of Point objects defining vertices
     */
    public Quadrilateral(Point[] vertices) {
        this.vertices = vertices;
        computeCenter();
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
     * Scale quad such that ?
     */
    public void scaleQuad() {
        
    }
    
}
