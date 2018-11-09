package net.exkazuu.battle2048.game;

import net.exkazuu.gameaiarena.api.Direction4;
import net.exkazuu.gameaiarena.api.Point2;

import java.util.List;

public class Game {
  private static final int BOARD_NUM_ROWS = 5;
  private static final int BOARD_NUM_COLS = 5;
  private static final int MAX_TURNS = 1000 * 2;

  private final Board[] _boards;
  private int _turn;
  private GameResult _result;

  public Game() {
    _boards = new Board[]{
      new Board(BOARD_NUM_COLS, BOARD_NUM_ROWS),
      new Board(BOARD_NUM_COLS, BOARD_NUM_ROWS),
    };
  }

  public DefeatReason advanceInitialPhase(int playerIndex, Point2 p) {
    Board opponentBoard = getOpponentBoard(playerIndex);
    if (!opponentBoard.withinBounds(p)) {
      return DefeatReason.INVALID_POSITION_AT_INIT;
    }
    opponentBoard.setExponent(1, p);
    return null;
  }

  public DefeatReason advanceMainAndAttackPhase(int playerIndex, Direction4 direction, int count, int exponent, List<Point2> ps) {
    _turn++;

    Board myBoard = getMyBoard(playerIndex);
    int countMerged = myBoard.move(direction);
    if (countMerged == -1) {
      return DefeatReason.CANNOT_MOVE_AT_MAIN;
    }

    Board opponentBoard = getOpponentBoard(playerIndex);
    if ((1 << (countMerged + 1)) != (1 << exponent) * count) {
      return DefeatReason.INVALID_M_AND_V_AT_ATTACK;
    }
    if (count != ps.size()) {
      return DefeatReason.INVALID_NUMBER_OF_R_AND_C_AT_ATTACK;
    }
    for (Point2 p : ps) {
      if (!opponentBoard.withinBounds(p)) {
        return DefeatReason.INVALID_POSITION_AT_ATTACK;
      }
    }
    for (Point2 p : ps) {
      if (!opponentBoard.tileAvailable(p)) {
        return DefeatReason.ALREADY_OCCUPIED_AT_ATTACK;
      }
      opponentBoard.setExponent(exponent, p);
    }
    return null;
  }

  public boolean finished() {
    return _turn >= MAX_TURNS || _result != null;
  }

  public Board getMyBoard(int playerIndex) {
    return _boards[playerIndex];
  }

  public Board getOpponentBoard(int playerIndex) {
    return _boards[getOpponentIndex(playerIndex)];
  }

  public int getOpponentIndex(int playerIndex) {
    return (playerIndex + 1) & 1;
  }

  public GameResult getGameResult() {
    return _result;
  }

  public GameResult lose(int playerIndex, DefeatReason reason) {
    _result = new GameResult(getOpponentIndex(playerIndex), reason);
    return _result;
  }

  public GameResult judge() {
    int score1 = getMyBoard(0).getScore();
    int score2 = getMyBoard(1).getScore();
    if (score1 > score2) {
      _result = new GameResult(getOpponentIndex(0), DefeatReason.LESS_SCORE_AT_END);
    } else if (score1 < score2) {
      _result = new GameResult(getOpponentIndex(1), DefeatReason.LESS_SCORE_AT_END);
    } else {
      _result = new GameResult(getOpponentIndex(1), DefeatReason.FIRST_BUT_SAME_SCORE_AT_END);
    }

    return _result;
  }
}
