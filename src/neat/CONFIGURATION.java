package neat;

public class CONFIGURATION {

  public float C1 = 1.0f;
  public float C2 = 1.0f;
  public float C3 = 0.4f;
  public float DISTANCE_THRESHOLD = 3.0f;
  public float MUTATION_THRESHOLD = 0.9f;
  public float ADD_CONNECTION_THRESHOLD = 0.05f;
  public float ADD_NODE_THRESHOLD = 0.03f;
  public float MUTATION_WITHOUT_CROSSOVER = 0.25f;

  private int populationSize;

  public CONFIGURATION(int populationSize) {
    this.populationSize = populationSize;
  }

  public int getPopulationSize() {
    return populationSize;
  }
}
