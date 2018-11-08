package net.exkazuu.battle2048.game;

import net.exkazuu.gameaiarena.api.Direction4;
import net.exkazuu.gameaiarena.api.Point2;

import java.util.HashSet;

public class Board {
  public final int width;
  public final int height;
  private int _score;

  /**
   * 2-dimension array which represents the numbers on this board.
   * It contains 0 (meaning an empty) or an exponent (n in 2^n).
   */
  private int[][] _exponents;

  public Board(int width, int height) {
    this.width = width;
    this.height = height;
    _score = 0;

    _exponents = new int[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        _exponents[y][x] = 0;
      }
    }
  }

  /***
   * Returns the number of merged numbers when successful to move.
   * Returns -1 when failed to move.
   *
   * @param direction the given direction to move
   * @return the number of merged numbers or -1
   */
  public int move(Direction4 direction) {
    boolean moved = false;
    int countMerged = 0;

    int dx = direction.dx == 1 ? -1 : 1;
    int dy = direction.dy == 1 ? -1 : 1;
    int startX = direction.dx == 1 ? width - 1 : 0;
    int startY = direction.dy == 1 ? height - 1 : 0;

    HashSet<Point2> mergedPositions = new HashSet<>();

    for (int y = startY; 0 <= y && y < height; y += dy) {
      for (int x = startX; 0 <= x && x < width; x += dx) {
        Point2 p = new Point2(x, y);
        int exponent = this.exponentAt(p);
        if (exponent == 0) continue;

        Point2 farthest = this.findFarthestPosition(p, direction);
        Point2 next = direction.move(farthest);
        if (!mergedPositions.contains(next) && this.exponentAtOrNegativeInteger(next) == exponent) {
          mergedPositions.add(next);
          this.setExponent(0, p);
          this.setExponent(exponent + 1, next);
          countMerged++;
          _score += 1L << (exponent + 1);
        } else if (!p.equals(farthest)) {
          this.setExponent(0, p);
          this.setExponent(exponent, farthest);
          moved = true;
        }
      }
    }

    return moved || countMerged != 0 ? countMerged : -1;
  }

  public boolean canMove(Direction4 direction) {
    return simulateMove(direction) != -1;
  }

  public int simulateMove(Direction4 direction) {
    int[][] exponentCopy = _exponents.clone();
    int scoreCopy = _score;
    for (int i = 0; i < exponentCopy.length; i++) {
      exponentCopy[i] = exponentCopy[i].clone();
    }
    int result = move(direction);
    _exponents = exponentCopy;
    _score = scoreCopy;
    return result;
  }

  public boolean withinBounds(Point2 p) {
    return 0 <= p.y && p.y < height && 0 <= p.x && p.x < width;
  }

  public boolean tileAvailable(Point2 p) {
    return this.exponentAt(p) == 0;
  }

  public int exponentAt(Point2 p) {
    return _exponents[p.y][p.x];
  }

  public int exponentAtOrNegativeInteger(Point2 p) {
    return withinBounds(p) ? this.exponentAt(p) : -1;
  }

  public int getScore() {
    return _score;
  }

  public void setExponent(int exponent, Point2 p) {
    _exponents[p.y][p.x] = exponent;
  }

  private Point2 findFarthestPosition(Point2 start, Direction4 direction) {
    Point2 next = start, current;
    do {
      current = next;
      next = direction.move(next);
    } while (this.withinBounds(next) && this.tileAvailable(next));
    return current;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    String newLine = "";
    for (int y = 0; y < height; y++) {
      String delimiter = "";
      sb.append(newLine);
      for (int x = 0; x < width; x++) {
        sb.append(delimiter);
        sb.append(_exponents[y][x]);
        delimiter = " ";
      }
      newLine = "\n";
    }
    return sb.toString();
  }
}
