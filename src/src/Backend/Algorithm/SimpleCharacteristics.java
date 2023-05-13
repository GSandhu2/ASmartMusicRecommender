package Backend.Algorithm;

import Backend.Algorithm.Reader.Channel;
import Backend.Helper.PrintHelper;

import java.io.*;

/**
 * @author Ethan Carnahan
 * Basic sound analysis that calculates the perceived frequency balance and dynamics of a song.
 * How to use: Pass in a Transform object and duration, and call get methods for volume/dynamics information.
 * For now, more dynamic means the rate of change in volume.
 * In the future, more dynamic will mean the bigger peaks in volume are narrower in time.
 */
public class SimpleCharacteristics {
  //region Fields and public methods
  // Average volume of each frequency bin.
  private final double[] averageLeftVolume, averageRightVolume;
  // Average rate of volume change per second for each frequency bin.
  private final double[] averageLeftDynamicRating, averageRightDynamicRating;

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

  // Used for loading.
  private SimpleCharacteristics(double[] averageLeftVolume, double[] averageRightVolume,
  double[] averageLeftDynamicRating, double[] averageRightDynamicRating) {
    this.averageLeftVolume = averageLeftVolume;
    this.averageRightVolume = averageRightVolume;
    this.averageLeftDynamicRating = averageLeftDynamicRating;
    this.averageRightDynamicRating = averageRightDynamicRating;
  }

  public double[] getAverageVolume(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftVolume : averageRightVolume;
  }

  public double[] getDynamicRating(Channel channel) {
    return (channel == Channel.LEFT) ? averageLeftDynamicRating : averageRightDynamicRating;
  }

  public void write(String filepath) throws IOException {
    File file = new File(filepath);
    file.createNewFile();
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));

    writer.write(averageRightVolume != null ? "Stereo" : "Mono");
    writer.newLine();
    writeArray(writer, averageLeftVolume);
    writeArray(writer, averageLeftDynamicRating);
    if (averageRightVolume != null) {
      writeArray(writer, averageRightVolume);
      writeArray(writer, averageRightDynamicRating);
    }

    writer.flush();
    writer.close();
  }

  private static void writeArray(BufferedWriter writer, double[] array) throws IOException {
    for (double value : array) {
      writer.write(String.valueOf(value));
      writer.newLine();
    }
  }

  public static SimpleCharacteristics load(String filepath) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filepath));

    boolean stereo = reader.readLine().equals("Stereo");

    double[] lv = loadArray(reader, Transform.FREQUENCY_RESOLUTION);
    double[] ld = loadArray(reader, Transform.FREQUENCY_RESOLUTION);
    double[] rv, rd;
    if (stereo) {
      rv = loadArray(reader, Transform.FREQUENCY_RESOLUTION);
      rd = loadArray(reader, Transform.FREQUENCY_RESOLUTION);
    } else {
      rv = null;
      rd = null;
    }

    return new SimpleCharacteristics(lv, rv, ld, rd);
  }

  private static double[] loadArray(BufferedReader reader, int length) throws IOException {
    double[] result = new double[length];
    for (int i = 0; i < length; i++)
      result[i] = Double.parseDouble(reader.readLine());
    return result;
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
        result[j] += Math.abs(channel[i][j] - channel[i - 1][j]);
      }

      result[j] /= channel.length;
    }

    return result;
  }
  //endregion

  // Prints the average volume/DR information of the audio file in args[0].
  public static void main(String[] args) {
    try {
      Reader reader = Reader.readFile(args[0]);
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
