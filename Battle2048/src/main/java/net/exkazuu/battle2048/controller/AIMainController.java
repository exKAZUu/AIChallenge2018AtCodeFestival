package net.exkazuu.battle2048.controller;

import com.google.common.base.Strings;
import net.exkazuu.battle2048.game.Game;
import net.exkazuu.battle2048.util.Logger;
import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;

public class AIMainController extends AIController {
  private String _line;
  private String _input;

  public AIMainController(ExternalComputerPlayer com, int index) {
    super(com, index);
  }

  @Override
  protected void runPreProcessing(Game game) {
    _input = game.getMyBoard(_index) + "\n" + game.getOpponentBoard(_index);
    Logger.getInstance().outputLog("AI" + _index + ">>Writing to stdin, waiting for stdout", Logger.LOG_LEVEL_DETAILS);
    Logger.getInstance().outputLog(_input, Logger.LOG_LEVEL_DETAILS);

    _line = "";
  }

  @Override
  protected void sendDataToAI(Game game) {
    _com.writeLine(_input);
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