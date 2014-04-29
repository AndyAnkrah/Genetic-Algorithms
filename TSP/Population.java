import java.util.ArrayList;

public class Population
{
    private int populationSize; // stores the fixed size for this population
    private Tour[] allTours; // array to store all tours (individuals) in the population
    private int generationNumber; // generation counter
    
    // Population constructor method
    public Population(int popSize, boolean newPop){
        generationNumber = 0;
        allTours = new Tour[popSize];
        populationSize = popSize;
        // Initialise population if not done already
        if(newPop){
            for(int i=0; i<popSize; i++){
                setTour(i, new Tour(true));
            }
        }
    }
    
    // Get all tours
    public Tour[] getAllTours(){
        return allTours;
    }  
    
    // Get population size
    public int getSize(){
        return populationSize;
    }
    
    // Set tour in index of allTours array
    public void setTour(int index, Tour t){
        allTours[index] = t;
    }
    
    // Get tour in specified index of array
    public Tour getTour(int index){
        return allTours[index];
    }
    
    // Increment generation counter
    public void incrementGeneration(){
        generationNumber++;
    }
    
    // Get generation number
    public int getGeneration(){
        return generationNumber;
    }
    
    // Get fittest member of population
    public Tour getFittest(){
        double maxFitness = 0.0;
        Tour t = new Tour(false);
        for(int i=0; i<populationSize; i++){
            double fitness = allTours[i].getFitness();
            if(fitness > maxFitness){
                maxFitness = fitness;
                t = allTours[i];
            }
        }
        return t;
    }
    
    // Get the x fittest members of population
    public ArrayList<Tour> getXFittest(int howMany){
        ArrayList<Tour> xFittest = new ArrayList<Tour>();
        ArrayList<Tour> clone = new ArrayList<Tour>();
        for(int i=0; i<populationSize; i++){
            clone.add(getTour(i));
        }
        for(int j=0; j<howMany; j++){
            double maxFitness = 0.0;
            Tour t = new Tour(false);
            for(int i=0; i<clone.size(); i++){
                double fitness = clone.get(i).getFitness();
                if(fitness > maxFitness){
                    maxFitness = fitness;
                    t = clone.get(i);
                }
            }
            xFittest.add(t);
            clone.remove(t);
        }
        return xFittest;
    }
    
    // Printed output when toString() method is called
    public String toString(){
        return "Generation: " + generationNumber + "\nFittest Individual: " + getFittest().toString() + "\nDistance: " + getFittest().getTourDistance();
    }
}
