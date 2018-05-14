package dt;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Create interface to allow users to click and define a point set
 * 
 * @author Lee Glendenning
 */
public class DefinePoints {
    
    ArrayList<Point> points;
    
    /**
     * Initialize a point set
     */
    public DefinePoints() {
        points = new ArrayList();
    }
    
    /**
     * Add point to the point set
     * 
     * @param p Point to add to point set
     */
    public void addPoint(Point p) {
        points.add(p);
    }
    
}
