package dt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * Painter class is used to draw all Graphics2D elements to the screen
 * 
 * @author Lee Glendenning
 */
public class Painter {
    
    private final Graphics2D g2d;
    private final VoronoiDiagram vd;
    
    public Painter(Graphics2D g2d, VoronoiDiagram vd) {
        this.g2d = g2d;
        this.vd = vd;
    }
    
    /**
     * Draw points in point set and the Quadrilateral around each point
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param pointRadius Visual radius of points in point set
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     */
    public void drawPointsAndQuads(Graphics2D g2d, int pointRadius, int yMax) {
        for (Point p : vd.points) {
            g2d.setColor(p.getColour());
            // Subtract pointRadius because points are drawn at coordinates from top left
            g2d.fill(new Ellipse2D.Double(p.x * vd.pixelFactor - pointRadius, yMax - (p.y * vd.pixelFactor + pointRadius), pointRadius * 2, pointRadius * 2)); // x, y, width, height
            vd.quad.drawQuad(g2d, p, 1.0, vd.pixelFactor, yMax/*, false*/); // Original quad
            vd.quad.drawQuad(g2d, p, vd.curScale, vd.pixelFactor, yMax/*, false*/); // Scaled quad
        }
    }
    
    /**
     * TODO: what does this actually draw?
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param voronoiPointRadius Visual radius of bisector ray points
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     */
    public void drawBisectorRayPoints(Graphics2D g2d, int voronoiPointRadius, int yMax) {
        for (Point bisector : vd.voronoiPoints.toArray(new Point[vd.voronoiPoints.size()])) {
            g2d.fill(new Ellipse2D.Double(bisector.x * vd.pixelFactor + voronoiPointRadius, yMax - (bisector.y * vd.pixelFactor + voronoiPointRadius), voronoiPointRadius * 2, voronoiPointRadius * 2)); // x, y, width, height
        }
    }
    
    /**
     * Draw all bisectors between two sites
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB2S If true, draw bisectors between 2 sites that are marked "chosen"
     * @param showB2S_hiddenCones If true, draw bisectors between 2 sites that are marked "hidden"
     */
    public void drawB2S(Graphics2D g2d, int yMax, boolean showB2S, boolean showB2S_hiddenCones) {
        for (VoronoiBisector bisector : vd.voronoiEdgesB2S.toArray(new VoronoiBisector[vd.voronoiEdgesB2S.size()])) {
            if (bisector.getTag().startsWith("b2s_chosen") && showB2S ||
                    bisector.getTag().startsWith("b2s_hidden") && showB2S_hiddenCones){
                g2d.drawLine((int)Math.round(bisector.startPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * vd.pixelFactor), (int)Math.round(bisector.endPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * vd.pixelFactor));
            }
        }
    }
    
    /**
     * Draw bisectors between 3 sites
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB3S_hidden If true, show bisectors between 3 sites that are marked "hidden"
     */
    public void drawB3S(Graphics2D g2d, int yMax, boolean showB3S_hidden) {
        for (VoronoiBisector bisector : vd.voronoiEdgesB3S.toArray(new VoronoiBisector[vd.voronoiEdgesB3S.size()])) {
            if (bisector.getTag().startsWith("b3s") && showB3S_hidden){
                g2d.setColor(Color.red);
                g2d.drawLine((int)Math.round(bisector.startPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * vd.pixelFactor), (int)Math.round(bisector.endPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * vd.pixelFactor));
            }
            
        }
    }
    
    /**
     * Draw chosen bisectors between 3 points and their corresponding min quads in blue
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB3S If true, show bisectors between 3 sites that are marked "chosen"
     */
    public void drawChosenB3SAndMinQuads(Graphics2D g2d, int yMax, boolean showB3S) {
        for (VoronoiBisector bisector : vd.voronoiEdgesB3S.toArray(new VoronoiBisector[vd.voronoiEdgesB3S.size()])) {
            if (bisector.getTag().startsWith("b3s_chosen") && showB3S) {
                g2d.setStroke(new BasicStroke(7));
                g2d.drawLine((int)Math.round(bisector.startPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * vd.pixelFactor), (int)Math.round(bisector.endPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * vd.pixelFactor));
                g2d.setStroke(new BasicStroke(2));
                vd.quad.drawQuad(g2d, bisector.startPoint, 1.0, vd.pixelFactor, yMax/*, bisector.isReflected()*/); // Original quad
                vd.quad.drawQuad(g2d, bisector.startPoint, bisector.getMinQuadScale(), vd.pixelFactor, yMax/*, bisector.isReflected()*/);
                //vd.quad.drawQuad(g2d, bisector.startPoint, vd.curScale, vd.pixelFactor, yMax/*, false*/);
            }
        }
    }
    
    /**
     * Draw lines for debugging that show the process of the triangulation
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param showB2S_hgRegion If true, show lines corresponding to the hg region of b2s
     * @param showB3S_fgRegion If true, show lines corresponding to the fg region of b3s
     */
    public void drawDisplayEdges(Graphics2D g2d, int yMax, boolean showB2S_hgRegion, boolean showB3S_fgRegion) {
        for (VoronoiBisector bisector : vd.displayEdges.toArray(new VoronoiBisector[vd.displayEdges.size()])) {
            // TODO: sometimes the bisector start or end point are null and I don't know why
            if (bisector.startPoint != null && bisector.endPoint != null &&
                    (bisector.getTag().equals("b2s_step") && showB2S_hgRegion ||
                    bisector.getTag().equals("b3s_step") && showB3S_fgRegion ||
                    bisector.getTag().equals("debug"))) {
                g2d.drawLine((int)Math.round(bisector.startPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.startPoint.y * vd.pixelFactor), (int)Math.round(bisector.endPoint.x * vd.pixelFactor), yMax - (int)Math.round(bisector.endPoint.y * vd.pixelFactor));
            }
        }
    }
    
    /**
     * Draw HG points used to find bisectors between 2 sites
     * 
     * @param g2d Graphics2D object used to draw to the screen
     * @param yMax Max y pixel on screen used to draw from bottom to top of screen as y increases
     * @param pointRadius Visual radius of HG points
     * @param showB2S_hgPoints If true, show hg points used to find bisectors between 2 sites
     */
    public void drawB2S_hgPoints(Graphics2D g2d, int yMax, int pointRadius, boolean showB2S_hgPoints) {
        // Draw h12, g12 points on quads
        if (showB2S_hgPoints) {
            g2d.setColor(Color.red);
            for(int i = 0; i < vd.h1.size(); i ++) {
                g2d.fill(new Ellipse2D.Double(vd.h1.get(i).x - pointRadius, yMax - vd.h1.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(vd.h2.get(i).x - pointRadius, yMax - vd.h2.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(vd.g1.get(i).x - pointRadius, yMax - vd.g1.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
                g2d.fill(new Ellipse2D.Double(vd.g2.get(i).x - pointRadius, yMax - vd.g2.get(i).y - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
            }
        }
    }
    
}
