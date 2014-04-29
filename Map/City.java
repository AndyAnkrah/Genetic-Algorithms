import java.util.ArrayList;

public class City
{
    private String reference; // Name of city
    private double xcoord; // City's x coordinate on GUI
    private double ycoord; // City's y coordinate on GUI
    private ArrayList<City> unblockedPaths; // List of neighbours to this city
    private ArrayList<Double> distances; // Stores distances to the other cities (used for map GA / constructing graph)
    private boolean isObstacle; // True if this city is an obstacle vertex
    
    // City constructor method (parameters: name, x coordinate, y coordinate)
    public City(String name, double x, double y){
        reference = name;
        xcoord = x;
        ycoord = y;
        unblockedPaths = new ArrayList<City>();
        distances = new ArrayList<Double>();
    }
    
    // Get name
    public String getReference(){
        return reference;
    }
    
    // Get x coordinate
    public double getX(){
        return xcoord;
    }
    
    // Get y coordinate
    public double getY(){
        return ycoord;
    }
    
    // Set x coordinate
    public void setX(double x){
        xcoord = x;
    }
    
    // Set y coordinate
    public void setY(double y){
        ycoord = y;
    }
    
    // Set distances to other cities
    public void setDistances(double[] dists){
        distances.clear();
        for(double d : dists){
            distances.add(d);
        }
        
    }
    
    // Get distances to other cities
    public ArrayList<Double> getDistances(){
        return distances;
    }
    
    // Set feasible options from this city
    public void setUnblockedPaths(String[] cities){
        unblockedPaths.clear();
        for(String s : cities){
            unblockedPaths.add(RunGA.getCityByReference(s));
        }
    }
    
    // Get feasible options from this city
    public ArrayList<City> getUnblockedPaths(){
        return unblockedPaths;
    }
    
    // Printed output if the toString() method called
    public String toString(){
        return reference;
    }
    
}
