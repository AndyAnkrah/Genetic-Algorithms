import java.util.ArrayList;

public class AllCities
{
    private static ArrayList<City> allCities = new ArrayList<City>(); // Stores an arraylist of all cities in the graph (including obstacle vertices)
    
    // Add cities (can change these to whichever points are desired)
    public static void addCities(){
        
        // Example Map Cities (coordinates don't matter since there is no visual output, just the edge weights below matter):
        addCity(new City("Collingwood", 180.0, 170.0));
        addCity(new City("Grey", 190.0, 170.0));
        addCity(new City("Hatfield", 50.0, 50.0));
        addCity(new City("Butler", 190.0, 190.0));
        addCity(new City("Aidans", 160.0, 150.0));
        addCity(new City("Chads", 20.0, 40.0));
        addCity(new City("Cuths", 45.0, 80.0));
        addCity(new City("Hild Bede", 190.0, 10.0));
        addCity(new City("Johns", 40.0, 60.0));
        addCity(new City("Marys", 120.0, 150.0));
        addCity(new City("Trevs", 70.0, 130.0));
        addCity(new City("Castle", 0.0, 30.0));
        addCity(new City("Van Mildert", 160.0, 170.0));
        addCity(new City("Ustinov", 200.0, 200.0));
        
        // Example graph edges set up (must specify all edges in the graph)         
        // Collingwood
        allCities.get(0).setUnblockedPaths(new String[]{"Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(0).setDistances(new double[]{0.5, 1.0, 0.4, 0.4, 0.9, 1.1, 1.5, 1.0, 0.4, 0.2, 0.7, 0.3, 0.3});
        // Grey
        allCities.get(1).setUnblockedPaths(new String[]{"Collingwood", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(1).setDistances(new double[]{0.5, 1.0, 0.9, 0.9, 1.0, 1.2, 1.6, 1.1, 0.8, 0.7, 0.4, 0.7, 0.8});
        // Hatfield
        allCities.get(2).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(2).setDistances(new double[]{1.0, 1.0, 1.2, 1.1, 0.06, 0.3, 0.8, 0.2, 0.9, 1.0, 0.6, 1.1, 1.1});
        // Butler
        allCities.get(3).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(3).setDistances(new double[]{0.4, 0.9, 1.2, 0.6, 1.2, 1.4, 1.8, 1.3, 0.5, 0.4, 0.9, 0.4, 0.07});
        // Aidans
        allCities.get(4).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(4).setDistances(new double[]{0.4, 0.9, 1.1, 0.6, 1.1, 1.3, 1.7, 1.2, 0.2, 0.2, 0.8, 0.1, 0.5});
        // Chads
        allCities.get(5).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(5).setDistances(new double[]{0.9, 1.0, 0.06, 1.2, 1.1, 0.2, 0.9, 0.1, 0.9, 1.0, 0.6, 1.0, 1.1});
        // Cuths
        allCities.get(6).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(6).setDistances(new double[]{1.1, 1.2, 0.3, 1.4, 1.3, 0.2, 1.1, 0.1, 1.1, 1.2, 0.8, 1.2, 1.3});
        // Hild Bede
        allCities.get(7).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Johns", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(7).setDistances(new double[]{1.5, 1.6, 0.8, 1.8, 1.7, 0.9, 1.1, 1.0, 1.5, 1.6, 1.2, 1.6, 1.7});
        // Johns
        allCities.get(8).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Marys", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(8).setDistances(new double[]{1.0, 1.1, 0.2, 1.3, 1.2, 0.1, 0.1, 1.0, 1.0, 1.1, 0.7, 1.1, 1.2});
        // Marys
        allCities.get(9).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Trevs", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(9).setDistances(new double[]{0.4, 0.8, 0.9, 0.5, 0.2, 0.9, 1.1, 1.5, 1.0, 0.2, 0.6, 0.2, 0.5});
        // Trevs
        allCities.get(10).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Castle", "Van Mildert", "Ustinov"});
        allCities.get(10).setDistances(new double[]{0.2, 0.7, 1.0, 0.4, 0.2, 1.0, 1.2, 1.6, 1.1, 0.2, 0.7, 0.07, 0.3});
        // Castle
        allCities.get(11).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Van Mildert", "Ustinov"});
        allCities.get(11).setDistances(new double[]{0.7, 0.4, 0.6, 0.9, 0.8, 0.6, 0.8, 1.2, 0.7, 0.6, 0.7, 1.2, 1.3});
        // Van Mildert
        allCities.get(12).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Ustinov"});
        allCities.get(12).setDistances(new double[]{0.3, 0.7, 1.1, 0.4, 0.1, 1.0, 1.2, 1.6, 1.1, 0.2, 0.07, 1.2, 0.3});
        // Ustinov
        allCities.get(13).setUnblockedPaths(new String[]{"Collingwood", "Grey", "Hatfield", "Butler", "Aidans", "Chads", "Cuths", "Hild Bede", "Johns", "Marys", "Trevs", "Castle", "Van Mildert"});
        allCities.get(13).setDistances(new double[]{0.3, 0.8, 1.1, 0.07, 0.5, 1.1, 1.3, 1.7, 1.2, 0.5, 0.3, 1.3, 0.3});
    }
    
    // Add city to arraylists
    public static void addCity(City c){
        allCities.add(c);
    }
    
    // Set specified city to the specified index in the list
    public static void setCity(int index, City c){
        allCities.set(index, c);
    }
    
    // Get list of all cities
    public static ArrayList<City> getCities(){
        return allCities;
    }
    
    // Returns the total number of cities in the list (determines the number of nodes in the graph for a given problem)
    public static int getTotalCities(){
        return allCities.size();
    }
}
