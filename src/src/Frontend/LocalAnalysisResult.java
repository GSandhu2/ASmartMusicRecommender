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

    JFreeChart chart = ChartFactory.createLineChart("Frequency Response of " + filename,
        "Frequency Bins", "Perceived Loudness", dataset,
        PlotOrientation.VERTICAL, false, false, false);

    ChartPanel panel = new ChartPanel(chart);
    setContentPane(panel);

    pack();
    setSize(1000, 500);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  private static DefaultCategoryDataset createDataset(SimpleCharacteristics characteristics) {
    String seriesName = "Perceived Loudness";
    DefaultCategoryDataset result = new DefaultCategoryDataset();

    double[] loudness = getCenterLoudness(characteristics);
    for (int i = 0; i < Transform.FREQUENCY_RESOLUTION; i++)
      result.addValue(loudness[i], seriesName, String.valueOf((int)(Transform.frequencyAtBin(i))));

    return result;
  }

  // For mono: Returns left channel loudness
  // For stereo: Returns 0.5*left + 0.5*right.
  private static double[] getCenterLoudness(SimpleCharacteristics characteristics) {
    if (characteristics.getAverageVolume(Channel.RIGHT) != null) {
      double[] result = new double[Transform.FREQUENCY_RESOLUTION];

      double[] left = characteristics.getAverageVolume(Channel.LEFT);
      double[] right = characteristics.getAverageVolume(Channel.RIGHT);
      for (int i = 0; i < result.length; i++)
        result[i] = (left[i] + right[i]) / 2.0;

      return result;
    } else {
      return characteristics.getAverageVolume(Channel.LEFT);
    }
  }
}
