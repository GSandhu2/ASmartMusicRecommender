package Backend.Algorithm;

import Backend.Algorithm.Reader.Channel;
import Backend.Helper.PrintHelper;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * @author Ethan Carnahan
 * Performs a <a href="https://en.wikipedia.org/wiki/Constant-Q_transform">Constant-Q Transform</a> on an audio
 * signal. How to use: Pass in a Reader object to generate the Transform, then use getTransform to
 * get the frequency/amplitude data.
 */
public class Transform {

  //region Fields and public methods
  // The sample rate of the fourier analysis in samples per second.
  public static final double TIME_RESOLUTION = 20;
  // There are this many frequency bins between the top/bottom frequency.
  public static final int FREQUENCY_RESOLUTION = 120; // 10 octaves * 12 notes per octaves.
  // Smaller number = less leaking between frequency bins but takes longer to calculate.
  private static final double BOTTOM_FILTER_WIDTH = 2;
  public static final double BOTTOM_FREQUENCY = 20; // Lowest audible pitch in Hz.
  public static final double TOP_FREQUENCY = 20480; // Slightly over highest audible pitch, but is a convenient 20 * 2^10.
  public static final double TOP_BOTTOM_RATIO = TOP_FREQUENCY / BOTTOM_FREQUENCY;
  private static final double TWO_PI = Math.PI * 2.0;
  private static final float HALF_PI = (float)Math.PI / 2.0f;

  // First dimension is time index, second dimension is frequency index, value is amplitude.
  private final float[][] leftFrequencyAmplitudes, rightFrequencyAmplitudes;

  public Transform(Reader audio) {
    System.out.println("Transform: Running transform on " + (int)audio.getDuration() + " seconds of audio");

    // Check audio length
    int timeSamples = (int) (TIME_RESOLUTION * audio.getDuration());
      if (timeSamples < 1) {
          throw new IllegalArgumentException(
              "Transform: Audio file is too short, needs to be at least " + (1 / TIME_RESOLUTION)
                  + " seconds long.");
      }

    // Perform transform
    leftFrequencyAmplitudes = multithread_cqt(audio.getChannel(Channel.LEFT), timeSamples,
        audio.getSampleRate());
      if (audio.getMode() == Reader.Mode.STEREO) {
          rightFrequencyAmplitudes = multithread_cqt(audio.getChannel(Channel.RIGHT), timeSamples,
              audio.getSampleRate());
      } else {
          rightFrequencyAmplitudes = null;
      }
  }

  // Passing right channel on mono song will return null.
  public float[][] getFrequencyAmplitudes(Channel channel) {
    return (channel == Channel.LEFT) ? leftFrequencyAmplitudes : rightFrequencyAmplitudes;
  }

  // Tells you the frequency of any bin.
  public static double frequencyAtBin(int index) {
    return BOTTOM_FREQUENCY * Math.pow(TOP_BOTTOM_RATIO, (double) index / FREQUENCY_RESOLUTION);
  }
  //endregion

  //region CQT multithreaded class and methods

  // Virgin Single-threaded CQT
  /*
  private static float[][] cqt(short[] audioSamples, int samples, int sampleRate) {
    float[][] result = new float[samples][FREQUENCY_RESOLUTION];
    double audioSamplesPerSample = (double) audioSamples.length / samples;

    // for each frequency bin
    for (int j = 0; j < FREQUENCY_RESOLUTION; j++) {
      double frequency = frequencyAtBin(j);
      int windowLength = windowLength(j, sampleRate);
      // for each time sample
      for (int i = 0; i < samples; i++) {
        int center = (int) (i * audioSamplesPerSample);
        int start = center - (windowLength / 2);
        result[i][j] = transform(audioSamples, start, windowLength, frequency, sampleRate);
        result[i][j] /= windowLength;
      }
    }

    return result;
  }
  */

  // Chad Multi-threaded CQT
  private static float[][] multithread_cqt(short[] audioSamples, int samples, int sampleRate) {
    CQT task = new CQT(audioSamples, sampleRate, samples, 0, samples);
    try (ForkJoinPool fjp = new ForkJoinPool()) {
      return fjp.invoke(task);
    }
  }

  private static class CQT extends RecursiveTask<float[][]> {
    private final short[] audioSamples;
    private final int sampleRate, samples, sampleStart, sampleEnd;
    // Each thread runs on <1 second of audio.
    private static final int threshold = (int)TIME_RESOLUTION;

    public CQT(short[] audioSamples, int sampleRate, int samples, int sampleStart, int sampleEnd) {
      this.audioSamples = audioSamples;
      this.sampleRate = sampleRate;
      this.samples = samples;
      this.sampleStart = sampleStart;
      this.sampleEnd = sampleEnd;
    }

    @Override
    protected float[][] compute() {
      int length = sampleEnd - sampleStart;
      if (length <= threshold)
        return partialCQT();

      CQT firstTask = new CQT(audioSamples, sampleRate, samples,
          sampleStart, sampleStart + (length / 2));
      firstTask.fork();
      CQT secondTask = new CQT(audioSamples, sampleRate, samples, sampleStart + (length / 2),
          sampleEnd);
      float[][] secondResult = secondTask.compute();
      float[][] firstResult = firstTask.join();

      return joinArrays(firstResult, secondResult);
    }

    private float[][] partialCQT() {
      int length = sampleEnd - sampleStart;
      float[][] result = new float[length][FREQUENCY_RESOLUTION];
      double audioSamplesPerSample = (double) audioSamples.length / samples;

      // for each frequency bin
      for (int j = 0; j < FREQUENCY_RESOLUTION; j++) {
        double frequency = frequencyAtBin(j);
        int windowLength = windowLength(j, sampleRate);
        // for each time sample
        for (int i = sampleStart; i < sampleEnd; i++) {
          int audioCenter = (int) (i * audioSamplesPerSample);
          int audioStart = audioCenter - (windowLength / 2);
          result[i - sampleStart][j] = transform(audioSamples, audioStart, windowLength, frequency, sampleRate) / windowLength;
        }
      }

      return result;
    }

    private static float[][] joinArrays(float[][] a, float[][] b) {
      float[][] result = new float[a.length + b.length][a[0].length];

      for (int i = 0; i < a.length; i++)
        System.arraycopy(a[i], 0, result[i], 0, a[0].length);
      for (int i = 0; i < b.length; i++)
        System.arraycopy(b[i], 0, result[i + a.length], 0, b[0].length);

      return result;
    }
  }

  // Performs transform for a specific frequency and time period.
  private static float transform(short[] audioSamples, int start, int length, double frequency, int sampleRate) {
    double realSum = 0.0, complexSum = 0.0;

    int end = start + length;
    for (int i = start; i < end; i++) {
      double angle = (i - start) * TWO_PI * frequency / sampleRate;
      double window = window(i - start, length);
      realSum += window * mirrorBounds(audioSamples, i) * FasterTrig.cos(angle);
      complexSum += window * mirrorBounds(audioSamples, i) * FasterTrig.sin(angle);
    }

    return (float) Math.sqrt((realSum * realSum) + (complexSum * complexSum));
  }

  private static int windowLength(int frequencyBin, int sampleRate) {
    return (int)Math.ceil(sampleRate / filterWidth(frequencyBin));
  }

  private static double filterWidth(int frequencyBin) {
    return BOTTOM_FILTER_WIDTH * Math.pow(TOP_BOTTOM_RATIO, (double)frequencyBin / FREQUENCY_RESOLUTION);
  }

  // Nuttall window.
  // See https://en.wikipedia.org/wiki/Window_function#Nuttall_window,_continuous_first_derivative
  public static double window(int index, int length) {
    double angle = TWO_PI * index / length;
    return ((0.355768) - (0.4891775 * FasterTrig.cos(angle)) + (0.1365995 * FasterTrig.cos(2 * angle)) -
        (0.0106411 * FasterTrig.cos(3 * angle))) / 0.355768;
  }

  // Allows you to go out of bounds by mirroring the index back in-bounds and inverting the sample value.
  // Just make sure you don't go double out of bounds.
  private static short mirrorBounds(short[] audioSamples, int index) {
    if (index >= 0 && index < audioSamples.length) {
      return audioSamples[index];
    }
    if (index < 0) {
      return (short) (-audioSamples[-index] & 0xFFFF);
    } else {
      return (short) (-audioSamples[(2 * audioSamples.length) - index - 2] & 0xFFFF);
    }
  }

  // Precalculate lots of sine/cosine values to reduce math to array lookups.
  private static class FasterTrig {
    // Higher number = more accurate but uses more memory.
    private static final int ANGLE_RESOLUTION = 10000; // 4kB.
    private static final float[] angleValues = generateAngleValues();

    private static float sin(double radians) {
      int index = (int)(radians * ANGLE_RESOLUTION / TWO_PI) % ANGLE_RESOLUTION;
      return angleValues[index];
    }

    private static float cos(double radians) {
      return sin(radians + HALF_PI);
    }

    private static float[] generateAngleValues() {
      float[] result = new float[ANGLE_RESOLUTION];
      for (int i = 0; i < ANGLE_RESOLUTION; i++)
        result[i] = (float)Math.sin(TWO_PI * i / ANGLE_RESOLUTION);
      return result;
    }
  }
  //endregion

  // Prints the frequency/amplitude information of the audio file in args[0]
  public static void main(String[] args) {
    try {
      Reader reader = Reader.readFile(args[0]);
      long startTime = System.nanoTime();
      Transform transform = new Transform(reader);
      System.out.println("Calculation time: " + ((System.nanoTime() - startTime) / 1000000000.0) + " seconds");

      System.out.println("Left channel frequency analysis:");
      PrintHelper.printFrequencies();
      float[][] left = transform.getFrequencyAmplitudes(Channel.LEFT);
      for (int i = 0; i < left.length; i += TIME_RESOLUTION)
        PrintHelper.printValues("T = " + PrintHelper.format.format(i / TIME_RESOLUTION), left[i]);

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
