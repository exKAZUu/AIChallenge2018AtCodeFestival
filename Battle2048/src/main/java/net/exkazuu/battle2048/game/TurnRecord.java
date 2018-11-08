package net.exkazuu.battle2048.game;

public class TurnRecord {
  private int playerIndex;
  private String AIInput;
  private String AIOutput;
  private double runtime;

  public TurnRecord(final int playerIndex) {
    this.playerIndex = playerIndex;
  }

  public TurnRecord setAIInput(final Game game, final int playerIndex) {
    this.AIInput = game.getMyBoard(playerIndex) + "\n" + game.getOpponentBoard(playerIndex);
    return this;
  }

  public TurnRecord setAIOutput(final String AIOutputs[]) {
    this.AIOutput = String.join(" ", AIOutputs);
    return this;
  }

  public TurnRecord setRuntime(final double runtime) {
    this.runtime = runtime;
    return this;
  }

  public int getPlayerIndex() {
    return playerIndex;
  }

  public String getAIInput() {
    return AIInput;
  }

  public String getAIOutput() {
    return AIOutput;
  }

  public double getRuntime() {
    return runtime;
  }
}
