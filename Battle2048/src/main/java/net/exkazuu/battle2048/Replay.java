package net.exkazuu.battle2048;

import net.exkazuu.battle2048.game.GameResult;
import net.exkazuu.battle2048.game.TurnRecord;

import java.util.List;

public class Replay {
  final public List<TurnRecord> commands;
  final public GameResult gameResult;

  public Replay(final List<TurnRecord> commands, GameResult result) {
    this.commands = commands;
    this.gameResult = result;
  }
}
