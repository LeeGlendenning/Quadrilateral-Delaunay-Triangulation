package dt;

import java.awt.Graphics2D;

/**
 * Maintains a quadrilateral given 4 vertices. Assumes that vertices are ordered clockwise
 * 
 * @author Lee Glendenning
 */
public class Quadrilateral {
    
    private Vertex[] vertices = new Vertex[4];
    private Vertex center;

    /**
     * Create quad using array of vertices. Center defined as average of vertices
     * 
     * @param vertices array of Vertex objects defining vertices
     */
    public Quadrilateral(Vertex[] vertices) {
        this.vertices = vertices;
        this.center = new Vertex(0,0);
        computeCenter();
        printInfo();
        //minimizeQuad();
    }
    
    /**
     * Create quad using array of vertices and predefined center
     * 
     * @param vertices Array of Vertex objects defining vertices
     * @param center Arbitrary Vertex representing the center of the quad
     */
    public Quadrilateral(Vertex[] vertices, Vertex center) {
        this.vertices = vertices;
        this.center = new Vertex(0,0);
        this.center = center;
        printInfo();
        //minimizeQuad();
    }
    
    /**
     * Print various details about the defined quadrilateral
     */
    private void printInfo() {
        Utility.debugPrint("Original quad: ");
        printVertices(this.vertices);
        Utility.debugPrintln("Center of quad: (" + center.x + ", " + center.y + ")");
    }
    
    /**
     * Print Vertex array to console
     * 
     * @param verts Vertex array of vertices
     */
    public void printVertices(Vertex[] verts) {
        for (int i = 0; i < 4; i ++) {
            Utility.debugPrint("(" + verts[i].x + ", " + verts[i].y + ") ");
        }
        Utility.debugPrintln("");
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
        this.center = new Vertex(x, y);
    }
    
    /**
     * Compute and store the distance of each vertex to the center of the quad
     * 
     * @param verts Vertex array of vertices of a quad
     * @param p Vertex to draw quad around
     * @param isReflected If true, reflect quad vertices about x coordinate of p
     * @return Vertex array with x-y distance of each vertex to the center of the quad
     */
    private Vertex[] computeVertDistToVertex(Vertex[] verts, Vertex p/*, boolean isReflected*/) {
        Vertex[] distToCenter = new Vertex[4];
        for (int i = 0; i < 4; i ++) {
            /*if (isReflected) {
                distToCenter[i] = new Vertex(verts[i].x - p.x, p.y - verts[i].y);
            } else {*/
                distToCenter[i] = new Vertex(verts[i].x - p.x, verts[i].y - p.y);
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
    public Vertex[] scaleQuad(double scaleFactor) {
        Vertex[] scaledVertices = Utility.deepCopyVertexArray(this.vertices);
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
        Utility.debugPrintln("Minimizing quad");
        
        double curScale = 1.0;
        Vertex[] tempVertices = Utility.deepCopyVertexArray(this.vertices);
        while (edgeLengthsLargerThanMin(tempVertices, 3.0)) {
            this.vertices = Utility.deepCopyVertexArray(tempVertices);
            curScale -= 0.1;
            tempVertices = scaleQuad(curScale);
        }
        Utility.debugPrintln("");
    }
    
    /**
     * Check that area of quad is less than 1
     * 
     * @param vertices Set of vertices defining a quad
     * @param min Minimum allowed length of an edge in the quad
     * @return True if area is larger than 1, false otherwise
     */
    private boolean edgeLengthsLargerThanMin(Vertex[] vertices, double min) {
        int j = 1;
        for (int i = 0; i < 4; i ++) 
        {
            j = (j==3) ? 0 : i+1;
            //Utility.debugPrintln("edge length: " + euclideanDistance(vertices[i], vertices[j]));
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
    public Vertex[] getPixelVertsForVertex(Vertex p, double scale) {
        Vertex[] distToCenter = computeVertDistToVertex(scaleQuad(scale), this.center);
        Vertex[] verts = new Vertex[4];
        for (int i = 0; i < 4; i ++) {
            verts[i] = new Vertex( (p.x + distToCenter[i].x), (p.y + distToCenter[i].y));
        }
        return verts;
    }
    
    /**
     * 
     * @return Cloned list of vertices defining the quad
     */
    public Vertex[] getVertices() {
        return Utility.deepCopyVertexArray(this.vertices);
    }
    
    /**
     * @param v A vertex of the Quadrilateral
     * @return Next clockwise vertex in the vertex array of the Quadrilateral
     */
    public Vertex nextVertex(Vertex v) {
        for (int i = 0; i < this.vertices.length; i ++) {
            if (v.equals(this.vertices[i])) {
                
                return (i != this.vertices.length-1) ? new Vertex(this.vertices[i+1].x, this.vertices[i+1].y) : new Vertex(this.vertices[0].x, this.vertices[0].y);
            }
        }
        return null; // v is an invalid vertex
    }
    
    /**
     * @param v A vertex of the Quadrilateral
     * @return Previous clockwise vertex in the vertex array of the Quadrilateral
     */
    public Vertex prevVertex(Vertex v) {
        for (int i = 0; i < this.vertices.length; i ++) {
            if (v.equals(this.vertices[i])) {
                return (i != 0) ? new Vertex(this.vertices[i-1].x, this.vertices[i-1].y) : new Vertex(this.vertices[this.vertices.length-1].x, this.vertices[this.vertices.length-1].y);
            }
        }
        return null; // v is an invalid vertex
    }
    
    /**
     * 
     * @return Center point of quad
     */
    public Vertex getCenter() {
        return new Vertex(this.center.x, this.center.y);
    }
    
    /**
     * Draw quad around a point
     * 
     * @param g2d Graphics 2D object used to draw to the screen
     * @param p Vertex to draw quad around
     * @param scale Amount to scale quad by
     * @param yMax Height of screen. Used to draw from bottom left corner
     */
    public void drawQuad(Graphics2D g2d, Vertex p, double scale, int yMax) {
        Vertex[] distToCenter = computeVertDistToVertex(scaleQuad(scale), this.center);
        
        int j = 1;
        for (int i = 0; i < 4; i ++) {
            j = (j==3) ? 0 : i+1; // Wrap around to draw edge from vertices[3] to vertices[0]
            g2d.drawLine(((int)Math.round(p.x + distToCenter[i].x)), yMax - ((int)Math.round(p.y + distToCenter[i].y)), 
                    ((int)Math.round(p.x + distToCenter[j].x)), yMax - ((int)Math.round(p.y + distToCenter[j].y))); // x1, y1, x2, y2
        }
        
    }
    
}
