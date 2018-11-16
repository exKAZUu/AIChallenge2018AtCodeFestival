package net.exkazuu.gameaiarena.controller;

import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public abstract class Controller<Arg, Result extends Serializable> {

  public final Result run(Arg input) {
    runPreProcessing(input);
    sendDataToAI(input);
    receiveDataFromAI(input);
    return runPostProcessing(input);
  }

  protected abstract void runPreProcessing(Arg input);

  protected abstract void sendDataToAI(Arg input);

  protected abstract void receiveDataFromAI(Arg input);

  protected abstract Result runPostProcessing(Arg input);

  public abstract boolean released();

  protected abstract void beforeRelease();

  protected abstract void afterRelease();

  public abstract void release();

  public abstract ExternalComputerPlayer getExternalComputerPlayer();

  public IgnoringExceptionController<Arg, Result> ignoringException() {
    return new IgnoringExceptionController<>(this);
  }

  public LimitingTimeController<Arg, Result> limitingTime(int maxMillisecond) {
    return new LimitingTimeController<>(this, maxMillisecond);
  }

  public LimitingTimeController<Arg, Result> limitingTime(int maxMillisecond, int maxTotalMillisecond) {
    return new LimitingTimeController<>(this, maxMillisecond, maxTotalMillisecond);
  }

  public LimitingSumTimeController<Arg, Result> limitingSumTime(int availableMillisecond,
                                                                int maxSumExeededMillisecond) {
    return new LimitingSumTimeController<>(this, maxSumExeededMillisecond,
      availableMillisecond);
  }

  public PauseUnpauseController<Arg, Result> pauseUnpause(String[] pauseCommand,
                                                          String[] unpauseCommand) {
    return new PauseUnpauseController<>(this, pauseCommand, unpauseCommand);
  }

  public RecordingMemoryController<Arg, Result> recordingMemory(List<Result> oos) {
    return new RecordingMemoryController<>(this, oos);
  }

  public RecordingStreamController<Arg, Result> recordingStream(ObjectOutputStream oos) {
    return new RecordingStreamController<>(this, oos);
  }

  public ReplayingMemoryController<Arg, Result> replayingMemory(Iterable<Result> results) {
    return new ReplayingMemoryController<>(this, results);
  }

  public ReplayingStreamController<Arg, Result> replayingStream(ObjectInputStream ois) {
    return new ReplayingStreamController<>(this, ois);
  }
}
