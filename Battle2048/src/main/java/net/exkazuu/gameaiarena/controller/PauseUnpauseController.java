package net.exkazuu.gameaiarena.controller;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.io.Serializable;

public class PauseUnpauseController<Arg, Result extends Serializable>
    extends DefaultController<Arg, Result> {

  private final String[] pauseCommand;
  private final String[] unpauseCommand;
  private boolean paused;

  public PauseUnpauseController(Controller<Arg, Result> controller, String[] pauseCommand,
                                String[] unpauseCommand) {
    super(controller);
    this.pauseCommand = pauseCommand;
    this.unpauseCommand = unpauseCommand;
  }

  @Override
  protected void runPreProcessing(Arg input) {
    // for safety, unpause before runPreProcessing
    unpause();
    controller.runPreProcessing(input);
  }

  @Override
  protected Result runPostProcessing(Arg input) {
    Result act = controller.runPostProcessing(input);
    // for safety, pause after runPreProcessing
    pause();
    return act;
  }

  public final void unpause() {
    if (!released() && paused) {
      try {
        new ProcessBuilder(unpauseCommand).start().waitFor();
      } catch (IOException e) {
        System.err.println("Fail to launch the specified command for unpausing an AI program");
        System.err.println("    Command with args: " + Joiner.on(" ").join(unpauseCommand));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public final void pause() {
    if (!released()) {
      try {
        new ProcessBuilder(pauseCommand).start().waitFor();
        paused = true;
      } catch (IOException e) {
        System.err.println("Fail to launch the specified command for pausing an AI program");
        System.err.println("    Command with args: " + Joiner.on(" ").join(pauseCommand));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public final boolean paused() {
    return paused;
  }

  public final void setPaused(boolean paused) {
    this.paused = paused;
  }
}
