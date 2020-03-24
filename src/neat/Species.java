package neat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Species {

  public Genome leader;
  public List<Genome> population;
  public List<GenomeFitnessPair> fitnessPairs;
  public float totalFitness = 0f;

  public Species(Genome leader) {
    this.leader = leader;
    population = new ArrayList<>();
    fitnessPairs = new ArrayList<>();
    population.add(leader);
  }

  public void addFitness(float fitness) {
    totalFitness += fitness;
  }

  public void resetSpecies(Random r) {
    leader = population.get(r.nextInt(population.size()));
    population = new ArrayList<>();
    fitnessPairs = new ArrayList<>();
    totalFitness = 0f;
  }


}
