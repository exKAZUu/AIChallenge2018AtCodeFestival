import { Position } from './types';

export class Tile {
  public row: number;
  public col: number;
  public readonly value?: number;
  public mergedFrom?: Tile[];
  public previousPosition?: Position;

  constructor(position: Position, initialValue?: number) {
    this.row = position.row;
    this.col = position.col;
    this.value = initialValue;
  }

  public clone() {
    return new Tile({ row: this.row, col: this.col }, this.value);
  }

  public savePosition() {
    this.previousPosition = { row: this.row, col: this.col };
  }

  public updatePosition(position: Position) {
    this.row = position.row;
    this.col = position.col;
  }

  public toString() {
    if (this.value) {
      return Math.log2(this.value).toString(10);
    } else {
      return '0';
    }
  }
}
