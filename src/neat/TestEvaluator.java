package neat;

import java.util.Random;

public class TestEvaluator {

  public static void main(String[] args) {

    InnovationNumber nodeInnovation = new InnovationNumber();
    InnovationNumber connectionInnovation = new InnovationNumber();

    Genome genesis = new Genome();
    int n1 = nodeInnovation.getInnovationNo();
    int n2 = nodeInnovation.getInnovationNo();
    int n3 = nodeInnovation.getInnovationNo();
    genesis.addNode(new NodeGenome(NodeType.INPUT, n1));
    genesis.addNode(new NodeGenome(NodeType.INPUT, n2));
    genesis.addNode(new NodeGenome(NodeType.OUTPUT, n3));

    genesis.addConnections(
        new ConnectionGenome(n1, n3, 0.5f, true, connectionInnovation.getInnovationNo()));
    genesis.addConnections(
        new ConnectionGenome(n2, n3, 0.5f, true, connectionInnovation.getInnovationNo()));

    Evaluator evaluator = new Evaluator(100, genesis, nodeInnovation, connectionInnovation) {
      @Override
      protected float evaluateGenome(Genome g) {
        float totalWeight = 0f;
        for (ConnectionGenome c : g.getConnections().values()) {
          if (c.isActive()) {
            totalWeight += Math.abs(c.getWeight());
          }
        }
        return (1000f / (Math.abs(totalWeight - 100f)));
      }
    };

    for (int i = 0; i < 100; i++) {
      evaluator.evaluate();
      System.out.print("Generation: " + (i + 1));
      System.out.print("\t\tNumber of Species: " + evaluator.getSpeciesSize());
      System.out.print("\t\tHighest Fitness: " + evaluator.getHighestScore());
      System.out.print("\t\tNumber in best Species: " + evaluator.getSpeciesMap().get(evaluator.getBestGenome()).population.size());

      float totalWeight = 0f;
      for (ConnectionGenome c : evaluator.getBestGenome().getConnections().values()) {
        if (c.isActive()) {
          totalWeight += Math.abs(c.getWeight());
        }
      }
      System.out.println("\t\tSum of weights: " + totalWeight);
    }
    Genome.print(evaluator.getBestGenome());

  }

}
