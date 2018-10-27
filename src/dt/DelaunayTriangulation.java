package dt;

import java.awt.Color;
import java.awt.Dimension;
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
 * Constructs a Delaunay Triangulation from a vertex set using a Quadrilateral
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
    private final Dimension screenSize;
    
    private boolean showB2S_hgRegion = false, showB2S_hgVertices = false, showB2S_hiddenCones = true, showB2S = false;
    private boolean showB3S_fgRegion = false, showB3S_hidden = false, showB3S = false;
    private final boolean doAnimation = false;
    private boolean showCoordinates = true, highlightShortestPath = true, /*clearSelectedPath = false,*/ showBoundaryTriangle = true;
    
    private Vertex movingVertex = null, movingVertexOldLoc = null, movingVertexOriginalLoc = null;
    private int movingVertIndex = -1;
    private int mouseX, mouseY;

    /**
     * Construct Voronoi diagram for vertex set using a Quadrilateral
     *
     * @param q Quadrilateral
     * @param vertices Vertex set
     * @param screenSize Dimension of JFrame window size
     */
    public DelaunayTriangulation(Quadrilateral q, ArrayList<Vertex> vertices, Dimension screenSize) {
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
        this.screenSize = screenSize;
        this.dtGraph = new Graph(this.screenSize.width, this.screenSize.height);
        
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
        this.dtGraph = new Graph(this.screenSize.width, this.screenSize.height);
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
        
        Vertex[] triangle = this.dtGraph.locateTriangle(v);
        if (triangle == null) {
            Utility.debugPrintln("Skipping adding vertex because it is on a line.");
            return;
        }
        Utility.debugPrintln("V contained by vertices: " + Arrays.toString(triangle));
        
        // Connect new vertex with all vertices in the located triangle
        this.dtGraph.addVertex(v);
        for (Vertex tVert : triangle) {
            this.dtGraph.addEdge(v, tVert);
        }
        
        // Faces to calculate B3S for and check minQuad
        Vertex[][] faces = findQuestionableFaces(v, triangle);
        Utility.debugPrintln("Questionable faces:");
        for (Vertex[] face : faces) {
            Utility.debugPrintln("  " + Arrays.toString(face));
        }
        
        HashMap<List<Vertex>, List<Bisector>> bisectors2S = calculateB2S(faces);
        this.chosenB2S = bisectors2S;
        List<Bisector> bisectors3S;
        bisectors3S = calculateB3S(faces, bisectors2S);
        
        this.chosenB3S = new ArrayList();
        for (Bisector b : bisectors3S) {
            if (b.getEndVertex() != null) {
                this.chosenB3S.add(b.deepCopy());
                // Set scaling for minQuad
                calculateMinQuad(this.chosenB3S.get(this.chosenB3S.size()-1));
            }
        }
        
        checkForBadEdges(v, bisectors3S); // If necessary, flip bad edges
        
        // Calculate all pairs shortest paths and stretch factor of the DT
        updateShortestPaths();
        
        Utility.debugPrintln("");
        repaint();
    }
    
    /**
     * 
     * @param v Newly triangulated vertex
     * @param vContainerFace Vertices containing v
     * @return Array of faces that should be checked with empty quad
     */
    private Vertex[][] findQuestionableFaces(Vertex v, Vertex[] vContainerFace) {
        Vertex[][] faces = new Vertex[6][3];
        
        // 3 Faces adjacent to v
        faces[0] = new Vertex[]{v, vContainerFace[0], vContainerFace[1]};
        faces[1] = new Vertex[]{v, vContainerFace[1], vContainerFace[2]};
        faces[2] = new Vertex[]{v, vContainerFace[2], vContainerFace[0]};
        
        // 3 Faces commonly adjacent to vContainerFace
        List<Vertex> commonVerts = intersectVertexSets(vContainerFace[0].getNeighbours(), vContainerFace[1].getNeighbours());
        Vertex closest = closestVertex(vContainerFace[0], vContainerFace[1], commonVerts, new Vertex[]{v, vContainerFace[2]});
        if (closest == null) {
            faces[3] = null;
        } else {
            faces[3] = new Vertex[]{vContainerFace[0], vContainerFace[1], closest};
        }
        
        commonVerts = intersectVertexSets(vContainerFace[1].getNeighbours(), vContainerFace[2].getNeighbours());
        closest = closestVertex(vContainerFace[1], vContainerFace[2], commonVerts, new Vertex[]{v, vContainerFace[0]});
        if (closest == null) {
            faces[4] = null;
        } else {
            faces[4] = new Vertex[]{vContainerFace[1], vContainerFace[2], closest};
        }
        
        commonVerts = intersectVertexSets(vContainerFace[2].getNeighbours(), vContainerFace[0].getNeighbours());
        closest = closestVertex(vContainerFace[2], vContainerFace[0], commonVerts, new Vertex[]{v, vContainerFace[1]});
        if (closest == null) {
            faces[5] = null;
        } else {
            faces[5] = new Vertex[]{vContainerFace[2], vContainerFace[0], closest};
        }
        
        return faces;
    }
    
    /**
     * 
     * @param v1 A vertex of edge v1v2
     * @param v2 A vertex of edge v1v2
     * @param comparators List of vertices to compare distance to v1v2
     * @param ignore Vertex to ignore
     * @return Vertex having min distance to v1 and v2
     */
    private Vertex closestVertex(Vertex v1, Vertex v2, List<Vertex> comparators, Vertex[] ignore) {
        Vertex closest = null;
        for (Vertex c : comparators) {
            if (!c.equals(ignore[0]) && !c.equals(ignore[1]) &&
                    (closest == null || (Utility.euclideanDistance(v1, c) + Utility.euclideanDistance(v1, c)) <
                    (Utility.euclideanDistance(v1, closest) + Utility.euclideanDistance(v2, closest)))) {
                // Line through v1,v2 used to find the closest vertex on the side of the line opposite to v, the newly added vertex (ignore[0])
                Vertex[] lineCheck = constructLine(v1, v2);
                if (Utility.doLineSegmentsIntersect(lineCheck[0], lineCheck[1], ignore[0], c) != null) {
                    closest = c;
                }
            }
        }
        return closest;
    }
    
    /**
     * 
     * @param v1 A vertex of a line segment
     * @param v2 A vertex of a line segment
     * @return Line through the line segment v1,v2
     */
    private Vertex[] constructLine(Vertex v1, Vertex v2) {
        double rayEndx = Utility.RAY_SIZE;
        if (v1.x > v2.x || (v1.x == v2.x && v1.y > v2.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        Vertex rayEnd1 = new Vertex(rayEndx, Utility.midpoint(v1, v2).y); // End vertex of ray which is basically + or - infinity
        Vertex rayEnd2 = new Vertex(-rayEndx, Utility.midpoint(v1, v2).y);
        
        double angle = Utility.calculateAngle(v1, v2); // Angle that slope(v1, v2) makes with x axis
        
        // Define line by rotating rayEnd such that it has slope(v1, v2)
        Vertex[] line = new Vertex[]{this.b3s.findBoundaryVertexOnRay(Utility.rotateVertex(rayEnd1, new Vertex(0,0), -angle), Utility.midpoint(v1, v2)), 
                this.b3s.findBoundaryVertexOnRay(Utility.rotateVertex(rayEnd2, new Vertex(0,0), -angle), Utility.midpoint(v1, v2))};
        //Utility.debugPrintln("constructLine through " + v1 + ", " + v2 + ": " + Arrays.toString(line) + ", midpoint = " + Utility.midpoint(v1, v2));
        return line;
    }
    
    /**
     * 
     * @param faces Array of faces to calculate B2S for all pairs of vertices
     * @return HashMap of B2S
     */
    private HashMap<List<Vertex>, List<Bisector>> calculateB2S(Vertex[][] faces) {
        HashMap<List<Vertex>, List<Bisector>> tempB2S = new HashMap();
        // Find B2S between each pair of vertices in each face
        for (Vertex[] face : faces) {
            if (face == null) {
                continue;
            }
            int ii;
            for (int i = 0; i < face.length; i++) {
                if (i == face.length-1) {
                    ii = 0;
                } else {
                    ii = i+1;
                }
                // Calculate b2s between each of the vertices in the list
                tempB2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, face[i].deepCopy(), face[ii].deepCopy()));
                // Calculate b2s between v and each of the vertices in the list
                //tempB2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, vertices.get(i).deepCopy(), v));
            }
        }
        
        Utility.debugPrintln("");
        //this.displayEdges.addAll(this.b2s.getDisplayEdges());
        
        return tempB2S;
    }
    
    /**
     * 
     * @param faces Array of faces to calculate B3S for
     * @param bisectors2S HashMap of B2S
     * @return List of Bisector representing B3S for each given face
     */
    private List<Bisector> calculateB3S(Vertex[][] faces, HashMap<List<Vertex>, List<Bisector>> bisectors2S) {
        List<Bisector> tempB3S = Collections.synchronizedList(new ArrayList());
        
        // Find B3S for each face
        for (Vertex[] face : faces) {
            if (face == null) {
                continue;
            }
            
            Bisector b = this.b3s.findBisectorOfThreeSites(this.quad, bisectors2S, face[0].deepCopy(), face[1].deepCopy(), face[2].deepCopy());
            
            if (b != null /*&& edgesExist(face)*/) {
                tempB3S.add(b);
            } else {
                tempB3S.add(new Bisector(new Vertex[]{face[0].deepCopy(), face[1].deepCopy(), face[2].deepCopy()}, null, null, ""));
            }
        }
        
        if (showB3S_fgRegion) {
            this.displayEdges.addAll(cleanFGEdges(this.b3s.getDisplayEdges()));
        }
        return tempB3S;
    }
    
    /**
     * This is called when considering a B3S. If edges don't exist, don't check b3s
     * @param face Vertex[] representing a triangle face in the graph
     * @return true if all edges in the face exist in the graph, false otherwise
     */
    private boolean edgesExist(Vertex[] face) {
        List<Edge> edges = this.dtGraph.getEdges();
        if (edges.contains(new Edge(face[0], face[1])) &&
                edges.contains(new Edge(face[1], face[2])) &&
                edges.contains(new Edge(face[2], face[0]))) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param v Newly triangulated vertex
     * @param b3sList List of B3S to check
     */
    private void checkForBadEdges(Vertex v, List<Bisector> b3sList) {
        //Utility.debugPrintln("Checking " + b3sList.size() + " b3s for vertex inside");
        List<Bisector> originalB3SList = new ArrayList(Arrays.asList(Utility.deepCopyVBArray(b3sList.toArray(new Bisector[b3sList.size()]))));
        List<Bisector> nullB3S = new ArrayList();
        
        List<Bisector> possibleBadEdges = new ArrayList(); // Queue of b3s to check for edges that need to be removed after b3sList is empty
        
        while (!b3sList.isEmpty()) {
            Bisector b = b3sList.get(0).deepCopy();
            Utility.debugPrintln("Checking validity of b3s: " + b.getAdjacentPtsList().toString() + ": " + b.getEndVertex());
            b3sList.remove(0);
            Vertex vInQuad;
            if (b.getEndVertex() != null && 
                    (vInQuad = vertexInsideQuad(calculateMinQuad(b), b, b.getAdjacentPtsList(), this.dtGraph.getVertices())) != null &&
                    !b.getAdjacentPtsList().contains(vInQuad)) {
                Vertex v1 = null, v2 = null;
                // Get the two vertices in the bad edge
                for (Vertex adjV : b.getAdjacentPtsArray()) {
                    //System.out.println("b3s vertex " + adjV + " neighbours: " + this.dtGraph.getVertex(adjV.x, adjV.y).getNeighbours());
                    if (this.dtGraph.getVertex(adjV.x, adjV.y).getNeighbours().contains(v)) {
                        if (v1 == null) {
                            v1 = this.dtGraph.getVertex(adjV.x, adjV.y);
                        } else {
                            v2 = this.dtGraph.getVertex(adjV.x, adjV.y);
                        }
                    }
                }
                
                if ((v1 == null || v2 == null /*TODO: verify this is correct. I don't think they should ever be null??*/) || 
                        this.dtGraph.getBoundaryTriangle().contains(v1) && 
                        this.dtGraph.getBoundaryTriangle().contains(v2)) {
                    Utility.debugPrintln("vInQuad was either part of min quad or boundary triangle. Skipped.");
                    // The "bad edge" is a boundary edge so don't flip it
                    continue;
                }
                
                //System.out.println("Bad edge vertices: " + v1 + ", " + v2);
                
                // Find the vertex opposite v in the convex quad using original b3sList
                Vertex newEdgeVert = findOppoVertInConvexQuad(v1, v2, v, originalB3SList);
                
                // if it's still null, search the nullB3SList
                if (newEdgeVert == null) {
                    newEdgeVert = findOppoVertInConvexQuad(v1, v2, v, nullB3S);
                }
                
                // if it's still null then we have a problem
                if (newEdgeVert == null) {
                    Utility.debugPrintln("[checkForBadEdges] new Edge vertex is null - This shouldn't happen!!");
                }
                
                //Utility.debugPrintln("Trying to flip edge " + new Edge(v1, v2));
                //Utility.debugPrintln("New edge would be " + new Edge(v, newEdgeVert));
                
                // Flip edge and check affacted faces
                if (flipEdge(new Edge(v1, v2), v, newEdgeVert)) {
                    
                    // Check possibleBadEdges and remove b3s related to edge flip
                    for (Bisector badB : possibleBadEdges.toArray(new Bisector[possibleBadEdges.size()])) {
                        if (badB.getAdjacentPtsList().contains(v1) && badB.getAdjacentPtsList().contains(v2)) {
                            possibleBadEdges.remove(badB);
                        }
                    }
                    
                    // Check queue and remove B3S if it corresponds to a face altered by edge flip
                    // Old faces were (v, v1, v2) and (v1, v2, newEdgeVert)
                    for (Bisector oldB3S : b3sList.toArray(new Bisector[b3sList.size()])) {
                        if ((oldB3S.getAdjacentPtsList().contains(v) &&
                                oldB3S.getAdjacentPtsList().contains(v1) &&
                                oldB3S.getAdjacentPtsList().contains(v2)) 
                                ||
                                (oldB3S.getAdjacentPtsList().contains(v1) &&
                                oldB3S.getAdjacentPtsList().contains(v2) &&
                                oldB3S.getAdjacentPtsList().contains(newEdgeVert))) {
                            Utility.debugPrintln("Removing B3S from queue: " + oldB3S.getAdjacentPtsList().toString());
                            b3sList.remove(oldB3S);
                            originalB3SList.remove(oldB3S);
                        }
                    }
                    // Check nullB3S queue and remove B3S if it corresponds to a face altered by edge flip
                    for (Bisector nB3S : nullB3S.toArray(new Bisector[nullB3S.size()])) {
                        if ((nB3S.getAdjacentPtsList().contains(v) &&
                                nB3S.getAdjacentPtsList().contains(v1) &&
                                nB3S.getAdjacentPtsList().contains(v2)) 
                                ||
                                (nB3S.getAdjacentPtsList().contains(v1) &&
                                nB3S.getAdjacentPtsList().contains(v2) &&
                                nB3S.getAdjacentPtsList().contains(newEdgeVert))) {
                            Utility.debugPrintln("Removing B3S from nullB3S queue: " + nB3S.getAdjacentPtsList().toString());
                            nullB3S.remove(nB3S);
                        }
                    }
                    
                    // Calc the 4 corresponding B3S and add to b3sList
                    Bisector tempB;
                    
                    // For B3S between v, v1, closest
                    if ((tempB = checkFaceAfterFlip(v, v1, Utility.midpoint(v1, v2))) != null) {
                        Utility.debugPrintln("1Adding B3S: " + tempB.getAdjacentPtsList().toString());
                        b3sList.add(tempB);
                        originalB3SList.add(tempB);
                    }
                    
                    // For B3S between v, v2, closest
                    if ((tempB = checkFaceAfterFlip(v, v2, Utility.midpoint(v1, v2))) != null) {
                        Utility.debugPrintln("2Adding B3S: " + tempB.getAdjacentPtsList().toString());
                        b3sList.add(tempB);
                        originalB3SList.add(tempB);
                    }
                    
                    // For B3S between v1, newEdgeVert, closest
                    if ((tempB = checkFaceAfterFlip(v1, newEdgeVert, Utility.midpoint(v1, v2))) != null) {
                        Utility.debugPrintln("3Adding B3S: " + tempB.getAdjacentPtsList().toString());
                        b3sList.add(tempB);
                        originalB3SList.add(tempB);
                    }
                    
                    // For B3S between v2, newEdgeVert, closest
                    if ((tempB = checkFaceAfterFlip(v2, newEdgeVert, Utility.midpoint(v1, v2))) != null) {
                        Utility.debugPrintln("4Adding B3S: " + tempB.getAdjacentPtsList().toString());
                        b3sList.add(tempB);
                        originalB3SList.add(tempB);
                    }
                    
                    // Test two triangles in the flip quadrilateral for boundary edge to be removed
                    
                    // Test (v1, v, newEdgeVert)
                    Utility.debugPrintln("Testing flip quad triang: " + v + ", " + v1 + ", " + newEdgeVert);
                    if (checkInnerQuadFaceAfterFlip(v, v1, newEdgeVert) == null) {
                        possibleBadEdges.add(new Bisector(new Vertex[]{v, v1, newEdgeVert}, null, null, ""));
                        //removeEdgeIfNecessary(new Bisector(new Vertex[]{v, v1, newEdgeVert}, null, null, ""));
                    }
                    
                    // Test (v2, v, newEdgeVert)
                    Utility.debugPrintln("Testing flip quad triang: " + v + ", " + v2 + ", " + newEdgeVert);
                    if (checkInnerQuadFaceAfterFlip(v, v2, newEdgeVert) == null) {
                        possibleBadEdges.add(new Bisector(new Vertex[]{v, v2, newEdgeVert}, null, null, ""));
                        //removeEdgeIfNecessary(new Bisector(new Vertex[]{v, v2, newEdgeVert}, null, null, ""));
                    }
                }
            } else if (b.getEndVertex() == null) {
                //Utility.debugPrintln("Adding B3S to nullB3S list");
                nullB3S.add(b);
            }
        }
        
        while (!nullB3S.isEmpty()) {
            removeEdgeIfNecessary(nullB3S.get(0));
            nullB3S.remove(0);
        }
        for (Bisector b : possibleBadEdges) {
            removeEdgeIfNecessary(b);
        }
        
        Utility.debugPrintln("\n");
    }
    
    private Vertex findOppoVertInConvexQuad(Vertex v1, Vertex v2, Vertex v, List<Bisector> b3sList) {
        Vertex newEdgeVert = null;
        for (Bisector bisector : b3sList) {
            //System.out.println("looking for v1 and v2 in " + bisector.getAdjacentPtsList().toString());
            if (bisector.getAdjacentPtsList().contains(v1) && bisector.getAdjacentPtsList().contains(v2) &&
                    !bisector.getAdjacentPtsList().contains(v)) {
                for (Vertex vertex : bisector.getAdjacentPtsArray()) {
                    if (!vertex.equals(v1) && !vertex.equals(v2)) {
                        newEdgeVert = vertex;
                    }
                }
            }
        }
        return newEdgeVert;
    }
    
    /**
     * 
     * @param b B3S to check whether an edge should be removed
     */
    private void removeEdgeIfNecessary(Bisector b) {
        int ii, iii; // i, ii, iii are indices of each of the face vertices
        for (int i = 0; i < b.getAdjacentPtsArray().length; i ++) {
            if (i == b.getAdjacentPtsArray().length-1) {
                ii = 0;
            } else {
                ii = i+1;
            }
            if (ii == b.getAdjacentPtsArray().length-1) {
                iii = 0;
            } else {
                iii = ii+1;
            }
            List<Vertex> commonVerts = intersectVertexSets(this.dtGraph.getVertex(b.getAdjacentPtsArray()[i].x, 
                    b.getAdjacentPtsArray()[i].y).getNeighbours(), this.dtGraph.getVertex(b.getAdjacentPtsArray()[ii].x, 
                    b.getAdjacentPtsArray()[ii].y).getNeighbours());
            //Utility.debugPrintln(" Checking edge: " + b.getAdjacentPtsArray()[i] + ", " + b.getAdjacentPtsArray()[ii]);
            //Utility.debugPrintln(" Closest vert: " + closestVertex(b.getAdjacentPtsArray()[i], b.getAdjacentPtsArray()[ii], commonVerts, 
              //      new Vertex[]{b.getAdjacentPtsArray()[iii], b.getAdjacentPtsArray()[iii]}));
            // If edge exists in graph and it is an exterior then remove it
            if (this.dtGraph.getEdges().contains(new Edge(b.getAdjacentPtsArray()[i], b.getAdjacentPtsArray()[ii])) &&
                    closestVertex(b.getAdjacentPtsArray()[i], b.getAdjacentPtsArray()[ii], commonVerts, 
                    new Vertex[]{b.getAdjacentPtsArray()[iii], b.getAdjacentPtsArray()[iii]}) == null) {
                Utility.debugPrintln("Found and removed bad exterior edge: " + b.getAdjacentPtsArray()[i] + ", " + b.getAdjacentPtsArray()[ii]);
                this.dtGraph.removeEdge(new Edge(b.getAdjacentPtsArray()[i], b.getAdjacentPtsArray()[ii]), false);
            }
        }
    }
    
    /**
     * 
     * @param v1 A vertex in the face to be checked
     * @param v2 A vertex in the face to be checked
     * @param v3 A vertex in the face to be checked
     * @return a B3S if it exists
     */
    private Bisector checkInnerQuadFaceAfterFlip(Vertex v1, Vertex v2, Vertex v3) {
        HashMap<List<Vertex>, List<Bisector>> bisectors2S = new HashMap();
        bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v1.deepCopy(), v2.deepCopy()));
        bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v2.deepCopy(), v3.deepCopy()));
        bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v3.deepCopy(), v1.deepCopy()));
        Utility.debugPrintln("Checking B3S between " + v1 + ", " + v2 + ", " + v3);
        return this.b3s.findBisectorOfThreeSites(this.quad, bisectors2S, v1.deepCopy(), v2.deepCopy(), v3.deepCopy());
    }
    
    /**
     * 
     * @param v1 A vertex in the face to be checked
     * @param v2 A vertex in the face to be checked
     * @param ptInQuad A point inside the quad that the flip happened
     * @return A B3S if it exists, null otherwise
     */
    private Bisector checkFaceAfterFlip(Vertex v1, Vertex v2, Vertex ptInQuad) {
        List<Vertex> commonVerts = intersectVertexSets(this.dtGraph.getVertex(v1.x, v1.y).getNeighbours(), 
                this.dtGraph.getVertex(v2.x, v2.y).getNeighbours());
        Vertex v3 = closestVertex(v1, v2, commonVerts, new Vertex[]{ptInQuad, ptInQuad});
        if (v3 == null) {
            return null;
        }
        
        HashMap<List<Vertex>, List<Bisector>> bisectors2S = new HashMap();
        bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v1.deepCopy(), v2.deepCopy()));
        bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v2.deepCopy(), v3.deepCopy()));
        bisectors2S.putAll(this.b2s.findBisectorOfTwoSites(this.quad, v3.deepCopy(), v1.deepCopy()));
        Utility.debugPrintln("Checking B3S between " + v1 + ", " + v2 + ", " + v3);
        return this.b3s.findBisectorOfThreeSites(this.quad, bisectors2S, v1.deepCopy(), v2.deepCopy(), v3.deepCopy());
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
     * @param oldE Edge to flip
     * @param newV1 Newly added vertex
     * @param newV2 Other vertex for the new edge
     */
    private boolean flipEdge(Edge oldE, Vertex newV1, Vertex newV2) {
        // Check if flipping edge leads to edge overlap
        for (Edge compareEdge : this.dtGraph.getEdges()) {
            Vertex intersectionPt = Utility.doLineSegmentsIntersect(compareEdge.getVertices()[0], 
                    compareEdge.getVertices()[1], newV1, newV2);
            if (!compareEdge.equals(oldE) && intersectionPt != null && 
                    !intersectionPt.equals(newV1) && !intersectionPt.equals(newV2)) {
                Utility.debugPrintln("Edge flip would lead to edge overlap. skipping...");
                return false;
            }
        }
        
        Utility.debugPrintln("Flipping edge " + oldE);
        this.dtGraph.removeEdge(oldE, true);
        Utility.debugPrintln("Adding new flipped edge " + new Edge(newV1, newV2));
        this.dtGraph.addEdge(this.dtGraph.getVertex(newV1.x, newV1.y), this.dtGraph.getVertex(newV2.x, newV2.y));
        return true;
    }
    
    /**
     * Update shortest paths matrix and stretch factor of the DT
     */
    private void updateShortestPaths() {
        this.oldSelectedPath = this.curSelectedPath;
        this.curSelectedPath = new ArrayList();
        this.shortestPaths = findAllPairsShortestPath();
        
        updateStretchFactor();
    }
    
    /**
     * @param quad Array of vertices defining the quad
     * @param vIgnore List of vertices on the quad boundary
     * @param pts List of vertices to check
     * @return True if a vertex in the vertex set lies inside quad. False otherwise
     */
    private Vertex vertexInsideQuad(Vertex[] quad, Bisector b3s, List<Vertex> vIgnore, List<Vertex> pts) {
        //Utility.debugPrintln("Checking if vertex inside: " + Arrays.toString(b3s.getAdjacentPtsArray()));
        for (Vertex v : pts) {
            //Utility.debugPrintln("Checking if " + v + " inside quad");
            if (!vIgnore.contains(v) &&
                    Utility.isLeftOfSegment(quad[0], quad[1], v, 0.1) <= 0 &&
                    Utility.isLeftOfSegment(quad[1], quad[2], v, 0.1) <= 0 &&
                    Utility.isLeftOfSegment(quad[2], quad[3], v, 0.1) <= 0 &&
                    Utility.isLeftOfSegment(quad[3], quad[0], v, 0.1) <= 0) {
                System.out.println("Vertex " + v + " inside " + Arrays.toString(b3s.getAdjacentPtsArray()));
                return v;
            }
        }
        //Utility.debugPrintln("No vertex inside quad");
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
            return this.quad.getPixelVertsForVertex(chosenB3S.getEndVertex(), scale, true);
        } else {
            Utility.debugPrintln("[calculateMinQuad] DID NOT SET SCALE! - this is a problem");
        }
        return null;
    }
    
    /**
     * @param chosenB3S Chosen VoronoiBisector between 3 sites
     * @return Amount the quad needs to be scaled such that it goes through the adjacent B3S vertices
     */
    private Double findMinimumQuadScaling(Bisector chosenB3S) {
        Vertex[] qVerts = this.quad.getPixelVertsForVertex(chosenB3S.getEndVertex(), this.curScale, true);
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

        /*this.movingVertEdges = */this.dtGraph.removeVertex(this.dtGraph.getVertex(v.x, v.y));
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
            painter.drawChosenB3SAndMinQuads(g2d, this.quad, this.chosenB3S, yMax, true);
        }
        
        if (this.showB3S_fgRegion) {
            painter.drawFGRegion(g2d, this.displayEdges, yMax);
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
        
        /*if (this.startMovingVertex) {
            Utility.debugPrintln("moving vert edge #: " + this.dtGraph.getVertex(movingVertexOriginalLoc.x, movingVertexOriginalLoc.y).getNeighborCount());
            painter.eraseEdgesAndCoords(g2d, this.dtGraph.getVertex(this.movingVertexOriginalLoc.x, this.movingVertexOriginalLoc.y), yMax, this.getBackground());
            this.startMovingVertex = false;
        }*/
        
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
        Vertex[] quad1 = q.getPixelVertsForVertex(p1, this.curScale, false);
        Vertex[] quad2 = q.getPixelVertsForVertex(p2, this.curScale, false);

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
