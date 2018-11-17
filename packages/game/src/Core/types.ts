/**
 * The direction in which to move the board
 */
export enum Direction {
  Up = 'UP',
  Down = 'DOWN',
  Left = 'LEFT',
  Right = 'RIGHT',
}

export const DIRECTIONS = [Direction.Down, Direction.Up, Direction.Left, Direction.Right];

export const DIRECTION_TO_CHAR = new Map([
  [Direction.Down, 'D'],
  [Direction.Right, 'R'],
  [Direction.Left, 'L'],
  [Direction.Up, 'U'],
]);

export const CHAR_TO_DIRECTION = new Map([
  ['U', Direction.Up],
  ['D', Direction.Down],
  ['R', Direction.Right],
  ['L', Direction.Left],
]);

/**
 * The position of a tile
 */
export interface Position {
  row: number;
  col: number;
}

/**
 * A representation of a 2D vector
 * x is the horizontal axis from left to right
 * y is the vertical axis from top to bottom
 */
export interface Vector2 {
  x: number;
  y: number;
}

export interface MovedResult {
  moved: boolean;
  countMerged: number;
  scoreObtained: number;
}
