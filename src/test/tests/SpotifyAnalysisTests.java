package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import Backend.Analysis.SoundAnalysis;
import Backend.Analysis.SpotifyAnalysis;
import org.junit.jupiter.api.*;

public class SpotifyAnalysisTests {

  @Test
  public void testReflexive() {
    SoundAnalysis a = generateRandomAnalysis();
    assertEquals(a.compareTo(a), 1.0);
  }

  @Test
  public void testSymmetric() {
    SoundAnalysis a = generateRandomAnalysis();
    SoundAnalysis b = generateRandomAnalysis();
    assertEquals(a.compareTo(b), b.compareTo(a));
  }

  private static SpotifyAnalysis generateRandomAnalysis() {
    return new SpotifyAnalysis(Math.random(), Math.random(), Math.random(),
        Math.random(), Math.random(), Math.random(), Math.random(), Math.random(),
        Math.random(), randomInt(1, 1000), randomInt(0, 11),
        randomInt(0, 1), randomInt(1, 8), "");
  }

  private static int randomInt(int low, int high) {
    int difference = 1 + high - low;
    return low + (int)(difference * Math.random());
  }
}
