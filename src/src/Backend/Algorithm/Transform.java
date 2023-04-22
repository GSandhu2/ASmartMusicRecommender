package Backend.Algorithm;

import Backend.Algorithm.Reader.Channel;
import java.io.IOException;

/**
 * @author Ethan Carnahan
 * Performs a <a href="https://en.wikipedia.org/wiki/Constant-Q_transform">Constant-Q Transform</a> on an audio signal.
 * How to use: Pass in a Reader object to generate the Transform, then use getTransform to get the frequency/amplitude data.
 */
public class Transform {
    // The sample rate of the fourier analysis in samples per second.
    public static final double TIME_RESOLUTION = 20;
    // There are this many frequency bins between 20Hz and 20,480Hz.
    public static final int FREQUENCY_RESOLUTION = 120; // 10 octaves * 12 notes per octaves
    public static final double BOTTOM_FREQUENCY = 20; // Lowest audible pitch in Hz.

    // First dimension is time index, second dimension is frequency index, value is amplitude.
    private final float[][] leftFrequencyAmplitudes, rightFrequencyAmplitudes;

    public Transform(Reader audio) {
        // Check audio length
        int timeSamples = (int) (TIME_RESOLUTION * audio.getDuration());
        if (timeSamples < 1)
            throw new IllegalArgumentException("Transform: Audio file is too short, needs to be at least " + (1/TIME_RESOLUTION) + " seconds long.");

        // Initialize arrays
        leftFrequencyAmplitudes = new float[timeSamples][FREQUENCY_RESOLUTION];
        if (audio.getMode() == Reader.Mode.STEREO)
            rightFrequencyAmplitudes = new float[timeSamples][FREQUENCY_RESOLUTION];
        else
            rightFrequencyAmplitudes = null;

        // Todo: Perform transform
    }

    public float[][] getFrequencyAmplitudes(Channel channel) {
        return (channel == Channel.LEFT) ? leftFrequencyAmplitudes : rightFrequencyAmplitudes;
    }


    // Prints the frequency/amplitude information of the audio file in args[0]
    public static void main(String[] args) {
        try {
            Transform transform = new Transform(new Reader(args[0]));

            System.out.println("Left channel frequency analysis:");
            float[][] left = transform.getFrequencyAmplitudes(Channel.LEFT);
            for (int i = 0; i < left.length; i++) {
                System.out.print("At time " + (i / TIME_RESOLUTION) + ":");
                for (int j = 0; j < left[i].length; j++)
                    System.out.print(" " + left[i][j]);
                System.out.println();
            }

        } catch (IOException e) { System.out.println(e.getMessage()); }
    }
}
