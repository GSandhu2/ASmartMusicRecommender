package Backend.Algorithm;

import Backend.Algorithm.Reader.Channel;
import Backend.Helper.PrintHelper;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Ethan Carnahan
 * Advanced sound analysis that analyzes:
 * - How a frequency bin's volume changes correlates with the volume changes of other bins over time.
 * - The best-matching frequencies of each bin's peaks (BPM).
 */
public class TemporalCharacteristics extends SimpleCharacteristics {
  //region Fields and public methods
  // Dimensions from left-to-right: [This bin][Bin compared to][Number of samples ahead]
  private final double[][][] leftCorrelaton, rightCorrelation;
  // Dimensions from left-to-right: [This bin][Possible peak frequency]
  private final double[][] leftPeakRates, rightPeakRates;
  // Number of samples to look ahead.
  private static final int CORRELATION_SAMPLES = (int)Math.round(Transform.TIME_RESOLUTION * 2); // From 0 to 2 seconds ahead
  // Which peak rates to check in beats-per-minute.
  private static final int RATE_MIN = 30;
  private static final int RATE_MAX = 600; // Needs to be less than (60 * Transform.TIME_RESOLUTION / 3).

  public TemporalCharacteristics(Normalizer normalizer) {
    super(normalizer);
    float[][] left = normalizer.getNormalized(Channel.LEFT);
    float[][] right = normalizer.getNormalized(Channel.RIGHT);

    System.out.println("TemporalCharacteristics: Calculating characteristics");

    leftCorrelaton = calculateCorrelation(left, getAverageVolume(Channel.LEFT));
    leftPeakRates = calculatePeakRates(left, getAverageVolume(Channel.LEFT));
    if (right != null) {
      rightCorrelation = calculateCorrelation(right, getAverageVolume(Channel.RIGHT));
      rightPeakRates = calculatePeakRates(right, getAverageVolume(Channel.RIGHT));
    } else {
      rightCorrelation = null;
      rightPeakRates = null;
    }
  }

  public double[][][] getCorrelation(Channel channel) {
    return (channel == Channel.LEFT ? leftCorrelaton : rightCorrelation);
  }

  public double[][] getPeakRates(Channel channel) {
    return (channel == Channel.LEFT ? leftPeakRates : rightPeakRates);
  }
  //endregion

  //region Private methods
  private static double[][][] calculateCorrelation(float[][] channel, double[] averageVolume) {
    double[][][] result = new double[Transform.FREQUENCY_RESOLUTION][Transform.FREQUENCY_RESOLUTION][CORRELATION_SAMPLES];
    for (int i = 0; i < result.length; i++)
      for (int j = 0; j < result[0].length; j++)
        for (int k = 0; k < result[0][0].length; k++)
          result[i][j][k] = correlation(channel, i, j, k, averageVolume);
    return result;
  }

  // Correlation = sum(bin A volume change * bin B volume change).
  private static double correlation(float[][] channel, int binA, int binB, int samplesAhead, double[] averageVolume) {
    double result = 0.0;

    for (int i = 1; i < channel.length - samplesAhead; i++) {
      double a = channel[i][binA] - channel[i - 1][binA];
      double b = channel[i + samplesAhead][binB] - channel[i + samplesAhead - 1][binB];
      result += a * b;
    }

    return result / ((channel.length - samplesAhead - 1) * averageVolume[binA]);
  }

  private static double[][] calculatePeakRates(float[][] channel, double[] averageVolume) {
    double[][] result = new double[Transform.FREQUENCY_RESOLUTION][RATE_MAX - RATE_MIN + 1];
    for (int i = 0; i < result.length; i++)
      for (int j = 0; j < result[0].length; j++)
        result[i][j] = peakRateMatch(channel, i, RATE_MIN + j, averageVolume);
    return result;
  }

  // Peak rate strategy: Use lots of interpolation.
  // Generate a peak detection window for each BPM, with {-1.0, 1.0, 1.0, -1.0} being the smallest possible window.
  // Slide window across channel to generate "is peak" values for each sample.
  // Iterate across "is peak" array by different amounts for each BPM to calculate match amount.
  private static double peakRateMatch(float[][] channel, int bin, int rate, double[] averageVolume) {
    double result = 0.0;
    double samplesPerBeat = Transform.TIME_RESOLUTION / (rate / 60.0);

    double[] window = getWindow(rate);
    double[] peaks = getPeaks(channel, bin, window);

    for (int i = 0; i < window.length; i++) {
      double loopResult = 0.0;
      for (double j = i; j < channel.length - 1; j += samplesPerBeat)
        loopResult += interpolate(peaks, j);
      result = Math.max(result, loopResult);
    }

    return result * samplesPerBeat / (channel.length * averageVolume[bin]);
  }

  private static double[] getWindow(int rate) {
    // convert rate from BPM to minimum required odd window length
    int windowLength = (int)Math.ceil(Transform.TIME_RESOLUTION / (rate / 60.0));
    if (windowLength % 2 == 1) windowLength++;

    // calculate window values
    double negativeValues = -2.0 / (windowLength - 2);

    // create window
    double[] result = new double[windowLength];
    Arrays.fill(result, negativeValues);
    result[(windowLength - 1) / 2] = 1.0;
    result[windowLength / 2] = 1.0;

    return result;
  }

  // Runs peak detection window through transform samples.
  private static double[] getPeaks(float[][] channel, int bin, double[] window) {
    double[] result = new double[channel.length];

    for (int i = window.length / 2; i < result.length - (window.length / 2); i++) {
      double windowSum = 0.0;
      for (int j = 0; j < window.length; j++)
        windowSum += window[j] * channel[i + j - (window.length / 2)][bin];
      result[i] = windowSum;
    }

    return result;
  }

  private static double interpolate(double[] channel, double index) {
    int bottomIndex = (int)Math.floor(index);
    int topIndex = (int)Math.ceil(index);

    double bottomToTopIndex = index - bottomIndex;
    double bottomToTopValue = channel[topIndex] - channel[bottomIndex];

    return (channel[bottomIndex] + (bottomToTopValue * bottomToTopIndex));
  }
  //endregion

  // Prints the average correlation/peak rate information of the audio file in args[0].
  public static void main(String[] args) {
    try {
      Reader reader = Reader.readFile(args[0]);
      Transform transform = new Transform(reader);
      Normalizer normalizer = new Normalizer(transform);
      long startTime = System.nanoTime();
      TemporalCharacteristics temporalCharacteristics = new TemporalCharacteristics(normalizer);
      System.out.println("Calculation time: " + ((System.nanoTime() - startTime) / 1000000000.0) + " seconds");

      System.out.println("Left channel characteristics:");
      double[] leftVolume = temporalCharacteristics.getAverageVolume(Channel.LEFT);
      double[] leftRise = temporalCharacteristics.getAverageRise(Channel.LEFT);
      double[] leftFall = temporalCharacteristics.getAverageFall(Channel.LEFT);

      PrintHelper.printFrequencies();
      PrintHelper.printValues("Loudness", leftVolume);
      PrintHelper.printValues("Rise", leftRise);
      PrintHelper.printValues("Fall", leftFall);
      System.out.println();

      System.out.println("Correlation (same time only):");
      for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i += 2) {
        for (int j = 0; j < Transform.FREQUENCY_RESOLUTION; j += 2) {
          System.out.print(PrintHelper.format.format(Transform.frequencyAtBin(i)) +
              " with " + PrintHelper.format.format(Transform.frequencyAtBin(j)) + ": ");
          System.out.println(PrintHelper.format.format(temporalCharacteristics.getCorrelation(Channel.LEFT)[i][j][0]));
        }
      }
      System.out.println();

      double[] bpms = new double[RATE_MAX - RATE_MIN + 1];
      for (int i = 0; i < bpms.length; i++)
        bpms[i] = RATE_MIN + i;
      PrintHelper.printValues("BPM Matches", bpms);
      for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i += 2)
        PrintHelper.printValues(PrintHelper.format.format(Transform.frequencyAtBin(i)) +
            "hz", temporalCharacteristics.getPeakRates(Channel.LEFT)[i]);
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
