package dt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Lee Glendenning
 */
public class FindBisectorsThreeSites {
    
    private final List<Bisector> voronoiEdgesB3S, displayEdges, chosenBisectors;
    private final int height, width;
    
    public FindBisectorsThreeSites(int height, int width) {
        this.height = height;
        this.width = width;
        
        this.voronoiEdgesB3S = Collections.synchronizedList(new ArrayList());
        this.displayEdges = Collections.synchronizedList(new ArrayList());
        this.chosenBisectors = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * If it exists, find the bisector vertex between v1, v2, v3
     * 
     * @param q Quadrilateral around each vertex
     * @param bisectors2S HashMap<Vertex[], List<Bisector>> of bisectors between 2 sites
     * @param v1 A vertex to find bisector of
     * @param v2 A vertex to find bisector of
     * @param v3 A vertex to find bisector of
     * @return The chosen B3S
     */
    public Bisector findBisectorOfThreeSites(Quadrilateral q, HashMap<List<Vertex>, List<Bisector>> bisectors2S, Vertex v1, Vertex v2, Vertex v3) throws NullPointerException {
        Utility.debugPrintln("\nFinding Bisector Between 3 sites:");
        Vertex pLeft = new Vertex(), pRight = new Vertex();
        Utility.setLeftAndRightVertex(v1, v2, pLeft, pRight, Utility.calculateAngle(v1, v2));
                    
        Utility.debugPrintln("\na1 = " + pLeft + " a2 = " + pRight + " a3 = " + v3);
        int bisectorCase = caseBisectorBetween3Vertices(q, pLeft, pRight, v3);
        if (bisectorCase == 1) {
            return null;
        }
        
        // Keys to bisectors2S for the 3 bisectors for v1, v2, v3
        List<Vertex> key1 = getB2SKey(bisectors2S, v1, v2);
        List<Vertex> key2 = getB2SKey(bisectors2S, v2, v3);
        List<Vertex> key3 = getB2SKey(bisectors2S, v1, v3);
        
        // If case is 1, ignore. Means there is no bisector vertex
        if (bisectorCase == 2) { // case 2: single vertex is bisector of 3 
            Bisector bisector = findIntersectionB3S(bisectors2S, pLeft, pRight, v3, key3, key2);
            if (bisector != null) {
                this.chosenBisectors.add(bisector.deepCopy());
            } else {
                Utility.debugPrintln("[findBisectorOfThreeSites] case 2 bisector null - this shouldn't happen.");
            }
            return bisector;
        } else if (bisectorCase == 3 && !Utility.isCollinear(pLeft, pRight, v3)) {
            Utility.debugPrintln("Handling case 3 - not collinear");
            //BC(a1; a2; a3) is a polygonal chain completed with one ray at the end
            
            //Utility.debugPrintln("Finding overlaps");
            Bisector bisector = findOverlapsB3S(bisectors2S, pLeft, pRight, v3, key1, key2, key3);
            if (bisector != null) {
                //Utility.debugPrintln("Adding bisector to voronoiEdges");
                this.chosenBisectors.add(bisector.deepCopy());
            } else {
                Utility.debugPrintln("[findBisectorOfThreeSites] case 3 bisector overlaps empty - this shouldn't happen.");
            }
            return bisector;
        } else if (bisectorCase == 3 && Utility.isCollinear(pLeft, pRight, v3)) {
            Utility.debugPrintln("Handling case 3 - collinear");
            //BC(a1; a2; a3) consists of one or two cones
            Bisector bisector = findConeIntersectionsB3S(bisectors2S, pLeft, pRight, v3);
            if (bisector != null) {
                this.chosenBisectors.add(bisector.deepCopy());
            } else {
                Utility.debugPrintln("[findBisectorOfThreeSites] case 3 bisector cone overlaps empty - this shouldn't happen.");
            }
            return bisector;
        }
        
        Utility.debugPrintln("[findBisectorOfThreeSites] Could not determine case - this shouldn't happen.");
        return null;
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
        //Utility.debugPrintln("caseBisectorBetween3Vertices: " + a1 + ", " + a2 + ", " + a3);
        //a3 = new Vertex(a1.x,a1.y);
        double caseTolerance = 0.01;
        
        // Check for degenerate case. FG consists of a line through a1a2
        if (segsParallelToa1a2(q, a1, a2) == 2) { // FG12 is a line
            Utility.debugPrintln("B3P Special case - two quad edges parallel to a1a2");

            Vertex[] ray1 = findB3SUVRays(q, a2, a1, a1); // Ray from a1 to left
            Vertex[] ray2 = findB3SUVRays(q, a1, a2, a2); // Ray from a2 to right
            
            this.displayEdges.add(new Bisector(new Vertex[]{a1, a2, a3}, a1, a2, "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{a1, a2, a3}, ray1[0], ray1[1], "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{a1, a2, a3}, ray2[0], ray2[1], "b3s_step"));

            if (Utility.isLeftOfSegment(a1, a2, a3, caseTolerance) == 0 ||
                    Utility.isLeftOfSegment(ray1[0], ray1[1], a3, caseTolerance) == 0 ||
                    Utility.isLeftOfSegment(ray2[0], ray2[1], a3, caseTolerance) == 0 ) {

                Utility.debugPrintln("Vertex on boundary - case 3 (degenerate case)");
                return 3;
            } else {
                Utility.debugPrintln("Vertex not on boundary - case 2 (degenerate case)");
                return 2;
            }
        }
        Vertex[] uv;
        
        uv = finduv(q, a1, a2); // Vertex[2] = {u, ray1+, ray2-, v, ray1+, ray2-}
        
        if (segsParallelToa1a2(q, a1, a2) == 1) { // FG12 is a triangle
            Utility.debugPrintln("B3P Special case - one quad edge parallel to a1a2");
            Vertex[] ray1 = findB3SUVRays(q, a2, a1, a1); // Ray from a1 to left
            Vertex[] ray2 = findB3SUVRays(q, a1, a2, a2); // Ray from a2 to right

            this.displayEdges.add(new Bisector(new Vertex[]{a1, a2, a3}, a1, a2, "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{a1, a2, a3}, ray1[0], ray1[1], "b3s_step"));
            this.displayEdges.add(new Bisector(new Vertex[]{a1, a2, a3}, ray2[0], ray2[1], "b3s_step"));
            
            if (uv[0] == null) {
                uv[0] = Utility.midpoint(a1, a2);
                uv[1] = ray2[1];
                uv[2] = ray1[1];
            } if (uv[3] == null) {
                uv[3] = Utility.midpoint(a1, a2);
                uv[4] = ray2[1];
                uv[5] = ray1[1];
            }
        }
        // Non-degenerate case
        
        //Utility.debugPrint(Arrays.toString(uv));
        
        // Case 1 split into 3 parts for debugging
        if (Utility.isLeftOfSegment(a1, uv[0], a3, caseTolerance) == -1 &&
                Utility.isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(uv[3],a1, a3, caseTolerance) == -1) 
        {
            Utility.debugPrintln("Vertex inside F - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 1 &&
                Utility.isLeftOfSegment(a1,uv[4], a3, caseTolerance) == 1) 
        {
            Utility.debugPrintln("Vertex inside G12 - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(uv[2], a2, a3, caseTolerance) == -1 &&
                Utility.isLeftOfSegment(a2,uv[5], a3, caseTolerance) == -1) 
        {
            Utility.debugPrintln("Vertex inside G21 - case 1 (do nothing)");
            return 1;
            
        } else if (Utility.isLeftOfSegment(a2, uv[0], a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[3], a2, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[3], a1, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[1], a1, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(a1, uv[4], a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(uv[2], a2, a3, caseTolerance) == 0 ||
                Utility.isLeftOfSegment(a2, uv[5], a3, caseTolerance) == 0) 
        {
            Utility.debugPrintln("Vertex on boundary - case 3");
            return 3;
        }
        
        Utility.debugPrintln("Vertex outside FG - case 2");
        return 2;
    }
    
    /**
     * 
     * @param q Quadrilateral to check parallelism with
     * @param a1 Vertex
     * @param a2 Vertex
     * @return True if 2 rotated line segments are parallel (within 10 decimal places) to a1a2, false otherwise
     */
    private int segsParallelToa1a2(Quadrilateral q, Vertex a1, Vertex a2) {
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
     * @param bisectors2S HashMap of B2S
     * @param v1 A vertex
     * @param v2 A vertex
     * @return A key to the HashMap holding the bisector of v1 and v2
     */
    private List<Vertex> getB2SKey(HashMap<List<Vertex>, List<Bisector>> bisectors2S, Vertex v1, Vertex v2) {
        /*Utility.debugPrintln("Looking for key " + v1 + ", " + v2 + " or reversed");
        for (Map.Entry<List<Vertex>, List<Bisector>> entry : bisectors2S.entrySet()) {
            Utility.debugPrintln(" *" + entry.getKey().toString());
        }*/
        // Get the Bisector from the HashMap and add this ray to it
        if (bisectors2S.containsKey(Arrays.asList(v1, v2))) {
            return Arrays.asList(v1, v2);
        } else if (bisectors2S.containsKey(Arrays.asList(v2, v1))) {
            return Arrays.asList(v2, v1);
        } else {
            Utility.debugPrintln("[getB2SKey] Couldn't find HashMap entry for B2S to add rays to. This shouldn't happen!");
            return null;
        }
    }
    
    /**
     * 
     * @param q Quadrilateral to find u and v for
     * @param a1 A center vertex
     * @param a2 A center vertex
     * @return Vertex array holding u and vertices representing its 2 rays and v and vertices representing its 2 rays respectively
     */
    private Vertex[] finduv(Quadrilateral q, Vertex a1, Vertex a2) throws NullPointerException{
        double angle = Utility.calculateAngle(a1, a2);
        Vertex[] td = new Vertex[2];
        ArrayList<Vertex> niVerts = Utility.findNonInnerVertices(q, a1, a2, angle);
        
        //Utility.debugPrintln("nonInner Verts: " + niVerts.toString());
        
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
        
        //Utility.debugPrintln("td vertices: " + Arrays.toString(td));
        
        Vertex[] u1 = findB3SUVRays(q, td[0], a1, q.prevVertex(td[0]));
        //Utility.debugPrintln("u1: " + u1[0] + ", " + u1[1]);
        Vertex[] u2 = findB3SUVRays(q, td[0], a2, q.nextVertex(td[0]));
        //Utility.debugPrintln("u2: " + u2[0] + ", " + u2[1]);
        
        double tolerance = 0.00001;
        Vertex[] v1;
        // Edge parallel to a1a2
        if (Math.abs(td[1].y - Utility.rotateVertex(q.prevVertex(td[1]), Utility.midpoint(a1, a2), angle).y) < tolerance) {
            Utility.debugPrintln("Handling B3S triangle FG region");
            v1 = findB3SUVRays(q, td[1], a1, q.prevVertex(td[1]));
        } else {
            v1 = findB3SUVRays(q, td[1], a1, q.nextVertex(td[1]));
        }
        //Utility.debugPrintln("v1: " + v1[0] + ", " + v1[1]);
        Vertex[] v2 = findB3SUVRays(q, td[1], a2, q.prevVertex(td[1]));
        //Utility.debugPrintln("v2: " + v2[0] + ", " + v2[1]);
        
        Vertex u = Utility.doLineSegmentsIntersect(u1[0], u1[1], u2[0], u2[1]);
        Vertex v = Utility.doLineSegmentsIntersect(v1[0], v1[1], v2[0], v2[1]);
        
        // If u or v are null, it is possible that an FG line segment passes through
        // vertex a1 or a2. This is an extra check for that case since it is not
        // caught in the first check for some reason
        if (u == null) {
            if (Utility.isLeftOfSegment(Utility.rotateVertex(u2[0], Utility.midpoint(a1, a2), -angle),
                    Utility.rotateVertex(u2[1], Utility.midpoint(a1, a2), -angle), a1, 0.1) == 0) {
                u = a1.deepCopy();
            } else if (Utility.isLeftOfSegment(Utility.rotateVertex(u1[0], Utility.midpoint(a1, a2), -angle),
                    Utility.rotateVertex(u1[1], Utility.midpoint(a1, a2), -angle), a2, 0.1) == 0) {
                u = a2.deepCopy();
            }
        }
        if (v == null) {
            if (Utility.isLeftOfSegment(Utility.rotateVertex(v2[0], Utility.midpoint(a1, a2), -angle),
                    Utility.rotateVertex(v2[1], Utility.midpoint(a1, a2), -angle), a1, 0.1) == 0) {
                v = a1.deepCopy();
            } else if (Utility.isLeftOfSegment(Utility.rotateVertex(v1[0], Utility.midpoint(a1, a2), -angle),
                    Utility.rotateVertex(v1[1], Utility.midpoint(a1, a2), -angle), a2, 0.1) == 0) {
                v = a2.deepCopy();
            }
        }
        //Utility.debugPrintln("u = " + u + ", v = " + v);
        
        //below lines only for debugging when u or v is null (shouldn't happen)
        /*this.displayEdges.add(new Bisector(new Vertex[]{}, Utility.rotateVertex(u1[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u1[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new Bisector(new Vertex[]{}, Utility.rotateVertex(u2[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(u2[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new Bisector(new Vertex[]{}, Utility.rotateVertex(v1[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v1[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        this.displayEdges.add(new Bisector(new Vertex[]{}, Utility.rotateVertex(v2[0], Utility.midpoint(a1, a2), -angle), Utility.rotateVertex(v2[1], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        */
        
        // Draw FG region
        //Utility.debugPrintln("FG edges:");
        this.displayEdges.add(new Bisector(new Vertex[]{a1, a2}, u, Utility.rotateVertex(u1[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        //Utility.debugPrintln(this.displayEdges.get(displayEdges.size()-1).getStartVertex() + " " + this.displayEdges.get(displayEdges.size()-1).getEndVertex());
        this.displayEdges.add(new Bisector(new Vertex[]{a1, a2}, u, Utility.rotateVertex(u2[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        //Utility.debugPrintln(this.displayEdges.get(displayEdges.size()-1).getStartVertex() + " " + this.displayEdges.get(displayEdges.size()-1).getEndVertex());
        this.displayEdges.add(new Bisector(new Vertex[]{a1, a2}, v, Utility.rotateVertex(v1[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        //Utility.debugPrintln(this.displayEdges.get(displayEdges.size()-1).getStartVertex() + " " + this.displayEdges.get(displayEdges.size()-1).getEndVertex());
        this.displayEdges.add(new Bisector(new Vertex[]{a1, a2}, v, Utility.rotateVertex(v2[3], Utility.midpoint(a1, a2), -angle), "b3s_step"));
        //Utility.debugPrintln(this.displayEdges.get(displayEdges.size()-1).getStartVertex() + " " + this.displayEdges.get(displayEdges.size()-1).getEndVertex());
        
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
        Vertex v1 = new Vertex(), v2 = new Vertex();
        // EndPt is relative to Quadrilateral. Translate relative to a
        v1.x = endPt.x + a.x - quad.getCenter().x;
        v1.y = endPt.y + a.y - quad.getCenter().y;
        
        // NonInnerVertex is relative to Quadrilateral. Translate relative to a
        v2.x = nextPt.x + a.x - quad.getCenter().x;
        v2.y = nextPt.y + a.y - quad.getCenter().y;
        
        // v1,v2 is now an edge of the quad around a
        
        //Utility.debugPrintln("findB3SUVRays: a = " + a + ", after translation: endPt = " + v1 + ", nextPt = " + v2);
        
        // Define the direction of the ray starting at a
        double rayEndx = Utility.RAY_SIZE;
        //Utility.debugPrintln(a + " : " + nonInnerVertex);
        double tolerance = 0.0001;
        if (v1.x > v2.x+tolerance || (Math.abs(Math.abs(v1.x) - Math.abs(v2.x)) < tolerance && v1.y+tolerance < v2.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        
        //Utility.debugPrintln("rayEndx = " + rayEndx);
        
        Vertex rayEnd = new Vertex(rayEndx, a.y); // End vertex of ray which is basically + or - infinity
        Vertex rayEnd2 = new Vertex(-rayEndx, a.y);
        
        double angle = Utility.calculateAngle(v1, v2);
        // Make angle negative due to the rayEndx direction
        // Maybe could be cleaner but this works
        if (angle > 0) {
            angle *= -1;
        }
        //Utility.debugPrintln("angle(v1, v2) = " + Math.toDegrees(angle) + ", radians: " + angle);
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Vertex[] ray = {a, Utility.rotateVertex(rayEnd, a, angle)};
        Vertex[] ray2 = {a, Utility.rotateVertex(rayEnd2, a, angle)};
        
        //Utility.debugPrintln("  ray = " + ray[0] + ", " + ray[1]);
        
        return new Vertex[]{ray[0], ray[1], ray2[0], ray2[1]};
    }
    
    /**
     * @param bisectors2S HashMap<Vertex[], List<Bisector>> of bisectors between 2 sites
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @param key1 A key to the bisectors2S HashMap for bisector between a1a3
     * @param key2 A key to the bisectors2S HashMap for bisector between a2a3
     * @return VoronoiBisector representing the intersection vertex between bisector of a1a3 and a2a3. case 2
     */
    private Bisector findIntersectionB3S(HashMap<List<Vertex>, List<Bisector>> bisectors2S, Vertex a1, Vertex a2, Vertex a3, List<Vertex> key1, List<Vertex> key2) {
        //Utility.debugPrintln("a1 = " + a1 + " a2 = " + a2 + " a3 = " + a3 + ". # b2s = " + bisectors2S.size());
        //printEdges(bisectors2S);
        List<Bisector> bisector1 = bisectors2S.get(key1);
        List<Bisector> bisector2 = bisectors2S.get(key2);
        for (Bisector b1 : bisector1) {
            for (Bisector b2 : bisector2) {
                // Look for intersection between the 2 edge segments
                Vertex b3s = Utility.doLineSegmentsIntersect(b1.getStartVertex(), b1.getEndVertex(), 
                        b2.getStartVertex(), b2.getEndVertex());
                if (b3s != null) {
                    //Utility.debugPrint("Found intersection vertex: " + b3s);
                    return new Bisector(new Vertex[]{a1, a2, a3}, b3s, b3s, "b3s_chosen");
                }
            }
        }
        Utility.debugPrintln("[findIntersectionB3S] Bisector is null! ");
        return null;
    }
    
    /**
     * @param bisectors2S HashMap<Vertex[], List<Bisector>> of bisectors between 2 sites
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @param key1 A key to the bisectors2S HashMap for bisector between a1a2
     * @param key2 A key to the bisectors2S HashMap for bisector between a2a3
     * @param key3 A key to the bisectors2S HashMap for bisector between a1a3
     * @return The chosen bisector point of the overlapping B3S
     */
    private Bisector findOverlapsB3S(HashMap<List<Vertex>, List<Bisector>> bisectors2S,
            Vertex a1, Vertex a2, Vertex a3, List<Vertex> key1, List<Vertex> key2, List<Vertex> key3) throws NullPointerException {
        //ArrayList<Bisector> overlaps = new ArrayList();
        
        List<Bisector> bisector1 = bisectors2S.get(key1);
        List<Bisector> bisector2 = bisectors2S.get(key2);
        List<Bisector> bisector3 = bisectors2S.get(key3);
        for (Bisector b1 : bisector1) {
            for (Bisector b2 : bisector2) {
                // Check for overlap between a1a2
                Vertex[] overlap1 = doLineSegmentsOverlap(b1.getStartVertex(), b1.getEndVertex(), 
                                b2.getStartVertex(), b2.getEndVertex());
                Bisector tempB1 = checkOverlap(overlap1, a1, a2, a3);
                if (tempB1 != null) {
                    return tempB1;
                    //overlaps.add(tempB1);
                }
            
                for (Bisector b3 : bisector3) {
                    // Check for overlap between a2a3
                    Vertex[] overlap2 = doLineSegmentsOverlap(b2.getStartVertex(), b2.getEndVertex(), 
                                b3.getStartVertex(), b3.getEndVertex());
                    Bisector tempB2 = checkOverlap(overlap2, a1, a2, a3);
                    if (tempB2 != null) {
                        //overlaps.add(tempB2);
                        return tempB2;
                    }
                    
                    // Check for overlap between a1a3
                    Vertex[] overlap3 = doLineSegmentsOverlap(b1.getStartVertex(), b1.getEndVertex(), 
                                b3.getStartVertex(), b3.getEndVertex());
                    Bisector tempB3 = checkOverlap(overlap3, a1, a2, a3);
                    if (tempB3 != null) {
                        //overlaps.add(tempB3);
                        return tempB3;
                    }
                }
            }
        }
        Utility.debugPrintln("[findOverlapsB3S] Bisector is null! ");
        return null;
    }
    
    /**
     * 
     * @param overlap A Vertex[2] representing the line segment which is an overlap
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @return A Bisector representing the overlap if it exists, null otherwise
     */
    private Bisector checkOverlap(Vertex[] overlap, Vertex a1, Vertex a2, Vertex a3) {
        if (overlap != null) {
            //Utility.debugPrint("Found overlap: " + overlap[0] + ", " + overlap[1]);
            Vertex chosenPt;
            if (Math.abs(overlap[0].x) > 999 || Math.abs(overlap[0].y) > 999) {
                chosenPt = overlap[1];
            } else {
                chosenPt = overlap[0];
            }

            this.voronoiEdgesB3S.add(new Bisector(new Vertex[]{a1, a2, a3}, overlap[0], overlap[1], "b3s_overlap"));
            return new Bisector(new Vertex[]{a1, a2, a3}, chosenPt, chosenPt, "b3s_chosen_overlap");
        } else {
            return null;
        }
    }
    
    /**
     * @param bisectors2S HashMap<Vertex[], List<Bisector>> of bisectors between 2 sites
     * @param a1 A vertex
     * @param a2 A vertex
     * @param a3 A vertex
     * @return The chosen bisector point of the overlapping cones
     */
    private Bisector findConeIntersectionsB3S(HashMap<List<Vertex>, List<Bisector>> bisectors2S, Vertex a1, Vertex a2, Vertex a3) {
        //ArrayList<Bisector[]> coneIntersections = new ArrayList();
        ArrayList<Bisector[]> cones = getCones(bisectors2S); // List of VoronoiBisector tuples representing cones
        
        for (int i = 0; i < cones.size(); i ++) {
            // If cone belongs to a1a2, a1a3, or a2a3
            if (cones.get(i)[0].getAdjacentPtsList().contains(a1) && cones.get(i)[0].getAdjacentPtsList().contains(a2) ||
                    cones.get(i)[0].getAdjacentPtsList().contains(a1) && cones.get(i)[0].getAdjacentPtsList().contains(a3) ||
                    cones.get(i)[0].getAdjacentPtsList().contains(a2) && cones.get(i)[0].getAdjacentPtsList().contains(a3)) {
                
                for (int j = i+1; j < cones.size(); j ++) {
                    // If cone belongs to a1a2, a1a3, or a2a3
                    if (cones.get(j)[0].getAdjacentPtsList().contains(a1) && cones.get(j)[0].getAdjacentPtsList().contains(a2) ||
                            cones.get(j)[0].getAdjacentPtsList().contains(a1) && cones.get(j)[0].getAdjacentPtsList().contains(a3) ||
                            cones.get(j)[0].getAdjacentPtsList().contains(a2) && cones.get(j)[0].getAdjacentPtsList().contains(a3)) {
                        
                        //Utility.debugPrint("Comparing " + cones.get(i)[0].getStartVertex() + ", " + cones.get(i)[0].getEndVertex());
                        //Utility.debugPrint("and " + cones.get(i)[1].getStartVertex() + ", " + cones.get(i)[1].getEndVertex());
                        //Utility.debugPrint("to " + cones.get(j)[0].getStartVertex() + ", " + cones.get(j)[0].getEndVertex());
                        //Utility.debugPrint("and " + cones.get(j)[1].getStartVertex() + ", " + cones.get(j)[1].getEndVertex());
                        Bisector[] coneIntersection;
                        if ((coneIntersection = doConesIntersect(cones.get(i), cones.get(j))) != null) {
                            // Add chosen B3S vertex to list (apex of cone)
                            Vertex chosenPt = Utility.doLineSegmentsIntersect(coneIntersection[0].getStartVertex(), coneIntersection[0].getEndVertex(), 
                                    coneIntersection[1].getStartVertex(), coneIntersection[1].getEndVertex());
                            if (!vertexIsInfinite(chosenPt)) {
                                Vertex[] adjacentUnion = Utility.vertexArrayUnion(coneIntersection[0].getAdjacentPtsArray(), coneIntersection[1].getAdjacentPtsArray());
                                //Utility.debugPrint("Found cone intersection at " + chosenPt + "\n");
                                
                                // Add entire cone to list for displaying
                                this.voronoiEdgesB3S.add(coneIntersection[0]);
                                this.voronoiEdgesB3S.add(coneIntersection[1]);
                                
                                Bisector chosenB3S = new Bisector(adjacentUnion, chosenPt, chosenPt, "b3s_chosen_cone");
                                this.chosenBisectors.add(chosenB3S.deepCopy());
                                return chosenB3S;
                            }
                        }
                    }
                }
            }
        }
        Utility.debugPrintln("[findConeIntersectionsB3S] Bisector is null! ");
        return null;
    }
    
    /**
     * 
     * @return ArrayList of cones represented by VoronoiBisector tuples
     */
    private ArrayList<Bisector[]> getCones(HashMap<List<Vertex>, List<Bisector>> bisectors2S) {
        ArrayList<Bisector[]> cones = new ArrayList();
        int index = 0; // Also coneID
        boolean isFirstRay = true;
        
        for (Map.Entry<List<Vertex>, List<Bisector>> bisectorEntry : bisectors2S.entrySet()) {
            List<Bisector> bisector = bisectorEntry.getValue();
            
            for (Bisector cone : bisector) {
            
                if (cone.getTag().contains("cone")) {
                    String curConeID = cone.getTag().substring(cone.getTag().indexOf("=")+1, cone.getTag().length());

                    if (Integer.parseInt(curConeID) == index) {
                        if (isFirstRay) {
                            cones.add(new Bisector[2]);
                            cones.get(index)[0] = cone;
                            //Utility.debugPrint("Adding cone " + curConeID + " at index " + index + " [0]");
                            //Utility.debugPrint(cone.getStartVertex() + ", " + cone.getEndVertex());
                            isFirstRay = false;
                        } else {
                            cones.get(index)[1] = cone;
                            //Utility.debugPrint("Adding cone " + curConeID + " at index " + index + " [1]");
                            //Utility.debugPrint(cone.getStartVertex() + ", " + cone.getEndVertex());
                            index ++;
                            isFirstRay = true;
                        }
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
     * @param v1 Endvertex of first line segment P
     * @param v2 Endvertex of first line segment Q
     * @param q1 Endvertex of second line segment
     * @param q2 Endvertex of second line segment
     * @return Line segment representing overlap of P and Q
     */
    private Vertex[] doLineSegmentsOverlap(Vertex v1, Vertex v2, Vertex q1, Vertex q2) {
        if (v1.equals(v2) || q1.equals(q2)) {
            return null;
        }
        
        double overlapTolerane = 0.1;
        Vertex[] overlap = {null, null};
        
        Vertex pl = new Vertex(), pr = new Vertex(), ql = new Vertex(), qr = new Vertex();
        Utility.setLeftAndRightVertex(v1, v2, pl, pr, Utility.calculateAngle(v1, v2));
        Utility.setLeftAndRightVertex(q1, q2, ql, qr, Utility.calculateAngle(q1, q2));
        //Utility.debugPrint("\nInitial vertices: " + pl + ", " + pr + " : " + ql + ", " + qr);
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

        //Utility.debugPrint("\nDoLineSegmentsOverlap: " + pl + ", " + pr + " : " + ql + ", " + qr);

        double qlOverlap = Utility.isLeftOfSegment(pl, pr, ql, overlapTolerane);
        double qrOverlap = Utility.isLeftOfSegment(pl, pr, qr, overlapTolerane);

        //Utility.debugPrint("qlOverlap = " + qlOverlap);
        //Utility.debugPrint("qrOverlap = " + qrOverlap);

        // No overlap
        if (qlOverlap != 0 && qrOverlap != 0) {
            //Utility.debugPrint("No overlap");
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
     * @param v1 Endvertex of ray represented as line segment
     * @param v2 Endvertex of ray represented as line segment
     * @return Vertex on line segment at boundary of screen
     */
    private Vertex findBoundaryVertexOnRay(Vertex v1, Vertex v2) {
        double boundaryBuff = 10000;
        Vertex[] leftScreen = {new Vertex(-boundaryBuff, -boundaryBuff), new Vertex(-boundaryBuff, this.height+boundaryBuff)};
        Vertex[] rightScreen = {new Vertex(this.width+boundaryBuff, -boundaryBuff), new Vertex(this.width+boundaryBuff, this.height+boundaryBuff)};
        Vertex[] topScreen = {new Vertex(-boundaryBuff, this.height+boundaryBuff), new Vertex(this.width+boundaryBuff, this.height+boundaryBuff)};
        Vertex[] bottomScreen = {new Vertex(-boundaryBuff, -boundaryBuff), new Vertex(this.width+boundaryBuff, -boundaryBuff)};
        
        Vertex boundary;
        //Utility.debugPrintln("Checking intersection with boundary between " + v1 + ", " + v2);
        // Left side of screen will intersect ray
        if ((boundary = Utility.doLineSegmentsIntersect(v1, v2, leftScreen[0], leftScreen[1])) != null) {
            //Utility.debugPrint("boundary vertex: " + boundary);
            return boundary;
        } else if ((boundary = Utility.doLineSegmentsIntersect(v1, v2, rightScreen[0], rightScreen[1])) != null) { // Right side of screen will intersect ray
            //Utility.debugPrint("boundary vertex: " + boundary);
            return boundary;        
        } else if ((boundary = Utility.doLineSegmentsIntersect(v1, v2, topScreen[0], topScreen[1])) != null) { // Right side of screen will intersect ray
            //Utility.debugPrint("boundary vertex: " + boundary);
            return boundary;        
        } else if ((boundary = Utility.doLineSegmentsIntersect(v1, v2, bottomScreen[0], bottomScreen[1])) != null) { // Right side of screen will intersect ray
            //Utility.debugPrint("boundary vertex: " + boundary);
            return boundary;        
        } else {
            Utility.debugPrintln("!!! Could not find intersection of ray with screen boundary !!!");
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
        return Utility.deepCopyVBArray(this.chosenBisectors.toArray(new Bisector[this.chosenBisectors.size()]));
    }
    
}
