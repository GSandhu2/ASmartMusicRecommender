package Backend.Algorithm;

import Backend.Algorithm.Reader.Channel;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * @author Ethan Carnahan
 * Performs a <a href="https://en.wikipedia.org/wiki/Constant-Q_transform">Constant-Q Transform</a> on an audio
 * signal. How to use: Pass in a Reader object to generate the Transform, then use getTransform to
 * get the frequency/amplitude data.
 */
public class Transform {

  //region Fields and public methods
  // The sample rate of the fourier analysis in samples per second.
  // Higher value has more temporal resolution, lower value uses less space and has less spectral leakage.
  public static final double TIME_RESOLUTION = 20;
  // There are this many frequency bins between the top/bottom frequency.
  public static final int FREQUENCY_RESOLUTION = 120; // 10 octaves * 12 notes per octaves.
  public static final double BOTTOM_FREQUENCY = 20; // Lowest audible pitch in Hz.
  public static final double TOP_FREQUENCY = 20480; // Slightly over highest audible pitch, but is a convenient 20 * 2^10.
  public static final double TOP_BOTTOM_RATIO = TOP_FREQUENCY / BOTTOM_FREQUENCY;
  private static final double TWO_PI = Math.PI * 2.0;

  // First dimension is time index, second dimension is frequency index, value is amplitude.
  private final float[][] leftFrequencyAmplitudes, rightFrequencyAmplitudes;

  public Transform(Reader audio) {
    // Check audio length
    int timeSamples = (int) (TIME_RESOLUTION * audio.getDuration());
      if (timeSamples < 1) {
          throw new IllegalArgumentException(
              "Transform: Audio file is too short, needs to be at least " + (1 / TIME_RESOLUTION)
                  + " seconds long.");
      }

    // Perform transform
    leftFrequencyAmplitudes = cqt(audio.getChannel(Channel.LEFT), timeSamples,
        audio.getSampleRate());
      if (audio.getMode() == Reader.Mode.STEREO) {
          rightFrequencyAmplitudes = cqt(audio.getChannel(Channel.RIGHT), timeSamples,
              audio.getSampleRate());
      } else {
          rightFrequencyAmplitudes = null;
      }
  }

  // Passing right channel on mono song will return null.
  public float[][] getFrequencyAmplitudes(Channel channel) {
    return (channel == Channel.LEFT) ? leftFrequencyAmplitudes : rightFrequencyAmplitudes;
  }
  //endregion

  //region Transform methods
  // Performs full transform on a channel of audio.
  private static float[][] cqt(short[] audioSamples, int samples, int sampleRate) {
    float[][] result = new float[samples][FREQUENCY_RESOLUTION + 1];
    double audioSamplesPerSample = (double) audioSamples.length / samples;

    // for each frequency bin
    for (int j = 0; j <= FREQUENCY_RESOLUTION; j++) {
      double frequency =
          BOTTOM_FREQUENCY * Math.pow(TOP_BOTTOM_RATIO, (double) j / FREQUENCY_RESOLUTION);
      // for each time sample
      for (int i = 0; i < samples; i++) {
        int center = (int) (i * audioSamplesPerSample);
        int start = center - (int) (audioSamplesPerSample / 2.0);
        int end = center + (int) (audioSamplesPerSample / 2.0);
        result[i][j] = transform(audioSamples, start, end, frequency, sampleRate);
      }
    }

    return result;
  }

  // Performs transform for a specific frequency and time period.
  private static float transform(short[] audioSamples, int start, int end, double frequency,
      int sampleRate) {
    double realSum = 0.0, complexSum = 0.0;

    for (int i = start; i <= end; i++) {
      double angle = (i - start) * TWO_PI * frequency / sampleRate;
      double window = window(i - start, end - start);
      realSum += window * mirrorBounds(audioSamples, i) * Math.cos(angle);
      complexSum += window * mirrorBounds(audioSamples, i) * Math.sin(angle);
    }

    realSum /= (end - start);
    complexSum /= (end - start);
    return (float) Math.sqrt((realSum * realSum) + (complexSum * complexSum));
  }

  // Nuttall window.
  // See https://en.wikipedia.org/wiki/Window_function#Nuttall_window,_continuous_first_derivative
  private static double window(int index, int length) {
    double angle = TWO_PI * index / length;
    return ((0.355768) - (0.4891775 * Math.cos(angle)) + (0.1365995 * Math.cos(2 * angle)) -
        (0.0106411 * Math.cos(3 * angle))) / 0.355768;
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
  //endregion

  // Prints the frequency/amplitude information of the audio file in args[0]
  public static void main(String[] args) {
    DecimalFormat format = new DecimalFormat("#####.00");
    try {
      long startTime = System.nanoTime();
      Reader reader = new Reader(args[0]);
      Transform transform = new Transform(reader);
      System.out.println("Calculation time (nanoseconds): " + (System.nanoTime() - startTime));

      System.out.println(
          "Left channel frequency analysis (window length = " + (reader.getSampleRate()
              / (int) TIME_RESOLUTION) + " samples):");

      System.out.print("    Frequencies:");
        for (int i = 0; i <= FREQUENCY_RESOLUTION; i++) {
            System.out.print(" " + String.format("%8s", format.format(
                BOTTOM_FREQUENCY * Math.pow(TOP_BOTTOM_RATIO, (double) i / FREQUENCY_RESOLUTION))));
        }
      System.out.println();

      float[][] left = transform.getFrequencyAmplitudes(Channel.LEFT);
      for (int i = 0; i < left.length; i++) {
        System.out.print(
            "At time " + String.format("%8s", format.format(i / TIME_RESOLUTION) + ":"));
          for (int j = 0; j < left[i].length; j++) {
              System.out.print(" " + String.format("%8s", format.format(left[i][j])));
          }
        System.out.println();
      }

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
