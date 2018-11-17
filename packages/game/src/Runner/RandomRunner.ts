import seedrandom from 'seedrandom';
import { BoardManager } from '../Core/BoardManager';
import { DIRECTION_TO_CHAR, DIRECTIONS } from '../Core/types';
import { GameManager } from '../GameManager';
import { RunnerBase } from './RunnerBase';
import { Input, Output } from './types';

export class RandomRunner extends RunnerBase {
  private rng: seedrandom.prng;
  private initialized = false;

  private readonly nRow = GameManager.BOARD_NUM_ROWS;
  private readonly nCol = GameManager.BOARD_NUM_COLS;

  constructor(seed?: number) {
    super();

    this.rng = seedrandom(seed ? seed.toString(10) : new Date().toString());
  }

  public async sendAndReceiveData(data: Input, timeLimtMs: number): Promise<Output> {
    const startTime = process.hrtime();

    if (!this.initialized) {
      this.initialized = true;
      return {
        verdict: 'OK',
        output: `${this.randomRange(1, this.nRow + 1)} ${this.randomRange(1, this.nCol + 1)}`,
        runtimeNs: process.hrtime(startTime)[1],
      };
    }

    const boardsStrings = data.commands.join('\n').split('\n');

    const myBoardManager = BoardManager.FROM_STRINGS(boardsStrings.slice(1, this.nRow + 1));
    const opBoardManager = BoardManager.FROM_STRINGS(boardsStrings.slice(this.nRow + 1, this.nRow * 2 + 1));

    const movable = myBoardManager.movesAvailable();
    if (!movable) {
      return { verdict: 'TLE', runtimeNs: timeLimtMs * 2 * 1000 * 1000, output: '' };
    }

    const availableDirections = DIRECTIONS.filter(d => myBoardManager.clone().move(d).moved);
    const dir = availableDirections[this.randomRange(0, availableDirections.length)];

    const availableTiles = opBoardManager.board.getAvailableTiles();
    const addTile = availableTiles[this.randomRange(0, availableTiles.length)];
    const addTileValLog = myBoardManager.move(dir).countMerged + 1;

    const output = `${DIRECTION_TO_CHAR.get(dir)} 1 ${addTileValLog} ${addTile.row + 1} ${addTile.col + 1}`;

    return { verdict: 'OK', runtimeNs: process.hrtime(startTime)[1], output };
  }

  private randomRange(lb: number, ub: number) {
    return lb + (Math.abs(this.rng.int32()) % (ub - lb));
  }
}
