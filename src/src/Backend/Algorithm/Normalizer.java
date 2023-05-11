package Backend.Algorithm;

/**
 * @author Ethan Carnahan
 * Sets a sound to a specified average volume, and then
 * converts fourier transform amplitudes from actual loudness to perceived loudness.
 * Perceived loudness is arbitrarily set so 0 phons = 1 loudness.
 */
public class Normalizer {
  //region Constants and public method
  // A full amplitude sine wave will be treated as this volume.
  private static final double dbOfMax = 90;
  // The normalizer will try to set a fourier transform to this perceived volume +- errorBound.
  private static final double targetVolume = 128; // 70 phons
  private static final double errorBound = 0.1, ratioMultiplier = 1, spreadDamp = 1;

  public static double[] normalize(double[] averageVolume) {
    // Copy array
    double[] result = new double[averageVolume.length];
    System.arraycopy(averageVolume, 0, result, 0, averageVolume.length);

    // Do nothing for silence.
    double currentVolume = getOverallVolume(result);
    if (currentVolume == 0)
      return result;

    // Find correct volume
    while (Math.abs(currentVolume - targetVolume) > errorBound) {
      System.out.println("Volume: " + currentVolume);
      double multiplier = 1 + (((targetVolume / currentVolume) - 1) * ratioMultiplier);
      multiplyArray(result, multiplier);
      currentVolume = getOverallVolume(result);
    }

    // Convert loudness to perceived loudness.
    for (int i = 0; i < result.length; i++) {
      double frequency = Transform.frequencyAtBin(i);
      double db = loudnessToDb(result[i]);
      double phons = EqualLoudness.dbToPhons(db, frequency);
      result[i] = phonsToLoudness(phons);
    }

    return result;
  }
  //endregion

  //region Private methods
  private static double getOverallVolume(double[] averageVolume) {
    double sum = 0;
    for (int i = 0; i < averageVolume.length; i++) {
      double frequency = Transform.frequencyAtBin(i);
      if (averageVolume[i] > 0) {
        double db = loudnessToDb(averageVolume[i]);
        double phons = EqualLoudness.dbToPhons(db, frequency);
        sum += phonsToLoudness(phons);
      }
    }

    // The more spread out the amplitudes are, the more dependent the result is on the number of bins.
    double average = sum / averageVolume.length;
    double spread = 0;
    for (int i = 0; i < averageVolume.length; i++) {
      double frequency = Transform.frequencyAtBin(i);
      if (averageVolume[i] > 0) {
        double db = loudnessToDb(averageVolume[i]);
        double phons = EqualLoudness.dbToPhons(db, frequency);
        double loudness = phonsToLoudness(phons);
        spread += (loudness - average) * (loudness - average);
      }
    }
    spread /= (averageVolume.length * average);
    System.out.println("Spread: " + spread);

    //if (spread != 0)
      //return sum / (averageVolume.length * spread * spreadDamp);
    //else
      return sum;
  }

  private static double loudnessToDb(double loudness) {
    return dbOfMax + (20 * Math.log10(loudness / Short.MAX_VALUE));
  }

  private static double phonsToLoudness(double phons) {
    return Math.pow(2, phons / 10);
  }

  private static void multiplyArray(double[] array, double multiplier) {
    for (int i = 0; i < array.length; i++)
      array[i] *= multiplier;
  }
  //endregion
}
