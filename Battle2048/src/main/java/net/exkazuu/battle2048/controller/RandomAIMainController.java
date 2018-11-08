package net.exkazuu.battle2048.controller;

import net.exkazuu.battle2048.game.Board;
import net.exkazuu.battle2048.game.Game;
import net.exkazuu.battle2048.util.Logger;
import net.exkazuu.gameaiarena.api.Direction4;
import net.exkazuu.gameaiarena.api.Point2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomAIMainController extends AIController {
  private Random _random = new Random(0);
  private String _line;

  public RandomAIMainController(int index) {
    super(null, index);
  }

  @Override
  protected void runPreProcessing(Game game) {
    String input = game.getMyBoard(_index) + "\n" + game.getOpponentBoard(_index);
    Logger.getInstance().outputLog("AI" + _index + ">>Writing to stdin, waiting for stdout", Logger.LOG_LEVEL_DETAILS);
    Logger.getInstance().outputLog(input, Logger.LOG_LEVEL_DETAILS);

    _line = "";
  }

  @Override
  protected void sendDataToAI(Game game) {
  }

  @Override
  protected void receiveDataFromAI(Game game) {
    Board myBoard = game.getMyBoard(_index);
    Board opponentBoard = game.getOpponentBoard(_index);
    List<Direction4> dirs = new ArrayList<>();
    for (Direction4 d : Direction4.values()) {
      if (myBoard.canMove(d)) {
        dirs.add(d);
      }
    }
    Collections.shuffle(dirs, _random);
    if (dirs.isEmpty()) {
      return;
    }
    Direction4 d = dirs.get(0);

    int count = myBoard.simulateMove(d);
    List<Point2> ps = new ArrayList<>();
    for (int y = 0; y < opponentBoard.height; y++) {
      for (int x = 0; x < opponentBoard.width; x++) {
        Point2 p = new Point2(x, y);
        if (opponentBoard.tileAvailable(p)) {
          ps.add(p);
        }
      }
    }
    Collections.shuffle(ps, _random);

    _line = d.name().charAt(0) + " " + 1 + " " + (count + 1) + " " + (ps.get(0).y + 1) + " " + (ps.get(0).x + 1);
  }

  @Override
  protected String[] runPostProcessing(Game game) {
    Logger.getInstance().outputLog("AI" + _index + ">>STDOUT: " + _line, Logger.LOG_LEVEL_DETAILS);
    return _line.trim().split(" ");
  }
}