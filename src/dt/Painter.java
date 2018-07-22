package dt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Painter class is used to draw all Graphics2D elements to the screen
 * 
 * @author Lee Glendenning
 */
public class Painter {
    
    public Painter() {
        
    }
    
    /**
     * Draw points in point set and the Quadrilateral around each point
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param points List of points to draw
     * @param quad Quadrilateral to draw around the points
     * @param pointRadius Visual radius of points in point set
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param curScale Current scaling factor to draw Quadrilateral at
     */
    public void drawPointsAndQuads(Graphics2D g2d, List<Point> points, Quadrilateral quad, int yMax, int pointRadius, double curScale) {
        for (Point p : points) {
            g2d.setColor(p.getColour());
            // Subtract pointRadius because points are drawn at coordinates from top left
            g2d.fill(new Ellipse2D.Double(p.x - pointRadius, yMax - (p.y + pointRadius), pointRadius * 2, pointRadius * 2)); // x, y, width, height
            quad.drawQuad(g2d, p, 1.0, yMax); // Original quad
            quad.drawQuad(g2d, p, curScale, yMax); // Scaled quad
        }
    }
    
    /**
     * Draw animation points
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param voronoiPoints Intersection points of quads as they blow up
     * @param voronoiPointRadius Visual radius of bisector ray points
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     */
    public void drawBisectorRayPoints(Graphics2D g2d, List<Point> voronoiPoints, int yMax, int voronoiPointRadius) {
        for (Point bisector : voronoiPoints.toArray(new Point[voronoiPoints.size()])) {
            g2d.fill(new Ellipse2D.Double(bisector.x + voronoiPointRadius, yMax - (bisector.y + voronoiPointRadius), voronoiPointRadius * 2, voronoiPointRadius * 2)); // x, y, width, height
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
    public void drawB2S(Graphics2D g2d, VoronoiBisector[] voronoiEdgesB2S, int yMax, boolean showB2S, boolean showB2S_hiddenCones) {
        for (VoronoiBisector bisector : voronoiEdgesB2S) {
            if (bisector.getTag().startsWith("b2s_chosen") && showB2S ||
                    bisector.getTag().startsWith("b2s_hidden") && showB2S_hiddenCones){
                g2d.drawLine((int)Math.round(bisector.getStartPoint().x), yMax - (int)Math.round(bisector.getStartPoint().y),
                        (int)Math.round(bisector.getEndPoint().x), yMax - (int)Math.round(bisector.getEndPoint().y));
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
    public void drawB3S(Graphics2D g2d, VoronoiBisector[] voronoiEdgesB3S, int yMax, boolean showB3S, boolean showB3S_hidden) {
        for (VoronoiBisector bisector : voronoiEdgesB3S) {
            if (bisector.getTag().startsWith("b3s") && showB3S && showB3S_hidden){
                g2d.setColor(Color.red);
                g2d.drawLine((int)Math.round(bisector.getStartPoint().x), yMax - (int)Math.round(bisector.getStartPoint().y),
                        (int)Math.round(bisector.getEndPoint().x), yMax - (int)Math.round(bisector.getEndPoint().y));
            }
            
        }
    }
    
    /**
     * Draw chosen bisectors between 3 points and their corresponding min quads in blue
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param quad Quadrilateral to draw through 3 points
     * @param chosenB3S Array of VoronoiBisector objects to draw to screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB3S If true, show bisectors between 3 sites that are marked "chosen"
     */
    public void drawChosenB3SAndMinQuads(Graphics2D g2d, Quadrilateral quad, VoronoiBisector[] chosenB3S, int yMax, boolean showB3S) {
        for (VoronoiBisector bisector : chosenB3S) {
            if (showB3S) {
                g2d.setStroke(new BasicStroke(7));
                g2d.drawLine((int)Math.round(bisector.getStartPoint().x), yMax - (int)Math.round(bisector.getStartPoint().y),
                        (int)Math.round(bisector.getEndPoint().x), yMax - (int)Math.round(bisector.getEndPoint().y));
                g2d.setStroke(new BasicStroke(2));
                //vd.quad.drawQuad(g2d, bisector.startPoint, 1.0, yMax); // Original quad
                //System.out.println(bisector.getMinQuadScale());
                quad.drawQuad(g2d, bisector.getStartPoint(), bisector.getMinQuadScale(), yMax);
                //vd.quad.drawQuad(g2d, bisector.startPoint, vd.curScale, yMax);
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
    public void drawDisplayEdges(Graphics2D g2d, List<VoronoiBisector> displayEdges, int yMax, boolean showB2S_hgRegion, boolean showB3S_fgRegion) {
        for (VoronoiBisector bisector : displayEdges.toArray(new VoronoiBisector[displayEdges.size()])) {
            // TODO: sometimes the bisector start or end point are null and I don't know why
            if (bisector.getStartPoint() != null && bisector.getEndPoint() != null &&
                    (bisector.getTag().equals("b2s_step") && showB2S_hgRegion ||
                    bisector.getTag().equals("b3s_step") && showB3S_fgRegion ||
                    bisector.getTag().equals("debug"))) {
                g2d.drawLine((int)Math.round(bisector.getStartPoint().x), yMax - (int)Math.round(bisector.getStartPoint().y),
                        (int)Math.round(bisector.getEndPoint().x), yMax - (int)Math.round(bisector.getEndPoint().y));
            }
        }
    }
    
    /**
     * Draw HG points used to find bisectors between 2 sites
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param h1
     * @param h2
     * @param g1
     * @param g2
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param pointRadius Visual radius of HG points
     * @param showB2S_hgPoints If true, show hg points used to find bisectors between 2 sites
     */
    public void drawB2S_hgPoints(Graphics2D g2d, Point[] h1, Point[] h2, Point[] g1, Point[] g2, int yMax, int pointRadius, boolean showB2S_hgPoints) {
        // Draw h12, g12 points on quads
        if (showB2S_hgPoints) {
            g2d.setColor(Color.red);
            for(int i = 0; i < h1.length; i ++) {
                g2d.fill(new Ellipse2D.Double(h1[i].x - pointRadius, yMax - h1[i].y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(h2[i].x - pointRadius, yMax - h2[i].y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(g1[i].x - pointRadius, yMax - g1[i].y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(g2[i].x - pointRadius, yMax - g2[i].y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
            }
        }
    }
    
}
