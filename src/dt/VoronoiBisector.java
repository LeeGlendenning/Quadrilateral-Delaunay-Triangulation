package dt;


/**
 * Bisector for Voronoi diagram defined by at most 4 segments
 * 
 * @author Lee Glendenning
 */
public class VoronoiBisector {
    
    Point[] adjacentPoints; // Points that this bisector belongs to. Size 2 or 3. Necessary for computing dual of Voronoi
    //ArrayList<Point[]> bisectorSegments; // List of start and end Points of the bisector segments. start/endpts may be equal, list size <= 4
    Point startPoint, endPoint;
    private final String tag; // "b2s" = bisector of 2 sites, "b3s" = bisector of 3 sites
    
    /**
     * Create a Bisector having two endpoints and store the points that belong to it
     * 
     * @param adjacentPts Subset of the point set that the bisector belongs to. Size 2 or 3
     * @param startPt An endpoint of the bisector
     * @param endPt An endpoint of the bisector
     */
    public VoronoiBisector(Point[] adjacentPts, Point startPt, Point endPt, String tag) {
        this.adjacentPoints = adjacentPts;
        this.startPoint = startPt;
        this.endPoint = endPt;
        this.tag = tag;
    }
    
    /**
     * 
     * @return String tag set for the bisector
     */
    public String getTag() {
        return this.tag;
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
