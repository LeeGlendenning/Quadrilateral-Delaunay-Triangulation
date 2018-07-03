package dt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Constructs a Voronoi diagram from a point set using a Quadrilateral
 *
 * @author Lee Glendenning
 */
public class VoronoiDiagram extends JPanel {

    private final ArrayList<Point> points;
    private final Quadrilateral quad;
    private ArrayList<VoronoiBisector> voronoiEdges;
    private ArrayList<Point> voronoiPoints; // Temporary until bisector points are grouped as edges
    private double curScale = 1.0;
    private final int pixelFactor = 1;
    private Timer timer;
    private int scaleIterations;
    
    private final boolean onlyShowMainBisectors = true;
    
    ArrayList<Point> h1, h2, g1, g2;

    /**
     * Construct Voronoi diagram for point set using a Quadrilateral
     *
     * @param q Quadrilateral
     * @param p Point set
     */
    public VoronoiDiagram(Quadrilateral q, ArrayList<Point> p) {
        this.points = p;
        this.quad = q;
        this.voronoiEdges = new ArrayList();
        this.voronoiPoints = new ArrayList();
        this.scaleIterations = 0;
        this.h1 = new ArrayList();
        this.h2 = new ArrayList();
        this.g1 = new ArrayList();
        this.g2 = new ArrayList();
        createJFrame();
        //constructVoronoi();
        doVoronoiAnimation(40, 2000);
        
        Point left = new Point(), right = new Point();
        setLeftAndRightPoint(this.points.get(0), this.points.get(1), left, right, calculateAngle(this.points.get(0), this.points.get(1)));
        findBisectorOfThreeSites(this.quad, left, right, new Point());
    }
    
    /**
     * Animate quad scaling and intersection discovery
     */
    private void doVoronoiAnimation(int delay, int maxScaleIterations) {
        
        // for each pair of points, find main bisector
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                findBisectorOfTwoSites(this.quad, this.points.get(i), this.points.get(j));
            }
        }
        
        // Consider having a method which checks whether all quad segments are off the screen and stop animation only if true
        timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                constructVoronoiStep();
                repaint();
                scaleIterations ++;
                // Limit iterations such that only intersections are found within the window area
                if (scaleIterations > maxScaleIterations) {
                    timer.stop();
                }
            }
        });
        timer.start();
    }
    
    
    
    /**
     * 
     * @param p1 A Point
     * @param p2 A Point
     * @return Angle p1,p2 makes with the x axis
     */
    private double calculateAngle(Point p1, Point p2) {
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
     * Find main bisector between all pairs of points
     * 
     * @param q Quadrilateral to iterate over
     * @param p1 A point in the point set
     * @param p2 A point in the point set
     */
    private void findBisectorOfTwoSites(Quadrilateral q, Point p1, Point p2) {
        double angle = calculateAngle(p1, p2); // Angle that slope(p1p2) makes with x axis
        
        System.out.println("Angle = " + Math.toDegrees(angle));
        Point a1 = new Point(), a2 = new Point();
        setLeftAndRightPoint(p1, p2, a1, a2, angle);
        System.out.println("left point : " + a1 + ", right point: " + a2);
        
        // Two "middle" vertices of quad wrt y value and angle
        Point[] innerVertices = findInnerVertices(q, angle);
        
        h1.add(new Point());
        h2.add(new Point());
        g1.add(new Point());
        g2.add(new Point());
        findh12g12(h1.get(h1.size()-1), h2.get(h1.size()-1), g1.get(h1.size()-1), g2.get(h1.size()-1), a1, a2, q, innerVertices, angle);
        System.out.println("h1 = " + h1 + ", h2 = " + h2);
        System.out.println("g1 = " + g1 + ", g2 = " + g2);
        
        // Endpoints of main bisector between p1 and p2
        Point h = doRaysIntersect(a1, h1.get(h1.size()-1), a2, h2.get(h2.size()-1));
        Point g = doRaysIntersect(a1, g1.get(g1.size()-1), a2, g2.get(g2.size()-1));
        
        System.out.println("Endpoints: " + h + ", " + g);
        this.voronoiEdges.add(new VoronoiBisector(h, g));
        
        // Find intersections between non-inner vertices
        Point[] nonInnerVertices = findNonInnerVertices(q, a1, a2, angle);
        
        Point[] hRay = findBisectorRay(h, a1, nonInnerVertices[0]);
        this.voronoiEdges.add(new VoronoiBisector(hRay[0], hRay[1]));
        
        Point[] gRay = findBisectorRay(g, a2, nonInnerVertices[1]);
        this.voronoiEdges.add(new VoronoiBisector(gRay[0], gRay[1]));
    }
    
    /**
     * Compute the midpoint of two points
     * 
     * @param p1 First point
     * @param p2 Second point
     * @return Midpoint of p1 and p2
     */
    private Point midpoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x)/2, (p1.y + p2.y)/2);
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
    private void setLeftAndRightPoint(Point p1, Point p2, Point left, Point right, double angle) {
        
        //System.out.println("Rotating " + p1 + " and " + p2 + " by " + Math.toDegrees(angle) + " degrees");
        Point r1 = rotatePoint(p1, midpoint(p1, p2), angle);
        Point r2 = rotatePoint(p2, midpoint(p1, p2), angle);
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
     * Compute slope of line segment
     * 
     * @param p1 Endpoint of line segment
     * @param p2 Endpoint of line segment
     * @return Slope of p1p2
     */
    private double slope(Point p1, Point p2) {
        //System.out.println("Slope(" + p1 + ", " + p2 + ") = (" + p2.y + " - " + p1.y + ") / (" + p2.x + " - " + p1.x + ") = " + (p2.y - p1.y) / (p2.x - p1.x));
        return (p2.y - p1.y) / (p2.x - p1.x);
    }
    
    /**
     * Rotate a point around a pivot point by an angle
     * 
     * @param pivotx X coordinate of pivot point
     * @param pivoty Y coordinate of pivot point
     * @param angle Rotation angle
     * @param p Point to rotate
     * @return New location of rotated point
     */
    private Point rotatePoint(Point p, Point pivot, double angle) {
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
     * Find the two vertices of a quad that do not have max or min y values wrt an angle
     * 
     * @param q A quadrilateral to iterate over
     * @param angle Angle to rotate quad by such that a1a2 is parallel to x axis
     * @return Array of inner vertices of size 2
     */
    private Point[] findInnerVertices(Quadrilateral q, double angle) {
        Point[] innerVerts = new Point[2], rVerts = new Point[4];
        System.out.print("Rotated quad: ");
        // Rotate all quad vertices
        for (int i = 0; i < 4; i ++) {
            rVerts[i] = rotatePoint(q.getVertices()[i], q.getCenter(), angle);
            System.out.print(rVerts[i] + " ");
        }
        System.out.println();
        
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
        
        innerVerts[0] = rVerts[1];
        innerVerts[1] = rVerts[2];
        
        //System.out.println("Inner verts: " + innerVerts[0] + " " + innerVerts[1]);
        return innerVerts;
    }
    
    /**
     * Find the two vertices of a quad that have max and min y values wrt an angle
     * 
     * @param q A quadrilateral to iterate over
     * @param angle Angle to rotate quad by such that a1a2 is parallel to x axis
     * @return Array of nonInner vertices of size 2
     */
    private Point[] findNonInnerVertices(Quadrilateral q, Point a1, Point a2, double angle) {
        Point[] nonInnerVerts = new Point[2], rVerts = new Point[4];
        
        // Rotate all quad vertices
        for (int i = 0; i < 4; i ++) {
            rVerts[i] = rotatePoint(q.getVertices()[i], midpoint(a1, a2), angle);
        }
        
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
        
        // Check for SL hitting an edge
        if (rVerts[0].y == rVerts[1].y && rVerts[0].x < rVerts[1].x) {
            nonInnerVerts[0] = rotatePoint(rVerts[1], midpoint(a1, a2), -angle);
        } else {
            nonInnerVerts[0] = rotatePoint(rVerts[0], midpoint(a1, a2), -angle);
        }
        
        if (rVerts[2].y == rVerts[3].y && rVerts[2].x > rVerts[3].x) {
            nonInnerVerts[1] = rotatePoint(rVerts[2], midpoint(a1, a2), -angle);
        } else {
            nonInnerVerts[1] = rotatePoint(rVerts[3], midpoint(a1, a2), -angle);
        }
        
        //System.out.println("nonInner verts: " + nonInnerVerts[0] + " " + nonInnerVerts[1]);
        return nonInnerVerts;
    }
    
    /**
     * Find intersection points of lines through inner vertices with the right side of the quad around the left point a1
     * 
     * @param h1 Will be assigned. Intersection point of line through upper inner vertex with right side of quad
     * @param g1 Will be assigned. Intersection point of line through lower inner vertex with right side of quad
     * @param h2 Will be assigned. Intersection point of line through upper inner vertex with left side of quad
     * @param g2 Will be assigned. Intersection point of line through lower inner vertex with left side of quad
     * @param a1 Left point
     * @param a1 Right point
     * @param q Quadrilateral to iterate over
     * @param innerVerts Array of size two holding the inner vertices on the quad
     * @param slope Slope of the lines through inner vertices
     */
    private void findh12g12(Point h1, Point h2, Point g1, Point g2, Point a1, Point a2, Quadrilateral q, Point[] innerVerts, double angle) {
        Point temph1 = null, temph2 = null, tempg1 = null, tempg2 = null;
        
        // If inner vertex is to the right of center of quad
        if (innerVerts[0].x > q.getCenter().x) {
            temph1 = innerVerts[0];
        } else {
            temph2 = innerVerts[0];
        }
        
        // If inner vertex is to the right of center of quad
        if (innerVerts[1].x > q.getCenter().x) {
            tempg1 = innerVerts[1];
        } else {
            tempg2 = innerVerts[1];
        }
        
        //System.out.println("temph1 = " + temph1 + ", temph2 = " + temph2);
        //System.out.println("tempg1 = " + tempg1 + ", tempg2 = " + tempg2);
        
        Point[] rVerts = new Point[4];
        // Rotate all quad vertices
        for (int i = 0; i < 4; i ++) {
            rVerts[i] = rotatePoint(q.getVertices()[i], q.getCenter(), angle);
        }
        
        // Horizontal lines going through the inner vertices
        Point[] l1 = {new Point(-1000000, innerVerts[0].y), new Point(1000000, innerVerts[0].y)};
        Point[] l2 = {new Point(-1000000, innerVerts[1].y), new Point(1000000, innerVerts[1].y)};
        
        // Find other h and g points and rotate quad back to its original place
        int j;
        for (int i = 0; i < 4; i ++) {
            if (i == 3) {
                j = 0;
            } else {
                j = i + 1;
            }
            Point intersectionPoint1;
            // Found an h
            if ((intersectionPoint1 = doLineSegmentsIntersect(l1[0], l1[1], rVerts[i], rVerts[j])) != null && !intersectionPoint1.equals(innerVerts[0])) {
                if (temph1 == null && intersectionPoint1.x > temph2.x) {
                    temph1 = intersectionPoint1;
                } else if (temph2 == null) {
                    temph2 = intersectionPoint1;
                }
            }
            
            Point intersectionPoint2;
            
            // found a g
            if ((intersectionPoint2 = doLineSegmentsIntersect(l2[0], l2[1], rVerts[i], rVerts[j])) != null && !intersectionPoint2.equals(innerVerts[1])) {
                if (tempg1 == null && intersectionPoint2.x > tempg2.x) {
                    tempg1 = intersectionPoint2;
                } else if (tempg2 == null) {
                    tempg2 = intersectionPoint2;
                }
            }
        }
        
        //System.out.println("temph1 = " + temph1 + ", temph2 = " + temph2);
        //System.out.println("tempg1 = " + tempg1 + ", tempg2 = " + tempg2);
        
        // Assert that temph1.x > temph2.x and tempg1.x > tempg2.x
        if (temph1.x < temph2.x) {
            Point temp = new Point(temph1.x, temph1.y);
            
            temph1.x = temph2.x;
            temph1.y = temph2.y;
            
            temph2.x = temp.x;
            temph2.y = temp.y;
        }
        if (tempg1.x < tempg2.x) {
            Point temp = new Point(tempg1.x, tempg1.y);
            
            tempg1.x = tempg2.x;
            tempg1.y = tempg2.y;
            
            tempg2.x = temp.x;
            tempg2.y = temp.y;
        }
        
        // Rotate points back to original coordinate system and translate to a1 and a2
        temph1 = rotatePoint(temph1, q.getCenter(), -angle);
        temph2 = rotatePoint(temph2, q.getCenter(), -angle);
        tempg1 = rotatePoint(tempg1, q.getCenter(), -angle);
        tempg2 = rotatePoint(tempg2, q.getCenter(), -angle);
        
        h1.x = a1.x + temph1.x - q.getCenter().x;
        h1.y = a1.y + temph1.y - q.getCenter().y;
        g1.x = a1.x + tempg1.x - q.getCenter().x;
        g1.y = a1.y + tempg1.y - q.getCenter().y;
        
        h2.x = a2.x + temph2.x - q.getCenter().x;
        h2.y = a2.y + temph2.y - q.getCenter().y;
        g2.x = a2.x + tempg2.x - q.getCenter().x;
        g2.y = a2.y + tempg2.y - q.getCenter().y;
        
    }
    
    /**
     * Determine point where rays through h1 and h2 and through g1 and g2 intersect
     * 
     * @param a1 Enpoint of new ray
     * @param h1 Point on new ray
     * @param a2 Enpoint of new ray
     * @param h2 Point on new ray
     * @return Intersection point of rays
     */
    private Point doRaysIntersect(Point a1, Point h1, Point a2, Point h2) {
        
        // Rotate a1h1 to be horizontal with x axis
        double angle = calculateAngle(a1, h1); // Angle that slope(a1h1) makes with x axis
        
        Point ra1 = rotatePoint(a1, midpoint(a1, h1), angle);
        Point rh1 = rotatePoint(h1, midpoint(a1, h1), angle);
        
        // Define the ray a1h1 and rotate back to original position
        double rayEndx1 = 1000000.0;
        double rayEndy1 = rh1.y;
        if (a1.x > h1.x) {
            rayEndx1 = -1000000.0;
        }
        
        //System.out.println("raya1h1 end = " + new Point(rayEndx1, rayEndy1));
        Point[] raya1h1 = new Point[2];
        if (a1.x == h1.x) {
            raya1h1[0] = new Point(a1.x, a1.y);
            raya1h1[1] = new Point(a1.x, (a1.y < h1.y) ? 1000000 : -1000000);
        } else {
            raya1h1[0] = rotatePoint(new Point(ra1.x, ra1.y), midpoint(a1, h1), -angle);
            raya1h1[1] = rotatePoint(new Point(rayEndx1, rayEndy1), midpoint(a1, h1), -angle);
        }
        
        if (!onlyShowMainBisectors) {
            this.voronoiEdges.add(new VoronoiBisector(raya1h1[0], raya1h1[1]));
        }
        
        // Rotate a2h2 to be horizontal with x axis
        if (a2.x == h2.x) {
            angle = Math.toRadians(-90);
            /*if (a2.y < h2.y) {
                angle = Math.toRadians(90);
            } else {
                angle = Math.toRadians(-90);
            }*/
        } else {
            angle = Math.atan((a2.y - h2.y) / (h2.x - a2.x));
        }
        
        Point ra2 = rotatePoint(a2, midpoint(a2, h2), angle);
        Point rh2 = rotatePoint(h2, midpoint(a2, h2), angle);
        
        // Define the ray a1h1 and rotate back to original position
        double rayEndx2 = 1000000.0;
        double rayEndy2 = rh2.y;
        if (a2.x > h2.x) {
            rayEndx2 = -1000000.0;
        }
        
        //System.out.println("raya2h2 end = " + new Point(rayEndx2, rayEndy2));
        Point[] raya2h2 = new Point[2];
        if (a2.x == h2.x) {
            raya2h2[0] = new Point(a2.x, a2.y);
            raya2h2[1] = new Point(a2.x, (a2.y < h2.y) ? 1000000 : -1000000);
        } else {
            raya2h2[0] = rotatePoint(new Point(ra2.x, ra2.y), midpoint(a2, h2), -angle);
            raya2h2[1] = rotatePoint(new Point(rayEndx2, rayEndy2), midpoint(a2, h2), -angle);
        }
        
        if (!onlyShowMainBisectors) {
            this.voronoiEdges.add(new VoronoiBisector(raya2h2[0], raya2h2[1]));
        }
        
        //System.out.println("comparing " + raya1h1[0] + ", " + raya1h1[1] + " and " + raya2h2[0] + ", " + raya2h2[1]);
        //System.out.println(slope(a1, h1) + " : " + slope(a2, h2));
        if (slope(a1, h1) == slope(a2, h2) || (slope(a1, h1) == Double.POSITIVE_INFINITY && slope(a2, h2) == Double.NEGATIVE_INFINITY) || (slope(a1, h1) == Double.NEGATIVE_INFINITY && slope(a2, h2) == Double.POSITIVE_INFINITY)) {
            System.out.println("\nHandling degenerate case for main bisector segment !!!");
            ra1 = rotatePoint(a1, midpoint(a1, a2), angle);
            ra2 = rotatePoint(a2, midpoint(a1, a2), angle);
            rh1 = rotatePoint(h1, midpoint(a1, a2), angle);
            rh2 = rotatePoint(h2, midpoint(a1, a2), angle);
            
            /*System.out.println("Points before 1st rotation: a1.x = " + a1.x + ", h1.x = " + h1.x + " a2.x = " + a2.x + ", h2.x = " + h2.x);
            System.out.println("Points before 2nd rotation: a1.x = " + ra1.x + ", h1.x = " + rh1.x + " a2.x = " + ra2.x + ", h2.x = " + rh2.x);
            System.out.println("Point before 2nd rotation: " + new Point((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y));
            System.out.println(rotatePoint(new Point((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y), midpoint(a1, a2), -angle));*/
            
            return rotatePoint(new Point((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y), midpoint(a1, a2), -angle);
        } else {
            return doLineSegmentsIntersect(raya1h1[0], raya1h1[1], raya2h2[0], raya2h2[1]);
        }
    }
    
    /**
     * Find a bisector ray from given bisector endpoint
     * 
     * @param endPt Endpoint of main bisector
     * @param a Point in a quad
     * @param nonInnerVertex A vertex of the quad with an extreme y value
     */
    private Point[] findBisectorRay(Point endPt, Point a, Point nonInnerVertex) {
        // NonInnerVertex is relative to Quadrilateral. Translate relative to a
        nonInnerVertex.x += a.x - this.quad.getCenter().x;
        nonInnerVertex.y += a.y - this.quad.getCenter().y;
        
        //System.out.println("endPt = " + endPt + ", a = " + a + ", nonInnerVertex = " + nonInnerVertex);
        
        // Define the direction of the ray starting at a
        int rayEndx = 1000000;
        //System.out.println(a + " : " + nonInnerVertex);
        if (a.x > nonInnerVertex.x || (a.x == nonInnerVertex.x && a.y < nonInnerVertex.y)) {
            rayEndx = -1000000;
        }
        Point rayEnd = new Point(rayEndx, a.y); // End point of ray which is basically + or - infinity
        
        double angle  = calculateAngle(a, nonInnerVertex); // Angle that slope(a, nonInnerVertex) makes with x axis
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Point[] ray = {new Point(a.x, a.y), rotatePoint(rayEnd, new Point(0,0), -angle)};
        
        //System.out.println("ray = " + ray[0] + ", " + ray[1]);
        
        //Translate ray so that it starts at endPt
        ray[0].x += endPt.x - a.x;
        ray[0].y += endPt.y - a.y;
        ray[1].x += endPt.x - a.x;
        ray[1].y += endPt.y - a.y;
        
        //this.voronoiEdges.add(new VoronoiBisector(ray[0], ray[1]));
        return new Point[]{ray[0], ray[1]};
    }
    
    
    
    
    
    /**
     * If it exists, find the bisector point between p1, p2, p3
     * 
     * @param q Quadrilateral around each point
     * @param p1 A point to find bisector of
     * @param p2 A point to find bisector of
     * @param p3 A point to find bisector of
     */
    private void findBisectorOfThreeSites(Quadrilateral q, Point p1, Point p2, Point p3) {
        caseBisectorBetween3Points(q, p1, p2, p3);
        int bisectorCase = caseBisectorBetween3Points(q, p1, p2, p3);
        
        // If case is 1, ignore. Means there is no bisector point
        if (bisectorCase == 2) {
            voronoiPoints.add(findIntersectionBisectors3Points(p1, p2, p3));
        } else if (bisectorCase == 3 && !pointsAreCollinear(p1, p2, p3)) {
            //BC(a1; a2; a3) is a polygonal chain completed with one ray at the end
        } else if (bisectorCase == 3 && pointsAreCollinear(p1, p2, p3)) {
            //BC(a1; a2; a3) consists of one or two cones
        }
    }
    
    /**
     * Case 1. If a3 lies in the interior of FG12 then BC(a1; a2; a3) is empty.
     * Case 2. If a3 lies in the complement of FG12 then BC(a1; a2; a3) consists of exactly
     * one point.
     * Case 3. Otherwise, a3 lies on the boundary of FG12. If a1, a2, a3 are not collinear
     * then BC(a1; a2; a3) is a polygonal chain completed with one ray at the end, else
     * BC(a1; a2; a3) consists of one or two cones.
     * 
     * @param q Quadrilateral around each point
     * @param a1 A point
     * @param a2 A point
     * @param a3 A point
     * @return Integer representing the case
     */
    private int caseBisectorBetween3Points(Quadrilateral q, Point a1, Point a2, Point a3) {
        
        Point[] uv = finduv(q, a1, a2); // Point[2] = {u, ray1+, ray2-, v, ray1, ray2}
        
        /* DEBUGGING
        a3 = new Point(uv[0].x,uv[0].y);
        //F12 U F21:
        // (a1,uv[0]), (uv[0],a2), (a2,uv[3]), (uv[3],a1)   // true:
        System.out.println(isLeftOfSegment(a1, uv[0], a3)); // -1
        System.out.println(isLeftOfSegment(a2, uv[0], a3)); //  1
        System.out.println(isLeftOfSegment(uv[3], a2, a3)); //  1
        System.out.println(isLeftOfSegment(uv[3], a1, a3)); // -1
        //G12:
        // (a1,uv[1]), (a1,uv[4])
        System.out.println(isLeftOfSegment(uv[1], a1, a3)); //  1
        System.out.println(isLeftOfSegment(a1, uv[4], a3)); //  1
        //G21:
        // (a2,uv[2]), (a2,uv[5])
        System.out.println(isLeftOfSegment(uv[2], a2, a3)); // -1
        System.out.println(isLeftOfSegment(a2, uv[5], a3)); // -1*/
        
        // Can replace case 1 being split into 3 parts
        /*if ((isLeftOfSegment(a1, uv[0], a3) == -1 &&
                isLeftOfSegment(a2, uv[0], a3) == 1 &&
                isLeftOfSegment(uv[3], a2, a3) == 1 &&
                isLeftOfSegment(uv[3],a1, a3) == -1) 
                ||
                (isLeftOfSegment(uv[1], a1, a3) == 1 &&
                isLeftOfSegment(a1,uv[4], a3) == 1) 
                ||
                (isLeftOfSegment(uv[2], a2, a3) == -1 &&
                isLeftOfSegment(a2,uv[5], a3) == -1)) {
            System.out.println("Point inside FG");
        }*/
        
        // Case 1 split into 3 parts for debugging
        if (isLeftOfSegment(a1, uv[0], a3) == -1 &&
                isLeftOfSegment(a2, uv[0], a3) == 1 &&
                isLeftOfSegment(uv[3], a2, a3) == 1 &&
                isLeftOfSegment(uv[3],a1, a3) == -1) 
        {
            System.out.println("Point inside F");
            return 1;
            
        } else if (isLeftOfSegment(uv[1], a1, a3) == 1 &&
                isLeftOfSegment(a1,uv[4], a3) == 1) 
        {
            System.out.println("Point inside G12");
            return 1;
            
        } else if (isLeftOfSegment(uv[2], a2, a3) == -1 &&
                isLeftOfSegment(a2,uv[5], a3) == -1) 
        {
            System.out.println("Point inside G21");
            return 1;
            
        } else if (isLeftOfSegment(a1, uv[0], a3) == 0 ||
                isLeftOfSegment(a2, uv[0], a3) == 0 ||
                isLeftOfSegment(uv[3], a2, a3) == 0 ||
                isLeftOfSegment(uv[3], a1, a3) == 0 ||
                isLeftOfSegment(uv[1], a1, a3) == 0 ||
                isLeftOfSegment(a1, uv[4], a3) == 0 ||
                isLeftOfSegment(uv[2], a2, a3) == 0 ||
                isLeftOfSegment(a2, uv[5], a3) == 0) 
        {
            System.out.println("Point on boundary");
            return 3;
        }
        
        return 2;
    }
    
    /**
     * NOTE: Point a should have less or equal x value to point b for sign to be correct
     * 
     * @param a Endpoint of line segment
     * @param b Endpoint of line segment
     * @param c Query point
     * @return +1 if point is left of line (ccw order), 0 if point is on line (collinear), -1 otherwise (cw order)
     */
    public int isLeftOfSegment(Point a, Point b, Point c){
        double cross = (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x);
        
        if (cross > 1) {
            return 1;
        } else if (cross == 0) {
            return 0;
        } else {
            return -1;
        }
   }
    
    /**
     * 
     * @param q Quadrilateral to find u and v for
     * @param a1 A center point
     * @param a2 A center point
     * @return Point array holding u and points representing its 2 rays and v and points representing its 2 rays respectively
     */
    private Point[] finduv(Quadrilateral q, Point a1, Point a2) {
        double angle = calculateAngle(a1, a2);
        
        Point[] td =  findNonInnerVertices(q, a1, a2, angle);
        
        Point[] u1 = find3PointUVRays(rotatePoint(td[0], midpoint(a1, a2), angle), rotatePoint(a1, midpoint(a1, a2), angle), rotatePoint(q.prevVertex(td[0]), midpoint(a1, a2), angle));
        //System.out.println("u1: " + td[0] + ", " + q.prevVertex(td[0]));
        Point[] u2 = find3PointUVRays(rotatePoint(td[0], midpoint(a1, a2), angle), rotatePoint(a2, midpoint(a1, a2), angle), rotatePoint(q.nextVertex(td[0]), midpoint(a1, a2), angle));
        //System.out.println("u2: " + td[0] + ", " + q.nextVertex(td[0]));
        Point[] v1 = find3PointUVRays(rotatePoint(td[1], midpoint(a1, a2), angle), rotatePoint(a1, midpoint(a1, a2), angle), rotatePoint(q.nextVertex(td[1]), midpoint(a1, a2), angle));
        //System.out.println("v1: " + td[1] + ", " + q.nextVertex(td[1]));
        Point[] v2 = find3PointUVRays(rotatePoint(td[1], midpoint(a1, a2), angle), rotatePoint(a2, midpoint(a1, a2), angle), rotatePoint(q.prevVertex(td[1]), midpoint(a1, a2), angle));
        //System.out.println("v2: " + td[1] + ", " + q.prevVertex(td[1]));
        
        Point u = doLineSegmentsIntersect(rotatePoint(u1[0], midpoint(a1, a2), -angle), rotatePoint(u1[1], midpoint(a1, a2), -angle), rotatePoint(u2[0], midpoint(a1, a2), -angle), rotatePoint(u2[1], midpoint(a1, a2), -angle));
        Point v = doLineSegmentsIntersect(rotatePoint(v1[0], midpoint(a1, a2), -angle), rotatePoint(v1[1], midpoint(a1, a2), -angle), rotatePoint(v2[0], midpoint(a1, a2), -angle), rotatePoint(v2[1], midpoint(a1, a2), -angle));
        
        // Draw lines for debugging
        this.voronoiEdges.add(new VoronoiBisector(u, rotatePoint(u1[3], midpoint(a1, a2), -angle)));
        this.voronoiEdges.add(new VoronoiBisector(u, rotatePoint(u2[3], midpoint(a1, a2), -angle)));
        this.voronoiEdges.add(new VoronoiBisector(v, rotatePoint(v1[3], midpoint(a1, a2), -angle)));
        this.voronoiEdges.add(new VoronoiBisector(v, rotatePoint(v2[3], midpoint(a1, a2), -angle)));
                
        return new Point[]{u, rotatePoint(u1[3], midpoint(a1, a2), -angle), rotatePoint(u2[3], midpoint(a1, a2), -angle), v, rotatePoint(v1[3], midpoint(a1, a2), -angle), rotatePoint(v2[3], midpoint(a1, a2), -angle)};
    }
    
    private Point[] find3PointUVRays(Point endPt, Point a, Point nextPt) {
        Point p1 = new Point(), p2 = new Point();
        // EndPt is relative to Quadrilateral. Translate relative to a
        p1.x = endPt.x + a.x - this.quad.getCenter().x;
        p1.y = endPt.y + a.y - this.quad.getCenter().y;
        
        // NonInnerVertex is relative to Quadrilateral. Translate relative to a
        p2.x = nextPt.x + a.x - this.quad.getCenter().x;
        p2.y = nextPt.y + a.y - this.quad.getCenter().y;
        
        //System.out.println("endPt = " + p1 + ", a = " + a + ", nextPt = " + p2);
        
        // Define the direction of the ray starting at a
        int rayEndx = 1000000;
        //System.out.println(a + " : " + nonInnerVertex);
        if (p1.x > p2.x || (p1.x == p2.x && p1.y < p2.y)) {
            rayEndx = -1000000;
        }
        Point rayEnd = new Point(rayEndx, a.y); // End point of ray which is basically + or - infinity
        Point rayEnd2 = new Point(-rayEndx, a.y);
        
        double angle = calculateAngle(p1, p2); // Angle that slope(a, nonInnerVertex) makes with x axis
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Point[] ray = {new Point(p1.x, p1.y), rotatePoint(rayEnd, new Point(0,0), -angle)};
        Point[] ray2 = {new Point(p1.x, p1.y), rotatePoint(rayEnd2, new Point(0,0), -angle)};
        
        //Translate ray so that it starts at a
        ray[0].x += a.x - p1.x;
        ray[0].y += a.y - p1.y;
        ray[1].x += a.x - p1.x;
        ray[1].y += a.y - p1.x;
        
        ray2[0].x += a.x - p1.x;
        ray2[0].y += a.y - p1.y;
        ray2[1].x += a.x - p1.x;
        ray2[1].y += a.y - p1.x;
        
        //System.out.println("ray = " + ray[0] + ", " + ray[1]);
        
        //this.voronoiEdges.add(new VoronoiBisector(ray[0], ray[1]));
        return new Point[]{ray[0], ray[1], ray2[0], ray2[1]};
    }
    
    /**
     * Find intersection points between the bisectors of 3 points
     * 
     * @param a1 A point
     * @param a2 A point
     * @param a3 A point
     * @return Intersection points between the 3 bisectors of the given points
     */
    private Point findIntersectionBisectors3Points(Point a1, Point a2, Point a3) {
        
        
        return new Point();
    }
    
    /**
     * Determine whether 3 points are collinear
     * 
     * @param p1 A point
     * @param p2 A point
     * @param p3 A point
     * @return True if points are collinear, false otherwise
     */
    private boolean pointsAreCollinear(Point p1, Point p2, Point p3) {
        return false;
    }
    
    
    
    
    
    
    

    /**
     * **Consider renaming method**
     * 
     * Construct Voronoi diagram for the point set using the quad
     * Find bisector rays on either side of previously found main bisector
     * 
     * 
     * Note: can replace animation by uncommenting the for loop. Consider renaming this method to constructVoronoi() then
     */
    private void constructVoronoiStep() {
        
        //for (int iterations = 0; iterations < 1000; iterations++) {
        this.curScale += 0.1;
        this.quad.scaleQuad(this.curScale);

        // for each pair of points, check for quad intersection
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                // Find and store intersections for current quad scaling
                findQuadIntersections(this.quad, this.points.get(i), this.points.get(j));
            }
        }

        //}
    }

    /**
     * Determine whether Quadrilateral q around two points has an intersection,
     * add to voronoiPoints
     *
     * @param q Reference quad
     * @param p1 First point
     * @param p2 Second point
     */
    public void findQuadIntersections(Quadrilateral q, Point p1, Point p2) {
        Point[] quad1 = q.getPixelVertsForPoint(p1, this.curScale, this.pixelFactor);
        Point[] quad2 = q.getPixelVertsForPoint(p2, this.curScale, this.pixelFactor);

        int k, l;
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                k = 0;
            } else {
                k = i + 1;
            }
            for (int j = 0; j < 4; j++) {
                if (j == 3) {
                    l = 0;
                } else {
                    l = j + 1;
                }
                //System.out.println("i = " + i + ", k = " + k + ", j = " + j + ", l = " + l);
                //System.out.println("Comparing line segments: (" + quad1[i].x + ", " + quad1[i].y + ") ("+ quad1[k].x + ", " + quad1[k].y + ") and (" + quad2[j].x + ", " + quad2[j].y + ") ("+ quad2[l].x + ", " + quad2[l].y + ")");
                Point intersectionPoint;
                if ((intersectionPoint = doLineSegmentsIntersect(quad1[i], quad1[k], quad2[j], quad2[l])) != null) {
                    //System.out.println("Found intersection at (" + intersectionPoint.x + ", " + intersectionPoint.y + ")");
                    this.voronoiPoints.add(intersectionPoint);
                }
            }
        }

    }

    /**
     * Determine whether two line segments intersect using vector cross product
     * approach Method outlined in http://stackoverflow.com/a/565282/786339
     *
     * @param p1 First point of first line segment
     * @param p2 Second point of first line segment
     * @param q1 First point of second line segment
     * @param q2Second point of second line segment
     * @return Intersection point if the line segments intersect, null otherwise
     */
    private Point doLineSegmentsIntersect(Point p1, Point p2, Point q1, Point q2) {
        Point r = subtractPoints(p2, p1);
        Point s = subtractPoints(q2, q1);

        double numerator = crossProduct(subtractPoints(q1, p1), r);
        double denominator = crossProduct(r, s);
        
        // Lines are collinear
        if (numerator == 0 && denominator == 0) {

            // If line segments share an endpoint, line segments intersect
            if (equalPoints(p1, q1) || equalPoints(p1, q2) || equalPoints(p2, q1) || equalPoints(p2, q2)) {
                Point intersection;
                if (equalPoints(p1, q1) || equalPoints(p1, q2)) {
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
        double t = crossProduct(subtractPoints(q1, p1), s) / denominator;
        
        // Lines are not parallel but intersect
        if ((t >= 0) && (t <= 1) && (u >= 0) && (u <= 1)) {
            Point intersection;
            r.x *= t;
            r.y *= t;
            intersection = addPoints(p1, r);
            //System.out.println("2Found intersection at (" + intersection.x + ", " + intersection.y + ")");
            return intersection;
        }

        return null;
    }

    /**
     * Take the cross product of two point objects
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Cross product of the two points
     */
    private double crossProduct(Point p1, Point p2) {
        return (p1.x * p2.y) - (p1.y * p2.x);
    }

    /**
     * Subtract the x and y values of point object from another
     *
     * @param p1 Point to be subtracted from
     * @param p2 Point to subtract other point by
     * @return Result of p1 - p2
     */
    private Point subtractPoints(Point p1, Point p2) {
        Point subP = new Point();
        subP.x = p1.x - p2.x;
        subP.y = p1.y - p2.y;

        return subP;
    }

    /**
     * Add the x and y values of two points
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Result of p1 + p2
     */
    private Point addPoints(Point p1, Point p2) {
        Point addP = new Point();
        addP.x = p1.x + p2.x;
        addP.y = p1.y + p2.y;

        return addP;
    }

    /**
     * Determine whether the x and y values of two points are both equal
     *
     * @param p1 First point to compare
     * @param p2 Second point to compare
     * @return True if point are equal, false otherwise
     */
    private boolean equalPoints(Point p1, Point p2) {
        return (Math.round(p1.x) == Math.round(p2.x)) && (Math.round(p1.y) == Math.round(p2.y));
    }

    /**
     * Determine whether an array of boolean values all have the same value
     *
     * @param arguments Array of boolean values
     * @return True if all array elements are the same, false otherwise
     */
    private boolean allEqual(boolean[] arguments) {
        boolean firstValue = arguments[0];

        for (int i = 1; i < arguments.length; i++) {
            if (arguments[i] != firstValue) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a window to draw the Voronoi diagram to the screen
     */
    private void createJFrame() {
        System.out.println("Drawing Voronoi diagram\n");

        // Set up display window
        JFrame window = new JFrame("Voronoi Diagram");
        window.setSize(800, 700);
        window.setResizable(false);
        window.setLocation(375, 25);
        window.getContentPane().setBackground(Color.BLACK);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = window.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this, BorderLayout.CENTER);
        window.setPreferredSize(new Dimension(800, 700));
        window.setLocationRelativeTo(null);
        window.pack();
        window.setVisible(true);
    }

    /**
     * Draws the Voronoi diagram to the window
     *
     * @param g Graphics object used to draw to the screen
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int pointRadius = 3, voronoiPointRadius = 1;
        int yMax = this.getBounds().getSize().height;

        Graphics2D g2d = (Graphics2D) g;

        // Draw points and quads
        for (Point p : this.points) {
            g2d.setColor(p.getColour());
            // Subtract pointRadius because points are drawn at coordinates from top left
            g2d.fill(new Ellipse2D.Double(p.x * this.pixelFactor - pointRadius, yMax - (p.y * this.pixelFactor + pointRadius), pointRadius * 2, pointRadius * 2)); // x, y, width, height
            quad.drawQuad(g2d, p, 1.0, this.pixelFactor, yMax); // Original quad
            quad.drawQuad(g2d, p, this.curScale, this.pixelFactor, yMax); // Scaled quad
        }

        g2d.setColor(Color.black);

        // Draw bisector ray points
        for (Point bisector : this.voronoiPoints) {
            g2d.fill(new Ellipse2D.Double(bisector.x * this.pixelFactor + voronoiPointRadius, yMax - (bisector.y * this.pixelFactor + voronoiPointRadius), voronoiPointRadius * 2, voronoiPointRadius * 2)); // x, y, width, height
        }
        
        // Draw bisector segments
        for (VoronoiBisector bisector : this.voronoiEdges) {
            g2d.drawLine((int)Math.round(bisector.startPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * this.pixelFactor), (int)Math.round(bisector.endPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * this.pixelFactor));
        }
        
        // Draw h12, g12 points on quads
        g2d.setColor(Color.red);
        for(int i = 0; i < h1.size(); i ++) {
            g2d.fill(new Ellipse2D.Double(h1.get(i).x - pointRadius, yMax - h1.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
            g2d.fill(new Ellipse2D.Double(h2.get(i).x - pointRadius, yMax - h2.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
            g2d.fill(new Ellipse2D.Double(g1.get(i).x - pointRadius, yMax - g1.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
            g2d.fill(new Ellipse2D.Double(g2.get(i).x - pointRadius, yMax - g2.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
        }
    }

}
