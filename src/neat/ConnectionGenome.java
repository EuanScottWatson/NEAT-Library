package neat;

public class ConnectionGenome {

  private int inputNode;
  private int outputNode;
  private float weight;
  private boolean active;
  private int innovationNo;

  public ConnectionGenome(int inputNode, int outputNode, float weight, boolean active,
      int innovationNo) {
    this.inputNode = inputNode;
    this.outputNode = outputNode;
    this.weight = weight;
    this.active = active;
    this.innovationNo = innovationNo;
  }

  public void disable() {
    active = false;
  }

  public int getInputNode() {
    return inputNode;
  }

  public int getOutputNode() {
    return outputNode;
  }

  public float getWeight() {
    return weight;
  }

  public boolean isActive() {
    return active;
  }

  public int getInnovationNo() {
    return innovationNo;
  }
}
