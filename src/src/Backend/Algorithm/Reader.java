package Backend.Algorithm;

// .mp3 decode
import fr.delthas.javamp3.Sound;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ethan Carnahan
 * Extracts audio information from .mp3 files.
 * How to use: Create a new Reader for each audio file and call the getter methods.
 */
public class Reader {
    // Mono or stereo audio
    public enum Mode {MONO, STEREO}
    // Left or right ear
    public enum Channel {LEFT, RIGHT}

    private static final int BUFFER_SIZE = 32768; // 32 kB

    // Audio channels (mono uses only left)
    private final short[] left, right;
    private final Mode mode;
    private final int sampleRate;

    public Reader(String mp3Filepath) throws IOException {
        // Decode mp3 file.
        Sound sound;
        List<Byte> soundBytes = new ArrayList<>(BUFFER_SIZE);
        try {
            sound = new Sound(new FileInputStream(mp3Filepath));

            // JavaMP3's decodeFullyInto() method doesn't work and Java primitive/object typing is garbage.
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int readLength = sound.read(buffer);
                if (readLength <= 0)
                    break;
                for (int i = 0; i < readLength; i++)
                    soundBytes.add(buffer[i]);
            }

            sound.close();
        } catch (IOException e) { throw new IOException("Reader: Failed to read MP3 file - " + e.getMessage()); }

        // Get mode and sample rate.
        mode = sound.isStereo() ? Mode.STEREO : Mode.MONO;
        sampleRate = sound.getSamplingFrequency();

        // Convert bytes to shorts (samples are 16 bits).
        if (sound.isStereo()) {
            left  = new short[soundBytes.size() / 4];
            right = new short[soundBytes.size() / 4];
            for(int i = 0; i < soundBytes.size() - 3; i += 4) {
                left[i/4]  = (short) ((soundBytes.get(i  ) & 0xFF) | (soundBytes.get(i+1) << 8));
                right[i/4] = (short) ((soundBytes.get(i+2) & 0xFF) | (soundBytes.get(i+3) << 8));
            }
        } else {
            left  = new short[soundBytes.size() / 2];
            right = null;
            for(int i = 0; i < soundBytes.size() - 1; i += 2) {
                left[i/2] = (short) ((soundBytes.get(i  ) & 0xFF) | (soundBytes.get(i+1) << 8));
            }
        }
    }

    public Mode getMode() {
        return mode;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    // Returns duration in seconds.
    public double getDuration() {
        return (double)left.length / (double)sampleRate;
    }

    // returns null for right channel of mono audio file.
    public short[] getChannel(Channel channel) {
        return (channel == Channel.LEFT) ? left : right;
    }

    // Prints out the values of the .mp3 file in args[0].
    public static void main(String[] args) {
        try {
            Reader mp3 = new Reader(args[0]);

            // mp3 info
            System.out.println("mp3 mode = " + mp3.getMode());
            System.out.println("mp3 sample rate = " + mp3.getSampleRate());
            System.out.println("mp3 duration = " + mp3.getDuration());

            // left channel samples
            System.out.println("\nmp3 samples:");
            for (Short s : mp3.getChannel(Channel.LEFT))
                System.out.println(s);

        } catch (IOException e) { System.out.println(e.getMessage()); }
    }
}
