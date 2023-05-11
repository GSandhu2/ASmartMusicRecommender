package Backend.Helper;

import Backend.Algorithm.Transform;
import java.text.DecimalFormat;

// Help us print arrays so Algorithm main methods aren't so chonky.
public class PrintHelper {

  public static final DecimalFormat format = new DecimalFormat("#####.00");

  public static void printFrequencies() {
    System.out.print("Frequencies:");
    for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i++) {
      System.out.print(" " + String.format("%8s", format.format(
          Transform.BOTTOM_FREQUENCY * Math.pow(Transform.TOP_BOTTOM_RATIO,
              (double) i / Transform.FREQUENCY_RESOLUTION))));
    }
    System.out.println();
  }

  public static void printValues(String title, double[] values) {
    System.out.print(String.format("%11s", title) + ":");
    for (double d : values)
      System.out.print(" " + String.format("%8s", format.format(d)));
    System.out.println();
  }

  public static void printValues(String title, float[] values) {
    System.out.print(String.format("%11s", title) + ":");
    for (float f : values)
      System.out.print(" " + String.format("%8s", format.format(f)));
    System.out.println();
  }
}
