package dt;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Constructs a Voronoi diagram from a point set using a Quadrilateral
 * 
 * @author Lee Glendenning
 */
public class VoronoiDiagram extends JPanel{
    
    private final ArrayList<Point> points;
    private final Quadrilateral quad;
    private ArrayList<VoronoiBisector> voronoiEdges;
    
    /**
     * Construct Voronoi diagram for point set using a Quadrilateral
     * 
     * @param q Quadrilateral
     * @param p Point set
     */
    public VoronoiDiagram(Quadrilateral q, ArrayList<Point> p) {
        this.points = p;
        this.quad = q;
        voronoiEdges = new ArrayList();
        
        constructVoronoi();
        drawVoronoi();
    }
    
    /**
     * Construct Voronoi diagram for the point set using the quad
     */
    private void constructVoronoi() {
        // for each pair of points, find bisectors
        for (int i = 0; i < points.size(); i ++) 
        {
            for (int j = 1+i; j < points.size(); j ++) 
            {
                // 1 bisector has negative recipricol of slope p1,p2
                /*Point p1 = points.get(i);
                Point p2 = points.get(j);
                ArrayList<Point> adjPoints = new ArrayList();
                adjPoints.add(p1);
                adjPoints.add(p2);
                int xIntercept = p2.x - p1.x;
                Point bisectorStartPt = new Point(-(p2.y - p1.y) + xIntercept, (p2.x - p1.x));
                Point bisectorEndPt = new Point((p2.y - p1.y) + xIntercept, -(p2.x - p1.x));
                System.out.println("BisectorStartPt: (" + bisectorStartPt.x + ", " + bisectorStartPt.y + ")");
                System.out.println("bisectorEndPt: (" + bisectorEndPt.x + ", " + bisectorEndPt.y + ")");
                voronoiEdges.add(new VoronoiBisector(adjPoints, bisectorStartPt, bisectorEndPt));*/
            }
        }
    }
    
    /**
     * Create a window and draw the Voronoi diagram to the screen
     */
    private void drawVoronoi() {
        System.out.println("Drawing Voronoi diagram");
        
        // Set up display window
        JFrame window = new JFrame("Voronoi Diagram");
        window.setSize(800, 700);
        window.setResizable(false);
        window.setLocation(375, 25);
        window.getContentPane().setBackground(Color.BLACK);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = window.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this, BorderLayout.CENTER);
        window.setPreferredSize(new Dimension(800, 700));
        window.setLocationRelativeTo(null);
        window.pack();
        window.setVisible(true);
        quad.scaleQuad(4.0);
    }
    
    /** 
     * Draws the Voronoi diagram to the window
     * 
     * @param g Graphics object used to draw to the screen
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int pixelFactor = 1;
        int pointRadius = 3;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(1.5f));
        
        // Draw points and quads
        for (Point p : this.points) 
        {
            // Subtract pointRadius because points are drawn at coordinates from top left
            g2d.fill(new Ellipse2D.Double(p.x * pixelFactor - pointRadius, p.y * pixelFactor - pointRadius, pointRadius*2, pointRadius*2)); // x, y, width, height
            quad.drawQuad(g2d, p, pixelFactor);
        }
        
        // Draw bisectors
        /*for (VoronoiBisector bisector : voronoiEdges)
        {
            g2.drawLine(bisector.startPoint.x * scaleFactor, bisector.startPoint.y * scaleFactor, bisector.endPoint.x * scaleFactor, bisector.endPoint.y * scaleFactor);
        }*/
        
        System.out.println("***********************");
    }
    
}
