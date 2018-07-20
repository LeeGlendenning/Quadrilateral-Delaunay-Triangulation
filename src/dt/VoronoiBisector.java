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
    private final String tag; // "b2s" = bisector of 2 sites, "b3s" = bisector of 3 sites
    private double minQuadScale;
    //private boolean reflected;
    
    /**
     * Create a Bisector having two endpoints and store the points that belong to it
     * 
     * @param adjacentPts Subset of the point set that the bisector belongs to. Size 2 or 3
     * @param startPt An endpoint of the bisector
     * @param endPt An endpoint of the bisector
     * @param tag String describing the bisector
     */
    public VoronoiBisector(Point[] adjacentPts, Point startPt, Point endPt, String tag) {
        this.adjacentPoints = Utility.arrayToList(adjacentPts);
        this.startPoint = startPt;
        this.endPoint = endPt;
        this.tag = tag;
        this.minQuadScale = 1.0;
        //this.reflected = false;
    }
    
    /**
     * 
     * @return True if min quad for this bisector should be reflected
     */
    /*public boolean isReflected() {
        return this.reflected;
    }*/
    
    /**
     * 
     * @param isReflected Boolean for whether to reflect the min quad for this bisector
     */
    /*public void setReflected(boolean isReflected) {
        this.reflected = isReflected;
    }*/
    
    /**
     * 
     * @param scale Scaling for minimum quad
     */
    public void setMinQuadScale(double scale) {
        this.minQuadScale = scale;
    }
    
    /**
     * 
     * @return Scaling for minimum quad
     */
    public double getMinQuadScale() {
        return this.minQuadScale;
    }
    
    /**
     * 
     * @return Deep copy of adjacent points array as an ArrayList
     */
    public ArrayList<Point> getAdjacentPtsArrayList() {
        ArrayList<Point> adjCopy = new ArrayList();
        for (int i = 0; i < this.adjacentPoints.size(); i ++) {
            adjCopy.add(new Point(this.adjacentPoints.get(i).x, this.adjacentPoints.get(i).y));
        }
        return adjCopy;
    }
    
    /**
     * 
     * @return Deep copy of adjacent points array
     */
    public Point[] getAdjacentPtsArray() {
        Point[] adjCopy = new Point[this.adjacentPoints.size()];
        for (int i = 0; i < this.adjacentPoints.size(); i ++) {
            adjCopy[i] = new Point(this.adjacentPoints.get(i).x, this.adjacentPoints.get(i).y);
        }
        return adjCopy;
    }
    
    /**
     * 
     * @return String tag set for the bisector
     */
    public String getTag() {
        return this.tag;
    }
    
    /**
     * 
     * @return start Point
     */
    public Point getStartPoint() {
        return new Point(this.startPoint.x, this.startPoint.y);
    }
    
    /**
     * 
     * @return end Point
     */
    public Point getEndPoint() {
        return new Point(this.endPoint.x, this.endPoint.y);
    }
    
    /**
     * 
     * @return Deep copy of this VoronoiBisector
     */
    public VoronoiBisector deepCopy() {
        VoronoiBisector copy = new VoronoiBisector(Utility.deepCopyPointArray(this.getAdjacentPtsArray()), 
                new Point(this.startPoint.x, this.startPoint.y), new Point(this.endPoint.x, this.endPoint.y), this.tag);
        copy.setMinQuadScale(this.minQuadScale);
        
        return copy;
    }
    
}
