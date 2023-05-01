import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Backend.Algorithm.Reader;
import Backend.Algorithm.Reader.Channel;
import Backend.Algorithm.Transform;
import java.io.IOException;
import org.junit.jupiter.api.*;

public class TransformTests {
  private static final float errorBound = 0.4f;

  // Passing requirements:
  // - frequencyAmplitudes.length / TIME_RESOLUTION ~ Duration
  // - Flat frequency balance.
  @Test
  public void testNoiseTransform() throws IOException {
    Reader reader = new Reader("src/test/resource/white_noise_-6db.mp3");
    Transform transform = new Transform(reader);
    float[][] frequencyAmplitudes = transform.getFrequencyAmplitudes(Channel.LEFT);

    // test duration
    assertEquals(reader.getDuration(), (frequencyAmplitudes.length /
        Transform.TIME_RESOLUTION), errorBound, "Incorrect duration of transform.");

    // gather averages
    double[] averages = new double[frequencyAmplitudes[0].length];
    double overallAverage = 0.0;
    // for each frequency
    for (int i = 0; i < frequencyAmplitudes[0].length; i++) {
      averages[i] = 0.0;

      // for each time sample
      for (float[] frequencyAmplitude : frequencyAmplitudes) {
        averages[i] += frequencyAmplitude[i];
        overallAverage += frequencyAmplitude[i];
      }

      averages[i] /= frequencyAmplitudes.length;
    }
    overallAverage /= (frequencyAmplitudes.length * frequencyAmplitudes[0].length);

    // test averages
    for (int i = 0; i < averages.length; i++) {
      double ratio = (averages[i] / overallAverage);
      assertEquals(1.0, ratio, errorBound, "Average of frequency " +
          (Transform.frequencyAtBin(i)) + " is outside error bound (1 +- " + errorBound + ")");
    }
  }

  // Passing requirements:
  // - frequencyAmplitudes.length / TIME_RESOLUTION ~ Duration
  // - The loudest frequency bin is the one closest to 1,000 Hz
  // - The loudest frequency bin is around -6dB (or 0.5 * Short.MAX_VALUE).
  @Test
  public void testToneTransform() throws IOException {
    Reader reader = new Reader("src/test/resource/tone_1000hz_-6db.mp3");
    Transform transform = new Transform(reader);
    float[][] frequencyAmplitudes = transform.getFrequencyAmplitudes(Channel.LEFT);

    // test duration
    assertEquals(reader.getDuration(), (frequencyAmplitudes.length /
        Transform.TIME_RESOLUTION), errorBound, "Incorrect duration of transform.");

    // gather averages
    double[] averages = new double[frequencyAmplitudes[0].length];
    for (int i = 0; i < frequencyAmplitudes[0].length; i++) {
      averages[i] = 0.0;
      for (float[] frequencyAmplitude : frequencyAmplitudes)
        averages[i] += frequencyAmplitude[i];
      averages[i] /= frequencyAmplitudes.length;
    }

    // get loudest frequency bin
    double maxValue = 0.0;
    int maxIndex = 0;
    for (int i = 0; i < averages.length; i++) {
      if (averages[i] > maxValue) {
        maxValue = averages[i];
        maxIndex = i;
      }
    }

    // test loudest frequency bin by frequency
    assertTrue(maxIndex > 0 && maxIndex < averages.length - 1,
        "Loudest frequency should not be first/last bin");
    assertTrue(Math.abs(Transform.frequencyAtBin(maxIndex) - 1000) <
        Math.abs(Transform.frequencyAtBin(maxIndex-1) - 1000),
        "Loudest frequency is too far under 1000Hz");
    assertTrue(Math.abs(Transform.frequencyAtBin(maxIndex) - 1000) <
        Math.abs(Transform.frequencyAtBin(maxIndex+1) - 1000),
        "Loudest frequency is too far over 1000Hz");

    // test loudest frequency bin by amplitude
    assertEquals(1.0, (maxValue / (0.25 * Short.MAX_VALUE)), errorBound);
  }

  // Basic test for helper method.
  @Test
  public void testMirrorBounds() {
    short[] samples = {0, 1, 2, 1, 0};
    assertEquals(-2, Transform.mirrorBounds(samples, -2));
    assertEquals(-1, Transform.mirrorBounds(samples, -1));
    assertEquals(0, Transform.mirrorBounds(samples, 0));
    assertEquals(1, Transform.mirrorBounds(samples, 1));
    assertEquals(2, Transform.mirrorBounds(samples, 2));
    assertEquals(1, Transform.mirrorBounds(samples, 3));
    assertEquals(0, Transform.mirrorBounds(samples, 4));
    assertEquals(-1, Transform.mirrorBounds(samples, 5));
    assertEquals(-2, Transform.mirrorBounds(samples, 6));
  }
}
