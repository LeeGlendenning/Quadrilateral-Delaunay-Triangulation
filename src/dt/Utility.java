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
    public static boolean debugMode = true;
    
    
    /**
     * 
     * @param a A Vertex
     * @param b A Vertex
     * @param c A Vertex
     * @return True if a, b, c are collinear. False otherwise
     */
    public static boolean isCollinear(Vertex a, Vertex b, Vertex c) {
        return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x) == 0;
    }
    
    /**
     * 
     * @param v1 Endpoint of first line segment
     * @param v2 Endpoint of first line segment
     * @param p3 Endpoint of second line segment
     * @param p4 Endpoint of second line segment
     * @param floatTolerance Double tolerance for comparing two floating point numbers
     * @return True if v1v2 is parallel (has same slope within 10 decimal places) to p3p4
     */
    public static boolean isParallel(Vertex v1, Vertex v2, Vertex p3, Vertex p4, double floatTolerance) {
        //Utility.debugPrintln("v1 = " + v1 + "v2 = " + v2 + "p3 = " + p3 + "p4 = " + p4);
        //Utility.debugPrintln("isparallel: " + calculateAngle(v1, v2) + " : " + calculateAngle(p3, p4) + " == " + (Math.abs(calculateAngle(v1, v2) - calculateAngle(p3, p4)) < this.floatTolerance));
        return Math.abs(calculateAngle(v1, v2) - calculateAngle(p3, p4)) < floatTolerance;
    }
    
    /**
     * Rotate a point around a pivot point by an angle
     * 
     * @param pivot Pivot Vertex
     * @param angle Rotation angle
     * @param p Vertex to rotate
     * @return New location of rotated point
     */
    public static Vertex rotateVertex(Vertex p, Vertex pivot, double angle) {
        Vertex r = new Vertex(p.x, p.y);
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
     * @param v1 Endpoint of line segment
     * @param v2 Endpoint of line segment
     * @return Slope of v1v2
     */
    public static double slope(Vertex v1, Vertex v2) {
        //Utility.debugPrintln("Slope(" + v1 + ", " + v2 + ") = (" + v2.y + " - " + v1.y + ") / (" + v2.x + " - " + v1.x + ") = " + (v2.y - v1.y) / (v2.x - v1.x));
        return (v2.y - v1.y) / (v2.x - v1.x);
    }
    
    /**
     * Compute the midpoint of two points
     * 
     * @param v1 First point
     * @param v2 Second point
     * @return Midpoint of v1 and v2
     */
    public static Vertex midpoint(Vertex v1, Vertex v2) {
        return new Vertex((v1.x + v2.x)/2, (v1.y + v2.y)/2);
    }
    
    /**
     * 
     * @param v1 A Vertex
     * @param v2 A Vertex
     * @return Angle v1,v2 makes with the x axis
     */
    public static double calculateAngle(Vertex v1, Vertex v2) {
        double angle; // Angle that slope(v1v2) makes with x axis
        if (v1.x == v2.x) {
            angle = Math.toRadians(-90);
            /*if (v1.y < v2.y) {
                angle = Math.toRadians(90);
            } else {
                angle = Math.toRadians(-90);
            }*/
        } else {
            angle = Math.atan((v1.y - v2.y) / (v2.x - v1.x));
        }
        return angle;
    }
    
    /**
     * NOTE: Vertex a should have less or equal x value to point b for sign to be correct
     *       
     * 
     * @param a Endpoint of line segment
     * @param b Endpoint of line segment
     * @param c Query point
     * @param tolerance Double tolerance for comparing equivalence of doubles
     * @return +1 if point is left of line (ccw order), 0 if point is on line (collinear), -1 otherwise (cw order)
     */
    public static int isLeftOfSegment(Vertex a, Vertex b, Vertex c, double tolerance){
        Vertex left = new Vertex(), right = new Vertex();
        Utility.setLeftAndRightVertex(a, b, left, right, calculateAngle(a, b));
        //Utility.debugPrintln("a = " + a + ", b = " + b + ", c = " + c);
        double cross = (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x);
        //Utility.debugPrintln("isLeftOfSegment: cross = " + cross);
        
        Vertex ra = rotateVertex(left, midpoint(left, right), calculateAngle(left, right));
        Vertex rb = rotateVertex(right, midpoint(left, right), calculateAngle(left, right));
        Vertex rc = rotateVertex(c, midpoint(left, right), calculateAngle(left, right));
        //Utility.debugPrintln("ra = " + ra + "rb = " + rb + "rc = " + rc);
        
        // Test if point c is on segment ab
        //Utility.debugPrintln("isLeft: ra.y - rc.y = " + Math.abs(ra.y - rc.y) + ", rb.y - rc.y = " + Math.abs(rb.y - rc.y));
        //Utility.debugPrintln(((Math.abs(ra.y - rc.y) < tolerance && Math.abs(rb.y - rc.y) < tolerance) || cross == 0));
        //Utility.debugPrintln("rc.x = " + rc.x + ", min(ra.x, rb.x) = " + Math.min(ra.x, rb.x) + ", max(ra.x, rb.x) = " + Math.max(ra.x, rb.x));
        //Utility.debugPrintln(Math.abs(rc.x - Math.max(ra.x, rb.x)) + ", " + Math.abs(rc.x - Math.min(ra.x, rb.x)));
        //Utility.debugPrintln(((rc.x > Math.min(a.x, b.x) && rc.x < Math.max(a.x, b.x)) || Math.abs(rc.x - Math.max(a.x, b.x)) < tolerance || Math.abs(rc.x - Math.min(a.x, b.x)) < tolerance));
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
     * @param arr1 Vertex array
     * @param arr2 Vertex array
     * @return Vertex array containing the union of arr1 and arr2
     */
    public static Vertex[] vertexArrayUnion(Vertex[] arr1, Vertex[] arr2) {
        ArrayList<Vertex> union = new ArrayList();
        
        for (Vertex v1 : arr1) {
            union.add(v1);
        }
        
        for (Vertex v2 : arr2) {
            if (!union.contains(v2)) {
                union.add(v2);
            }
        }
        
        return union.toArray(new Vertex[union.size()]);
    }
    
    /**
     * Compute the Euclidean distance between two points
     * 
     * @param v1 First point
     * @param v2 Second point
     * @return Euclidean distance between v1 and v2
     */
    public static double euclideanDistance(Vertex v1, Vertex v2) {
        return Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2));
    }
    
    /**
     * Take the cross product of two point objects
     *
     * @param v1 First point
     * @param v2 Second point
     * @return Cross product of the two points
     */
    public static double crossProduct(Vertex v1, Vertex v2) {
        return (v1.x * v2.y) - (v1.y * v2.x);
    }
    
    /**
     * Add the x and y values of two points
     *
     * @param v1 First point
     * @param v2 Second point
     * @return Result of v1 + v2
     */
    public static Vertex addVertexs(Vertex v1, Vertex v2) {
        return new Vertex(v1.x + v2.x, v1.y + v2.y);
    }
    
    /**
     * Subtract the x and y values of point object from another
     *
     * @param v1 Vertex to be subtracted from
     * @param v2 Vertex to subtract other vertex by
     * @return Result of v1 - v2
     */
    public static Vertex subtractVertexs(Vertex v1, Vertex v2) {
        return new Vertex(v1.x - v2.x, v1.y - v2.y);
    }
    
    /**
     * Determine whether the x and y values of two points are both equal
     *
     * @param v1 First point to compare
     * @param v2 Second point to compare
     * @return True if point are equal, false otherwise
     */
    public static boolean equalVertexs(Vertex v1, Vertex v2, double tolerance) {
        return (Math.abs(v1.x - v2.x) < tolerance && Math.abs(v1.y - v2.y) < tolerance);
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
     * @param v1 First point of first line segment
     * @param v2 Second point of first line segment
     * @param q1 First point of second line segment
     * @param q2 Second point of second line segment
     * @return Intersection point if the line segments intersect, null otherwise
     */
    public static Vertex doLineSegmentsIntersect(Vertex v1, Vertex v2, Vertex q1, Vertex q2) {
        //Utility.debugPrintln("DoLineSegmentsIntersect: " + v1 + ", " + v2 + " : " + q1 + ", " + q2);
        Vertex r = Utility.subtractVertexs(v2, v1);
        Vertex s = Utility.subtractVertexs(q2, q1);

        double numerator = Utility.crossProduct(Utility.subtractVertexs(q1, v1), r);
        double denominator = Utility.crossProduct(r, s);
        
        // Lines are collinear
        double tolerance = 0.01;
        if (Math.abs(numerator) < tolerance && Math.abs(denominator) < tolerance) {
            // If line segments share an endpoint, line segments intersect
            if (Utility.equalVertexs(v1, q1, tolerance) || Utility.equalVertexs(v1, q2, tolerance) || Utility.equalVertexs(v2, q1, tolerance) || Utility.equalVertexs(v2, q2, tolerance)) {
                Vertex intersection;
                if (Utility.equalVertexs(v1, q1, tolerance) || Utility.equalVertexs(v1, q2, tolerance)) {
                    intersection = v1;
                } else {
                    intersection = v2;
                }
                //Utility.debugPrintln("1Found intersection at (" + intersection.x + ", " + intersection.y + ")");
                return intersection;
            }

            // Line segments overlap if all point differences in either direction do not have the same sign
            if (!allEqual(new boolean[]{(q1.x - v1.x < 0), (q1.x - v2.x < 0),
                (q2.x - v1.x < 0), (q2.x - v2.x < 0)}) || !allEqual(new boolean[]{(q1.y - v1.y < 0),
                (q1.y - v2.y < 0), (q2.y - v1.y < 0), (q2.y - v2.y < 0)})) 
            {
                // Need to return multiple points or a line segment?
                return null;
            } else {
                return null;
            }
        }

        // Lines are parallel and do not intersect
        if (Math.abs(denominator) < tolerance) {
            return null;
        }

        double u = numerator / denominator;
        double t = Utility.crossProduct(Utility.subtractVertexs(q1, v1), s) / denominator;
        
        // Lines are not parallel but intersect
        if ((t >= 0) && (t <= 1) && (u >= 0) && (u <= 1)) {
            Vertex intersection;
            r.x *= t;
            r.y *= t;
            intersection = Utility.addVertexs(v1, r);
            //Utility.debugPrintln("2Found intersection at (" + intersection.x + ", " + intersection.y + ")");
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
     * @param v1 First point to consider
     * @param v2 Second  point to consider
     * @param left Vertex object to assign as left point
     * @param right Vertex object to assign as right point
     * @param axisRotation Angle of slope v1v2
     */
    public static void setLeftAndRightVertex(Vertex v1, Vertex v2, Vertex left, Vertex right, double angle) {
        
        //Utility.debugPrintln("Rotating " + v1 + " and " + v2 + " by " + Math.toDegrees(angle) + " degrees");
        Vertex r1 = Utility.rotateVertex(v1, Utility.midpoint(v1, v2), angle);
        Vertex r2 = Utility.rotateVertex(v2, Utility.midpoint(v1, v2), angle);
        //Utility.debugPrintln("Rotated points: " + r1 + ", " + r2);
        
        if (Math.min(r1.x, r2.x) == r1.x) {
            left.x = v1.x;
            left.y = v1.y;
            right.x = v2.x;
            right.y = v2.y;
        } else {
            left.x = v2.x;
            left.y = v2.y;
            right.x = v1.x;
            right.y = v1.y;
        }
        
    }
    
    /**
     * Find the two vertices of a quad that have max and min y values wrt an angle
     * 
     * @param q A quadrilateral to iterate over
     * @param a1 Vertex used as reference for rotation
     * @param a2 Vertex used as reference for rotation
     * @param angle Angle to rotate quad by such that a1a2 is parallel to x axis
     * @return Array of nonInner vertices
     */
    public static ArrayList<Vertex> findNonInnerVertices(Quadrilateral q, Vertex a1, Vertex a2, double angle) {
        ArrayList<Vertex> nonInnerVerts = new ArrayList();
        Vertex[] rVerts = new Vertex[4];
        // Rotate all quad vertices
        //Utility.debugPrint("Non-Inner Vertices rVerts: ");
        for (int i = 0; i < q.getVertices().length; i ++) {
            rVerts[i] = Utility.rotateVertex(q.getVertices()[i], Utility.midpoint(a1, a2), angle);
            //Utility.debugPrint(rVerts[i] + ", ");
        }
        //Utility.debugPrintln();
        
        // Sort rotated quad vertices by ascending y value (more or less sweep line)
        Arrays.sort(rVerts, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex v1, Vertex v2) {
                if (v1.y > v2.y) {
                    return +1;
                } else if (v1.y < v2.y) {
                     return -1;
                } else {
                    return 0;
                }
            }
        });
        
        //Utility.debugPrintln("rVerts: " + Arrays.toString(rVerts));
        double tolerance = 0.00001;
        // Check for SL hitting an edge
        if (Math.abs(rVerts[0].y - rVerts[1].y) < tolerance /*&& rVerts[0].x < rVerts[1].x*/) {
            nonInnerVerts.add(Utility.rotateVertex(rVerts[0], Utility.midpoint(a1, a2), -angle));
            nonInnerVerts.add(Utility.rotateVertex(rVerts[1], Utility.midpoint(a1, a2), -angle));
        } else {
            nonInnerVerts.add(Utility.rotateVertex(rVerts[0], Utility.midpoint(a1, a2), -angle));
        }
        
        if (Math.abs(rVerts[2].y - rVerts[3].y) < tolerance /*&& rVerts[2].x > rVerts[3].x*/) {
            nonInnerVerts.add(Utility.rotateVertex(rVerts[2], Utility.midpoint(a1, a2), -angle));
            nonInnerVerts.add(Utility.rotateVertex(rVerts[3], Utility.midpoint(a1, a2), -angle));
        } else {
            nonInnerVerts.add(Utility.rotateVertex(rVerts[3], Utility.midpoint(a1, a2), -angle));
        }
        
        //Utility.debugPrintln("Non-inner vertices: " + nonInnerVerts.toString());
        
        return nonInnerVerts;
    }
    
    /**
     * Create deep copy of a point array
     * 
     * @param ptArr Vertex array to clone
     * @return Deep copy of ptSet
     */
    public static Vertex[] deepCopyVertexArray(Vertex[] ptArr) {
        Vertex[] newSet = new Vertex[ptArr.length];
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
    
    /**
     * 
     * @param s String to print to console without line break
     */
    public static void debugPrint(String s) {
        if (debugMode) {
            System.out.print(s);
        }
    }
    
    /**
     * 
     * @param s String to print to console with line break
     */
    public static void debugPrintln(String s) {
        if (debugMode) {
            System.out.println(s);
        }
    }
    
}
