package net.exkazuu.gameaiarena.controller;

import java.io.Serializable;

public class LimitingTimeController<Arg, Result extends Serializable>
    extends DefaultController<Arg, Result> {

  private final int maxMillisecond;
  private int lastConsumedMillisecond;
  private boolean timeExceeded;

  public LimitingTimeController(Controller<Arg, Result> controller, int maxMillisecond) {
    super(controller);
    this.maxMillisecond = maxMillisecond;
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
      e.printStackTrace();
    }

    lastConsumedMillisecond = (int)(System.currentTimeMillis() - currentTimeMillis);
    if (lastConsumedMillisecond > maxMillisecond || thread.isAlive()) {
      System.err.println("Terminated the thread because time was exceeded.");
      thread.stop();
      release();
      timeExceeded = true;
    }
  }

  public boolean timeExceeded() {
    return timeExceeded;
  }

  public int getLastConsumedMillisecond() {
    return lastConsumedMillisecond;
  }
}
