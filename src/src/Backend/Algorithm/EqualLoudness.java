package Backend.Algorithm;

import Backend.Helper.PrintHelper;

/**
 * @author Ethan Carnahan
 * Converts loudness from decibels to phons based on frequency.
 * See <a href="https://www.desmos.com/calculator/n90bihrual">how I calculate it.</a>
 */
public class EqualLoudness {
  //region Equal loudness values
  private static final double[] frequencies = {20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315,
      400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500};

  private static final double[] af = {0.532, 0.506, 0.480, 0.455, 0.432, 0.409, 0.387, 0.367,
      0.349, 0.330, 0.315, 0.301, 0.288, 0.276, 0.267, 0.259, 0.253, 0.250, 0.246, 0.244,
      0.243, 0.243, 0.243, 0.242, 0.242, 0.245, 0.254, 0.271, 0.301};

  private static final double[] Lu = {-31.6, -27.2, -23.0, -19.1, -15.9, -13.0, -10.3, -8.1, -6.2, -4.5,
      -3.1, -2.0, -1.1, -0.4, 0.0, 0.3, 0.5, 0.0, -2.7, -4.1, -1.0, 1.7,
      2.5, 1.2, -2.1, -7.1, -11.2, -10.7, -3.1};

  public static final double[] Tf = {78.5, 68.7, 59.5, 51.1, 44.0, 37.5, 31.5, 26.5, 22.1, 17.9, 14.4,
      11.4, 8.6, 6.2, 4.4, 3.0, 2.2, 2.4, 3.5, 1.7, -1.3, -4.2,
      -6.0, -5.4, -1.5, 6.0, 12.6, 13.9, 12.3};
  //endregion

  //region Methods
  public static double phonsToDb(double phons, double frequency) {
    // Get constants
    double valTf = interpolate(Tf, frequency);
    double valAf = interpolate(af, frequency);
    double valLu = interpolate(Lu, frequency);

    // Calculate Af term
    double a = 0.00447 * (Math.pow(10, (0.025 * phons) ) - 1.15);
    double b = Math.pow(0.4 * Math.pow(10, ( ( (valTf + valLu) / 10) - 9) ), valAf);
    double af = a + b;

    // Calculate Lp term
    a = (10 / valAf) * (Math.log10(af));
    b = -valLu;
    return a + b + 94;
  }

  public static double dbToPhons(double dB, double frequency) {
    // Get constants
    double valTf = interpolate(Tf, frequency);
    double valAf = interpolate(af, frequency);
    double valLu = interpolate(Lu, frequency);

    // Calculate big terms
    double a = Math.pow(10, ( (valAf / 10) * (dB + valLu - 94) ) );
    double b = Math.pow(0.4 * Math.pow(10, ( ( (valTf + valLu) / 10) - 9) ), valAf);
    double af = a - b;

    // Calculate result value
    return 40 * Math.log10( (af / 0.00447) + 1.15);
  }

  // Used to interpolate for frequencies between/outside the frequencies array.
  private static double interpolate(double[] outArray, double frequency) {
    // Check array bounds
    if (frequency < frequencies[0])
      return outArray[0];
    if (frequency > frequencies[frequencies.length-1])
      return outArray[outArray.length-1];

    // Find first index in array equal or higher than given value.
    int index = 1;
    while (index < frequencies.length && frequencies[index] < frequency)
      index++;

    // Use exact value if it matches array value.
    if (frequencies[index-1] == frequency)
      return outArray[index-1];

    // Calculate where value is in-between array values.
    double inStart = frequencies[index-1], inEnd = frequencies[index];
    double startToEnd = (frequency - inStart) / (inEnd - inStart);

    double outStart = outArray[index-1], outEnd = outArray[index];
    double outDifference = outEnd - outStart;

    return outStart + (outDifference * startToEnd);
  }
  //endregion

  // Print the equal loudness contours of the 0, 10, 20, ..., 90 phon lines.
  public static void main(String[] args) {
    double[][] phonLines = new double[10][Transform.FREQUENCY_RESOLUTION];
    for (int i = 0; i < 100; i += 10) {
      for (int j = 0; j < Transform.FREQUENCY_RESOLUTION; j++) {
        double frequency = Transform.BOTTOM_FREQUENCY * Math.pow(Transform.TOP_BOTTOM_RATIO,
            (double) j / Transform.FREQUENCY_RESOLUTION);
        phonLines[i/10][j] = phonsToDb(i, frequency);
      }
    }

    PrintHelper.printFrequencies();
    for (int i = 0; i < 100; i += 10) {
      PrintHelper.printValues(i + " phons", phonLines[i/10]);
    }
  }
}
