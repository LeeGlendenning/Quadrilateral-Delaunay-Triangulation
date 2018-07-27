package dt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lee Glendenning
 */
public class FindBisectorsThreeSites {
    
    List<Bisector> voronoiEdgesB3S, displayEdges;
    int height, width;
    
    public FindBisectorsThreeSites(int height, int width) {
        this.height = height;
        this.width = width;
        
        this.voronoiEdgesB3S = Collections.synchronizedList(new ArrayList());
        // TODO: pass displayEdges to VoronoiDiagram so that they can be passed to Painter
        this.displayEdges = Collections.synchronizedList(new ArrayList());
    }
    
    
    /**
     * If it exists, find the bisector vertex between p1, p2, p3
     * 
     * @param q Quadrilateral around each vertex
     * @param voronoiEdgesB2S Array of B2S line segments
     * @param p1 A vertex to find bisector of
     * @param p2 A vertex to find bisector of
     * @param p3 A vertex to find bisector of
     */
    public void findBisectorOfThreeSites(Quadrilateral q, Bisector[] voronoiEdgesB2S, Vertex p1, Vertex p2, Vertex p3) {
        System.out.println("\nFinding Bisector Between 3 sites:");
        Vertex pLeft = new Vertex(), pRight = new Vertex();
        Utility.setLeftAndRightVertex(p1, p2, pLeft, pRight, Utility.calculateAngle(p1, p2));
                    
        System.out.println("a1 = " + pLeft + " a2 = " + pRight + " a3 = " + p3);
        int bisectorCase = caseBisectorBetween3Vertices(q, pLeft, pRight, p3);
        
        // If case is 1, ignore. Means there is no bisector vertex
        if (bisectorCase == 2) { // case 2: single vertex is bisector of 3 
            Bisector bisector = findIntersectionB3S(voronoiEdgesB2S, pLeft, pRight, p3);
            if (bisector != null) {
                this.voronoiEdgesB3S.add(bisector);
            } else {
                System.out.println("!!! case 2 bisector null - this shouldn't happen !!!");
            }
        } else if (bisectorCase == 3 && !Utility.isCollinear(pLeft, pRight, p3)) {
            System.out.println("Handling case 3 - not collinear");
            //BC(a1; a2; a3) is a polygonal chain completed with one ray at the end
            
            
            ArrayList<Bisector> bisectors = findOverlapsB3S(voronoiEdgesB2S, pLeft, pRight, p3);
            if (!bisectors.isEmpty()) {
                for (Bisector bisector : bisectors) {
                    this.voronoiEdgesB3S.add(bisector);
                }
            } else {
                System.out.println("!!! case 3 bisector overlaps empty - this shouldn't happen !!!");
            }
        } else if (bisectorCase == 3 && Utility.isCollinear(pLeft, pRight, p3)) {
            System.out.println("Handling case 3 - collinear");
            //BC(a1; a2; a3) consists of one or two cones
            ArrayList<Bisector[]> cones = findConeIntersectionsB3S(voronoiEdgesB2S, pLeft, pRight, p3);
            if (!cones.isEmpty()) {
                for (Bisector[] cone : cones) {
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
     * one vertex.
     * Case 3. Otherwise, a3 lies on the boundary of FG12. If a1, a2, a3 are not collinear
     * then BC(a1; a2; a3) is a polygonal chain completed with one ray at the end, else
     * BC(a1; a2; a3) consists of one or two cones.
     * 
     * @param q Quadrilateral around each vertex
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @return Integer representing the case
     */
    private int caseBisectorBetween3Vertices(Quadrilateral q, Vertex a1, Vertex a2, Vertex a3) {
        //System.out.println("caseBisectorBetween3Vertices: " + a1 + ", " + a2 + ", " + a3);
        //a3 = new Vertex(a1.x,a1.y);
        double caseTolerance = 0.01;
        
        double angle = Utility.calculateAngle(a1, a2);
        
        // Check for degenerate case. FG consists of a line through a1a2
        if (segsParallelToa1a2(q, a1, a2, angle) == 2) { // FG12 is a line
            System.out.println("B3P Special case - two quad edges parallel to a1a2");

            Vertex[] ray1 = findB3SUVRays(q, a2, a1, a1); // Ray from a1 to left
            Vertex[] ray2 = findB3SUVRays(q, a1, a2, a2); // Ray from a2 to right

            this.displayEdges.add(new Bisector(new Vertex[]{}, a1, a2, "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{}, ray1[0], ray1[1], "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{}, ray2[0], ray2[1], "b3s_step"));

            if (Utility.isLeftOfSegment(a1, a2, a3, caseTolerance) == 0 ||
                    Utility.isLeftOfSegment(ray1[0], ray1[1], a3, caseTolerance) == 0 ||
                    Utility.isLeftOfSegment(ray2[0], ray2[1], a3, caseTolerance) == 0 ) {

                System.out.println("Vertex on boundary - case 3 (degenerate case)");
                return 3;
            } else {
                System.out.println("Vertex not on boundary - case 2 (degenerate case)");
                return 2;
            }
        }
                
        Vertex[] uv = finduv(q, a1, a2); // Vertex[2] = {u, ray1+, ray2-, v, ray1+, ray2-}
        if (segsParallelToa1a2(q, a1, a2, angle) == 1) { // FG12 is a triangle
            System.out.println("B3P Special case - one quad edge parallel to a1a2");
            Vertex[] ray1 = findB3SUVRays(q, a2, a1, a1); // Ray from a1 to left
            Vertex[] ray2 = findB3SUVRays(q, a1, a2, a2); // Ray from a2 to right

            this.displayEdges.add(new Bisector(new Vertex[]{}, a1, a2, "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{}, ray1[0], ray1[1], "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{}, ray2[0], ray2[1], "b3s_step"));
            
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
        
        //System.out.println(Arrays.toString(uv));
        
        // Case 1 split into 3 parts for debugging
        if (Utility.isLeftOfSegment(a1, uv[0], a3, caseTolerance) == -1 &&
                Utility.isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(uv[3],a1, a3, caseTolerance) == -1) 
        {
            System.out.println("Vertex inside F - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(a1,uv[4], a3, caseTolerance) == 1) 
        {
            System.out.println("Vertex inside G12 - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(uv[2], a2, a3, caseTolerance) == -1 &&
                Utility.isLeftOfSegment(a2,uv[5], a3, caseTolerance) == -1) 
        {
            System.out.println("Vertex inside G21 - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[3], a1, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(a1, uv[4], a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[2], a2, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(a2, uv[5], a3, caseTolerance) == 0) 
        {
            System.out.println("Vertex on boundary - case 3");
            return 3;
        }
        
        System.out.println("Vertex outside FG - case 2");
        return 2;
    }
    
    /**
     * 
     * @param q Quadrilateral to check parallelism with
     * @param a1 Vertex
     * @param a2 Vertex
     * @return True if 2 rotated line segments are parallel (within 10 decimal places) to a1a2, false otherwise
     */
    private int segsParallelToa1a2(Quadrilateral q, Vertex a1, Vertex a2, double angle) {
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
     * @param a1 A center vertex
     * @param a2 A center vertex
     * @return Vertex array holding u and vertices representing its 2 rays and v and vertices representing its 2 rays respectively
     */
    private Vertex[] finduv(Quadrilateral q, Vertex a1, Vertex a2) {
        double angle = Utility.calculateAngle(a1, a2);
        //System.out.print("finduv(): ");
        Vertex[] td = new Vertex[2];
        ArrayList<Vertex> niVerts = Utility.findNonInnerVertices(q, a1, a2, angle);
        
        switch (niVerts.size()) {
            case 2:
                td[0] = niVerts.get(0);
                td[1] = niVerts.get(1);
                break;
            case 3:
                double tolerance = 0.00001;
                if (Math.abs(Utility.rotateVertex(niVerts.get(0), Utility.midpoint(a1, a2), angle).y - Utility.rotateVertex(niVerts.get(1), Utility.midpoint(a1, a2), angle).y) < tolerance) {
                    if (niVerts.get(0).x > niVerts.get(1).x) {
                        td[0] = niVerts.get(0);
                    } else {
                        td[0] = niVerts.get(1);
                    }   
                    td[1] = niVerts.get(2);
                } else if (Math.abs(Utility.rotateVertex(niVerts.get(1), Utility.midpoint(a1, a2), angle).y - Utility.rotateVertex(niVerts.get(2), Utility.midpoint(a1, a2), angle).y) < tolerance) {
                    td[0] = niVerts.get(0);
                    if (niVerts.get(1).x > niVerts.get(2).x) {
                        td[1] = niVerts.get(1);
                    } else {
                        td[1] = niVerts.get(2);
                    }
                }
                break;
            case 4:
                if (Utility.rotateVertex(niVerts.get(0), Utility.midpoint(a1, a2), angle).x > Utility.rotateVertex(niVerts.get(1), Utility.midpoint(a1, a2), angle).x) {
                    td[0] = niVerts.get(0);
                } else {
                    td[0] = niVerts.get(1);
                }
                if (Utility.rotateVertex(niVerts.get(2), Utility.midpoint(a1, a2), angle).x > Utility.rotateVertex(niVerts.get(1), Utility.midpoint(a1, a2), angle).x) {
                    td[1] = niVerts.get(2);
                } else {
                    td[1] = niVerts.get(3);
                }
                break;
        }
        
        /*System.out.print("td vertices ");
        for (Vertex p : td) {
            System.out.print(p + " ");
        }
        System.out.println();*/
        
        Vertex[] u1 = findB3SUVRays(q, Utility.rotateVertex(td[0], Utility.midpoint(a1, a2), angle), Utility.rotateVertex(a1, Utility.midpoint(a1, a2), angle), Utility.rotateVertex(q.prevVertex(td[0]), Utility.midpoint(a1, a2), angle));
        //System.out.println("u1: " + td[0] + ", " + q.prevVertex(td[0]));
        Vertex[] u2 = findB3SUVRays(q, Utility.rotateVertex(td[0], Utility.midpoint(a1, a2), angle), Utility.rotateVertex(a2, Utility.midpoint(a1, a2), angle), Utility.rotateVertex(q.nextVertex(td[0]), Utility.midpoint(a1, a2), angle));
        //System.out.println("u2: " + td[0] + ", " + q.nextVertex(td[0]));
        
        double tolerance = 0.00001;
        Vertex[] v1;
        // Edge parallel to a1a2
        if (Math.abs(td[1].y - Utility.rotateVertex(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle).y) < tolerance) {
            System.out.println("Handling B3S triangle FG region");
            v1 = findB3SUVRays(q, Utility.rotateVertex(td[1], Utility.midpoint(a1, a2), angle), Utility.rotateVertex(a1, Utility.midpoint(a1, a2), angle), Utility.rotateVertex(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle));
        } else {
            v1 = findB3SUVRays(q, Utility.rotateVertex(td[1], Utility.midpoint(a1, a2), angle), Utility.rotateVertex(a1, Utility.midpoint(a1, a2), angle), Utility.rotateVertex(q.nextVertex(td[1]), Utility.midpoint(a1, a2), angle));
        }
        //System.out.println("v1: " + v1[0] + ", " + v1[1]);
        Vertex[] v2 = findB3SUVRays(q, Utility.rotateVertex(td[1], Utility.midpoint(a1, a2), angle), Utility.rotateVertex(a2, Utility.midpoint(a1, a2), angle), Utility.rotateVertex(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle));
        //System.out.println("v2: " + td[1] + ", " + q.prevVertex(td[1]));
        
        Vertex u = Utility.doLineSegmentsIntersect(Utility.rotateVertex(u1[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u1[1], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u2[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u2[1], Utility.midpoint(a1, a2), -angle));
        Vertex v = Utility.doLineSegmentsIntersect(Utility.rotateVertex(v1[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v1[1], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v2[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v2[1], Utility.midpoint(a1, a2), -angle));
        //System.out.println("u = " + u + ", v = " + v);
        
        //below lines only for debugging when u or v is null (shouldn't happen)
        /*this.displayEdges.add(new VoronoiBisector(new Vertex[]{}, Utility.rotateVertex(u1[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u1[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Vertex[]{}, Utility.rotateVertex(u2[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u2[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Vertex[]{}, Utility.rotateVertex(v1[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v1[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new VoronoiBisector(new Vertex[]{}, Utility.rotateVertex(v2[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v2[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        */
        
        // Draw FG region
        this.displayEdges.add(new Bisector(new Vertex[]{}, u, Utility.rotateVertex(u1[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new Bisector(new Vertex[]{}, u, Utility.rotateVertex(u2[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new Bisector(new Vertex[]{}, v, Utility.rotateVertex(v1[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new Bisector(new Vertex[]{}, v, Utility.rotateVertex(v2[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
                
        return new Vertex[]{u, Utility.rotateVertex(u1[3], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u2[3], Utility.midpoint(a1, a2), -angle), v, Utility.rotateVertex(v1[3], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v2[3], Utility.midpoint(a1, a2), -angle)};
    }
    
    /**
     * 
     * @param endPt Initial endPt of ray
     * @param a Vertex to translate endPt to
     * @param nextPt Vertex initial ray passes through
     * @return Vertex array containing ray starting at endPt and passing through nextPt then translated to a
     */
    private Vertex[] findB3SUVRays(Quadrilateral quad, Vertex endPt, Vertex a, Vertex nextPt) {
        Vertex p1 = new Vertex(), p2 = new Vertex();
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
        Vertex rayEnd = new Vertex(rayEndx, a.y); // End vertex of ray which is basically + or - infinity
        Vertex rayEnd2 = new Vertex(-rayEndx, a.y);
        
        double angle = Utility.calculateAngle(p1, p2);
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Vertex[] ray = {new Vertex(p1.x, p1.y), Utility.rotateVertex(rayEnd, new Vertex(0,0), -angle)};
        Vertex[] ray2 = {new Vertex(p1.x, p1.y), Utility.rotateVertex(rayEnd2, new Vertex(0,0), -angle)};
        
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
        
        return new Vertex[]{ray[0], ray[1], ray2[0], ray2[1]};
    }
    
    /**
     * 
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @return VoronoiBisector representing the intersection vertex between bisector of a1a3 and a2a3. case 2
     */
    private Bisector findIntersectionB3S(Bisector[] voronoiEdgesB2S, Vertex a1, Vertex a2, Vertex a3) {
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
                        Vertex b3s = Utility.doLineSegmentsIntersect(voronoiEdgesB2S[i].getStartVertex(), voronoiEdgesB2S[i].getEndVertex(), 
                                voronoiEdgesB2S[j].getStartVertex(), voronoiEdgesB2S[j].getEndVertex());
                        if (b3s != null) {
                            //System.out.println("Found intersection vertex: " + b3s);
                            return new Bisector(new Vertex[]{a1, a2, a3}, b3s, b3s, "b3s_chosen");
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @return ArrayList of VoronoiBisector representing the overlapping segments between bisector of a1a3 and a2a3. case 3 non-collinear
     */
    private ArrayList<Bisector> findOverlapsB3S(Bisector[] voronoiEdgesB2S, Vertex a1, Vertex a2, Vertex a3) {
        ArrayList<Bisector> overlaps = new ArrayList();
        
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
                        Vertex[] overlap = doLineSegmentsOverlap(voronoiEdgesB2S[i].getStartVertex(), voronoiEdgesB2S[i].getEndVertex(), 
                                voronoiEdgesB2S[j].getStartVertex(), voronoiEdgesB2S[j].getEndVertex());

                        if (overlap != null) {
                            //System.out.println("Found overlap: " + overlap[0] + ", " + overlap[1]);
                            Vertex chosenPt;
                            if (Math.abs(overlap[0].x) > 999 || Math.abs(overlap[0].y) > 999) {
                                chosenPt = overlap[1];
                            } else {
                                chosenPt = overlap[0];
                            }
                            
                            Bisector bisector = new Bisector(new Vertex[]{a1, a2, a3}, chosenPt, chosenPt, "b3s_chosen_overlap");
                            this.voronoiEdgesB3S.add(bisector);
                            overlaps.add(new Bisector(new Vertex[]{a1, a2, a3}, overlap[0], overlap[1], "b3s_overlap"));
                        }
                    }
                }
            }
        }
        return overlaps;
    }
    
    /**
     * 
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @return ArrayList of VoronoiBisector representing the overlapping cones between bisector of a1a3 and a2a3. case 3 collinear
     */
    private ArrayList<Bisector[]> findConeIntersectionsB3S(Bisector[] voronoiEdgesB2S, Vertex a1, Vertex a2, Vertex a3) {
        ArrayList<Bisector[]> coneIntersections = new ArrayList();
        ArrayList<Bisector[]> cones = getCones(voronoiEdgesB2S); // List of VoronoiBisector tuples representing cones
        
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
                        
                        //System.out.println("Comparing " + cones.get(i)[0].getStartVertex() + ", " + cones.get(i)[0].getEndVertex());
                        //System.out.println("and " + cones.get(i)[1].getStartVertex() + ", " + cones.get(i)[1].getEndVertex());
                        //System.out.println("to " + cones.get(j)[0].getStartVertex() + ", " + cones.get(j)[0].getEndVertex());
                        //System.out.println("and " + cones.get(j)[1].getStartVertex() + ", " + cones.get(j)[1].getEndVertex());
                        Bisector[] coneIntersection;
                        if ((coneIntersection = doConesIntersect(cones.get(i), cones.get(j))) != null) {
                            // Add chosen B3S vertex to list (apex of cone)
                            Vertex chosenPt = Utility.doLineSegmentsIntersect(coneIntersection[0].getStartVertex(), coneIntersection[0].getEndVertex(), 
                                    coneIntersection[1].getStartVertex(), coneIntersection[1].getEndVertex());
                            if (!vertexIsInfinite(chosenPt)) {
                                Vertex[] adjacentUnion = Utility.vertexArrayUnion(coneIntersection[0].getAdjacentPtsArray(), coneIntersection[1].getAdjacentPtsArray());
                                //System.out.println("Found cone intersection at " + chosenPt + "\n");
                                this.voronoiEdgesB3S.add(new Bisector(adjacentUnion, chosenPt, chosenPt, "b3s_chosen_cone"));

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
    private ArrayList<Bisector[]> getCones(Bisector[] voronoiEdgesB2S) {
        ArrayList<Bisector[]> cones = new ArrayList();
        int index = 0; // Also coneID
        boolean isFirstRay = true;
        
        for (Bisector cone : voronoiEdgesB2S) {
            if (cone.getTag().contains("cone")) {
                String curConeID = cone.getTag().substring(cone.getTag().indexOf("=")+1, cone.getTag().length());
                
                if (Integer.parseInt(curConeID) == index) {
                    if (isFirstRay) {
                        cones.add(new Bisector[2]);
                        cones.get(index)[0] = cone;
                        //System.out.println("Adding cone " + curConeID + " at index " + index + " [0]");
                        //System.out.println(cone.getStartVertex() + ", " + cone.getEndVertex());
                        isFirstRay = false;
                    } else {
                        cones.get(index)[1] = cone;
                        //System.out.println("Adding cone " + curConeID + " at index " + index + " [1]");
                        //System.out.println(cone.getStartVertex() + ", " + cone.getEndVertex());
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
    private Bisector[] doConesIntersect(Bisector[] cone1, Bisector[] cone2) {
        
        for (Bisector coneEdge1 : cone1) {
            
            for (Bisector coneEdge2 : cone2) {
                Vertex intersection;
                if ((intersection = Utility.doLineSegmentsIntersect(coneEdge1.getStartVertex(), coneEdge1.getEndVertex(), coneEdge2.getStartVertex(), coneEdge2.getEndVertex())) != null) {
                    return new Bisector[]{new Bisector(cone1[0].getAdjacentPtsArray(), intersection, coneEdge1.getEndVertex(), "b3s_cone"), 
                        new Bisector(cone2[0].getAdjacentPtsArray(), intersection, coneEdge2.getEndVertex(), "b3s_cone")};
                }
            }
        }
        
        return null;
    }
    
    /**
     * 
     * @param edges ArrayList of Voronoi Bisectors to print formatted
     */
    private void printVoronoiEdges(ArrayList<Bisector> edges) {
        for (Bisector vb : edges) {
            System.out.println(" " + /*vb.adjacentVertices.get(0) + ", " + vb.adjacentVertices.get(1) + ": " +*/ vb.getStartVertex() + ", " + vb.getEndVertex());
        }
    }
    
    /**
     * 
     * @param p1 Endvertex of first line segment P
     * @param p2 Endvertex of first line segment Q
     * @param q1 Endvertex of second line segment
     * @param q2 Endvertex of second line segment
     * @return Line segment representing overlap of P and Q
     */
    private Vertex[] doLineSegmentsOverlap(Vertex p1, Vertex p2, Vertex q1, Vertex q2) {
        if (p1.equals(p2) || q1.equals(q2)) {
            return null;
        }
        
        double overlapTolerane = 0.1;
        Vertex[] overlap = {null, null};
        
        Vertex pl = new Vertex(), pr = new Vertex(), ql = new Vertex(), qr = new Vertex();
        Utility.setLeftAndRightVertex(p1, p2, pl, pr, Utility.calculateAngle(p1, p2));
        Utility.setLeftAndRightVertex(q1, q2, ql, qr, Utility.calculateAngle(q1, q2));
        //System.out.println("\nInitial vertices: " + pl + ", " + pr + " : " + ql + ", " + qr);
        // Adjust ray endvertices to be at screen boundary
        if (vertexIsInfinite(pl)) {
            pl = findBoundaryVertexOnRay(pl, pr);
        }
        if (vertexIsInfinite(pr)) {
            pr = findBoundaryVertexOnRay(pl, pr);
        }
        if (vertexIsInfinite(ql)) {
            ql = findBoundaryVertexOnRay(ql, qr);
        }
        if (vertexIsInfinite(qr)) {
            qr = findBoundaryVertexOnRay(ql, qr);
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
     * @param p A vertex
     * @return True if x or y of vertex is infinite or -infinite wrt screen size
     */
    private boolean vertexIsInfinite(Vertex p) {
        double inf = 10000;
        return p.x > inf || p.y > inf || p.x < -inf || p.y < -inf;
    }
    
    /**
     * 
     * @param p1 Endvertex of ray represented as line segment
     * @param p2 Endvertex of ray represented as line segment
     * @return Vertex on line segment at boundary of screen
     */
    private Vertex findBoundaryVertexOnRay(Vertex p1, Vertex p2) {
        double boundaryBuff = 1000;
        Vertex[] leftScreen = {new Vertex(-boundaryBuff, -boundaryBuff), new Vertex(-boundaryBuff, this.height+boundaryBuff)};
        Vertex[] rightScreen = {new Vertex(this.width+boundaryBuff, -boundaryBuff), new Vertex(this.width+boundaryBuff, this.height+boundaryBuff)};
        Vertex[] topScreen = {new Vertex(-boundaryBuff, this.height+boundaryBuff), new Vertex(this.width+boundaryBuff, this.height+boundaryBuff)};
        Vertex[] bottomScreen = {new Vertex(-boundaryBuff, -boundaryBuff), new Vertex(this.width+boundaryBuff, -boundaryBuff)};
        
        Vertex boundary;
        // Left side of screen will intersect ray
        if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, leftScreen[0], leftScreen[1])) != null) {
            //System.out.println("boundary vertex: " + boundary);
            return boundary;
        } else if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, rightScreen[0], rightScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary vertex: " + boundary);
            return boundary;        
        } else if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, topScreen[0], topScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary vertex: " + boundary);
            return boundary;        
        } else if ((boundary = Utility.doLineSegmentsIntersect(p1, p2, bottomScreen[0], bottomScreen[1])) != null) { // Right side of screen will intersect ray
            //System.out.println("boundary vertex: " + boundary);
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
    public Bisector[] getVoronoiEdges() {
        return Utility.deepCopyVBArray(this.voronoiEdgesB3S.toArray(new Bisector[this.voronoiEdgesB3S.size()]));
    }
    
    /**
     * 
     * @return Deep copy of display edges as array
     */
    public List<Bisector> getDisplayEdges() {
        return (List)Utility.arrayToList(Utility.deepCopyVBArray(this.displayEdges.toArray(new Bisector[this.displayEdges.size()])));
    }
    
    /**
     * 
     * @return Deep copy array of chosen bisectors between 3 vertices
     */
    public Bisector[] getChosenBisectors() {
        List<Bisector> chosenBisectors = new ArrayList();
        for (Bisector chosenB3S : this.voronoiEdgesB3S) {
            if (chosenB3S.getTag().contains("chosen")) {
                chosenBisectors.add(chosenB3S);
            }
        }
        return Utility.deepCopyVBArray(chosenBisectors.toArray(new Bisector[chosenBisectors.size()]));
    }
    
}
