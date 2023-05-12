package Backend.Algorithm;

import Backend.Algorithm.Reader.Channel;
import Backend.Helper.PrintHelper;
import java.io.IOException;

/**
 * @author Ethan Carnahan
 * Basic sound analysis that calculates the perceived frequency balance and dynamics of a song.
 * How to use: Pass in a Transform object and duration, and call get methods for volume/dynamics information.
 * For now, more dynamic means the rate of change in volume increases as a sound gets louder.
 * In the future, more dynamic will mean the bigger peaks in volume are narrower in time.
 */
public class SimpleCharacteristics {
  //region Fields and public methods
  // Average volume of each frequency bin.
  private final double[] averageLeftVolume, averageRightVolume;
  // Average rate of volume change per second for each frequency bin.
  private final double[] averageLeftDynamicRating, averageRightDynamicRating;
  private static final double globalDynamicMultiplier = 0.001;

  public SimpleCharacteristics(Transform transform) {
    float[][] left = Normalizer.normalizeTransform(transform.getFrequencyAmplitudes(Channel.LEFT));
    float[][] right = Normalizer.normalizeTransform(transform.getFrequencyAmplitudes(Channel.RIGHT));

    System.out.println("SimpleCharacteristics: Calculating characteristics");

    averageLeftVolume = calculateVolume(left);
    averageLeftDynamicRating = calculateDynamicRating(left);
    if (right != null) {
      averageRightVolume = calculateVolume(right);
      averageRightDynamicRating = calculateDynamicRating(right);
    } else {
      averageRightVolume = null;
      averageRightDynamicRating = null;
    }
  }

  public double[] getAverageVolume(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftVolume : averageRightVolume;
  }

  public double[] getDynamicRating(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftDynamicRating : averageRightDynamicRating;
  }
  //endregion

  //region Private methods
  private static double[] calculateVolume(float[][] channel) {
    double[] result = new double[channel[0].length];

    // for each frequency
    for (int i = 0; i < result.length; i++) {
      result[i] = 0;

      // for each time
      for (float[] sample : channel) {
        result[i] += sample[i];
      }

      result[i] /= channel.length;
    }

    return result;
  }

  private static double[] calculateDynamicRating(float[][] channel) {
    double[] result = new double[channel[0].length];

    // for each frequency
    for (int j = 0; j < result.length; j++) {
      result[j] = 0;

      // for each time
      for (int i = 1; i < channel.length; i++) {
        result[j] += channel[i - 1][j] * Math.abs(channel[i][j] - channel[i - 1][j]);
      }

      result[j] *= globalDynamicMultiplier / channel.length;
    }

    return result;
  }
  //endregion

  // Prints the average volume/DR information of the audio file in args[0].
  public static void main(String[] args) {
    try {
      Reader reader = new Reader(args[0]);
      Transform transform = new Transform(reader);
      long startTime = System.nanoTime();
      SimpleCharacteristics simpleCharacteristics = new SimpleCharacteristics(transform);
      System.out.println("Calculation time: " + ((System.nanoTime() - startTime) / 1000000000.0) + " seconds");

      System.out.println("Left channel characteristics:");
      double[] leftVolume = simpleCharacteristics.getAverageVolume(Channel.LEFT);
      double[] leftDR = simpleCharacteristics.getDynamicRating(Channel.LEFT);

      PrintHelper.printFrequencies();
      PrintHelper.printValues("Loudness", leftVolume);
      PrintHelper.printValues("Dynamics", leftDR);

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
