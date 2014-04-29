import java.util.ArrayList;

public class City
{
    private String reference; // Name of city
    private double xcoord; // City's x coordinate on GUI
    private double ycoord; // City's y coordinate on GUI
    
    // City constructor method (parameters: name, x coordinate, y coordinate)
    public City(String name, double x, double y){
        reference = name;
        xcoord = x;
        ycoord = y;
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
    
    // Get distance to the specified city (Euclidean)
    public double getDistanceTo(City c){
        double xdifference = Math.abs(c.getX() - xcoord);
        double ydifference = Math.abs(c.getY() - ycoord);
        double zsquared = (xdifference*xdifference) + (ydifference*ydifference);
        return (double)Math.sqrt((double)zsquared);
    }
    
    // Printed output if the toString() method called
    public String toString(){
        return reference;
    }
    
}
