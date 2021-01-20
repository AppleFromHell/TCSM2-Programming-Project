package dt.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Emiel Rous and Wouter Koning */
public class DistributedRandomNumberGenerator {

    private Map<Integer, Double> distribution;
    private double totalDistribution;

    public DistributedRandomNumberGenerator(){
        distribution = new HashMap<>();
        this.totalDistribution = 0d;
    }

    /**
     * Add a number with an associated distribution, so that later on a random number can be generated using these
     * distributions using {@link DistributedRandomNumberGenerator#getRandomNumber()}.
     * @param number The number that is put into this random number generator
     * @param distribution The associated distribution of the number put into this random number generator.
     */
    public void addNumber(int number, double distribution){
        // If it already exists, subtract the current distribution from the total
        if(this.distribution.get(number) != null){
            this.totalDistribution -= this.distribution.get(number);
        }
        this.distribution.put(number, distribution); //A dd the number and the distribution to the Map
        this.totalDistribution += distribution; // Update the total distribution
    }

    /**
     * Find a random number based upon the distribution of the numbers put into this random number generator. A random
     * number is generated and then by iterating over the values put into this class, a value is returned that is put
     * into this function.
     * @return A random number based upon distributions of values when it was put into this class.
     */
    public int getRandomNumber() {
        double randomNr = Math.random();
        double tempDistribution = 0d;
        for(Integer i : distribution.keySet()){
            tempDistribution += distribution.get(i);
            if(randomNr * totalDistribution <= tempDistribution){
                return i;
            }
        }
        return 0;
    }

    /**
     * Uses the method {@link DistributedRandomNumberGenerator#getRandomNumber()}, but it does not return any of the
     * integer values given in the list of exceptions passed on to this method.
     * @param exceptions A list of integers which are not to be generated.
     * @return A random number that is put in the random number generator, but is not one of the integers that is given
     * as a parameter to this method.
     */
    public int getRandomNumber(List<Integer> exceptions){
        Map<Integer, Double> copyDistribution = this.distributionDeepCopy();
        double copyTotal = this.totalDistribution;
        for(Integer exception : exceptions){ //Remove all of the keys you do not want to have.
            if(this.distribution.get(exception) != null) { // It exists
                this.totalDistribution -= this.distribution.get(exception);
                this.distribution.remove(exception);
            }
        }

        int randomNumber = getRandomNumber();

        this.distribution = copyDistribution;
        this.totalDistribution = copyTotal;

        return randomNumber;
    }

    /**
     * Create a deep copy of the distribution {@link Map} that this class uses.
     * @return a deep copy of the distribution {@link Map}.
     */
    public Map<Integer, Double> distributionDeepCopy(){
        Map<Integer, Double> copy = new HashMap<>();
        for(Map.Entry<Integer, Double> entry : this.distribution.entrySet()){
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public Map<Integer, Double> getDistribution(){
        return this.distribution;
    }

}
