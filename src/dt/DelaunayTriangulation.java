package dt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Constructs a DT of a point set and a quadrilateral by constructing a VoronoiDiagram then computing its dual
 * 
 * @author Lee Glendenning
 */
public class DelaunayTriangulation extends JPanel{
    
    private final ArrayList<Point> points;
    private ArrayList<DelaunayNode> nodes;
    
    /**
     * Create Delaunay Triangulation from dual of Voronoi diagram
     * 
     * @param q Quadrilateral
     * @param p Point set
     */
    public DelaunayTriangulation(Quadrilateral q, ArrayList<Point> p) {
        this.points = p;
        
        System.out.println("Creating DT");
        constructDT(new VoronoiDiagram(q, points));
    }
    
    /**
     * Construct DT by computing the dual of the Voronoi diagram
     * 
     * @param v Voronoi diagram to construct DT from
     */
    private void constructDT(VoronoiDiagram v) {
        initNodes();
        computeDual(v);
    }
    
    /**
     * Create a DelaunayNode object for each point in the point set
     */
    private void initNodes() {
        
    }
    
    /**
     * Define all DelaunayNode neighbours by computing the dual of the VoronoiDiagram
     * 
     * @param v Voronoi diagram to compute dual of
     */
    private void computeDual(VoronoiDiagram v) {
        
    }
    
    /**
     * Compute the shortest path in the DT between two points
     * 
     * @param p1 A point in the DT
     * @param p2 A point in the DT
     * @return List of DeulaunayNode objects representing the shortest path
     */
    public ArrayList<DelaunayNode> computeShortestPath(Point p1, Point p2) {
        ArrayList<DelaunayNode> shortestPath = new ArrayList();
        
        return shortestPath;
    }
    
    /**
     * Compute the length of the path
     * 
     * @param path List of DelaunayNodes defining a path in the DT
     * @return Length of the path
     */
    public double computeLengthOfPath(ArrayList<DelaunayNode> path) {
        double length = 0.0;
        
        return length;
    }
    
    /**
     * Compute the stretch factor of the DT
     * 
     * @return Stretch factor of the DT
     */
    public double computeStretchFactorOfDT() {
        double stretchFactor = 0.0;
        
        return stretchFactor;
    }
    
    /**
     * Compute the stretch factor of the path
     * 
     * @param path List of DelaunayNodes defining a path in the DT
     * @return Stretch factor of the path
     */
    public double computeStretchFactorOfPath(ArrayList<DelaunayNode> path) {
        double stretchFactor = 0.0;
        
        return stretchFactor;
    }
    
    /**
     * Draw the Delaunay Triangulation to the screen
     */
    public void drawDT() {
        System.out.println("Drawing DT");
        
        // Set up display window
        JFrame window = new JFrame("Delaunay Triangulation");
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
    
    /** 
     * Draws the Delaunay triangulation to the window
     * 
     * @param g Graphics object used to draw to the screen
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        //g.setColor(new Color(51, 153, 255));
        
        // Draw points - change to nodes later
        for (Point p : this.points) {
            g.fillOval((int)Math.round(p.x) * 100, (int)Math.round(p.y) * 100, 10, 10); // x, y, width, height
        }
        
    }
    
}
