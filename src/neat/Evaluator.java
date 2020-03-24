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

  private InnovationNumber nodeInnovation;
  private InnovationNumber connectionInnovation;

  private Random random = new Random();

  private List<Genome> population;
  private List<Genome> nextGeneration;  // Only used temporarily during evaluation
  private List<Species> species;

  private Map<Genome, Species> speciesMap;
  private Map<Genome, Float> fitnessMap;

  private float highestScore;
  private Genome bestGenome;


  public Evaluator(int populationSize, Genome starter, InnovationNumber nodeInnovation,
      InnovationNumber connectionInnovation) {
    this.nodeInnovation = nodeInnovation;
    this.connectionInnovation = connectionInnovation;
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

  public Map<Genome, Species> getSpeciesMap() {
    return speciesMap;
  }

  public Map<Genome, Float> getFitnessMap() {
    return fitnessMap;
  }

  public float getHighestScore() {
    return highestScore;
  }

  public Genome getBestGenome() {
    return bestGenome;
  }

  public int getSpeciesSize() {
    return species.size();
  }

  public void evaluate() {
    //System.out.println(bestGenome);
    //System.out.println();

    //System.out.println("Evaluating...");
    for (Species s : species) {
      s.resetSpecies(random);
    }
    fitnessMap.clear();
    speciesMap.clear();
    nextGeneration = new ArrayList<>();

    //System.out.println("\t\tSpeciating...");
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

    //System.out.println("\t\tEvaluating Genomes...");
    // Evaluate the genomes in each species
    for (Genome g : population) {
      Species s = speciesMap.get(g);

      float fitness = evaluateGenome(g) / s.population.size();
      GenomeFitnessPair newPair = new GenomeFitnessPair(g, fitness);

      s.addFitness(fitness);
      s.fitnessPairs.add(newPair);

      fitnessMap.put(g, fitness);

      if (fitness > highestScore) {
        float totalWeight = 0f;
        for (ConnectionGenome c : g.getConnections().values()) {
          if (c.isActive()) {
            totalWeight += Math.abs(c.getWeight());
          }
        }
        //System.out.println("Sum of weights: " + totalWeight);
        highestScore = fitness;
        bestGenome = g;
        //System.out.println(evaluateGenome(bestGenome));
      }

    }

    // Carry over the best two genomes in each species
    //System.out.println("\t\tCarry over...");
    nextGeneration.add(bestGenome);
    //System.out.println(bestGenome);
    //System.out.println(evaluateGenome(bestGenome));

    for (Species s : species) {
      s.fitnessPairs.sort(comparator);
      Collections.reverse(s.fitnessPairs);
      nextGeneration.add(s.fitnessPairs.get(0).g);
      if (s.fitnessPairs.size() >= 2) {
        nextGeneration.add(s.fitnessPairs.get(1).g);
      }
    }

    // Breed for the remaining places
    //System.out.println("\t\tBreeding...");
    while (nextGeneration.size() < populationSize) {
      boolean found = false;
      Species s1 = getRandomSpecies();
      Species s2 = getRandomSpecies();

      Genome parent1 = getRandomGenome(s1);
      Genome parent2 = getRandomGenome(s2);

      while (!found) {
        s1 = getRandomSpecies();
        s2 = getRandomSpecies();

        parent1 = getRandomGenome(s1);
        parent2 = getRandomGenome(s2);

        if (parent1 != bestGenome && parent2 != bestGenome) {
          found = true;
        }
      }

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
          child.newConnectionMutation(random, connectionInnovation);
        }
        if (random.nextFloat() < configuration.ADD_NODE_THRESHOLD) {
          child.newNodeMutation(random, connectionInnovation, nodeInnovation);
        }

        nextGeneration.add(child);

      }

    }
    //System.out.println("After breeding");
    //System.out.println(bestGenome);
    //System.out.println(evaluateGenome(bestGenome));

    // Reset for next generation
    population = nextGeneration;
    //System.out.println(bestGenome);
    //System.out.println(evaluateGenome(bestGenome));
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
      if (targetFitness <= runningFitness) {
        return s;
      }
    }
    throw new IndexOutOfBoundsException("Species not available");
  }

  protected abstract float evaluateGenome(Genome g);
}
