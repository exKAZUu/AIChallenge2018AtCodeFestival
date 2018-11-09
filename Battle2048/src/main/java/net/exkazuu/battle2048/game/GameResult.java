package net.exkazuu.battle2048.game;

public class GameResult {
  public final int winner;
  public final DefeatReason defeatReason;

  public GameResult(int winner, DefeatReason defeatReason) {
    this.winner = winner;
    this.defeatReason = defeatReason;
  }
}
