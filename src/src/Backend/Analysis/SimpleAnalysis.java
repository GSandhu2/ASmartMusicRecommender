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
  private final SimpleCharacteristics characteristics;
  private final String filePath, fileName;
  // Multiplies the power of loudness/dynamics differences on the match result.
  // Lower values = higher match result for the same comparison.
  private static final double LOUDNESS_WEIGHT = 0.0000002, DYNAMICS_WEIGHT = 0.0000005;
  private static final double DIFFERENCE_EXPONENT = 2.0;
  // Multiplies arctan bounds from pi/2 to 1.
  private static final double ARCTAN_MULTIPLIER = 2.0 / Math.PI;

  public SimpleAnalysis(String filePath) throws IOException {
    this.filePath = filePath;

    try {
      Path path = Paths.get(filePath);
      this.fileName = path.getFileName().toString();
    } catch (InvalidPathException e) {
      throw new IOException("SimpleAnalysis: Invalid filepath - " + e.getMessage());
    }

    Reader reader = new Reader(filePath);
    Transform transform = new Transform(reader);
    this.characteristics = new SimpleCharacteristics(transform);
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
      return stereoCompare(thisLeftLoudness, otherLeftLoudness, thisLeftDynamics, thisRightDynamics,
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
    double difference = 0.0;

    difference += sumDifferences(aL, bL) * LOUDNESS_WEIGHT;
    difference += sumDifferences(aD, bD) * DYNAMICS_WEIGHT;

    difference = ARCTAN_MULTIPLIER * Math.atan(difference);

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

  private static double sumDifferences(double[] a, double[] b) {
    double result = 0.0;
    for (int i = 0; i < a.length; i++)
      result += Math.pow(Math.abs(a[i] - b[i]), DIFFERENCE_EXPONENT);
    return result;
  }
  //endregion

  // Compares each sound file in args to each other and sorts results by match percentage.
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("SimpleAnalysis: Needs at least two songs in args[] to run main method.");
      System.exit(1);
    }
    try {
      List<SoundAnalysis> analyses = new ArrayList<>(args.length);
      for (String file : args)
        analyses.add(new SimpleAnalysis(file));

      List<CompareResult> results = AnalysisCompare.compareAnalyses(analyses);
      for (CompareResult result : results) {
        SimpleAnalysis a = (SimpleAnalysis)result.a;
        SimpleAnalysis b = (SimpleAnalysis)result.b;
        System.out.println(a.fileName + " compared to " + b.fileName + " = " +
            PrintHelper.format.format(result.result * 100) + "% match");
      }
    } catch (IOException e) {
      System.out.println("SimpleAnalysis: File error - " + e.getMessage());
    }
  }
}
