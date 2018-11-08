package net.exkazuu.battle2048.controller;

import com.google.common.base.Strings;
import net.exkazuu.battle2048.game.Game;
import net.exkazuu.battle2048.util.Logger;
import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;

public class AIInitialController extends AIController {
  private String _line;

  public AIInitialController(ExternalComputerPlayer com, int index) {
    super(com, index);
  }

  @Override
  protected void runPreProcessing(Game game) {
    _line = "";
  }

  @Override
  protected void sendDataToAI(Game game) {
    _com.writeLine(String.valueOf(_index));
  }

  @Override
  protected void receiveDataFromAI(Game game) {
    _line = _com.readLine();
  }

  @Override
  protected String[] runPostProcessing(Game game) {
    if (!_com.getErrorLog().isEmpty()) {
      Logger.getInstance().outputLog("AI" + _index + ">>STDERR: " + _com.getErrorLog(), Logger.LOG_LEVEL_DETAILS);
    }
    Logger.getInstance().outputLog("AI" + _index + ">>STDOUT: " + _line, Logger.LOG_LEVEL_DETAILS);
    return !Strings.isNullOrEmpty(_line) ? _line.trim().split(" ") : new String[0];
  }
}