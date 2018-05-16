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
        pts.add(new Point(2, 3));
        
        Point[] quad = {new Point(0,0), new Point(2,0), new Point(2,2), new Point(0,2)};
        Quadrilateral q = new Quadrilateral(quad);
        
        DelaunayTriangulation dt = new DelaunayTriangulation(q, pts);
        //dt.drawDT();
    }

}
