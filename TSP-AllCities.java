import java.util.ArrayList;

public class AllCities
{
    private static ArrayList<City> allCities = new ArrayList<City>(); // Stores an arraylist of all cities in the graph (including obstacle vertices)
    
    // Add cities (can change these to whichever points are desired)
    public static void addCities(){
        // Example fixed Cities:
        addCity(new City("A", 103.0, 0.0));
        addCity(new City("B", 5.0, 2.0));
        addCity(new City("C", 47.0, 20.0));
        addCity(new City("D", 11.0, 59.0));
        addCity(new City("E", 78.0, 55.0));
        addCity(new City("F", 149.0, 49.0));
        addCity(new City("G", 180.0, 5.0));
        addCity(new City("H", 194.0, 71.0));
        addCity(new City("I", 115.0, 98.0));
        addCity(new City("J", 41.0, 94.0));
        addCity(new City("K", 17.0, 119.0));
        addCity(new City("L", 78.0, 126.0));
        addCity(new City("M", 0.0, 179.0));
        addCity(new City("N", 45.0, 161.0));
        addCity(new City("O", 42.0, 200.0));
        addCity(new City("P", 102.0, 196.0));
        addCity(new City("Q", 136.0, 162.0));
        addCity(new City("R", 163.0, 113.0));
        addCity(new City("S", 200.0, 137.0));
        addCity(new City("T", 189.0, 194.0));
        // Example adding 20 random cities
        addRandomCities(20);
    }
    
    // Add city to arraylists
    public static void addCity(City c){
        allCities.add(c);
    }
    
    // Add the specified number of random cities to the list
    public static void addRandomCities(int howMany){
        for(int i=0; i<howMany; i++){
            int ranX = (int)Math.round(Math.random()*200.0);
            int ranY = (int)Math.round(Math.random()*200.0);
            addCity(new City("" + i, ranX, ranY));
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
    
    // Returns the total number of cities in the list (determines the number of nodes in the graph for a given problem)
    public static int getTotalCities(){
        return allCities.size();
    }
}
