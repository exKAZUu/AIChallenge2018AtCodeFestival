package net.exkazuu.battle2048.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.exkazuu.battle2048.Replay;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class GameRecorder {
  private static ObjectMapper objectMapper = new ObjectMapper();

  private final List<TurnRecord> replayLog;

  public GameRecorder() {
    this.replayLog = new ArrayList<>();
  }

  public boolean add(TurnRecord record) {
    return replayLog.add(record);
  }

  public void writeReplayLog(final String filename, final GameResult result) {
    try {
      Files.write(Paths.get(filename), getReplayLog(result).getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Failed to write replay log.");
    }
  }

  public String getReplayLog(final GameResult result) throws JsonProcessingException {
    return objectMapper.writeValueAsString(new Replay(replayLog, result));
  }

  public List<TurnRecord> getReplayList() {
    return replayLog;
  }
}
