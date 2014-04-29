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
    private static String[] viaArray = new String[]{"A", "H", "G", "C", "F", "D", "E", "B"}; // Array of via points (ordered names of cities to visit along the path)
    private static String[] unorderedViaArray = new String[]{}; // Array of unordered via points (names of cities to visit along the path in any order)
    private double borderWidth = 0.1; // Width of exterior border around the map on GUI
    private boolean createBorder = true; // Set true to show this border
    
    // Evolution parameters (method choices for unordered via points, otherwise variable length genome methods assumed)
    public static boolean elitism = true; // Elitism on / off
    public static double elitismProportion = 0.6; // Elitism proportion
    public static double tournamentProportion = 0.005; // Tournament size as a proportion of population size
    public static int selectionMethod = 1; // 0 -> random | 1 -> tournament | 2-> roulette wheel
    public static int crossoverMethod = 1; // 0 -> random | 1 -> default | 2 -> PMX
    public static int mutationMethod = 2; // 0 - > random | 1 -> swapping | 2 -> insertion
    public static double crossoverRate = 1.0; // Rate of crossover
    public static double mutationRate = 0.09; // Rate of mutation
    
    // GUI Appearance
    private final int frameWidth = 1600; // Width of frame (automatically scales)
    private final int frameHeight = 1600; // Height of frame (automatically scales)
    private final int yOffset = 50; // Distance between text lines in GUI
    private final boolean splitRoute = false; // Splits a long path into two lines (outputted as text on the GUI) if true
    private final int[] borders = new int[]{20,175,20,20}; // left, top, right, bottom borders around map
    private final int townCircleDiameter = 40; // Diameter of circles representing cities on GUI (labels inside)
    private final int tspTownCircleDiameter = 20; // Diameter of city circles for TSP (no labels inside)
    private final int[] stringOffset = new int[]{6,14}; // Centering offset for city labels in circles
    private final int strokeWidth = 5; // Line thickness for GUI
    private int maxXscale = 200; // Max x coordinate of specified cities (for best results keep the same)
    private int maxYscale = 200; // Max y coordinate of specified cities (for best results keep the same)
    private boolean showNodes = false; // Set true if you want to show all of the nodes in the graph (including obstacle vertices) as circles
    
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
    private Obstacle borderPolygon;
    private static HashMap<String, Tour> shortestPaths;
    private static boolean tspGA;
    private ArrayList<Double> trialDistances;
    private boolean canConnect;
    
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
        mouse = new Integer[2];
        mouse[0] = 0;
        mouse[1] = 0;
        routeIndexCumulative = 0;
        scalingX = 0.0;
        scalingY = 0.0;
        startCity = new City("",0,0,false);
        endCity = new City("",0,0,false);
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
        // Run GA with obstacles
        if(!definedStartCity()){
            // Show error message and quit if there are no defined start and end cities
            System.out.println("You must define a start and destination city for this option.");
            System.exit(0);
        }
        // Add obstacles to the grid
        AllObstacles.addObstacles();
        // Calculate scaling factors for GUI
        scaleCities();
        // Find unnecessary obstacle cities
        ArrayList<City> citiesToRemove = new ArrayList<City>();
        for(City c : AllCities.getCities()){
            if(c.isObstacle()){
                for(Obstacle o : AllObstacles.getObstacles()){
                    if(o.contains(c.getX(), c.getY()) && c.getReference().indexOf(o.getReference()+".") < 0){
                        citiesToRemove.add(c);
                        break;
                    }
                }
            }
        }
        // Remove unnecessary obstacle cities
        City[] copiedCities = new City[AllCities.getCities().size()];
        for(int i=0; i<AllCities.getCities().size(); i++){
            copiedCities[i] = AllCities.getCities().get(i);
        }
        for(City c : citiesToRemove){
            int index = AllCities.getCities().indexOf(c);
            AllCities.getCities().remove(c);
            coordinates.remove(index);
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
                // Optimize route (can either do here for each subpath in turn, or as 3 lines below for post-final-solution optimization)
                //prepareOptimize(i, false, true);
            }
            populationsClone.addAll(populations);
            prepareOptimize(0, true, true);
        } else{
           // If there are unordered via points, TSP with GA-calculated paths between pairs of cities
           shortestPaths = new HashMap<String, Tour>();
           // Add start city
           unorderedViaPoints.add(0, viaPoints.get(0));
           // Add end city
           unorderedViaPoints.add(viaPoints.get(1));
           // Find shortest paths between each pair of cities
           for(int i=0; i<unorderedViaPoints.size(); i++){
               for(int j=i+1; j<unorderedViaPoints.size(); j++){
                   populations.clear();
                   prepareGA(unorderedViaPoints.get(i), unorderedViaPoints.get(j), generations, popSize);
                   // Optimize the paths between each pair of cities as they are calculated (if optimize parameter set to true)
                   prepareOptimize(0, false, false);
                   String startToEnd = unorderedViaPoints.get(i) + "" + unorderedViaPoints.get(j);
                   // Store shortest path found in a hashmap for later use (contains info on the start->end arc, and the full path respectively)
                   shortestPaths.put(startToEnd, populations.get(0).getFittest());
               }
           }
           // Start GA to solve TSP for unordered via points
           tspGA = true;
           canConnect = false;
           prepareGA(viaPoints.get(0), viaPoints.get(1), tspGenerations, tspPopSize);
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
        // Trivial case for Euclidean plane: if there is a direct route between start and end cities, return that (if there are no compulsory via points)
        if(!AllObstacles.hitsAnyObstacle(startCity, endCity) && canConnect && !tspGA){
            startedGA = true;
            Tour solution = new Tour(false);
            solution.getTour().add(startCity);
            solution.getTour().add(endCity);
            Population pop = new Population(1, true);
            pop.setTour(0, solution);
            populations.add(pop);
            generation = pop.getGeneration();
            fittest = solution;
            if(unorderedViaPoints.size() == 0){
                updateGUI();
            }
        } else{
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
        City city = new City("",0,0,false);
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
    
    // Calculate GUI scalings between raw coordinates and frame positions for cities / obstacles
    public void scaleCities(){
        // Calculate scaling factors for GUI
        double minX = AllCities.getMinX();
        double maxX = AllCities.getMaxX();
        double minY = AllCities.getMinY();
        double maxY = AllCities.getMaxY();
        double widthDifference = maxX - minX;
        double heightDifference = maxY - minY;
        scalingX = ((double)frameWidth-((double)borders[0]+(double)borders[2]))/(double)maxX;
        scalingY = ((double)frameHeight-((double)borders[1]+(double)borders[3]))/(double)maxY;
        
        // Create border obstacle
        if(createBorder && !userCities){
            int[] x = new int[10];
            int[] y = new int[10];
            x[0] = (int)Math.round(minX);
            y[0] = (int)Math.round(minY);
            x[1] = (int)Math.round(minX);
            y[1] = (int)Math.round(maxY);
            x[2] = (int)Math.round(maxX);
            y[2] = (int)Math.round(maxY);
            x[3] = (int)Math.round(maxX);
            y[3] = (int)Math.round(minY);
            x[4] = (int)Math.round(minX);
            y[4] = (int)Math.round(minY);
            x[5] = (int)Math.round(minX-borderWidth);
            y[5] = (int)Math.round(minY-borderWidth);
            x[6] = (int)Math.round(minX-borderWidth);
            y[6] = (int)Math.round(maxY+borderWidth);
            x[7] = (int)Math.round(maxX+borderWidth);
            y[7] = (int)Math.round(maxY+borderWidth);
            x[8] = (int)Math.round(maxX+borderWidth);
            y[8] = (int)Math.round(minY-borderWidth);
            x[9] = (int)Math.round(minX-borderWidth);
            y[9] = (int)Math.round(minY-borderWidth);
            
            borderPolygon = new Obstacle("border", x, y, 0, 0, 0);
            AllObstacles.getObstacles().add(borderPolygon);
        }
        
        // Case if user defines their own cities at runtime
        if(userCities){
            double xScale = (double)(widthDifference)/(double)maxXscale;
            double yScale = (double)(heightDifference)/(double)maxYscale;
            for(City c : AllCities.getCities()){
                int newX = (int)Math.round((double)(c.getX() - minX)/xScale);
                int newY = (int)Math.round((double)(c.getY() - minY)/yScale);
                c.setX(newX);
                c.setY(newY);
            }
        } else{
            // Add city positions to coordinates arraylist for GUI
            for(City c : AllCities.getCities()){
                double[] coords = new double[2];
                coords[0] = (double)borders[0]+((double)c.getX()*scalingX);
                coords[1] = (double)borders[1]+((double)c.getY()*scalingY);
                coordinates.add(coords);
            }
        }
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
            frame = new JFrame("Path finding GUI");
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
                
                if(startedGA){
                    // Set new font
                    g.setFont(f2);
                    // Draw and fill obstacles
                    g.setColor(Color.ORANGE);
                    for(Obstacle o : AllObstacles.getObstacles()){
                        Polygon p = new Polygon();
                        for(int i=0; i<o.npoints; i++){
                            int x = borders[0]+(int)((double)o.xpoints[i]*scalingX);
                            int y = borders[1]+(int)((double)o.ypoints[i]*scalingY);
                            p.addPoint(x,y);
                        }
                        // Draw a polygon for each obstacle
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setStroke(new BasicStroke(3));
                        g.drawPolygon(p);
                        g.fillPolygon(p);
                    }
                    
                    // Paint lines to represent each arc between cities in route so far
                    g.setColor(Color.BLACK);
                    for(int i=0; i<route.getLength()-1; i++){
                        City from = route.getCity(i);
                        int fromIndex = AllCities.getCities().indexOf(from);
                        City to = route.getCity(i+1);
                        int toIndex = AllCities.getCities().indexOf(to);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setStroke(new BasicStroke(strokeWidth));
                        g.drawLine((int)Math.round(coordinates.get(fromIndex)[0]), (int)Math.round(coordinates.get(fromIndex)[1]), (int)Math.round(coordinates.get(toIndex)[0]), (int)Math.round(coordinates.get(toIndex)[1]));
                    }
                    
                    // Paint lines to represent each arc between cities in fittest individual
                    if(optimizing){
                        g.setColor(Color.CYAN);
                    }
                    for(int i=0; i<fittest.getLength()-1; i++){
                        City from = fittest.getCity(i);
                        int fromIndex = AllCities.getCities().indexOf(from);
                        City to = fittest.getCity(i+1);
                        int toIndex = AllCities.getCities().indexOf(to);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setStroke(new BasicStroke(strokeWidth));
                        g.drawLine((int)Math.round(coordinates.get(fromIndex)[0]), (int)Math.round(coordinates.get(fromIndex)[1]), (int)Math.round(coordinates.get(toIndex)[0]), (int)Math.round(coordinates.get(toIndex)[1]));
                    }
                    
                    // Paint circles representing cities, and associated references as labels
                    g.setColor(Color.BLACK);
                    for(int i=0; i<AllCities.getCities().size(); i++){
                        // Paint if not a hidden city (corner of obstacle)
                        if(!AllCities.getCities().get(i).isObstacle() || showNodes){
                            double[] coords = coordinates.get(i);
                            Graphics2D g2d = (Graphics2D)g;
                            Ellipse2D.Double circle = new Ellipse2D.Double(coords[0]-(townCircleDiameter/2), coords[1]-(townCircleDiameter/2), townCircleDiameter, townCircleDiameter);
                            g2d.fill(circle);
                            g.setColor(Color.WHITE);
                            if(!AllCities.getCities().get(i).isObstacle()){
                                g.drawString(AllCities.getCities().get(i).getReference(), (int)Math.round(coords[0]-stringOffset[0]), (int)Math.round(coords[1])+6);
                            } else{
                                g.drawString(AllCities.getCities().get(i).getReference(), (int)Math.round(coords[0]-stringOffset[1]), (int)Math.round(coords[1])+6);
                            }
                            g.setColor(Color.BLACK);
                        }
                    }
                
                    // Paint generation summary text
                    g.setFont(f1);
                    Color textColour = Color.decode("#333333");
                    g.setColor(Color.BLACK);
                    if(hasOptimized){
                        String line4 = "OPTIMIZED SOLUTION FOUND";
                        Rectangle2D bounds4 = metrics1.getStringBounds(line4, null);  
                        int line4width = (int) bounds4.getWidth();
                        g.drawString(line4, (frameWidth/2)-(line4width/2), 50);
                    } else{
                        if(!optimizing){
                            String line1 = "GENERATION: " + generation;
                            Rectangle2D bounds1 = metrics1.getStringBounds(line1, null);  
                            int line1width = (int) bounds1.getWidth();
                            g.drawString(line1, (frameWidth/2)-(line1width/2), 50);
                        } else{
                            String line4 = "OPTIMIZING...";
                            Rectangle2D bounds4 = metrics1.getStringBounds(line4, null);  
                            int line4width = (int) bounds4.getWidth();
                            g.drawString(line4, (frameWidth/2)-(line4width/2), 50);
                        }
                    }
                    // Split route onto two lines if parameter set to true
                    if(splitRoute && populationsClone.size() > 0){
                        for(int i=0; i<populationsClone.size(); i++){
                            double roundedDistance = Math.round(populationsClone.get(i).getFittest().getTourDistance()*100.0)/100.0;
                            String line2 = "FITTEST: " + populationsClone.get(i).getFittest().toString() + " (" + roundedDistance + ")";
                            Rectangle2D bounds2 = metrics1.getStringBounds(line2, null);
                            int line2width = (int) bounds2.getWidth();
                            g.drawString(line2, (frameWidth/2)-(line2width/2), 100+(i*yOffset));
                        }
                    } else{
                        // Otherwise print textual output normally
                        String line2 = "FITTEST: " + fittest.toString();
                        Rectangle2D bounds2 = metrics1.getStringBounds(line2, null);  
                        int line2width = (int) bounds2.getWidth();
                        g.drawString(line2, (frameWidth/2)-(line2width/2), 100);
                        double roundedBefore = Math.round(currentRoute.getTourDistance()*100.0)/100.0;
                        double roundedDistance = Math.round(fittest.getTourDistance()*100.0)/100.0;
                        String line3;
                        // Displays before and after distances if optimization has been performed
                        if(roundedBefore > 0.0 && hasOptimized){
                            line3 = "DISTANCE: " + roundedBefore + " --> " + roundedDistance;
                        } else{
                            line3 = "DISTANCE: " + roundedDistance;
                        }
                        Rectangle2D bounds3 = metrics1.getStringBounds(line3, null);  
                        int line3width = (int) bounds3.getWidth();
                        g.drawString(line3, (frameWidth/2)-(line3width/2), 150);
                    }
                }
            }
        }
    }
}
