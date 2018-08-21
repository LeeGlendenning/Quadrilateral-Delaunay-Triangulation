package dt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Constructs a Voronoi diagram from a vertex set using a Quadrilateral
 *
 * @author Lee Glendenning
 */
public class DelaunayTriangulation extends JPanel {

    private Graph dtGraph;
    private List<Vertex> voronoiVertices; // voronoiVertices used for animation
    protected Quadrilateral quad;
    // Consider using synchronized list to avoid concurrent modification...
    private List<Bisector> displayEdges;
    private  HashMap<List<Vertex>, List<Bisector>> chosenB2S;
    private List<Bisector> chosenB3S;
    private double curScale;
    private int scaleIterations;
    private Timer timer;
    private FindBisectorsTwoSites b2s;
    private FindBisectorsThreeSites b3s;
    private final Painter painter;
    private double[][] shortestPaths; // Shortest path lengths between any 2 vertices in the DT
    private Integer[][] next; // Holds shortestPaths indices for finding path between 2 vertices
    private double stretchFactor;
    private final Integer[] sfVertices;
    private ArrayList<Vertex> curSelectedPath, oldSelectedPath; // Last path user has queried
    private ArrayList<int[]> performanceData;
    private final int vertexRadius = 3;
    
    private boolean showB2S_hgRegion = false, showB2S_hgVertices = false, showB2S_hiddenCones = false, showB2S = false;
    private boolean showB3S_fgRegion = false, showB3S_hidden = false, showB3S = true;
    private final boolean doAnimation = false;
    private boolean showCoordinates = true, highlightShortestPath = true, clearSelectedPath = false, showBoundaryTriangle = true;
    
    private boolean startMovingVertex = false;
    private Vertex movingVertex = null, movingVertexOldLoc = null, movingVertexOriginalLoc = null;
    private int movingVertIndex = -1;
    private int mouseX, mouseY;
    
    //private final ArrayList<Vertex> h1, h2, g1, g2;

    /**
     * Construct Voronoi diagram for vertex set using a Quadrilateral
     *
     * @param q Quadrilateral
     * @param vertices Vertex set
     */
    public DelaunayTriangulation(Quadrilateral q, ArrayList<Vertex> vertices) {
        //this.dtGraph.getVertices() = new ArrayList();
        this.quad = q;
        this.painter = new Painter();
        this.mouseX = this.mouseY = 0;
        
        this.displayEdges = Collections.synchronizedList(new ArrayList());
        this.voronoiVertices = Collections.synchronizedList(new ArrayList());
        this.scaleIterations = 0;
        this.curScale = 1.0;
        this.stretchFactor = 0;
        this.sfVertices = new Integer[]{null, null};
        this.curSelectedPath = new ArrayList();
        this.oldSelectedPath = new ArrayList();
        this.chosenB3S = new ArrayList();
        this.performanceData = new ArrayList();
        this.dtGraph = new Graph(800, 700);
        
        this.b2s = new FindBisectorsTwoSites();
        this.b3s = new FindBisectorsThreeSites(this.getBounds().getSize().height, this.getBounds().getSize().width);
        
        addVertexSet(vertices);
        
        if (this.doAnimation) {
            doVoronoiAnimation(40, 1000, b2s, b3s);
        }
    }
    
    /**
     * Calls addVertex for initial vertex set passed to constructor
     */
    private void addVertexSet(List<Vertex> pts) {
        for (int i = 0; i < pts.size(); i ++) {
            addVertex(pts.get(i));
        }
    }
    
    /**
     * Reset all instance variables
     */
    public void reset() {
        this.displayEdges = Collections.synchronizedList(new ArrayList());
        this.voronoiVertices = Collections.synchronizedList(new ArrayList());
        this.dtGraph = new Graph(800, 700);
        this.scaleIterations = 0;
        this.chosenB2S = new HashMap();
        this.chosenB3S = new ArrayList();
        this.performanceData = new ArrayList();
        this.oldSelectedPath = new ArrayList();
        this.curSelectedPath = new ArrayList();
        this.sfVertices[0] = this.sfVertices[1] = null;
        this.stretchFactor = 0.0;
        
        this.b2s = new FindBisectorsTwoSites();
        this.b3s = new FindBisectorsThreeSites(this.getBounds().getSize().height, this.getBounds().getSize().width);
        this.repaint();
    }
    
    
    
    
    
    /**
     * Delaunay triangulation methods
     */
    
    /**
     * Find B2S, B3S, triangulate the new point, then update the shortest paths
     * locate triangle v is in. connect v to triangle. Check if point in quad for v and each pair of edges in triangle.
     *     if vertex in quad, edge must be flipped
     * 
     * @param v New Vertex to add to the Delaunay triangulation
     */
    public void addVertex(Vertex v) {
        if (this.dtGraph.getVertices().contains(v)) {
            Utility.debugPrintln("Vertex not added. Already exists.");
            return;
        }
        Utility.debugPrintln("Adding vertex " + v + "\n");
        
        this.performanceData.add(new int[4]); // Init new performance data array for runtime tracking
        
        Vertex[] triangle = this.dtGraph.locateTriangle(v);
        Utility.debugPrintln("V contained by vertices: " + Arrays.toString(triangle));
        
        // Connect new vertex with all vertices in the located triangle
        this.dtGraph.addVertex(v);
        for (Vertex tVert : triangle) {
            this.dtGraph.addEdge(v, tVert);
        }
        
        HashMap<List<Vertex>, List<Bisector>> bisectors2S = calculateB2S(v, Arrays.asList(triangle));
        this.chosenB2S = bisectors2S;
        List<Bisector> bisectors3S = calculateB3S(v, Arrays.asList(triangle), bisectors2S); // Calculate b3s' for v and containing triangle
        this.chosenB3S = new ArrayList();
        for (Bisector b : bisectors3S) {
            this.chosenB3S.add(b.deepCopy());
            // Set scaling for minQuad
            calculateMinQuad(this.chosenB3S.get(this.chosenB3S.size()-1));
        }
        
        checkForBadEdges(v, bisectors3S); // If necessary, flip bad edges
        
        // Calculate all pairs shortest paths and stretch factor of the DT
        updateShortestPaths();
        
        Utility.debugPrintln("");
        repaint();
    }
    
    /**
     * 
     * @param v Vertex to find all B2S for
     * @param vertices List of vertices to calculate B2S with v
     */
    private HashMap<List<Vertex>, List<Bisector>> calculateB2S(Vertex v, List<Vertex> vertices) {
        HashMap<List<Vertex>, List<Bisector>> tempB2S = new HashMap();
        // Find B2S between p and other vertices
        long startTime = System.nanoTime();
        int ii;
        for (int i = 0; i < vertices.size(); i++) {
            if (i == vertices.size()-1) {
                ii = 0;
            } else {
                ii = i+1;
            }
            // Calculate b2s between each of the vertices in the list
            tempB2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, vertices.get(i).deepCopy(), vertices.get(ii).deepCopy()));
            // Calculate b2s between v and each of the vertices in the list
            tempB2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, vertices.get(i).deepCopy(), v));
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        this.performanceData.get(this.performanceData.size()-1)[0] = Math.round(duration);
        
        Utility.debugPrintln("");
        //this.displayEdges.addAll(this.b2s.getDisplayEdges());
        
        return tempB2S;
    }
    
    /**
     * 
     * @param v Vertex to find all B3S for
     * @param vertices List of vertices to calculate B3S with v
     */
    private List<Bisector> calculateB3S(Vertex v, List<Vertex> vertices, HashMap<List<Vertex>, List<Bisector>> bisectors2S) {
        List<Bisector> tempB3S = new ArrayList();
        // Find B3S between p and all other pairs of vertices
        long startTime = System.nanoTime();
        for (int i = 0; i < vertices.size(); i ++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                Utility.debugPrintln("Finding B3S between: " + vertices.get(i).deepCopy() + ", " + vertices.get(j).deepCopy() + ", and v = " + v);
                Bisector b = this.b3s.findBisectorOfThreeSites(this.quad, bisectors2S, vertices.get(i).deepCopy(), vertices.get(j).deepCopy(), v);
                if (b != null) {
                    tempB3S.add(b);
                }
            }
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        this.performanceData.get(this.performanceData.size()-1)[1] = Math.round(duration);
        Utility.debugPrintln("");
        
        if (showB3S_fgRegion) {
            this.displayEdges.addAll(cleanFGEdges(this.b3s.getDisplayEdges()));
        }
        return tempB3S;
    }
    
    /**
     * 
     * @param v Newly triangulated vertex
     */
    private void checkForBadEdges(Vertex v, List<Bisector> b3sList) {
        long startTime = System.nanoTime();
        //Utility.debugPrintln("Checking " + b3sList.size() + " b3s'");
        for (int i = 0; i < b3sList.size(); i ++) {
            Bisector b = b3sList.get(i);
            Vertex vInQuad;
            
            // List holds the vertices that could possibly be inside the min quads for the b3s
            List<Vertex> ptsToCheck = new ArrayList();
            //Utility.debugPrintln("Finding intersection of neighbour points for " + b.getAdjacentPtsArray()[0] + " and " + b.getAdjacentPtsArray()[1]);
            ptsToCheck.addAll(intersectVertexSets(this.dtGraph.getVertex(b.getAdjacentPtsArray()[0].x, b.getAdjacentPtsArray()[0].y).getNeighbours(), 
                    this.dtGraph.getVertex(b.getAdjacentPtsArray()[1].x, b.getAdjacentPtsArray()[1].y).getNeighbours()));
            //Utility.debugPrintln("Finding intersection of neighbour points for " + b.getAdjacentPtsArray()[1] + " and " + b.getAdjacentPtsArray()[2]);
            ptsToCheck.addAll(intersectVertexSets(this.dtGraph.getVertex(b.getAdjacentPtsArray()[1].x, b.getAdjacentPtsArray()[1].y).getNeighbours(), 
                    this.dtGraph.getVertex(b.getAdjacentPtsArray()[2].x, b.getAdjacentPtsArray()[2].y).getNeighbours()));
            //Utility.debugPrintln("Finding intersection of neighbour points for " + b.getAdjacentPtsArray()[2] + " and " + b.getAdjacentPtsArray()[0]);
            ptsToCheck.addAll(intersectVertexSets(this.dtGraph.getVertex(b.getAdjacentPtsArray()[2].x, b.getAdjacentPtsArray()[2].y).getNeighbours(), 
                    this.dtGraph.getVertex(b.getAdjacentPtsArray()[0].x, b.getAdjacentPtsArray()[0].y).getNeighbours()));
            
            //Utility.debugPrintln("Finding minQuad for " + this.chosenB3S.get(i).getEndVertex());
            if ((vInQuad = vertexInsideQuad(calculateMinQuad(this.chosenB3S.get(i)), b.getAdjacentPtsList(), ptsToCheck)) != null) {
                Vertex v1 = null, v2 = null;
                // Get the two vertices in the triangle that aren't v
                for (Vertex adjV : b.getAdjacentPtsArray()) {
                    if (!adjV.equals(v) && !adjV.equals(vInQuad)) {
                        if (v1 == null) {
                            v1 = adjV;
                        } else {
                            v2 = adjV;
                        }
                    }
                }
                
                if ((v1 == null || v2 == null /*TODO: verify this is correct*/) || 
                        this.dtGraph.getBoundaryTriangle().contains(v1) && 
                        this.dtGraph.getBoundaryTriangle().contains(v2)) {
                    Utility.debugPrintln("vInQuad was either part of min quad or boundary triangle. Skipped.");
                    // The "bad edge" is a boundary edge so don't flip it
                    return;
                }
                
                // Flip the edge defined by the 2 vertices in the triangle that aren't v
                // Then calculate new b3s' and add to queue
                Utility.debugPrintln("Flipping bad edge");
                if (flipEdge(new Edge(v1, v2), v, vInQuad)) {
                    // Calculate b2s between (v,v1) (v,v2) and (v,vInQuad)
                    HashMap<List<Vertex>, List<Bisector>> bisectors2S = new HashMap();
                    bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v.deepCopy(), v1.deepCopy()));
                    bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v.deepCopy(), v2.deepCopy()));
                    bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v.deepCopy(), vInQuad.deepCopy()));
                    
                    // Calculate b3s for (v1, v, vInQuad) and (v2, v, vInQuad) and add to b3sList to be checked in further iterations
                    Utility.debugPrintln("calcing b3s for " + v + ", " + v1 + ", " + vInQuad);
                    b3sList.add(this.b3s.findBisectorOfThreeSites(this.quad, bisectors2S, v1.deepCopy(), vInQuad.deepCopy(), v.deepCopy()));
                    Utility.debugPrintln("calcing b3s for " + v + ", " + v2 + ", " + vInQuad);
                    b3sList.add(this.b3s.findBisectorOfThreeSites(this.quad, bisectors2S, v2.deepCopy(), vInQuad.deepCopy(), v.deepCopy()));
                    
                    // TODO: deal with timing stuff?
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1000000;
                    this.performanceData.get(this.performanceData.size()-1)[2] = Math.round(duration);
                    
                    return;
                }
            }
        }
        
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        this.performanceData.get(this.performanceData.size()-1)[2] = Math.round(duration);
    }
    
    /**
     * 
     * @param vSet1 An array of vertices
     * @param vSet2 An array of vertices
     * @return The vertex in common between the two arrays
     */
    private List<Vertex> intersectVertexSets(List<Vertex> vSet1, List<Vertex> vSet2) {
        List<Vertex> intersection = new ArrayList();
        for (Vertex v1 : vSet1) {
            for (Vertex v2 : vSet2) {
                if (v1.equals(v2)) {
                    //Utility.debugPrintln("Common vertex: " + v1);
                    intersection.add(v1);
                }
            }
        }
        
        if (intersection.isEmpty()) {
            Utility.debugPrintln("[intersectVertexSets] Intersection of vertex sets is empty. This shouldn't happen");
        }
        
        return intersection;
    }
    
    /**
     * 
     * @param e Edge to flip
     * @param newV Newly added vertex
     * @param vInQuad Vertex causing the edge flip
     */
    private boolean flipEdge(Edge e, Vertex newV, Vertex vInQuad) {
        // Check if flipping edge leads to edge overlap
        for (Edge compareEdge : this.dtGraph.getEdges()) {
            Vertex intersectionPt = Utility.doLineSegmentsIntersect(compareEdge.getVertices()[0], 
                    compareEdge.getVertices()[1], newV, vInQuad);
            if (!compareEdge.equals(e) && intersectionPt != null && 
                    !intersectionPt.equals(newV) && !intersectionPt.equals(vInQuad)) {
                Utility.debugPrintln("Edge flip would leave to edge overlap. skipping...");
                return false;
            }
        }
        
        Utility.debugPrintln("Flipping edge " + e);
        this.dtGraph.removeEdge(e);
        Utility.debugPrintln("Adding new flipped edge " + new Edge(newV, vInQuad));
        this.dtGraph.addEdge(newV, vInQuad);
        return true;
    }
    
    /**
     * Update shortest paths matrix and stretch factor of the DT
     */
    private void updateShortestPaths() {
        this.oldSelectedPath = this.curSelectedPath;
        this.curSelectedPath = new ArrayList();
        long startTime = System.nanoTime();
        this.shortestPaths = findAllPairsShortestPath();
        
        updateStretchFactor();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        this.performanceData.get(this.performanceData.size()-1)[3] = Math.round(duration);
    }
    
    /**
     * @param quad Array of vertices defining the quad
     * @param vIgnore List of vertices on the quad boundary
     * @param pts List of vertices to check
     * @return True if a vertex in the vertex set lies inside quad. False otherwise
     */
    private Vertex vertexInsideQuad(Vertex[] quad, List<Vertex> vIgnore, List<Vertex> pts) {
        for (Vertex v : pts) {
            Utility.debugPrintln("Checking if " + v + " inside quad");
            if (!vIgnore.contains(v) &&
                    Utility.isLeftOfSegment(quad[0], quad[1], v, 0.1) == -1 &&
                    Utility.isLeftOfSegment(quad[1], quad[2], v, 0.1) == -1 &&
                    Utility.isLeftOfSegment(quad[2], quad[3], v, 0.1) == -1 &&
                    Utility.isLeftOfSegment(quad[3], quad[0], v, 0.1) == -1) {
                Utility.debugPrintln("Vertex " + v + " inside");
                return v;
            }
        }
        Utility.debugPrintln("No vertex inside quad");
        return null;
    }
    
    /**
     * @param chosenB3S Chosen Bisector between 3 vertices
     * @return Min quad around given chosenB3S
     */
    public Vertex[] calculateMinQuad(Bisector chosenB3S) {
        Double scale;
        if (chosenB3S.getTag().contains("chosen") && (scale = findMinimumQuadScaling(chosenB3S)) != null) {
            //Utility.debugPrintln("Set scale = " + scale + "\n");
            chosenB3S.setMinQuadScale(scale);
            return this.quad.getPixelVertsForVertex(chosenB3S.getEndVertex(), scale);
        } else {
            System.out.println("[calculateMinQuad] DID NOT SET SCALE! - this is a problem");
        }
        return null;
    }
    
    /**
     * @param chosenB3S Chosen VoronoiBisector between 3 sites
     * @return Amount the quad needs to be scaled such that it goes through the adjacent B3S vertices
     */
    private Double findMinimumQuadScaling(Bisector chosenB3S) {
        Vertex[] qVerts = this.quad.getPixelVertsForVertex(chosenB3S.getEndVertex(), this.curScale);
        /*Utility.debugPrintln("qVerts for " + chosenB3S.getEndVertex());
        for (Vertex p : qVerts) {
            Utility.debugPrint(p + " ");
        }
        Utility.debugPrintln();*/
        
        Vertex[][] quadRays = new Vertex[this.quad.getVertices().length][2]; // Rays from quad center through each vertex
        for (int i = 0; i < this.quad.getVertices().length; i ++) {
            quadRays[i] = findMinQuadRay(chosenB3S.getEndVertex(), chosenB3S.getEndVertex(), qVerts[i]);
            //this.displayEdges.add(new VoronoiBisector(new Vertex[]{}, quadRays[i][0], quadRays[i][1], "debug"));
        }
        
        Double scale = null, tempScale;
        for (Vertex adj : chosenB3S.getAdjacentPtsArray()) {
            tempScale = findScaleForAdjacentB3SPt(adj, quadRays, qVerts, chosenB3S.getEndVertex());
            //Utility.debugPrintln(tempScale);
            if (scale == null || tempScale > scale) {
                scale = tempScale;
            }
        }
        
        return scale;
    }
    
    /**
     * 
     * @param adj A Vertex belonging to the B3S
     * @param quadRays Rays from quad center through each vertex
     * @param qVerts Vertex array of quad vertices
     * @return Quad scaling based on intersection of adj Vertex parallel to all quad vertices and the quad rays
     */
    private Double findScaleForAdjacentB3SPt(Vertex adj, Vertex[][] quadRays, Vertex[] qVerts, Vertex chosenB3SPt) {
        Double scale = null;
        //Utility.debugPrintln("chosenB3S vertex = " + chosenB3SPt);
        
        Vertex intersectionPt;
        
        int ii;
        // For each edge of the quad
        for (int i = 0; i < qVerts.length; i ++) {
            if (i == qVerts.length-1) {
                ii = 0;
            } else {
                ii = i+1;
            }
            
            double tempScale;
            // Create ray from adj parallel to quad edge
            Vertex[] intersectionRay1 = findMinQuadRay(adj, qVerts[i], qVerts[ii]);
            //this.displayEdges.add(new VoronoiBisector(new Vertex[]{}, intersectionRay1[0], intersectionRay1[1], "debug"));
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay1[0], intersectionRay1[1], quadRays[i][0], quadRays[i][1])) != null) {
                //Utility.debugPrintln("qVerts[i] = " + qVerts[i] + ", chosenB3SPt = " + chosenB3SPt);
                //Utility.debugPrintln("1dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[i], chosenB3SPt));
                tempScale = Utility.euclideanDistance(intersectionPt, chosenB3SPt) / Utility.euclideanDistance(qVerts[i], chosenB3SPt);
                if (scale == null || tempScale > scale) {
                    scale = tempScale;
                }
            }
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay1[0], intersectionRay1[1], quadRays[ii][0], quadRays[ii][1])) != null) {
                //Utility.debugPrintln("2dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[ii], chosenB3SPt));
                tempScale = Utility.euclideanDistance(intersectionPt, chosenB3SPt) / Utility.euclideanDistance(qVerts[ii], chosenB3SPt);
                if (scale == null || tempScale > scale) {
                    scale = tempScale;
                }
            }
            
            // Create ray from adj parallel to quad edge in other direction
            Vertex[] intersectionRay2 = findMinQuadRay(adj, qVerts[ii], qVerts[i]);
            //this.displayEdges.add(new VoronoiBisector(new Vertex[]{}, intersectionRay2[0], intersectionRay2[1], "debug"));
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay2[0], intersectionRay2[1], quadRays[i][0], quadRays[i][1])) != null) {
                //Utility.debugPrintln("3dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[i], chosenB3SPt));
                tempScale = Utility.euclideanDistance(intersectionPt, chosenB3SPt) / Utility.euclideanDistance(qVerts[i], chosenB3SPt);
                if (scale == null || tempScale > scale) {
                    scale = tempScale;
                }
            }
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay2[0], intersectionRay2[1], quadRays[ii][0], quadRays[ii][1])) != null) {
                //Utility.debugPrintln("4dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[ii], chosenB3SPt));
                tempScale = Utility.euclideanDistance(intersectionPt, chosenB3SPt) / Utility.euclideanDistance(qVerts[ii], chosenB3SPt);
                if (scale == null || tempScale > scale) {
                    scale = tempScale;
                }
            }
        }
        
        return scale;
    }
    
    /**
     * Constructs a ray from startPt through throughPt then translated to translatePt
     * 
     * @param translatePt Vertex ray startPt will be translated to
     * @param startPt Initial start vertex of ray
     * @param throughPt Vertex the ray will pass through before being translated
     */
    private Vertex[] findMinQuadRay(Vertex translatePt, Vertex startPt, Vertex throughPt) {
        //Utility.debugPrintln("endPt = " + endPt + ", a = " + a + ", nonInnerVertex = " + nonInnerVertex);
        
        // Define the direction of the ray starting at a
        double rayEndx = Utility.RAY_SIZE;
        //Utility.debugPrintln(a + " : " + nonInnerVertex);
        if (startPt.x > throughPt.x || (startPt.x == throughPt.x && startPt.y > throughPt.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        Vertex rayEnd = new Vertex(rayEndx, startPt.y); // End vertex of ray which is basically + or - infinity
        
        double angle  = Utility.calculateAngle(startPt, throughPt); // Angle that slope(a, nonInnerVertex) makes with x axis
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Vertex[] ray = {new Vertex(startPt.x, startPt.y), Utility.rotateVertex(rayEnd, new Vertex(0,0), -angle)};
        
        //Utility.debugPrintln("ray = " + ray[0] + ", " + ray[1]);
        
        //Translate ray so that it starts at endPt
        ray[0].x += translatePt.x - startPt.x;
        ray[0].y += translatePt.y - startPt.y;
        ray[1].x += translatePt.x - startPt.x;
        ray[1].y += translatePt.y - startPt.y;
        
        //this.voronoiEdges.add(new VoronoiBisector(ray[0], ray[1]));
        return new Vertex[]{ray[0], ray[1]};
    }
    
    /**
     * Uses Floyd-Warshall algorithm to find the shortest path between each pair of vertices
     * @return 
     */
    private double[][] findAllPairsShortestPath() {
        // 2D array representing distance between each vertex in DT
        double[][] dist = new double[this.dtGraph.getDisplayVertices().size()][this.dtGraph.getDisplayVertices().size()];
        this.next = new Integer[this.dtGraph.getDisplayVertices().size()][this.dtGraph.getDisplayVertices().size()];
        // Initialize 2D array with infinity
        for (double[] arr : dist) {
            Arrays.fill(arr, Double.MAX_VALUE);
        }
        // Initialize 2D array with null
        for (Integer[] arr : this.next) {
            Arrays.fill(arr, null);
        }
        
        // Set distance from vertices sharing an edge to the weight of the edge
        for (Edge e : this.dtGraph.getDisplayEdges()) {
            dist[this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[0])][this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[1])] = e.getWeight();
            dist[this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[1])][this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[0])] = e.getWeight();
        
            next[this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[0])][this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[1])] = this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[1]);
            next[this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[1])][this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[0])] = this.dtGraph.getDisplayVertices().indexOf(e.getVertices()[0]);
        }
        
        // Set distance from a vertex to itself as 0 (diagonal)
        for (int i = 0; i < dist.length; i ++) {
            dist[i][i] = 0;
        }
        
        /*
        6 for k from 1 to |V|
        7    for i from 1 to |V|
        8       for j from 1 to |V|
        9          if dist[i][j] > dist[i][k] + dist[k][j] 
        10             dist[i][j] ← dist[i][k] + dist[k][j]
        11         end if
        */
        for (int k = 0; k < dist.length; k ++) 
        {
            for (int i = 0; i < dist.length; i ++) 
            {
                for (int j = 0; j < dist.length; j ++) 
                {
                    if (dist[i][j] > dist[i][k] + dist[k][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                    
                }
            }
        }
        
        return dist;
    }
    
    /**
     * 
     * @param u Index of a Vertex in the DT
     * @param v Index of a Vertex in the DT
     * @return List of vertices making up a path from u to v
     */
    public ArrayList<Vertex> shortestPath(int u, int v) {
        if (this.next[u][v] == null) {
            return new ArrayList();
        }
        ArrayList<Vertex> path = new ArrayList();
        path.add(this.dtGraph.getDisplayVertices().get(u));
        while (u != v) {
            u = this.next[u][v];
            path.add(this.dtGraph.getDisplayVertices().get(u));
        }
        
        this.oldSelectedPath = new ArrayList(this.curSelectedPath);
        this.curSelectedPath = new ArrayList(path);
        return path;
    }
    
    /**
     * 
     * @param vi Index of v1
     * @param vj Index of v2
     * @return Shortest path length between v1 and v2
     */
    public double getShortestPathLength(int vi, int vj) {
        return this.shortestPaths[vi][vj];
    }
    
    /**
     * 
     * @return Stretch factor of VD
     */
    private void updateStretchFactor() {
        double stretch = -1;
        Integer v1 = null, v2 = null;
        for (int i = 0; i < this.shortestPaths.length; i ++) {
            for (int j = i+1; j < this.shortestPaths.length; j ++) {
                if (this.shortestPaths[i][j] != Double.MAX_VALUE /*Avoid infinitely large paths bc no edge*/&&
                        this.shortestPaths[i][j] / (Utility.euclideanDistance(this.dtGraph.getDisplayVertices().get(i), this.dtGraph.getDisplayVertices().get(j))) > stretch) {
                    stretch = this.shortestPaths[i][j] / (Utility.euclideanDistance(this.dtGraph.getDisplayVertices().get(i), this.dtGraph.getDisplayVertices().get(j)));
                    v1 = i;
                    v2 = j;
                }
            }
        }
        if (v1 != null && v2 != null) {
            this.stretchFactor = stretch;
            this.sfVertices[0] = v1;
            this.sfVertices[1] = v2;
        }
    }
    
    
    
    
    
    
    
    
    
    /*
     *  User Interface methods
     */
    
    /**
     * 
     * @param x X coordinate of mouse click
     * @param y Y coordinate of mouse click
     * @return Vertex at given (x,y) or null if no such vertex exists
     */
    public Vertex vertexAt(int x, int y) {
        for (Vertex v : this.dtGraph.getVertices()) {
            if (v.x <= x+this.vertexRadius && v.x >= x-this.vertexRadius &&
                    v.y <= y+this.vertexRadius && v.y >= y-this.vertexRadius) {
                return v;
            }
        }
        return null;
    }
    
    public void removeVertex(Vertex v) {
        this.dtGraph.removeVertex(this.dtGraph.getVertex(v.x, v.y));
    }
    
    /**
     * 
     * @param v Vertex to move
     * @param x New x location of vertex
     * @param y New y location of vertex
     */
    public void moveVertex(Vertex v, int x, int y) {
        Utility.debugPrintln("Moving vertex " + v + ": " + v.getNeighborCount());
        Vertex vNew = new Vertex(x, y);
        this.movingVertex = null;
        this.movingVertexOldLoc = null;

        this.dtGraph.removeVertex(this.dtGraph.getVertex(v.x, v.y));
        addVertex(vNew);
        this.repaint();
    }
    
    /**
     * 
     * @param v Original location of moving vertex
     */
    public void setMovingVertexLoc(Vertex v) {
        this.movingVertIndex = this.dtGraph.getVertices().indexOf(this.dtGraph.getVertex(v.x, v.y));
        this.movingVertexOriginalLoc = v.deepCopy();
        this.startMovingVertex = true;
    }
    
    /**
     * 
     * @param verts New list of Vertices to create Quad for
     */
    public void newQuad(Vertex[] verts) {
        this.quad = new Quadrilateral(verts);
        Vertex[] tempPts = this.dtGraph.getVertices().toArray(new Vertex[this.dtGraph.getVertices().size()]);
        reset();
        // Reconstruct VoronoiDiagram with new quad
        for (Vertex tempP : tempPts) {
            addVertex(tempP);
        }
    }
    
    /**
     * 
     * @param vertices New list of Vertices to create DT for
     */
    public void newVertexSet(List<Vertex> vertices) {
        reset();
        addVertexSet(vertices);
    }
    
    /**
     * Remove vertex and reconstruct Voronoi Diagram
     * @param i Vertex to remove from vertex set
     */
    public void removeVertex(int i) {
        i += 3; // To account for boundary vertices
        if (this.dtGraph.getVertices().size() > i) {
            this.dtGraph.removeVertex(new Vertex(this.dtGraph.getVertices().get(i).x, this.dtGraph.getVertices().get(i).y));
            Vertex[] tempPts = this.dtGraph.getVertices().toArray(new Vertex[this.dtGraph.getVertices().size()]);
            reset();
            // Reconstruct VoronoiDiagram with remaining vertices
            for (Vertex tempP : tempPts) {
                addVertex(tempP);
            }
        } else {
            Utility.debugPrintln("Couldn't delete vertex because it doesn't exist.");
        }
    }
    
    /**
     * 
     * @param x X coordinate of moving vertex
     * @param y Y coordinate of moving vertex
     */
    public void setMovingVertex(int x, int y) {
        if (this.movingVertex == null ) {
            this.movingVertexOldLoc = new Vertex(x, y);
            this.movingVertex = new Vertex(x, y);
        } else {
            this.movingVertexOldLoc = this.movingVertex.deepCopy();
            this.movingVertex = new Vertex(x, y);
        }
        this.repaint();
    }
    
    /**
     * 
     * @param setting Boolean to set
     */
    public void setShowB2S(boolean setting) {
        this.showB2S = setting;
        this.repaint();
    }
    
    /**
     * 
     * @return Boolean showB2S
     */
    public boolean getShowB2S() {
        return this.showB2S;
    }
    
    /**
     * 
     * @param setting Boolean to set
     */
    public void setShowB3S(boolean setting) {
        this.showB3S = setting;
        this.repaint();
    }
    
    /**
     * 
     * @return Boolean showB3S
     */
    public boolean getShowB3S() {
        return this.showB3S;
    }
    
    /**
     * 
     * @param setting Boolean to set
     */
    public void setOnlyShowChosenB2S(boolean setting) {
        this.showB2S_hiddenCones = !setting;
        this.repaint();
    }
    
    /**
     * 
     * @return Boolean showB2S_hiddenCones
     */
    public boolean getShowB2SHidden() {
        return this.showB2S_hiddenCones;
    }
    
    /**
     * 
     * @param setting Boolean to set
     */
    public void setOnlyShowChosenB3S(boolean setting) {
        this.showB3S_hidden = !setting;
        this.repaint();
    }
    
    /**
     * 
     * @return Boolean showB3S_hidden
     */
    public boolean getShowB3SHidden() {
        return this.showB3S_hidden;
    }
    
    /**
     * 
     * @param setting Boolean to set
     */
    public void setShowCoordinates(boolean setting) {
        this.showCoordinates= setting;
        this.repaint();
    }
    
    /**
     * 
     * @return Boolean showCoordinates
     */
    public boolean getShowCoordinates() {
        return this.showCoordinates;
    }
    
    /**
     * 
     * @param x X coordinate of mouse
     * @param y Y coordinate of mouse
     */
    public void setMouseCoordinates(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        this.repaint();
    }
    
    /**
     * 
     * @param setting Boolean to set
     */
    public void setHighlightShortestPath(boolean setting) {
        this.highlightShortestPath = setting;
        this.repaint();
    }
    
    /**
     * 
     * @param setting Boolean to set
     */
    public void setShowBoundaryTriangle(boolean setting) {
        this.showBoundaryTriangle = setting;
        this.repaint();
    }
    
    /**
     * 
     * @return Boolean highlightShortestPath
     */
    public boolean getHighlightShortestPath() {
        return this.highlightShortestPath;
    }
    
    /**
     * 
     * @return List<Vertex> list of vertices in the DT
     */
    public List<Vertex> getVertices() {
        return this.dtGraph.getVertices();
    }
    
    /**
     * 
     * @return ArrayList holding performance data of operation vs. time
     */
    public ArrayList<int[]> getPerformanceData() {
        return new ArrayList(this.performanceData);
    }
    
    /**
     * 
     * @param fgEdges List of edges that make up the FG region
     * @return List of FG edges ignoring the boundary triangle vertices
     */
    private List<Bisector> cleanFGEdges(List<Bisector> fgEdges) {
        List<Bisector> cleanEdges = new ArrayList();
        List<Vertex> boundaryVerts = this.dtGraph.getBoundaryTriangle();
        Utility.debugPrintln(boundaryVerts.toString());
        for (Bisector b : fgEdges) {
            boolean edgeIsClean = true;
            for (Vertex v : b.getAdjacentPtsArray()) {
                //Utility.debugPrintln("Checking if boundary verts includes " + v);
                if (boundaryVerts.contains(v)) {
                    edgeIsClean = false;
                    break;
                }
            }
            if (edgeIsClean) {
                cleanEdges.add(b);
            }
        }
        return cleanEdges;
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
        int voronoiVertexRadius = 1;
        int yMax = this.getBounds().getSize().height;
        
        if (this.showBoundaryTriangle) {
            painter.drawVerticesAndQuads(g2d, this.dtGraph.getVertices(), this.quad, yMax, this.vertexRadius, this.curScale);
        } else {
            // Take away boundary vertices from vertex set
            painter.drawVerticesAndQuads(g2d, this.dtGraph.getDisplayVertices(), this.quad, yMax, this.vertexRadius, this.curScale);
        }
        
        painter.drawBisectorRayVertices(g2d, this.voronoiVertices, yMax, voronoiVertexRadius);
        
        // Draw bisector segments between 2 sites
        if (this.showB2S && this.chosenB2S != null) {
            painter.drawB2S(g2d, this.chosenB2S, yMax, this.showB2S_hiddenCones);
        }
        
        // Draw bisector segments between 3 sites
        if (this.showB3S && this.showB3S_hidden) {
            painter.drawB3S(g2d, this.b3s.getVoronoiEdges(), yMax);
        }
        
        if (this.showB3S && this.chosenB3S != null) {
            painter.drawChosenB3SAndMinQuads(g2d, this.quad, this.chosenB3S, yMax);
        }
        
        if (this.showB3S_fgRegion /*|| this.showB2S_hgRegion*/) {
            painter.drawFGRegion(g2d, this.displayEdges, yMax/*, this.showB2S_hgRegion, this.showB3S_fgRegion*/);
        }
        
        if (this.showB2S_hgVertices) {
            painter.drawB2S_hgVertices(g2d, b2s.geth1(), b2s.geth2(), b2s.getg1(), b2s.getg2(), yMax, this.vertexRadius);
        }
        
        if (this.showCoordinates) {
            painter.drawVertexCoordinates(g2d, this.dtGraph.getDisplayVertices(), yMax);
        }
        
        painter.drawMouseCoordinates(g2d, mouseX, mouseY, yMax);
        
        if (this.showBoundaryTriangle) {
            painter.drawDelaunayEdges(g2d, this.dtGraph.getEdges(), yMax);
        } else {
            // Take away boundary vertices from vertex set
            painter.drawDelaunayEdges(g2d, this.dtGraph.getDisplayEdges(), yMax);
        }
        
        painter.drawStretchFactor(g2d, this.sfVertices, this.stretchFactor);
        
        if (this.sfVertices[0] != null && this.sfVertices[1] != null) {
            // Clear old highlighted path
            painter.highlightStretchFactorPath(g2d, this.oldSelectedPath, yMax, this.highlightShortestPath, Color.black);
            // Draw desired highlighted path
            painter.highlightStretchFactorPath(g2d, shortestPath(this.sfVertices[0], this.sfVertices[1]), yMax, this.highlightShortestPath, Color.red);
        }
        
        if (this.movingVertex != null) {
            // Paint over old location of vertex
            painter.drawMovingVertex(g2d, this.movingVertexOldLoc, this.quad, this.movingVertIndex-this.dtGraph.getBoundaryTriangle().size(), this.vertexRadius, yMax, this.getBackground());
            // Draw vertex at new location
            painter.drawMovingVertex(g2d, this.movingVertex, this.quad, this.movingVertIndex-this.dtGraph.getBoundaryTriangle().size(), this.vertexRadius, yMax, Color.black);
        }
        
        if (this.startMovingVertex) {
            Utility.debugPrintln("moving vert edge #: " + this.dtGraph.getVertex(movingVertexOriginalLoc.x, movingVertexOriginalLoc.y).getNeighborCount());
            painter.eraseEdgesAndCoords(g2d, this.dtGraph.getVertex(this.movingVertexOriginalLoc.x, this.movingVertexOriginalLoc.y), yMax, this.getBackground());
            this.startMovingVertex = false;
        }
        
    }

    
    
    
    
    
    
    
    /* 
     *  Animation methods
     */
    
    /**
     * Animate quad scaling and intersection discovery
     */
    private void doVoronoiAnimation(int delay, int maxScaleIterations, FindBisectorsTwoSites b2s, FindBisectorsThreeSites b3s) {
        // Consider having a method which checks whether all quad segments are off the screen and stop animation only if true
        timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                animationStep();
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
     * Applies one step for the animation
     */
    private void animationStep() {
        Utility.debugPrintln("Doing animation step");
        this.curScale += 0.1;
        this.quad.scaleQuad(this.curScale);

        // for each pair of vertices, check for quad intersection
        for (int i = 0; i < this.dtGraph.getVertices().size(); i++) {
            for (int j = i + 1; j < this.dtGraph.getVertices().size(); j++) {
                // Find and store intersections for current quad scaling
                findQuadIntersections(this.quad, this.dtGraph.getVertices().get(i), this.dtGraph.getVertices().get(j));
            }
        }
    }

    /**
     * Determine whether Quadrilateral q around two vertices has an intersection,
     * add to voronoiVertices
     *
     * @param q Reference quad
     * @param p1 First vertex
     * @param p2 Second vertex
     */
    public void findQuadIntersections(Quadrilateral q, Vertex p1, Vertex p2) {
        Vertex[] quad1 = q.getPixelVertsForVertex(p1, this.curScale);
        Vertex[] quad2 = q.getPixelVertsForVertex(p2, this.curScale);

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
                //Utility.debugPrintln("i = " + i + ", k = " + k + ", j = " + j + ", l = " + l);
                //Utility.debugPrintln("Comparing line segments: (" + quad1[i].x + ", " + quad1[i].y + ") ("+ quad1[k].x + ", " + quad1[k].y + ") and (" + quad2[j].x + ", " + quad2[j].y + ") ("+ quad2[l].x + ", " + quad2[l].y + ")");
                Vertex intersectionVertex;
                if ((intersectionVertex = Utility.doLineSegmentsIntersect(quad1[i], quad1[k], quad2[j], quad2[l])) != null) {
                    //Utility.debugPrintln("Found intersection at (" + intersectionVertex.x + ", " + intersectionVertex.y + ")");
                    this.voronoiVertices.add(intersectionVertex);
                }
            }
        }

    }
    
    
}
