package dt;

import java.awt.Color;
import java.util.Random;

/**
 * Maintains a 2D point
 * 
 * @author Lee Glendenning
 */
public class Point {
    
    public double x;
    public double y;
    private Color c;
    
    public Point () {
        
    }
    
    public Point (double x, double y) {
        this.x = x;
        this.y = y;
        this.c = randomColour();
    }
    
    public Color getColour() {
        return this.c;
    }
    
    private Color randomColour() {
        Random rand = new Random();
        return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
    }
    
}
