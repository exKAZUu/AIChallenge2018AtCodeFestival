package net.exkazuu.battle2048;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.exkazuu.battle2048.game.GameResult;
import net.exkazuu.battle2048.game.TurnRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JudgeOutput {
  public static final ObjectMapper objectMapper = new ObjectMapper();

  private final List<LogRecord> log = new ArrayList<>();
  private int winner;
  public final Replay replay;

  public JudgeOutput(final int winner, final List<TurnRecord> replay, final GameResult result) throws JsonProcessingException {
    this.winner = winner;
    this.replay = new Replay(replay, result);
    addLog(-1, objectMapper.writeValueAsString(result));
  }

  public List<LogRecord> getLog() {
    return log;
  }

  public int getWinner() {
    return winner;
  }

  public void addLog(final int target, final String message) {
    log.add(new LogRecord(target, message));
  }

  class LogRecord {
    public int target;
    public String message;

    LogRecord(final int target, final String message) {
      this.target = target;
      this.message = message;
    }
  }
}
