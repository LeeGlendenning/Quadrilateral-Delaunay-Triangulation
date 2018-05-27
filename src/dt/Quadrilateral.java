package dt;

import java.awt.Graphics2D;

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
        this.center = new Point();
        computeCenter();
        minimizeQuad();
    }
    
    /**
     * Print Point array to console
     * 
     * @param verts Point array of vertices
     */
    public void printVertices(Point[] verts) {
        for (int i = 0; i < 4; i ++) {
            System.out.print("(" + verts[i].x + ", " + verts[i].y + ") ");
        }
        System.out.println();
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
        double x = (vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x) / 4;
        double y = (vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y) / 4;
        center = new Point(x, y);
        //System.out.println("Center of quad: (" + center.x + ", " + center.y + ")");
    }
    
    /**
     * Compute and store the distance of each vertex to the center of the quad
     * 
     * @param verts Point array of vertices of a quad
     * @return Point array with x-y distance of each vertex to the center of the quad
     */
    private Point[] computeDistToCenter(Point[] verts) {
        Point[] distToCenter = new Point[4];
        for (int i = 0; i < 4; i ++) {
            distToCenter[i] = new Point(verts[i].x - this.center.x, verts[i].y - this.center.y);
        }
        return distToCenter;
    }
    
    /**
     * Scale quad by a scaling factor
     * 
     * @param scaleFactor Factor to scale vertices by
     * @return Array of scaled vertices
     */
    public Point[] scaleQuad(double scaleFactor) {
        Point[] scaledVertices = deepCopyPointSet(this.vertices);
        for (int i = 0; i < 4; i ++) {
            // Translate center of quad to origin
            scaledVertices[i].x -= this.center.x;
            scaledVertices[i].y -= this.center.y;
            
            // Multiply x and y coords by scale factor
            scaledVertices[i].x *= scaleFactor;
            scaledVertices[i].y *= scaleFactor;
            
            // Translate quad back to its location
            scaledVertices[i].x += this.center.x;
            scaledVertices[i].y += this.center.y;
        }
        
        //printVertices(scaledVertices);
        
        return scaledVertices;
    }
    
    /**
     * Create deep copy of a point array
     * 
     * @param ptSet Point array to clone
     * @return Deep copy of ptSet
     */
    private Point[] deepCopyPointSet(Point[] ptSet) {
        Point[] newSet = new Point[4];
        for (int i = 0; i < 4; i ++) {
            newSet[i] = new Point();
            newSet[i].x = ptSet[i].x;
            newSet[i].y = ptSet[i].y;
        }
        return newSet;
    }
    
    /**
     * Scale quad to minimum size
     * Scales just below min but scaling back up by a small amount doesn't do anything because of integer coordinates (doubles rounded)
     */
    private void minimizeQuad() {
        System.out.println("Minimizing quad");
        
        double curScale = 1.0;
        Point[] tempVertices = deepCopyPointSet(this.vertices);
        while (edgeLengthsLargerThanMin(tempVertices, 3.0)) {
            this.vertices = deepCopyPointSet(tempVertices);
            curScale -= 0.1;
            tempVertices = scaleQuad(curScale);
        }
        System.out.println();
    }
    
    /**
     * Check that area of quad is less than 1
     * 
     * @param vertices Set of vertices defining a quad
     * @param min Minimum allowed length of an edge in the quad
     * @return True if area is larger than 1, false otherwise
     */
    private boolean edgeLengthsLargerThanMin(Point[] vertices, double min) {
        int j = 1;
        for (int i = 0; i < 4; i ++) 
        {
            j = (j==3) ? 0 : i+1;
            //System.out.println("edge length: " + euclideanDistance(vertices[i], vertices[j]));
            if (euclideanDistance(vertices[i], vertices[j]) < min){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compute the Euclidean distance between two points
     * 
     * @param p1 First point
     * @param p2 Second point
     * @return Euclidean distance between p1 and p2
     */
    private double euclideanDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    
    /**
     * Returns pixel coordinates of quad at current scaling for a given point
     * 
     * @param p Reference point
     * @param scale Amount to scale quad by
     * @param pixelFactor Factor to scale pixels by
     * @return Pixel coordinates
     */
    public Point[] getPixelVertsForPoint(Point p, double scale, int pixelFactor) {
        Point[] distToCenter = computeDistToCenter(scaleQuad(scale));
        Point[] verts = new Point[4];
        for (int i = 0; i < 4; i ++) {
            verts[i] = new Point( (p.x + distToCenter[i].x)*pixelFactor, (p.y + distToCenter[i].y)*pixelFactor);
        }
        return verts;
    }
    
    /**
     * 
     * @return Cloned list of vertices defining the quad
     */
    public Point[] getVertices() {
        return this.vertices.clone();
    }
    
    /**
     * 
     * @return Center point of quad
     */
    public Point getCenter() {
        return new Point(this.center.x, this.center.y);
    }
    
    /**
     * Draw quad around a point
     * 
     * @param g2d Graphics 2D object used to draw to the screen
     * @param p Point to draw quad around
     * @param scale Amount to scale quad by
     * @param pixelFactor Factor to scale pixels by
     * @param yMax Height of screen. Used to draw from bottom left corner
     */
    public void drawQuad(Graphics2D g2d, Point p, double scale, int pixelFactor, int yMax) {
        Point[] distToCenter = computeDistToCenter(scaleQuad(scale));
        
        int j = 1;
        for (int i = 0; i < 4; i ++) {
            j = (j==3) ? 0 : i+1; // Wrap around to draw edge from vertices[3] to vertices[0]
            g2d.drawLine(((int)Math.round(p.x + distToCenter[i].x))*pixelFactor, yMax - ((int)Math.round(p.y + distToCenter[i].y))*pixelFactor, 
                    ((int)Math.round(p.x + distToCenter[j].x))*pixelFactor, yMax - ((int)Math.round(p.y + distToCenter[j].y))*pixelFactor); // x1, y1, x2, y2
        }
        
    }
    
}
