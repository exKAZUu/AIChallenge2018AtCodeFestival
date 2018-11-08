package net.exkazuu.battle2048;

import net.exkazuu.battle2048.game.Board;
import net.exkazuu.gameaiarena.api.Direction4;
import net.exkazuu.gameaiarena.api.Point2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {
  @ParameterizedTest
  @MethodSource("argumentsForMoveOneNumber")
  void moveNumberOnce(Direction4 direction, int x, int y) {
    Board board = new Board(5, 5);
    Point2 p = new Point2(2, 2);
    board.setExponent(2, p);

    assertTrue(board.canMove(direction));
    assertEquals(0, board.move(direction));
    assertEquals(0, board.getScore());
    assertEquals(0, board.exponentAt(p));
    assertEquals(2, board.exponentAt(new Point2(x, y)));
  }

  @Test
  void canMove() {
    Board board = new Board(5, 5);
    Point2 p = new Point2(2, 2);
    board.setExponent(2, p);

    assertTrue(board.canMove(Direction4.LEFT));
    assertTrue(board.canMove(Direction4.UP));
    assertTrue(board.canMove(Direction4.RIGHT));
    assertTrue(board.canMove(Direction4.DOWN));
  }

  private static Stream<Arguments> argumentsForMoveOneNumber() {
    return Stream.of(
      Arguments.of(Direction4.LEFT, 0, 2),
      Arguments.of(Direction4.UP, 2, 0),
      Arguments.of(Direction4.RIGHT, 4, 2),
      Arguments.of(Direction4.DOWN, 2, 4)
    );
  }

  @Test
  void moveNumberMultipleTimes() {
    Board board = new Board(5, 5);
    Point2 p = new Point2(0, 0);
    board.setExponent(2, p);
    assertFalse(board.canMove(Direction4.LEFT));
    assertEquals(-1, board.move(Direction4.LEFT));
    assertTrue(board.canMove(Direction4.RIGHT));
    assertEquals(0, board.move(Direction4.RIGHT));
    assertTrue(board.canMove(Direction4.LEFT));
    assertEquals(0, board.move(Direction4.LEFT));
    assertFalse(board.canMove(Direction4.LEFT));
    assertEquals(-1, board.move(Direction4.LEFT));
    assertEquals(2, board.exponentAt(p));
  }

  @Test
  void mergeNumbers() {
    Board board = new Board(5, 5);
    board.setExponent(2, new Point2(0, 0));
    board.setExponent(2, new Point2(1, 0));
    board.setExponent(3, new Point2(2, 0));
    board.setExponent(8, new Point2(3, 0));
    board.setExponent(8, new Point2(4, 0));

    assertTrue(board.canMove(Direction4.RIGHT));
    assertEquals(2, board.move(Direction4.RIGHT));
    assertEquals(9, board.exponentAt(new Point2(4, 0)));
    assertEquals(3, board.exponentAt(new Point2(3, 0)));
    assertEquals(3, board.exponentAt(new Point2(2, 0)));
    assertEquals((1 << 9) + (1 << 3), board.getScore());
  }

  @Test
  void testToString() {
    Board board = new Board(4, 5);
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 5; y++) {
        board.setExponent(x + y * 4 + 1, new Point2(x, y));
      }
    }
    assertEquals("1 2 3 4\n5 6 7 8\n9 10 11 12\n13 14 15 16\n17 18 19 20", board.toString());
  }

  @Test
  void tryImpossibleMove() {
    Board board = new Board(3, 3);
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        board.setExponent(x + y * 3 + 1, new Point2(x, y));
      }
    }
    for (Direction4 direction : Direction4.values()) {
      assertFalse(board.canMove(direction));
      assertEquals(-1, board.move(direction));
    }
    assertEquals("1 2 3\n4 5 6\n7 8 9", board.toString());
  }
}