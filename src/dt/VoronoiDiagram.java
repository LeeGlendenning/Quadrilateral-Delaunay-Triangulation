package dt;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Constructs a Voronoi diagram from a point set using a Quadrilateral
 *
 * @author Lee Glendenning
 */
public class VoronoiDiagram extends JPanel {

    protected final List<Point> points, voronoiPoints; // voronoiPoints used for animation
    protected final Quadrilateral quad;
    // Consider using synchronized list to avoid concurrent modification...
    protected List<VoronoiBisector> displayEdges;
    private VoronoiBisector[] chosenB3S;
    protected double curScale = 1.0;
    protected final int pixelFactor = 1;
    private int scaleIterations;
    private Timer timer;
    private final FindBisectorsTwoSites b2s;
    private final FindBisectorsThreeSites b3s;
    
    private final boolean showB2S_hgRegion = false, showB2S_hgPoints = false, showB2S_hiddenCones = true, showB2S = true;
    private final boolean showB3S_fgRegion = false, showB3S_hidden = true, showB3S = true;
    private final boolean doAnimation = false;
    
    UI userInterface;
    
    //protected final ArrayList<Point> h1, h2, g1, g2;

    /**
     * Construct Voronoi diagram for point set using a Quadrilateral
     *
     * @param q Quadrilateral
     * @param p Point set
     */
    public VoronoiDiagram(Quadrilateral q, ArrayList<Point> p) {
        this.points = p;
        this.quad = q;
        
        this.displayEdges = Collections.synchronizedList(new ArrayList());
        this.voronoiPoints = Collections.synchronizedList(new ArrayList());
        this.scaleIterations = 0;
        
        b2s = new FindBisectorsTwoSites();
        b3s = new FindBisectorsThreeSites(this.getBounds().getSize().height, this.getBounds().getSize().width);
        
        findB2SAndB3S();
        
        //displayVoronoiDiagram();
        //constructVoronoi();
        if (this.doAnimation) {
            doVoronoiAnimation(40, 1000, b2s, b3s);
        } else {
            doVoronoiAnimation(40, 0, b2s, b3s);
        }
    }
    
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
     * Find all bisectors between 2 points and bisectors between 3 points
     */
    private void findB2SAndB3S() {
        System.out.println("Finding Bisectors Between 2 Sites:\n");
        
        // For each pair of points, find bisector
        for (int i = 0; i < this.points.size(); i++) {
            for (int j = i + 1; j < this.points.size(); j++) {
                b2s.findBisectorOfTwoSites(this.quad, this.points.get(i), this.points.get(j));
                System.out.println();
            }
        }
        
        VoronoiBisector[] voronoiEdgesB2S = b2s.getVoronoiEdges();
        
        System.out.println("\nFinding Bisectors Between 3 Sites:\n");
        // For each triplet of points, find bisector
        for (int i = 0; i < this.points.size(); i ++) {
            for (int j = i + 1; j < this.points.size(); j++) {
                for (int k = j + 1; k < this.points.size(); k++) {
                    //System.out.println("i = " + i + ", j = " + j + ", k = " + k);
                    Point left = new Point(), right = new Point();
                    Utility.setLeftAndRightPoint(this.points.get(i), this.points.get(j), left, right, Utility.calculateAngle(this.points.get(i), this.points.get(j)));
                    b3s.findBisectorOfThreeSites(this.quad, voronoiEdgesB2S, left, right, this.points.get(k));
                    System.out.println();
                }
            }
        }
        
        System.out.println("Drawing minimum quads");
        this.chosenB3S = b3s.getChosenBisectors();
        calculateMinQuads();
    }
    
    
    
    
    
    
    /**
     * Find and apply the scaling for each of the minimum quads
     */
    public void calculateMinQuads() {
        for (VoronoiBisector bisector : chosenB3S) {
            Double scale;
            if (bisector.getTag().contains("chosen") && (scale = findMinimumQuadScaling(bisector, this.curScale, this.pixelFactor)) != null) {
                System.out.println("Scale = " + scale + "\n");
                bisector.setMinQuadScale(scale);
            }
        }
    }
    
    /**
     * @param chosenB3S Chosen VoronoiBisector between 3 sites
     * @return Amount the quad needs to be scaled such that it goes through the adjacent B3S points
     */
    private Double findMinimumQuadScaling(VoronoiBisector chosenB3S, double curScale, int pixelFactor) {
        Point[] qVerts = this.quad.getPixelVertsForPoint(chosenB3S.getEndPoint(), curScale, pixelFactor/*, chosenB3S.isReflected()*/);
        /*System.out.println("qVerts for " + chosenB3S.getEndPoint());
        for (Point p : qVerts) {
            System.out.print(p + " ");
        }
        System.out.println();*/
        
        Point[][] quadRays = new Point[4][2]; // Rays from quad center through each vertex
        for (int i = 0; i < 4; i ++) {
            quadRays[i] = findMinQuadRay(chosenB3S.getEndPoint(), chosenB3S.getEndPoint(), qVerts[i]);
            //this.displayEdges.add(new VoronoiBisector(new Point[]{}, quadRays[i][0], quadRays[i][1], "debug"));
        }
        
        Double scale = null, tempScale;
        for (Point adj : chosenB3S.getAdjacentPtsArray()) {
            tempScale = findScaleForAdjacentB3SPt(adj, quadRays, qVerts, chosenB3S.getEndPoint());
            if (scale == null || tempScale > scale) {
                scale = tempScale;
            }
        }
        
        return scale;
    }
    
    /**
     * 
     * @param adj A Point belonging to the B3S
     * @param quadRays Rays from quad center through each vertex
     * @param qVerts Point array of quad vertices
     * @return Quad scaling based on intersection of adj Point parallel to all quad vertices and the quad rays
     */
    private Double findScaleForAdjacentB3SPt(Point adj, Point[][] quadRays, Point[] qVerts, Point chosenB3SPt) {
        Double scale = null;
        
        Point intersectionPt;
        
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
            Point[] intersectionRay1 = findMinQuadRay(adj, qVerts[i], qVerts[ii]);
            //this.displayEdges.add(new VoronoiBisector(new Point[]{}, intersectionRay1[0], intersectionRay1[1], "debug"));
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay1[0], intersectionRay1[1], quadRays[i][0], quadRays[i][1])) != null) {
                //System.out.println("1dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[i], chosenB3SPt));
                tempScale = Utility.euclideanDistance(intersectionPt, chosenB3SPt) / Utility.euclideanDistance(qVerts[i], chosenB3SPt);
                if (scale == null || tempScale > scale) {
                    scale = tempScale;
                }
            }
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay1[0], intersectionRay1[1], quadRays[ii][0], quadRays[ii][1])) != null) {
                //System.out.println("2dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[ii], chosenB3SPt));
                tempScale = Utility.euclideanDistance(intersectionPt, chosenB3SPt) / Utility.euclideanDistance(qVerts[ii], chosenB3SPt);
                if (scale == null || tempScale > scale) {
                    scale = tempScale;
                }
            }
            
            // Create ray from adj parallel to quad edge in other direction
            Point[] intersectionRay2 = findMinQuadRay(adj, qVerts[ii], qVerts[i]);
            //this.displayEdges.add(new VoronoiBisector(new Point[]{}, intersectionRay2[0], intersectionRay2[1], "debug"));
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay2[0], intersectionRay2[1], quadRays[i][0], quadRays[i][1])) != null) {
                //System.out.println("3dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[i], chosenB3SPt));
                tempScale = Utility.euclideanDistance(intersectionPt, chosenB3SPt) / Utility.euclideanDistance(qVerts[i], chosenB3SPt);
                if (scale == null || tempScale > scale) {
                    scale = tempScale;
                }
            }
            if ((intersectionPt = Utility.doLineSegmentsIntersect(intersectionRay2[0], intersectionRay2[1], quadRays[ii][0], quadRays[ii][1])) != null) {
                //System.out.println("4dist(intersectionpt, chosenB3S) = " + Utility.euclideanDistance(qVerts[ii], chosenB3SPt));
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
     * @param translatePt Point ray startPt will be translated to
     * @param startPt Initial start point of ray
     * @param throughPt Point the ray will pass through before being translated
     */
    private Point[] findMinQuadRay(Point translatePt, Point startPt, Point throughPt) {
        //System.out.println("endPt = " + endPt + ", a = " + a + ", nonInnerVertex = " + nonInnerVertex);
        
        // Define the direction of the ray starting at a
        double rayEndx = Utility.RAY_SIZE;
        //System.out.println(a + " : " + nonInnerVertex);
        if (startPt.x > throughPt.x || (startPt.x == throughPt.x && startPt.y > throughPt.y)) {
            rayEndx = -Utility.RAY_SIZE;
        }
        Point rayEnd = new Point(rayEndx, startPt.y); // End point of ray which is basically + or - infinity
        
        double angle  = Utility.calculateAngle(startPt, throughPt); // Angle that slope(a, nonInnerVertex) makes with x axis
        
        // Define ray by rotating rayEnd such that it has slope(a, nonInnerVertex)
        Point[] ray = {new Point(startPt.x, startPt.y), Utility.rotatePoint(rayEnd, new Point(0,0), -angle)};
        
        //System.out.println("ray = " + ray[0] + ", " + ray[1]);
        
        //Translate ray so that it starts at endPt
        ray[0].x += translatePt.x - startPt.x;
        ray[0].y += translatePt.y - startPt.y;
        ray[1].x += translatePt.x - startPt.x;
        ray[1].y += translatePt.y - startPt.y;
        
        //this.voronoiEdges.add(new VoronoiBisector(ray[0], ray[1]));
        return new Point[]{ray[0], ray[1]};
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * Applies one step for the animation
     */
    private void animationStep() {
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

    /**
     * Determine whether Quadrilateral q around two points has an intersection,
     * add to voronoiPoints
     *
     * @param q Reference quad
     * @param p1 First point
     * @param p2 Second point
     */
    public void findQuadIntersections(Quadrilateral q, Point p1, Point p2) {
        Point[] quad1 = q.getPixelVertsForPoint(p1, this.curScale, this.pixelFactor/*, false*/);
        Point[] quad2 = q.getPixelVertsForPoint(p2, this.curScale, this.pixelFactor/*, false*/);

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
                //System.out.println("i = " + i + ", k = " + k + ", j = " + j + ", l = " + l);
                //System.out.println("Comparing line segments: (" + quad1[i].x + ", " + quad1[i].y + ") ("+ quad1[k].x + ", " + quad1[k].y + ") and (" + quad2[j].x + ", " + quad2[j].y + ") ("+ quad2[l].x + ", " + quad2[l].y + ")");
                Point intersectionPoint;
                if ((intersectionPoint = Utility.doLineSegmentsIntersect(quad1[i], quad1[k], quad2[j], quad2[l])) != null) {
                    //System.out.println("Found intersection at (" + intersectionPoint.x + ", " + intersectionPoint.y + ")");
                    this.voronoiPoints.add(intersectionPoint);
                }
            }
        }

    }

    /**
     * Create a window to draw the Voronoi diagram to the screen
     */
    /*private void displayVoronoiDiagram() {
        System.out.println("\nDrawing Voronoi diagram\n");

        this.userInterface = new UI(this);
    }*/
    
    /**
     * Draws the Voronoi diagram to the window
     *
     * @param g Graphics object used to draw to the screen
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Painter painter = new Painter(g2d, this);

        int pointRadius = 3, voronoiPointRadius = 1;
        int yMax = this.getBounds().getSize().height;

        
        painter.drawPointsAndQuads(g2d, yMax, pointRadius);

        g2d.setColor(Color.black);
        painter.drawBisectorRayPoints(g2d, yMax, voronoiPointRadius);
        
        // Draw bisector segments between 2 sites
        painter.drawB2S(g2d, b2s.getVoronoiEdges(), yMax, this.showB2S, this.showB2S_hiddenCones);
        
        // Draw bisector segments between 3 sites
        g2d.setStroke(new BasicStroke(5));
        painter.drawB3S(g2d, b3s.getVoronoiEdges(), yMax, this.showB3S_hidden);
        
        g2d.setColor(Color.blue);
        painter.drawChosenB3SAndMinQuads(g2d, this.chosenB3S, yMax, this.showB3S);
        
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(1));
        painter.drawDisplayEdges(g2d, yMax, this.showB2S_hgRegion, this.showB3S_fgRegion);
        
        painter.drawB2S_hgPoints(g2d, b2s.geth1(), b2s.geth2(), b2s.getg1(), b2s.getg2(), yMax, pointRadius, this.showB2S_hgPoints);
    }

}
