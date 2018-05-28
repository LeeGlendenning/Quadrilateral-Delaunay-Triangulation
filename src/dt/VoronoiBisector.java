package dt;

import java.util.ArrayList;

/**
 * Bisector for Voronoi diagram defined by at most 4 segments
 * 
 * @author Lee Glendenning
 */
public class VoronoiBisector {
    
    ArrayList<Point> adjacentPoints; // Points that this bisector belongs to. Size 2 or 3. Necessary for computing dual of Voronoi
    //ArrayList<Point[]> bisectorSegments; // List of start and end Points of the bisector segments. start/endpts may be equal, list size <= 4
    Point startPoint, endPoint;
    
    /**
     * Create a Bisector having two endpoints and store the points that belong to it
     * 
     * @param adjacentPts Subset of the point set that the bisector belongs to. Size 2 or 3
     * @param startPt An endpoint of the bisector
     * @param endPt An endpoint of the bisector
     */
    public VoronoiBisector(/*ArrayList<Point> adjacentPts,*/ Point startPt, Point endPt) {
        //this.adjacentPoints = adjacentPts;
        //bisectorSegments = new ArrayList();
        this.startPoint = startPt;
        this.endPoint = endPt;
    }
    
    /**
     * Add a line segment to the bisector
     * 
     * @param startPt An endpoint of the bisector segment
     * @param endPt An endpoint of the bisector segment
     */
    /*public void addSegment(Point startPt, Point endPt) {
        bisectorSegments.add(new Point[]{startPt, endPt});
    }*/
    
}
