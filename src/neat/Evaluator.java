package neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Evaluator {

  private GenomeComparator comparator = new GenomeComparator();

  private int populationSize;
  private CONFIGURATION configuration;

  private Random random = new Random();

  private List<Genome> population;
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

    nextGeneration = new ArrayList<>();
    speciesMap = new HashMap<>();
    fitnessMap = new HashMap<>();

  }

  public void evaluate() {

    for (Species s : species) {
      s.resetSpecies(random);
    }
    fitnessMap.clear();
    speciesMap.clear();
    nextGeneration.clear();

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

      fitnessMap.put(g, fitness);

      if (fitness > highestScore) {
        highestScore = fitness;
        bestGenome = g;
      }

    }

    // Carry over the best two genomes in each species
    nextGeneration.add(bestGenome);

    for (Species s : species) {
      s.fitnessPairs.sort(comparator);
      Collections.reverse(s.fitnessPairs);
      nextGeneration.add(s.fitnessPairs.get(0).g);
      if (s.fitnessPairs.size() >= 2) {
        nextGeneration.add(s.fitnessPairs.get(1).g);
      }
    }

    // Breed for the remaining places
    while (nextGeneration.size() < populationSize) {
      Species s1 = getRandomSpecies();
      Species s2 = getRandomSpecies();

      Genome parent1 = getRandomGenome(s1);
      Genome parent2 = getRandomGenome(s2);

      if (random.nextFloat() < configuration.MUTATION_WITHOUT_CROSSOVER) {
        if (fitnessMap.get(parent1) > fitnessMap.get(parent2)) {
          Genome mutatedChild = new Genome(parent1);
          mutatedChild.mutation(random);
          nextGeneration.add(mutatedChild);
        } else {
          Genome mutatedChild = new Genome(parent2);
          mutatedChild.mutation(random);
          nextGeneration.add(mutatedChild);
        }
      } else {
        Genome child;
        if (fitnessMap.get(parent1) > fitnessMap.get(parent2)) {
          child = Genome.crossover(parent1, parent2, random);
        } else {
          child = Genome.crossover(parent2, parent1, random);
        }

        if (random.nextFloat() < configuration.MUTATION_THRESHOLD) {
          child.mutation(random);
        }
        if (random.nextFloat() < configuration.ADD_CONNECTION_THRESHOLD) {
          child.newConnectionMutation(random);
        }
        if (random.nextFloat() < configuration.ADD_NODE_THRESHOLD) {
          child.newNodeMutation(random);
        }

        nextGeneration.add(child);

      }

    }

    // Reset for next generation
    population = nextGeneration;
  }

  private Genome getRandomGenome(Species s1) {
    float totalFitness = 0f;
    for (GenomeFitnessPair gf : s1.fitnessPairs) {
      totalFitness += gf.fitness;
    }

    float targetFitness = (float) (Math.random() * totalFitness);
    float runningFitness = 0f;

    for (GenomeFitnessPair gf : s1.fitnessPairs) {
      runningFitness += gf.fitness;
      if (targetFitness < runningFitness) {
        return gf.g;
      }
    }
    throw new IndexOutOfBoundsException("Genome not available");

  }

  private Species getRandomSpecies() {
    float totalFitness = 0f;
    for (Species s : species) {
      totalFitness += s.totalFitness;
    }

    float targetFitness = (float) (Math.random() * totalFitness);
    float runningFitness = 0f;

    for (Species s : species) {
      runningFitness += s.totalFitness;
      if (targetFitness < runningFitness) {
        return s;
      }
    }
    throw new IndexOutOfBoundsException("Species not available");
  }

  protected abstract float evaluateGenome(Genome g);
}
