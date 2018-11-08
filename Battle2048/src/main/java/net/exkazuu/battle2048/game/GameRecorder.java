package net.exkazuu.battle2048.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

  public void writeReplayLog(String filename) {
    try {
      Files.write(Paths.get(filename), getReplayLog().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Failed to write replay log.");
    }
  }

  public String getReplayLog() throws JsonProcessingException {
    return objectMapper.writeValueAsString(replayLog);
  }
}
