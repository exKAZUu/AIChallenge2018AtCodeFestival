package net.exkazuu.battle2048.controller;

import net.exkazuu.battle2048.game.Game;
import net.exkazuu.gameaiarena.controller.RootController;
import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;

public abstract class AIController extends RootController<Game, String[]> {
  protected ExternalComputerPlayer _com;
  protected int _index;

  public AIController(ExternalComputerPlayer com, int index) {
    _com = com;
    _index = index;
  }

  @Override
  public ExternalComputerPlayer getExternalComputerPlayer() {
    return _com;
  }
}