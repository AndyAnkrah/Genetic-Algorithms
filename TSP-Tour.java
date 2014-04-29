import java.util.*;

public class Tour
{
    private ArrayList<City> tour; // arraylist to store the ordered cities in a tour (individual)
    private double distance; // variable to store the tour distance
    private int fitness; // variable to store the individual's fitness
    
    public Tour(boolean generateNew){
        fitness = 0;
        distance = 0;
        tour = new ArrayList<City>();
        // Generate a new genotype if true
        if(generateNew){
            generateTour(true);
        }
    }
    
    // Generate a new tour (with intelligent shortcuts if paramter true)
    public void generateTour(boolean intelligent){
        // If running normal TSP, randomise all cities into a tour
        tour.clear();
        for(int i=0; i<AllCities.getTotalCities(); i++){
            tour.add(AllCities.getCities().get(i));
        }
        randomiseTour();
        if(tour.size() > 0){
            tour.add(tour.get(0));
        }
        getTourDistance();
    }
    
    // Set all tours as null (used at start of crossover)
    public void setToursNull(){
        for(int i=0; i<tour.size(); i++){
            setCity(i, null);
        }
    }
    
    // Randomise tour
    public void randomiseTour(){
        Collections.shuffle(tour);
    }
    
    // Set city in specified index of arraylist
    public void setCity(int index, City c){
        tour.set(index, c);
    }
    
    // Get city in specified index of arraylist
    public City getCity(int index){
        return tour.get(index);
    }
    
    // Set tour to a new arraylist of cities
    public void setTour(ArrayList<City> cities){
        tour = cities;
    }
    
    // Get tour
    public ArrayList<City> getTour(){
        return tour;
    }
    
    // Get tour distance
    public double getTourDistance(){
        distance = 0.0;
        for(int i=0; i<tour.size()-1; i++){
            distance += tour.get(i).getDistanceTo(tour.get(i+1));
        }
        return distance;
    }
    
    // Get individual's fitness
    public double getFitness(){
        return Fitness.getFitness(this);
    }
    
    // Get length of the tour
    public int getLength(){
        return tour.size();
    }
    
    // Returns true if tour contains the specified city (used in crossover)
    public boolean containsCity(City city){
        boolean contains = false;
        for(City c : tour){
            if(c == city){
                contains = true;
            }
        }
        return contains;
    }
    
    // Printed output if toString() method is called
    public String toString(){
        String s = "";
        for(int i=0; i<tour.size(); i++){
            if(tour.get(i) != null){
                s += tour.get(i).getReference();
                if(i < tour.size()-1){
                    s += "-";
                }
            } else{
                s += " _ ";
            }
        }
        return s;
    }
}
