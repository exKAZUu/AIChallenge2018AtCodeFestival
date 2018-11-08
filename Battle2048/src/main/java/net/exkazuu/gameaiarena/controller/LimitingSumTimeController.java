package net.exkazuu.gameaiarena.controller;

import net.exkazuu.gameaiarena.controller.Controller;

import java.io.Serializable;

public class LimitingSumTimeController<Arg, Result extends Serializable>
    extends net.exkazuu.gameaiarena.controller.DefaultController<Arg, Result> {

  private final int availableMillisecond;
  private int restExceededMillisecond;
  private int lastConsumedMillisecond;
  private boolean timeExceeded;

  public LimitingSumTimeController(Controller<Arg, Result> controller, int availableMillisecond,
                                   int maxExceededMillisecond) {
    super(controller);
    this.availableMillisecond = availableMillisecond;
    this.restExceededMillisecond = maxExceededMillisecond;
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
    Thread thread = new Thread(() -> controller.receiveDataFromAI(input));
    long currentTimeMillis = System.currentTimeMillis();
    thread.start();
    try {
      thread.join(availableMillisecond + restExceededMillisecond + 100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    lastConsumedMillisecond = (int)(System.currentTimeMillis() - currentTimeMillis);
    if (lastConsumedMillisecond > availableMillisecond) {
      restExceededMillisecond -= lastConsumedMillisecond - availableMillisecond;
      System.err.println("Time was exceeded.");
      System.err.println("- Consumed milliseconds in this turn: " + lastConsumedMillisecond);
      System.err.println("- Available milliseconds in this turn: " + availableMillisecond);
      System.err.println("- All remaining available milliseconds: " + restExceededMillisecond);
    }
    if (restExceededMillisecond <= 0 || thread.isAlive()) {
      System.err.println("Terminating the thread because time was exceeded.");
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

  public int getAvailableMillisecond() {
    return availableMillisecond;
  }

  public int getRestExceededMillisecond() {
    return restExceededMillisecond;
  }
}
