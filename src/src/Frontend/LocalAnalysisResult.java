package Frontend;

import Backend.Algorithm.Reader.Channel;
import Backend.Algorithm.SimpleCharacteristics;
import Backend.Algorithm.Transform;
import javax.swing.JFrame;

import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

// See https://www.javatpoint.com/jfreechart-line-chart
public class LocalAnalysisResult extends JFrame {

  public LocalAnalysisResult(String filename, SimpleCharacteristics characteristics) {
    super("Analysis of " + filename);

    DefaultCategoryDataset dataset = createDataset(characteristics);

    JFreeChart chart = ChartFactory.createLineChart("Simple Analysis of " + filename,
        "Frequency Bins", "Perceived Loudness", dataset,
        PlotOrientation.VERTICAL, true, true, false);

    ChartPanel panel = new ChartPanel(chart);
    setContentPane(panel);

    pack();
    setSize(1000, 500);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  private static DefaultCategoryDataset createDataset(SimpleCharacteristics characteristics) {
    String series1 = "Perceived Loudness", series2 = "Rise Speed", series3 = "Fall Speed";
    DefaultCategoryDataset result = new DefaultCategoryDataset();

    double[] loudness = getLoudness(characteristics);
    for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i++)
      result.addValue(loudness[i], series1, String.valueOf((int)(Transform.frequencyAtBin(i))));

    double[] rise = getRise(characteristics);
    for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i++)
      result.addValue(rise[i], series2, String.valueOf((int)(Transform.frequencyAtBin(i))));

    double[] fall = getFall(characteristics);
    for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i++)
      result.addValue(fall[i], series3, String.valueOf((int)(Transform.frequencyAtBin(i))));

    return result;
  }


  private static double[] getLoudness(SimpleCharacteristics characteristics) {
    return getCenter(characteristics.getAverageVolume(Channel.LEFT),
        characteristics.getAverageVolume(Channel.RIGHT));
  }

  private static double[] getRise(SimpleCharacteristics characteristics) {
    return getCenter(characteristics.getAverageRise(Channel.LEFT),
        characteristics.getAverageRise(Channel.RIGHT));
  }

  private static double[] getFall(SimpleCharacteristics characteristics) {
    return getCenter(characteristics.getAverageFall(Channel.LEFT),
        characteristics.getAverageFall(Channel.RIGHT));
  }

  // For mono: Returns left
  // For stereo: Returns 0.5*left + 0.5*right.
  private static double[] getCenter(double[] left, double[] right) {
    if (right != null) {
      double[] result = new double[left.length];

      for (int i = 0; i < result.length; i++)
        result[i] = (left[i] + right[i]) / 2.0;

      return result;
    } else {
      return left;
    }
  }
}
