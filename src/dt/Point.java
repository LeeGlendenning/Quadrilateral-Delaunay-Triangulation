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
        //this.c = randomColour();
        this.c = Color.black;
    }
    
    public Color getColour() {
        return this.c;
    }
    
    private Color randomColour() {
        Random rand = new Random();
        return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
    @Override
    public boolean equals(Object other) {
        double tolerance = 0.01;
        if (other.getClass() == this.getClass() && Math.abs(this.x - ((Point) other).x) < tolerance && Math.abs(this.y - ((Point) other).y) < tolerance) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 
     * @return Deep copy of this Point
     */
    public Point deepCopy() {
        return new Point(this.x, this.y);
    }
    
}
