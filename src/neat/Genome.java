package neat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Genome {

  // Nodes mapped by id
  private Map<Integer, NodeGenome> nodes;
  // Connections mapped by the innovation number
  private Map<Integer, ConnectionGenome> connections;

  // Global innovation to keep track of the connection numbers
  private InnovationNumber connectionInnovation = new InnovationNumber();

  private static List<Integer> throwAwayList1 = new ArrayList<>();
  private static List<Integer> throwAwayList2 = new ArrayList<>();

  // Probability of mutating the weight of a connection
  public final float MUTATION_PROBABILITY = 0.9f;

  public Genome() {
    nodes = new HashMap<>();
    connections = new HashMap<>();
  }

  public Genome(Genome starter) {
    nodes = new HashMap<>();
    connections = new HashMap<>();

    for (int i : starter.getNodes().keySet()) {
      nodes.put(i, starter.getNodes().get(i).copy());
    }

    for (int i : starter.getConnections().keySet()) {
      connections.put(i, starter.getConnections().get(i).copy());
    }
  }

  public void addNode(NodeGenome node) {
    nodes.put(node.getId(), node);
  }

  public void addConnections(ConnectionGenome connection) {
    connections.put(connection.getInnovationNo(), connection);
  }

  public Map<Integer, NodeGenome> getNodes() {
    return nodes;
  }

  public Map<Integer, ConnectionGenome> getConnections() {
    return connections;
  }

  public void mutation(Random r) {
    for (ConnectionGenome c : connections.values()) {
      if (r.nextFloat() < MUTATION_PROBABILITY) {
        c.setWeight(c.getWeight() * (r.nextFloat() * 2f - 1f));   // Slight change
      } else {
        c.setWeight(r.nextFloat() * 2f - 1f);                     // New value
      }
    }
  }

  public void newConnectionMutation(Random r) {
    NodeGenome node1 = nodes.get(r.nextInt(nodes.size()));
    NodeGenome node2 = nodes.get(r.nextInt(nodes.size()));
    float weight = r.nextFloat() * 2f - 1f;   // Scale it to allow a range of -1 to 1 inclusive

    if ((node1.getType() == NodeType.HIDDEN && node2.getType() == NodeType.INPUT) || (
        node1.getType() == NodeType.OUTPUT && node2.getType() == NodeType.HIDDEN) || (
        node1.getType() == NodeType.OUTPUT && node2.getType() == NodeType.INPUT)) {
      NodeGenome temp = node1;
      node1 = node2;
      node2 = temp;
    }

    if (!(node1.getType() == NodeType.INPUT && node2.getType() == NodeType.INPUT) &&
        !(node1.getType() == NodeType.HIDDEN && node2.getType() == NodeType.HIDDEN) &&
        !(node1.getType() == NodeType.OUTPUT && node2.getType() == NodeType.OUTPUT)) {

      boolean exists = false;
      for (ConnectionGenome c : connections.values()) {
        if (c.getInputNode() == node1.getId() && c.getOutputNode() == node2.getId()) {
          exists = true;
          break;
        }
      }

      if (!exists) {
        ConnectionGenome newConnection = new ConnectionGenome(node1.getId(), node2.getId(), weight,
            true, connectionInnovation.getInnovationNo());
        connections.put(newConnection.getInnovationNo(), newConnection);
      }
    }

  }

  public void newNodeMutation(Random r) {
    ConnectionGenome c = connections.get(r.nextInt(connections.size()));

    c.disable();

    NodeGenome node1 = nodes.get(c.getInputNode());
    NodeGenome node2 = nodes.get(c.getOutputNode());
    NodeGenome newNode = new NodeGenome(NodeType.HIDDEN, nodes.size());

    ConnectionGenome connection1 = new ConnectionGenome(node1.getId(), newNode.getId(), 1f, true,
        connectionInnovation.getInnovationNo());
    ConnectionGenome connection2 = new ConnectionGenome(newNode.getId(), node2.getId(),
        c.getWeight(), true, connectionInnovation.getInnovationNo());

    nodes.put(newNode.getId(), newNode);
    connections.put(connection1.getInnovationNo(), connection1);
    connections.put(connection2.getInnovationNo(), connection2);
  }

  public static Genome crossover(Genome parent1, Genome parent2, Random r) {
    // PRE: Parent1.fitness >= Parent2.fitness
    Genome child = new Genome();

    for (NodeGenome n : parent1.getNodes().values()) {
      // Add all the nodes from Parent1 as this is the higher fitness so we know all these nodes
      // will be included
      child.addNode(n.copy());
    }

    // If matching: pick randomly
    // If excess/disjoint: pick the fittest parent's gene
    for (ConnectionGenome parent1Connection : parent1.getConnections().values()) {
      // Check for matching connection gene
      if (parent2.getConnections().containsKey(parent1Connection.getInnovationNo())) {
        // Matching gene
        // Random boolean = True -> Pick Parent1
        ConnectionGenome childConnection = r.nextBoolean() ? parent1Connection.copy()
            : parent2.getConnections().get(parent1Connection.getInnovationNo());
        child.addConnections(childConnection);
      } else {
        // Excess or disjoint genes
        child.addConnections(parent1Connection.copy());
      }
    }
    return child;
  }

  public static float compatibilityDistance(Genome genome1, Genome genome2, float c1, float c2,
      float c3) {
    int[] excessDisjoint = countExcessDisjoint(genome1, genome2);
    float avWeightDifference = getAverageWeightDifference(genome1, genome2);

    return c1 * excessDisjoint[0] + c2 * excessDisjoint[1] + c3 * avWeightDifference;
  }

  public static int[] countExcessDisjoint(Genome genome1, Genome genome2) {
    int excessGenes = 0;
    int disjointGenes = 0;

    // Count the nodes
    List<Integer> nodeOneKeys = asSortedList(genome1.getNodes().keySet(), throwAwayList1);
    List<Integer> nodeTwoKeys = asSortedList(genome2.getNodes().keySet(), throwAwayList2);

    int gene1Innovation = nodeOneKeys.get(nodeOneKeys.size() - 1);
    int gene2Innovation = nodeTwoKeys.get(nodeTwoKeys.size() - 1);
    int highestInnovation = Math.max(gene1Innovation, gene2Innovation);

    for (int i = 0; i <= highestInnovation; i++) {
      excessGenes = getDisjointExcessNodes(genome1, genome2, excessGenes, i, gene1Innovation < i,
          gene2Innovation < i);

      disjointGenes = getDisjointExcessNodes(genome1, genome2, disjointGenes, i,
          gene1Innovation > i, gene2Innovation > i
      );
    }

    // Count the connections
    List<Integer> connectionOneKeys = asSortedList(genome1.getConnections().keySet(),
        throwAwayList1);
    List<Integer> connectionTwoKeys = asSortedList(genome2.getConnections().keySet(),
        throwAwayList2);

    gene1Innovation = connectionOneKeys.get(nodeOneKeys.size() - 1);
    gene2Innovation = connectionTwoKeys.get(nodeTwoKeys.size() - 1);
    highestInnovation = Math.max(gene1Innovation, gene2Innovation);

    for (int i = 0; i <= highestInnovation; i++) {
      excessGenes = getExcessDisjointConnections(genome1, genome2, excessGenes, i,
          gene1Innovation < i,
          gene2Innovation < i
      );

      disjointGenes = getExcessDisjointConnections(genome1, genome2, disjointGenes, i,
          gene1Innovation > i, gene2Innovation > i);
    }

    // Return as array
    return new int[]{excessGenes, disjointGenes};
  }

  private static int getDisjointExcessNodes(Genome genome1, Genome genome2, int count,
      int i, boolean b, boolean b2) {
    if (genome1.getNodes().get(i) == null && b
        && genome2.getNodes().get(i) != null) {
      count++;
    } else if (genome2.getNodes().get(i) == null && b2
        && genome1.getNodes().get(i) != null) {
      count++;
    }
    return count;
  }

  private static int getExcessDisjointConnections(Genome genome1, Genome genome2, int count, int i,
      boolean b, boolean b2) {
    if (genome1.getConnections().get(i) == null && b
        && genome2.getConnections().get(i) != null) {
      count++;
    } else if (genome2.getConnections().get(i) == null && b2
        && genome1.getConnections().get(i) != null) {
      count++;
    }
    return count;
  }

  public static float getAverageWeightDifference(Genome genome1, Genome genome2) {
    int matchingGenes = 0;
    int weightDifference = 0;

    List<Integer> connectionOneKeys = asSortedList(genome1.getConnections().keySet(),
        throwAwayList1);
    List<Integer> connectionTwoKeys = asSortedList(genome2.getConnections().keySet(),
        throwAwayList2);

    int gene1Innovation = connectionOneKeys.get(connectionOneKeys.size() - 1);
    int gene2Innovation = connectionTwoKeys.get(connectionTwoKeys.size() - 1);
    int highestInnovation = Math.max(gene1Innovation, gene2Innovation);

    for (int i = 0; i <= highestInnovation; i++) {
      if (genome1.getConnections().get(i) != null && genome2.getConnections().get(i) != null) {
        matchingGenes++;
        weightDifference += Math.abs(
            genome1.getConnections().get(i).getWeight() - genome2.getConnections().get(i)
                .getWeight());
      }
    }

    return weightDifference / (float) matchingGenes;
  }

  private static List<Integer> asSortedList(Collection<Integer> c, List<Integer> throwAway) {
    // Taken from StackOverflow:
    // https://stackoverflow.com/questions/740299/how-do-i-sort-a-set-to-a-list-in-java
    // Helper to sort in ascending
    throwAway.clear();
    throwAway.addAll(c);
    java.util.Collections.sort(throwAway);
    return throwAway;
  }

  public static void print(Genome g) {
    for (NodeGenome n : g.getNodes().values()) {
      System.out.println(n.getId());
    }
    for (ConnectionGenome c : g.getConnections().values()) {
      System.out.println("");
      System.out.println("Innovation:" + c.getInnovationNo());
      System.out.println("Connection: " + c.getInputNode() + " -> " + c.getOutputNode());
      System.out.println("Enabled: " + c.isActive());
    }
  }


  public static void main(String[] args) {
    Genome g1 = new Genome();

    g1.addNode(new NodeGenome(NodeType.INPUT, 1));
    g1.addNode(new NodeGenome(NodeType.INPUT, 2));
    g1.addNode(new NodeGenome(NodeType.INPUT, 3));
    g1.addNode(new NodeGenome(NodeType.OUTPUT, 4));
    g1.addNode(new NodeGenome(NodeType.HIDDEN, 5));

    g1.addConnections(new ConnectionGenome(1, 4, 1.0f, true, 1));
    g1.addConnections(new ConnectionGenome(2, 4, 1.0f, false, 2));
    g1.addConnections(new ConnectionGenome(3, 4, 1.0f, true, 3));
    g1.addConnections(new ConnectionGenome(2, 5, 1.0f, true, 4));
    g1.addConnections(new ConnectionGenome(5, 4, 1.0f, true, 5));
    g1.addConnections(new ConnectionGenome(1, 5, 1.0f, true, 8));

    Genome g2 = new Genome();

    g1.addNode(new NodeGenome(NodeType.INPUT, 1));
    g1.addNode(new NodeGenome(NodeType.INPUT, 2));
    g1.addNode(new NodeGenome(NodeType.INPUT, 3));
    g1.addNode(new NodeGenome(NodeType.OUTPUT, 4));
    g1.addNode(new NodeGenome(NodeType.HIDDEN, 5));
    g2.addNode(new NodeGenome(NodeType.HIDDEN, 6));

    g2.addConnections(new ConnectionGenome(1, 4, 2.0f, true, 1));
    g2.addConnections(new ConnectionGenome(2, 4, 1.0f, false, 2));
    g2.addConnections(new ConnectionGenome(3, 4, 1.0f, true, 3));
    g2.addConnections(new ConnectionGenome(2, 5, 1.0f, true, 4));
    g2.addConnections(new ConnectionGenome(5, 4, 1.0f, false, 5));
    g2.addConnections(new ConnectionGenome(5, 6, 1.0f, true, 6));
    g2.addConnections(new ConnectionGenome(6, 4, 1.0f, true, 7));
    g2.addConnections(new ConnectionGenome(3, 5, 1.0f, true, 9));
    g2.addConnections(new ConnectionGenome(1, 6, 1.0f, true, 10));

    System.out.println(Arrays.toString(countExcessDisjoint(g1, g2)));
    System.out.println(getAverageWeightDifference(g1, g2));
  }
}
