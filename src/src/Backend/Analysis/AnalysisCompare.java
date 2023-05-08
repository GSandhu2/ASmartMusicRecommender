package Backend.Analysis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Calls CompareTo on a list of sound analyses and returns a sorted list of song pairs sorted by match value.
 */
public class AnalysisCompare {

  public static class CompareResult {
    final SoundAnalysis a, b;
    final double result;

    public CompareResult(SoundAnalysis a, SoundAnalysis b) {
      this.a = a;
      this.b = b;
      this.result = a.compareTo(b);
    }
  }

  public static List<CompareResult> compareAnalyses(List<SoundAnalysis> analyses) {
    List<CompareResult> result = new ArrayList<>(analyses.size());

    // gather results
    for (int i = 0; i < analyses.size(); i++)
      for (int j = i+1; j < analyses.size(); j++)
        result.add(new CompareResult(analyses.get(i), analyses.get(j)));

    // sort results
    result.sort(Comparator.comparingDouble(o -> o.result));
    Collections.reverse(result);

    return result;
  }

  public static List<CompareResult> compareAnalyses(List<SoundAnalysis> userAnalyses, List<SoundAnalysis> compareTo) {
    List<CompareResult> result = new ArrayList<>(userAnalyses.size() * compareTo.size());

    // gather results
    for (SoundAnalysis userAnalysis : userAnalyses)
      for (SoundAnalysis soundAnalysis : compareTo)
        result.add(new CompareResult(userAnalysis, soundAnalysis));

    // sort results
    result.sort(Comparator.comparingDouble(o -> o.result));
    Collections.reverse(result);

    return result;
  }

  // Test running compareAnalyses with RandomAnalysis and print results.
  public static void main(String[] args) {
    List<SoundAnalysis> analyses = new ArrayList<>();
    for(int i = 0; i < 4; i++)
      analyses.add(new RandomAnalysis());

    List<CompareResult> results = compareAnalyses(analyses);

    DecimalFormat format = new DecimalFormat("0.00%");
    System.out.println("AnalysisCompare results:");
    for(CompareResult result : results)
      System.out.println(format.format(result.result)); // definitely a readable line of code
  }
}
