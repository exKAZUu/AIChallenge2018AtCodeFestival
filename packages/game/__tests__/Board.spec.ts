import { Board } from '../src/Core/Board';
import { BoardManager } from '../src/Core/BoardManager';
import { Tile } from '../src/Core/Tile';
import { Direction } from '../src/Core/types';

test('BoardManager construction', () => {
  expect(new BoardManager(5, 5)).not.toBeNull();
});

test('Basic movement of isolated tiles on the board', () => {
  const testValue = 128;

  for (const direction of Object.values(Direction)) {
    const board = new Board(5, 5);
    // middle of the board
    board.insertTile(new Tile({ row: 2, col: 2 }, testValue));
    const bm = new BoardManager(5, 5, board);

    bm.move(direction);
    let nextTile;
    switch (direction) {
      case Direction.Down: {
        nextTile = board.tileAt({ row: 4, col: 2 });
        break;
      }
      case Direction.Right: {
        nextTile = board.tileAt({ row: 2, col: 4 });
        break;
      }
      case Direction.Up: {
        nextTile = board.tileAt({ row: 0, col: 2 });
        break;
      }
      case Direction.Left: {
        nextTile = board.tileAt({ row: 2, col: 0 });
        break;
      }
      default:
        fail('Undefined direction');
    }
    expect(nextTile && nextTile.value).toStrictEqual(testValue);
  }
});

test('Random tile movement', () => {
  const size = 5;
  const board = new Board(size, size);
  board.insertTile(new Tile({ row: 0, col: 0 }, 1));
  const bm = new BoardManager(size, size, board);

  bm.move(Direction.Left);
  bm.move(Direction.Left);
  bm.move(Direction.Right);
  bm.move(Direction.Left);
  expect(board.getAvailableTiles().length).toStrictEqual(size * size - 1);
  expect(board.tileOccupied({ row: 0, col: 0 })).toStrictEqual(true);
});

test('Tile merging behavior', () => {
  const size = 5;
  const board = new Board(size, size);
  board.insertTile(new Tile({ row: 0, col: 0 }, 2));
  board.insertTile(new Tile({ row: 0, col: 1 }, 2));
  board.insertTile(new Tile({ row: 0, col: 2 }, 4));
  board.insertTile(new Tile({ row: 0, col: 3 }, 8));
  board.insertTile(new Tile({ row: 0, col: 4 }, 8));
  const bm = new BoardManager(size, size, board);

  bm.move(Direction.Right);
  const testTile0 = board.tileAt({ row: 0, col: 4 });
  const testTile1 = board.tileAt({ row: 0, col: 3 });
  const testTile2 = board.tileAt({ row: 0, col: 2 });
  expect(testTile0 && testTile0.value).toStrictEqual(16);
  expect(testTile1 && testTile1.value).toStrictEqual(4);
  expect(testTile2 && testTile2.value).toStrictEqual(4);
});

test('Tile output behavior', () => {
  const nRows = 5;
  const nCols = 4;
  const board = new Board(nRows, nCols);
  for (let row = 0; row < nRows; row++) {
    for (let col = 0; col < nCols; col++) {
      board.insertTile(new Tile({ row, col }, Math.pow(2, row * nCols + col + 1)));
    }
  }

  const expectedResult = '1 2 3 4\n5 6 7 8\n9 10 11 12\n13 14 15 16\n17 18 19 20';

  expect(board.toString()).toStrictEqual(expectedResult);
});

test('Tile output behavior', () => {
  const nRows = 3;
  const nCols = 3;
  const board = new Board(nRows, nCols);
  board.insertTile(new Tile({ row: 0, col: 0 }, 1073741824));

  const expectedResult = '30 0 0\n0 0 0\n0 0 0';

  expect(board.toString()).toStrictEqual(expectedResult);
});

test('Validation of move availability', () => {
  const size = 3;
  expect(new BoardManager(size, size).movesAvailable()).toStrictEqual(true);

  const board = new Board(size, size);
  for (let row = 0; row < size; row++) {
    for (let col = 0; col < size; col++) {
      board.insertTile(new Tile({ row, col }, 2));
    }
  }

  expect(new BoardManager(size, size, board).movesAvailable()).toStrictEqual(true);

  const board2 = new Board(size, size);
  for (let row = 0; row < size; row++) {
    for (let col = 0; col < size; col++) {
      board2.insertTile(new Tile({ row, col }, Math.pow(2, row * size + col + 1)));
    }
  }

  expect(new BoardManager(size, size, board2).movesAvailable()).toStrictEqual(false);
});
