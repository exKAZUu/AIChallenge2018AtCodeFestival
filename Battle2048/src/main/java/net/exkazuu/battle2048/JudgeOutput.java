package net.exkazuu.battle2048;

import net.exkazuu.battle2048.game.TurnRecord;

import java.util.Collections;
import java.util.List;

public class JudgeOutput {
  private final List<String> log = Collections.emptyList();
  private int winner;
  private List<TurnRecord> replay;

  public JudgeOutput setWinner(final int winner) {
    this.winner = winner;
    return this;
  }

  public JudgeOutput setReplay(final List<TurnRecord> replay) {
    this.replay = replay;
    return this;
  }

  public List<String> getLog() {
    return log;
  }

  public int getWinner() {
    return winner;
  }

  public List<TurnRecord> getReplay() {
    return replay;
  }
}
