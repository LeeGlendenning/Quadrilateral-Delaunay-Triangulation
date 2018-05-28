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
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
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
    private final boolean timerOn;   // for starting and stopping animation
    private int scaleIterations;
    
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
        this.timerOn = true;
        this.scaleIterations = 0;
        this.h1 = new ArrayList();
        this.h2 = new ArrayList();
        this.g1 = new ArrayList();
        this.g2 = new ArrayList();
        createJFrame();
        //constructVoronoi();
        doVoronoiAnimation(5, 0);
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
     * Find main bisector between all pairs of points
     * 
     * @param q Quadrilateral to iterate over
     * @param p1 A point in the point set
     * @param p2 A point in the point set
     */
    private void findBisectorOfTwoSites(Quadrilateral q, Point p1, Point p2) {
        double angle; // Angle that slope(p1p2) makes with x axis
        if (p1.x == p2.x) {
            angle = 0;
        } else {
            angle = Math.atan((p1.y - p2.y) / (p2.x - p1.x));
        }
        
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
     * Find the two vertices of a quad that do not have max or min y values wrt a normal
     * 
     * @param q A quadrilateral to iterate over
     * @param normalSlope Slope of a normal defining the coordinate system
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
        System.out.print("Original quad: ");
        q.printVertices(q.getVertices());
        
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
        
        System.out.println("Inner verts: " + innerVerts[0] + " " + innerVerts[1]);
        return innerVerts;
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
        
        if (slope(q.getCenter(), innerVerts[0]) < 0) {
            temph1 = innerVerts[0];
        } else if (slope(q.getCenter(), innerVerts[0]) > 0) {
            temph2 = innerVerts[0];
        } else {
            System.err.println("!!! Slope of quad edge cannot be equal to slope a1a2 !!!");
            System.exit(1);
        }
        
        if (slope(q.getCenter(), innerVerts[1]) > 0) {
            tempg1 = innerVerts[1];
        } else if (slope(q.getCenter(), innerVerts[1]) < 0) {
            tempg2 = innerVerts[1];
        } else {
            System.err.println("!!! Slope of quad edge cannot be equal to slope a1a2 !!!");
            System.exit(1);
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
        for (int i = 0; i < 3; i ++) {
            if (i == 0) {
                j = 3;
            } else {
                j = i + 1;
            }
            Point intersectionPoint1;
            //found an h
            if ((intersectionPoint1 = doLineSegmentsIntersect(l1[0], l1[1], rVerts[i], rVerts[j])) != null && !intersectionPoint1.equals(innerVerts[0])) {
                //System.out.println("Found intersection for h");
                if (temph1 == null) {
                    temph1 = intersectionPoint1;
                } else {
                    temph2 = intersectionPoint1;
                }
            }
            
            Point intersectionPoint2;
            // found a g
            if ((intersectionPoint2 = doLineSegmentsIntersect(l2[0], l2[1], rVerts[i], rVerts[j])) != null && !intersectionPoint2.equals(innerVerts[1])) {
                if (tempg1 == null) {
                    tempg1 = intersectionPoint2;
                } else {
                    tempg2 = intersectionPoint2;
                }
            }
        }
        
        // Rotate points back to original coordinate system and translate to a1 and a2
        temph1 = rotatePoint(temph1, q.getCenter(), -angle);
        temph2 = rotatePoint(temph2, q.getCenter(), -angle);
        tempg1 = rotatePoint(tempg1, q.getCenter(), -angle);
        tempg2 = rotatePoint(tempg2, q.getCenter(), -angle);
        //System.out.println("h1 = " + h1 + ", h2 = " + h2);
        //System.out.println("g1 = " + g1 + ", g2 = " + g2);
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
     * @return 
     */
    private Point doRaysIntersect(Point a1, Point h1, Point a2, Point h2) {
        
        // Rotate a1h1 to be horizontal with x axis
        double angle; // Angle that slope(a1h1) makes with x axis
        if (a1.x == h1.x) {
            angle = 0;
        } else {
            angle = Math.atan((a1.y - h1.y) / (h1.x - a1.x));
        }
        
        Point ra1 = rotatePoint(a1, midpoint(a1, h1), angle);
        Point rh1 = rotatePoint(h1, midpoint(a1, h1), angle);
        
        // Define the ray a1h1 and rotate back to original position
        Point[] raya1h1 = {new Point(ra1.x, ra1.y), new Point(1000000, rh1.y)};
        raya1h1[0] = rotatePoint(raya1h1[0], midpoint(a1, h1), -angle);
        raya1h1[1] = rotatePoint(raya1h1[1], midpoint(a1, h1), -angle);
        
        this.voronoiEdges.add(new VoronoiBisector(raya1h1[0], raya1h1[1]));
        
        
        
        
        
        // Rotate a2h2 to be horizontal with x axis
        if (a2.x == h2.x) {
            angle = 0;
        } else {
            angle = Math.atan((a2.y - h2.y) / (h2.x - a2.x));
        }
        
        Point ra2 = rotatePoint(a2, midpoint(a2, h2), angle);
        Point rh2 = rotatePoint(h2, midpoint(a2, h2), angle);
        
        // Define the ray a1h1 and rotate back to original position
        Point[] raya2h2 = {new Point(ra2.x, ra2.y), new Point(-1000000, rh2.y)};
        raya2h2[0] = rotatePoint(raya2h2[0], midpoint(a2, h2), -angle);
        raya2h2[1] = rotatePoint(raya2h2[1], midpoint(a2, h2), -angle);
        
        this.voronoiEdges.add(new VoronoiBisector(raya2h2[0], raya2h2[1]));
        
        return doLineSegmentsIntersect(raya1h1[0], raya1h1[1], raya2h2[0], raya2h2[1]);
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
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                k = 3;
            } else {
                k = i + 1;
            }
            for (int j = 0; j < 3; j++) {
                if (j == 0) {
                    l = 3;
                } else {
                    l = j + 1;
                }
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
            quad.drawQuad(g2d, p, this.curScale, this.pixelFactor, yMax);
        }

        g2d.setColor(Color.black);

        // Draw bisector ray points
        for (Point bisector : this.voronoiPoints) {
            g2d.fill(new Ellipse2D.Double(bisector.x * this.pixelFactor + voronoiPointRadius, yMax - (bisector.y * this.pixelFactor + voronoiPointRadius), voronoiPointRadius * 2, voronoiPointRadius * 2)); // x, y, width, height
            
        }
        
        // Draw main bisectors
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
        //System.out.println("***********************");
    }

}
