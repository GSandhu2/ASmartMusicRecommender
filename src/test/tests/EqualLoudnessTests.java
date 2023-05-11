package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import Backend.Algorithm.*;

import org.junit.jupiter.api.*;

public class EqualLoudnessTests {
  private static final float errorBound = 0.1f;

  // Passing requirement: 1000hz dB = 1000hz phons
  @Test
  public void Test1000hz() {
    assertEquals(0, EqualLoudness.phonsToDb(0, 1000), errorBound);
    assertEquals(40, EqualLoudness.phonsToDb(40, 1000), errorBound);
    assertEquals(80, EqualLoudness.phonsToDb(80, 1000), errorBound);
  }

  // Passing requirements:
  // - <20hz phons = 20hz phons
  // - >12500hz phons = 12500hz phons
  @Test
  public void TestBounds() {
    assertEquals(EqualLoudness.phonsToDb(60, 10), EqualLoudness.phonsToDb(60, 20));
    assertEquals(EqualLoudness.phonsToDb(60, 15000), EqualLoudness.phonsToDb(60, 12500));
  }

  // Passing Requirements
  // Lowest dB of given phons is at 3000hz
  // Highest dB of given phons is at lowest frequency
  @Test
  public void TestQuietestAndLoudestFrequencies() {
    double[] dbResults = new double[40];
    for (int i = 0; i < 40; i++)
      dbResults[i] = EqualLoudness.phonsToDb(60, 500 * (i));

    double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
    int minIndex = 0, maxIndex = 0;
    for (int i = 0; i < 40; i++) {
      if (dbResults[i] < min) {
        min = dbResults[i];
        minIndex = i;
      }
      if (dbResults[i] > max) {
        max = dbResults[i];
        maxIndex = i;
      }
    }

    assertEquals(6, minIndex);
    assertEquals(0, maxIndex);
  }

  // Passing requirement: phonsToDb(dbToPhons(dB)) = dB for any dB and frequency
  @Test
  public void TestDbToPhons() {
    assertEquals(80, EqualLoudness.phonsToDb(EqualLoudness.dbToPhons(80, 500), 500), errorBound);
    assertEquals(20, EqualLoudness.phonsToDb(EqualLoudness.dbToPhons(20, 500), 500), errorBound);
    assertEquals(80, EqualLoudness.phonsToDb(EqualLoudness.dbToPhons(80, 5000), 5000), errorBound);
  }
}
