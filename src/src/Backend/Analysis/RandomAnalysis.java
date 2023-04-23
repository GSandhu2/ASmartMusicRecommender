package Backend.Analysis;

/**
 * @author Ethan Carnahan
 * Toy SoundAnalysis that just returns a random number when comparing two songs.
 */
public class RandomAnalysis implements SoundAnalysis {

  @Override
  public double compareTo(SoundAnalysis other) {
      if (!(other instanceof RandomAnalysis)) {
          throw new IllegalArgumentException("Incompatible sound analysis types.");
      }

    return Math.random();
  }
}
