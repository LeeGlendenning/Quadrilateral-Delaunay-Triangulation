package dt.Test;

import dt.Vertex;
import dt.Quadrilateral;
import dt.UI;
import dt.Vertex;
import java.util.ArrayList;

/**
 * Test cases for the dt package
 * 
 * @author Lee Glendenning
 */
public class Test {
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ArrayList<Vertex> pts = new ArrayList();
        //b3p case example: (100,100), (400,300), (200,500), diamond, center = (23, 15)
        //pts.add(new Vertex(100, 100));
        //pts.add(new Vertex(200, 200));
        //pts.add(new Vertex(300, 300));
        //pts.add(new Vertex(150, 350));
        //pts.add(new Vertex(150, 300));
        //pts.add(new Vertex(400, 300));
        //pts.add(new Vertex(600, 300));
        //pts.add(new Vertex(200, 320));
        
        // Question case: this is not a triangulation. how to do point location?
        /*pts.add(new Vertex(209, 407));
        pts.add(new Vertex(561, 223));
        pts.add(new Vertex(216, 115));
        pts.add(new Vertex(374, 226));
        pts.add(new Vertex(422, 268));*/
        
        
        /*pts.add(new Vertex(548, 285));
        pts.add(new Vertex(326, 474));
        pts.add(new Vertex(276, 271));*/
        
        
        // Meeting July 18 Test Cases...
        
        // Show working case for min quad
        // Test case 3 - non-collinear (diamond) a3 on a1u
        /*pts.add(new Vertex(150, 300));
        pts.add(new Vertex(400, 250));
        pts.add(new Vertex(200, 350));*/
        
        // Problem: min quad does not pass through all vertices (quad1)
        /*pts.add(new Vertex(150, 300));
        pts.add(new Vertex(300, 300));
        pts.add(new Vertex(150, 350));*/
        
        // Test case 3 - non-collinear (quad1) a3 on a1u
        /*pts.add(new Vertex(200, 300));
        pts.add(new Vertex(400, 300));
        pts.add(new Vertex(300, 325));*/
        
        
        
        
        
        
        
        // Test case 2 and 3_non-collinear (square)
        /*pts.add(new Vertex(150, 350));
        pts.add(new Vertex(400, 300));
        pts.add(new Vertex(200, 300));
        pts.add(new Vertex(300, 500));*/
        
        // Test case 2 and 3_non-collinear (quad2)
        /*pts.add(new Vertex(100, 100));
        pts.add(new Vertex(200, 200));
        pts.add(new Vertex(150, 350));
        pts.add(new Vertex(200, 300));*/
        
        
        // Case 3 - collinear square
        /*pts.add(new Vertex(150, 300));
        pts.add(new Vertex(400, 300));
        pts.add(new Vertex(200, 300));*/
        
        // Case 2,3 - collinear, 3 - non-collinear diamond
        /*pts.add(new Vertex(150, 300));
        pts.add(new Vertex(100, 100));
        pts.add(new Vertex(300, 300));
        pts.add(new Vertex(400, 400));*/
        
        
        // Case 3 - non collinear square
        /*pts.add(new Vertex(150, 350));
        pts.add(new Vertex(400, 300));
        pts.add(new Vertex(200, 350));*/
        
        // Case 3 - not collinear diamond
        /*pts.add(new Vertex(150, 350));
        pts.add(new Vertex(200, 200));
        pts.add(new Vertex(200, 300));
        pts.add(new Vertex(200, 500));*/
        
        // Quad1
        //Vertex[] quad = {new Vertex(0,0), new Vertex(10,30), new Vertex(30,40), new Vertex(40,10)};
        //Vertex center = new Vertex(13, 17);
        
        // Quad2
        //Vertex[] quad = {new Vertex(20,0), new Vertex(10,30), new Vertex(30,40), new Vertex(40,10)};
        
        // Quad3 - One cone for vertices having same y
        //Vertex[] quad = {new Vertex(0,0), new Vertex(10,30), new Vertex(20,20), new Vertex(20,0)};
        
        // Quad4 - One cone for vertices having same y
        //Vertex[] quad = {new Vertex(0,0), new Vertex(10,30), new Vertex(20,30), new Vertex(20,10)};
        
        // Square
        Vertex[] quad = {new Vertex(20,20), new Vertex(20,0), new Vertex(0,0), new Vertex(0,20)};
        
        // Diamond
        //Vertex[] quad = {new Vertex(0,20), new Vertex(20,40), new Vertex(40,20), new Vertex(20,0)};
        
        Quadrilateral q = new Quadrilateral(quad);
        //Quadrilateral q = new Quadrilateral(quad, center);
        
        //DelaunayTriangulation dt = new DelaunayTriangulation(q, pts);
        UI ui = new UI(q, pts);
        //dt.drawDT();
    }

}
