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
    // GA Parameters (this default example solves a 40-city tsp with a high number of generations to demonstrate the process)
    private static final int generations = 5000; // Number of generations
    private static final int popSize = 400; // Population size
    private static String[] viaArray = new String[]{}; // Array of via points (ordered names of cities to visit along the path)
    
    // Evolution methods & Data collection
    private static final int trials = 100; // Number of trials per data point to average over
    private static final double optimum = 1064; // Known optimum solution for comparison
    public static boolean elitism = true; // Elitism on / off
    public static double elitismProportion = 0.1; // Elitism proportion
    public static double tournamentProportion = 0.005; // Tournament size as a proportion of population size
    public static int selectionMethod = 1; // 0 -> random | 1 -> tournament | 2-> roulette wheel
    public static int crossoverMethod = 1; // 0 -> random | 1 -> segment | 2 -> PMX
    public static int mutationMethod = 2; // 0 - > random | 1 -> swapping | 2 -> insertion
    public static double crossoverRate = 1.0; // Rate of crossover
    public static double mutationRate = 0.09; // Rate of mutation
    
    // GUI Appearance
    private final int frameWidth = 1600; // Width of frame (automatically scales)
    private final int frameHeight = 1600; // Height of frame (automatically scales)
    private final int border = 200; // Border width
    private final int townCircleDiameter = 30; // Diameter of circles representing cities on GUI
    private final int strokeWidth = 5; // Line thickness for GUI
    
    // Initialise variables
    private static double scalingX;
    private static double scalingY;
    private int generation;
    private static GUI gui;
    private ArrayList<double[]> coordinates;
    private static Tour fittest;
    private boolean userCities;
    private boolean startedGA;
    private Integer[] mouse;
    private ArrayList<Double> trialDistances;
    
    // Main method called when program is run from terminal. Runs the GA chosen by the parameters above
    public static void main(String args[]){
        RunGA r = new RunGA(false);
    }
    
    // RunGA constructor method (paramter: set true if you want to plot your own cities for TSP at runtime by clicking on the map)
    public RunGA(boolean userDefinedCities){
        userCities = userDefinedCities;
        startedGA = false;
        mouse = new Integer[2];
        mouse[0] = 0;
        mouse[1] = 0;
        // Store coordinates of all cities in a single ArrayList
        coordinates = new ArrayList<double[]>();
        // Initialise instance variables
        generation = 0;
        fittest = new Tour(true);
        // Initialise population of creatures (tours)
        Population pop = new Population(popSize, true);
        if(!userDefinedCities){
            // Add cities to the grid
            AllCities.addCities();
            // Calculate scaling factors for GUI
            scalingX = ((double)frameWidth-((double)border*2.0))/(double)AllCities.getMaxX();
            scalingY = ((double)frameHeight-((double)border*2.0))/(double)AllCities.getMaxY();
            // Add city positions to coordinates ArrayList
            for(City c : AllCities.getCities()){
                double[] coords = new double[2];
                coords[0] = border+((double)c.getX()*scalingX);
                coords[1] = border+((double)c.getY()*scalingY);
                coordinates.add(coords);
            }
            // Start GUI
            gui = new GUI();
            // Start Genetic Algorithm
            startGA();
        } else{
            // Initialise instance variables
            generation = 0;
            fittest = new Tour(true);
            // Start GUI
            gui = new GUI();
            // Initialise population of creatures (tours)
        }
    }
    
    // Data collection method for Section 2.4: Experimental Analysis
    public void dataCollect(int select, double tournProp, boolean elit, double elitProp, int cross, double crossRate, int mut, double mutRate){
        selectionMethod = select;
        tournamentProportion = tournProp;
        elitism = elit;
        elitismProportion = elitProp;
        crossoverMethod = cross;
        crossoverRate = crossRate;
        mutationMethod = mut;
        mutationRate = mutRate;
        trialDistances.clear();
        for(int i=0; i<trials; i++){
            generation = 0;
            startGA();
            double dist = fittest.getTourDistance();
            trialDistances.add(dist);
        }
        double cumulativeDistance = 0.0;
        for(double d : trialDistances){
            cumulativeDistance += d;
        }
        double averageDistance = Math.round((cumulativeDistance/trialDistances.size())*100.0)/100.0;
        double roundedPercent = Math.round((((averageDistance - optimum)/optimum)*100.0)*100.0)/100.0;
        System.out.println("Populations: " + popSize + ", Generations: " + generations);
        System.out.println("(S) (TPROP) (ELIT) (EPROP) (C) (C_%) (M) (M_%)");
        System.out.println("("+select+") ("+(Math.round(tournProp*1000.0)/1000.0)+") ("+elit+") ("+(Math.round(elitProp*1000.0)/1000.0)+") ("+cross+") ("+(Math.round(crossRate*1000.0)/1000.0)+") ("+mut+") ("+(Math.round(mutRate*1000.0)/1000.0)+")");
        System.out.println("Average solution distance: " + averageDistance);
        System.out.println("Average % over optimum: " + roundedPercent);
        System.out.println("__________________________________________________");
    }
    
    // Starts the algorithm with the specified number of generations and population size
    public void startGA(){
        startedGA = true;
        Population pop = new Population(popSize, true);
        // Evolve population over the specified number of generations
        for(int i=0; i<generations; i++){
            fittest = pop.getFittest();
            pop = Algorithm.evolve(pop);
            generation++;
            updateGUI();
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
        private JButton start;
        private JButton print;
        private JButton undo;
        private JButton random;
        private JButton nodes;
        private MouseAdapter ma;
        
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
            frame = new JFrame("Travelling Salesman Problem GUI");
            panel = new Panel();
            /*frame.addKeyListener(new KeyboardListener()); // Uncomment if enabling keyboard commands*/
            final MouseAdapter ma = new MouseAdapter();
            if(userCities){
                frame.addMouseListener(ma);
            }
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.setSize(frameWidth+20, frameHeight+54);
            frame.setVisible(true);
                    
            // Sets up buttons and interface for user-defined cities at runtime
            if(userCities){
                // Random city Button
                random = new JButton("Add Random City");
                random.setMnemonic(KeyEvent.VK_R);
                random.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            double x = (Math.random()*AllCities.getMaxX());
                            double y = (Math.random()*AllCities.getMaxY());
                            double[] coords = new double[2];
                            coords[0] = (double)border+((double)x*scalingX);
                            coords[1] = (double)border+((double)y*scalingY);
                            coordinates.add(coords);
                            int ref = AllCities.getCities().size()+1;
                            AllCities.addCity(new City("" + ref, x, y));
                            updateGUI();
                        }
                    });
                    
                // Print code to add cities Button
                print = new JButton("Print code to generate these cities");
                print.setMnemonic(KeyEvent.VK_P);
                print.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            for(City c : AllCities.getCities()){
                                System.out.println("addCity(new City(\"" + c.getReference() + "\", " + c.getX() + ", " + c.getY() + ", false));");
                            }
                        }
                    });
                    
                // Undo last city Button
                undo = new JButton("Undo");
                undo.setMnemonic(KeyEvent.VK_U);
                undo.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if(coordinates.size() > 0){
                                AllCities.getCities().remove(AllCities.getCities().size()-1);
                                coordinates.remove(coordinates.size()-1);
                                updateGUI();
                            }
                        }
                    });
                    
                // Start Genetic Algorithm Button
                start = new JButton("Start Genetic Algorithm");
                start.setMnemonic(KeyEvent.VK_S);
               start.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // SwingWorker to keep repainting each generation while button responds
                            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                                @Override
                                // Let startGA() run in background
                                public Void doInBackground() {
                                    frame.removeMouseListener(ma);
                                    start.setVisible(false);
                                    undo.setVisible(false);
                                    startGA();
                                    return null;
                                }
                                // Once completed
                                @Override
                                protected void done() {
                                    // get() would be available here if you want to use it
                                    generation = 0;
                                    start.setVisible(true);
                                    start.setText("Start again");
                                }
                            };
                            worker.execute();
                        }
                    });
                    
                panel.add(start);
                start.setVisible(true);
                panel.add(random);
                random.setVisible(true);
                panel.add(print);
                print.setVisible(true);
                panel.add(undo);
                undo.setVisible(true);
            }
        }
        
        public class Panel extends JPanel{

            public Panel(){
                // Set JPanel background colour
                Color bgColour = Color.decode("#6365ff");
                setBackground(bgColour); 
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
                g.setFont(f1);
                g.setColor(Color.BLACK);
                // If the GA has been started (i.e. not still defining user cities), display the following
                if(startedGA){
                    // Set new font
                    g.setFont(f2);
                    
                    // Paint lines to represent each arc between cities in route so far
                    g.setColor(Color.BLACK);
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
                        double[] coords = coordinates.get(i);
                        Graphics2D g2d = (Graphics2D)g;
                        Ellipse2D.Double circle = new Ellipse2D.Double(coords[0]-(townCircleDiameter/2), coords[1]-(townCircleDiameter/2), townCircleDiameter, townCircleDiameter);
                        g2d.fill(circle);
                        g.setColor(Color.WHITE);
                        g.drawString(AllCities.getCities().get(i).getReference(), (int)Math.round(coords[0]-6), (int)Math.round(coords[1])+6);
                        g.setColor(Color.BLACK);
                    }
                
                    // Paint generation summary text
                    g.setFont(f1);
                    Color textColour = Color.decode("#333333");
                    g.setColor(Color.BLACK);
                    String line1 = "GENERATION: " + generation;
                    Rectangle2D bounds1 = metrics1.getStringBounds(line1, null);  
                    int line1width = (int) bounds1.getWidth();
                    g.drawString(line1, (frameWidth/2)-(line1width/2), 50);
                    String line2 = "FITTEST: " + fittest.toString();
                    Rectangle2D bounds2 = metrics1.getStringBounds(line2, null);  
                    int line2width = (int) bounds2.getWidth();
                    g.drawString(line2, (frameWidth/2)-(line2width/2), 100);
                    double roundedDistance = Math.round(fittest.getTourDistance()*100.0)/100.0;
                    String line3 = "DISTANCE: " + roundedDistance;
                    Rectangle2D bounds3 = metrics1.getStringBounds(line3, null);  
                    int line3width = (int) bounds3.getWidth();
                    g.drawString(line3, (frameWidth/2)-(line3width/2), 150);
                    
                } else{
                    // Set new font
                    g.setFont(f2);
                    // Paint circles representing cities, and associated references as labels
                    if(coordinates.size() > 0){
                        for(int i=0; i<AllCities.getCities().size(); i++){
                            double[] coords = coordinates.get(i);
                            Graphics2D g2d = (Graphics2D)g;
                            // Assume x, y, and diameter are instance variables.
                            Ellipse2D.Double circle = new Ellipse2D.Double(coords[0]-(townCircleDiameter/2), coords[1]-(townCircleDiameter/2), townCircleDiameter, townCircleDiameter);
                            g2d.fill(circle);
                            g.drawString(AllCities.getCities().get(i).getReference(), (int)Math.round(coords[0]-4), (int)Math.round(coords[1]+30));
                        }
                    }
                }
            }
        }
    }
    
    // Mouse Listener for user-defined cities (by clicking on JPanel)
    public class MouseAdapter implements MouseListener{
        public void mousePressed( MouseEvent e ) {     
            PointerInfo a = MouseInfo.getPointerInfo();
            Point point = new Point(a.getLocation());
            SwingUtilities.convertPointFromScreen(point, e.getComponent()); // gets mouse coordinates relative to component, not screen
            int x = (int)((point.getX()-4));
            int y = (int)((point.getY()-26));
            mouse[0] = x;
            mouse[1] = y;
            double[] coords = new double[2];
            coords[0] = x;
            coords[1] = y;
            coordinates.add(coords);
            int ref = AllCities.getCities().size()+1;
            AllCities.addCity(new City("" + ref, x, y));
            updateGUI();
        }

        public void mouseReleased(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}

        public void mouseEntered(MouseEvent e) {}

        public void mouseClicked(MouseEvent e) {}
    }
}
