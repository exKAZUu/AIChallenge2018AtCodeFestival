package net.exkazuu.gameaiarena.controller;

import net.exkazuu.gameaiarena.controller.Controller;
import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;

import java.io.Serializable;

class DefaultController<Arg, Result extends Serializable> extends Controller<Arg, Result> {

  protected final Controller<Arg, Result> controller;

  public DefaultController(Controller<Arg, Result> controller) {
    this.controller = controller;
  }

  @Override
  protected void runPreProcessing(Arg input) {
    controller.runPreProcessing(input);
  }

  @Override
  protected void sendDataToAI(Arg input) {
    controller.sendDataToAI(input);
  }

  @Override
  protected void receiveDataFromAI(Arg input) {
    controller.receiveDataFromAI(input);
  }

  @Override
  protected Result runPostProcessing(Arg input) {
    return controller.runPostProcessing(input);
  }

  @Override
  public final boolean released() {
    return controller.released();
  }

  @Override
  protected void beforeRelease() {
    controller.beforeRelease();
  }

  @Override
  protected void afterRelease() {
    controller.afterRelease();
  }

  @Override
  public final void release() {
    controller.release();
  }

  @Override
  public final ExternalComputerPlayer getExternalComputerPlayer() {
    return controller.getExternalComputerPlayer();
  }

  @Override
  public final String toString() {
    return controller.toString();
  }
}
