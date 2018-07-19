package dt;

import java.util.ArrayList;

/**
 *
 * @author Lee Glendenning
 */
public class Utility {
    
    /**
     * 
     * @param a A Point
     * @param b A Point
     * @param c A Point
     * @return True if a, b, c are collinear. False otherwise
     */
    public static boolean isCollinear(Point a, Point b, Point c) {
        return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x) == 0;
    }
    
    /**
     * 
     * @param p1 Endpoint of first line segment
     * @param p2 Endpoint of first line segment
     * @param p3 Endpoint of second line segment
     * @param p4 Endpoint of second line segment
     * @param floatTolerance Double tolerance for comparing two floating point numbers
     * @return True if p1p2 is parallel (has same slope within 10 decimal places) to p3p4
     */
    public static boolean isParallel(Point p1, Point p2, Point p3, Point p4, double floatTolerance) {
        //System.out.println("p1 = " + p1 + "p2 = " + p2 + "p3 = " + p3 + "p4 = " + p4);
        //System.out.println("isparallel: " + calculateAngle(p1, p2) + " : " + calculateAngle(p3, p4) + " == " + (Math.abs(calculateAngle(p1, p2) - calculateAngle(p3, p4)) < this.floatTolerance));
        return Math.abs(calculateAngle(p1, p2) - calculateAngle(p3, p4)) < floatTolerance;
    }
    
    /**
     * Rotate a point around a pivot point by an angle
     * 
     * @param pivot Pivot Point
     * @param angle Rotation angle
     * @param p Point to rotate
     * @return New location of rotated point
     */
    public static Point rotatePoint(Point p, Point pivot, double angle) {
        Point r = new Point(p.x, p.y);
        double s = Math.sin(angle);
        double c = Math.cos(angle);

        // Translate point to origin (pivot point)
        r.x -= pivot.x;
        r.y -= pivot.y;

        // Rotate point
        double xnew = r.x * c - r.y * s;
        double ynew = r.x * s + r.y * c;

        // Translate point back
        r.x = xnew + pivot.x;
        r.y = ynew + pivot.y;
        
        return r;
    }
    
    /**
     * Compute slope of line segment
     * 
     * @param p1 Endpoint of line segment
     * @param p2 Endpoint of line segment
     * @return Slope of p1p2
     */
    public static double slope(Point p1, Point p2) {
        //System.out.println("Slope(" + p1 + ", " + p2 + ") = (" + p2.y + " - " + p1.y + ") / (" + p2.x + " - " + p1.x + ") = " + (p2.y - p1.y) / (p2.x - p1.x));
        return (p2.y - p1.y) / (p2.x - p1.x);
    }
    
    /**
     * Compute the midpoint of two points
     * 
     * @param p1 First point
     * @param p2 Second point
     * @return Midpoint of p1 and p2
     */
    public static Point midpoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x)/2, (p1.y + p2.y)/2);
    }
    
    /**
     * 
     * @param p1 A Point
     * @param p2 A Point
     * @return Angle p1,p2 makes with the x axis
     */
    public static double calculateAngle(Point p1, Point p2) {
        double angle; // Angle that slope(p1p2) makes with x axis
        if (p1.x == p2.x) {
            angle = Math.toRadians(-90);
            /*if (p1.y < p2.y) {
                angle = Math.toRadians(90);
            } else {
                angle = Math.toRadians(-90);
            }*/
        } else {
            angle = Math.atan((p1.y - p2.y) / (p2.x - p1.x));
        }
        return angle;
    }
    
    /**
     * NOTE: Point a should have less or equal x value to point b for sign to be correct
     * 
     * @param a Endpoint of line segment
     * @param b Endpoint of line segment
     * @param c Query point
     * @return +1 if point is left of line (ccw order), 0 if point is on line (collinear), -1 otherwise (cw order)
     */
    public static int isLeftOfSegment(Point a, Point b, Point c, double tolerance){
        //System.out.println("a = " + a + ", b = " + b + ", c = " + c);
        double cross = (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x);
        //System.out.println("isLeftOfSegment: cross = " + cross);
        
        Point ra = rotatePoint(a, midpoint(a, b), calculateAngle(a, b));
        Point rb = rotatePoint(b, midpoint(a, b), calculateAngle(a, b));
        Point rc = rotatePoint(c, midpoint(a, b), calculateAngle(a, b));
        //System.out.println("ra = " + ra + "rb = " + rb + "rc = " + rc);
        
        // Test if point c is on segment ab
        //System.out.println("isLeft: ra.y - rc.y = " + Math.abs(ra.y - rc.y) + ", rb.y - rc.y = " + Math.abs(rb.y - rc.y));
        //System.out.println(((Math.abs(ra.y - rc.y) < tolerance && Math.abs(rb.y - rc.y) < tolerance) || cross == 0));
        //System.out.println("rc.x = " + rc.x + ", min(ra.x, rb.x) = " + Math.min(ra.x, rb.x) + ", max(ra.x, rb.x) = " + Math.max(ra.x, rb.x));
        //System.out.println(Math.abs(rc.x - Math.max(ra.x, rb.x)) + ", " + Math.abs(rc.x - Math.min(ra.x, rb.x)));
        //System.out.println(((rc.x > Math.min(a.x, b.x) && rc.x < Math.max(a.x, b.x)) || Math.abs(rc.x - Math.max(a.x, b.x)) < tolerance || Math.abs(rc.x - Math.min(a.x, b.x)) < tolerance));
        if (((Math.abs(ra.y - rc.y) < tolerance && Math.abs(rb.y - rc.y) < tolerance) || cross == 0) &&
                (rc.x > Math.min(ra.x, rb.x) && rc.x < Math.max(ra.x, rb.x) || Math.abs(rc.x - Math.max(ra.x, rb.x)) < tolerance || Math.abs(rc.x - Math.min(ra.x, rb.x)) < tolerance)) {
            return 0;
        } else if (cross > 1) {
            return 1;
        } else {
            return -1;
        }
   }
    
    /**
     * 
     * @param arr1 Point array
     * @param arr2 Point array
     * @return Point array containing the union of arr1 and arr2
     */
    public static Point[] pointArrayUnion(Point[] arr1, Point[] arr2) {
        ArrayList<Point> union = new ArrayList();
        
        for (Point p1 : arr1) {
            union.add(p1);
        }
        
        for (Point p2 : arr2) {
            if (!union.contains(p2)) {
                union.add(p2);
            }
        }
        
        return union.toArray(new Point[union.size()]);
    }
    
    /**
     * Compute the Euclidean distance between two points
     * 
     * @param p1 First point
     * @param p2 Second point
     * @return Euclidean distance between p1 and p2
     */
    public static double euclideanDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    
    /**
     * Take the cross product of two point objects
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Cross product of the two points
     */
    public static double crossProduct(Point p1, Point p2) {
        return (p1.x * p2.y) - (p1.y * p2.x);
    }
    
    /**
     * Add the x and y values of two points
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Result of p1 + p2
     */
    public static Point addPoints(Point p1, Point p2) {
        Point addP = new Point();
        addP.x = p1.x + p2.x;
        addP.y = p1.y + p2.y;

        return addP;
    }
    
    /**
     * Subtract the x and y values of point object from another
     *
     * @param p1 Point to be subtracted from
     * @param p2 Point to subtract other point by
     * @return Result of p1 - p2
     */
    public static Point subtractPoints(Point p1, Point p2) {
        Point subP = new Point();
        subP.x = p1.x - p2.x;
        subP.y = p1.y - p2.y;

        return subP;
    }
    
    /**
     * Determine whether the x and y values of two points are both equal
     *
     * @param p1 First point to compare
     * @param p2 Second point to compare
     * @return True if point are equal, false otherwise
     */
    public static boolean equalPoints(Point p1, Point p2, double tolerance) {
        return (Math.abs(p1.x - p2.x) < tolerance && Math.abs(p1.y - p2.y) < tolerance);
    }
    
    /**
     * 
     * @param pts Array of Points
     * @return ArrayList version of given array
     */
    public static ArrayList<Point> arrayToList(Point[] pts) {
        ArrayList<Point> ptsList = new ArrayList();
        for (int i = 0; i < pts.length; i ++) {
            ptsList.add(pts[i]);
        }
        return ptsList;
    }
}
