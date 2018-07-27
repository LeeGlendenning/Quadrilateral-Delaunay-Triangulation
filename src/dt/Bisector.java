package dt;

import java.util.ArrayList;


/**
 * Bisector for Voronoi diagram defined by at most 4 segments
 * 
 * @author Lee Glendenning
 */
public class Bisector {
    
    private final ArrayList<Vertex> adjacentVertices; // Vertices that this bisector belongs to. Size 2 or 3. Necessary for computing dual of Voronoi
    //ArrayList<Vertex[]> bisectorSegments; // List of start and end Vertices of the bisector segments. start/endpts may be equal, list size <= 4
    private final Vertex startVertex, endVertex;
    private final String tag; // "b2s" = bisector of 2 sites, "b3s" = bisector of 3 sites
    private double minQuadScale;
    //private boolean reflected;
    
    /**
     * Create a Bisector having two endvertices and store the vertices that belong to it
     * 
     * @param adjacentPts Subset of the vertex set that the bisector belongs to. Size 2 or 3
     * @param startPt An endvertex of the bisector
     * @param endPt An endvertex of the bisector
     * @param tag String describing the bisector
     */
    public Bisector(Vertex[] adjacentPts, Vertex startPt, Vertex endPt, String tag) {
        this.adjacentVertices = (ArrayList)Utility.arrayToList(adjacentPts);
        this.startVertex = startPt;
        this.endVertex = endPt;
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
     * @return Deep copy of adjacent vertices array as an ArrayList
     */
    public ArrayList<Vertex> getAdjacentPtsArrayList() {
        ArrayList<Vertex> adjCopy = new ArrayList();
        for (int i = 0; i < this.adjacentVertices.size(); i ++) {
            adjCopy.add(new Vertex(this.adjacentVertices.get(i).x, this.adjacentVertices.get(i).y));
        }
        return adjCopy;
    }
    
    /**
     * 
     * @return Deep copy of adjacent vertices array
     */
    public Vertex[] getAdjacentPtsArray() {
        Vertex[] adjCopy = new Vertex[this.adjacentVertices.size()];
        for (int i = 0; i < this.adjacentVertices.size(); i ++) {
            adjCopy[i] = new Vertex(this.adjacentVertices.get(i).x, this.adjacentVertices.get(i).y);
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
     * @return start Vertex
     */
    public Vertex getStartVertex() {
        return this.startVertex.deepCopy();
    }
    
    /**
     * 
     * @return end Vertex
     */
    public Vertex getEndVertex() {
        return this.endVertex.deepCopy();
    }
    
    /**
     * 
     * @return Deep copy of this VoronoiBisector
     */
    public Bisector deepCopy() {
        Bisector copy = new Bisector(Utility.deepCopyVertexArray(this.getAdjacentPtsArray()), 
                new Vertex(this.startVertex.x, this.startVertex.y), new Vertex(this.endVertex.x, this.endVertex.y), this.tag);
        copy.setMinQuadScale(this.minQuadScale);
        
        return copy;
    }
    
}
