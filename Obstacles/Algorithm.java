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
        
        if(RunGA.isTSP()){
            // TSP crossover
            for(int i=elitismOffset; i<p.getSize(); i++){
                // Evaluation and Selection
                Tour parent1 = select(p);
                Tour parent2 = select(p);
                // Crossover
                Tour[] children = cross(parent1, parent2);
                for(int j=0; j<children.length; j++){
                    evolvedPop.setTour(i, children[0]);
                    if(children.length == 2){
                        i++;
                        evolvedPop.setTour(i, children[1]);
                    }
                }
            }
            // Mutation
            mutation(evolvedPop);
        } else{
            // Variable length genome crossover
            for(int i=elitismOffset; i<p.getSize(); i++){
                Tour child = null;
                while(child == null){
                    // Evaluation and Selection
                    Tour parent1 = select(p);
                    Tour parent2 = select(p);
                    // Crossover
                    child = variableCrossover(parent1, parent2);
                }
                evolvedPop.setTour(i, child);
            }
            // Mutation
            variableMutate(evolvedPop, RunGA.mutationRate);
        }
        
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
        }
        return child;
    }
    
    // Partially Matched Crossover (PMX)
    public static Tour[] partiallyMatchedCrossover(Tour parent1, Tour parent2){
        Tour child1 = new Tour(false);
        child1.getTour().addAll(parent1.getTour());
        // Start with the parent genotypes
        Tour child2 = new Tour(false);
        child2.getTour().addAll(parent2.getTour());
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
        }
        Tour[] children = new Tour[]{child1, child2};
        return children;
    }
    
    // Returns true if it is feasible to combine the two specified paths into one
    public static boolean canMerge(ArrayList<City> merge1, ArrayList<City> merge2){
        if(merge1.get(merge1.size()-1).getUnblockedPaths().contains(merge2.get(0))){
            return true;
        }
        return false;
    }
    
    // Crossover which allows variable length genomes (new idea)
    public static Tour variableCrossover(Tour parent1, Tour parent2){
        Tour child = new Tour(false);
        // If parents are identical, return either parent (could use clone prevention etc. instead)
        if(parent1.toString().equals(parent2.toString())){
            for(int i=0; i<parent1.getLength(); i++){
                child.getTour().add(parent1.getCity(i));
            }
            return child;
        }
        // If start / end cities have been defined, remove them temporarily
        if(RunGA.definedStartCity()){
            ArrayList<ArrayList<City>> blocks = new ArrayList<ArrayList<City>>();
            // Block to build up from left to right
            ArrayList<City> block1 = new ArrayList<City>();
            block1.add(RunGA.getStartCity());
            // Block to store shared arcs between parents
            ArrayList<City> block2 = parent1.sharedArcsWith(parent2);
            // Block to build up from right to left
            ArrayList<City> block3 = new ArrayList<City>();
            block3.add(RunGA.getEndCity());
            // Store blocks in arraylist
            blocks.add(block1);
            blocks.add(block2);
            blocks.add(block3);
            boolean merged12 = false;
            boolean merged23 = false;
            
            // Randomly choose starting parent
            int chooseStarter = (int)Math.round(Math.random());
            Tour[] parents = new Tour[2];
            if(chooseStarter == 0){
                parents[0] = parent1;
                parents[1] = parent2;
            } else{
                parents[0] = parent2;
                parents[1] = parent1;
            }
            
            // If there are no shared arcs between the parents, alternate arcs from parent1 and parent2
            if(block2.size() == 0){
                // Marriage restriction
                return null;
            }
            // Merge blocks 1 and 2 if feasible to move between the adjacent cities
            if(canMerge(blocks.get(0), blocks.get(1))){
                blocks.get(0).addAll(blocks.get(1));
                merged12 = true;
            }
            // Merge blocks 2 and 3 if feasible to move between the adjacent cities
            if(canMerge(blocks.get(1),blocks.get(2))){
                if(merged12){
                    // If already merged blocks 1 and 2, merge product with block 3
                    blocks.get(0).addAll(blocks.get(2));
                    merged23 = true;
                } else{
                    // If not, merge block 2 with block 3
                    blocks.get(1).addAll(blocks.get(2));
                    merged23 = true;
                }
            }
            
            while(!merged12 || !merged23){
                // Merge blocks 1 and 2 if feasible to move between the adjacent cities
                if(!merged12){
                    // While new blocks contain the proposed city, continue moving through the parent
                    boolean continueWhile = true;
                    City validCity = null;
                    attempt:
                    while(continueWhile){
                        // First attempt checks potential cities from parent 1
                        continueWhile = false;
                        ArrayList<City> unchecked = new ArrayList<City>();
                        unchecked.addAll(AllCities.getCities());
                        cityloop:
                        for(City c : parents[0].getTour()){
                            if(unchecked.contains(c)){
                                unchecked.remove(c);
                                // Check if arc to append is unblocked, if not continue while loop
                                if(!blocks.get(0).get(blocks.get(0).size()-1).getUnblockedPaths().contains(c)){
                                    continueWhile = true;
                                } else{
                                    // Check if proposed city is already in one of the blocks
                                    for(int i=0; i<blocks.size(); i++){
                                        if(blocks.get(i).contains(c)){
                                            continueWhile = true;
                                        }
                                    }
                                }
                                // Valid if continueWhile is still false, so break for each loop and move on
                                if(!continueWhile){
                                    validCity = c;
                                    break attempt;
                                }
                            }
                        }
                        
                        // Second attempt checks potential cities from parent 2
                        continueWhile = false;
                        for(City c : parents[1].getTour()){
                            if(unchecked.contains(c)){
                                unchecked.remove(c);
                                // Check if arc to append is unblocked, if not continue while loop
                                if(!blocks.get(0).get(blocks.get(0).size()-1).getUnblockedPaths().contains(c)){
                                    continueWhile = true;
                                } else{
                                    for(int i=0; i<blocks.size(); i++){
                                        if(blocks.get(i).contains(c)){
                                            continueWhile = true;
                                        }
                                    }
                                }
                                // Valid if continueWhile is still false, so break for each loop and move on
                                if(!continueWhile){
                                    validCity = c;
                                    break attempt;
                                }
                            }
                        }
                        
                        // Finally check remaining unchecked cities
                        for(City c : unchecked){
                            // Check if arc to append is unblocked, if not continue while loop
                            if(!blocks.get(0).get(blocks.get(0).size()-1).getUnblockedPaths().contains(c)){
                                continueWhile = true;
                            } else{
                                for(int i=0; i<blocks.size(); i++){
                                    if(blocks.get(i).contains(c)){
                                        continueWhile = true;
                                    }
                                }
                            }
                            // Valid if continueWhile is still false, so break for each loop and move on
                            if(!continueWhile){
                                validCity = c;
                                break attempt;
                            }
                        }
                        
                        // If this line is reached, no valid candidate has been found in either parent, so return null
                        return null;
                    }
                    
                    // Add the new city to the end of the first block
                    blocks.get(0).add(validCity);
                    
                    // Now merge blocks 1 and 2 if feasible
                    if(canMerge(blocks.get(0),blocks.get(1))){
                        blocks.get(0).addAll(blocks.get(1));
                        merged12 = true;
                    }
                }
                // Merge blocks 2 and 3 if feasible to move between the adjacent cities
                if(!merged23){
                    // While new blocks contain the proposed city, continue moving through the parent
                    boolean continueWhile = true;
                    City validCity = null;
                    attempt:
                    while(continueWhile){
                        // First attempt checks potential cities from parent 1
                        continueWhile = false;
                        ArrayList<City> unchecked = new ArrayList<City>();
                        unchecked.addAll(AllCities.getCities());
                        cityloop:
                        for(City c : parents[0].getTour()){
                            if(unchecked.contains(c)){
                                unchecked.remove(c);
                                // Check if arc to append is unblocked, if not continue while loop
                                if(!blocks.get(2).get(0).getUnblockedPaths().contains(c)){
                                    continueWhile = true;
                                } else{
                                    // Check if proposed city is already in one of the blocks
                                    for(int i=0; i<blocks.size(); i++){
                                        if(blocks.get(i).contains(c)){
                                            continueWhile = true;
                                        }
                                    }
                                }
                                // Valid if continueWhile is still false, so break for each loop and move on
                                if(!continueWhile){
                                    validCity = c;
                                    break attempt;
                                }
                            }
                        }
                        
                        // Second attempt checks potential cities from parent 2
                        continueWhile = false;
                        for(City c : parents[1].getTour()){
                            if(unchecked.contains(c)){
                                unchecked.remove(c);
                                // Check if arc to append is unblocked, if not continue while loop
                                if(!blocks.get(2).get(0).getUnblockedPaths().contains(c)){
                                    continueWhile = true;
                                } else{
                                    for(int i=0; i<blocks.size(); i++){
                                        if(blocks.get(i).contains(c)){
                                            continueWhile = true;
                                        }
                                    }
                                }
                                // Valid if continueWhile is still false, so break for each loop and move on
                                if(!continueWhile){
                                    validCity = c;
                                    break attempt;
                                }
                            }
                        }
                        
                        // Finally check remaining unchecked cities
                        for(City c : unchecked){
                            // Check if arc to append is unblocked, if not continue while loop
                            if(!blocks.get(2).get(0).getUnblockedPaths().contains(c)){
                                continueWhile = true;
                            } else{
                                for(int i=0; i<blocks.size(); i++){
                                    if(blocks.get(i).contains(c)){
                                        continueWhile = true;
                                    }
                                }
                            }
                            // Valid if continueWhile is still false, so break for each loop and move on
                            if(!continueWhile){
                                validCity = c;
                                break attempt;
                            }
                        }
                        
                        // If this line is reached, no valid candidate has been found in either parent, so return null
                        return null;
                    }
                    
                    // Add the new city to the start of the final block
                    blocks.get(2).add(0, validCity);
                    
                    // Now merge blocks 2 and 3 if feasible
                    if(canMerge(blocks.get(1),blocks.get(2))){
                        if(merged12){
                            // If already merged blocks 1 and 2, merge product with block 3
                            blocks.get(0).addAll(blocks.get(2));
                            merged23 = true;
                        } else{
                            // If not, merge block 2 with block 3
                            blocks.get(1).addAll(blocks.get(2));
                            merged23 = true;
                        }
                    }
                }
            }
            // Check if valid child was created
            child.setTour(blocks.get(0));
            if(child.getTour().get(child.getLength()-1) == RunGA.getEndCity()){
                // Compress method
                for(int i=0; i<child.getTour().size()-2; i++){
                    inner:
                    for(int j=child.getTour().size()-1; j>i+1; j--){
                        if(child.getTour().get(i).getUnblockedPaths().contains(child.getTour().get(j))){
                            int removeFrom = i+1;
                            int removeTo = j-1;
                            for(int k=removeFrom; k<=removeTo; k++){
                                // Always remove same index as the arraylist collapses
                                child.getTour().remove(removeFrom);
                            }
                            break inner;
                        }
                    }
                }
                // Update fittest on GUI if child is new fittest individual
                if(child.getFitness() > currentFittest.getFitness()){
                    RunGA.updateFittest(child);
                    currentFittest = child;
                }
                return child;
            }
            // If this line is reached, child was invalid (only used for testing, this never occurs)
            return null;
        }
        return child;
    }
    
    // Mutation which allows variable length genomes (new idea)
    public static Population variableMutate(Population p, double rate){
        if(RunGA.definedStartCity()){
            for(int j=elitismOffset; j<p.getSize(); j++){
                Tour t = p.getTour(j);
                for(int i=1; i<t.getLength()-1; i++){
                    // If mutation probability achieved, and city to mutate is not a compulsory via point
                    if((Math.random() < RunGA.mutationRate) && !RunGA.getUnorderedViaPoints().contains(t.getCity(i))){
                        // Find if there is a feasible arc to city to the left, then right
                        ArrayList<City> feasible = new ArrayList<City>();
                        ArrayList<City> feasibleLeft = t.optionsFromThisCity(t.getCity(i-1));
                        ArrayList<City> feasibleRight = t.optionsFromThisCity(t.getCity(i+1));
                        if(feasibleLeft.size() == 0 || feasibleRight.size() == 0){
                            continue;
                        }
                        // Feasible mutations must have unblocked arcs to both the left and right cities
                        for(City c : feasibleLeft){
                            if(feasibleRight.contains(c)){
                                feasible.add(c);
                            }
                        }
                        // If there are feasible options for this mutation, randomly choose one
                        if(feasible.size() > 0){
                            int replacement = (int) Math.round(Math.random()*(feasible.size()-1));
                            t.setCity(i, feasible.get(replacement));
                        }
                    }
                }
                // Update fittest on GUI if child is new fittest individual
                if(t.getFitness() > currentFittest.getFitness()){
                    RunGA.updateFittest(t);
                    currentFittest = t;
                }
            }
        } 
        return p;
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
                    }
                }
            }
        }
        return p;
    }
}
