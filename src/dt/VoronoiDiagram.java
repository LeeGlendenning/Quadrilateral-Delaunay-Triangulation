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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Constructs a Voronoi diagram from a point set using a Quadrilateral
 *
 * @author Lee Glendenning
 */
public class VoronoiDiagram extends JPanel {

    protected final List<Point> points, voronoiPoints; // voronoiPoints used for animation
    protected final Quadrilateral quad;
    // Consider using synchronized list to avoid concurrent modification...
    protected final List<VoronoiBisector> voronoiEdgesB3S, displayEdges;
    protected double curScale = 1.0;
    protected final int pixelFactor = 1;
    private int scaleIterations;
    private Timer timer;
    private final FindBisectorsTwoSites b2s;
    
    private final boolean showB2S_hgRegion = false, showB2S_hgPoints = false, showB2S_hiddenCones = true, showB2S = true;
    private final boolean showB3S_fgRegion = false, showB3S_hidden = true, showB3S = true;
    private final boolean doAnimation = false;
    
    //protected final ArrayList<Point> h1, h2, g1, g2;

    /**
     * Construct Voronoi diagram for point set using a Quadrilateral
     *
     * @param q Quadrilateral
     * @param p Point set
     */
    public VoronoiDiagram(Quadrilateral q, ArrayList<Point> p) {
        this.points = p;
        this.quad = q;
        
        this.voronoiEdgesB3S = Collections.synchronizedList(new ArrayList<VoronoiBisector>());
        this.displayEdges = Collections.synchronizedList(new ArrayList<VoronoiBisector>());
        this.voronoiPoints = Collections.synchronizedList(new ArrayList<Point>());
        this.scaleIterations = 0;
        
        b2s = new FindBisectorsTwoSites();
        
        createJFrame();
        //constructVoronoi();
        if (this.doAnimation) {
            doVoronoiAnimation(40, 1000, b2s);
        } else {
            doVoronoiAnimation(40, 0, b2s);
        }
    }
    
    /**
     * Animate quad scaling and intersection discovery
     */
    private void doVoronoiAnimation(int delay, int maxScaleIterations, FindBisectorsTwoSites b2s) {
        System.out.println("Finding Bisectors Between 2 Sites:\n");
        
        // For each pair of points, find bisector
        for (int i = 0; i < this.points.size(); i++) {
            for (int j = i + 1; j < this.points.size(); j++) {
                b2s.findBisectorOfTwoSites(this.quad, this.points.get(i), this.points.get(j));
                System.out.println();
            }
        }
        
        VoronoiBisector[] voronoiEdgesB2S = b2s.getVoronoiEdges();
        
        System.out.println("\nFinding Bisectors Between 3 Sites:\n");
        // For each triplet of points, find bisector
        for (int i = 0; i < this.points.size(); i ++) {
            for (int j = i + 1; j < this.points.size(); j++) {
                for (int k = j + 1; k < this.points.size(); k++) {
                    //System.out.println("i = " + i + ", j = " + j + ", k = " + k);
                    Point left = new Point(), right = new Point();
                    Utility.setLeftAndRightPoint(this.points.get(i), this.points.get(j), left, right, Utility.calculateAngle(this.points.get(i), this.points.get(j)));
                    findBisectorOfThreeSites(this.quad, voronoiEdgesB2S, left, right, this.points.get(k));
                    System.out.println();
                }
            }
        }
        
        System.out.println("Drawing minimum quads");
        calculateMinQuads();
        
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
     * If it exists, find the bisector point between p1, p2, p3
     * 
     * @param q Quadrilateral around each point
     * @param p1 A point to find bisector of
     * @param p2 A point to find bisector of
     * @param p3 A point to find bisector of
     */
    private void findBisectorOfThreeSites(Quadrilateral q, VoronoiBisector[] voronoiEdgesB2S, Point p1, Point p2, Point p3) {
        System.out.println("a1 = " + p1 + " a2 = " + p2 + " a3 = " + p3);
        int bisectorCase = caseBisectorBetween3Points(q, p1, p2, p3);
        
        // If case is 1, ignore. Means there is no bisector point
        if (bisectorCase == 2) { // case 2: single point is bisector of 3 
            VoronoiBisector bisector = findIntersectionB3S(voronoiEdgesB2S, p1, p2, p3);
            if (bisector != null) {
                this.voronoiEdgesB3S.add(bisector);
            } else {
                System.out.println("!!! case 2 bisector null - this shouldn't happen !!!");
            }
        } else if ((bisectorCase == 3 || bisectorCase == 4) && !Utility.isCollinear(p1, p2, p3)) {
            System.out.println("Handling case 3 - not collinear");
            //BC(a1; a2; a3) is a polygonal chain completed with one ray at the end
            boolean isReflected = false;
            if (bisectorCase == 4) {
                System.out.println("Reflecting B3S");
                isReflected = true;
            }
            
            ArrayList<VoronoiBisector> bisectors = findOverlapsB3S(voronoiEdgesB2S, p1, p2, p3, isReflected);
            if (!bisectors.isEmpty()) {
                for (VoronoiBisector bisector : bisectors) {
                    this.voronoiEdgesB3S.add(bisector);
                }
            } else {
                System.out.println("!!! case 3 bisector overlaps empty - this shouldn't happen !!!");
            }
        } else if (bisectorCase == 3 && Utility.isCollinear(p1, p2, p3)) {
            System.out.println("Handling case 3 - collinear");
            //BC(a1; a2; a3) consists of one or two cones
            ArrayList<VoronoiBisector[]> cones = findConeIntersectionsB3S(voronoiEdgesB2S, p1, p2, p3);
            if (!cones.isEmpty()) {
                for (VoronoiBisector[] cone : cones) {
                    this.voronoiEdgesB3S.add(cone[0]);
                    this.voronoiEdgesB3S.add(cone[1]);
                }
            } else {
                System.out.println("!!! case 3 bisector cone overlaps empty - this shouldn't happen !!!");
            }
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
        
        double angle = Utility.calculateAngle(a1, a2);
        
        // Check for degenerate case. FG consists of a line through a1a2
        if (segsParallelToa1a2(q, a1, a2, angle) == 2) { // FG12 is a line
            System.out.println("B3P Special case - two quad edges parallel to a1a2");

            Point[] ray1 = findB3SUVRays(a2, a1, a1); // Ray from a1 to left
            Point[] ray2 = findB3SUVRays(a1, a2, a2); // Ray from a2 to right

            this.displayEdges.add(new VoronoiBisector(new Point[]{}, a1, a2, "b3s_step"));
            this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray1[0], ray1[1], "b3s_step"));
            this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray2[0], ray2[1], "b3s_step"));

            if (Utility.isLeftOfSegment(a1, a2, a3, caseTolerance) == 0 ||
                    Utility.isLeftOfSegment(ray1[0], ray1[1], a3, caseTolerance) == 0 ||
                    Utility.isLeftOfSegment(ray2[0], ray2[1], a3, caseTolerance) == 0 ) {

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
                uv[0] = Utility.midpoint(a1, a2);
                uv[1] = ray2[1];
                uv[2] = ray1[1];
            } else if (uv[3] == null) {
                uv[3] = Utility.midpoint(a1, a2);
                uv[4] = ray2[1];
                uv[5] = ray1[1];
            }
        }
        // Non-degenerate case
        
        System.out.println(Arrays.toString(uv));
        
        // Case 1 split into 3 parts for debugging
        if (Utility.isLeftOfSegment(a1, uv[0], a3, caseTolerance) == -1 &&
                Utility.isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(uv[3],a1, a3, caseTolerance) == -1) 
        {
            System.out.println("Point inside F - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(a1,uv[4], a3, caseTolerance) == 1) 
        {
            System.out.println("Point inside G12 - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(uv[2], a2, a3, caseTolerance) == -1 &&
                Utility.isLeftOfSegment(a2,uv[5], a3, caseTolerance) == -1) 
        {
            System.out.println("Point inside G21 - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(a1, uv[0], a3, caseTolerance) == 0) {
            System.out.println("Point on boundary a1u - case 3 reflect B3S");
            return 4;
        } else if (Utility.isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[3], a1, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(a1, uv[4], a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[2], a2, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(a2, uv[5], a3, caseTolerance) == 0) 
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
            
            double tolerance = 0.00001;
            if (Utility.isParallel(q.getVertices()[i], q.getVertices()[j], a1, a2, tolerance)) {
                parallelCount ++;
            }
        }
        
        return parallelCount;
    }
    
    /**
     * 
     * @param q Quadrilateral to find u and v for
     * @param a1 A center point
     * @param a2 A center point
     * @return Point array holding u and points representing its 2 rays and v and points representing its 2 rays respectively
     */
    private Point[] finduv(Quadrilateral q, Point a1, Point a2) {
        double angle = Utility.calculateAngle(a1, a2);
        //System.out.print("finduv(): ");
        Point[] td = new Point[2];
        ArrayList<Point> niVerts = Utility.findNonInnerVertices(q, a1, a2, angle);
        
        switch (niVerts.size()) {
            case 2:
                td[0] = niVerts.get(0);
                td[1] = niVerts.get(1);
                break;
            case 3:
                double tolerance = 0.00001;
                if (Math.abs(Utility.rotatePoint(niVerts.get(0), Utility.midpoint(a1, a2), angle).y - Utility.rotatePoint(niVerts.get(1), Utility.midpoint(a1, a2), angle).y) < tolerance) {
                    if (niVerts.get(0).x > niVerts.get(1).x) {
                        td[0] = niVerts.get(0);
                    } else {
                        td[0] = niVerts.get(1);
                    }   
                    td[1] = niVerts.get(2);
                } else if (Math.abs(Utility.rotatePoint(niVerts.get(1), Utility.midpoint(a1, a2), angle).y - Utility.rotatePoint(niVerts.get(2), Utility.midpoint(a1, a2), angle).y) < tolerance) {
                    td[0] = niVerts.get(0);
                    if (niVerts.get(1).x > niVerts.get(2).x) {
                        td[1] = niVerts.get(1);
                    } else {
                        td[1] = niVerts.get(2);
                    }
                }
                break;
            case 4:
                if (Utility.rotatePoint(niVerts.get(0), Utility.midpoint(a1, a2), angle).x > Utility.rotatePoint(niVerts.get(1), Utility.midpoint(a1, a2), angle).x) {
                    td[0] = niVerts.get(0);
                } else {
                    td[0] = niVerts.get(1);
                }
                if (Utility.rotatePoint(niVerts.get(2), Utility.midpoint(a1, a2), angle).x > Utility.rotatePoint(niVerts.get(1), Utility.midpoint(a1, a2), angle).x) {
                    td[1] = niVerts.get(2);
                } else {
                    td[1] = niVerts.get(3);
                }
                break;
        }
        
        System.out.print("td vertices ");
        for (Point p : td) {
            System.out.print(p + " ");
        }
        System.out.println();
        
        Point[] u1 = findB3SUVRays(Utility.rotatePoint(td[0], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a1, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.prevVertex(td[0]), Utility.midpoint(a1, a2), angle));
        //System.out.println("u1: " + td[0] + ", " + q.prevVertex(td[0]));
        Point[] u2 = findB3SUVRays(Utility.rotatePoint(td[0], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a2, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.nextVertex(td[0]), Utility.midpoint(a1, a2), angle));
        //System.out.println("u2: " + td[0] + ", " + q.nextVertex(td[0]));
        
        double tolerance = 0.00001;
        Point[] v1;
        // Edge parallel to a1a2
        if (Math.abs(td[1].y - Utility.rotatePoint(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle).y) < tolerance) {
            System.out.println("Handling B3S triangle FG region");
            v1 = findB3SUVRays(Utility.rotatePoint(td[1], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a1, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle));
        } else {
            v1 = findB3SUVRays(Utility.rotatePoint(td[1], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a1, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.nextVertex(td[1]), Utility.midpoint(a1, a2), angle));
        }
        //System.out.println("v1: " + v1[0] + ", " + v1[1]);
        Point[] v2 = findB3SUVRays(Utility.rotatePoint(td[1], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a2, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle));
        //System.out.println("v2: " + td[1] + ", " + q.prevVertex(td[1]));
        
        Point u = Utility.doLineSegmentsIntersect(Utility.rotatePoint(u1[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(u1[1], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(u2[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(u2[1], Utility.midpoint(a1, a2), -angle));
        Point v = Utility.doLineSegmentsIntersect(Utility.rotatePoint(v1[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(v1[1], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(v2[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(v2[1], Utility.midpoint(a1, a2), -angle));
        //System.out.println("u = " + u + ", v = " + v);
        
        //below lines only for debugging when u or v is null (shouldn't happen)
        /*this.displayEdges.add(new VoronoiBisector(new Point[]{}, Utility.rotatePoint(u1[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(u1[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, Utility.rotatePoint(u2[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(u2[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, Utility.rotatePoint(v1[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(v1[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, Utility.rotatePoint(v2[0], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(v2[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        */
        
        // Draw FG region
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, u, Utility.rotatePoint(u1[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, u, Utility.rotatePoint(u2[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, v, Utility.rotatePoint(v1[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, v, Utility.rotatePoint(v2[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
                
        return new Point[]{u, Utility.rotatePoint(u1[3], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(u2[3], Utility.midpoint(a1, a2), -angle), v, Utility.rotatePoint(v1[3], Utility.midpoint(a1, a2), -angle), Utility.rotatePoint(v2[3], Utility.midpoint(a1, a2), -angle)};
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
        double rayEndx = Utility.RAY_SIZE;
        //System.out.println(a + " : " + nonInnerVertex);
        if (p1.x > p2.x || (p1.x == p2.x && p1.y > p2.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        Point rayEnd = new Point(rayEndx, a.y); // End point of ray which is basically + or - infinity
        Point rayEnd2 = new Point(-rayEndx, a.y);
        
        double angle = Utility.calculateAngle(p1, p2);
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Point[] ray = {new Point(p1.x, p1.y), Utility.rotatePoint(rayEnd, new Point(0,0), -angle)};
        Point[] ray2 = {new Point(p1.x, p1.y), Utility.rotatePoint(rayEnd2, new Point(0,0), -angle)};
        
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
        
        return new Point[]{ray[0], ray[1], ray2[0], ray2[1]};
    }
    
    /**
     * 
     * @param a1 A point
     * @param a2 A point
     * @param a3 A point
     * @return VoronoiBisector representing the intersection point between bisector of a1a3 and a2a3. case 2
     */
    private VoronoiBisector findIntersectionB3S(VoronoiBisector[] voronoiEdgesB2S, Point a1, Point a2, Point a3) {
        //System.out.println("a1 = " + a1 + " a2 = " + a2 + " a3 = " + a3 + ". # b2s = " + voronoiEdgesB2S.length);
        //printEdges(voronoiEdgesB2S);
        for (int i = 0; i < voronoiEdgesB2S.length; i ++) {
            
            // If the voronoi edge segment belongs to a1a3
            if (voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a1) &&
                    voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a3)) {
                
                //System.out.println("Considering " + voronoiEdgesB2S[i].getAdjacentPts().get(0) + " and " + voronoiEdgesB2S[i].getAdjacentPts().get(1));
                for (int j = 0; j < voronoiEdgesB2S.length; j ++) {
                    //System.out.println("Comparing with " + voronoiEdgesB2S[j].getAdjacentPts());
                    
                    //System.out.println("Considering " + voronoiEdgesB2S[j].getAdjacentPts().get(0) + " and " + voronoiEdgesB2S[j].getAdjacentPts().get(1));
                    // If the voronoi edge segment belongs to a2a3
                    if (voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a2) &&
                            voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a3)) {
                        
                        // Look for intersection between the 2 edge segments
                        Point b3s = Utility.doLineSegmentsIntersect(voronoiEdgesB2S[i].startPoint, voronoiEdgesB2S[i].endPoint, 
                                voronoiEdgesB2S[j].startPoint, voronoiEdgesB2S[j].endPoint);
                        if (b3s != null) {
                            System.out.println("Found intersection point: " + b3s);
                            return new VoronoiBisector(new Point[]{a1, a2, a3}, b3s, b3s, "b3s_chosen");
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
    private ArrayList<VoronoiBisector> findOverlapsB3S(VoronoiBisector[] voronoiEdgesB2S, Point a1, Point a2, Point a3, boolean isReflected) {
        ArrayList<VoronoiBisector> overlaps = new ArrayList();
        
        for (int i = 0; i < voronoiEdgesB2S.length; i ++) {
            //System.out.println("Considering " + voronoiEdgesB2S[i].getAdjacentPts().get(0) + " and " + voronoiEdgesB2S[i].getAdjacentPts().get(1));
            // If bisector belongs to a1a2, a1a3, or a2a3
            if (voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a1) && voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a2) ||
                    voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a1) && voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a3) ||
                    voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a2) && voronoiEdgesB2S[i].getAdjacentPtsArrayList().contains(a3)) {
                for (int j = i+1; j < voronoiEdgesB2S.length; j ++) {
                    //System.out.println("Considering " + voronoiEdgesB2S[j].getAdjacentPts().get(0) + " and " + voronoiEdgesB2S[j].getAdjacentPts().get(1));
                    // If bisector belongs to a1a2, a1a3, or a2a3
                    if (voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a1) && voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a2) ||
                            voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a1) && voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a3) ||
                            voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a2) && voronoiEdgesB2S[j].getAdjacentPtsArrayList().contains(a3)) {
                        Point[] overlap = doLineSegmentsOverlap(voronoiEdgesB2S[i].startPoint, voronoiEdgesB2S[i].endPoint, 
                                voronoiEdgesB2S[j].startPoint, voronoiEdgesB2S[j].endPoint);

                        if (overlap != null) {
                            //System.out.println("Found overlap: " + overlap[0] + ", " + overlap[1]);
                            Point chosenPt;
                            if (Math.abs(overlap[0].x) > 999 || Math.abs(overlap[0].y) > 999) {
                                chosenPt = overlap[1];
                            } else {
                                chosenPt = overlap[0];
                            }
                            
                            VoronoiBisector bisector = new VoronoiBisector(new Point[]{a1, a2, a3}, chosenPt, chosenPt, "b3s_chosen_overlap");
                            /*if (isReflected) {
                                bisector.setReflected(true);
                            }*/
                            this.voronoiEdgesB3S.add(bisector);
                            overlaps.add(new VoronoiBisector(new Point[]{a1, a2, a3}, overlap[0], overlap[1], "b3s_overlap"));
                        }
                    }
                }
            }
        }
        return overlaps;
    }
    
    /**
     * 
     * @param a1 A point
     * @param a2 A point
     * @param a3 A point
     * @return ArrayList of VoronoiBisector representing the overlapping cones between bisector of a1a3 and a2a3. case 3 collinear
     */
    private ArrayList<VoronoiBisector[]> findConeIntersectionsB3S(VoronoiBisector[] voronoiEdgesB2S, Point a1, Point a2, Point a3) {
        ArrayList<VoronoiBisector[]> coneIntersections = new ArrayList();
        ArrayList<VoronoiBisector[]> cones = getCones(voronoiEdgesB2S); // List of VoronoiBisector tuples representing cones
        
        for (int i = 0; i < cones.size(); i ++) {
            // If cone belongs to a1a2, a1a3, or a2a3
            if (cones.get(i)[0].getAdjacentPtsArrayList().contains(a1) && cones.get(i)[0].getAdjacentPtsArrayList().contains(a2) ||
                    cones.get(i)[0].getAdjacentPtsArrayList().contains(a1) && cones.get(i)[0].getAdjacentPtsArrayList().contains(a3) ||
                    cones.get(i)[0].getAdjacentPtsArrayList().contains(a2) && cones.get(i)[0].getAdjacentPtsArrayList().contains(a3)) {
                
                for (int j = i+1; j < cones.size(); j ++) {
                    // If cone belongs to a1a2, a1a3, or a2a3
                    if (cones.get(j)[0].getAdjacentPtsArrayList().contains(a1) && cones.get(j)[0].getAdjacentPtsArrayList().contains(a2) ||
                            cones.get(j)[0].getAdjacentPtsArrayList().contains(a1) && cones.get(j)[0].getAdjacentPtsArrayList().contains(a3) ||
                            cones.get(j)[0].getAdjacentPtsArrayList().contains(a2) && cones.get(j)[0].getAdjacentPtsArrayList().contains(a3)) {
                        
                        //System.out.println("Comparing " + cones.get(i)[0].startPoint + ", " + cones.get(i)[0].endPoint);
                        //System.out.println("and " + cones.get(i)[1].startPoint + ", " + cones.get(i)[1].endPoint);
                        //System.out.println("to " + cones.get(j)[0].startPoint + ", " + cones.get(j)[0].endPoint);
                        //System.out.println("and " + cones.get(j)[1].startPoint + ", " + cones.get(j)[1].endPoint);
                        VoronoiBisector[] coneIntersection;
                        if ((coneIntersection = doConesIntersect(cones.get(i), cones.get(j))) != null) {
                            // Add chosen B3S point to list (apex of cone)
                            Point chosenPt = Utility.doLineSegmentsIntersect(coneIntersection[0].startPoint, coneIntersection[0].endPoint, 
                                    coneIntersection[1].startPoint, coneIntersection[1].endPoint);
                            if (!pointIsInfinite(chosenPt)) {
                                Point[] adjacentUnion = Utility.pointArrayUnion(coneIntersection[0].getAdjacentPtsArray(), coneIntersection[1].getAdjacentPtsArray());
                                //System.out.println("Found cone intersection at " + chosenPt + "\n");
                                this.voronoiEdgesB3S.add(new VoronoiBisector(adjacentUnion, chosenPt, chosenPt, "b3s_chosen_cone"));

                                // Add entire cone to list for displaying
                                coneIntersections.add(coneIntersection);
                            }
                        }
                    }
                }
            }
        }
        
        return coneIntersections;
    }
    
    /**
     * 
     * @return ArrayList of cones represented by VoronoiBisector tuples
     */
    private ArrayList<VoronoiBisector[]> getCones(VoronoiBisector[] voronoiEdgesB2S) {
        ArrayList<VoronoiBisector[]> cones = new ArrayList();
        int index = 0; // Also coneID
        boolean isFirstRay = true;
        
        for (VoronoiBisector cone : voronoiEdgesB2S) {
            if (cone.getTag().contains("cone")) {
                String curConeID = cone.getTag().substring(cone.getTag().indexOf("=")+1, cone.getTag().length());
                
                if (Integer.parseInt(curConeID) == index) {
                    if (isFirstRay) {
                        cones.add(new VoronoiBisector[2]);
                        cones.get(index)[0] = cone;
                        //System.out.println("Adding cone " + curConeID + " at index " + index + " [0]");
                        //System.out.println(cone.startPoint + ", " + cone.endPoint);
                        isFirstRay = false;
                    } else {
                        cones.get(index)[1] = cone;
                        //System.out.println("Adding cone " + curConeID + " at index " + index + " [1]");
                        //System.out.println(cone.startPoint + ", " + cone.endPoint);
                        index ++;
                        isFirstRay = true;
                    }
                }
                
            }
        }
        
        return cones;
    }
    
    /**
     * 
     * @param cone1 VoronoiBisector array of size 2 representing a cone
     * @param cone2 VoronoiBisector array of size 2 representing a cone
     * @return VoronoiBisector array of size 2 representing the intersection of cone1 and cone2 if it exists, null otherwise
     */
    private VoronoiBisector[] doConesIntersect(VoronoiBisector[] cone1, VoronoiBisector[] cone2) {
        
        for (VoronoiBisector coneEdge1 : cone1) {
            
            for (VoronoiBisector coneEdge2 : cone2) {
                Point intersection;
                if ((intersection = Utility.doLineSegmentsIntersect(coneEdge1.getStartPoint(), coneEdge1.getEndPoint(), coneEdge2.getStartPoint(), coneEdge2.getEndPoint())) != null) {
                    return new VoronoiBisector[]{new VoronoiBisector(cone1[0].getAdjacentPtsArray(), intersection, coneEdge1.getEndPoint(), "b3s_cone"), 
                        new VoronoiBisector(cone2[0].getAdjacentPtsArray(), intersection, coneEdge2.getEndPoint(), "b3s_cone")};
                }
            }
        }
        
        return null;
    }
    
    /**
     * 
     * @param edges ArrayList of Voronoi Bisectors to print formatted
     */
    private void printVoronoiEdges(ArrayList<VoronoiBisector> edges) {
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
        Utility.setLeftAndRightPoint(p1, p2, pl, pr, Utility.calculateAngle(p1, p2));
        Utility.setLeftAndRightPoint(q1, q2, ql, qr, Utility.calculateAngle(q1, q2));
        //System.out.println("\nInitial points: " + pl + ", " + pr + " : " + ql + ", " + qr);
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

        double qlOverlap = Utility.isLeftOfSegment(pl, pr, ql, overlapTolerane);
        double qrOverlap = Utility.isLeftOfSegment(pl, pr, qr, overlapTolerane);

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

        double plOverlap = Utility.isLeftOfSegment(ql, qr, pl, overlapTolerane);
        double prOverlap = Utility.isLeftOfSegment(ql, qr, pr, overlapTolerane);

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
        double boundaryBuff = 1000;
        Point[] leftScreen = {new Point(-boundaryBuff, -boundaryBuff), new Point(-boundaryBuff, this.getBounds().getSize().height+boundaryBuff)};
        Point[] rightScreen = {new Point(this.getBounds().getSize().width+boundaryBuff, -boundaryBuff), new Point(this.getBounds().getSize().width+boundaryBuff, this.getBounds().getSize().height+boundaryBuff)};
        Point[] topScreen = {new Point(-boundaryBuff, this.getBounds().getSize().height+boundaryBuff), new Point(this.getBounds().getSize().width+boundaryBuff, this.getBounds().getSize().height+boundaryBuff)};
        Point[] bottomScreen = {new Point(-boundaryBuff, -boundaryBuff), new Point(this.getBounds().getSize().width+boundaryBuff, -boundaryBuff)};
        
        Point boundary;
        // Left side of screen will intersect ray
        if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, leftScreen[0], leftScreen[1])) != null) {
            //System.out.println("boundary point: " + boundary);
            return boundary;
        } else if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, rightScreen[0], rightScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary point: " + boundary);
            return boundary;        
        } else if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, topScreen[0], topScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary point: " + boundary);
            return boundary;        
        } else if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, bottomScreen[0], bottomScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary point: " + boundary);
            return boundary;        
        } else {
            System.out.println("!!! Could not find intersection of ray with screen boundary !!!");
            return null;
        }
    }        
            
    
    
    
    
    
    
    
    
    /**
     * Find the scaling for each of the minimum quads
     */
    private void calculateMinQuads() {
        for (VoronoiBisector chosenB3S : voronoiEdgesB3S) {
            Double scale;
            if (chosenB3S.getTag().contains("chosen") && (scale = findMinimumQuadScaling(this.quad, chosenB3S)) != null) {
                System.out.println("Scale = " + scale + "\n");
                chosenB3S.setMinQuadScale(scale);
            }
        }
    }
    
    /**
     * @param q Quadrilateral to scale through the adjacent B3S points
     * @param chosenB3S Chosen VoronoiBisector between 3 sites
     * @return Amount the quad needs to be scaled such that it goes through the adjacent B3S points
     */
    private Double findMinimumQuadScaling(Quadrilateral q, VoronoiBisector chosenB3S) {
        Point[] qVerts = q.getPixelVertsForPoint(chosenB3S.endPoint, curScale, pixelFactor/*, chosenB3S.isReflected()*/);
        System.out.println("qVerts for " + chosenB3S.endPoint);
        for (Point p : qVerts) {
            System.out.print(p + " ");
        }
        System.out.println();
        
        int ii;
        for (int i = 0; i < qVerts.length; i ++) {
            if (i == qVerts.length-1) {
                ii = 0;
            } else {
                ii = i+1;
            }
            
            Point[] ray1 = findMinQuadRay(chosenB3S.endPoint, chosenB3S.endPoint, qVerts[i]);
            Point[] ray2 = findMinQuadRay(chosenB3S.endPoint, chosenB3S.endPoint, qVerts[ii]);
            
            //Point[] adjB3S = chosenB3S.getAdjacentPtsArray();
            Point furthestAdjB3S = findFurthestPoint(chosenB3S.getAdjacentPtsArray(), chosenB3S.endPoint);
            //for (int j = 0; j < adjB3S.length; j ++) {
                // If chosenB3S is right of ray1 and left of ray2 (i.e. in between rays)
                //this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray1[0], ray1[1], "debug"));
                //this.displayEdges.add(new VoronoiBisector(new Point[]{}, ray2[0], ray2[1], "debug"));
                System.out.println("B3S at " + chosenB3S.endPoint);
                //System.out.println("Found point " + adjB3S[j] + " betwen Ray1 through " + qVerts[i] + ", Ray2 through " + qVerts[ii]);

                // Construct a ray in direction of edge closest to point and find intersection point with either ray1 or ray2
                // The distance between start of ray causing intersection and the intersection point is the scale

                Point[] intersectionRay = findMinQuadRay(furthestAdjB3S, qVerts[i], qVerts[ii]);
                //this.displayEdges.add(new VoronoiBisector(new Point[]{}, intersectionRay[0], intersectionRay[1], "debug"));
                //System.out.println("ray: " + intersectionRay[0] + ", " + intersectionRay[1]);
                Point scalePoint;
                if ((scalePoint = Utility.doLineSegmentsIntersect(intersectionRay[0], intersectionRay[1], ray1[0], ray1[1])) != null) {
                    System.out.println("Found scalePt: " + scalePoint);
                    //this.displayEdges.add(new VoronoiBisector(new Point[]{}, chosenB3S.endPoint, scalePoint, "debug"));
                    return Utility.euclideanDistance(scalePoint, chosenB3S.endPoint) / Utility.euclideanDistance(qVerts[i], chosenB3S.endPoint);
                } else if ((scalePoint = Utility.doLineSegmentsIntersect(intersectionRay[0], intersectionRay[1], ray2[0], ray2[1])) != null) {
                    System.out.println("Found scalePt: " + scalePoint);
                    //this.displayEdges.add(new VoronoiBisector(new Point[]{}, chosenB3S.endPoint, scalePoint, "debug"));
                    return Utility.euclideanDistance(scalePoint, chosenB3S.endPoint) / Utility.euclideanDistance(qVerts[ii], chosenB3S.endPoint);
                }
            //}
        }
        
        return null;
    }
    
    /**
     * 
     * @param pointSet Point array of which one point will be returned as furthest from the refPoint
     * @param refPoint Reference Point to find distance with pointSet
     * @return Point in pointSet having largest Euclidean distance to refPoint
     */
    public Point findFurthestPoint(Point[] pointSet, Point refPoint) {
        Point furthest = null;
        double furthestDist = -1;
        for (Point p : pointSet) {
            if (furthest == null) {
                furthest = p;
                furthestDist = Utility.euclideanDistance(p, refPoint);
            } else if (Utility.euclideanDistance(p, refPoint) > furthestDist) {
                furthest = p;
                furthestDist = Utility.euclideanDistance(p, refPoint);
            }
        }
        return furthest;
    }
    
    /**
     * Constructs a ray from startPt through throughPt then translated to translatePt
     * 
     * @param translatePt Point ray startPt will be translated to
     * @param startPt Initial start point of ray
     * @param throughPt Point the ray will pass through before being translated
     */
    private Point[] findMinQuadRay(Point translatePt, Point startPt, Point throughPt) {
        //System.out.println("endPt = " + endPt + ", a = " + a + ", nonInnerVertex = " + nonInnerVertex);
        
        // Define the direction of the ray starting at a
        double rayEndx = Utility.RAY_SIZE;
        //System.out.println(a + " : " + nonInnerVertex);
        if (startPt.x > throughPt.x || (startPt.x == throughPt.x && startPt.y > throughPt.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        Point rayEnd = new Point(rayEndx, startPt.y); // End point of ray which is basically + or - infinity
        
        double angle  = Utility.calculateAngle(startPt, throughPt); // Angle that slope(a, nonInnerVertex) makes with x axis
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Point[] ray = {new Point(startPt.x, startPt.y), Utility.rotatePoint(rayEnd, new Point(0,0), -angle)};
        
        //System.out.println("ray = " + ray[0] + ", " + ray[1]);
        
        //Translate ray so that it starts at endPt
        ray[0].x += translatePt.x - startPt.x;
        ray[0].y += translatePt.y - startPt.y;
        ray[1].x += translatePt.x - startPt.x;
        ray[1].y += translatePt.y - startPt.y;
        
        //this.voronoiEdges.add(new VoronoiBisector(ray[0], ray[1]));
        return new Point[]{ray[0], ray[1]};
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
        Point[] quad1 = q.getPixelVertsForPoint(p1, this.curScale, this.pixelFactor/*, false*/);
        Point[] quad2 = q.getPixelVertsForPoint(p2, this.curScale, this.pixelFactor/*, false*/);

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
                if ((intersectionPoint = Utility.doLineSegmentsIntersect(quad1[i], quad1[k], quad2[j], quad2[l])) != null) {
                    //System.out.println("Found intersection at (" + intersectionPoint.x + ", " + intersectionPoint.y + ")");
                    this.voronoiPoints.add(intersectionPoint);
                }
            }
        }

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
        Graphics2D g2d = (Graphics2D) g;
        Painter painter = new Painter(g2d, this);

        int pointRadius = 3, voronoiPointRadius = 1;
        int yMax = this.getBounds().getSize().height;

        
        painter.drawPointsAndQuads(g2d, yMax, pointRadius);

        g2d.setColor(Color.black);
        painter.drawBisectorRayPoints(g2d, yMax, voronoiPointRadius);
        
        // Draw bisector segments between 2 sites
        painter.drawB2S(g2d, b2s.getVoronoiEdges(), yMax, this.showB2S, this.showB2S_hiddenCones);
        
        // Draw bisector segments between 3 sites
        g2d.setStroke(new BasicStroke(5));
        painter.drawB3S(g2d, yMax, this.showB3S_hidden);
        
        g2d.setColor(Color.blue);
        painter.drawChosenB3SAndMinQuads(g2d, yMax, this.showB3S);
        
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(1));
        painter.drawDisplayEdges(g2d, yMax, this.showB2S_hgRegion, this.showB3S_fgRegion);
        
        painter.drawB2S_hgPoints(g2d, b2s.geth1(), b2s.geth2(), b2s.getg1(), b2s.getg2(), yMax, pointRadius, this.showB2S_hgPoints);
    }

}
