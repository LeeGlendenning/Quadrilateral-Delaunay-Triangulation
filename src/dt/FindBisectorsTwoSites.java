package dt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Lee Glendenning
 */
public class FindBisectorsTwoSites {
    
    private final ArrayList<Point> h1, h2, g1, g2;
    private final List<VoronoiBisector> voronoiEdgesB2S, displayEdges;
    private int coneID = 0;
    
    public FindBisectorsTwoSites() {
        this.h1 = new ArrayList();
        this.h2 = new ArrayList();
        this.g1 = new ArrayList();
        this.g2 = new ArrayList();
        
        this.voronoiEdgesB2S = Collections.synchronizedList(new ArrayList());
        // TODO: pass displayEdges to VoronoiDiagram so that they can be passed to Painter
        this.displayEdges = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * Find main bisector between all pairs of points
     * 
     * @param quad Quadrilateral to iterate over
     * @param p1 A point in the point set
     * @param p2 A point in the point set
     */
    public void findBisectorOfTwoSites(Quadrilateral quad, Point p1, Point p2) {
        System.out.println("\nFinding Bisector Between 2 Sites:");
        double angle = Utility.calculateAngle(p1, p2); // Angle that slope(p1p2) makes with x axis
        
        System.out.println("Angle = " + Math.toDegrees(angle));
        Point a1 = new Point(), a2 = new Point();
        Utility.setLeftAndRightPoint(p1, p2, a1, a2, angle);
        System.out.println("left point : " + a1 + ", right point: " + a2);
        
        // Two "middle" vertices of quad wrt y value and angle
        Point[] innerVertices = findInnerVertices(quad, angle);
        
        h1.add(new Point());
        h2.add(new Point());
        g1.add(new Point());
        g2.add(new Point());
        findh12g12(h1.get(h1.size()-1), h2.get(h1.size()-1), g1.get(h1.size()-1), g2.get(h1.size()-1), a1, a2, quad, innerVertices, angle);
        System.out.println("h1 = " + h1 + ", h2 = " + h2);
        System.out.println("g1 = " + g1 + ", g2 = " + g2);
        
        // Endpoints of main bisector between p1 and p2
        Point h = doRaysIntersect(a1, h1.get(h1.size()-1), a2, h2.get(h2.size()-1));
        Point g = doRaysIntersect(a1, g1.get(g1.size()-1), a2, g2.get(g2.size()-1));
        
        //System.out.println("Endpoints of main bisector segment: " + h + ", " + g);
        this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, h, g, "b2s_chosen"));
        
        // Find intersections between non-inner vertices
        ArrayList<Point> nonInnerVertices = Utility.findNonInnerVertices(quad, a1, a2, angle);
        
        calculateAllBisectorRays(nonInnerVertices, quad, h, g, a1, p1, p2, angle);
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
    private void calculateAllBisectorRays(ArrayList<Point> nonInnerVertices, Quadrilateral quad, Point h, Point g, Point a1, Point p1, Point p2, double angle) {
        ArrayList<Point> rNonInner = new ArrayList();
        for (Point niVert : nonInnerVertices) {
            rNonInner.add(Utility.rotatePoint(niVert, a1, angle));
        }
        
        // If SL hits edge there are 2 non-inner verts at that y height
        // The right-most non-inner vert is the "chosen" one and should
        // Only be shown. the left-most is stored for B3S calculations
        // But should not be displayed
        Point[] ray;
        if (nonInnerVertices.size() == 2) {
            ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
            ray = findBisectorRay(g, a1, nonInnerVertices.get(1), quad);
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
        }
        
        double tolerance = 0.00001;
        if (nonInnerVertices.size() == 3 && Math.abs(rNonInner.get(0).y - rNonInner.get(1).y) < tolerance) {
            if (rNonInner.get(0).x < rNonInner.get(1).x) {
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                this.coneID ++;
            }
            
            ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
        }
        
        if (nonInnerVertices.size() == 3 && Math.abs(rNonInner.get(1).y - rNonInner.get(2).y) < tolerance) {
            System.out.println("here");
            ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
            this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
            
            if (rNonInner.get(1).x < rNonInner.get(2).x) {
                ray = findBisectorRay(g, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(g, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                this.coneID ++;
            }
        }
        
        if (nonInnerVertices.size() == 4) {
            if (rNonInner.get(0).x < rNonInner.get(1).x) {
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                this.coneID ++;
            }
            
            if (rNonInner.get(2).x < rNonInner.get(3).x) {
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(3), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(3), quad);
                this.voronoiEdgesB2S.add(new VoronoiBisector(new Point[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                this.coneID ++;
            }
        }
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
            rVerts[i] = Utility.rotatePoint(q.getVertices()[i], q.getCenter(), angle);
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
            rVerts[i] = Utility.rotatePoint(q.getVertices()[i], q.getCenter(), angle);
        }
        
        // Horizontal lines going through the inner vertices
        Point[] l1 = {new Point(-Utility.RAY_SIZE, innerVerts[0].y), new Point(Utility.RAY_SIZE, innerVerts[0].y)};
        Point[] l2 = {new Point(-Utility.RAY_SIZE, innerVerts[1].y), new Point(Utility.RAY_SIZE, innerVerts[1].y)};
        
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
            if ((intersectionPoint1 = Utility.doLineSegmentsIntersect(l1[0], l1[1], rVerts[i], rVerts[j])) != null && !intersectionPoint1.equals(innerVerts[0])) {
                if (temph1 == null && intersectionPoint1.x > temph2.x) {
                    temph1 = intersectionPoint1;
                } else if (temph2 == null) {
                    temph2 = intersectionPoint1;
                }
            }
            
            Point intersectionPoint2;
            
            // found a g
            if ((intersectionPoint2 = Utility.doLineSegmentsIntersect(l2[0], l2[1], rVerts[i], rVerts[j])) != null && !intersectionPoint2.equals(innerVerts[1])) {
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
        temph1 = Utility.rotatePoint(temph1, q.getCenter(), -angle);
        temph2 = Utility.rotatePoint(temph2, q.getCenter(), -angle);
        tempg1 = Utility.rotatePoint(tempg1, q.getCenter(), -angle);
        tempg2 = Utility.rotatePoint(tempg2, q.getCenter(), -angle);
        
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
        double angle = Utility.calculateAngle(a1, h1); // Angle that slope(a1h1) makes with x axis
        
        Point ra1 = Utility.rotatePoint(a1, Utility.midpoint(a1, h1), angle);
        Point rh1 = Utility.rotatePoint(h1, Utility.midpoint(a1, h1), angle);
        
        // Define the ray a1h1 and rotate back to original position
        double rayEndx1 = Utility.RAY_SIZE;
        double rayEndy1 = rh1.y;
        if (a1.x > h1.x) {
            rayEndx1 = -Utility.RAY_SIZE;
        }
        
        //System.out.println("raya1h1 end = " + new Point(rayEndx1, rayEndy1));
        Point[] raya1h1 = new Point[2];
        if (a1.x == h1.x) {
            raya1h1[0] = new Point(a1.x, a1.y);
            raya1h1[1] = new Point(a1.x, (a1.y < h1.y) ? Utility.RAY_SIZE : -Utility.RAY_SIZE);
        } else {
            raya1h1[0] = Utility.rotatePoint(new Point(ra1.x, ra1.y), Utility.midpoint(a1, h1), -angle);
            raya1h1[1] = Utility.rotatePoint(new Point(rayEndx1, rayEndy1), Utility.midpoint(a1, h1), -angle);
        }
        
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, raya1h1[0], raya1h1[1], "b2s_step"));
        
        // Rotate a2h2 to be horizontal with x axis
        angle = Utility.calculateAngle(a2, h2);
        
        Point ra2 = Utility.rotatePoint(a2, Utility.midpoint(a2, h2), angle);
        Point rh2 = Utility.rotatePoint(h2, Utility.midpoint(a2, h2), angle);
        
        // Define the ray a1h1 and rotate back to original position
        double rayEndx2 = Utility.RAY_SIZE;
        double rayEndy2 = rh2.y;
        if (a2.x > h2.x) {
            rayEndx2 = -Utility.RAY_SIZE;
        }
        
        //System.out.println("raya2h2 end = " + new Point(rayEndx2, rayEndy2));
        Point[] raya2h2 = new Point[2];
        if (a2.x == h2.x) {
            raya2h2[0] = new Point(a2.x, a2.y);
            raya2h2[1] = new Point(a2.x, (a2.y < h2.y) ? Utility.RAY_SIZE : -Utility.RAY_SIZE);
        } else {
            raya2h2[0] = Utility.rotatePoint(new Point(ra2.x, ra2.y), Utility.midpoint(a2, h2), -angle);
            raya2h2[1] = Utility.rotatePoint(new Point(rayEndx2, rayEndy2), Utility.midpoint(a2, h2), -angle);
        }
        
        this.displayEdges.add(new VoronoiBisector(new Point[]{}, raya2h2[0], raya2h2[1], "b2s_step"));
        
        //System.out.println("comparing " + raya1h1[0] + ", " + raya1h1[1] + " and " + raya2h2[0] + ", " + raya2h2[1]);
        //System.out.println(slope(a1, h1) + " : " + slope(a2, h2));
        double tolerance = 0.00001;
        if (Math.abs(Utility.slope(a1, h1) - Utility.slope(a2, h2)) < tolerance || (Utility.slope(a1, h1) == Double.POSITIVE_INFINITY && Utility.slope(a2, h2) == Double.NEGATIVE_INFINITY) || (Utility.slope(a1, h1) == Double.NEGATIVE_INFINITY && Utility.slope(a2, h2) == Double.POSITIVE_INFINITY)) {
            System.out.println("Handling degenerate case for main bisector segment !!!");
            ra1 = Utility.rotatePoint(a1, Utility.midpoint(a1, a2), angle);
            ra2 = Utility.rotatePoint(a2, Utility.midpoint(a1, a2), angle);
            rh1 = Utility.rotatePoint(h1, Utility.midpoint(a1, a2), angle);
            rh2 = Utility.rotatePoint(h2, Utility.midpoint(a1, a2), angle);
            
            /*System.out.println("Points before 1st rotation: a1.x = " + a1.x + ", h1.x = " + h1.x + " a2.x = " + a2.x + ", h2.x = " + h2.x);
            System.out.println("Points before 2nd rotation: a1.x = " + ra1.x + ", h1.x = " + rh1.x + " a2.x = " + ra2.x + ", h2.x = " + rh2.x);
            System.out.println("Point before 2nd rotation: " + new Point((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y));
            System.out.println(rotatePoint(new Point((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y), midpoint(a1, a2), -angle));*/
            
            return Utility.rotatePoint(new Point((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y), Utility.midpoint(a1, a2), -angle);
        } else {
            return Utility.doLineSegmentsIntersect(raya1h1[0], raya1h1[1], raya2h2[0], raya2h2[1]);
        }
    }
    
    /**
     * Constructs a ray from a through nonInnerVertex then translated to endPt
     * 
     * @param endPt Endpoint of main bisector
     * @param a Point in a quad
     * @param nonInnerVertex A vertex of the quad with an extreme y value
     */
    private Point[] findBisectorRay(Point endPt, Point a, Point niVertex, Quadrilateral quad) {
        // NonInnerVertex is relative to Quadrilateral. Translate relative to a
        Point nonInnerVertex = new Point(niVertex.x, niVertex.y);
        nonInnerVertex.x += a.x - quad.getCenter().x;
        nonInnerVertex.y += a.y - quad.getCenter().y;
        
        //System.out.println("endPt = " + endPt + ", a = " + a + ", nonInnerVertex = " + nonInnerVertex);
        
        // Define the direction of the ray starting at a
        double rayEndx = Utility.RAY_SIZE;
        //System.out.println(a + " : " + nonInnerVertex);
        if (a.x > nonInnerVertex.x || (a.x == nonInnerVertex.x && a.y > nonInnerVertex.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        Point rayEnd = new Point(rayEndx, a.y); // End point of ray which is basically + or - infinity
        
        double angle = Utility.calculateAngle(a, nonInnerVertex); // Angle that slope(a, nonInnerVertex) makes with x axis
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Point[] ray = {new Point(a.x, a.y), Utility.rotatePoint(rayEnd, new Point(0,0), -angle)};
        
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
     * 
     * @return Deep copy of the VoronoiBisector List for B2S
     */
    public VoronoiBisector[] getVoronoiEdges() {
        return Utility.deepCopyVBArray(this.voronoiEdgesB2S.toArray(new VoronoiBisector[this.voronoiEdgesB2S.size()]));
    }
    
    /**
     * 
     * @return Deep copy of display edges as array
     */
    public List<VoronoiBisector> getDisplayEdges() {
        return (List)Utility.arrayToList(Utility.deepCopyVBArray(this.displayEdges.toArray(new VoronoiBisector[this.displayEdges.size()])));
    }
    
    /**
     * 
     * @return Deep copy array of h1 List
     */
    public Point[] geth1() {
        return Utility.deepCopyPointArray(this.h1.toArray(new Point[this.h1.size()]));
    }
    
    /**
     * 
     * @return Deep copy array of h2 List
     */
    public Point[] geth2() {
        return Utility.deepCopyPointArray(this.h2.toArray(new Point[this.h2.size()]));
    }
    
    /**
     * 
     * @return Deep copy array of g1 List
     */
    public Point[] getg1() {
        return Utility.deepCopyPointArray(this.g1.toArray(new Point[this.g1.size()]));
    }
    
    /**
     * 
     * @return Deep copy array of g2 List
     */
    public Point[] getg2() {
        return Utility.deepCopyPointArray(this.g2.toArray(new Point[this.g2.size()]));
    }
    
}
