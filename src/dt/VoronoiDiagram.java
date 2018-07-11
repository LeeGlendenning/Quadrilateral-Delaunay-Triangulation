package dt;

import java.awt.BasicStroke;
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
    // Consider using synchronized list to avoid concurrent modification...
    private final ArrayList<VoronoiBisector> voronoiEdgesB2S, voronoiEdgesB3S;
    private final ArrayList<VoronoiBisector> displayEdges; // Edges for showing steps in process
    private final ArrayList<Point> voronoiPoints; // Used for animation
    private double curScale = 1.0;
    private final int pixelFactor = 1;
    private Timer timer;
    private int scaleIterations;
    private final double floatTolerance = 0.0000000001;
    
    private final boolean showB2S_steps = false, showB2S_hg12 = false, showB3S_steps = false;
    private final boolean showB2S = true, showB3S = true, showB2S_hidden = false;
    private final boolean doAnimation = false;
    
    private final double raySize = 10000000;
    
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
        this.voronoiEdgesB2S = new ArrayList();
        this.voronoiEdgesB3S = new ArrayList();
        this.displayEdges = new ArrayList();
        this.voronoiPoints = new ArrayList();
        this.scaleIterations = 0;
        this.h1 = new ArrayList();
        this.h2 = new ArrayList();
        this.g1 = new ArrayList();
        this.g2 = new ArrayList();
        createJFrame();
        //constructVoronoi();
        if (this.doAnimation) {
            doVoronoiAnimation(40, 1000);
        } else {
            doVoronoiAnimation(40, 0);
        }
    }
    
    /**
     * Animate quad scaling and intersection discovery
     */
    private void doVoronoiAnimation(int delay, int maxScaleIterations) {
        System.out.println("Finding Bisectors Between 2 Sites:\n");
        // For each pair of points, find bisector
        for (int i = 0; i < this.points.size(); i++) {
            for (int j = i + 1; j < this.points.size(); j++) {
                findBisectorOfTwoSites(this.quad, this.points.get(i), this.points.get(j));
                System.out.println();
            }
        }
        
        System.out.println("\nFinding Bisectors Between 3 Sites:\n");
        // For each triplet of points, find bisector
        for (int i = 0; i < this.points.size(); i ++) {
            for (int j = i + 1; j < this.points.size(); j++) {
                for (int k = j + 1; k < this.points.size(); k++) {
                    //System.out.println("i = " + i + ", j = " + j + ", k = " + k);
                    Point left = new Point(), right = new Point();
                    setLeftAndRightPoint(this.points.get(i), this.points.get(j), left, right, calculateAngle(this.points.get(i), this.points.get(j)));
                    findBisectorOfThreeSites(this.quad, left, right, this.points.get(k));
                    System.out.println();
                }
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
        this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, h, g, "b2s"));
        
        // Find intersections between non-inner vertices
        ArrayList<Point> nonInnerVertices = findNonInnerVertices(q, a1, a2, angle);
        System.out.print("Non-inner vertices ");
        for (Point p : nonInnerVertices) {
            System.out.print(p + " ");
        }
        System.out.println();
        
        calculateAllBisectorRays(nonInnerVertices, h, g, a1, p1, p2, angle);
    }
    
    /**
     * 
     * @param nonInnerVertices ArrayList of non-inner vertices of the Quadrilateral
     * @param h Intersection point of h rays
     * @param g Intersection point of g rays
     * @param a1 A center point of the Quadrilateral
     * @param p1 An adjacent point of the bisector rays
     * @param p2 An adjacent point of the bisector rays
     */
    private void calculateAllBisectorRays(ArrayList<Point> nonInnerVertices, Point h, Point g, Point a1, Point p1, Point p2, double angle) {
        ArrayList<Point> rNonInner = new ArrayList();
        for (Point niVert : nonInnerVertices) {
            rNonInner.add(rotatePoint(niVert, a1, angle));
        }
        
        // If SL hits edge there are 2 non-inner verts at that y height
        // The right-most non-inner vert is the "chosen" one and should
        // Only be shown. the left-most is stored for B3S calculations
        // But should not be displayed
        Point[] ray;
        if (nonInnerVertices.size() == 2) {
            ray = findBisectorRay(h, a1, nonInnerVertices.get(0));
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
            ray = findBisectorRay(g, a1, nonInnerVertices.get(1));
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
        }
        
        if (nonInnerVertices.size() == 3 && rNonInner.get(0).y == rNonInner.get(1).y) {
            if (rNonInner.get(0).x < rNonInner.get(1).x) {
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
            } else{
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
            }
            
            ray = findBisectorRay(g, a1, nonInnerVertices.get(2));
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
        }
        
        if (nonInnerVertices.size() == 3 && rNonInner.get(1).y == rNonInner.get(2).y) {
            ray = findBisectorRay(h, a1, nonInnerVertices.get(0));
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
            
            if (rNonInner.get(1).x < rNonInner.get(2).x) {
                ray = findBisectorRay(g, a1, nonInnerVertices.get(1));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
            } else{
                ray = findBisectorRay(g, a1, nonInnerVertices.get(1));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
            }
        }
        
        if (nonInnerVertices.size() == 4) {
            if (rNonInner.get(0).x < rNonInner.get(1).x) {
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
            } else{
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
            }
            
            if (rNonInner.get(2).x < rNonInner.get(3).x) {
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(3));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
            } else{
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s"));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(3));
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden"));
            }
        }
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
    private ArrayList<Point> findNonInnerVertices(Quadrilateral q, Point a1, Point a2, double angle) {
        ArrayList<Point> nonInnerVerts = new ArrayList();
        Point[] rVerts = new Point[4];
        
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
        if (rVerts[0].y == rVerts[1].y /*&& rVerts[0].x < rVerts[1].x*/) {
            nonInnerVerts.add(rotatePoint(rVerts[0], midpoint(a1, a2), -angle));
            nonInnerVerts.add(rotatePoint(rVerts[1], midpoint(a1, a2), -angle));
        } else {
            nonInnerVerts.add(rotatePoint(rVerts[0], midpoint(a1, a2), -angle));
        }
        
        if (rVerts[2].y == rVerts[3].y /*&& rVerts[2].x > rVerts[3].x*/) {
            nonInnerVerts.add(rotatePoint(rVerts[2], midpoint(a1, a2), -angle));
            nonInnerVerts.add(rotatePoint(rVerts[3], midpoint(a1, a2), -angle));
        } else {
            nonInnerVerts.add(rotatePoint(rVerts[3], midpoint(a1, a2), -angle));
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
        Point[] l1 = {new Point(-this.raySize, innerVerts[0].y), new Point(this.raySize, innerVerts[0].y)};
        Point[] l2 = {new Point(-this.raySize, innerVerts[1].y), new Point(this.raySize, innerVerts[1].y)};
        
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
        double rayEndx1 = this.raySize;
        double rayEndy1 = rh1.y;
        if (a1.x > h1.x) {
            rayEndx1 = -this.raySize;
        }
        
        //System.out.println("raya1h1 end = " + new Point(rayEndx1, rayEndy1));
        Point[] raya1h1 = new Point[2];
        if (a1.x == h1.x) {
            raya1h1[0] = new Point(a1.x, a1.y);
            raya1h1[1] = new Point(a1.x, (a1.y < h1.y) ? this.raySize : -this.raySize);
        } else {
            raya1h1[0] = rotatePoint(new Point(ra1.x, ra1.y), midpoint(a1, h1), -angle);
            raya1h1[1] = rotatePoint(new Point(rayEndx1, rayEndy1), midpoint(a1, h1), -angle);
        }
        
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, raya1h1[0], raya1h1[1], "b2s_step"));
        
        // Rotate a2h2 to be horizontal with x axis
        angle = calculateAngle(a2, h2);
        
        Point ra2 = rotatePoint(a2, midpoint(a2, h2), angle);
        Point rh2 = rotatePoint(h2, midpoint(a2, h2), angle);
        
        // Define the ray a1h1 and rotate back to original position
        double rayEndx2 = this.raySize;
        double rayEndy2 = rh2.y;
        if (a2.x > h2.x) {
            rayEndx2 = -this.raySize;
        }
        
        //System.out.println("raya2h2 end = " + new Point(rayEndx2, rayEndy2));
        Point[] raya2h2 = new Point[2];
        if (a2.x == h2.x) {
            raya2h2[0] = new Point(a2.x, a2.y);
            raya2h2[1] = new Point(a2.x, (a2.y < h2.y) ? this.raySize : -this.raySize);
        } else {
            raya2h2[0] = rotatePoint(new Point(ra2.x, ra2.y), midpoint(a2, h2), -angle);
            raya2h2[1] = rotatePoint(new Point(rayEndx2, rayEndy2), midpoint(a2, h2), -angle);
        }
        
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, raya2h2[0], raya2h2[1], "b2s_step"));
        
        //System.out.println("comparing " + raya1h1[0] + ", " + raya1h1[1] + " and " + raya2h2[0] + ", " + raya2h2[1]);
        //System.out.println(slope(a1, h1) + " : " + slope(a2, h2));
        if (Math.abs(slope(a1, h1) - slope(a2, h2)) < this.floatTolerance || (slope(a1, h1) == Double.POSITIVE_INFINITY && slope(a2, h2) == Double.NEGATIVE_INFINITY) || (slope(a1, h1) == Double.NEGATIVE_INFINITY && slope(a2, h2) == Double.POSITIVE_INFINITY)) {
            System.out.println("Handling degenerate case for main bisector segment !!!");
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
        double rayEndx = this.raySize;
        //System.out.println(a + " : " + nonInnerVertex);
        if (a.x > nonInnerVertex.x || (a.x == nonInnerVertex.x && a.y > nonInnerVertex.y)) {
            rayEndx = -this.raySize;
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
        System.out.println("a1 = " + p1 + " a2 = " + p2 + " a3 = " + p3);
        int bisectorCase = caseBisectorBetween3Points(q, p1, p2, p3);
        
        // If case is 1, ignore. Means there is no bisector point
        if (bisectorCase == 2) { // case 2: single point is bisector of 3 
            VoronoiBisector bisector = findIntersectionB3S(p1, p2, p3);
            if (bisector != null) {
                this.voronoiEdgesB3S.add(bisector);
            } else {
                System.out.println("!!! case 2 bisector null - this shouldn't happen !!!");
            }
        } else if (bisectorCase == 3 && !isCollinear(p1, p2, p3)) {
            System.out.println("Handling case 3 - not collinear");
            //BC(a1; a2; a3) is a polygonal chain completed with one ray at the end
            ArrayList<VoronoiBisector> bisectors = findOverlapsB3S(p1, p2, p3);
            if (!bisectors.isEmpty()) {
                for (VoronoiBisector bisector : bisectors) {
                    this.voronoiEdgesB3S.add(bisector);
                }
            } else {
                System.out.println("!!! case 3 bisector overlaps empty - this shouldn't happen !!!");
            }
        } else if (bisectorCase == 3 && isCollinear(p1, p2, p3)) {
            System.out.println("Handling case 3 - not collinear");
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
        //System.out.println("caseBisectorBetween3Points: " + a1 + ", " + a2 + ", " + a3);
        //a3 = new Point(a1.x,a1.y);
        double caseTolerance = 0.01;
        
        double angle = calculateAngle(a1, a2);
        
        // Check for degenerate case. FG consists of a line through a1a2
        if (segsParallelToa1a2(q, a1, a2, angle) == 2) { // FG12 is a line
            System.out.println("B3P Special case - two quad edges parallel to a1a2");

            Point[] ray1 = findB3SUVRays(a2, a1, a1); // Ray from a1 to left
            Point[] ray2 = findB3SUVRays(a1, a2, a2); // Ray from a2 to right

            this.displayEdges.add(new VoronoiBisector(new Point[]{}, a1, a2, "b3s_step"));
            this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray1[0], ray1[1], "b3s_step"));
            this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray2[0], ray2[1], "b3s_step"));

            if (isLeftOfSegment(a1, a2, a3, caseTolerance) == 0 ||
                    isLeftOfSegment(ray1[0], ray1[1], a3, caseTolerance) == 0 ||
                    isLeftOfSegment(ray2[0], ray2[1], a3, caseTolerance) == 0 ) {

                System.out.println("Point on boundary - case 3 (degenerate case)");
                return 3;
            } else {
                System.out.println("Point not on boundary - case 2 (degenerate case)");
                return 2;
            }
        }
                
        Point[] uv = finduv(q, a1, a2); // Point[2] = {u, ray1+, ray2-, v, ray1+, ray2-}
        if (segsParallelToa1a2(q, a1, a2, angle) == 1) { // FG12 is a triangle
            System.out.println("B3P Special case - one quad edge parallel to a1a2");
            Point[] ray1 = findB3SUVRays(a2, a1, a1); // Ray from a1 to left
            Point[] ray2 = findB3SUVRays(a1, a2, a2); // Ray from a2 to right

            this.displayEdges.add(new VoronoiBisector(new Point[]{}, a1, a2, "b3s_step"));
            this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray1[0], ray1[1], "b3s_step"));
            this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray2[0], ray2[1], "b3s_step"));
            
            if (uv[0] == null) {
                uv[0] = midpoint(a1, a2);
                uv[1] = ray2[1];
                uv[2] = ray1[1];
            } else if (uv[3] == null) {
                uv[3] = midpoint(a1, a2);
                uv[4] = ray2[1];
                uv[5] = ray1[1];
            }
        }
        // Non-degenerate case
        
        System.out.println(Arrays.toString(uv));
        
        // Case 1 split into 3 parts for debugging
        if (isLeftOfSegment(a1, uv[0], a3, caseTolerance) == -1 &&
                isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 1 &&
                isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 1 &&
                isLeftOfSegment(uv[3],a1, a3, caseTolerance) == -1) 
        {
            System.out.println("Point inside F - case 1 (do nothing)");
            return 1;
            
        } else if (isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 1 &&
                isLeftOfSegment(a1,uv[4], a3, caseTolerance) == 1) 
        {
            System.out.println("Point inside G12 - case 1 (do nothing)");
            return 1;
            
        } else if (isLeftOfSegment(uv[2], a2, a3, caseTolerance) == -1 &&
                isLeftOfSegment(a2,uv[5], a3, caseTolerance) == -1) 
        {
            System.out.println("Point inside G21 - case 1 (do nothing)");
            return 1;
            
        } else if (isLeftOfSegment(a1, uv[0], a3, caseTolerance) == 0 ||
                isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 0 ||
                isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 0 ||
                isLeftOfSegment(uv[3], a1, a3, caseTolerance) == 0 ||
                isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 0 ||
                isLeftOfSegment(a1, uv[4], a3, caseTolerance) == 0 ||
                isLeftOfSegment(uv[2], a2, a3, caseTolerance) == 0 ||
                isLeftOfSegment(a2, uv[5], a3, caseTolerance) == 0) 
        {
            System.out.println("Point on boundary - case 3");
            return 3;
        }
        
        System.out.println("Point outside FG - case 2");
        return 2;
    }
    
    /**
     * 
     * @param q Quadrilateral to check parallelism with
     * @param a1 Point
     * @param a2 Point
     * @return True if 2 rotated line segments are parallel (within 10 decimal places) to a1a2, false otherwise
     */
    private int segsParallelToa1a2(Quadrilateral q, Point a1, Point a2, double angle) {
        int parallelCount = 0;
        
        int j;
        for (int i = 0; i < q.getVertices().length; i ++) {
            if (i == q.getVertices().length - 1) {
                j = 0;
            } else {
                j = i + 1;
            }
            
            if (isParallel(q.getVertices()[i], q.getVertices()[j], a1, a2)) {
                parallelCount ++;
            }
        }
        
        return parallelCount;
    }
    
    /**
     * 
     * @param p1 Endpoint of first line segment
     * @param p2 Endpoint of first line segment
     * @param p3 Endpoint of second line segment
     * @param p4 Endpoint of second line segment
     * @return True if p1p2 is parallel (has same slope within 10 decimal places) to p3p4
     */
    private boolean isParallel(Point p1, Point p2, Point p3, Point p4) {
        //System.out.println("p1 = " + p1 + "p2 = " + p2 + "p3 = " + p3 + "p4 = " + p4);
        //System.out.println("isparallel: " + calculateAngle(p1, p2) + " : " + calculateAngle(p3, p4) + " == " + (Math.abs(calculateAngle(p1, p2) - calculateAngle(p3, p4)) < this.floatTolerance));
        return Math.abs(calculateAngle(p1, p2) - calculateAngle(p3, p4)) < this.floatTolerance;
    }
    
    /**
     * 
     * @param a A Point
     * @param b A Point
     * @param c A Point
     * @return True if a, b, c are collinear. False otherwise
     */
    private boolean isCollinear(Point a, Point b, Point c) {
        return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x) == 0;
    }
    
    /**
     * NOTE: Point a should have less or equal x value to point b for sign to be correct
     * 
     * @param a Endpoint of line segment
     * @param b Endpoint of line segment
     * @param c Query point
     * @return +1 if point is left of line (ccw order), 0 if point is on line (collinear), -1 otherwise (cw order)
     */
    private int isLeftOfSegment(Point a, Point b, Point c, double tolerance){
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
     * @param q Quadrilateral to find u and v for
     * @param a1 A center point
     * @param a2 A center point
     * @return Point array holding u and points representing its 2 rays and v and points representing its 2 rays respectively
     */
    
    private Point[] finduv(Quadrilateral q, Point a1, Point a2) {
        double angle = calculateAngle(a1, a2);
        //System.out.print("finduv(): ");
        Point[] td = new Point[2];
        ArrayList<Point> niVerts = findNonInnerVertices(q, a1, a2, angle);
        
        switch (niVerts.size()) {
            case 2:
                td[0] = niVerts.get(0);
                td[1] = niVerts.get(1);
                break;
            case 3:
                if (niVerts.get(0).y == niVerts.get(1).y && niVerts.get(0).x > niVerts.get(1).x) {
                    td[0] = niVerts.get(0);
                } else {
                    td[0] = niVerts.get(1);
                }   
                td[1] = niVerts.get(2);
                break;
            case 4:
                if (niVerts.get(0).x > niVerts.get(1).x) {
                    td[0] = niVerts.get(0);
                } else {
                    td[0] = niVerts.get(1);
                }
                if (niVerts.get(2).x > niVerts.get(3).x) {
                    td[0] = niVerts.get(2);
                } else {
                    td[0] = niVerts.get(3);
                }
                break;
        }
        
        Point[] u1 = findB3SUVRays(rotatePoint(td[0], midpoint(a1, a2), angle), rotatePoint(a1, midpoint(a1, a2), angle), rotatePoint(q.prevVertex(td[0]), midpoint(a1, a2), angle));
        //System.out.println("u1: " + td[0] + ", " + q.prevVertex(td[0]));
        Point[] u2 = findB3SUVRays(rotatePoint(td[0], midpoint(a1, a2), angle), rotatePoint(a2, midpoint(a1, a2), angle), rotatePoint(q.nextVertex(td[0]), midpoint(a1, a2), angle));
        //System.out.println("u2: " + td[0] + ", " + q.nextVertex(td[0]));
        Point[] v1 = findB3SUVRays(rotatePoint(td[1], midpoint(a1, a2), angle), rotatePoint(a1, midpoint(a1, a2), angle), rotatePoint(q.nextVertex(td[1]), midpoint(a1, a2), angle));
        //System.out.println("v1: " + td[1] + ", " + q.nextVertex(td[1]));
        Point[] v2 = findB3SUVRays(rotatePoint(td[1], midpoint(a1, a2), angle), rotatePoint(a2, midpoint(a1, a2), angle), rotatePoint(q.prevVertex(td[1]), midpoint(a1, a2), angle));
        //System.out.println("v2: " + td[1] + ", " + q.prevVertex(td[1]));
        
        Point u = doLineSegmentsIntersect(rotatePoint(u1[0], midpoint(a1, a2), -angle), rotatePoint(u1[1], midpoint(a1, a2), -angle), rotatePoint(u2[0], midpoint(a1, a2), -angle), rotatePoint(u2[1], midpoint(a1, a2), -angle));
        Point v = doLineSegmentsIntersect(rotatePoint(v1[0], midpoint(a1, a2), -angle), rotatePoint(v1[1], midpoint(a1, a2), -angle), rotatePoint(v2[0], midpoint(a1, a2), -angle), rotatePoint(v2[1], midpoint(a1, a2), -angle));
        //System.out.println("u = " + u + ", v = " + v);
        
        //below lines only for debugging when u or v is null (shouldn't happen)
        /*this.displayEdges.add(new VoronoiBisector(new Point[]{}, rotatePoint(u1[0], midpoint(a1, a2), -angle), rotatePoint(u1[1], midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, rotatePoint(u2[0], midpoint(a1, a2), -angle), rotatePoint(u2[1], midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, rotatePoint(v1[0], midpoint(a1, a2), -angle), rotatePoint(v1[1], midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, rotatePoint(v2[0], midpoint(a1, a2), -angle), rotatePoint(v2[1], midpoint(a1, a2), -angle), "b3s_step"));
        */
        
        // Draw lines for debugging
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, u, rotatePoint(u1[3], midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, u, rotatePoint(u2[3], midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, v, rotatePoint(v1[3], midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, v, rotatePoint(v2[3], midpoint(a1, a2), -angle), "b3s_step"));
                
        return new Point[]{u, rotatePoint(u1[3], midpoint(a1, a2), -angle), rotatePoint(u2[3], midpoint(a1, a2), -angle), v, rotatePoint(v1[3], midpoint(a1, a2), -angle), rotatePoint(v2[3], midpoint(a1, a2), -angle)};
    }
    
    /**
     * 
     * @param endPt Initial endPt of ray
     * @param a Point to translate endPt to
     * @param nextPt Point initial ray passes through
     * @return Point array containing ray starting at endPt and passing through nextPt then translated to a
     */
    private Point[] findB3SUVRays(Point endPt, Point a, Point nextPt) {
        Point p1 = new Point(), p2 = new Point();
        // EndPt is relative to Quadrilateral. Translate relative to a
        p1.x = endPt.x + a.x - this.quad.getCenter().x;
        p1.y = endPt.y + a.y - this.quad.getCenter().y;
        
        // NonInnerVertex is relative to Quadrilateral. Translate relative to a
        p2.x = nextPt.x + a.x - this.quad.getCenter().x;
        p2.y = nextPt.y + a.y - this.quad.getCenter().y;
        
        //System.out.println("endPt = " + p1 + ", a = " + a + ", nextPt = " + p2);
        
        // Define the direction of the ray starting at a
        double rayEndx = this.raySize;
        //System.out.println(a + " : " + nonInnerVertex);
        if (p1.x > p2.x || (p1.x == p2.x && p1.y > p2.y)) {
            rayEndx = -this.raySize;
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
     * 
     * @param a1 A point
     * @param a2 A point
     * @param a3 A point
     * @return VoronoiBisector representing the intersection point between bisector of a1a3 and a2a3. case 2
     */
    private VoronoiBisector findIntersectionB3S(Point a1, Point a2, Point a3) {
        //System.out.println("a1 = " + a1 + " a2 = " + a2 + " a3 = " + a3 + ". # b2s = " + this.voronoiEdgesB2S.size());
        //printEdges(this.voronoiEdgesB2S);
        for (int i = 0; i < this.voronoiEdgesB2S.size(); i ++) {
            
            // If the voronoi edge segment belongs to a1a3
            if (this.voronoiEdgesB2S.get(i).getAdjacentPts().contains(a1) &&
                    this.voronoiEdgesB2S.get(i).getAdjacentPts().contains(a3)) {
                
                //System.out.println("Considering " + this.voronoiEdgesB2S.get(i).getAdjacentPts().get(0) + " and " + this.voronoiEdgesB2S.get(i).getAdjacentPts().get(1));
                for (int j = 0; j < this.voronoiEdgesB2S.size(); j ++) {
                    //System.out.println("Comparing with " + this.voronoiEdgesB2S.get(j).getAdjacentPts());
                    
                    //System.out.println("Considering " + this.voronoiEdgesB2S.get(j).getAdjacentPts().get(0) + " and " + this.voronoiEdgesB2S.get(j).getAdjacentPts().get(1));
                    // If the voronoi edge segment belongs to a2a3
                    if (this.voronoiEdgesB2S.get(j).getAdjacentPts().contains(a2) &&
                            this.voronoiEdgesB2S.get(j).getAdjacentPts().contains(a3)) {
                        
                        // Look for intersection between the 2 edge segments
                        Point b3s = doLineSegmentsIntersect(this.voronoiEdgesB2S.get(i).startPoint, this.voronoiEdgesB2S.get(i).endPoint, 
                                this.voronoiEdgesB2S.get(j).startPoint, this.voronoiEdgesB2S.get(j).endPoint);
                        if (b3s != null) {
                            System.out.println("Found intersection point: " + b3s);
                            return new VoronoiBisector(new Point[]{a1, a2, a3}, b3s, b3s, "b3s");
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 
     * @param a1 A point
     * @param a2 A point
     * @param a3 A point
     * @return ArrayList of VoronoiBisector representing the overlapping segments between bisector of a1a3 and a2a3. case 3 non-collinear
     */
    private ArrayList<VoronoiBisector> findOverlapsB3S(Point a1, Point a2, Point a3) {
        ArrayList<VoronoiBisector> overlaps = new ArrayList();
        
        for (int i = 0; i < this.voronoiEdgesB2S.size(); i ++) {
            //System.out.println("Considering " + this.voronoiEdgesB2S.get(i).getAdjacentPts().get(0) + " and " + this.voronoiEdgesB2S.get(i).getAdjacentPts().get(1));
            for (int j = i+1; j < this.voronoiEdgesB2S.size(); j ++) {
                //System.out.println("Considering " + this.voronoiEdgesB2S.get(j).getAdjacentPts().get(0) + " and " + this.voronoiEdgesB2S.get(j).getAdjacentPts().get(1));
                Point[] overlap = doLineSegmentsOverlap(this.voronoiEdgesB2S.get(i).startPoint, this.voronoiEdgesB2S.get(i).endPoint, 
                        this.voronoiEdgesB2S.get(j).startPoint, this.voronoiEdgesB2S.get(j).endPoint);
                
                if (overlap != null) {
                    System.out.println("Found overlap: " + overlap[0] + ", " + overlap[1]);
                    overlaps.add(new VoronoiBisector(new Point[]{a1, a2, a3}, overlap[0], overlap[1], "b3s"));
                }
            }
        }
        return overlaps;
    }
    
    
    /**
     * 
     * @param edges ArrayList of Voronoi Bisectors to print formatted
     */
    private void printEdges(ArrayList<VoronoiBisector> edges) {
        for (VoronoiBisector vb : edges) {
            System.out.println(" " + /*vb.adjacentPoints.get(0) + ", " + vb.adjacentPoints.get(1) + ": " +*/ vb.startPoint + ", " + vb.endPoint);
        }
    }
    
    /**
     * 
     * @param p1 Endpoint of first line segment P
     * @param p2 Endpoint of first line segment Q
     * @param q1 Endpoint of second line segment
     * @param q2 Endpoint of second line segment
     * @return Line segment representing overlap of P and Q
     */
    private Point[] doLineSegmentsOverlap(Point p1, Point p2, Point q1, Point q2) {
        if (p1.equals(p2) || q1.equals(q2)) {
            return null;
        }
        
        double overlapTolerane = 0.1;
        Point[] overlap = {null, null};
        
        Point pl = new Point(), pr = new Point(), ql = new Point(), qr = new Point();
        setLeftAndRightPoint(p1, p2, pl, pr, calculateAngle(p1, p2));
        setLeftAndRightPoint(q1, q2, ql, qr, calculateAngle(q1, q2));
        
        // Adjust ray endpoints to be at screen boundary
        if (pointIsInfinite(pl)) {
            pl = findBoundaryPointOnRay(pl, pr);
        }
        if (pointIsInfinite(pr)) {
            pr = findBoundaryPointOnRay(pl, pr);
        }
        if (pointIsInfinite(ql)) {
            ql = findBoundaryPointOnRay(ql, qr);
        }
        if (pointIsInfinite(qr)) {
            qr = findBoundaryPointOnRay(ql, qr);
        }

        //System.out.println("\nDoLineSegmentsOverlap: " + pl + ", " + pr + " : " + ql + ", " + qr);

        double qlOverlap = isLeftOfSegment(pl, pr, ql, overlapTolerane);
        double qrOverlap = isLeftOfSegment(pl, pr, qr, overlapTolerane);

        //System.out.println("qlOverlap = " + qlOverlap);
        //System.out.println("qrOverlap = " + qrOverlap);

        // No overlap
        if (qlOverlap != 0 && qrOverlap != 0) {
            //System.out.println("No overlap");
            return null;
        }

        // Left part of Q overlaps P
        if (qlOverlap == 0) {
            overlap[0] = ql;
        }
        // Right part of Q overlaps P
        if (qrOverlap == 0) {
            overlap[1] = qr;
        }

        double plOverlap = isLeftOfSegment(ql, qr, pl, overlapTolerane);
        double prOverlap = isLeftOfSegment(ql, qr, pr, overlapTolerane);

        // Left part of P overlaps Q
        if (plOverlap == 0) {
            overlap[0] = pl;
        }
        // Right part of P overlaps Q
        if (prOverlap == 0) {
            overlap[1] = pr;
        }
        
        if (overlap[0] == null || overlap[1] == null || overlap[0].equals(overlap[1])) {
            return null;
        } else {
            return overlap;
        }
    }
    
    /**
     * 
     * @param p A point
     * @return True if x or y of point is infinite or -infinite wrt screen size
     */
    private boolean pointIsInfinite(Point p) {
        double inf = 10000;
        return p.x > inf || p.y > inf || p.x < -inf || p.y < -inf;
    }
    
    /**
     * 
     * @param p1 Endpoint of ray represented as line segment
     * @param p2 Endpoint of ray represented as line segment
     * @return Point on line segment at boundary of screen
     */
    private Point findBoundaryPointOnRay(Point p1, Point p2) {
        Point[] leftScreen = {new Point(0, 0), new Point(0, this.getBounds().getSize().height)};
        Point[] rightScreen = {new Point(this.getBounds().getSize().width, 0), new Point(this.getBounds().getSize().width, this.getBounds().getSize().height)};
        Point[] topScreen = {new Point(0, this.getBounds().getSize().height), new Point(this.getBounds().getSize().width, this.getBounds().getSize().height)};
        Point[] bottomScreen = {new Point(0, 0), new Point(this.getBounds().getSize().width, 0)};
        
        Point boundary;
        // Left side of screen will intersect ray
        if ((boundary = doLineSegmentsIntersect(p1, p2, leftScreen[0], leftScreen[1])) != null) {
            //System.out.println("boundary point: " + boundary);
            return boundary;
        } else if ((boundary = doLineSegmentsIntersect(p1, p2, rightScreen[0], rightScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary point: " + boundary);
            return boundary;        
        } else if ((boundary = doLineSegmentsIntersect(p1, p2, topScreen[0], topScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary point: " + boundary);
            return boundary;        
        } else if ((boundary = doLineSegmentsIntersect(p1, p2, bottomScreen[0], bottomScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary point: " + boundary);
            return boundary;        
        } else {
            System.out.println("!!! Could not find intersection of ray with screen boundary !!!");
            return null;
        }
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
        //System.out.println("DoLineSegmentsOverlap: " + p1 + ", " + p2 + " : " + q1 + ", " + q2);
        Point r = subtractPoints(p2, p1);
        Point s = subtractPoints(q2, q1);

        double numerator = crossProduct(subtractPoints(q1, p1), r);
        double denominator = crossProduct(r, s);
        
        // Lines are collinear
        if (numerator == 0 && denominator == 0) {
            double tolerance = 0.01;
            // If line segments share an endpoint, line segments intersect
            if (equalPoints(p1, q1, tolerance) || equalPoints(p1, q2, tolerance) || equalPoints(p2, q1, tolerance) || equalPoints(p2, q2, tolerance)) {
                Point intersection;
                if (equalPoints(p1, q1, tolerance) || equalPoints(p1, q2, tolerance)) {
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
    private boolean equalPoints(Point p1, Point p2, double tolerance) {
        return (Math.abs(p1.x - p2.x) < tolerance && Math.abs(p1.y - p2.y) < tolerance);
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
        
        // Draw bisector segments between 2 sites
        for (VoronoiBisector bisector : this.voronoiEdgesB2S) {
            if (bisector.getTag().equals("b2s") && this.showB2S ||
                    bisector.getTag().equals("b2s_hidden") && this.showB2S_hidden){
                g2d.drawLine((int)Math.round(bisector.startPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * this.pixelFactor), (int)Math.round(bisector.endPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * this.pixelFactor));
            }
        }
        
        // Draw bisector segments between 3 sites
        g2d.setColor(Color.red);
        g2d.setStroke(new BasicStroke(5));
        for (VoronoiBisector bisector : this.voronoiEdgesB3S) {
            if (bisector.getTag().equals("b3s") && this.showB3S && bisector.startPoint != null && bisector.endPoint != null/*TODO: remove once bug is gone*/){
                g2d.drawLine((int)Math.round(bisector.startPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * this.pixelFactor), (int)Math.round(bisector.endPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * this.pixelFactor));
            }
        }
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(1));
        
        // Draw display edges
        for (VoronoiBisector bisector : this.displayEdges) {
            // TODO: sometimes the bisector start or end point are null and I don't know why
            if (bisector.startPoint != null && bisector.endPoint != null &&
                    (bisector.getTag().equals("b2s_step") && this.showB2S_steps ||
                    bisector.getTag().equals("b3s_step") && this.showB3S_steps)) {
                g2d.drawLine((int)Math.round(bisector.startPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * this.pixelFactor), (int)Math.round(bisector.endPoint.x * this.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * this.pixelFactor));
            }
        }
        
        
        // Draw h12, g12 points on quads
        if (this.showB2S_hg12) {
            g2d.setColor(Color.red);
            for(int i = 0; i < h1.size(); i ++) {
                g2d.fill(new Ellipse2D.Double(h1.get(i).x - pointRadius, yMax - h1.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(h2.get(i).x - pointRadius, yMax - h2.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(g1.get(i).x - pointRadius, yMax - g1.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(g2.get(i).x - pointRadius, yMax - g2.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
            }
        }
    }

}
