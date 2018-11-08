package net.exkazuu.battle2048.controller;

import net.exkazuu.battle2048.game.Game;
import net.exkazuu.battle2048.util.Logger;

import java.util.Random;

public class RandomAIInitialController extends AIController {
  private Random _random = new Random(0);
  private String _line;

  public RandomAIInitialController(int index) {
    super(null, index);
  }

  @Override
  protected void runPreProcessing(Game game) {
    Logger.getInstance().outputLog("AI" + _index + ">>Writing to stdin, waiting for stdout", Logger.LOG_LEVEL_DETAILS);
    Logger.getInstance().outputLog(String.valueOf(_index), Logger.LOG_LEVEL_DETAILS);

    _line = "";
  }

  @Override
  protected void sendDataToAI(Game game) {
  }

  @Override
  protected void receiveDataFromAI(Game game) {
    _line = (_random.nextInt(5) + 1) + " " + (_random.nextInt(5) + 1);
  }

  @Override
  protected String[] runPostProcessing(Game game) {
    Logger.getInstance().outputLog("AI" + _index + ">>STDOUT: " + _line, Logger.LOG_LEVEL_DETAILS);
    return _line.trim().split(" ");
  }
}