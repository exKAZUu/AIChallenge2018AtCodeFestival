import { Board } from './Core/Board';
import { BoardManager } from './Core/BoardManager';
import { Tile } from './Core/Tile';
import { CHAR_TO_DIRECTION } from './Core/types';
import { RunnerBase } from './Runner/RunnerBase';
import { Input, Output } from './Runner/types';
import { GameResult, GameResultReason, TurnRecord } from './types';

type AttackParseResult = {
  verdict: 'OK' | 'NG';
  reason?: GameResultReason;
  tiles?: Tile[];
};

export class GameManager {
  public static readonly BOARD_NUM_ROWS = 5;
  public static readonly BOARD_NUM_COLS = 5;

  private static readonly INTIALIZE_TIME_MS = 1000;
  private static readonly MAIN_TURN_TIME_MS = 1000;
  private static readonly TOTAL_MAIN_TIME_MS = 100 * 1000;
  private static readonly MAX_TURN = 1000;

  private static readonly POSITIVE_REGEXP = /^\+0|[1-9]\d*$/;

  private ioLog: [Input, Output][] = [];

  private readonly runners: RunnerBase[];

  constructor(firstPlayer: RunnerBase, secondPlayer: RunnerBase) {
    this.runners = [firstPlayer, secondPlayer];
  }

  public static PARSE_ATTACK_COMMAND(commands: string[], enemyBoard: Board): AttackParseResult {
    if (!commands.every(GameManager.isPositiveInteger)) {
      return { verdict: 'NG', reason: 'PRESENTATION_ERROR' };
    }

    const [M, V, ...vec] = commands.map(v => parseInt(v, 10));

    if (M * 2 !== vec.length || !V) {
      return { verdict: 'NG', reason: 'PRESENTATION_ERROR' };
    }

    const value = Math.pow(2, V);

    const tiles: Tile[] = [];
    for (let i = 0; i < M; i++) {
      const [row, col] = vec.slice(i * 2, i * 2 + 2);
      const position = { row: row - 1, col: col - 1 };
      if (enemyBoard.tileOccupied(position)) {
        return { verdict: 'NG', reason: 'INVALID_POSITION' };
      }
      tiles.push(new Tile(position, value));
    }

    return { verdict: 'OK', tiles };
  }

  private static isPositiveInteger(str: string) {
    return GameManager.POSITIVE_REGEXP.test(str);
  }

  // tslint:disable max-func-body-length TODO fixme
  public async gameMain() {
    // Prepare internal state
    const consumedTimesNs = [0, 0];
    let turn = 0;
    const scores = [0, 0];
    const timeLefts = [GameManager.TOTAL_MAIN_TIME_MS, GameManager.TOTAL_MAIN_TIME_MS];
    const boards = [
      new BoardManager(GameManager.BOARD_NUM_ROWS, GameManager.BOARD_NUM_COLS),
      new BoardManager(GameManager.BOARD_NUM_ROWS, GameManager.BOARD_NUM_COLS),
    ];

    const buildResult = (winner: number, reason: GameResultReason): GameResult => {
      return {
        winner: winner === 0 ? 'FIRST' : 'SECOND',
        defeatReason: reason,
        turn,
        stats: {
          first: { time: consumedTimesNs[0], score: scores[0] },
          second: { time: consumedTimesNs[1], score: scores[1] },
        },
      };
    };

    // Initialize
    for (let player = 0; player < 2; player++) {
      const runner = this.runners[player];
      const input = { commands: [player.toString(10)] };
      const ret = await runner.sendAndReceiveData(input, GameManager.INTIALIZE_TIME_MS);

      console.log(ret);
      this.ioLog.push([input, ret]);

      consumedTimesNs[player] += ret.runtimeNs;

      if (ret.verdict !== 'OK') {
        if (ret.verdict === 'TLE') {
          return buildResult((player + 1) % 2, 'TIME_LIMIT_EXCEEDED');
        }
        // TODO return more detailed result.
        return buildResult((player + 1) % 2, 'RUNTIME_ERROR');
      }

      const reason = (() => {
        if (!ret.output) {
          return 'PRESENTATION_ERROR';
        }
        const initialTile = ret.output.split(' ');
        if (!initialTile.every(GameManager.isPositiveInteger)) {
          return 'PRESENTATION_ERROR';
        }

        const [row, col] = initialTile.map(v => {
          return parseInt(v, 10) - 1;
        });
        if (!boards[player].board.withinBounds({ row, col })) {
          return 'INVALID_POSITION';
        }
        boards[(player + 1) % 2].board.insertTile(new Tile({ row, col }, 2));

        return null;
      })();

      if (reason) {
        return buildResult((player + 1) % 2, reason);
      }
    }

    // Game Body
    for (turn = 1; turn <= GameManager.MAX_TURN; turn++) {
      for (let player = 0; player < 2; player++) {
        const runner = this.runners[player];
        const myBoard = boards[player];
        const enemyBoard = boards[(player + 1) % 2];

        const input = {
          commands: [
            `${turn} ${Math.round(timeLefts[player])} ${scores[player]} ${scores[(player + 1) % 2]}`,
            myBoard.toString(),
            enemyBoard.toString(),
          ],
        };
        const ret = await runner.sendAndReceiveData(input, GameManager.MAIN_TURN_TIME_MS);

        console.log(ret);
        this.ioLog.push([input, ret]);

        consumedTimesNs[player] += ret.runtimeNs;

        if (ret.verdict !== 'OK') {
          if (ret.verdict === 'TLE') {
            return buildResult((player + 1) % 2, 'TIME_LIMIT_EXCEEDED');
          }
          return buildResult((player + 1) % 2, 'RUNTIME_ERROR');
        }

        // Validate Return Value
        const reason = (() => {
          if (!ret.output) {
            return 'PRESENTATION_ERROR';
          }
          const [command, ...attack] = ret.output.split(' ');
          if (!command) {
            return 'PRESENTATION_ERROR';
          }

          // Move
          const dirCommand = CHAR_TO_DIRECTION.get(command);
          if (!dirCommand) {
            return 'INVALID_DIRECTION';
          }
          const movedResult = myBoard.move(dirCommand);
          if (!movedResult.moved) {
            return 'INVALID_DIRECTION';
          }

          // Attack
          const parsed = GameManager.PARSE_ATTACK_COMMAND(attack, enemyBoard.board);
          if (parsed.verdict !== 'OK' || !parsed.tiles) {
            return parsed.reason || 'PRESENTATION_ERROR';
          }

          let usedValue = 0;
          for (const tile of parsed.tiles) {
            if (enemyBoard.board.tileAt({ row: tile.row, col: tile.col })) {
              return 'INVALID_PLACE_TO_PUT';
            }
            enemyBoard.board.insertTile(tile);
            usedValue += tile.value || 0;
          }

          if (usedValue !== Math.pow(2, movedResult.countMerged + 1)) {
            return 'INVALID_VALUE_TO_PUT';
          }

          scores[player] += movedResult.scoreObtained;
          timeLefts[player] -= ret.runtimeNs / 1000 / 1000;

          return null;
        })();

        if (reason) {
          return buildResult((player + 1) % 2, reason);
        }
      } // player
    } // turn

    // verdict by score
    if (scores[0] > scores[1]) {
      return buildResult(0, 'SCORE');
    } else if (scores[1] > scores[0]) {
      return buildResult(1, 'SCORE');
    } else {
      return buildResult(1, 'EVEN_SCORE');
    }
  }

  public writeLogsToFile(fileName: string) {
    if (process.env.RUNTIME === 'node') {
      const writer = require('fs').createWriteStream(fileName); // tslint:disable-line no-require-imports
      writer.setDefaultEncoding('utf-8');
      const logList = this.ioLog.map(
        ([input, output], turn): TurnRecord => {
          return {
            playerIndex: turn % 2 === 0 ? 0 : 1,
            runtime: output.runtimeNs / 1000 / 1000 || 0,
            aiinput: input.commands.join('\n'),
            aioutput: output.output,
          };
        }
      );
      writer.write(JSON.stringify(logList));
      writer.end();
    }
  }
}
