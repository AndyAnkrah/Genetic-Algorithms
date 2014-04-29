import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.net.URL;
import java.awt.event.*;
import java.util.Timer;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Polygon;
import java.awt.geom.Line2D;

public class RunGA
{
    // GA Parameters
    private static final int generations = 5; // Number of generations
    private static final int popSize = 100; // Population size
    private static final boolean optimize = true; // Set true if you want to use optimizing GAs
    private static final int optimizeRange = 4; // Leave as 0 to optimize all (from longest to shortest)
    private static final int optimizeGenerations = 5; // Generations for optimizing GAs
    private static final int optimizePopSize = 50; // Population size for optimizing GAs
    private static int tspGenerations = 150; // Generations over which to run the TSP GA
    private static int tspPopSize = 400; // Population size over which to run the TSP GA
    private static String[] viaArray = new String[]{"Hild Bede", "Butler"}; // Array of via points (ordered names of cities to visit along the path)
    private static String[] unorderedViaArray = new String[]{"Collingwood", "Hatfield", "Castle", "Aidans", "Chads", "Cuths", "Ustinov", "Johns", "Marys", "Trevs", "Van Mildert", "Grey"}; // Array of unordered via points (names of cities to visit along the path in any order)
    
    // Evolution parameters (method choices for unordered via points, otherwise variable length genome methods assumed)
    public static boolean elitism = true; // Elitism on / off
    public static double elitismProportion = 0.6; // Elitism proportion
    public static double tournamentProportion = 0.005; // Tournament size as a proportion of population size
    public static int selectionMethod = 1; // 0 -> random | 1 -> tournament | 2-> roulette wheel
    public static int crossoverMethod = 1; // 0 -> random | 1 -> default | 2 -> PMX
    public static int mutationMethod = 0; // 0 - > random | 1 -> swapping | 2 -> insertion
    public static double crossoverRate = 1.0; // Rate of crossover
    public static double mutationRate = 0.09; // Rate of mutation
    
    // GUI Appearance
    private final int frameWidth = 1600; // Width of frame (automatically scales)
    private final int frameHeight = 1600; // Height of frame (automatically scales)
    
    // Initialise variables
    private static double scalingX;
    private static double scalingY;
    private ArrayList<Population> populations;
    private ArrayList<Population> populationsClone;
    private int generation;
    private static GUI gui;
    private ArrayList<double[]> coordinates;
    private static Tour fittest;
    private boolean userCities;
    private boolean startedGA;
    private Integer[] mouse;
    private static City startCity;
    private static City endCity;
    private static ArrayList<String> viaPoints;
    private static ArrayList<String> unorderedViaPoints;
    private boolean optimizing;
    private boolean hasOptimized;
    private Tour route;
    private Tour currentRoute;
    private Tour optimizedRoute;
    private int routeIndexCumulative;
    private int optRange;
    private boolean routeChanged;
    private static HashMap<String, Tour> shortestPaths;
    private static boolean tspGA;
    private ArrayList<Double> trialDistances;
    private boolean canConnect;
    private boolean unorderedCalculating;
    
    // Main method called when program is run from terminal. Runs the GA chosen by the parameters above
    public static void main(String args[]){
        RunGA r = new RunGA();
    }
    
    // RunGA constructor method (paramter: set true if you want to plot your own cities for TSP at runtime by clicking on the map)
    public RunGA(){
        // Initialise other variables
        startedGA = false;
        optimizing = false;
        hasOptimized = false;
        optRange = optimizeRange;
        route = new Tour(false);
        currentRoute = new Tour(false);
        optimizedRoute = new Tour(false);
        routeChanged = false;
        tspGA = false;
        unorderedCalculating = false;
        mouse = new Integer[2];
        mouse[0] = 0;
        mouse[1] = 0;
        routeIndexCumulative = 0;
        scalingX = 0.0;
        scalingY = 0.0;
        startCity = new City("",0,0);
        endCity = new City("",0,0);
        // Store coordinates of all cities in a single ArrayList
        coordinates = new ArrayList<double[]>();
        trialDistances = new ArrayList<Double>();
        // Store populations in arraylist
        populations = new ArrayList<Population>();
        populationsClone = new ArrayList<Population>();
        // Store from via and to cities in arraylist
        viaPoints = new ArrayList<String>();
        unorderedViaPoints = new ArrayList<String>();
        generation = 0;
        // Start GUI
        gui = new GUI();
        fittest = new Tour(false);
        canConnect = true;
        
        // Add cities to the grid
        AllCities.addCities();
        // Run GA for maps
        if(!definedStartCity() && (viaArray.length != 0)){
            // Show error message and quit if there are no defined start and end cities
            System.out.println("You cannot define just a single via point.");
            System.exit(0);
        }
        // Add required route to arraylists
        for(String s : viaArray){
            viaPoints.add(s);
        }
        for(String s : unorderedViaArray){
            unorderedViaPoints.add(s);
        }
        
        // Run different GA depending on whether there are unordered via points
        if(unorderedViaPoints.size() == 0){
            // Run GA between each of the via points
            for(int i=0; i<viaPoints.size()-1; i++){
                prepareGA(viaPoints.get(i), viaPoints.get(i+1), generations, popSize);
                if(i==0){
                    route.getTour().addAll(fittest.getTour());
                    currentRoute.getTour().addAll(fittest.getTour());
                } else{
                    route = concatenateTours(route, fittest);
                    currentRoute = concatenateTours(currentRoute, fittest);
                }
            }
            populationsClone.addAll(populations);
            prepareOptimize(0, true, true);
        } else{
           // If there are unordered via points, TSP with GA-calculated paths between pairs of cities
           unorderedCalculating = true;
           shortestPaths = new HashMap<String, Tour>();
           if(viaPoints.size() >= 2){
               // Add start city
               unorderedViaPoints.add(0, viaPoints.get(0));
               // Add end city
               unorderedViaPoints.add(viaPoints.get(1));
            }
           // Find shortest paths between each pair of cities
           for(int i=0; i<unorderedViaPoints.size(); i++){
               for(int j=i+1; j<unorderedViaPoints.size(); j++){
                   populations.clear();
                   prepareGA(unorderedViaPoints.get(i), unorderedViaPoints.get(j), generations, popSize);
                   prepareOptimize(0, false, false);
                   String startToEnd = unorderedViaPoints.get(i) + "" + unorderedViaPoints.get(j);
                   shortestPaths.put(startToEnd, populations.get(0).getFittest());
               }
           }
           // Start GA to solve TSP for unordered via points
           tspGA = true;
           canConnect = false;
           unorderedCalculating = false;
           if(viaPoints.size() >= 2){
               prepareGA(viaPoints.get(0), viaPoints.get(1), tspGenerations, tspPopSize);
           } else if(viaPoints.size() == 0){
               startGA(tspGenerations, tspPopSize);
           }
        }
    }
    
    // Defines the start and end city over which to solve the shortest path problem, then starts the algorithm with the specified number of generations and population size
    public void prepareGA(String start, String end, int gens, int populationSize){
        // Get start and end cities if defined
        if(!start.equals("")){
            startCity = getCityByReference(start);
        }
        if(!end.equals("")){
            endCity = getCityByReference(end);
        }
        // Initialise fittest variable
        fittest = new Tour(false);
        // Start Genetic Algorithm
        startGA(gens, populationSize);
    }
    
    // Starts the algorithm with the specified number of generations and population size
    public void startGA(int gens, int populationSize){
        if(!tspGA){
            // Normal case from start to end city
            startedGA = true;
            Population pop = new Population(populationSize, true);
            generation = pop.getGeneration();
            fittest = pop.getFittest();
            populations.add(pop);
            updateGUI();
            // Evolve population over the specified number of generations
            for(int i=0; i<gens; i++){
                pop = Algorithm.evolve(pop);
                generation = pop.getGeneration();
                fittest = pop.getFittest();
                updateGUI();
            }
        } else{
            // If running tspGA for unordered via points
            startedGA = true;
            Population pop = new Population(populationSize, true);
            generation = pop.getGeneration();
            fittest.getTour().addAll(pop.getFittest().getTour());
            populations.add(pop);
            updateGUI();
            // Evolve population over the specified number of generations
            for(int i=0; i<gens; i++){
                pop = Algorithm.evolve(pop);
                generation = pop.getGeneration();
                ArrayList<City> orderedPath = new ArrayList<City>();
                orderedPath.addAll(pop.getFittest().getTour());
                // Construct expanded fittest path to show in GUI (using previously stored hashmap of shortest paths)
                for(int z=0; z<orderedPath.size()-1; z++){
                    String fromTo1 = orderedPath.get(z).getReference() + "" + orderedPath.get(z+1).getReference();
                    String fromTo2 = orderedPath.get(z+1).getReference() + "" + orderedPath.get(z).getReference();
                    if(shortestPaths.containsKey(fromTo1)){
                        if(z==0){
                            fittest.getTour().clear();
                            fittest.getTour().addAll(shortestPaths.get(fromTo1).getTour());
                        } else{
                            fittest = concatenateTours(fittest, shortestPaths.get(fromTo1));
                        }
                    } else if(shortestPaths.containsKey(fromTo2)){
                        Tour reversed = new Tour(false);
                        reversed.getTour().addAll(shortestPaths.get(fromTo2).getTour());
                        Collections.reverse(reversed.getTour());
                        if(z==0){
                            fittest.getTour().clear();
                            fittest.getTour().addAll(reversed.getTour());
                        } else{
                            fittest = concatenateTours(fittest, reversed);
                        }
                    }
                }
                route = fittest;
                updateGUI();
            }
        }
    }
    
    // Prepares and performs post-solution optimization (parameters: index of population to optimize in populations arralist, true if you want it to loop through all optimizations for you, true if route can be altered on GUI)
    public void prepareOptimize(int whichpop, boolean loopthrough, boolean alterRoute){
        if(!loopthrough){
            // Optimize route
            if(optimize && (optimizeRange == 0 || optimizeRange >= 3)){
                populationsClone.clear();
                populationsClone.add(populations.get(populations.size()-1));
                optimize(populationsClone.get(0), true, alterRoute);
                if(whichpop==0){
                    optimizedRoute.getTour().clear();
                    optimizedRoute.getTour().addAll(populationsClone.get(0).getFittest().getTour());
                } else{
                    optimizedRoute = concatenateTours(optimizedRoute, populationsClone.get(0).getFittest());
                }
                if(whichpop == viaPoints.size()-2){
                    // After optimisation, show final route
                    hasOptimized = true;
                    route = optimizedRoute;
                    fittest = optimizedRoute;
                    updateGUI();
                }
            } else{
                fittest = route;
                updateGUI();
            }
        } else{
            // Optimize route
            if(optimize && (optimizeRange == 0 || optimizeRange >= 3)){
                // If first path has unordered via points, need fittest route for each segment stored in new population in populations
                if(unorderedViaPoints.size() > 0){
                    // Different set up if there have been compulsory unordered via points
                    ArrayList<Integer> indeces = new ArrayList<Integer>();
                    for(int i=0; i<unorderedViaPoints.size(); i++){
                        int index = route.getTour().indexOf(getUnorderedViaPoints().get(i));
                        indeces.add(index);
                    }
                    Collections.sort(indeces);
                    populations.clear();
                    Tour tour = new Tour(false);
                    tour.getTour().addAll(route.getTour());
                    // Loop through unordered via points subpaths to optimize
                    for(int i=0; i<unorderedViaPoints.size()+1; i++){
                        Population p = new Population(1, false);
                        Tour t = new Tour(false);
                        ArrayList<City> segment = new ArrayList<City>();
                        int start;
                        int end;
                        if(i==0){
                            start = 0;
                        } else{
                            start = indeces.get(i-1);
                        }
                        if(i==unorderedViaPoints.size()){
                            end = route.getLength()-1;
                        } else{
                            end = indeces.get(i);
                        }
                        for(int j=start; j<=end; j++){
                            segment.add(route.getTour().get(j));
                        }
                        t.getTour().addAll(segment);
                        p.setTour(0, t);
                        populations.add(p);
                    }
                }
                
                // Clear unordered via points after route has been found - for further GAs
                unorderedViaPoints.clear();
                int counter = populations.size();
                for(int i=0; i<counter; i++){
                    boolean isFirst;
                    if(i==0){
                        isFirst = true;
                    } else{
                        isFirst = false;
                    }
                    optimize(populations.get(i), isFirst, true);
                }
                // After optimisation, show final route
                hasOptimized = true;
                fittest = route;
                updateGUI();
            } else{
                fittest = route;
                updateGUI();
            }
        }
    }
    
    // Post-solution optimization (new idea) (parameters: specified population, true if it's the first population in the list (so doesn't need to be added to prior path), true if route can be altered for GUI)
    public void optimize(Population p, boolean isFirst, boolean alterRoute){
        // Optimize by using genetic algorithm on small segments of the route in turn
        boolean stillImproving = true;
        boolean optRangeStarted = false;
        boolean optRangeDecrementing = true;
        // Continue while optimization range is still decreasing to 3
        while(optRangeDecrementing){
            optRangeDecrementing = false;
            // Continue until no more improvements can be found for the current optimization range, then decrement it
            while(stillImproving){
                Tour t = p.getFittest();
                // Route changes for GUI
                if(!alterRoute){
                    route.getTour().clear();
                    route.getTour().addAll(t.getTour());
                }
                if(optimizeRange == 0){
                    // Start from the longest
                    if(!optRangeStarted){
                        optRange = t.getLength();
                        optRangeStarted = true;
                    }
                } else{
                    // Start from the chosen oprimize range
                    if(!optRangeStarted){
                        optRange = optimizeRange;
                        optRangeStarted = true;
                    }
                }
                // Optimize as long as the length of the subpath is greater than the optimization range
                if(t.getLength() >= optRange){
                    // Store pre-optimization distance for comparison
                    double distanceBefore = t.getTourDistance();
                    optimizing = true;
                    for(int i=0; i<t.getLength()-optRange+1; i++){
                        Tour current = new Tour(false);
                        for(int j=i; j<i+optRange; j++){
                            current.getTour().add(t.getTour().get(j));
                        }
                        // Run optimizing GA
                        prepareGA(t.getTour().get(i).getReference(), t.getTour().get(i+optRange-1).getReference(), optimizeGenerations, optimizePopSize);
                        // Replace this section of current route with the new fittest if superior
                        if(fittest.getTourDistance() < current.getTourDistance()){
                            /*optRangeStarted = false; // uncomment for extra optimizations (takes longer)*/
                            int routeIndex;
                            if(routeIndexCumulative > 0 && alterRoute){
                                routeIndex = routeIndexCumulative + i-1;
                            } else{
                                routeIndex =  i;
                            }
                            // Remove current subpath
                            for(int j=i; j<i+optRange; j++){
                                t.getTour().remove(i);
                                route.getTour().remove(routeIndex);
                            }
                            int counter = 0;
                            // Add new subpath
                            for(int j=0; j<fittest.getLength(); j++){
                                t.getTour().add(i+counter, fittest.getTour().get(j));
                                route.getTour().add(routeIndex+counter, fittest.getTour().get(j));
                                counter++;
                            }
                            updateGUI();
                        }
                    }
                    fittest = t;
                    double distanceAfter = t.getTourDistance();
                    // Determines whether improvements are still being found for this optimization range
                    if(distanceBefore <= distanceAfter){
                        stillImproving = false;
                    }
                    updateGUI();
                } else{
                    fittest = t;
                    stillImproving = false;
                }
            }
            // Decrement optimize range once no further improvements can be found for this range
            if(optRangeStarted){
                if(optRange > 3){
                    optRangeDecrementing = true;
                    stillImproving = true;
                    optRange--;
                }
            }
        }
        optimizing = false;
        // Set correct cumulative index depending on whether this is the first population in the list or not
        routeIndexCumulative += fittest.getLength();
        if(!isFirst){
            routeIndexCumulative--;
        }
    }
    
    // Combines two paths into one
    public Tour concatenateTours(Tour t1, Tour t2){
        Tour combined = new Tour(false);
        combined.getTour().addAll(t1.getTour());
        combined.getTour().remove(combined.getTour().size()-1);
        combined.getTour().addAll(t2.getTour());
        return combined;
    }
    
    // Updates the fittest variable to the specified tour
    public static void updateFittest(Tour t){
        fittest = t;
        updateGUI();
    }
    
    // Returns true if parameters have defined a fixed starting city
    public static boolean definedStartCity(){
        if(viaArray.length >= 2){
            return true;
        }
        return false;
    }
    
    // Get starting city
    public static City getStartCity(){
        return startCity;
    }
    
    // Get destination city
    public static City getEndCity(){
        return endCity;
    }
    
    // Returns true if TSP is being run for unordered via points (needed for different calculations for distances between cities)
    public static boolean isTSP(){
        return tspGA;
    }
    
    // Get list of ordered via points
    public static ArrayList<City> getViaPoints(){
        ArrayList<City> result = new ArrayList<City>();
        for(String s : viaPoints){
            result.add(getCityByReference(s));
        }
        return result;
    }
    
    // Get list of unordered via points
    public static ArrayList<City> getUnorderedViaPoints(){
        ArrayList<City> result = new ArrayList<City>();
        for(String s : unorderedViaPoints){
            result.add(getCityByReference(s));
        }
        return result;
    }
    
    // Get the city object corresponding to the specified city name
    public static City getCityByReference(String s){
        City city = new City("",0,0);
        for(City c : AllCities.getCities()){
            if(c.getReference().equals(s)){
                return c;
            }
        }
        return city;
    }
    
    // Get the hashmap of shortest paths between each pair of non-obstacle cities in the graph (calculated for unordered via points)
    public static HashMap<String, Tour> getShortestPaths(){
        return shortestPaths;
    }
    
    // Updates Graphical User Interface (GUI)
    public static void updateGUI(){
        gui.refreshGUI();
    }
    
    // GUI class - controls everything that is visually outputted in a frame
    public class GUI extends JPanel{
        
        private JFrame frame;
        private Panel panel;
        
        // Constructor method for GUI
        public GUI(){
            showFrame();
        }
        
        // Refreshes the GUI to account for any changes
        public void refreshGUI(){
            panel.revalidate();
            panel.repaint();
        }
        
        // Shows frame to "paint" on
        public void showFrame(){
            frame = new JFrame("Map route finding GUI");
            panel = new Panel();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.setSize(frameWidth+20, frameHeight+54);
            frame.setVisible(true);
        }
        
        public class Panel extends JPanel{

            public Panel(){
                // Set JPanel background colour
                setBackground(Color.WHITE); 
            }

            // The paint method determines everything that is displayed on the frame
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                // Set fonts to be used
                Font f1 = new Font("Calibri", Font.BOLD, 32);
                FontMetrics metrics1 = new FontMetrics(f1) {  
                    }; 
                Font f2 = new Font("Calibri", Font.BOLD, 20);
                FontMetrics metrics2 = new FontMetrics(f2) {  
                    };
                Font f3 = new Font("Calibri", Font.BOLD, 64);
                FontMetrics metrics3 = new FontMetrics(f3) {  
                    };
                Font f4 = new Font("Calibri", Font.BOLD, 240);
                FontMetrics metrics4 = new FontMetrics(f4) {  
                    };
                g.setFont(f1);
                g.setColor(Color.BLACK);
                // If unordered via points distances are being calculated, display a loading message
                if(unorderedCalculating){
                    g.setFont(f1);
                    // Paint calculating text
                    String line1 = "Calculating distances between unordered via points...";
                    Rectangle2D bounds1 = metrics1.getStringBounds(line1, null);  
                    int line1width = (int) bounds1.getWidth();
                    int line1height = (int) bounds1.getHeight();
                    g.drawString(line1, (frameWidth/2)-(line1width/2), (frameHeight/2)-(line1height/2));
                } else if(startedGA){
                    // If the GA has been started, display the following
                    g.setFont(f3);
                    // Draw textual representation of tour
                    int startX = 200;
                    int spacingY = 75;
                    int textHeight = (fittest.getLength()*spacingY);
                    for(int i=0; i<fittest.getLength(); i++){
                        g.drawString(fittest.getTour().get(i).toString(), startX, ((frameHeight-textHeight)/2)+(i*spacingY));
                    }
                    // Paint generation summary text
                    String line1 = "GENERATION: " + generation;
                    Rectangle2D bounds1 = metrics1.getStringBounds(line1, null);  
                    int line1width = (int) bounds1.getWidth();
                    g.drawString(line1, 800, 600);
                    g.setFont(f4);
                    g.setColor(Color.CYAN);
                    double roundedDistance = Math.round(fittest.getTourDistance()*100.0)/100.0;
                    String line3 = "" + roundedDistance;
                    Rectangle2D bounds3 = metrics1.getStringBounds(line3, null);  
                    int line3width = (int) bounds3.getWidth();
                    g.drawString(line3, 800, 900);
                    g.setFont(f3);
                    g.setColor(Color.BLACK);
                    String line4 = "miles";
                    g.drawString(line4, 800, 1000);
                }
            }
        }
    }
}
