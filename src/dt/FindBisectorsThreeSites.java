package dt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lee Glendenning
 */
public class FindBisectorsThreeSites {
    
    List<VoronoiBisector> voronoiEdgesB3S, displayEdges;
    int height, width;
    
    public FindBisectorsThreeSites(int height, int width) {
        this.height = height;
        this.width = width;
        
        this.voronoiEdgesB3S = Collections.synchronizedList(new ArrayList());
        // TODO: pass displayEdges to VoronoiDiagram so that they can be passed to Painter
        this.displayEdges = Collections.synchronizedList(new ArrayList());
    }
    
    
    /**
     * If it exists, find the bisector point between p1, p2, p3
     * 
     * @param q Quadrilateral around each point
     * @param p1 A point to find bisector of
     * @param p2 A point to find bisector of
     * @param p3 A point to find bisector of
     */
    public void findBisectorOfThreeSites(Quadrilateral q, VoronoiBisector[] voronoiEdgesB2S, Point p1, Point p2, Point p3) {
        Utility.setLeftAndRightPoint(p1, p2, p1, p2, Utility.calculateAngle(p1, p2));
                    
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

            Point[] ray1 = findB3SUVRays(q, a2, a1, a1); // Ray from a1 to left
            Point[] ray2 = findB3SUVRays(q, a1, a2, a2); // Ray from a2 to right

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
            Point[] ray1 = findB3SUVRays(q, a2, a1, a1); // Ray from a1 to left
            Point[] ray2 = findB3SUVRays(q, a1, a2, a2); // Ray from a2 to right

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
        
        Point[] u1 = findB3SUVRays(q, Utility.rotatePoint(td[0], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a1, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.prevVertex(td[0]), Utility.midpoint(a1, a2), angle));
        //System.out.println("u1: " + td[0] + ", " + q.prevVertex(td[0]));
        Point[] u2 = findB3SUVRays(q, Utility.rotatePoint(td[0], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a2, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.nextVertex(td[0]), Utility.midpoint(a1, a2), angle));
        //System.out.println("u2: " + td[0] + ", " + q.nextVertex(td[0]));
        
        double tolerance = 0.00001;
        Point[] v1;
        // Edge parallel to a1a2
        if (Math.abs(td[1].y - Utility.rotatePoint(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle).y) < tolerance) {
            System.out.println("Handling B3S triangle FG region");
            v1 = findB3SUVRays(q, Utility.rotatePoint(td[1], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a1, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle));
        } else {
            v1 = findB3SUVRays(q, Utility.rotatePoint(td[1], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a1, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.nextVertex(td[1]), Utility.midpoint(a1, a2), angle));
        }
        //System.out.println("v1: " + v1[0] + ", " + v1[1]);
        Point[] v2 = findB3SUVRays(q, Utility.rotatePoint(td[1], Utility.midpoint(a1, a2), angle), Utility.rotatePoint(a2, Utility.midpoint(a1, a2), angle), Utility.rotatePoint(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle));
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
    private Point[] findB3SUVRays(Quadrilateral quad, Point endPt, Point a, Point nextPt) {
        Point p1 = new Point(), p2 = new Point();
        // EndPt is relative to Quadrilateral. Translate relative to a
        p1.x = endPt.x + a.x - quad.getCenter().x;
        p1.y = endPt.y + a.y - quad.getCenter().y;
        
        // NonInnerVertex is relative to Quadrilateral. Translate relative to a
        p2.x = nextPt.x + a.x - quad.getCenter().x;
        p2.y = nextPt.y + a.y - quad.getCenter().y;
        
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
                        Point b3s = Utility.doLineSegmentsIntersect(voronoiEdgesB2S[i].getStartPoint(), voronoiEdgesB2S[i].getEndPoint(), 
                                voronoiEdgesB2S[j].getStartPoint(), voronoiEdgesB2S[j].getEndPoint());
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
                        Point[] overlap = doLineSegmentsOverlap(voronoiEdgesB2S[i].getStartPoint(), voronoiEdgesB2S[i].getEndPoint(), 
                                voronoiEdgesB2S[j].getStartPoint(), voronoiEdgesB2S[j].getEndPoint());

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
                        
                        //System.out.println("Comparing " + cones.get(i)[0].getStartPoint() + ", " + cones.get(i)[0].getEndPoint());
                        //System.out.println("and " + cones.get(i)[1].getStartPoint() + ", " + cones.get(i)[1].getEndPoint());
                        //System.out.println("to " + cones.get(j)[0].getStartPoint() + ", " + cones.get(j)[0].getEndPoint());
                        //System.out.println("and " + cones.get(j)[1].getStartPoint() + ", " + cones.get(j)[1].getEndPoint());
                        VoronoiBisector[] coneIntersection;
                        if ((coneIntersection = doConesIntersect(cones.get(i), cones.get(j))) != null) {
                            // Add chosen B3S point to list (apex of cone)
                            Point chosenPt = Utility.doLineSegmentsIntersect(coneIntersection[0].getStartPoint(), coneIntersection[0].getEndPoint(), 
                                    coneIntersection[1].getStartPoint(), coneIntersection[1].getEndPoint());
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
                        //System.out.println(cone.getStartPoint() + ", " + cone.getEndPoint());
                        isFirstRay = false;
                    } else {
                        cones.get(index)[1] = cone;
                        //System.out.println("Adding cone " + curConeID + " at index " + index + " [1]");
                        //System.out.println(cone.getStartPoint() + ", " + cone.getEndPoint());
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
            System.out.println(" " + /*vb.adjacentPoints.get(0) + ", " + vb.adjacentPoints.get(1) + ": " +*/ vb.getStartPoint() + ", " + vb.getEndPoint());
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
        Point[] leftScreen = {new Point(-boundaryBuff, -boundaryBuff), new Point(-boundaryBuff, this.height+boundaryBuff)};
        Point[] rightScreen = {new Point(this.width+boundaryBuff, -boundaryBuff), new Point(this.width+boundaryBuff, this.height+boundaryBuff)};
        Point[] topScreen = {new Point(-boundaryBuff, this.height+boundaryBuff), new Point(this.width+boundaryBuff, this.height+boundaryBuff)};
        Point[] bottomScreen = {new Point(-boundaryBuff, -boundaryBuff), new Point(this.width+boundaryBuff, -boundaryBuff)};
        
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
     * 
     * @return Deep copy of the VoronoiBisector List for B2S
     */
    public VoronoiBisector[] getVoronoiEdges() {
        return Utility.deepCopyVBArray(this.voronoiEdgesB3S.toArray(new VoronoiBisector[this.voronoiEdgesB3S.size()]));
    }
    
    /**
     * 
     * @return Deep copy array of chosen bisectors between 3 points
     */
    public VoronoiBisector[] getChosenBisectors() {
        List<VoronoiBisector> chosenBisectors = new ArrayList();
        for (VoronoiBisector chosenB3S : this.voronoiEdgesB3S) {
            if (chosenB3S.getTag().contains("chosen")) {
                chosenBisectors.add(chosenB3S);
            }
        }
        return Utility.deepCopyVBArray(chosenBisectors.toArray(new VoronoiBisector[chosenBisectors.size()]));
    }
    
}
