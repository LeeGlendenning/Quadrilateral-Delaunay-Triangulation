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
    
    private final ArrayList<Vertex> h1, h2, g1, g2;
    private final List<Bisector> voronoiEdgesB2S, displayEdges;
    private int coneID = 0;
    
    public FindBisectorsTwoSites() {
        this.h1 = new ArrayList();
        this.h2 = new ArrayList();
        this.g1 = new ArrayList();
        this.g2 = new ArrayList();
        
        this.voronoiEdgesB2S = Collections.synchronizedList(new ArrayList());
        this.displayEdges = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * Find main bisector between all pairs of vertices
     * 
     * @param quad Quadrilateral to iterate over
     * @param p1 A vertex in the vertex set
     * @param p2 A vertex in the vertex set
     */
    public void findBisectorOfTwoSites(Quadrilateral quad, Vertex p1, Vertex p2) {
        Utility.debugPrintln("\nFinding Bisector Between 2 Sites:");
        double angle = Utility.calculateAngle(p1, p2); // Angle that slope(p1p2) makes with x axis
        
        Utility.debugPrintln("Angle = " + Math.toDegrees(angle));
        Vertex a1 = new Vertex(), a2 = new Vertex();
        Utility.setLeftAndRightVertex(p1, p2, a1, a2, angle);
        Utility.debugPrintln("left vertex : " + a1 + ", right vertex: " + a2);
        
        // Two "middle" vertices of quad wrt y value and angle
        Vertex[] innerVertices = findInnerVertices(quad, angle);
        
        h1.add(new Vertex());
        h2.add(new Vertex());
        g1.add(new Vertex());
        g2.add(new Vertex());
        findh12g12(h1.get(h1.size()-1), h2.get(h1.size()-1), g1.get(h1.size()-1), g2.get(h1.size()-1), a1, a2, quad, innerVertices, angle);
        Utility.debugPrintln("h1 = " + h1 + ", h2 = " + h2);
        Utility.debugPrintln("g1 = " + g1 + ", g2 = " + g2);
        
        // Endvertices of main bisector between p1 and p2
        Vertex h = doRaysIntersect(a1, h1.get(h1.size()-1), a2, h2.get(h2.size()-1));
        Vertex g = doRaysIntersect(a1, g1.get(g1.size()-1), a2, g2.get(g2.size()-1));
        
        //Utility.debugPrintln("Endvertices of main bisector segment: " + h + ", " + g);
        this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, h, g, "b2s_chosen"));
        
        // Find intersections between non-inner vertices
        ArrayList<Vertex> nonInnerVertices = Utility.findNonInnerVertices(quad, a1, a2, angle);
        
        calculateAllBisectorRays(nonInnerVertices, quad, h, g, a1, p1, p2, angle);
    }
    
    /**
     * 
     * @param nonInnerVertices ArrayList of non-inner vertices of the Quadrilateral
     * @param h Intersection vertex of h rays
     * @param g Intersection vertex of g rays
     * @param a1 A center vertex of the Quadrilateral
     * @param p1 An adjacent vertex of the bisector rays
     * @param p2 An adjacent vertex of the bisector rays
     */
    private void calculateAllBisectorRays(ArrayList<Vertex> nonInnerVertices, Quadrilateral quad, Vertex h, Vertex g, Vertex a1, Vertex p1, Vertex p2, double angle) {
        ArrayList<Vertex> rNonInner = new ArrayList();
        for (Vertex niVert : nonInnerVertices) {
            rNonInner.add(Utility.rotateVertex(niVert, a1, angle));
        }
        
        // If SL hits edge there are 2 non-inner verts at that y height
        // The right-most non-inner vert is the "chosen" one and should
        // Only be shown. the left-most is stored for B3S calculations
        // But should not be displayed
        Vertex[] ray;
        if (nonInnerVertices.size() == 2) {
            ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
            this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
            ray = findBisectorRay(g, a1, nonInnerVertices.get(1), quad);
            this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
        }
        
        double tolerance = 0.00001;
        if (nonInnerVertices.size() == 3 && Math.abs(rNonInner.get(0).y - rNonInner.get(1).y) < tolerance) {
            if (rNonInner.get(0).x < rNonInner.get(1).x) {
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                this.coneID ++;
            }
            
            ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
            this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
        }
        
        if (nonInnerVertices.size() == 3 && Math.abs(rNonInner.get(1).y - rNonInner.get(2).y) < tolerance) {
            Utility.debugPrintln("here");
            ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
            this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen"));
            
            if (rNonInner.get(1).x < rNonInner.get(2).x) {
                ray = findBisectorRay(g, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(g, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                this.coneID ++;
            }
        }
        
        if (nonInnerVertices.size() == 4) {
            if (rNonInner.get(0).x < rNonInner.get(1).x) {
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(h, a1, nonInnerVertices.get(0), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(h, a1, nonInnerVertices.get(1), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                this.coneID ++;
            }
            
            if (rNonInner.get(2).x < rNonInner.get(3).x) {
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(3), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                this.coneID ++;
            } else{
                ray = findBisectorRay(g, a1, nonInnerVertices.get(2), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_chosen_cone=" + coneID));
                ray = findBisectorRay(g, a1, nonInnerVertices.get(3), quad);
                this.voronoiEdgesB2S.add(new Bisector(new Vertex[]{p1, p2}, ray[0], ray[1], "b2s_hidden_cone=" + coneID));
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
    private Vertex[] findInnerVertices(Quadrilateral q, double angle) {
        Vertex[] innerVerts = new Vertex[2], rVerts = new Vertex[4];
        Utility.debugPrint("Rotated quad: ");
        // Rotate all quad vertices
        for (int i = 0; i < q.getVertices().length; i ++) {
            rVerts[i] = Utility.rotateVertex(q.getVertices()[i], q.getCenter(), angle);
            Utility.debugPrint(rVerts[i] + " ");
        }
        Utility.debugPrintln("");
        
        // Sort rotated quad vertices by ascending y value (more or less sweep line)
        Arrays.sort(rVerts, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex p1, Vertex p2) {
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
        
        //Utility.debugPrintln("Inner verts: " + innerVerts[0] + " " + innerVerts[1]);
        return innerVerts;
    }
    
    /**
     * Find intersection vertices of lines through inner vertices with the right side of the quad around the left vertex a1
     * 
     * @param h1 Will be assigned. Intersection vertex of line through upper inner vertex with right side of quad
     * @param g1 Will be assigned. Intersection vertex of line through lower inner vertex with right side of quad
     * @param h2 Will be assigned. Intersection vertex of line through upper inner vertex with left side of quad
     * @param g2 Will be assigned. Intersection vertex of line through lower inner vertex with left side of quad
     * @param a1 Left vertex
     * @param a1 Right vertex
     * @param q Quadrilateral to iterate over
     * @param innerVerts Array of size two holding the inner vertices on the quad
     * @param slope Slope of the lines through inner vertices
     */
    private void findh12g12(Vertex h1, Vertex h2, Vertex g1, Vertex g2, Vertex a1, Vertex a2, Quadrilateral q, Vertex[] innerVerts, double angle) {
        Vertex temph1 = null, temph2 = null, tempg1 = null, tempg2 = null;
        
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
        
        //Utility.debugPrintln("temph1 = " + temph1 + ", temph2 = " + temph2);
        //Utility.debugPrintln("tempg1 = " + tempg1 + ", tempg2 = " + tempg2);
        
        Vertex[] rVerts = new Vertex[q.getVertices().length];
        // Rotate all quad vertices so a1a2 is parallel to x axis
        for (int i = 0; i < q.getVertices().length; i ++) {
            rVerts[i] = Utility.rotateVertex(q.getVertices()[i], q.getCenter(), angle);
        }
        
        // Horizontal lines going through the inner vertices
        Vertex[] l1 = {new Vertex(-Utility.RAY_SIZE, innerVerts[0].y), new Vertex(Utility.RAY_SIZE, innerVerts[0].y)};
        Vertex[] l2 = {new Vertex(-Utility.RAY_SIZE, innerVerts[1].y), new Vertex(Utility.RAY_SIZE, innerVerts[1].y)};
        
        // Find other h and g vertices and rotate quad back to its original place
        int j;
        for (int i = 0; i < q.getVertices().length; i ++) {
            if (i == 3) {
                j = 0;
            } else {
                j = i + 1;
            }
            Vertex intersectionVertex1;
            // Found an h
            if ((intersectionVertex1 = Utility.doLineSegmentsIntersect(l1[0], l1[1], rVerts[i], rVerts[j])) != null && !intersectionVertex1.equals(innerVerts[0])) {
                if (temph1 == null && intersectionVertex1.x > temph2.x) {
                    temph1 = intersectionVertex1;
                } else if (temph2 == null) {
                    temph2 = intersectionVertex1;
                }
            }
            
            Vertex intersectionVertex2;
            
            // found a g
            if ((intersectionVertex2 = Utility.doLineSegmentsIntersect(l2[0], l2[1], rVerts[i], rVerts[j])) != null && !intersectionVertex2.equals(innerVerts[1])) {
                if (tempg1 == null && intersectionVertex2.x > tempg2.x) {
                    tempg1 = intersectionVertex2;
                } else if (tempg2 == null) {
                    tempg2 = intersectionVertex2;
                }
            }
        }
        
        //Utility.debugPrintln("temph1 = " + temph1 + ", temph2 = " + temph2);
        //Utility.debugPrintln("tempg1 = " + tempg1 + ", tempg2 = " + tempg2);
        
        // Assert that temph1.x > temph2.x and tempg1.x > tempg2.x
        if (temph1.x < temph2.x) {
            Vertex temp = new Vertex(temph1.x, temph1.y);
            
            temph1.x = temph2.x;
            temph1.y = temph2.y;
            
            temph2.x = temp.x;
            temph2.y = temp.y;
        }
        if (tempg1.x < tempg2.x) {
            Vertex temp = new Vertex(tempg1.x, tempg1.y);
            
            tempg1.x = tempg2.x;
            tempg1.y = tempg2.y;
            
            tempg2.x = temp.x;
            tempg2.y = temp.y;
        }
        
        // Rotate vertices back to original coordinate system and translate to a1 and a2
        temph1 = Utility.rotateVertex(temph1, q.getCenter(), -angle);
        temph2 = Utility.rotateVertex(temph2, q.getCenter(), -angle);
        tempg1 = Utility.rotateVertex(tempg1, q.getCenter(), -angle);
        tempg2 = Utility.rotateVertex(tempg2, q.getCenter(), -angle);
        
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
     * Determine vertex where rays through h1 and h2 and through g1 and g2 intersect
     * 
     * @param a1 Envertex of new ray
     * @param h1 Vertex on new ray
     * @param a2 Envertex of new ray
     * @param h2 Vertex on new ray
     * @return Intersection vertex of rays
     */
    private Vertex doRaysIntersect(Vertex a1, Vertex h1, Vertex a2, Vertex h2) {
        
        // Rotate a1h1 to be horizontal with x axis
        double angle = Utility.calculateAngle(a1, h1); // Angle that slope(a1h1) makes with x axis
        
        Vertex ra1 = Utility.rotateVertex(a1, Utility.midpoint(a1, h1), angle);
        Vertex rh1 = Utility.rotateVertex(h1, Utility.midpoint(a1, h1), angle);
        
        // Define the ray a1h1 and rotate back to original position
        double rayEndx1 = Utility.RAY_SIZE;
        double rayEndy1 = rh1.y;
        if (a1.x > h1.x) {
            rayEndx1 = -Utility.RAY_SIZE;
        }
        
        //Utility.debugPrintln("raya1h1 end = " + new Vertex(rayEndx1, rayEndy1));
        Vertex[] raya1h1 = new Vertex[2];
        if (a1.x == h1.x) {
            raya1h1[0] = new Vertex(a1.x, a1.y);
            raya1h1[1] = new Vertex(a1.x, (a1.y < h1.y) ? Utility.RAY_SIZE : -Utility.RAY_SIZE);
        } else {
            raya1h1[0] = Utility.rotateVertex(new Vertex(ra1.x, ra1.y), Utility.midpoint(a1, h1), -angle);
            raya1h1[1] = Utility.rotateVertex(new Vertex(rayEndx1, rayEndy1), Utility.midpoint(a1, h1), -angle);
        }
        
        this.displayEdges.add(new Bisector(new Vertex[]{}, raya1h1[0], raya1h1[1], "b2s_step"));
        
        // Rotate a2h2 to be horizontal with x axis
        angle = Utility.calculateAngle(a2, h2);
        
        Vertex ra2 = Utility.rotateVertex(a2, Utility.midpoint(a2, h2), angle);
        Vertex rh2 = Utility.rotateVertex(h2, Utility.midpoint(a2, h2), angle);
        
        // Define the ray a1h1 and rotate back to original position
        double rayEndx2 = Utility.RAY_SIZE;
        double rayEndy2 = rh2.y;
        if (a2.x > h2.x) {
            rayEndx2 = -Utility.RAY_SIZE;
        }
        
        //Utility.debugPrintln("raya2h2 end = " + new Vertex(rayEndx2, rayEndy2));
        Vertex[] raya2h2 = new Vertex[2];
        if (a2.x == h2.x) {
            raya2h2[0] = new Vertex(a2.x, a2.y);
            raya2h2[1] = new Vertex(a2.x, (a2.y < h2.y) ? Utility.RAY_SIZE : -Utility.RAY_SIZE);
        } else {
            raya2h2[0] = Utility.rotateVertex(new Vertex(ra2.x, ra2.y), Utility.midpoint(a2, h2), -angle);
            raya2h2[1] = Utility.rotateVertex(new Vertex(rayEndx2, rayEndy2), Utility.midpoint(a2, h2), -angle);
        }
        
        this.displayEdges.add(new Bisector(new Vertex[]{}, raya2h2[0], raya2h2[1], "b2s_step"));
        
        //Utility.debugPrintln("comparing " + raya1h1[0] + ", " + raya1h1[1] + " and " + raya2h2[0] + ", " + raya2h2[1]);
        //Utility.debugPrintln(slope(a1, h1) + " : " + slope(a2, h2));
        double tolerance = 0.00001;
        if (Math.abs(Utility.slope(a1, h1) - Utility.slope(a2, h2)) < tolerance || (Utility.slope(a1, h1) == Double.POSITIVE_INFINITY && Utility.slope(a2, h2) == Double.NEGATIVE_INFINITY) || (Utility.slope(a1, h1) == Double.NEGATIVE_INFINITY && Utility.slope(a2, h2) == Double.POSITIVE_INFINITY)) {
            Utility.debugPrintln("Handling degenerate case for main bisector segment !!!");
            ra1 = Utility.rotateVertex(a1, Utility.midpoint(a1, a2), angle);
            ra2 = Utility.rotateVertex(a2, Utility.midpoint(a1, a2), angle);
            rh1 = Utility.rotateVertex(h1, Utility.midpoint(a1, a2), angle);
            rh2 = Utility.rotateVertex(h2, Utility.midpoint(a1, a2), angle);
            
            /*Utility.debugPrintln("Vertices before 1st rotation: a1.x = " + a1.x + ", h1.x = " + h1.x + " a2.x = " + a2.x + ", h2.x = " + h2.x);
            Utility.debugPrintln("Vertices before 2nd rotation: a1.x = " + ra1.x + ", h1.x = " + rh1.x + " a2.x = " + ra2.x + ", h2.x = " + rh2.x);
            Utility.debugPrintln("Vertex before 2nd rotation: " + new Vertex((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y));
            Utility.debugPrintln(rotateVertex(new Vertex((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y), midpoint(a1, a2), -angle));*/
            
            return Utility.rotateVertex(new Vertex((ra1.x*rh2.x - rh1.x*ra2.x) / (ra1.x - rh1.x + rh2.x - ra2.x), rh2.y), Utility.midpoint(a1, a2), -angle);
        } else {
            return Utility.doLineSegmentsIntersect(raya1h1[0], raya1h1[1], raya2h2[0], raya2h2[1]);
        }
    }
    
    /**
     * Constructs a ray from a through nonInnerVertex then translated to endPt
     * 
     * @param endPt Endvertex of main bisector
     * @param a Vertex in a quad
     * @param nonInnerVertex A vertex of the quad with an extreme y value
     */
    private Vertex[] findBisectorRay(Vertex endPt, Vertex a, Vertex niVertex, Quadrilateral quad) {
        // NonInnerVertex is relative to Quadrilateral. Translate relative to a
        Vertex nonInnerVertex = new Vertex(niVertex.x, niVertex.y);
        nonInnerVertex.x += a.x - quad.getCenter().x;
        nonInnerVertex.y += a.y - quad.getCenter().y;
        
        //Utility.debugPrintln("endPt = " + endPt + ", a = " + a + ", nonInnerVertex = " + nonInnerVertex);
        
        // Define the direction of the ray starting at a
        double rayEndx = Utility.RAY_SIZE;
        //Utility.debugPrintln(a + " : " + nonInnerVertex);
        if (a.x > nonInnerVertex.x || (a.x == nonInnerVertex.x && a.y > nonInnerVertex.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        Vertex rayEnd = new Vertex(rayEndx, a.y); // End vertex of ray which is basically + or - infinity
        
        double angle = Utility.calculateAngle(a, nonInnerVertex); // Angle that slope(a, nonInnerVertex) makes with x axis
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Vertex[] ray = {new Vertex(a.x, a.y), Utility.rotateVertex(rayEnd, new Vertex(0,0), -angle)};
        
        //Utility.debugPrintln("ray = " + ray[0] + ", " + ray[1]);
        
        //Translate ray so that it starts at endPt
        ray[0].x += endPt.x - a.x;
        ray[0].y += endPt.y - a.y;
        ray[1].x += endPt.x - a.x;
        ray[1].y += endPt.y - a.y;
        
        //this.voronoiEdges.add(new VoronoiBisector(ray[0], ray[1]));
        return new Vertex[]{ray[0], ray[1]};
    }
    
    /**
     * 
     * @return Deep copy of the VoronoiBisector List for B2S
     */
    public Bisector[] getVoronoiEdges() {
        return Utility.deepCopyVBArray(this.voronoiEdgesB2S.toArray(new Bisector[this.voronoiEdgesB2S.size()]));
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
     * @return Deep copy array of h1 List
     */
    public Vertex[] geth1() {
        return Utility.deepCopyVertexArray(this.h1.toArray(new Vertex[this.h1.size()]));
    }
    
    /**
     * 
     * @return Deep copy array of h2 List
     */
    public Vertex[] geth2() {
        return Utility.deepCopyVertexArray(this.h2.toArray(new Vertex[this.h2.size()]));
    }
    
    /**
     * 
     * @return Deep copy array of g1 List
     */
    public Vertex[] getg1() {
        return Utility.deepCopyVertexArray(this.g1.toArray(new Vertex[this.g1.size()]));
    }
    
    /**
     * 
     * @return Deep copy array of g2 List
     */
    public Vertex[] getg2() {
        return Utility.deepCopyVertexArray(this.g2.toArray(new Vertex[this.g2.size()]));
    }
    
}
