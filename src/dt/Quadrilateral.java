package dt;

import java.awt.Graphics2D;

/**
 * Maintains a quadrilateral given 4 vertices. Assumes that vertices are ordered clockwise
 * 
 * @author Lee Glendenning
 */
public class Quadrilateral {
    
    private Point[] vertices = new Point[4];
    private Point center;

    /**
     * Create quad using array of vertices. Center defined as average of vertices
     * 
     * @param vertices array of Point objects defining vertices
     */
    public Quadrilateral(Point[] vertices) {
        this.vertices = vertices;
        this.center = new Point();
        computeCenter();
        printInfo();
        //minimizeQuad();
    }
    
    /**
     * Create quad using array of vertices and predefined center
     * 
     * @param vertices Array of Point objects defining vertices
     * @param center Arbitrary Point representing the center of the quad
     */
    public Quadrilateral(Point[] vertices, Point center) {
        this.vertices = vertices;
        this.center = new Point();
        this.center = center;
        printInfo();
        //minimizeQuad();
    }
    
    /**
     * Print various details about the defined quadrilateral
     */
    private void printInfo() {
        System.out.print("Original quad: ");
        printVertices(this.vertices);
        System.out.println("Center of quad: (" + center.x + ", " + center.y + ")");
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
        this.center = new Point(x, y);
    }
    
    /**
     * Compute and store the distance of each vertex to the center of the quad
     * 
     * @param verts Point array of vertices of a quad
     * @param p Point to draw quad around
     * @param isReflected If true, reflect quad vertices about x coordinate of p
     * @return Point array with x-y distance of each vertex to the center of the quad
     */
    private Point[] computeVertDistToPoint(Point[] verts, Point p/*, boolean isReflected*/) {
        Point[] distToCenter = new Point[4];
        for (int i = 0; i < 4; i ++) {
            /*if (isReflected) {
                distToCenter[i] = new Point(verts[i].x - p.x, p.y - verts[i].y);
            } else {*/
                distToCenter[i] = new Point(verts[i].x - p.x, verts[i].y - p.y);
            //}
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
        Point[] scaledVertices = Utility.deepCopyPointArray(this.vertices);
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
     * Scale quad to minimum size
     * Scales just below min but scaling back up by a small amount doesn't do anything because of integer coordinates (doubles rounded)
     */
    private void minimizeQuad() {
        System.out.println("Minimizing quad");
        
        double curScale = 1.0;
        Point[] tempVertices = Utility.deepCopyPointArray(this.vertices);
        while (edgeLengthsLargerThanMin(tempVertices, 3.0)) {
            this.vertices = Utility.deepCopyPointArray(tempVertices);
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
            if (Utility.euclideanDistance(vertices[i], vertices[j]) < min){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns pixel coordinates of quad at current scaling for a given point
     * 
     * @param p Reference point
     * @param scale Amount to scale quad by
     * @return Pixel coordinates
     */
    public Point[] getPixelVertsForPoint(Point p, double scale) {
        Point[] distToCenter = computeVertDistToPoint(scaleQuad(scale), this.center);
        Point[] verts = new Point[4];
        for (int i = 0; i < 4; i ++) {
            verts[i] = new Point( (p.x + distToCenter[i].x), (p.y + distToCenter[i].y));
        }
        return verts;
    }
    
    /**
     * 
     * @return Cloned list of vertices defining the quad
     */
    public Point[] getVertices() {
        return Utility.deepCopyPointArray(this.vertices);
    }
    
    /**
     * @param v A vertex of the Quadrilateral
     * @return Next clockwise vertex in the vertex array of the Quadrilateral
     */
    public Point nextVertex(Point v) {
        for (int i = 0; i < this.vertices.length; i ++) {
            if (v.equals(this.vertices[i])) {
                
                return (i != this.vertices.length-1) ? new Point(this.vertices[i+1].x, this.vertices[i+1].y) : new Point(this.vertices[0].x, this.vertices[0].y);
            }
        }
        return null; // v is an invalid vertex
    }
    
    /**
     * @param v A vertex of the Quadrilateral
     * @return Previous clockwise vertex in the vertex array of the Quadrilateral
     */
    public Point prevVertex(Point v) {
        for (int i = 0; i < this.vertices.length; i ++) {
            if (v.equals(this.vertices[i])) {
                return (i != 0) ? new Point(this.vertices[i-1].x, this.vertices[i-1].y) : new Point(this.vertices[this.vertices.length-1].x, this.vertices[this.vertices.length-1].y);
            }
        }
        return null; // v is an invalid vertex
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
     * @param yMax Height of screen. Used to draw from bottom left corner
     */
    public void drawQuad(Graphics2D g2d, Point p, double scale, int yMax) {
        Point[] distToCenter = computeVertDistToPoint(scaleQuad(scale), this.center);
        
        int j = 1;
        for (int i = 0; i < 4; i ++) {
            j = (j==3) ? 0 : i+1; // Wrap around to draw edge from vertices[3] to vertices[0]
            g2d.drawLine(((int)Math.round(p.x + distToCenter[i].x)), yMax - ((int)Math.round(p.y + distToCenter[i].y)), 
                    ((int)Math.round(p.x + distToCenter[j].x)), yMax - ((int)Math.round(p.y + distToCenter[j].y))); // x1, y1, x2, y2
        }
        
    }
    
}
