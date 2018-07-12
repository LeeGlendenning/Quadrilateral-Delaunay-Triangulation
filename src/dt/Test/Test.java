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
        pts.add(new Point(300, 300));
        pts.add(new Point(150, 350));
        pts.add(new Point(150, 300));
        pts.add(new Point(400, 300));
        //pts.add(new Point(600, 300));
        pts.add(new Point(200, 320));
        
        
        
        // Test case 2 and 3_non-collinear (square)
        /*pts.add(new Point(150, 350));
        pts.add(new Point(400, 300));
        pts.add(new Point(200, 300));
        pts.add(new Point(300, 500));*/
        
        // Test case 2 and 3_non-collinear (quad2)
        /*pts.add(new Point(100, 100));
        pts.add(new Point(200, 200));
        pts.add(new Point(150, 350));
        pts.add(new Point(200, 300));*/
        
        
        // Case 3 - collinear square
        /*pts.add(new Point(150, 300));
        pts.add(new Point(400, 300));
        pts.add(new Point(200, 300));*/
        
        // Case 2,3 - collinear, 3 - non-collinear diamond
        /*pts.add(new Point(150, 300));
        pts.add(new Point(100, 100));
        pts.add(new Point(300, 300));
        pts.add(new Point(400, 400));*/
        
        
        // Case 3 - non collinear square
        /*pts.add(new Point(150, 350));
        pts.add(new Point(400, 300));
        pts.add(new Point(200, 300));*/
        
        // Case 3 - not collinear diamond
        /*pts.add(new Point(150, 350));
        pts.add(new Point(200, 200));
        pts.add(new Point(200, 300));
        pts.add(new Point(200, 500));*/
        
        // Quad1
        //Point[] quad = {new Point(0,0), new Point(10,30), new Point(30,40), new Point(40,10)};
        //Point center = new Point(13, 17);
        
        // Quad2
        //Point[] quad = {new Point(20,0), new Point(10,30), new Point(30,40), new Point(40,10)};
        
        // Quad3 - One cone for points having same y
        //Point[] quad = {new Point(0,0), new Point(10,30), new Point(20,20), new Point(20,0)};
        
        // Quad4 - One cone for points having same y
        //Point[] quad = {new Point(0,0), new Point(10,30), new Point(20,30), new Point(20,10)};
        
        // Square
        //Point[] quad = {new Point(20,20), new Point(20,0), new Point(0,0), new Point(0,20)};
        
        // Diamond
        Point[] quad = {new Point(0,20), new Point(20,40), new Point(40,20), new Point(20,0)};
        
        Quadrilateral q = new Quadrilateral(quad);
        //Quadrilateral q = new Quadrilateral(quad, center);
        
        DelaunayTriangulation dt = new DelaunayTriangulation(q, pts);
        //dt.drawDT();
    }

}
