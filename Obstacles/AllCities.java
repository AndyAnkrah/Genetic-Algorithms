import java.util.ArrayList;

public class AllCities
{
    private static ArrayList<City> allCities = new ArrayList<City>(); // Stores an arraylist of all cities in the graph (including obstacle vertices)
    private static ArrayList<City> allPoints = new ArrayList<City>(); // Stores an arraylist of the cities in the graph which are NOT obstacle vertices
    
    // Add cities (can change these to whichever points are desired)
    public static void addCities(){
        
        // Obstacles Cities:
        addCity(new City("A", 20, 20, false));
        addCity(new City("B", 180, 180, false));
        addCity(new City("C", 120, 60, false));
        addCity(new City("D", 5, 160, false));
        addCity(new City("E", 100, 175, false));
        addCity(new City("F", 110, 110, false));
        addCity(new City("G", 190, 10, false));
        addCity(new City("H", 70, 20, false));
        
    }
    
    // Add city to arraylists
    public static void addCity(City c){
        allCities.add(c);
        if(!c.isObstacle()){
            allPoints.add(c);
        }
    }
    
    // Set specified city to the specified index in the list
    public static void setCity(int index, City c){
        allCities.set(index, c);
    }
    
    // Get list of all cities
    public static ArrayList<City> getCities(){
        return allCities;
    }
    
    // Get list of all non-obstacle cities
    public static ArrayList<City> getPoints(){
        return allPoints;
    }
    
    // Get max x coordinate of all of the cities (for scaling on GUI)
    public static double getMaxX(){
        double max = 0;
        for(City c : allCities){
            if(c.getX() > max){
                max = c.getX();
            }
        }
        return max;
    }
    
    // Get max y coordinate of all of the cities (for scaling on GUI)
    public static double getMaxY(){
        double max = 0;
        for(City c : allCities){
            if(c.getY() > max){
                max = c.getY();
            }
        }
        return max;
    }
    
    // Get min x coordinate of all of the cities (for scaling on GUI)
    public static double getMinX(){
        double min = 100000;
        for(City c : allCities){
            if(c.getX() < min){
                min = c.getX();
            }
        }
        return min;
    }
    
    // Get min y coordinate of all of the cities (for scaling on GUI)
    public static double getMinY(){
        double min = 100000;
        for(City c : allCities){
            if(c.getY() < min){
                min = c.getY();
            }
        }
        return min;
    }
    
    // Returns the total number of cities in the list (determines the number of nodes in the graph for a given problem)
    public static int getTotalCities(){
        return allCities.size();
    }
}
