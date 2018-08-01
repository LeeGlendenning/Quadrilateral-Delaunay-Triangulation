package dt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Painter class is used to draw all Graphics2D elements to the screen
 * 
 * @author Lee Glendenning
 */
public class Painter {
    
    public Painter() {
        
    }
    
    /**
     * Draw vertices in vertex set and the Quadrilateral around each vertex
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param vertices List of vertices to draw
     * @param quad Quadrilateral to draw around the vertices
     * @param vertexRadius Visual radius of vertices in vertex set
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param curScale Current scaling factor to draw Quadrilateral at
     */
    public void drawVerticesAndQuads(Graphics2D g2d, List<Vertex> vertices, Quadrilateral quad, int yMax, int vertexRadius, double curScale) {
        //Utility.debugPrintln("Vertices.size() = " + vertices.size());
        for (Vertex v : vertices) {
            g2d.setColor(v.getColour());
            //Utility.debugPrintln("Drawing vertex at " + (p.x - vertexRadius) + ", " + (yMax - (p.y + vertexRadius)));
            // Subtract vertexRadius because vertices are drawn at coordinates from top left
            g2d.fill(new Ellipse2D.Double(v.x - vertexRadius, yMax - (v.y + vertexRadius), vertexRadius * 2, vertexRadius * 2)); // x, y, width, height
            quad.drawQuad(g2d, v, 1.0, yMax); // Original quad
            quad.drawQuad(g2d, v, curScale, yMax); // Scaled quad for animation
        }
    }
    
    /**
     * Draw animation vertices
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param voronoiVertices Intersection vertices of quads as they blow up
     * @param voronoiVertexRadius Visual radius of bisector ray vertices
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     */
    public void drawBisectorRayVertices(Graphics2D g2d, List<Vertex> voronoiVertices, int yMax, int voronoiVertexRadius) {
        for (Vertex bisector : voronoiVertices.toArray(new Vertex[voronoiVertices.size()])) {
            g2d.fill(new Ellipse2D.Double(bisector.x + voronoiVertexRadius, yMax - (bisector.y + voronoiVertexRadius), voronoiVertexRadius * 2, voronoiVertexRadius * 2)); // x, y, width, height
        }
    }
    
    /**
     * Draw all bisectors between two sites
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param voronoiEdgesB2S Array of VoronoiBisector objects to draw to screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB2S If true, draw bisectors between 2 sites that are marked "chosen"
     * @param showB2S_hiddenCones If true, draw bisectors between 2 sites that are marked "hidden"
     */
    public void drawB2S(Graphics2D g2d, Bisector[] voronoiEdgesB2S, int yMax, boolean showB2S, boolean showB2S_hiddenCones) {
        for (Bisector bisector : voronoiEdgesB2S) {
            if (bisector.getTag().startsWith("b2s_chosen") && showB2S ||
                    bisector.getTag().startsWith("b2s_hidden") && showB2S_hiddenCones){
                g2d.drawLine((int)Math.round(bisector.getStartVertex().x), yMax - (int)Math.round(bisector.getStartVertex().y),
                        (int)Math.round(bisector.getEndVertex().x), yMax - (int)Math.round(bisector.getEndVertex().y));
            }
        }
    }
    
    /**
     * Draw bisectors between 3 sites
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param voronoiEdgesB3S Array of VoronoiBisector objects to draw to screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB3S If true, show bisectors between 3 sites
     * @param showB3S_hidden If true, show bisectors between 3 sites that are marked "hidden"
     */
    public void drawB3S(Graphics2D g2d, Bisector[] voronoiEdgesB3S, int yMax, boolean showB3S, boolean showB3S_hidden) {
        for (Bisector bisector : voronoiEdgesB3S) {
            if (bisector.getTag().startsWith("b3s") && showB3S && showB3S_hidden){
                g2d.setColor(Color.red);
                g2d.drawLine((int)Math.round(bisector.getStartVertex().x), yMax - (int)Math.round(bisector.getStartVertex().y),
                        (int)Math.round(bisector.getEndVertex().x), yMax - (int)Math.round(bisector.getEndVertex().y));
            }
            
        }
    }
    
    /**
     * Draw chosen bisectors between 3 vertices and their corresponding min quads in blue
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param quad Quadrilateral to draw through 3 vertices
     * @param chosenB3S Array of VoronoiBisector objects to draw to screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB3S If true, show bisectors between 3 sites that are marked "chosen"
     */
    public void drawChosenB3SAndMinQuads(Graphics2D g2d, Quadrilateral quad, ArrayList<Bisector> chosenB3S, int yMax, boolean showB3S) {
        for (Bisector bisector : chosenB3S) {
            if (showB3S) {
                g2d.setStroke(new BasicStroke(7));
                g2d.drawLine((int)Math.round(bisector.getStartVertex().x), yMax - (int)Math.round(bisector.getStartVertex().y),
                        (int)Math.round(bisector.getEndVertex().x), yMax - (int)Math.round(bisector.getEndVertex().y));
                g2d.setStroke(new BasicStroke(2));
                //vd.quad.drawQuad(g2d, bisector.startVertex, 1.0, yMax); // Original quad
                //Utility.debugPrintln(bisector.getMinQuadScale());
                quad.drawQuad(g2d, bisector.getStartVertex(), bisector.getMinQuadScale(), yMax);
                //vd.quad.drawQuad(g2d, bisector.startVertex, vd.curScale, yMax);
            }
        }
    }
    
    /**
     * Draw lines for debugging that show the process of the triangulation
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param displayEdges List of line segments to draw
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB2S_hgRegion If true, show lines corresponding to the hg region of b2s
     * @param showB3S_fgRegion If true, show lines corresponding to the fg region of b3s
     */
    public void drawDisplayEdges(Graphics2D g2d, List<Bisector> displayEdges, int yMax, boolean showB2S_hgRegion, boolean showB3S_fgRegion) {
        for (Bisector bisector : displayEdges.toArray(new Bisector[displayEdges.size()])) {
            // TODO: sometimes the bisector start or end vertex are null and I don't know why
            if (bisector.getStartVertex() != null && bisector.getEndVertex() != null &&
                    (bisector.getTag().equals("b2s_step") && showB2S_hgRegion ||
                    bisector.getTag().equals("b3s_step") && showB3S_fgRegion ||
                    bisector.getTag().equals("debug"))) {
                g2d.drawLine((int)Math.round(bisector.getStartVertex().x), yMax - (int)Math.round(bisector.getStartVertex().y),
                        (int)Math.round(bisector.getEndVertex().x), yMax - (int)Math.round(bisector.getEndVertex().y));
            }
        }
    }
    
    /**
     * Draw HG vertices used to find bisectors between 2 sites
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param h1
     * @param h2
     * @param g1
     * @param g2
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param vertexRadius Visual radius of HG vertices
     * @param showB2S_hgVertices If true, show hg vertices used to find bisectors between 2 sites
     */
    public void drawB2S_hgVertices(Graphics2D g2d, Vertex[] h1, Vertex[] h2, Vertex[] g1, Vertex[] g2, int yMax, int vertexRadius, boolean showB2S_hgVertices) {
        // Draw h12, g12 vertices on quads
        if (showB2S_hgVertices) {
            g2d.setColor(Color.red);
            for(int i = 0; i < h1.length; i ++) {
                g2d.fill(new Ellipse2D.Double(h1[i].x - vertexRadius, yMax - h1[i].y - vertexRadius, vertexRadius * 2, vertexRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(h2[i].x - vertexRadius, yMax - h2[i].y - vertexRadius, vertexRadius * 2, vertexRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(g1[i].x - vertexRadius, yMax - g1[i].y - vertexRadius, vertexRadius * 2, vertexRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(g2[i].x - vertexRadius, yMax - g2[i].y - vertexRadius, vertexRadius * 2, vertexRadius * 2)); // x, y, width, height
            }
        }
    }
    
    /**
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param mouseX X coordinate of mouse location
     * @param mouseY Y coordinate of mouse location
     */
    public void drawMouseCoordinates(Graphics2D g2d, int mouseX, int mouseY, int yMax) {
        String s = mouseX + ", " + (yMax- mouseY);
        g2d.setColor(Color.red);
        g2d.drawString(s, Math.round(mouseX), Math.round(mouseY));
    }
    
    /**
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param vertexSet Set of vertices to draw coordinates for
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showCoordinates If true, draw vertex coordinates. Otherwise do nothing
     */
    public void drawVertexCoordinates(Graphics2D g2d, List<Vertex> vertexSet, int yMax, boolean showCoordinates) {
        
        if (showCoordinates) {
            int fontSize = 14;
            for (Vertex p : vertexSet) {
                g2d.setColor(Color.black);
                g2d.setFont(new Font("default", Font.BOLD, fontSize));
                g2d.drawString(vertexSet.indexOf(p) + ": ", Math.round(p.x)+2, Math.round(yMax - p.y));
                g2d.setColor(Color.red);
                g2d.setFont(new Font("default", Font.PLAIN, fontSize));
                g2d.drawString(Math.round(p.x) + ", " + Math.round(p.y), Math.round(p.x)+25, Math.round(yMax - p.y));
            }
        }
    }

    /**
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param delaunayEdges List of vertex tuples representing edges of the Delaunay triangulation
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     */
    public void drawDelaunayEdges(Graphics2D g2d, List<Edge> delaunayEdges, int yMax) {
        g2d.setColor(Color.black);
        for (Edge edge : delaunayEdges) {
            g2d.drawLine((int)Math.round(edge.getVertices()[0].x), yMax - (int)Math.round(edge.getVertices()[0].y), 
                    (int)Math.round(edge.getVertices()[1].x), yMax - (int)Math.round(edge.getVertices()[1].y));
        }
    }
    
    /**
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param sfVertices Array of size 2 holding the vertex indices causing the stretch factor of the DT
     * @param stretchFactor Double stretch factor to draw as a String to top left corner of screen
     */
    public void drawStretchFactor(Graphics2D g2d, Integer[] sfVertices, double stretchFactor) {
        g2d.setColor(Color.white);
        g2d.fill(new Rectangle2D.Double(0, 0, 250, 32));
        g2d.setColor(Color.black);
        g2d.drawString("Stretch factor = " + stretchFactor, 0, 14);
        g2d.drawString("Between V" + sfVertices[0] + " and V" + sfVertices[1], 0, 28);
        
    }
    
    /**
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param path List of vertices representing a path in the DT
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param highlightPath Boolean whether to highlight the path or not
     * @param c Colour to draw/highlight the path
     */
    public void highlightShortestPath(Graphics2D g2d, ArrayList<Vertex> path, int yMax, boolean highlightPath, Color c) {
        if (highlightPath) {
            g2d.setColor(c);
            if (c.equals(Color.red)) {
                g2d.setStroke(new BasicStroke(2));
            }
            for (int i = 0; i < path.size()-1; i ++) {
                g2d.drawLine((int)Math.round(path.get(i).x), yMax - (int)Math.round(path.get(i).y), 
                        (int)Math.round(path.get(i+1).x), yMax - (int)Math.round(path.get(i+1).y));
            }
            
            g2d.setStroke(new BasicStroke(1));
        }
    }
    
}
