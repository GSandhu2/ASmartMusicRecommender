package Backend.Algorithm;

// .mp3 decode

import fr.delthas.javamp3.Sound;
import java.io.File;
import javax.sound.sampled.*;

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

  public static Reader readFile(String filepath) throws IOException {
    // Get file extension
    int dotIndex = filepath.lastIndexOf('.');
    if (dotIndex == -1)
      throw new IllegalArgumentException("Reader: File must end in .wav or .mp3.");
    String extension = filepath.substring(dotIndex);

    if (extension.equals(".mp3"))
      return readMP3File(filepath);
    if (extension.equals(".wav"))
      return readWavFile(filepath);

    throw new IllegalArgumentException("Reader: File must end in .wav or .mp3.");
  }

  private Reader(short[] left, short[] right, Mode mode, int sampleRate) {
    this.left = left;
    this.right = right;
    this.mode = mode;
    this.sampleRate = sampleRate;
  }

  private static Reader readMP3File(String mp3Filepath) throws IOException {
    // Decode mp3 file.
    System.out.println("Reader: Reading file " + mp3Filepath);
    Sound sound;
    List<Byte> soundBytes = new ArrayList<>(BUFFER_SIZE);
    try {
      sound = new Sound(new FileInputStream(mp3Filepath));

      // JavaMP3's decodeFullyInto() method doesn't work and Java primitive/object typing is garbage.
      byte[] buffer = new byte[BUFFER_SIZE];
      while (true) {
        int readLength = sound.read(buffer);
        if (readLength <= 0) {
          break;
        }
        for (int i = 0; i < readLength; i++) {
          soundBytes.add(buffer[i]);
        }
      }

      sound.close();
    } catch (IOException e) {
      throw new IOException("Reader: Failed to read MP3 file - " + e.getMessage());
    }

    // Get mode and sample rate.
    Mode mode = sound.isStereo() ? Mode.STEREO : Mode.MONO;
    int sampleRate = sound.getSamplingFrequency();

    // Convert bytes to shorts (samples are 16 bits).
    short[] left, right;
    if (sound.isStereo()) {
      left = new short[soundBytes.size() / 4];
      right = new short[soundBytes.size() / 4];
      for (int i = 0; i < soundBytes.size() - 3; i += 4) {
        left[i / 4] = (short) ((soundBytes.get(i) & 0xFF) | (soundBytes.get(i + 1) << 8));
        right[i / 4] = (short) ((soundBytes.get(i + 2) & 0xFF) | (soundBytes.get(i + 3) << 8));
      }
    } else {
      left = new short[soundBytes.size() / 2];
      right = null;
      for (int i = 0; i < soundBytes.size() - 1; i += 2) {
        left[i / 2] = (short) ((soundBytes.get(i) & 0xFF) | (soundBytes.get(i + 1) << 8));
      }
    }

    return new Reader(left, right, mode, sampleRate);
  }

  // Based on https://docs.oracle.com/javase/tutorial/sound/converters.html
  private static Reader readWavFile(String wavFilepath) throws IOException {
    // file
    AudioInputStream inputStream;
    try {
      inputStream = AudioSystem.getAudioInputStream(new File(wavFilepath));
    } catch (UnsupportedAudioFileException e) {
      System.out.println("Reader: Invalid .wav file - " + e.getMessage());
      throw new IOException("Reader: Invalid .wav file - " + e.getMessage());
    }

    // metadata
    int numChannels = inputStream.getFormat().getChannels();
    if (numChannels <= 0 || numChannels > 2)
      throw new IllegalArgumentException("Reader: Does not support " + numChannels + "-channel audio");
    Mode mode = numChannels == 2 ? Mode.STEREO : Mode.MONO;
    int sampleRate = (int)inputStream.getFormat().getSampleRate();
    int bitDepth = inputStream.getFormat().getSampleSizeInBits();
    if (bitDepth != 16)
      throw new IllegalArgumentException("Reader: Does not support " + bitDepth + "-bit audio");

    // audio data
    short[] left, right;
    if (mode == Mode.STEREO) {
      short[][] channels = readWavStereo(inputStream);
      left = channels[0];
      right = channels[1];
    } else {
      left = readWavMono(inputStream);
      right = null;
    }
    inputStream.close();

    return new Reader(left, right, mode, sampleRate);
  }

  private static short[] readWavMono(AudioInputStream stream) throws IOException {
    List<Byte> soundBytes = readBytes(stream);

    // Convert bytes to audio samples
    short[] left = new short[soundBytes.size() / 2];
    for (int i = 0; i < soundBytes.size() - 1; i += 2) {
      left[i / 2] = (short) ((soundBytes.get(i) & 0xFF) | (soundBytes.get(i + 1) << 8));
    }

    return left;
  }

  private static short[][] readWavStereo(AudioInputStream stream) throws IOException {
    List<Byte> soundBytes = readBytes(stream);

    // Convert bytes to audio samples
    short[] left = new short[soundBytes.size() / 4];
    short[] right = new short[soundBytes.size() / 4];
    for (int i = 0; i < soundBytes.size() - 3; i += 4) {
      left[i / 4] = (short) ((soundBytes.get(i) & 0xFF) | (soundBytes.get(i + 1) << 8));
      right[i / 4] = (short) ((soundBytes.get(i + 2) & 0xFF) | (soundBytes.get(i + 3) << 8));
    }

    return new short[][]{left, right};
  }

  public static List<Byte> readBytes(AudioInputStream stream) throws IOException {
    List<Byte> soundBytes = new ArrayList<>(BUFFER_SIZE);
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;

    // Read bytes
    while ((bytesRead = stream.read(buffer)) != -1) {
      for (int i = 0; i < bytesRead; i++)
        soundBytes.add(buffer[i]);
    }

    return soundBytes;
  }

  public Mode getMode() {
    return mode;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  // Returns duration in seconds.
  public double getDuration() {
    return (double) left.length / (double) sampleRate;
  }

  // returns null for right channel of mono audio file.
  public short[] getChannel(Channel channel) {
    return (channel == Channel.LEFT) ? left : right;
  }

  // Prints out the values of the .mp3 or .wav file in args[0].
  public static void main(String[] args) {
    try {
      Reader reader = readFile(args[0]);

      // format info
      System.out.println("mode = " + reader.getMode());
      System.out.println("sample rate = " + reader.getSampleRate());
      System.out.println("duration = " + reader.getDuration());

      // left channel samples
      System.out.println("\nleft channel samples:");
      for (Short s : reader.getChannel(Channel.LEFT)) {
          System.out.println(s);
      }

      // right channel samples (if stereo)
      if (reader.getChannel(Channel.RIGHT) != null) {
        System.out.println("\nright channel samples:");
        for (Short s : reader.getChannel(Channel.RIGHT)) {
          System.out.println(s);
        }
      }

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
