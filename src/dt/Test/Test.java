package dt.Test;

import dt.DelaunayTriangulation;
import dt.Point;
import dt.Quadrilateral;
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
        //b3p case example: (100,100), (400,300), (200,500), diamond, center = (23, 15)
        pts.add(new Point(100, 100));
        pts.add(new Point(200, 200));
        pts.add(new Point(150, 350));
        pts.add(new Point(400, 300));
        pts.add(new Point(200, 300));
        pts.add(new Point(200, 500));
        
        
        // Quad1
        //Point[] quad = {new Point(0,0), new Point(10,30), new Point(30,40), new Point(40,10)};
        Point center = new Point(13, 17);
        
        // Quad2
        //Point[] quad = {new Point(20,0), new Point(10,30), new Point(30,40), new Point(40,10)};
        
        // Square
        Point[] quad = {new Point(0,0), new Point(0,20), new Point(20,20), new Point(20,0)};
        
        // Diamond
        //Point[] quad = {new Point(0,20), new Point(20,40), new Point(40,20), new Point(20,0)};
        
        //Quadrilateral q = new Quadrilateral(quad);
        Quadrilateral q = new Quadrilateral(quad, center);
        
        DelaunayTriangulation dt = new DelaunayTriangulation(q, pts);
        //dt.drawDT();
    }

}
