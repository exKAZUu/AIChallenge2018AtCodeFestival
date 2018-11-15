package net.exkazuu.battle2048.game;

public class GameResult {
  public final int winner;
  public final int turn;
  public final DefeatReason defeatReason;

  public GameResult(final int winner, final int turn, final DefeatReason defeatReason) {
    this.winner = winner;
    this.turn = turn;
    this.defeatReason = defeatReason;
  }
}
