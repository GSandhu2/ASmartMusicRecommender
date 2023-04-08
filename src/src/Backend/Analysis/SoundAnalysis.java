package Backend.Analysis;

/**
 * @author Ethan Carnahan
 * Container for all SoundAnalysis types.
 */
public interface SoundAnalysis {
    /**
     * Compares two songs using the analysis of each of them.
     * @param other The song to compare <code>this<code/> to.
     * @throws IllegalArgumentException if <code>other<code/> does not extend <code>this<code/>.
     * @return A match value from 0.0 to 1.0 inclusive, based on how similar <code>this<code/> is to <code>other<code/> according to the SoundAnalysis type.
     */
    double compareTo(SoundAnalysis other);
}
