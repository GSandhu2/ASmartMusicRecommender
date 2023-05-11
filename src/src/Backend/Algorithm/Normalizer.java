package Backend.Algorithm;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author Ethan Carnahan
 * Sets a sound to a specified average volume, and then
 * converts fourier transform amplitudes from actual loudness to perceived loudness.
 * Perceived loudness is arbitrarily set so 0 phons = 100 loudness.
 */
public class Normalizer {
  //region Constants and public method
  // A full amplitude sine wave will be treated as this volume.
  private static final double dbOfMax = 90;
  // The normalizer will try to set a fourier transform to this perceived volume +- errorBound.
  private static final double targetVolume = 25600; // 80 phons
  private static final double errorBound = 1, ratioMultiplier = 1;//, spreadDamp = 1;

  public static double[] normalize(double[] averageVolume) {
    // Copy array
    double[] result = new double[averageVolume.length];
    System.arraycopy(averageVolume, 0, result, 0, averageVolume.length);

    // Do nothing for silence
    double currentVolume = getOverallVolume(result);
    if (currentVolume == 0)
      return result;

    // Find correct volume
    while (Math.abs(currentVolume - targetVolume) > errorBound) {
      double multiplier = 1 + (((targetVolume / currentVolume) - 1) * ratioMultiplier);
      multiplyArray(result, multiplier);
      currentVolume = getOverallVolume(result);
    }

    // Convert to perceived loudness
    return loudnessToPerceivedLoudness(result);
  }
  //endregion

  //region Private methods
  private static double getOverallVolume(double[] averageVolume) {
    double sum = 0;
    double[] perceivedLoudness = loudnessToPerceivedLoudness(averageVolume);
    for (double loudness : perceivedLoudness) {
      sum += loudness;
    }

    // The more spread out the amplitudes are, the more dependent the result is on the number of bins.
    /*double average = sum / perceivedLoudness.length;
    double spread = 0;
    for (double loudness : perceivedLoudness)
        spread += (loudness - average) * (loudness - average);
    spread /= (averageVolume.length * average);

    if (spread != 0)
      return sum / (1 + (Transform.FREQUENCY_RESOLUTION * spread * spreadDamp));
    else*/
      return sum;
  }

  private static double loudnessToDb(double loudness) {
    return dbOfMax + (10 * Math.log10(loudness / Short.MAX_VALUE));
  }

  private static double phonsToLoudness(double phons) {
    return 100 * Math.pow(2, phons / 10);
  }

  // Convert loudness to perceived loudness.
  private static double[] loudnessToPerceivedLoudness(double[] loudness) {
    double[] result = new double[loudness.length];

    for (int i = 0; i < result.length; i++) {
      if (loudness[i] == 0.0) {
        result[i] = 0.0;
      } else {
        double db = loudnessToDb(loudness[i]);
        double frequency = Transform.frequencyAtBin(i);
        double phons = EqualLoudness.dbToPhons(db, frequency);
        result[i] = phonsToLoudness(phons);
      }
    }

    return result;
  }

  private static void multiplyArray(double[] array, double multiplier) {
    for (int i = 0; i < array.length; i++)
      array[i] *= multiplier;
  }
  //endregion

  // Test the effects of normalization on a perfectly flat frequency response.
  // Changing testVolume should have no effect. Changing targetVolume should.
  public static void main(String[] args) {
    DecimalFormat format = new DecimalFormat("#####.00");
    double testVolume = 50;

    double[] volume = new double[Transform.FREQUENCY_RESOLUTION];
    Arrays.fill(volume, testVolume);
    double[] loudness = normalize(volume);

    System.out.print("Frequencies:");
    for (int i = 0; i <= Transform.FREQUENCY_RESOLUTION; i++) {
      System.out.print(" " + String.format("%8s", format.format(
          Transform.BOTTOM_FREQUENCY * Math.pow(Transform.TOP_BOTTOM_RATIO,
              (double) i / Transform.FREQUENCY_RESOLUTION))));
    }
    System.out.println();

    System.out.print("   Loudness:");
    for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i++) {
      System.out.print(" " + String.format("%8s", format.format(
          loudness[i]
      )));
    }
    System.out.println();
  }
}