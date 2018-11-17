import { HTMLActuator } from '../../../viewer/src/HTMLActuator';
import { Board } from './Board';
import { Tile } from './Tile';
import { Direction, DIRECTIONS, MovedResult, Position, Vector2 } from './types';

export class BoardManager {
  public board: Board;
  private readonly nbRows: number;
  private readonly nbCols: number;
  private readonly actuator?: HTMLActuator;

  constructor(rows: number, cols: number, board?: Board, actuator?: HTMLActuator) {
    this.nbRows = rows;
    this.nbCols = cols;

    if (!board) {
      this.board = new Board(this.nbRows, this.nbCols);
    } else {
      this.board = board;
    }

    this.actuator = actuator;
    this.actuate();
  }

  public static FROM_STRINGS(strings: string[]) {
    const board = Board.FROM_STRINGS(strings);
    return new BoardManager(board.getSize().nbRows, board.getSize().nbCols, board);
  }

  public static GET_VECTOR(direction: Direction): Vector2 {
    switch (direction) {
      case Direction.Up: {
        return { x: 0, y: -1 };
      }
      case Direction.Down: {
        return { x: 0, y: 1 };
      }
      case Direction.Left: {
        return { x: -1, y: 0 };
      }
      case Direction.Right: {
        return { x: 1, y: 0 };
      }
      default: {
        return { x: 0, y: 0 };
      }
    }
  }

  private static positionsEqual(first: Position, second: Position) {
    return first.row === second.row && first.col === second.col;
  }

  public clone() {
    return new BoardManager(this.nbRows, this.nbCols, this.board.clone());
  }

  public move(direction: Direction): MovedResult {
    const vector = BoardManager.GET_VECTOR(direction);
    const traversals = this.buildTraversals(vector);
    let moved = false;

    this.prepareTiles();

    let countMerged = 0;
    let score = 0;
    traversals.row.forEach(row => {
      traversals.col.forEach(col => {
        const tilePosition = { row: row, col: col };
        const tile = this.board.tileAt(tilePosition);

        if (tile && tile.value) {
          const positions = this.findFarthestPosition(tilePosition, vector);
          const next = this.board.tileAt(positions.next);

          if (next && next.value === tile.value && !next.mergedFrom) {
            const merged = new Tile(positions.next, tile.value * 2);
            merged.mergedFrom = [tile, next];

            this.board.insertTile(merged);
            this.board.removeTile(tile);

            tile.updatePosition(positions.next);

            countMerged++;
            score += tile.value * 2;
          } else {
            this.moveTile(tile, positions.farthest);
          }

          if (!BoardManager.positionsEqual(tilePosition, tile)) {
            moved = true;
          }
        }
      });
    });

    if (moved) {
      this.actuate();
      return {
        moved: true,
        countMerged,
        scoreObtained: score,
      };
    }

    return {
      moved: false,
      countMerged: 0,
      scoreObtained: 0,
    };
  }

  public actuate() {
    if (this.actuator) {
      this.actuator.actuate(this.board.tiles);
    }
  }

  public movesAvailable() {
    return this.board.getAvailableTiles().length > 0 || this.tileMatchesAvailable();
  }

  public toString() {
    return this.board.toString();
  }

  public prepareTiles() {
    this.board.eachTile((x: number, y: number, tile: Tile) => {
      if (tile.value) {
        tile.mergedFrom = undefined;
        tile.savePosition();
      }
    });
  }

  public setup() {
    this.board = new Board(this.nbRows, this.nbCols);
    this.actuate();
  }

  public buildTraversals(vector: Vector2) {
    const traversals: { row: number[]; col: number[] } = { row: [], col: [] };

    for (let i = 0; i < this.nbRows; i++) {
      traversals.row.push(i);
    }
    for (let j = 0; j < this.nbCols; j++) {
      traversals.col.push(j);
    }
    if (vector.x === 1) {
      traversals.col = traversals.col.reverse();
    }
    if (vector.y === 1) {
      traversals.row = traversals.row.reverse();
    }

    return traversals;
  }

  private tileMatchesAvailable() {
    for (let i = 0; i < this.nbRows; i++) {
      for (let j = 0; j < this.nbCols; j++) {
        const tile = this.board.tileAt({ row: i, col: j });

        if (tile && tile.value) {
          const movable = DIRECTIONS.some(
            (direction): boolean => {
              const vector = BoardManager.GET_VECTOR(direction);
              const tilePosition = { row: i + vector.y, col: j + vector.x };
              const otherTile = this.board.tileAt(tilePosition);
              return !!otherTile && otherTile.value === tile.value;
            }
          );
          if (movable) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private findFarthestPosition(tilePosition: Position, vector: Vector2) {
    let previous;
    let current = tilePosition;
    do {
      previous = current;
      current = { row: previous.row + vector.y, col: previous.col + vector.x };
    } while (this.board.withinBounds(current) && this.board.tileAvailable(current));

    return {
      farthest: previous,
      next: current,
    };
  }

  private moveTile(tile: Tile, position: Position) {
    this.board.removeTile(tile);
    tile.updatePosition(position);
    this.board.insertTile(tile);
  }
}
