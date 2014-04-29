import java.util.ArrayList;
import java.util.Arrays;

public class Algorithm
{
    private static int elitismOffset; // Array starting index offset caused by preserving members by elitism
    private static Tour currentFittest; // Stores current fittest member of evolving population for comparison
    private static double[] cumulativeFitness = null; // Array of cumulative fitness values for members of the population
    
    // Evolve a population once (evaluation, selection, crossover, mutation)
    public static Population evolve(Population p){
        Population evolvedPop = p;
        currentFittest = evolvedPop.getFittest();
        
        if(RunGA.elitism){
            // Preserve fittest x individuals in first indeces (elitism)
            elitismOffset = (int)Math.round(RunGA.elitismProportion*p.getSize());
            ArrayList<Tour> preserved = p.getXFittest(elitismOffset);
            for(int i=0; i<preserved.size(); i++){
                evolvedPop.setTour(i, preserved.get(i));
            }
        } else{
            elitismOffset = 0;
        }
        for(int i=elitismOffset; i<p.getSize(); i++){
            // Evaluation and Selection
            Tour parent1 = select(p);
            Tour parent2 = select(p);
            // Crossover
            Tour[] children = cross(parent1, parent2);
            for(int j=0; j<children.length; j++){
                evolvedPop.setTour(i, children[0]);
                // Adds both offspring if two are produced, otherwise adds the sole offspring
                if(children.length == 2){
                    i++;
                    if(i < evolvedPop.getSize()){
                        evolvedPop.setTour(i, children[1]);
                    } else{
                        break;
                    }
                }
            }
        }
        // Mutation
        mutation(evolvedPop);
        // Increment generation count
        evolvedPop.incrementGeneration();
        cumulativeFitness = null;
        return evolvedPop;
    }
    
    // Selection methods
    public static Tour select(Population p){
        if(RunGA.selectionMethod == 0){
            return randomSelection(p);
        } else if(RunGA.selectionMethod == 1){
            return generateTournament(p);
        } else if(RunGA.selectionMethod == 2){
            return rouletteWheel(p);
        } 
        return null;
    }
    
    // Crossover methods (TSP)
    public static Tour[] cross(Tour p1, Tour p2){
        if(RunGA.crossoverMethod == 0){
            Tour[] temp = new Tour[]{randomCrossover(p1, p2)};
            return temp;
        } else if(RunGA.crossoverMethod == 1){
            Tour[] temp = new Tour[]{crossover(p1, p2)};
            return temp;
        } else if(RunGA.crossoverMethod == 2){
            return partiallyMatchedCrossover(p1, p2);
        }
        return null;
    }
    
    // Mutation methods (TSP)
    public static void mutation(Population p){
        if(RunGA.mutationMethod == 0){
            randomMutate(p);
        } else if(RunGA.mutationMethod == 1){
            mutate(p);
        } else if(RunGA.mutationMethod == 2){
            mutate2(p);
        }
    }
    
    // Random selection
    public static Tour randomSelection(Population p){
        int index = (int)Math.round(Math.random()*(p.getSize()-1));
        return p.getTour(index);
    }
    
    // Deterministic tournament selection
    public static Tour generateTournament(Population p){
        //int tournamentSize = (int)Math.round(RunGA.tournamentProportion*p.getSize());
        int tournamentSize = 2; // Found to be the most effective tournament size by experimental analysis
        Population tournament = new Population(tournamentSize, true);
        if(tournamentSize == 0){
            return randomSelection(p);
        }
        for(int i=0; i<tournamentSize; i++){
            int index = (int)Math.round(Math.random()*(p.getAllTours().length-1));
            tournament.setTour(i, p.getTour(index));
        }
        return tournament.getFittest();
    }
    
    // Roulette wheel selection
    public static Tour rouletteWheel(Population p){
        // Stagger fitness values if not done already
        if(cumulativeFitness == null){
            // Calculate cumulative fitnesses of population members
            cumulativeFitness = new double[p.getSize()];
            double prevFitness = 0.0;
            for(int i=0; i<p.getSize(); i++){
                cumulativeFitness[i] = prevFitness + p.getTour(i).getFitness();
                prevFitness = cumulativeFitness[i];
            }
        }
        // Randomly generate a number up to the total fitness
        double spin = Math.random()*cumulativeFitness[cumulativeFitness.length-1];
        // Find the cumulatively selected individual
        int selectedIndex = Arrays.binarySearch(cumulativeFitness, spin);
        // Convert negative insertion point to valid index
        if(selectedIndex < 0){
            selectedIndex = Math.abs(selectedIndex + 1);
        }
        Tour selected = p.getTour(selectedIndex);
        return selected;
    }
    
    // Random crossover
    public static Tour randomCrossover(Tour parent1, Tour parent2){
        Tour child = new Tour(true);
        if(Math.random() > RunGA.crossoverRate){
            if(Math.random() <= 0.5){
                return parent1;
            } else{
                return parent2;
            }
        }
        return child;
    }
    
    // Segment crossover
    public static Tour crossover(Tour parent1, Tour parent2){
        Tour child = new Tour(true);
        if(parent1 == parent2){
            return parent1;
        }
        if(Math.random() > RunGA.crossoverRate){
            if(Math.random() <= 0.5){
                return parent1;
            } else{
                return parent2;
            }
        } else{
            int iterateOffset = 0;
            // Fill arraylist with null entries
            child.getTour().remove(child.getLength()-1);
            iterateOffset = 1;
            child.setToursNull();
            int start = (int)Math.round((Math.random()*(parent1.getLength()-1-iterateOffset)));
            int end = (int)Math.round((Math.random()*(parent1.getLength()-1-iterateOffset)));
            // Ensure that start index is less than end index
            if(start > end){
                int temp = end;
                end = start;
                start = temp;
            }
            // Genes from parent 1
            for(int i=start; i<=end; i++){
                City parent1city = parent1.getCity(i);
                child.setCity(i, parent1city);
            }
            // Genes from parent 2
            for(int i=0; i<parent2.getLength()-iterateOffset; i++){
                // If city not already being visited
                if(!child.containsCity(parent2.getCity(i))){
                    // Find free index to place city into
                    for(int j=0; j<child.getLength(); j++){
                        if(child.getCity(j) == null){
                            child.setCity(j, parent2.getCity(i));
                            break;
                        }
                    }
                }
            }
            // Reform cycle
            child.getTour().add(child.getTour().get(0));
        }
        return child;
    }
    
    // Partially Matched Crossover (PMX)
    public static Tour[] partiallyMatchedCrossover(Tour parent1, Tour parent2){
        Tour child1 = new Tour(false);
        child1.getTour().addAll(parent1.getTour());
        // Remove the end city in the cycle
        child1.getTour().remove(child1.getLength()-1);
        // Start with the parent genotypes
        Tour child2 = new Tour(false);
        child2.getTour().addAll(parent2.getTour());
        child2.getTour().remove(child2.getLength()-1);
        // Return the parents unchanged as offspring if crossover rate not met or clones
        if((parent1 == parent2) || Math.random() > RunGA.crossoverRate){
            return new Tour[]{parent1, parent2};
        } else{
            int start = (int)(Math.random()*child1.getLength());
            int end = (int)(Math.random()*child1.getLength());
            if(start > end){
                int temp = start;
                start = end;
                end = temp;
            }
            // Swap the cities between the start and end indeces to the corresponding indeces in other parent
            for(int i=start; i<=end; i++){
                // Find index of the each city to be swapped in the genotype
                int swapIndex1 = child1.getTour().indexOf(parent2.getTour().get(i));
                int swapIndex2 = child2.getTour().indexOf(parent1.getTour().get(i));
                City temp1 = child1.getTour().get(i);
                City temp2 = child2.getTour().get(i);
                child1.getTour().set(i, parent2.getTour().get(i));
                child2.getTour().set(i, parent1.getTour().get(i));
                child1.getTour().set(swapIndex1, temp1);
                child2.getTour().set(swapIndex2, temp2);
            }
            // Reform cycle
            child1.getTour().add(child1.getTour().get(0));
            child2.getTour().add(child2.getTour().get(0));
        }
        Tour[] children = new Tour[]{child1, child2};
        return children;
    }
    
    // Random mutation
    public static Population randomMutate(Population p){
        for(int j=elitismOffset; j<p.getSize(); j++){
            if(Math.random() < RunGA.mutationRate){
                Tour randomTour = new Tour(true);
                p.getTour(j).getTour().clear();
                p.getTour(j).getTour().addAll(randomTour.getTour());
            }
        }
        return p;
    }
    
    // Swapping mutation
    public static Population mutate(Population p){
        for(int j=elitismOffset; j<p.getSize(); j++){
            Tour t = p.getTour(j);
            // Temporarily remove the end city of the cycle
            t.getTour().remove(t.getLength()-1);
            for(int i=0; i<t.getLength(); i++){
                if(Math.random() < RunGA.mutationRate){
                    // Swap the city in this index with the randomly generated position
                    City swap1 = t.getCity(i);
                    int position = (int) Math.round(Math.random()*(t.getLength()-1));
                    City swap2 = t.getCity(position);
                    if(position != i){
                        t.setCity(position, swap1);
                        t.setCity(i, swap2);
                    }
                }
            }
            // Reform cycle
            t.getTour().add(t.getCity(0));
        }
        return p;
    }
    
    // Insertion mutation
    public static Population mutate2(Population p){
        for(int j=elitismOffset; j<p.getSize(); j++){
            Tour t = p.getTour(j);
            for(int i=0; i<t.getLength(); i++){
                if(Math.random() < RunGA.mutationRate){
                    City swap1 = t.getCity(i);
                    int position = (int) Math.round(Math.random()*(t.getLength()-1));
                    // Ensure that the start city remains the same if specified
                    if(i == 0 || i == t.getLength()-1){
                        continue;
                    }
                    position = (int) Math.round(Math.random()*(t.getLength()-2)) + 1;
                    if(position != i){
                        // Add the cities to a new tour arraylist
                        ArrayList<City> newTour = new ArrayList<City>();
                        for(int k=0; k<position; k++){
                            newTour.add(t.getCity(k));
                        }
                        newTour.add(swap1);
                        for(int k=position; k<t.getLength(); k++){
                            newTour.add(t.getCity(k));
                        }
                        // Remove the repeated city after insertion
                        if(i > position){
                            newTour.remove(i+1);
                        } else{
                            newTour.remove(i);
                        }
                        t.setTour(newTour);
                        // Reform cycle
                        if(i != t.getLength()-1){
                            t.setCity(t.getLength()-1, t.getCity(0));
                        } else{
                            t.setCity(0, t.getCity(t.getLength()-1));
                        }
                    }
                }
            }
        }
        return p;
    }
}
