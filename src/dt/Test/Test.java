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
        pts.add(new Point(1, 1));
        pts.add(new Point(5, 3));
        
        Point[] quad = {new Point(0,0), new Point(1,0), new Point(1,1), new Point(0,1)};
        Quadrilateral q = new Quadrilateral(quad);
        
        DelaunayTriangulation dt = new DelaunayTriangulation(q, pts);
        dt.drawDT();
    }

}
