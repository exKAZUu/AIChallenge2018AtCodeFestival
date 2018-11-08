package net.exkazuu.gameaiarena.controller;

import net.exkazuu.gameaiarena.controller.Controller;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RecordingStreamController<Arg, Result extends Serializable>
    extends net.exkazuu.gameaiarena.controller.DefaultController<Arg, Result> {

  private final ObjectOutputStream oos;

  public RecordingStreamController(Controller<Arg, Result> controller, ObjectOutputStream oos) {
    super(controller);
    this.oos = oos;
  }

  @Override
  protected Result runPostProcessing(Arg input) {
    Result act = controller.runPostProcessing(input);
    try {
      oos.writeObject(act);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return act;
  }
}
