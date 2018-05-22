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
public class VoronoiDiagram extends JPanel {

    private final ArrayList<Point> points;
    private final Quadrilateral quad;
    private ArrayList<VoronoiBisector> voronoiEdges;
    private ArrayList<Point> voronoiPoints; // Temporary until bisector points are grouped as edges
    private double curScale = 1.0;
    private final int pixelFactor = 1;

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
        voronoiPoints = new ArrayList();

        constructVoronoi();
        drawVoronoi();
    }

    /**
     * Construct Voronoi diagram for the point set using the quad
     */
    private void constructVoronoi() {
        // Limit iterations such that only intersections are found within the window area
        for (int iterations = 0; iterations < 1000; iterations++) {
            this.curScale += 0.1;
            this.quad.scaleQuad(this.curScale);

            // for each pair of points, check for quad intersection
            for (int i = 0; i < points.size(); i++) {
                for (int j = i + 1; j < points.size(); j++) {
                    // Find and store intersections for current quad scaling
                    findQuadIntersections(this.quad, this.points.get(i), this.points.get(j));
                }
            }

        }
    }

    /**
     * Determine whether Quadrilateral q around two points has an intersection,
     * add to voronoiPoints
     *
     * @param q Reference quad
     * @param p1 First point
     * @param p2 Second point
     */
    public void findQuadIntersections(Quadrilateral q, Point p1, Point p2) {
        Point[] quad1 = q.getPixelVertsForPoint(p1, this.curScale, this.pixelFactor);
        Point[] quad2 = q.getPixelVertsForPoint(p2, this.curScale, this.pixelFactor);

        int k, l;
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                k = 3;
            } else {
                k = i + 1;
            }
            for (int j = 0; j < 3; j++) {
                if (j == 0) {
                    l = 3;
                } else {
                    l = j + 1;
                }
                //System.out.println("Comparing line segments: (" + quad1[i].x + ", " + quad1[i].y + ") ("+ quad1[k].x + ", " + quad1[k].y + ") and (" + quad2[j].x + ", " + quad2[j].y + ") ("+ quad2[l].x + ", " + quad2[l].y + ")");
                Point intersectionPoint;
                if ((intersectionPoint = doLineSegmentsIntersect(quad1[i], quad1[k], quad2[j], quad2[l])) != null) {
                    //System.out.println("Found intersection at (" + intersectionPoint.x + ", " + intersectionPoint.y + ")");
                    this.voronoiPoints.add(intersectionPoint);
                }
            }
        }

    }

    /**
     * Determine whether two line segments intersect using vector cross product
     * approach Method outlined in http://stackoverflow.com/a/565282/786339
     *
     * @param p1 First point of first line segment
     * @param p2 Second point of first line segment
     * @param q1 First point of second line segment
     * @param q2Second point of second line segment
     * @return Intersection point if the line segments intersect, null otherwise
     */
    private Point doLineSegmentsIntersect(Point p1, Point p2, Point q1, Point q2) {
        Point r = subtractPoints(p2, p1);
        Point s = subtractPoints(q2, q1);

        double numerator = crossProduct(subtractPoints(q1, p1), r);
        double denominator = crossProduct(r, s);

        if (numerator == 0 && denominator == 0) {

            // If line segments share an endpoint, line segments intersect
            if (equalPoints(p1, q1) || equalPoints(p1, q2) || equalPoints(p2, q1) || equalPoints(p2, q2)) {
                Point intersection;
                if (equalPoints(p1, q1) || equalPoints(p1, q2)) {
                    intersection = p1;
                } else {
                    intersection = p2;
                }
                //System.out.println("1Found intersection at (" + intersection.x + ", " + intersection.y + ")");
                return intersection;
            }

            // Line segments overlap if all point differences in either direction have the same sign
            // Otherwise they do not, return false
            if (!allEqual(new boolean[]{(q1.x - p1.x < 0),
                (q1.x - p2.x < 0),
                (q2.x - p1.x < 0),
                (q2.x - p2.x < 0)})
                    || !allEqual(new boolean[]{(q1.y - p1.y < 0),
                (q1.y - p2.y < 0),
                (q2.y - p1.y < 0),
                (q2.y - p2.y < 0)}
                    )) {
                return null;
            }
        }

        // Lines are parallel and do not intersect
        if (denominator == 0) {
            return null;
        }

        double u = numerator / denominator;
        double t = crossProduct(subtractPoints(q1, p1), s) / denominator;

        if ((t >= 0) && (t <= 1) && (u >= 0) && (u <= 1)) {
            Point intersection;
            r.x *= t;
            r.y *= t;
            intersection = addPoints(p1, r);
            //System.out.println("2Found intersection at (" + intersection.x + ", " + intersection.y + ")");
            return intersection;
        }

        return null;
    }

    /**
     * Take the cross product of two point objects
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Cross product of the two points
     */
    private double crossProduct(Point p1, Point p2) {
        return (p1.x * p2.y) - (p1.y * p2.x);
    }

    /**
     * Subtract the x and y values of point object from another
     *
     * @param p1 Point to be subtracted from
     * @param p2 Point to subtract other point by
     * @return Result of p1 - p2
     */
    private Point subtractPoints(Point p1, Point p2) {
        Point subP = new Point();
        subP.x = p1.x - p2.x;
        subP.y = p1.y - p2.y;

        return subP;
    }

    /**
     * Add the x and y values of two points
     *
     * @param p1 First point
     * @param p2 Second point
     * @return Result of p1 + p2
     */
    private Point addPoints(Point p1, Point p2) {
        Point addP = new Point();
        addP.x = p1.x + p2.x;
        addP.y = p1.y + p2.y;

        return addP;
    }

    /**
     * Determine whether the x and y values of two points are both equal
     *
     * @param p1 First point to compare
     * @param p2 Second point to compare
     * @return True if point are equal, false otherwise
     */
    private boolean equalPoints(Point p1, Point p2) {
        return (Math.round(p1.x) == Math.round(p2.x)) && (Math.round(p1.y) == Math.round(p2.y));
    }

    /**
     * Determine whether an array of boolean values all have the same value
     *
     * @param arguments Array of boolean values
     * @return True if all array elements are the same, false otherwise
     */
    private boolean allEqual(boolean[] arguments) {
        boolean firstValue = arguments[0];

        for (int i = 1; i < arguments.length; i++) {
            if (arguments[i] != firstValue) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a window and draw the Voronoi diagram to the screen
     */
    private void drawVoronoi() {
        System.out.println("Drawing Voronoi diagram\n");

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

        int pointRadius = 3, voronoiPointRadius = 1;

        Graphics2D g2d = (Graphics2D) g;

        // Draw points and quads
        for (Point p : this.points) {
            g2d.setColor(randomColour());
            // Subtract pointRadius because points are drawn at coordinates from top left
            g2d.fill(new Ellipse2D.Double(p.x * this.pixelFactor - pointRadius, p.y * this.pixelFactor - pointRadius, pointRadius * 2, pointRadius * 2)); // x, y, width, height
            quad.drawQuad(g2d, p, this.curScale, this.pixelFactor);
        }

        g2d.setColor(Color.black);

        // Draw bisectors
        for (Point bisector : this.voronoiPoints) {
            g2d.fill(new Ellipse2D.Double(bisector.x * this.pixelFactor - voronoiPointRadius, bisector.y * this.pixelFactor - voronoiPointRadius, voronoiPointRadius * 2, voronoiPointRadius * 2)); // x, y, width, height
            //g2d.drawLine(bisector.startPoint.x * scaleFactor, bisector.startPoint.y * scaleFactor, bisector.endPoint.x * scaleFactor, bisector.endPoint.y * scaleFactor);
        }

        //System.out.println("***********************");
    }

}
