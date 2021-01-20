package serverTests;

import dt.util.DistributedRandomNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


import java.util.Map;

public class DistributedRandomNumberGeneratorTest {

    DistributedRandomNumberGenerator generator = new DistributedRandomNumberGenerator();

    @Test
    public void testDistributionDeepCopy(){
        generator.addNumber(1, 2);
        generator.addNumber(2, 3);
        generator.addNumber(3, 4);
        Map<Integer, Double> copy = generator.distributionDeepCopy();
        Map<Integer, Double> original = generator.getDistribution();

        assertFalse(copy == original);
        assertTrue(copy.equals(original));
    }
}
