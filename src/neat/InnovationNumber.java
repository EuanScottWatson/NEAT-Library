package neat;

public class InnovationNumber {

  private int innovationNo;

  public InnovationNumber(int innovationNo) {
    this.innovationNo = innovationNo;
  }

  public int getInnovationNo() {
    return innovationNo++;
  }

}
