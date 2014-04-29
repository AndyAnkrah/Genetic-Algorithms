import java.util.ArrayList;

public class City
{
    private String reference; // Name of city
    private double xcoord; // City's x coordinate on GUI
    private double ycoord; // City's y coordinate on GUI
    private ArrayList<City> unblockedPaths; // List of neighbours to this city
    private boolean isObstacle; // True if this city is an obstacle vertex
    
    // City constructor method (parameters: name, x coordinate, y coordinate, if it's an obstacle vertex)
    public City(String name, double x, double y, boolean obstacle){
        reference = name;
        xcoord = x;
        ycoord = y;
        isObstacle = obstacle;
        unblockedPaths = new ArrayList<City>();
    }
    
    // Get name
    public String getReference(){
        return reference;
    }
    
    // Returns true if it's an obstacle vertex
    public boolean isObstacle(){
        return isObstacle;
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
    
    // Get distance to the specified city (Euclidean)
    public double getDistanceTo(City c){
        double xdifference = Math.abs(c.getX() - xcoord);
        double ydifference = Math.abs(c.getY() - ycoord);
        double zsquared = (xdifference*xdifference) + (ydifference*ydifference);
        return (double)Math.sqrt((double)zsquared);
    }
    
    // Returns true if the arc from this city to the parameter city hits any obstacles
    public boolean intersectsObstacle(City c){
        if(AllObstacles.hitsAnyObstacle(this, c)){
            return true;
        }
        return false;
    }
    
    // Get feasible options from this city
    public ArrayList<City> getUnblockedPaths(){
        // If unblocked paths are already calculated, return them
        if(unblockedPaths.size() > 0){
            return unblockedPaths;
        } else{
            // Otherwise check if the arc from this city to each other city is blocked
            for(City c : AllCities.getCities()){
                if(this != c){
                    if(!intersectsObstacle(c)){
                        unblockedPaths.add(c);
                    }
                }
            }
        }
        return unblockedPaths;
    }
    
    // Printed output if the toString() method called
    public String toString(){
        return reference;
    }
    
}
