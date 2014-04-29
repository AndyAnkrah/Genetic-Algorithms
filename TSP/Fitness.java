public class Fitness
{
    // Can include other desired parameters in fitness function
    // For this purpose, we just need the reciprocal of a tour's distance
    public static double getFitness(Tour t){
        return 1.0/t.getTourDistance();
    }
}
