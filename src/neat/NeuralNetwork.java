package neat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NeuralNetwork {

  private Map<Integer, Neuron> neurons;

  private List<Integer> inputs;
  private List<Integer> outputs;
  private List<Neuron> unprocessed;     // Neurons that haven't been calculated yet

  public NeuralNetwork(Genome genome) {
    inputs = new ArrayList<>();
    outputs = new ArrayList<>();
    unprocessed = new ArrayList<>();

    neurons = new HashMap<>();

    for (Integer id : genome.getNodes().keySet()) {
      Neuron neuron = new Neuron();
      NodeGenome n = genome.getNodes().get(id);

      if (n.getType() == NodeType.INPUT) {
        neuron.addInput();
        inputs.add(id);
      } else if (n.getType() == NodeType.OUTPUT) {
        outputs.add(id);
      }
      neurons.put(id, neuron);
    }

    for (Integer id : genome.getConnections().keySet()) {
      ConnectionGenome c = genome.getConnections().get(id);
      if (c.isActive()) {
        Neuron inputNeuron = neurons.get(c.getInputNode());
        Neuron outputNeuron = neurons.get(c.getOutputNode());

        inputNeuron.addOutput(c.getOutputNode(), c.getWeight());
        outputNeuron.addInput();
      }
    }
  }

  public float[] feedForward(float[] inputValues) {
    assert inputValues.length == inputs.size();

    for (Integer id : neurons.keySet()) {
      neurons.get(id).reset();
    }

    unprocessed.clear();
    unprocessed.addAll(neurons.values());

    // Feedforward first layer of inputs
    for (int i = 0; i < inputValues.length; i++) {
      Neuron input = neurons.get(inputs.get(i));
      input.addInputValue(inputValues[i]);
      input.calculateOutput();

      for (int j = 0; j < input.getOutputIDs().length; j++) {
        Neuron outputNeuron = neurons.get(input.getOutputIDs()[i]);
        outputNeuron.addInputValue(input.getFinalOutput() * input.getOutputWeights()[i]);
      }

      unprocessed.remove(input);
    }

    // Feedforward the remaining non-uniform layers
    int attempts = 0;
    while (unprocessed.size() > 0) {
      attempts++;
      if (attempts > 1000) {
        return null;
      }

      Iterator<Neuron> iterator = unprocessed.iterator();
      while (iterator.hasNext()) {
        Neuron next = iterator.next();
        if (next.ready()) {
          next.calculateOutput();
          for (int i = 0; i < next.getOutputIDs().length; i++) {
            int outputId = next.getOutputIDs()[i];
            neurons.get(outputId).addInputValue(next.getFinalOutput() * next.getOutputWeights()[i]);
          }
          iterator.remove();
        }
      }
    }

    float[] outputValues = new float[outputs.size()];
    for (int i=0;i<outputs.size(); i++) {
      outputValues[i] = neurons.get(outputs.get(i)).getFinalOutput();
    }

    return outputValues;
  }
}
