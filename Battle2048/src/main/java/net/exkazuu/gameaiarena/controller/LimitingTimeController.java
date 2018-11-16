package net.exkazuu.gameaiarena.controller;

import java.io.Serializable;

public class LimitingTimeController<Arg, Result extends Serializable>
  extends DefaultController<Arg, Result> {

  private final int maxMillisecond;
  private final int maxTotalMillisecond;
  private int lastConsumedMillisecond;
  private int totalConsumedMillisecond;
  private boolean timeExceeded;

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
    if (timeExceeded) {
      return;
    }
    controller.sendDataToAI(input);
  }

  @Override
  protected void receiveDataFromAI(Arg input) {
    if (timeExceeded) {
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
    timeExceeded = timeExceeded || (lastConsumedMillisecond > maxMillisecond);
    timeExceeded = timeExceeded || (0 < maxTotalMillisecond && totalConsumedMillisecond > maxTotalMillisecond);
    timeExceeded = timeExceeded || thread.isAlive();
    if (timeExceeded) {
      // System.err.println("Terminated the thread because time was exceeded.");
      thread.stop();
      release();
    }
  }

  public boolean timeExceeded() {
    return timeExceeded;
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
