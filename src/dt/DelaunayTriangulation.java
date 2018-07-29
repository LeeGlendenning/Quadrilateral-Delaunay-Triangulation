package dt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    //protected List<Vertex> vertices;
    private List<Vertex> voronoiVertices; // voronoiVertices used for animation
    //private List<Vertex[]> delaunayEdges; // List of Vertex tuples representing edges in the Delaunay triangulation
    protected Quadrilateral quad;
    // Consider using synchronized list to avoid concurrent modification...
    private List<Bisector> displayEdges;
    private Bisector[] chosenB3S;
    private double curScale;
    private int scaleIterations;
    private Timer timer;
    private FindBisectorsTwoSites b2s;
    private FindBisectorsThreeSites b3s;
    private final Painter painter;
    
    private boolean showB2S_hgRegion = false, showB2S_hgVertices = false, showB2S_hiddenCones = false, showB2S = false;
    private boolean showB3S_fgRegion = false, showB3S_hidden = false, showB3S = false;
    private final boolean doAnimation = false;
    private boolean showCoordinates = true;
    
    UI userInterface;
    int mouseX, mouseY;
    
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
        this.dtGraph = new Graph();
        this.scaleIterations = 0;
        this.curScale = 1.0;
        
        this.b2s = new FindBisectorsTwoSites();
        this.b3s = new FindBisectorsThreeSites(this.getBounds().getSize().height, this.getBounds().getSize().width);
        
        // Construct VD for initial vertex set
        addVertexSet(vertices);
        
        if (this.doAnimation) {
            doVoronoiAnimation(40, 1000, b2s, b3s);
        }
    }
    
    //int clickCount = 0; // Debugging
    
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
        this.dtGraph = new Graph();
        this.scaleIterations = 0;
        this.chosenB3S = new Bisector[]{};
        
        this.b2s = new FindBisectorsTwoSites();
        this.b3s = new FindBisectorsThreeSites(this.getBounds().getSize().height, this.getBounds().getSize().width);
        this.repaint();
    }
    
    
    
    
    
    /**
     * Delaunay triangulation methods
     */
    
    /**
     * Find B2S, B3S and min quad for vertex set now including a new vertex p
     * @param p A Vertex
     */
    public void addVertex(Vertex p) {
        /*clickCount++;
        // Add vertices automatically for debugging
        if (clickCount == 1) {
            p = new Vertex(150, 300);
        } else if (clickCount == 2) {
            p = new Vertex(400, 250);
        } else if (clickCount == 3) {
            p = new Vertex(200, 350);
        }*/
        if (this.dtGraph.getVertices().contains(p)) {
            Utility.debugPrintln("Vertex not added. Already exists.");
            return;
        }
        
        Utility.debugPrintln("Adding vertex " + p + "\n");
        
        // Find B2S between p and other vertices
        for (int i = 0; i < this.dtGraph.getVertices().size(); i++) {
            this.b2s.findBisectorOfTwoSites(this.quad, this.dtGraph.getVertices().get(i).deepCopy(), p);
        }
        Utility.debugPrintln("");
        this.displayEdges.addAll(this.b2s.getDisplayEdges());
        Bisector[] voronoiEdgesB2S = b2s.getVoronoiEdges();
        
        // Find B3S between p and all other pairs of vertices
        for (int i = 0; i < this.dtGraph.getVertices().size(); i ++) {
            for (int j = i + 1; j < this.dtGraph.getVertices().size(); j++) {
                Utility.debugPrintln("Finding B3S between: " + this.dtGraph.getVertices().get(i).deepCopy() + ", " + this.dtGraph.getVertices().get(j).deepCopy() + ", and p = " + p);
                this.b3s.findBisectorOfThreeSites(this.quad, voronoiEdgesB2S, this.dtGraph.getVertices().get(i).deepCopy(), this.dtGraph.getVertices().get(j).deepCopy(), p);
            }
        }
        Utility.debugPrintln("");
        this.dtGraph.addVertex(p);
        this.displayEdges.addAll(this.b3s.getDisplayEdges());
        
        this.chosenB3S = b3s.getChosenBisectors();
        
        removeAllEdges();
        
        for (Bisector chosenBisector : this.chosenB3S) {
            if (!vertexInsideQuad(calculateMinQuad(chosenBisector))) {
                triangulateVertices(chosenBisector.getAdjacentPtsArray());
            }
        }
        //Utility.debugPrintln("Vertex " + this.dtGraph.getVertices().get(this.dtGraph.getVertices().size()-1) + " neighbour size = " + this.dtGraph.getVertices().get(this.dtGraph.getVertices().size()-1).getNeighbours().size());
        // Retriangulate if necessary
        //checkAdjacentTriangles(p);
        
        double[][] dist = findAllPairsShortestPath();
        for (double[] d : dist) {
            Utility.debugPrintln(Arrays.toString(d));
        }
                
        Utility.debugPrintln("");
        repaint();
    }
    
    /**
     * Remove all edges of the triangulation
     */
    private void removeAllEdges() {
        for (Edge e : this.dtGraph.getEdges()) {
            this.dtGraph.removeEdge(e);
        }
    }
    
    /**
     * 
     * @return True if a vertex in the vertex set lies inside quad. False otherwise
     */
    private boolean vertexInsideQuad(Vertex[] quad) {
        
        /*Utility.debugPrintln("vertexInsideQuad: ");
        int ii;
        for (int i = 0; i < quad.length; i ++) {
            if (i == quad.length-1) {
                ii = 0;
            } else {
                ii = i+1;
            }
            Utility.debugPrintln(quad[i] + " " + quad[ii]);
        }*/
        
        for (Vertex p : this.dtGraph.getVertices()) {
            
            if (Utility.isLeftOfSegment(quad[0], quad[1], p, 0.1) == -1 &&
                    Utility.isLeftOfSegment(quad[1], quad[2], p, 0.1) == -1 &&
                    Utility.isLeftOfSegment(quad[2], quad[3], p, 0.1) == -1 &&
                    Utility.isLeftOfSegment(quad[3], quad[0], p, 0.1) == -1) {
                //Utility.debugPrintln("Vertex " + p + " inside");
                return true;
            }
            
        }
        return false;
    }
    
    /**
     * 
     * @param pts Array of 3 vertices to connect
     */
    private void triangulateVertices(Vertex[] verts) {
        int ii;
        for (int i = 0; i < verts.length; i ++) {
            if (i == verts.length-1) {
                ii = 0;
            } else {
                ii = i+1;
            }
            // If the edge doesn't already exist
            if (!this.dtGraph.getEdges().contains(new Edge(verts[i], verts[ii]))) {
                //Utility.debugPrintln("Adding edge between " + verts[i] + " and " + verts[ii]);
                this.dtGraph.addEdge(verts[i], verts[ii]);
            }
        }
    }
    
    /**
     * For a new vertex added to the DT, check triangles formed from a B3S having
     * the new vertex in the adjacent vertex list
     */
    /*private void checkAdjacentTriangles(Vertex v) {
        Utility.debugPrintln("Checking adjacent triangles...");
        
        for (Bisector b : this.chosenB3S) {
            // If all vertices contributing to the B3S are adjacent to v,
            // then the B3S may be a problem and needs to be retriangulated
            Utility.debugPrintln("Checking B3S at " + b.getStartVertex());
            for (Vertex adjV : b.getAdjacentPtsArray()) {
                if (!v.getNeighbours().contains(adjV)) {
                    //Utility.debugPrintln("v neighbours does not contain adjVert: " + adjV);
                    return;
                } else {
                    Utility.debugPrintln("v neighbours contains adjVert: " + adjV);
                }
            }
            
            Utility.debugPrintln("Retriangulating");
            if (vertexInsideQuad(calculateMinQuad(b))) {
                removeInvalidEdges(b);
            } else {
                triangulateVertices(b.getAdjacentPtsArray());
            }
        }
    }*/
    
    /**
     * 
     * @param b3s Bisector between 3 sites to retriangulate
     */
    /*private void removeInvalidEdges(Bisector b3s) {
        // Check if the edges associated to the b3s should be removed from DT
        
        Vertex[] adjVerts = b3s.getAdjacentPtsArray();

        int ii;
        // Remove edges associated to the B3S
        for (int i = 0; i < adjVerts.length; i ++) {
            if (i == adjVerts.length-1) {
                ii = 0;
            } else {
                ii = i+1;
            }

            // Remove edges if they exist - which they should
            if (this.dtGraph.getEdges().contains(new Edge(adjVerts[i], adjVerts[ii]))) {
                this.dtGraph.removeEdge(new Edge(adjVerts[i], adjVerts[ii]));
            }
        }
    }*/
    
    /**
     * @param chosenB3S Chosen Bisector between 3 vertices
     * @return Min quad around given chosenB3S
     */
    public Vertex[] calculateMinQuad(Bisector chosenB3S) {
        Double scale;
        if (chosenB3S.getTag().contains("chosen") && (scale = findMinimumQuadScaling(chosenB3S)) != null) {
            //Utility.debugPrintln("Scale = " + scale + "\n");
            chosenB3S.setMinQuadScale(scale);
            return this.quad.getPixelVertsForVertex(chosenB3S.getEndVertex(), scale);
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
        
        Vertex[][] quadRays = new Vertex[4][2]; // Rays from quad center through each vertex
        for (int i = 0; i < 4; i ++) {
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
        double[][] dist = new double[this.dtGraph.getVertices().size()][this.dtGraph.getVertices().size()];
        // Initialize 2D array with infinity
        for (double[] arr : dist) {
            Arrays.fill(arr, Double.MAX_VALUE);
        }
        
        // Set distance from vertices sharing an edge to the weight of the edge
        for (Edge e : this.dtGraph.getEdges()) {
            dist[this.dtGraph.getVertices().indexOf(e.getVertices()[0])][this.dtGraph.getVertices().indexOf(e.getVertices()[1])] = e.getWeight();
            dist[this.dtGraph.getVertices().indexOf(e.getVertices()[1])][this.dtGraph.getVertices().indexOf(e.getVertices()[0])] = e.getWeight();
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
                    }
                    
                }
            }
        }
        
        return dist;
    }
    
    
    
    
    
    
    /*
     *  User Interface methods
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
    
    public void newVertexSet(List<Vertex> vertices) {
        reset();
        addVertexSet(vertices);
    }
    
    /**
     * Remove vertex and reconstruct Voronoi Diagram
     * @param p Vertex to remove from vertex set
     */
    public void removeVertex(Vertex p) {
        if (this.dtGraph.getVertices().contains(p)) {
            this.dtGraph.removeVertex(p);
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
     * @return List<Vertex> list of vertices in the DT
     */
    public List<Vertex> getVertices() {
        return this.dtGraph.getVertices();
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

        int vertexRadius = 3, voronoiVertexRadius = 1;
        int yMax = this.getBounds().getSize().height;

        
        painter.drawVerticesAndQuads(g2d, this.dtGraph.getVertices(), this.quad, yMax, vertexRadius, this.curScale);

        g2d.setColor(Color.black);
        painter.drawBisectorRayVertices(g2d, this.voronoiVertices, yMax, voronoiVertexRadius);
        
        // Draw bisector segments between 2 sites
        painter.drawB2S(g2d, b2s.getVoronoiEdges(), yMax, this.showB2S, this.showB2S_hiddenCones);
        
        // Draw bisector segments between 3 sites
        g2d.setStroke(new BasicStroke(5));
        painter.drawB3S(g2d, b3s.getVoronoiEdges(), yMax, this.showB3S, this.showB3S_hidden);
        
        g2d.setColor(Color.blue);
        if (this.chosenB3S != null) {
            painter.drawChosenB3SAndMinQuads(g2d, this.quad, this.chosenB3S, yMax, this.showB3S);
        }
        
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(1));
        painter.drawDisplayEdges(g2d, this.displayEdges, yMax, this.showB2S_hgRegion, this.showB3S_fgRegion);
        
        painter.drawB2S_hgVertices(g2d, b2s.geth1(), b2s.geth2(), b2s.getg1(), b2s.getg2(), yMax, vertexRadius, this.showB2S_hgVertices);
        
        painter.drawVertexCoordinates(g2d, this.dtGraph.getVertices(), yMax, this.showCoordinates);
        
        painter.drawMouseCoordinates(g2d, mouseX, mouseY, yMax);
        
        painter.drawDelaunayEdges(g2d, this.dtGraph.getEdges(), yMax);
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
