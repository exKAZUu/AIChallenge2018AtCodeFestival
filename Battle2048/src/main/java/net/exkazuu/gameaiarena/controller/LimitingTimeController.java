package net.exkazuu.gameaiarena.controller;

import java.io.Serializable;

public class LimitingTimeController<Arg, Result extends Serializable>
  extends DefaultController<Arg, Result> {

  private final int maxMillisecond;
  private final int maxTotalMillisecond;
  private int lastConsumedMillisecond;
  private int totalConsumedMillisecond;
  private boolean turnTimeExceeded;
  private boolean totalTimeExceeded;

  public LimitingTimeController(Controller<Arg, Result> controller, int maxMillisecond) {
    this(controller, maxMillisecond, 0);
  }

  public LimitingTimeController(Controller<Arg, Result> controller, int maxMillisecond, int maxTotalMillisecond) {
    super(controller);
    this.maxMillisecond = maxMillisecond;
    this.maxTotalMillisecond = maxTotalMillisecond;
  }

  @Override
  protected void sendDataToAI(Arg input) {
    if (turnTimeExceeded || totalTimeExceeded) {
      return;
    }
    controller.sendDataToAI(input);
  }

  @Override
  protected void receiveDataFromAI(Arg input) {
    if (turnTimeExceeded || totalTimeExceeded) {
      return;
    }
    final Thread thread = new Thread(() -> controller.receiveDataFromAI(input));
    long currentTimeMillis = System.currentTimeMillis();
    thread.start();
    try {
      thread.join(maxMillisecond + 100);
    } catch (final InterruptedException e) {
      // e.printStackTrace();
    }

    lastConsumedMillisecond = (int) (System.currentTimeMillis() - currentTimeMillis);
    totalConsumedMillisecond += lastConsumedMillisecond;
    turnTimeExceeded = turnTimeExceeded || (lastConsumedMillisecond > maxMillisecond) || thread.isAlive();
    totalTimeExceeded = totalTimeExceeded || (0 < maxTotalMillisecond && totalConsumedMillisecond > maxTotalMillisecond);
    if (turnTimeExceeded || totalTimeExceeded) {
      // System.err.println("Terminated the thread because time was exceeded.");
      thread.stop();
      release();
    }
  }

  public boolean timeExceeded() {
    return turnTimeExceeded || totalTimeExceeded;
  }

  public boolean turnTimeExceeded() {
    return turnTimeExceeded;
  }

  public boolean totalTimeExceeded() {
    return totalTimeExceeded;
  }

  public int getLastConsumedMillisecond() {
    return lastConsumedMillisecond;
  }

  public int getTotalConsumedMillisecond() {
    return totalConsumedMillisecond;
  }

  public int getRestTotalConsumedMillisecond() {
    return Math.max(maxTotalMillisecond - totalConsumedMillisecond, 0);
  }
}
