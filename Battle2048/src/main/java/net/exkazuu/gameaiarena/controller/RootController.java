package net.exkazuu.gameaiarena.controller;

import net.exkazuu.gameaiarena.controller.Controller;
import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;

import java.io.Serializable;

public abstract class RootController<Arg, Result extends Serializable>
    extends Controller<Arg, Result> {

  private boolean released;

  @Override
  public final boolean released() {
    return released;
  }

  @Override
  protected void beforeRelease() {}

  @Override
  protected void afterRelease() {}

  @Override
  public final void release() {
    if (!released) {
      beforeRelease();
      ExternalComputerPlayer player = getExternalComputerPlayer();
      if (player != null) {
        player.release();
      }
      afterRelease();
      released = true;
    }
  }

  @Override
  public ExternalComputerPlayer getExternalComputerPlayer() {
    return null;
  }
}
