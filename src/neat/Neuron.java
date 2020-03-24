package neat;

import java.util.Arrays;

public class Neuron {

  private float finalOutput;

  private Float[] inputValues;
  private Float[] outputWeights;
  private int[] outputIDs;

  public Neuron() {
    inputValues = new Float[0];
    outputWeights = new Float[0];
    outputIDs = new int[0];
  }

  public float getFinalOutput() {
    return finalOutput;
  }

  public void addOutput(int id, float weight) {
    Float[] newOutputWeights = new Float[outputWeights.length + 1];
    System.arraycopy(outputWeights, 0, newOutputWeights, 0, outputWeights.length);
    newOutputWeights[outputWeights.length] = weight;
    outputWeights = newOutputWeights;

    int[] newOutputIDs = new int[outputIDs.length + 1];
    System.arraycopy(outputIDs, 0, newOutputIDs, 0, outputIDs.length);
    newOutputIDs[outputIDs.length] = id;
    outputIDs = newOutputIDs;
  }

  public boolean ready() {
    for (Float f : inputValues) {
      if (f == null) {
        return false;
      }
    }
    return true;
  }

  public void addInput() {
    Float[] newInputs = new Float[inputValues.length + 1];
    for (int i = 0; i < inputValues.length + 1; i++) {
      newInputs[i] = null;
    }
    inputValues = newInputs;
  }

  public float calculateOutput() {
    float outputSum = 0f;
    for (Float f : inputValues) {
      outputSum += f;
    }
    return sigmoid(outputSum);
  }

  private float sigmoid(Float x) {
    return (float) (1f / (1 + Math.exp(-1 * x)));
  }

  public void addInputValue(float input) {
    for (int i = 0; i < inputValues.length; i++) {
      if (inputValues[i] == null) {
        inputValues[i] = input;
        break;
      }
    }
  }

  public void reset() {
    Arrays.fill(inputValues, null);
    finalOutput = 0f;
  }

}
