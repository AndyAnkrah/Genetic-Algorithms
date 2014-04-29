import java.util.ArrayList;
import java.awt.*;

public class AllObstacles
{
    private static ArrayList<Obstacle> allObstacles = new ArrayList<Obstacle>(); // stores a static arraylist of all obstacles
    private static final double interiorOffset = 0.1; // how close a path can be to touching the vertices of the obstacles
    
    // Add obstacles to map (can be modified for whichever obstacles are desired)
    public static void addObstacles(){
        generateRandomRectangles(200, true);
        addObstacle(new Obstacle("a", new int[]{20,20,70,70}, new int[]{190,150,150,190}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("b", new int[]{10,10,40,40}, new int[]{140,90,90,140}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("c", new int[]{25,25,40,40}, new int[]{80,45,45,80}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("d", new int[]{180,180,195,195}, new int[]{170,160,160,170}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("e", new int[]{10,10,45,45}, new int[]{40,30,30,40}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("f", new int[]{55,55,60,60}, new int[]{50,10,10,55}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("g", new int[]{50,50,90,90}, new int[]{105,75,75,105}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("h", new int[]{55,55,180,180}, new int[]{140,120,120,140}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("i", new int[]{75,75,130,130}, new int[]{30,5,5,30}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("j", new int[]{75,75,90,90}, new int[]{60,50,50,60}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("k", new int[]{95,95,105,105}, new int[]{100,45,45,100}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("l", new int[]{125,125,145,145}, new int[]{95,50,50,95}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("m", new int[]{140,140,180,180}, new int[]{35,15,15,35}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("n", new int[]{155,155,190,190}, new int[]{155,50,50,155}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("o", new int[]{80,80,105,105}, new int[]{170,160,160,170}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("p", new int[]{85,85,145,145}, new int[]{190,185,185,190}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("q", new int[]{115,115,130,130}, new int[]{180,150,150,180}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("r", new int[]{140,140,170,170}, new int[]{170,160,160,170}, interiorOffset, -1, -1));
        addObstacle(new Obstacle("s", new int[]{0,0,15,15}, new int[]{200,195,195,200}, interiorOffset, -1, -1));
    }
    
    // Generates the specified number of random polygonal obstacles (regular if boolean set to true)
    public static void generateRandomObstacles(int howMany, boolean regular){
        int minRange = 5; // Minimum edge length
        int maxRange = 10; // Maximum edge length
        int minPoints = 3; // Minimum number of vertices
        int maxPoints = 10; // Maximum number of vertices
        for(int i=0; i<howMany; i++){
            Obstacle o = null;
            boolean generateNew = true;
            while(generateNew){
                generateNew = false;
                int randPoints = (int)Math.round(((Math.random()*(maxPoints-minPoints))+minPoints));
                int[] x = new int[randPoints];
                int[] y = new int[randPoints];
                if(regular){
                    int radius = (int)Math.round(((Math.random()*(maxRange-minRange))+minRange));
                    int centreX = (int)Math.round(((Math.random()*(200.0-(radius*2)))+radius));
                    int centreY = (int)Math.round(((Math.random()*(200.0-(radius*2)))+radius));
                    double startRotation = Math.random()*(2.0 * Math.PI);
                    for(int j=0; j<randPoints; j++){
                        // Calculate coordinates for regular polygon
                        double theta = startRotation+((2.0 * Math.PI * j)/(double)randPoints);
                        double xPos = (double)radius*Math.cos(theta);
                        double yPos = (double)radius*Math.sin(theta);
                        x[j] = centreX + (int)Math.round(xPos);
                        y[j] = centreY - (int)Math.round(yPos);
                    }
                    // Create a new obstacle object with centre calculated above
                    o = new Obstacle("" + i, x, y, interiorOffset, centreX, centreY);
                } else{
                    for(int j=0; j<randPoints; j++){
                        if(j==0){
                            x[0] = (int)Math.round(((Math.random()*(200.0-(maxRange*2)))+maxRange));
                            y[0] = (int)Math.round(((Math.random()*(200.0-(maxRange*2)))+maxRange));
                        } else{
                            x[j] = (int)Math.round(((Math.random()*(maxRange*2))+x[0]-maxRange));
                            y[j] = (int)Math.round(((Math.random()*(maxRange*2))+y[0]-maxRange));
                        }
                    }
                    // Create a new obstacle object with centre < (0,0) which triggers separate calculation in Obstacles class
                    o = new Obstacle("" + i, x, y, interiorOffset, -1, -1);
                }
                for(City c : AllCities.getPoints()){
                    // Generate new coordinates if on top of a city so as not to engulf it
                    if(o.contains(c.getX(), c.getY())){
                        generateNew = true;
                        break;
                    }
                }
            }
            addObstacle(o);
        }
    }
    
    // Generates the specified number of rectangles (squares if boolean set to true)
    public static void generateRandomRectangles(int howMany, boolean regular){
        for(int i=0; i<howMany; i++){
            int minSize = 5; // Minimum edge length
            int maxSize = 10; // Maximum edge length
            int width = (int)((Math.random()*(maxSize-minSize))+minSize);
            int height;
            if(regular){
                height = width;
            } else{
                height = (int)((Math.random()*(maxSize-minSize))+minSize);
            }
            boolean generateNew = true;
            int randX = (int)(Math.random()*(200.0-width));
            int randY = (int)(Math.random()*(200.0-height));
            Obstacle o = null;
            while(generateNew){
                generateNew = false;
                randX = (int)(Math.random()*190.0);
                randY = (int)(Math.random()*200.0);
                int[] x = new int[4];
                int[] y = new int[4];
                x[0] = randX;
                y[0] = randY;
                x[1] = x[0];
                y[1] = y[0] + height;
                x[2] = x[0] + width;
                y[2] = y[1];
                x[3] = x[2];
                y[3] = y[0];
                // Create a new obstacle object with calculated centre
                o = new Obstacle("" + i, x, y, interiorOffset, (randX+(randX+width))/2, (randY+(randY+height))/2);
                for(City c : AllCities.getPoints()){
                    // Generate new coordinates if on top of a city
                    if(o.contains(c.getX(), c.getY())){    
                        generateNew = true;
                        break;
                    }
                }
            }
            addObstacle(o);
        }
    }
    
    // Adds obstacle to the arraylist, and adds cities to represent its vertices
    public static void addObstacle(Obstacle o){
        double xOffset = 0.0;
        double yOffset = 0.0;
        //Offset cities from obstacle vertices so they do not intersect automatically
        for(int i=0; i<o.npoints; i++){
            int xdiff = o.xpoints[i] - o.getCentreX();
            int ydiff = o.ypoints[i] - o.getCentreY();
            if(xdiff > 0){
                xOffset = o.getOffset();
            } else{
                xOffset = -o.getOffset();
            }
            if(ydiff > 0){
                yOffset = o.getOffset();
            } else{
                yOffset = -o.getOffset();
            }
            // Add cities representing the vertices of each obstacle
            AllCities.addCity(new City(o.getReference() + "." + i, o.xpoints[i] + xOffset, o.ypoints[i] + yOffset, true));
        }
        // Add the obstacle to the arraylist
        allObstacles.add(o);
    }
    
    // Get all of the obstacles as an arraylist
    public static ArrayList<Obstacle> getObstacles(){
        return allObstacles;
    }
    
    // Returns true if the straight line between the two specified cities hits any of the obstacles
    public static boolean hitsAnyObstacle(City from, City to){
        for(Obstacle o : allObstacles){
            // Return true if path between cities hits obstacle o
            if(o.hitsObstacle(from, to)){
                return true;
            }
        }
        return false;
    }
    
}
