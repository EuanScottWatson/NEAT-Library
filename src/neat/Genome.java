package neat;

import java.util.List;
import java.util.Random;

public class Genome {

  private List<ConnectionGenome> connections;
  private List<NodeGenome> nodes;

  private InnovationNumber connectionInnovation = new InnovationNumber(1);

  public void newConnectionMutation(Random r) {
    NodeGenome node1 = nodes.get(r.nextInt(nodes.size()));
    NodeGenome node2 = nodes.get(r.nextInt(nodes.size()));
    float weight = r.nextFloat() * 2f - 1f;   // Scale it to allow a range of -1 to 1 inclusive

    boolean reverse = false;
    if ((node1.getType() == NodeType.HIDDEN && node2.getType() == NodeType.INPUT) || (
        node1.getType() == NodeType.OUTPUT && node2.getType() == NodeType.HIDDEN) || (
        node1.getType() == NodeType.OUTPUT && node2.getType() == NodeType.INPUT)) {
      reverse = true;
    }

    // Always makes node1 the input node and node2 the output
    if (reverse) {
      NodeGenome temp = node1;
      node1 = node2;
      node2 = temp;
    }

    boolean exists = false;
    for (ConnectionGenome c : connections) {
      if (c.getInputNode() == node1.getId() && c.getOutputNode() == node2.getId()) {
        exists = true;
        break;
      }
    }

    if (!exists) {
      connections.add(
          new ConnectionGenome(node1.getId(), node2.getId(), weight, true,
              connectionInnovation.getInnovationNo()));
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

    nodes.add(newNode);
    connections.add(connection1);
    connections.add(connection2);

  }

}
