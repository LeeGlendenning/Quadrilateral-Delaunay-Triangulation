package dt.Test;

import dt.DelaunayTriangulation;
import dt.Quadrilateral;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Short example to test the dt package
 * 
 * @author Lee Glendenning
 */
public class Test {
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ArrayList<Point> pts = new ArrayList();
        pts.add(new Point(100, 100));
        pts.add(new Point(500, 300));
        pts.add(new Point(200, 300));
        
        Point[] quad = {new Point(0,0), new Point(20,0), new Point(40,20), new Point(0,20)};
        Quadrilateral q = new Quadrilateral(quad);
        
        DelaunayTriangulation dt = new DelaunayTriangulation(q, pts);
        //dt.drawDT();
    }

}
