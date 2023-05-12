package Backend.Algorithm;

import Backend.Algorithm.Reader.Channel;
import Backend.Helper.PrintHelper;
import java.io.IOException;

/**
 * @author Ethan Carnahan
 * Basic sound analysis that analyzes the average frequency balance and dynamics info of a song.
 * How to use: Pass in a Transform object and duration, and call get methods for volume/dynamics information.
 */
public class SimpleCharacteristics {
  //region Fields and public methods
  // Average volume of each frequency bin.
  private final double[] averageLeftVolume, averageRightVolume;
  // Average rate of volume change per second for each frequency bin.
  private final double[] averageLeftVolumeChange, averageRightVolumeChange;
  // Average peak/dip ratio of each frequency bin.
  private final double[] averageLeftPeakRatio, averageRightPeakRatio;
  // Average peak/dip rate of each frequency bin.
  private final double[] averageLeftPeakRate, averageRightPeakRate;

  public SimpleCharacteristics(Transform transform, double duration) {
    float[][] left = Normalizer.normalizeTransform(transform.getFrequencyAmplitudes(Channel.LEFT));
    float[][] right = Normalizer.normalizeTransform(transform.getFrequencyAmplitudes(Channel.RIGHT));

    averageLeftVolume = calculateVolume(left);
    averageLeftVolumeChange = calculateVolumeChange(left, duration);
    double[][] averageLeftPeakInfo = calculatePeakInfo(left, duration);
    averageLeftPeakRatio = averageLeftPeakInfo[0];
    averageLeftPeakRate = averageLeftPeakInfo[1];
    if (right != null) {
      averageRightVolume = calculateVolume(right);
      averageRightVolumeChange = calculateVolumeChange(right, duration);
      double[][] averageRightPeakInfo = calculatePeakInfo(right, duration);
      averageRightPeakRatio = averageRightPeakInfo[0];
      averageRightPeakRate = averageRightPeakInfo[1];
    } else {
      averageRightVolume = null;
      averageRightVolumeChange = null;
      averageRightPeakRatio = null;
      averageRightPeakRate = null;
    }
  }

  public double[] getAverageVolume(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftVolume : averageRightVolume;
  }

  public double[] getAverageVolumeChange(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftVolumeChange : averageRightVolumeChange;
  }

  public double[] getAveragePeakRatio(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftPeakRatio : averageRightPeakRatio;
  }

  public double[] getAveragePeakRate(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftPeakRate : averageRightPeakRate;
  }
  //endregion

  //region Private methods

  private static double[] calculateVolume(float[][] channel) {
    double[] result = new double[channel[0].length];

    // for each frequency
    for (int j = 0; j < result.length; j++) {
      result[j] = 0;

      // for each time
      for (float[] sample : channel) {
        result[j] += sample[j];
      }

      result[j] /= channel.length;
    }

    return result;
  }

  private static double[] calculateVolumeChange(float[][] channel, double duration) {
    double[] result = new double[channel[0].length];

    // for each frequency
    for (int i = 0; i < channel[0].length; i++) {
      result[i] = 0.0f;

      // for each time
      for (int j = 1; j < channel.length; j++) {
        result[i] += Math.abs(channel[j][i] - channel[j-1][i]);
      }

      result[i] /= (duration * Transform.TIME_RESOLUTION);
    }

    return result;
  }

  // result[0] = peak differences, result[1] = peak rates.
  private static double[][] calculatePeakInfo(float[][] channel, double duration) {
    double[][] result = new double[2][channel[0].length];

    // for each frequency
    for (int i = 0; i < channel[0].length; i++) {
      result[0][i] = 0.0f;

      int peakCount0 = 0, peakCount1 = 0;
      float peakValue = channel[0][i];
      // for each time
      for (int j = 1; j < channel.length-1; j++) {
        // peak
        if (channel[j][i] > channel[j-1][i] && channel[j][i] > channel[j+1][i]) {
          if (peakValue > 0.0f) {
            result[0][i] += channel[j][i] / peakValue;
            peakCount0++;
          }
          peakValue = channel[j][i];
          peakCount1++;
        }
        // dip
        if (channel[j][i] < channel[j-1][i] && channel[j][i] < channel[j+1][i]) {
          if (channel[j][i] > 0.0f) {
            result[0][i] += peakValue / channel[j][i];
            peakCount0++;
          }
          peakValue = channel[j][i];
          peakCount1++;
        }
      }

      result[0][i] /= peakCount0;
      result[1][i] = peakCount1 / duration;
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
      SimpleCharacteristics simpleCharacteristics = new SimpleCharacteristics(transform,
          reader.getDuration());
      System.out.println("Calculation time: " + ((System.nanoTime() - startTime) / 1000000000.0) + " seconds");

      System.out.println("Left channel characteristics:");
      double[] leftVolume = simpleCharacteristics.getAverageVolume(Channel.LEFT);
      double[] leftVolumeChange = simpleCharacteristics.getAverageVolumeChange(Channel.LEFT);
      double[] leftRatios = simpleCharacteristics.getAveragePeakRatio(Channel.LEFT);
      double[] leftRates = simpleCharacteristics.getAveragePeakRate(Channel.LEFT);

      PrintHelper.printFrequencies();
      PrintHelper.printValues("Loudness", leftVolume);
      PrintHelper.printValues("Vol. Change", leftVolumeChange);
      PrintHelper.printValues("Peak Ratio", leftRatios);
      PrintHelper.printValues("Peak Rate", leftRates);

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
