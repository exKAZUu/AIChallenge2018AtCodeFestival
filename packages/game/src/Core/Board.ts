import { Tile } from './Tile';
import { Position } from './types';

export class Board {
  public readonly tiles: Tile[][];

  private readonly nbRows: number;
  private readonly nbCols: number;

  constructor(rows: number, cols: number) {
    this.nbRows = rows;
    this.nbCols = cols;
    this.tiles = Board.createEmptyTileBoard(this.nbRows, this.nbCols);
  }

  public static FROM_STRINGS(strings: string[]): Board {
    // TODO assert invalid input
    const numbers = strings.map(str =>
      str
        .trim()
        .split(' ')
        .map(v => parseInt(v, 10))
    );
    const nRows = numbers.length;
    const nCols = numbers[0].length;
    const board = new Board(nRows, nCols);

    for (let row = 0; row < nRows; row++) {
      for (let col = 0; col < nCols; col++) {
        if (numbers[row][col] > 0) {
          board.insertTile(new Tile({ row, col }, Math.pow(2, numbers[row][col])));
        }
      }
    }
    return board;
  }

  private static createEmptyTileBoard(nbRows: number, nbCols: number) {
    const tiles: Tile[][] = [];

    for (let i = 0; i < nbRows; i++) {
      tiles[i] = [];
      for (let j = 0; j < nbCols; j++) {
        tiles[i][j] = new Tile({ row: i, col: j }, undefined);
      }
    }
    return tiles;
  }

  public withinBounds(position: Position) {
    return position.row >= 0 && position.row < this.nbRows && position.col >= 0 && position.col < this.nbCols;
  }

  public insertTile(tile: Tile) {
    this.tiles[tile.row][tile.col] = tile;
  }

  public removeTile(tile: Tile) {
    this.tiles[tile.row][tile.col] = new Tile({ row: tile.row, col: tile.col }, undefined);
  }

  public tileAt(position: Position) {
    if (this.withinBounds(position) && this.tiles[position.row][position.col].value) {
      return this.tiles[position.row][position.col];
    } else {
      return undefined;
    }
  }

  public tileAvailable(position: Position) {
    return !this.tileOccupied(position);
  }

  public getRandomAvailableTile() {
    const availableTiles = this.getAvailableTiles();
    if (availableTiles.length) {
      return availableTiles[Math.floor(Math.random() * availableTiles.length)];
    }
  }

  public getAvailableTiles() {
    const availableTiles: Tile[] = [];
    this.eachTile((row: number, col: number, tile: Tile) => {
      if (!tile.value) {
        availableTiles.push(tile.clone());
      }
    });
    return availableTiles;
  }

  public eachTile(callback: (row: number, col: number, tile: Tile) => any) {
    for (let i = 0; i < this.nbRows; i++) {
      for (let j = 0; j < this.nbCols; j++) {
        callback(i, j, this.tiles[i][j]);
      }
    }
  }

  public tileOccupied(position: Position) {
    const tile = this.tileAt(position);
    if (tile) {
      return !!tile.value;
    } else {
      return undefined;
    }
  }

  public toString() {
    return this.tiles.map(row => row.map(tile => tile.toString()).join(' ')).join('\n');
  }

  public getSize() {
    return { nbRows: this.nbRows, nbCols: this.nbCols };
  }

  public clone() {
    const result = new Board(this.nbRows, this.nbCols);
    this.eachTile((row, col, tile) => {
      if (tile && tile.value && tile.value > 0) {
        result.insertTile(tile.clone());
      }
    });
    return result;
  }
}
