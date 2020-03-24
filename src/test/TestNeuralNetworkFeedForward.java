package test;

import java.util.Arrays;
import neat.ConnectionGenome;
import neat.Genome;
import neat.InnovationNumber;
import neat.NeuralNetwork;
import neat.NodeGenome;
import neat.NodeType;

public class TestNeuralNetworkFeedForward {

  public static void main(String[] args) {

    Genome genesis = new Genome();
    genesis.addNode(new NodeGenome(NodeType.INPUT, 0));
    genesis.addNode(new NodeGenome(NodeType.INPUT, 1));
    genesis.addNode(new NodeGenome(NodeType.INPUT, 2));
    genesis.addNode(new NodeGenome(NodeType.OUTPUT, 4));

    genesis.addConnections(new ConnectionGenome(0, 4, 0.5f, true, 0));
    genesis.addConnections(new ConnectionGenome(1, 4, 0.5f, true, 1));
    genesis.addConnections(new ConnectionGenome(2, 4, 0.5f, true, 2));


    NeuralNetwork nn = new NeuralNetwork(genesis);
    float[] inputs = {30f, 1f, 10f};
    System.out.println(Arrays.toString(nn.feedForward(inputs)));
  }

}
