package dt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Lee Glendenning
 */
public class Utility {
    
    public static final double RAY_SIZE = 10000000;
    
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
     * @param arr Array of Objects
     * @return ArrayList version of given array
     */
    public static ArrayList<Object> arrayToList(Object[] arr) {
        ArrayList<Object> list = new ArrayList();
        for (int i = 0; i < arr.length; i ++) {
            list.add(arr[i]);
        }
        return list;
    }
    
    /**
     * Determine whether two line segments intersect using vector cross product
     * approach Method outlined in http://stackoverflow.com/a/565282/786339
     *
     * @param p1 First point of first line segment
     * @param p2 Second point of first line segment
     * @param q1 First point of second line segment
     * @param q2 Second point of second line segment
     * @return Intersection point if the line segments intersect, null otherwise
     */
    public static Point doLineSegmentsIntersect(Point p1, Point p2, Point q1, Point q2) {
        //System.out.println("DoLineSegmentsIntersect: " + p1 + ", " + p2 + " : " + q1 + ", " + q2);
        Point r = Utility.subtractPoints(p2, p1);
        Point s = Utility.subtractPoints(q2, q1);

        double numerator = Utility.crossProduct(Utility.subtractPoints(q1, p1), r);
        double denominator = Utility.crossProduct(r, s);
        
        // Lines are collinear
        if (numerator == 0 && denominator == 0) {
            double tolerance = 0.01;
            // If line segments share an endpoint, line segments intersect
            if (Utility.equalPoints(p1, q1, tolerance) || Utility.equalPoints(p1, q2, tolerance) || Utility.equalPoints(p2, q1, tolerance) || Utility.equalPoints(p2, q2, tolerance)) {
                Point intersection;
                if (Utility.equalPoints(p1, q1, tolerance) || Utility.equalPoints(p1, q2, tolerance)) {
                    intersection = p1;
                } else {
                    intersection = p2;
                }
                //System.out.println("1Found intersection at (" + intersection.x + ", " + intersection.y + ")");
                return intersection;
            }

            // Line segments overlap if all point differences in either direction do not have the same sign
            if (!allEqual(new boolean[]{(q1.x - p1.x < 0), (q1.x - p2.x < 0),
                (q2.x - p1.x < 0), (q2.x - p2.x < 0)}) || !allEqual(new boolean[]{(q1.y - p1.y < 0),
                (q1.y - p2.y < 0), (q2.y - p1.y < 0), (q2.y - p2.y < 0)})) 
            {
                // Need to return multiple points or a line segment?
                return null;
            } else {
                return null;
            }
        }

        // Lines are parallel and do not intersect
        if (denominator == 0) {
            return null;
        }

        double u = numerator / denominator;
        double t = Utility.crossProduct(Utility.subtractPoints(q1, p1), s) / denominator;
        
        // Lines are not parallel but intersect
        if ((t >= 0) && (t <= 1) && (u >= 0) && (u <= 1)) {
            Point intersection;
            r.x *= t;
            r.y *= t;
            intersection = Utility.addPoints(p1, r);
            //System.out.println("2Found intersection at (" + intersection.x + ", " + intersection.y + ")");
            return intersection;
        }

        return null;
    }
    
    /**
     * Determine whether an array of boolean values all have the same value
     *
     * @param arguments Array of boolean values
     * @return True if all array elements are the same, false otherwise
     */
    private static boolean allEqual(boolean[] arguments) {
        boolean firstValue = arguments[0];

        for (int i = 1; i < arguments.length; i++) {
            if (arguments[i] != firstValue) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Determine which point is left and right based on normal
     * 
     * @param p1 First point to consider
     * @param p2 Second  point to consider
     * @param left Point object to assign as left point
     * @param right Point object to assign as right point
     * @param axisRotation Angle of slope p1p2
     */
    public static void setLeftAndRightPoint(Point p1, Point p2, Point left, Point right, double angle) {
        
        //System.out.println("Rotating " + p1 + " and " + p2 + " by " + Math.toDegrees(angle) + " degrees");
        Point r1 = Utility.rotatePoint(p1, Utility.midpoint(p1, p2), angle);
        Point r2 = Utility.rotatePoint(p2, Utility.midpoint(p1, p2), angle);
        //System.out.println("Rotated points: " + r1 + ", " + r2);
        
        if (Math.min(r1.x, r2.x) == r1.x) {
            left.x = p1.x;
            left.y = p1.y;
            right.x = p2.x;
            right.y = p2.y;
        } else {
            left.x = p2.x;
            left.y = p2.y;
            right.x = p1.x;
            right.y = p1.y;
        }
        
    }
    
    /**
     * Find the two vertices of a quad that have max and min y values wrt an angle
     * 
     * @param q A quadrilateral to iterate over
     * @param a1 Point used as reference for rotation
     * @param a2 Point used as reference for rotation
     * @param angle Angle to rotate quad by such that a1a2 is parallel to x axis
     * @return Array of nonInner vertices of size 2
     */
    public static ArrayList<Point> findNonInnerVertices(Quadrilateral q, Point a1, Point a2, double angle) {
        ArrayList<Point> nonInnerVerts = new ArrayList();
        Point[] rVerts = new Point[4];
        
        // Rotate all quad vertices
        //System.out.print("Non-Inner Vertices rVerts: ");
        for (int i = 0; i < 4; i ++) {
            rVerts[i] = Utility.rotatePoint(q.getVertices()[i], Utility.midpoint(a1, a2), angle);
            //System.out.print(rVerts[i] + ", ");
        }
        //System.out.println();
        
        // Sort rotated quad vertices by ascending y value (more or less sweep line)
        Arrays.sort(rVerts, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                if (p1.y > p2.y) {
                    return +1;
                } else if (p1.y < p2.y) {
                     return -1;
                } else {
                    return 0;
                }
            }
        });
        
        //System.out.println("rVerts: " + rVerts[0] + ", " + rVerts[1] + ", " + rVerts[2] + ", " + rVerts[3]);
        double tolerance = 0.00001;
        // Check for SL hitting an edge
        if (Math.abs(rVerts[0].y - rVerts[1].y) < tolerance /*&& rVerts[0].x < rVerts[1].x*/) {
            nonInnerVerts.add(Utility.rotatePoint(rVerts[0], Utility.midpoint(a1, a2), -angle));
            nonInnerVerts.add(Utility.rotatePoint(rVerts[1], Utility.midpoint(a1, a2), -angle));
        } else {
            nonInnerVerts.add(Utility.rotatePoint(rVerts[0], Utility.midpoint(a1, a2), -angle));
        }
        
        if (Math.abs(rVerts[2].y - rVerts[3].y) < tolerance /*&& rVerts[2].x > rVerts[3].x*/) {
            nonInnerVerts.add(Utility.rotatePoint(rVerts[2], Utility.midpoint(a1, a2), -angle));
            nonInnerVerts.add(Utility.rotatePoint(rVerts[3], Utility.midpoint(a1, a2), -angle));
        } else {
            nonInnerVerts.add(Utility.rotatePoint(rVerts[3], Utility.midpoint(a1, a2), -angle));
        }
        
        System.out.print("Non-inner vertices ");
        for (Point p : nonInnerVerts) {
            System.out.print(p + " ");
        }
        System.out.println();
        
        //System.out.println("nonInner verts: " + nonInnerVerts[0] + " " + nonInnerVerts[1]);
        return nonInnerVerts;
    }
    
    /**
     * Create deep copy of a point array
     * 
     * @param ptArr Point array to clone
     * @return Deep copy of ptSet
     */
    public static Point[] deepCopyPointArray(Point[] ptArr) {
        Point[] newSet = new Point[ptArr.length];
        for (int i = 0; i < ptArr.length; i ++) {
            newSet[i] = ptArr[i].deepCopy();
        }
        return newSet;
    }
    
    /**
     * 
     * @param vbArr Array of VoronoiBisector objects
     * @return Deep copy of vbArr
     */
    public static Bisector[] deepCopyVBArray(Bisector[] vbArr) {
        Bisector[] newVB = new Bisector[vbArr.length];
        for (int i = 0; i < vbArr.length; i ++) {
            newVB[i] = vbArr[i].deepCopy();
        }
        return newVB;
    }
    
}
