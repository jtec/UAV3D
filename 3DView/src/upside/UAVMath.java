package upside;

/**
 *
 * @author Jan
 */
public class UAVMath {

    static float saturate(float value, float lowerBound, float upperBound) {
        if (value > upperBound) {
            return upperBound;
        } else if (value < lowerBound) {
            return lowerBound;
        } else {
            return value;
        }
    }    
}
