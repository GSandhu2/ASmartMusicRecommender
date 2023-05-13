import static org.junit.jupiter.api.Assertions.assertEquals;

import Backend.Algorithm.*;

import org.junit.jupiter.api.*;

public class NormalizerTests {
    private static final double errorBound = 1;

    // Passing requirement: Two flat frequency responses of different volumes are normalized to the same perceived loudness.
    @Test
    public void testVolume() {
        float[][] quiet = generateNormalizedFlatTransform(10, 0.1f);
        float[][] loud = generateNormalizedFlatTransform(10, 10000f);
        assertEquals(0.0, sum2DArray(quiet) - sum2DArray(loud), errorBound);
    }

    // Passing requirement: Two flat frequency responses of different lengths are normalized to the same perceived loudness.
    @Test
    public void testDuration() {
        float[][] brief = generateNormalizedFlatTransform(10, 100f);
        float[][] lengthy = generateNormalizedFlatTransform(100, 100f);
        assertEquals(0.0, sum2DArray(brief) - sum2DArray(lengthy), errorBound);
    }

    private float[][] generateNormalizedFlatTransform(int length, float volume) {
        float[][] result = new float[length][Transform.FREQUENCY_RESOLUTION];
        for (int i = 0; i < result.length; i++)
            for (int j = 0; j < result[0].length; j++)
                result[i][j] = volume;
        return Normalizer.normalizeTransform(result);
    }

    private double sum2DArray(float[][] array) {
        double result = 0.0;
        for (float[] fa : array)
            for (float f : fa)
                result += f;
        return result / array.length;
    }
}
