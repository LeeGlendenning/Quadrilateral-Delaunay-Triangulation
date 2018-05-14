package dt;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Constructs a Voronoi diagram from a point set using a Quadrilateral
 * 
 * @author Lee Glendenning
 */
public class VoronoiDiagram {
    
    private final ArrayList<Point> points;
    private final Quadrilateral quad;
    
    /**
     * Construct Voronoi diagram for point set using a Quadrilateral
     * 
     * @param q Quadrilateral
     * @param p Point set
     */
    public VoronoiDiagram(Quadrilateral q, ArrayList<Point> p) {
        this.points = p;
        this.quad = q;
        
        constructVoronoi();
    }
    
    /**
     * Construct Voronoi diagram for the point set using the quad
     */
    private void constructVoronoi() {
        
    }
    
}
