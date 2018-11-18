import Combinatorics from 'js-combinatorics'; // tslint:disable-line match-default-export-name
import seedrandom from 'seedrandom';
import { Board } from '../Core/Board';
import { BoardManager } from '../Core/BoardManager';
import { Tile } from '../Core/Tile';
import { Direction, DIRECTION_TO_CHAR, DIRECTIONS, Position } from '../Core/types';
import { GameManager } from '../GameManager';
import { RunnerBase } from './RunnerBase';
import { Input, Output } from './types';

type PlanResult = {
  quality: number; // Quality of the grid
  probability: number; // Probability that the above quality will happen
  qualityLoss: number; // Sum of the amount that the quality will have decreased multiplied by the probability of the decrease
  direction: Direction;
};

// AI algorithm from http://aj-r.github.io/2048-AI/
export class SmartRunner extends RunnerBase {
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

    const direction = this.nextMove(myBoardManager);
    const moved = myBoardManager.move(direction);
    const addTiles = this.addTile(opBoardManager.board, Math.pow(2, moved.countMerged + 1));

    const output = [
      `${DIRECTION_TO_CHAR.get(direction)}`,
      addTiles.length,
      Math.log2(addTiles[0].value || 1),
      ...addTiles.map(tile => `${tile.row + 1} ${tile.col + 1}`),
    ].join(' ');

    return { verdict: 'OK', runtimeNs: process.hrtime(startTime)[1], output };
  }

  private randomRange(lb: number, ub: number) {
    return lb + (Math.abs(this.rng.int32()) % (ub - lb));
  }

  private nextMove(game: BoardManager) {
    const originalQuality = this.gridQuality(game);
    const results = this.planAhead(game.board, 3, originalQuality);
    const bestResult = this.chooseBestMove(results, originalQuality);

    return bestResult.direction;
  }

  private planAhead(grid: Board, numMoves: number, originalQuality: number): PlanResult[] {
    const results: PlanResult[] = [];

    for (const direction of DIRECTIONS) {
      const testGrid = grid.clone();
      const testGame = new BoardManager(this.nRow, this.nCol, testGrid);
      const moved = testGame.move(direction).moved;
      if (!moved) {
        continue;
      }
      const result = {
        quality: -1,
        probability: 1,
        qualityLoss: 0,
        direction,
      };
      const availableCells = testGrid.getAvailableTiles();
      for (const cell of availableCells) {
        let hasAdjacentTile = false;
        for (const d2 of DIRECTIONS) {
          const vector = BoardManager.GET_VECTOR(d2);
          const adjCell = {
            row: cell.row + vector.x,
            col: cell.col + vector.y,
          };
          if (testGrid.tileAt(adjCell)) {
            hasAdjacentTile = true;
            break;
          }
        }
        if (!hasAdjacentTile) {
          continue;
        }

        const testGrid2 = testGrid.clone();
        const testGame2 = new BoardManager(this.nRow, this.nCol, testGrid2);
        testGrid2.insertTile(new Tile(cell, 2));
        let tileResult;
        if (numMoves > 1) {
          const subResults = this.planAhead(testGrid2, numMoves - 1, originalQuality);
          tileResult = this.chooseBestMove(subResults, originalQuality);
        } else {
          const tileQuality = this.gridQuality(testGame2);
          tileResult = {
            quality: tileQuality,
            probability: 1,
            qualityLoss: Math.max(originalQuality - tileQuality, 0),
          };
        }
        if (result.quality === -1 || tileResult.quality < result.quality) {
          result.quality = tileResult.quality;
          result.probability = tileResult.probability / availableCells.length;
        } else if (tileResult.quality === result.quality) {
          result.probability += tileResult.probability / availableCells.length;
        }
        result.qualityLoss += tileResult.qualityLoss / availableCells.length;
      }
      results.push(result);
    }
    return results;
  }

  private chooseBestMove(results: PlanResult[], originalQuality: number): PlanResult {
    let bestResult;
    for (const result of results) {
      if (!result) {
        continue;
      }
      if (
        !bestResult ||
        result.qualityLoss < bestResult.qualityLoss ||
        (result.qualityLoss === bestResult.qualityLoss && result.quality > bestResult.quality) ||
        (result.qualityLoss === bestResult.qualityLoss &&
          result.quality === bestResult.quality &&
          result.probability < bestResult.probability)
      ) {
        bestResult = result;
      }
    }
    if (!bestResult) {
      bestResult = {
        quality: -1,
        probability: 1,
        qualityLoss: originalQuality,
        direction: DIRECTIONS[this.randomRange(0, DIRECTIONS.length)],
      };
    }
    return bestResult;
  }

  private gridQuality(game: BoardManager): number {
    let monoScore = 0; // monoticity score
    const traversals = game.buildTraversals({ x: -1, y: 0 });
    let prevValue = -1;
    let incScore = 0;
    let decScore = 0;

    const scoreCell = (cell: Position) => {
      const tile = game.board.tileAt(cell);
      const tileValue = tile && tile.value ? tile.value : 0;
      incScore += tileValue;
      if (tileValue <= prevValue || prevValue === -1) {
        decScore += tileValue;
        if (tileValue < prevValue) {
          incScore -= prevValue;
        }
      }
      prevValue = tileValue;
    };

    traversals.row.forEach(x => {
      prevValue = -1;
      incScore = 0;
      decScore = 0;
      traversals.col.forEach(y => {
        scoreCell({ row: x, col: y });
      });
      monoScore += Math.max(incScore, decScore);
    });
    traversals.col.forEach(y => {
      prevValue = -1;
      incScore = 0;
      decScore = 0;
      traversals.row.forEach(x => {
        scoreCell({ row: x, col: y });
      });
      monoScore += Math.max(incScore, decScore);
    });

    const availableCells = game.board.getAvailableTiles();
    const emptyCellWeight = 8;
    const emptyScore = availableCells.length * emptyCellWeight;

    return monoScore + emptyScore;
  }

  private addTile(board: Board, totalValue: number) {
    const availTiles = board.getAvailableTiles();

    let candidate: Tile[] = [];
    let worstQuality = Infinity;

    let searchedPuts = 0;
    for (let putValue = 2; putValue <= totalValue; putValue *= 2) {
      const numTile = totalValue / putValue;
      if (numTile > availTiles.length) {
        continue;
      }
      const combinations = Combinatorics.combination(availTiles, numTile);
      combinations.forEach(tiles => {
        const newBoard = board.clone();
        const game = new BoardManager(this.nRow, this.nCol, newBoard);
        for (const tile of tiles) {
          game.board.insertTile(new Tile({ row: tile.row, col: tile.col }, putValue));
        }
        if (!game.movesAvailable()) {
          candidate = tiles.map(tile => new Tile({ row: tile.row, col: tile.col }, putValue));
          worstQuality = -Infinity;
        } else {
          let currentBest = -Infinity;
          let currentCandidate: Tile[] = [];
          for (const direction of DIRECTIONS) {
            const testBoard = newBoard.clone();
            const testGame = new BoardManager(this.nRow, this.nCol, testBoard);
            const moved = testGame.move(direction).moved;
            if (!moved) {
              continue;
            }
            const currentQuality = this.gridQuality(testGame);
            if (currentQuality > currentBest) {
              currentCandidate = tiles.map(tile => new Tile({ row: tile.row, col: tile.col }, putValue));
              currentBest = currentQuality;
            }
          }
          if (currentCandidate.length > 0 && currentBest < worstQuality) {
            candidate = currentCandidate;
            worstQuality = currentBest;
          }
        }
        searchedPuts++;
      });
      if (searchedPuts >= 100) {
        break;
      }
    }

    return candidate;
  }
}
