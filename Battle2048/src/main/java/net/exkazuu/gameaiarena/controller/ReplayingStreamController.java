package net.exkazuu.gameaiarena.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ReplayingStreamController<Arg, Result extends Serializable>
    extends DefaultController<Arg, Result> {

  private final ObjectInputStream ois;

  public ReplayingStreamController(Controller<Arg, Result> controller, ObjectInputStream ois) {
    super(controller);
    this.ois = ois;
  }

  @Override
  protected void sendDataToAI(Arg input) {}

  @Override
  protected void receiveDataFromAI(Arg input) {}

  @SuppressWarnings("unchecked")
  @Override
  public Result runPostProcessing(Arg input) {
    try {
      return (Result) ois.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
