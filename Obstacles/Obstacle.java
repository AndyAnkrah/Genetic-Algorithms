import java.util.ArrayList;
import java.awt.geom.Line2D;
import java.awt.*;

public class Obstacle extends Polygon // Obstacle inherits the characteristics of the Polygon class
{
    private String reference; // name of obstacle
    private double offset; // offset by which the path avoids hitting the obstacle vertices
    private int centreX; // centre x coordinate
    private int centreY; // centre y coordinate
    
    // Obstacle constructor method (parameters: name, array of x coordinates of vertices, array of y coordinates of vertices, offset, centre x, centre y)
    public Obstacle(String name, int[] x, int[] y, double border, int centX, int centY){
        reference = name;
        offset = border;
        xpoints = x;
        ypoints = y;
        npoints = x.length; // number of vertices
        // if centre is < (0,0), further calculation finds centre
        if(centX > 0){
            centreX = centX;
        } else{
            centreX = (getMinFromArray(x)+getMaxFromArray(x))/2;
        }
        if(centY > 0){
            centreY = centY;
        } else{
            centreY = (getMinFromArray(y)+getMaxFromArray(y))/2;;
        }
    }
    
    // Get max value from a specified array of integers
    public int getMaxFromArray(int[] array){
        int max = 0;
        for(int i : array){
            if(i > max){
                max = i;
            }
        }
        return max;
    }
    
    // Get min value from a specified array of integers
    public int getMinFromArray(int[] array){
        int min = 10000;
        for(int i : array){
            if(i < min){
                min = i;
            }
        }
        return min;
    }
    
    // Get name of the obstacle
    public String getReference(){
        return reference;
    }
    
    // Get offset of the obstacle
    public double getOffset(){
        return offset;
    }
    
    // Get centre x coordinate of the obstacle
    public int getCentreX(){
        return centreX;
    }
    
    // Get centre y coordinate of the obstacle
    public int getCentreY(){
        return centreY;
    }
    
    // Returns true if the straight line between the two specified cities hits this obstacle
    public boolean hitsObstacle(City from, City to){
        for(int i=0; i<npoints-1; i++){
            // Use Line2D to check whether the path between the two cities intersects one of the obstacle's edges
            if(Line2D.linesIntersect(from.getX(),from.getY(),to.getX(),to.getY(),xpoints[i],ypoints[i],xpoints[i+1],ypoints[i+1])){
                return true;
            }
        }
        // Check last edge for intersection
        if(Line2D.linesIntersect(from.getX(),from.getY(),to.getX(),to.getY(),xpoints[0],ypoints[0],xpoints[npoints-1],ypoints[npoints-1])){
            return true;
        }
        return false;
    }
    
}
