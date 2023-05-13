package Backend.Analysis;

import Backend.Algorithm.Reader;
import Backend.Algorithm.Reader.Channel;
import Backend.Algorithm.SimpleCharacteristics;
import Backend.Algorithm.Transform;
import Backend.Analysis.AnalysisCompare.CompareResult;
import Backend.Helper.PrintHelper;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ethan Carnahan
 * Compares local sound files using SimpleCharacteristics
 */
public class SimpleAnalysis implements SoundAnalysis {
  //region Fields and public methods
  private SimpleCharacteristics characteristics;
  private final String filePath, fileName;
  // Multiplies the power of loudness/dynamics differences on the match result.
  // Lower values = higher match result.
  private static final double LOUDNESS_WEIGHT = 0.0000001, DYNAMICS_WEIGHT = 0.00000002;
  // Higher values = Bigger difference between big differences and small differences.
  private static final double LOUDNESS_EXPONENT = 2.0, DYNAMICS_EXPONENT = 2.0;
  // Multiplies the power of differences on individual frequency bins.
  private static final double RECURSIVE_WEIGHT = 0.1;
  // Multiplies arctan bounds from pi/2 to 1.
  private static final double ARCTAN_MULTIPLIER = 2.0 / Math.PI;

  // save/load will save a new analysis and load previously analyzed songs.
  public SimpleAnalysis(String filePath, boolean load, boolean save) throws IOException {
    this.filePath = filePath;

    try {
      Path path = Paths.get(filePath);
      this.fileName = path.getFileName().toString();
    } catch (InvalidPathException e) {
      throw new IOException("SimpleAnalysis: Invalid filepath - " + e.getMessage());
    }

    String savePath = System.getProperty("user.dir") + "/SavedAnalysis/" + fileName + ".simple";
    if (load) {
      System.out.println("SimpleAnalysis: Loading analysis for " + fileName);
      try {
        this.characteristics = SimpleCharacteristics.load(savePath);
        return;
      } catch (IOException e) {
        System.out.println("SimpleAnalysis: Failed to load file - " + e.getMessage());
      }
    }

    System.out.println("SimpleAnalysis: Analysing new song " + fileName);
    Reader reader = new Reader(filePath);
    Transform transform = new Transform(reader);
    this.characteristics = new SimpleCharacteristics(transform);
    if (save) {
      System.out.println("SimpleAnalysis: Saving analysis to " + savePath);
      this.characteristics.write(savePath);
    }
  }

  @Override
  public double compareTo(SoundAnalysis other) {
    if (!(other instanceof SimpleAnalysis otherSimple)) {
      throw new IllegalArgumentException("Incompatible sound analysis types.");
    }

    double[] thisLeftLoudness = this.characteristics.getAverageVolume(Channel.LEFT);
    double[] thisRightLoudness = this.characteristics.getAverageVolume(Channel.RIGHT);
    double[] thisLeftDynamics = this.characteristics.getDynamicRating(Channel.LEFT);
    double[] thisRightDynamics = this.characteristics.getDynamicRating(Channel.RIGHT);
    double[] otherLeftLoudness = otherSimple.characteristics.getAverageVolume(Channel.LEFT);
    double[] otherRightLoudness = otherSimple.characteristics.getAverageVolume(Channel.RIGHT);
    double[] otherLeftDynamics = otherSimple.characteristics.getDynamicRating(Channel.LEFT);
    double[] otherRightDynamics = otherSimple.characteristics.getDynamicRating(Channel.RIGHT);

    // if both stereo
    if (thisRightLoudness != null && otherRightLoudness != null)
      return stereoCompare(thisLeftLoudness, otherLeftLoudness, thisLeftDynamics, otherLeftDynamics,
          thisRightLoudness, otherRightLoudness, thisRightDynamics, otherRightDynamics);
    // if one stereo and one mono
    if (thisRightLoudness != null)
      return stereoToMonoCompare(thisLeftLoudness, thisRightLoudness, otherLeftLoudness,
          thisLeftDynamics, thisRightDynamics, otherLeftDynamics);
    if (otherRightLoudness != null)
      return stereoToMonoCompare(otherLeftLoudness, otherRightLoudness, thisLeftLoudness,
          otherLeftDynamics, otherRightDynamics, thisLeftDynamics);
    // if both mono
    return monoCompare(thisLeftLoudness, otherLeftLoudness, thisLeftDynamics, otherLeftDynamics);
  }

  public String getFilePath() {
    return filePath;
  }

  public String getFileName() {
    return fileName;
  }
  //endregion

  //region Private methods
  // a/b = SimpleAnalysis Objects
  // L/D = Loudness/Dynamics
  // 1/2 = Left/Right Channels
  private static double monoCompare(double[] aL, double[] bL, double[] aD, double[] bD) {
    double loudnessDifference = recursiveSumDifferences(aL, bL, 0, aL.length, LOUDNESS_EXPONENT) * LOUDNESS_WEIGHT;
    double dynamicsDifference = recursiveSumDifferences(aD, bD, 0, aD.length, DYNAMICS_EXPONENT) * DYNAMICS_WEIGHT;

    double difference = ARCTAN_MULTIPLIER * Math.atan(loudnessDifference + dynamicsDifference);

    return 1.0 - difference;
  }

  private static double stereoToMonoCompare(double[] aL1, double [] aL2, double[] bL,
      double[] aD1, double[] aD2, double[] bD) {
    double leftCompare = monoCompare(aL1, bL, aD1, bD);
    double rightCompare = monoCompare(aL2, bL, aD2, bD);
    return (0.5 * leftCompare) + (0.5 * rightCompare);
  }

  private static double stereoCompare(double[] aL1, double[] bL1, double[] aD1, double[] bD1,
  double[] aL2, double[] bL2, double[] aD2, double[] bD2) {
    double leftCompare = monoCompare(aL1, bL1, aD1, bD1);
    double rightCompare = monoCompare(aL2, bL2, aD2, bD2);
    return (0.5 * leftCompare) + (0.5 * rightCompare);
  }

  // Sums a/b together and calculates the sum difference, then splits a/b to do it again.
  private static double recursiveSumDifferences(double[] a, double[] b, int start, int length, double exp) {
    if (length == 0 || start >= a.length)
      return 0.0;
    if (length == 1)
      return Math.pow(Math.abs(a[start] - b[start]), exp);
    double result = Math.pow(Math.abs(sumArray(a, start, length) - sumArray(b, start, length)), exp);
    int nextLength = (int)Math.ceil((double)length / 2.0);
    int end = start + length;
    for (int i = start; i < end; i += nextLength)
      result += recursiveSumDifferences(a, b, i, nextLength, exp) * RECURSIVE_WEIGHT;
    return result;
  }

  private static double sumArray(double[] array, int start, int length) {
    double result = 0.0;
    int end = Math.min(start + length, array.length);
    for (int i = start; i < end; i++)
      result += array[i];
    return result;
  }
  //endregion

  // Compares each sound file in args to each other and sorts results by match percentage.
  public static void main(String[] args) {
    List<SoundAnalysis> analyses = new ArrayList<>(args.length);
    for (String file : args) {
      try {
        analyses.add(new SimpleAnalysis(file, true, true));
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("SimpleAnalysis: Failed to scan file - " + e.getMessage() + "\n");
      }
    }

    if (analyses.size() < 2) {
      System.out.println("SimpleAnalysis: Needs at least two scan-able songs in args[] to run main method.");
      System.exit(1);
    }

    List<CompareResult> results = AnalysisCompare.compareAnalyses(analyses);
    for (CompareResult result : results) {
      SimpleAnalysis a = (SimpleAnalysis)result.a;
      SimpleAnalysis b = (SimpleAnalysis)result.b;
      System.out.println(a.fileName + " compared to " + b.fileName + " = " +
              PrintHelper.format.format(result.result * 100) + "% match");
    }
  }
}
