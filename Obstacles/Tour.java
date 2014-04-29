import java.util.*;

public class Tour
{
    private ArrayList<City> tour; // arraylist to store the ordered cities in a tour (individual)
    private double distance; // variable to store the tour distance
    private int fitness; // variable to store the individual's fitness
    private ArrayList<City[]> arcs; // arraylist to store the arcs (edges in the graph) used by the tour
    
    public Tour(boolean generateNew){
        fitness = 0;
        distance = 0;
        tour = new ArrayList<City>();
        arcs = new ArrayList<City[]>();
        // Generate a new genotype if true
        if(generateNew){
            generateTour(true);
        }
    }
    
    // Generate a new tour (with intelligent shortcuts if paramter true)
    public void generateTour(boolean intelligent){
        if(RunGA.getUnorderedViaPoints().size() == 0 || !RunGA.isTSP()){
            // If there are edges from the start and destination city
            if(RunGA.getEndCity().getUnblockedPaths().size() > 0 && RunGA.getStartCity().getUnblockedPaths().size() > 0){
                boolean continueWhile = true;
                while(continueWhile){
                    continueWhile = false;
                    tour.clear();
                    tour.add(RunGA.getStartCity());
                    // Add feasible cities to the end of the path until destination is reached
                    while(!tour.contains(RunGA.getEndCity())){
                        if(optionsFromThisCity(tour.get(tour.size()-1)).size() > 0){
                            if(intelligent){
                                // If all via points have been visited, and there is a direct link to the end node, choose that
                                if(tour.get(tour.size()-1).getUnblockedPaths().contains(RunGA.getEndCity())){
                                    tour.add(RunGA.getEndCity());
                                    break;
                                }
                            }
                            // Add a random feasible city to the end of the path
                            int random = (int)Math.round(Math.random()*(optionsFromThisCity(tour.get(tour.size()-1)).size()-1));
                            tour.add(optionsFromThisCity(tour.get(tour.size()-1)).get(random));
                        } else{
                            // Obstacles in way, or no untraversed feasible arcs ==> cannot reach destination
                            continueWhile = true;
                            break;
                        }
                    }
                }
            } else{
                // Obstacles in way, or no untraversed feasible arcs ==> cannot reach destination
                System.out.println("This route is impossible, please try again (happens sometimes with randomness!)");
                System.exit(1);
            }
        } else{
            // TSP for unordered via points: Create random order of via points to be visited
            ArrayList<City> via = new ArrayList<City>();
            via.addAll(RunGA.getUnorderedViaPoints());
            if(RunGA.definedStartCity()){
                via.remove(RunGA.getStartCity());
                via.remove(RunGA.getEndCity());
            }
            tour.clear();
            for(int i=0; i<via.size(); i++){
                tour.add(via.get(i));
            }
            randomiseTour();
            if(RunGA.definedStartCity()){
                tour.add(0, RunGA.getStartCity());
                tour.add(RunGA.getEndCity());
            }
        }
        getArcs();
    }
    
    // Get feasible options from a city (edges to neighbours on graph)
    public ArrayList<City> optionsFromThisCity(City c){
        ArrayList<City> feasible = new ArrayList<City>();
        ArrayList<City> options = c.getUnblockedPaths();
        for(City o : options){
            if(!tour.contains(o)){
                feasible.add(o);
            }
        }
        return feasible;
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
    
    // Get arcs used by this tour
    public ArrayList<City[]> getArcs(){
        // Clear existing arcs if they exist
        if(arcs.size() > 0){
            arcs.clear();
        }
        // Add arcs between each pair of consecutive cities
        for(int i=1; i<tour.size()-2; i++){
            City[] arc = new City[2];
            arc[0] = tour.get(i);
            arc[1] = tour.get(i+1);
            arcs.add(arc);
        }
        return arcs;
    }
    
    // Get shared arcs with a specified tour
    public ArrayList<City> sharedArcsWith(Tour t){
        ArrayList<City[]> shared = new ArrayList<City[]>();
        ArrayList<City[]> tArcs = t.getArcs();
        ArrayList<City[]> uncombinedArcs = new ArrayList<City[]>();
        for(City[] c : tArcs){
            if(containsArc(c)){
                shared.add(c);
            }
        }
        ArrayList<City> ordered = new ArrayList<City>();
        for(City[] c : shared){
            // Check if one of the cities is already in the ordered list first
            if(ordered.contains(c[0]) && !ordered.contains(c[1])){
                if(ordered.indexOf(c[0]) == 0){
                    // If connecting city is at start, prepend
                    ordered.add(0, c[1]);
                } else if(ordered.indexOf(c[0]) == ordered.size()-1){
                    // If connecting city is at end, append
                    ordered.add(c[1]);
                }
            } else if(!ordered.contains(c[0]) && ordered.contains(c[1])){
                if(ordered.indexOf(c[1]) == 0){
                    // If connecting city is at start, prepend
                    ordered.add(0, c[0]);
                } else if(ordered.indexOf(c[1]) == ordered.size()-1){
                    // If connecting city is at end, append
                    ordered.add(c[0]);
                }
            } else if(!ordered.contains(c[0]) && !ordered.contains(c[1])){
                if(ordered.size() == 0){
                    ordered.add(c[0]);
                    ordered.add(c[1]);
                } else{
                    // Need to consider if joining it is feasible (unblocked arc)
                    if(c[0].getUnblockedPaths().contains(ordered.get(ordered.size()-1))){
                        // If first city's path to end of ordered is clear, append
                        ordered.add(c[0]);
                        ordered.add(c[1]);
                    } else if(c[1].getUnblockedPaths().contains(ordered.get(ordered.size()-1))){
                        // If second city's path to end of ordered is clear, append
                        ordered.add(c[1]);
                        ordered.add(c[0]);
                    } else if(c[0].getUnblockedPaths().contains(ordered.get(0))){
                        // If first city's path to start of ordered is clear, prepend
                        ordered.add(0, c[0]);
                        ordered.add(0, c[1]);
                    } else if(c[1].getUnblockedPaths().contains(ordered.get(0))){
                        // If second city's path to start of ordered is clear, prepend
                        ordered.add(0, c[1]);
                        ordered.add(0, c[0]);
                    } else{
                        // Cannot be combined so store in uncombined arcs arraylist
                        uncombinedArcs.add(c);
                    }
                }
            }
            // Try to add any uncombined Arcs
            ArrayList<City[]> removeArcs = new ArrayList<City[]>();
            for(City[] arc : uncombinedArcs){
                // Check if one of the cities is already in the ordered list first
                if(ordered.contains(arc[0]) && !ordered.contains(arc[1])){
                    if(ordered.indexOf(arc[0]) == 0){
                        // If connecting city is at start, prepend
                        ordered.add(0, arc[1]);
                        removeArcs.add(arc);
                    } else if(ordered.indexOf(arc[0]) == ordered.size()-1){
                        // If connecting city is at end, append
                        ordered.add(arc[1]);
                        removeArcs.add(arc);
                    }
                } else if(!ordered.contains(arc[0]) && ordered.contains(arc[1])){
                    if(ordered.indexOf(arc[1]) == 0){
                        // If connecting city is at start, prepend
                        ordered.add(0, arc[0]);
                        removeArcs.add(arc);
                    } else if(ordered.indexOf(arc[1]) == ordered.size()-1){
                        // If connecting city is at end, append
                        ordered.add(arc[0]);
                        removeArcs.add(arc);
                    }
                } else if(!ordered.contains(arc[0]) && !ordered.contains(arc[1])){
                    // Need to consider if joining it is feasible (unblocked arc)
                    if(arc[0].getUnblockedPaths().contains(ordered.get(ordered.size()-1))){
                        // If first city's path to end of ordered is clear, append
                        ordered.add(arc[0]);
                        ordered.add(arc[1]);
                        removeArcs.add(arc);
                    } else if(arc[1].getUnblockedPaths().contains(ordered.get(ordered.size()-1))){
                        // If second city's path to end of ordered is clear, append
                        ordered.add(arc[1]);
                        ordered.add(arc[0]);
                        removeArcs.add(arc);
                    } else if(arc[0].getUnblockedPaths().contains(ordered.get(0))){
                        // If first city's path to start of ordered is clear, prepend
                        ordered.add(0, arc[0]);
                        ordered.add(0, arc[1]);
                        removeArcs.add(arc);
                    } else if(arc[1].getUnblockedPaths().contains(ordered.get(0))){
                        // If second city's path to start of ordered is clear, prepend
                        ordered.add(0, arc[1]);
                        ordered.add(0, arc[0]);
                        removeArcs.add(arc);
                    } else{
                        // Still uncombined so don't remove
                    }
                }
            }
            for(City[] arc : removeArcs){
                uncombinedArcs.remove(arc);
            }
        }
        return ordered;
    }
    
    // Returns true if tour contains the specified arc
    public boolean containsArc(City[] arc){
        for(int i=0; i<arcs.size(); i++){
            // If 1st city contained in arc
            if(arcs.get(i)[0] == arc[0] || arcs.get(i)[0] == arc[1]){
                // And 2nd city contained in arc
                if(arcs.get(i)[1] == arc[0] || arcs.get(i)[1] == arc[1]){
                    return true;
                }
            }
        }
        return false;
    }
    
    // Get tour distance
    public double getTourDistance(){
        distance = 0.0;
        if(RunGA.isTSP()){
            for(int i=0; i<tour.size()-1; i++){
                City from = tour.get(i);
                City to = tour.get(i+1);
                String fromTo1 = from.getReference() + "" + to.getReference();
                String fromTo2 = to.getReference() + "" + from.getReference();
                if(RunGA.getShortestPaths().containsKey(fromTo1)){
                    distance += RunGA.getShortestPaths().get(fromTo1).getTourDistance();
                } else if(RunGA.getShortestPaths().containsKey(fromTo2)){
                    distance += RunGA.getShortestPaths().get(fromTo2).getTourDistance();
                } else{
                    System.out.println("error");
                    System.exit(0);
                }
            }
        } else{
            for(int i=0; i<tour.size()-1; i++){
                distance += tour.get(i).getDistanceTo(tour.get(i+1));
            }
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
