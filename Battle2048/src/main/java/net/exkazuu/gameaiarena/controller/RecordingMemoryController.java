package net.exkazuu.gameaiarena.controller;

import net.exkazuu.gameaiarena.controller.Controller;

import java.io.Serializable;
import java.util.List;

public class RecordingMemoryController<Arg, Result extends Serializable>
    extends net.exkazuu.gameaiarena.controller.DefaultController<Arg, Result> {
  private final List<Result> oos;

  public RecordingMemoryController(Controller<Arg, Result> controller, List<Result> oos) {
    super(controller);
    this.oos = oos;
  }

  @Override
  protected Result runPostProcessing(Arg input) {
    Result act = controller.runPostProcessing(input);
    oos.add(act);
    return act;
  }
}
