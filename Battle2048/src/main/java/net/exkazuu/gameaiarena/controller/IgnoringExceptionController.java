package net.exkazuu.gameaiarena.controller;

import java.io.Serializable;

public class IgnoringExceptionController<Arg, Result extends Serializable>
    extends DefaultController<Arg, Result> {

  public IgnoringExceptionController(Controller<Arg, Result> controller) {
    super(controller);
  }

  @Override
  protected void runPreProcessing(Arg input) {
    try {
      controller.runPreProcessing(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void sendDataToAI(Arg input) {
    try {
      controller.sendDataToAI(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void receiveDataFromAI(Arg input) {
    try {
      controller.receiveDataFromAI(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected Result runPostProcessing(Arg input) {
    try {
      return controller.runPostProcessing(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
