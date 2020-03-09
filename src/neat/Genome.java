package neat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Genome {

  // Nodes mapped by id
  private Map<Integer, NodeGenome> nodes;
  // Connections mapped by the innovation number
  private Map<Integer, ConnectionGenome> connections;

  // Global innovation to keep track of the connection numbers
  private InnovationNumber connectionInnovation = new InnovationNumber(1);

  // Probability of mutating the weight of a connection
  public final float mutationProb = 0.9f;

  public Genome() {
    nodes = new HashMap<Integer, NodeGenome>();
    connections = new HashMap<Integer, ConnectionGenome>();
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

  public void addNodeMutation(Random r) {
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
        // Random double < 0.5 = Pick Parent1
        if ((r.nextDouble() < 0.5)) {
          child.addConnections(parent1Connection.copy());
        } else {
          child.addConnections(parent2.getConnections().get(parent1Connection.getInnovationNo()));
        }
      } else {
        child.addConnections(parent1Connection);
      }
    }
    return child;
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
    Genome p1 = new Genome();

    p1.addNode(new NodeGenome(NodeType.INPUT, 1));
    p1.addNode(new NodeGenome(NodeType.INPUT, 2));
    p1.addNode(new NodeGenome(NodeType.INPUT, 3));
    p1.addNode(new NodeGenome(NodeType.HIDDEN, 5));
    p1.addNode(new NodeGenome(NodeType.OUTPUT, 4));

    p1.addConnections(new ConnectionGenome(1, 4, 0.5f, true, 1));
    p1.addConnections(new ConnectionGenome(2, 4, 0.5f, false, 2));
    p1.addConnections(new ConnectionGenome(3, 4, 0.5f, true, 3));
    p1.addConnections(new ConnectionGenome(2, 5, 0.5f, true, 4));
    p1.addConnections(new ConnectionGenome(5, 4, 0.5f, true, 5));
    p1.addConnections(new ConnectionGenome(1, 5, 0.5f, true, 8));

    Genome p2 = new Genome();

    p2.addNode(new NodeGenome(NodeType.INPUT, 1));
    p2.addNode(new NodeGenome(NodeType.INPUT, 2));
    p2.addNode(new NodeGenome(NodeType.INPUT, 3));
    p2.addNode(new NodeGenome(NodeType.HIDDEN, 5));
    p2.addNode(new NodeGenome(NodeType.HIDDEN, 6));
    p2.addNode(new NodeGenome(NodeType.OUTPUT, 4));

    p2.addConnections(new ConnectionGenome(1, 4, 0.5f, true, 1));
    p2.addConnections(new ConnectionGenome(2, 4, 0.5f, false, 2));
    p2.addConnections(new ConnectionGenome(3, 4, 0.5f, true, 3));
    p2.addConnections(new ConnectionGenome(2, 5, 0.5f, true, 4));
    p2.addConnections(new ConnectionGenome(5, 4, 0.5f, true, 5));
    p2.addConnections(new ConnectionGenome(5, 6, 0.5f, true, 6));
    p2.addConnections(new ConnectionGenome(6, 4, 0.5f, true, 7));
    p2.addConnections(new ConnectionGenome(3, 5, 0.5f, true, 9));
    p2.addConnections(new ConnectionGenome(1, 6, 0.5f, true, 10));

    Genome child = crossover(p2, p1, new Random());
    print(child);

  }

}
