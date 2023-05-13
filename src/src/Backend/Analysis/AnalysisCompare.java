package Backend.Analysis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calls CompareTo on a list of sound analyses and returns a sorted list of song pairs sorted by match value.
 */
public class AnalysisCompare {

  public static class CompareResult {
    public final SoundAnalysis a, b;
    public final double result;

    public CompareResult(SoundAnalysis a, SoundAnalysis b) {
      this.a = a;
      this.b = b;
      this.result = a.compareTo(b);
    }
  }

  public static List<CompareResult> compareAnalyses(List<? extends SoundAnalysis> analyses) {
    List<CompareResult> result = new ArrayList<>(analyses.size() * analyses.size() / 2);

    // gather results
    for (int i = 0; i < analyses.size(); i++)
      for (int j = i+1; j < analyses.size(); j++)
        result.add(new CompareResult(analyses.get(i), analyses.get(j)));

    // sort results
    result.sort(Comparator.comparingDouble(o -> o.result));
    Collections.reverse(result);

    return result;
  }

  public static List<CompareResult> compareAnalyses(List<? extends SoundAnalysis> userAnalyses, List<? extends SoundAnalysis> compareTo) {
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

  // Filter list so only the most and least similar match to each song is displayed.
  // Reduces the results size from 0.5n^2 down to 2n at most.
  public static List<CompareResult> mostAndLeastSimilar(List<CompareResult> matches) {
    Set<CompareResult> result = new HashSet<>();

    // get set of sound analyses
    Set<SoundAnalysis> set = new HashSet<>();
    for (CompareResult match : matches)
      set.add(match.a);

    // gather results
    for (SoundAnalysis song : set) {
      // get the most similar match
      for (int i = 0; i < matches.size(); i++) {
        if (matches.get(i).a.equals(song) || matches.get(i).b.equals(song)) {
          result.add(matches.get(i));
          break;
        }
      }

      // get the least similar match
      for (int i = matches.size() - 1; i >= 0; i--) {
        if (matches.get(i).a.equals(song) || matches.get(i).b.equals(song)) {
          result.add(matches.get(i));
          break;
        }
      }
    }

    // sort results
    List<CompareResult> resultList = new ArrayList<>(result);
    resultList.sort(Comparator.comparingDouble(o -> o.result));
    Collections.reverse(resultList);

    return resultList;
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
