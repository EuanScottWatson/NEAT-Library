package neat;

import java.util.Comparator;

public class GenomeComparator implements Comparator<GenomeFitnessPair> {

  @Override
  public int compare(GenomeFitnessPair g1, GenomeFitnessPair g2) {
    if (g1.fitness > g2.fitness) {
      return 1;
    } else if (g1.fitness < g2.fitness) {
      return -1;
    }
    return 0;
  }

}
