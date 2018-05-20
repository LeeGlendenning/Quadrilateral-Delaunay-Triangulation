package dt;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Random;
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
    private double curScale = 1.0;
    
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
        
        for (int iterations = 0; iterations < 340; iterations ++) {
            
            this.curScale += 0.1;
            this.quad.scaleQuad(this.curScale);
            
            // for each pair of points, check for quad intersection
            for (int i = 0; i < points.size(); i ++) 
            {
                for (int j = i+1; j < points.size(); j ++) 
                {
                    if (isIntersection(this.quad, this.points.get(i), this.points.get(j))) {
                        // Found first intersection, all we care about rn
                        // To do: draw it and keep going
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Determine whether Quadrilateral q around two points has an intersection
     * 
     * @param q Reference quad
     * @param p1 First point
     * @param p2 Second point
     * @return true if q intersects the quad, false otherwise
     */
    public boolean isIntersection(Quadrilateral q, Point p1, Point p2) {
        
        
        
        return false;
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
    }
    
    private Color randomColour() {
        Random rand = new Random();
        return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
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
        //g2d.setStroke(new BasicStroke(1.5f));
        
        // Draw points and quads
        for (Point p : this.points) 
        {
            g2d.setColor(randomColour());
            // Subtract pointRadius because points are drawn at coordinates from top left
            g2d.fill(new Ellipse2D.Double(p.x * pixelFactor - pointRadius, p.y * pixelFactor - pointRadius, pointRadius*2, pointRadius*2)); // x, y, width, height
            quad.drawQuad(g2d, p, this.curScale, pixelFactor);
        }
        
        // Draw bisectors
        /*for (VoronoiBisector bisector : voronoiEdges)
        {
            g2d.drawLine(bisector.startPoint.x * scaleFactor, bisector.startPoint.y * scaleFactor, bisector.endPoint.x * scaleFactor, bisector.endPoint.y * scaleFactor);
        }*/
        
        System.out.println("***********************");
    }
    
}
