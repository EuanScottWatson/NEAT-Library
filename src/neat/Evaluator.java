package neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Evaluator {

  private GenomeComparator comparator = new GenomeComparator();

  private int populationSize;
  private CONFIGURATION configuration;
  private InnovationNumber counter = new InnovationNumber();

  private Random random = new Random();

  private List<Genome> population;
  private List<GenomeFitnessPair> fitnessPairs;
  private List<Genome> nextGeneration;  // Only used temporarily during evaluation
  private List<Species> species;

  private Map<Genome, Species> speciesMap;
  private Map<Genome, Float> fitnessMap;

  private float highestScore;
  private Genome bestGenome;


  public Evaluator(int populationSize, Genome starter) {
    this.populationSize = populationSize;
    configuration = new CONFIGURATION(populationSize);

    population = new ArrayList<>();
    species = new ArrayList<>();

    for (int i = 0; i < populationSize; i++) {
      population.add(new Genome(starter));
    }

  }

  public void evaluate() {
    // Place genomes into respective species
    for (Genome g : population) {
      boolean foundSpecies = false;
      for (Species s : species) {
        if (Genome.compatibilityDistance(g, s.leader, configuration.C1, configuration.C2,
            configuration.C3) < configuration.DISTANCE_THRESHOLD) {
          s.population.add(g);
          speciesMap.put(g, s);
          foundSpecies = true;
          break;
        }
      }

      if (!foundSpecies) {
        Species newSpecies = new Species(g);
        species.add(newSpecies);
        speciesMap.put(g, newSpecies);
      }

    }

    species.removeIf(s -> s.population.isEmpty());

    // Evaluate the genomes in each species
    for (Genome g : population) {
      Species s = speciesMap.get(g);

      float fitness = evaluateGenome(g) / s.population.size();
      GenomeFitnessPair newPair = new GenomeFitnessPair(g, fitness);

      s.addFitness(fitness);
      s.fitnessPairs.add(newPair);

      fitnessPairs.add(newPair);
      fitnessMap.put(g, fitness);

      if (fitness > highestScore) {
        highestScore = fitness;
        bestGenome = g;
      }

    }

    // Carry over the best two genomes in each species
    nextGeneration.add(bestGenome);

    for (Species s : species) {
      if (s.population.size() > 5) {
        s.fitnessPairs.sort(comparator);
        Collections.reverse(s.fitnessPairs);
        for (int i = 0; i < 2; i++) {
          nextGeneration.add(s.fitnessPairs.get(i).g);
        }
      }
    }

    // Breed for the remaining places
    // Reset for next generation
  }

  public float evaluateGenome(Genome g) {
    return 0f;
  }
}
