package Backend.Algorithm;

import Backend.Helper.PrintHelper;
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
  private static final double targetVolume = 256; // 80 phons
  private static final double errorBound = 0.001, ratioMultiplier = 2;//, spreadDamp = 1;

  public static float[][] normalizeTransform(float[][] transform) {
    // Check null
    if (transform == null)
      return null;

    System.out.println("Normalizer: Running normalization on transform of " + transform.length + " samples");

    // Copy array
    float[][] result = new float[transform.length][transform[0].length];
    for (int i = 0; i < transform.length; i++)
      System.arraycopy(transform[i], 0, result[i], 0, transform[0].length);

    // Do nothing for silence
    double currentVolume = getOverallVolume(transform);
    if (currentVolume == 0)
      return result;

    // Find correct volume
    while (Math.abs(currentVolume - targetVolume) > errorBound) {
      float multiplier = (float) (1 + (((targetVolume / currentVolume) - 1) * ratioMultiplier));
      multiply2DArray(result, multiplier);
      currentVolume = getOverallVolume(result);
    }

    return loudnessToPerceivedLoudness(result);
  }

  public static double[] normalizeAverage(double[] averageVolume) {
    // Copy array
    double[] result = new double[averageVolume.length];
    System.arraycopy(averageVolume, 0, result, 0, averageVolume.length);

    // Do nothing for silence
    double currentVolume = getSampleVolume(result);
    if (currentVolume == 0)
      return result;

    // Find correct volume
    while (Math.abs(currentVolume - targetVolume) > errorBound) {
      double multiplier = 1 + (((targetVolume / currentVolume) - 1) * ratioMultiplier);
      multiplyArray(result, multiplier);
      currentVolume = getSampleVolume(result);
    }

    // Convert to perceived loudness
    return loudnessToPerceivedLoudness(result);
  }
  //endregion

  //region Private methods
  private static double getOverallVolume(float[][] transform) {
    double sum = 0.0;

    for (float[] sample : transform) {
      double sampleSum = 0.0;
      double[] perceivedLoudness = loudnessToPerceivedLoudness(sample);
      for (double loudness : perceivedLoudness)
        sampleSum += loudness;
      sum += sampleSum / Transform.FREQUENCY_RESOLUTION;
    }

    return sum / transform.length;
  }

  private static double getSampleVolume(double[] averageVolume) {
    double sum = 0.0;
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
    else
      return sum;*/
    return sum / (Transform.FREQUENCY_RESOLUTION);
  }

  private static double loudnessToDb(double loudness) {
    return dbOfMax + (20 * Math.log10(loudness / Short.MAX_VALUE));
  }

  private static double phonsToLoudness(double phons) {
    return Math.pow(2, phons / 10);
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

  private static double[] loudnessToPerceivedLoudness(float[] loudness) {
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

  private static float[][] loudnessToPerceivedLoudness(float[][] transform) {
    float[][] result = new float[transform.length][transform[0].length];

    for (int i = 0; i < result.length; i++) {
      for (int j = 0; j < result[0].length; j++) {
        if (transform[i][j] == 0.0) {
          result[i][j] = 0.0f;
        } else {
          double db = loudnessToDb(transform[i][j]);
          double frequency = Transform.frequencyAtBin(j);
          double phons = EqualLoudness.dbToPhons(db, frequency);
          result[i][j] = (float)phonsToLoudness(phons);
        }
      }
    }

    return result;
  }

  private static void multiplyArray(double[] array, double multiplier) {
    for (int i = 0; i < array.length; i++)
      array[i] *= multiplier;
  }

  private static void multiply2DArray(float[][] array, float multiplier) {
    for (int i = 0; i < array.length; i++)
      for (int j = 0; j < array[0].length; j++)
        array[i][j] *= multiplier;
  }
  //endregion

  // Test the effects of normalization on a perfectly flat frequency response.
  // Changing testVolume should have no effect. Changing targetVolume should.
  public static void main(String[] args) {
    double testVolume = Short.MAX_VALUE;
    double[] volume = new double[Transform.FREQUENCY_RESOLUTION];
    Arrays.fill(volume, testVolume);

    long startTime = System.nanoTime();
    double[] loudness = normalizeAverage(volume);
    System.out.println("Calculation time: " + ((System.nanoTime() - startTime) / 1000000000.0) + " seconds");

    PrintHelper.printFrequencies();
    PrintHelper.printValues("Loudness", loudness);
  }
}