public class NodeGenome {

  private NodeType type;
  private int id;

  public NodeGenome(NodeType t, int id) {
    type = t;
    this.id = id;
  }

  public NodeType getType() {
    return type;
  }

  public int getId() {
    return id;
  }

}
