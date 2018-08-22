package dt.Test;

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
        
        
        
        // General position test case
        /*pts.add(new Vertex(1, 1));
        pts.add(new Vertex(101, 1));
        pts.add(new Vertex(1, 101));
        pts.add(new Vertex(101, 102));*/
        
        
        
        
        
        // Quad1
        Vertex[] quad = {new Vertex(0,0), new Vertex(10,30), new Vertex(30,40), new Vertex(40,10)};
        //Vertex center = new Vertex(13, 17);
        
        // Quad2
        //Vertex[] quad = {new Vertex(20,0), new Vertex(10,30), new Vertex(30,40), new Vertex(40,10)};
        
        // Quad3 - One cone for vertices having same y
        //Vertex[] quad = {new Vertex(0,0), new Vertex(10,30), new Vertex(20,20), new Vertex(20,0)};
        
        // Quad4 - One cone for vertices having same y
        //Vertex[] quad = {new Vertex(0,0), new Vertex(10,30), new Vertex(20,30), new Vertex(20,10)};
        
        // Square
        //Vertex[] quad = {new Vertex(20,20), new Vertex(20,0), new Vertex(0,0), new Vertex(0,20)};
        
        // Diamond
        //Vertex[] quad = {new Vertex(0,20), new Vertex(20,40), new Vertex(40,20), new Vertex(20,0)};
        
        Quadrilateral q = new Quadrilateral(quad);
        //Quadrilateral q = new Quadrilateral(quad, center);
        
        UI ui = new UI(q, pts);
    }

}
