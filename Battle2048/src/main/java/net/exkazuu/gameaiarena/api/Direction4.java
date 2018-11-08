package net.exkazuu.gameaiarena.api;

import net.exkazuu.gameaiarena.api.Point2;

/**
 * ゲーム内における4つの方向を表します。
 */
public enum Direction4 {
  /**
   * 右方向
   */
  RIGHT(1, 0),
  /**
   * 上方向
   */
  UP(0, -1),
  /**
   * 左方向
   */
  LEFT(-1, 0),
  /**
   * 下方向
   */
  DOWN(0, +1), ;

  /**
   * 方向を示すX座標
   */
  public final int dx;
  /**
   * 方向を示すY座標
   */
  public final int dy;

  private Direction4(int x, int y) {
    this.dx = x;
    this.dy = y;
  }

  /**
   * Returns the moved location from the specified position with this direction.
   * 
   * @param p the start location
   * @return the moved location
   */
  public Point2 move(Point2 p) {
    return new Point2(p.x + dx, p.y + dy);
  }

  /**
   * このDirectionが示す(x, y)座標におけるPoint表現を返します。
   * 
   * @return このDirectionが示す(x, y)座標におけるPoint表現
   */
  public Point2 toPoint() {
    return new Point2(dx, dy);
  }

  /**
   * この方向とは逆の方向を取得します．
   * 
   * @return 逆の方向
   */
  public net.exkazuu.gameaiarena.api.Direction4 getOpposite() {
    switch (this) {
      case DOWN:
        return UP;
      case LEFT:
        return RIGHT;
      case RIGHT:
        return LEFT;
      case UP:
        return DOWN;
    }
    return null;
  }
}
