import { Board } from '../../game/src/Core/Board';
import { BoardManager } from '../../game/src/Core/BoardManager';
import { Tile } from '../../game/src/Core/Tile';
import { CHAR_TO_DIRECTION } from '../../game/src/Core/types';
import { GameManager } from '../../game/src/GameManager';
import { GameResult, TurnRecord } from '../../game/src/types';
import { HTMLActuator } from './HTMLActuator';
import './main.scss'; // tslint:disable-line:no-import-side-effect

const viewerQueryPrefix = '.buttle-2048-viewer';

const nRow = 5;
const nCol = 5;

const gameIntervalMin = 100;
const gameIntervalMax = 1000;
const initialTimeLeft = 100 * 1000;

const bms: BoardManager[] = [];
let commands: TurnRecord[] = [];
let resultsAvailable = false;

let nextTurn = 0;
let currentScores = [0, 0];
const mergeCounts = [0, 0];
let currentTimeLeft = [initialTimeLeft, initialTimeLeft];

let gameInterval = 500;
let timerId: number | undefined;

let turnText: Element | null;
let scoreTexts: (Element | null)[] = [];
let timeLeftTexts: (Element | null)[] = [];
let mergeCountTexts: (Element | null)[] = [];
let playerDivs: (Element | null)[] = [];
let resultsDiv: Element | null;
let winnerText: Element | null;
let reasonText: Element | null;
let toggleButton: HTMLElement | null;
let turnSlider: HTMLInputElement | null;

function updateTurn() {
  if (turnText) {
    turnText.textContent = String(Math.floor((nextTurn + 1) / 2));
  }
  const turnPlayer = nextTurn % 2;
  playerDivs.forEach((e, i) => {
    if (e) {
      e.className = i === turnPlayer ? 'player turn-player' : 'player';
    }
  });

  scoreTexts.forEach((e, i) => {
    if (e) {
      e.textContent = String(currentScores[i]);
    }
  });

  timeLeftTexts.forEach((e, i) => {
    if (e) {
      e.textContent = String(Math.round(currentTimeLeft[i]));
    }
  });

  mergeCountTexts.forEach((e, i) => {
    if (e) {
      e.textContent = String(mergeCounts[i]);
    }
  });

  if (resultsDiv) {
    if (nextTurn >= commands.length - 1 && resultsAvailable) {
      resultsDiv.className = 'results results-show';
    } else {
      resultsDiv.className = 'results results-hide';
    }
  }

  if (turnSlider) {
    turnSlider.value = nextTurn.toString(10);
  }
}

function advanceFrame() {
  if (commands.length <= nextTurn) {
    stopAnimation();
    return;
  }

  const obj = commands[nextTurn];
  console.log(obj, obj.aiinput, obj.aioutput);
  if (obj.aioutput) {
    const command = obj.aioutput.split(' ');
    if (nextTurn < 2) {
      // put initial tiles
      const [row, col] = command.map(v => {
        return parseInt(v, 10) - 1;
      });
      bms[(nextTurn + 1) % 2].board.insertTile(new Tile({ row, col }, 2));
      bms[(nextTurn + 1) % 2].actuate();
    } else {
      const [dirCommand, ...attack] = command;

      // Move
      const dir = CHAR_TO_DIRECTION.get(dirCommand);
      if (dir) {
        const result = bms[nextTurn % 2].move(dir);
        currentScores[nextTurn % 2] += result.scoreObtained;
        mergeCounts[nextTurn % 2] = result.countMerged;
      }

      // Attack
      bms[(nextTurn + 1) % 2].prepareTiles();
      const parsed = GameManager.PARSE_ATTACK_COMMAND(attack, bms[(nextTurn + 1) % 2].board);
      if (parsed.tiles) {
        parsed.tiles.forEach(tile => {
          bms[(nextTurn + 1) % 2].board.insertTile(tile);
        });
        bms[(nextTurn + 1) % 2].actuate();
      }

      if (obj.runtime) {
        currentTimeLeft[nextTurn % 2] -= obj.runtime;
      }
    }
  }

  updateTurn();

  nextTurn++;
}

function timerHandle() {
  advanceFrame();

  timerId = window.setTimeout(timerHandle, gameInterval);
}

function forceUpdateTurn(turn: number) {
  if (turn >= 0 && turn < commands.length) {
    const previouslyRunning = !!timerId;

    stopAnimation();

    if (turn < 2) {
      bms.forEach(bm => {
        bm.setup();
      });

      nextTurn = 0;
      currentScores = [0, 0];
      currentTimeLeft = [initialTimeLeft, initialTimeLeft];
    } else {
      const inputString = commands[turn].aiinput;
      if (inputString) {
        const inputStrings = inputString.split('\n');
        bms[turn % 2].board = Board.FROM_STRINGS(inputStrings.slice(1, nRow + 1));
        bms[(turn + 1) % 2].board = Board.FROM_STRINGS(inputStrings.slice(nRow + 1, nRow * 2 + 1));

        const [_, timeLeft, scoreMe, scoreOther] = inputStrings[0].split(' ').map(v => parseInt(v, 10));
        currentScores = turn % 2 === 0 ? [scoreMe, scoreOther] : [scoreOther, scoreMe];
        currentTimeLeft[turn % 2] = timeLeft;

        if (turn === 2) {
          currentTimeLeft[1] = initialTimeLeft;
        } else {
          const { aiinput: preInput, runtime: preRuntime } = commands[turn - 1];
          if (preInput && preRuntime) {
            const prevTimeLeft = parseInt(preInput.split('\n')[0].split(' ')[1], 10);
            currentTimeLeft[(turn + 1) % 2] = prevTimeLeft - preRuntime;
          }
        }
      }
      bms.forEach(bm => {
        bm.prepareTiles();
        bm.actuate();
      });
      nextTurn = turn - 1;
    }
    updateTurn();

    // temporary fix to show accurate turn number
    nextTurn = turn < 2 ? 0 : turn;

    if (previouslyRunning) {
      startAnimation();
    }
  }
}

function stopAnimation() {
  if (timerId) {
    clearTimeout(timerId);
  }
  timerId = undefined;

  if (toggleButton) {
    toggleButton.textContent = '再生';
  }
}

function startAnimation() {
  stopAnimation();

  let startTurn = 0;
  if (turnSlider) {
    turnSlider.setAttribute('max', (commands.length - 1).toString(10));
    startTurn = parseInt(turnSlider.value, 10) + 1;
  }

  forceUpdateTurn(startTurn);

  if (toggleButton) {
    toggleButton.textContent = '停止';
  }

  timerId = window.setTimeout(timerHandle, gameInterval);
}

function prepareField() {
  ['#player-1 .board-container', '#player-2 .board-container'].forEach(frameQuery => {
    const gameContainer = document.querySelector(`${viewerQueryPrefix} ${frameQuery}`);
    if (gameContainer) {
      const gridContainer = createElementWithClass('div', 'grid-container');

      for (let row = 0; row < nRow; row++) {
        const gridRow = createElementWithClass('div', 'grid-row');
        for (let col = 0; col < nCol; col++) {
          gridRow.appendChild(createElementWithClass('div', 'grid-cell'));
        }
        gridContainer.appendChild(gridRow);
      }
      gameContainer.appendChild(gridContainer);

      const tileContainer = createElementWithClass('div', 'tile-container');
      gameContainer.appendChild(tileContainer);

      bms.push(new BoardManager(nRow, nCol, undefined, new HTMLActuator(tileContainer)));
    }
  });
}

function createElementWithClass(tagName: string, className: string) {
  const elem = document.createElement(tagName);
  elem.setAttribute('class', className);
  return elem;
}

function selectPlayerElements(idPrefix: string) {
  return [1, 2].map(i => document.querySelector(`${viewerQueryPrefix} #${idPrefix}-${i}`));
}

function prepareButtons() {
  // output elements
  turnText = document.querySelector(`${viewerQueryPrefix} #turn`);
  scoreTexts = selectPlayerElements('score');
  timeLeftTexts = selectPlayerElements('time-left');
  mergeCountTexts = selectPlayerElements('merge-count');

  [turnText, ...scoreTexts, ...timeLeftTexts, ...mergeCountTexts].forEach(e => {
    if (e) {
      e.textContent = '0';
    }
  });

  playerDivs = selectPlayerElements('player');
  resultsDiv = document.querySelector(`${viewerQueryPrefix} #results`);
  if (resultsDiv) {
    resultsDiv.className = 'results results-hide';
  }
  winnerText = document.querySelector(`${viewerQueryPrefix} #winner`);
  if (winnerText) {
    winnerText.textContent = 'PLAYER X WINS!';
  }
  reasonText = document.querySelector(`${viewerQueryPrefix} #reason`);
  if (reasonText) {
    reasonText.textContent = 'UNDEFINED REASON';
  }

  // input elements
  const slider = document.querySelector(`${viewerQueryPrefix} #game-interval-slider`);
  if (slider) {
    const toGameInterval = (x: number) => gameIntervalMax - x * (gameIntervalMax - gameIntervalMin);
    slider.setAttribute('value', ((gameIntervalMax - gameInterval) / (gameIntervalMax - gameIntervalMin)).toString());
    slider.addEventListener('change', event => {
      gameInterval = toGameInterval(Number((<HTMLInputElement>event.target).value));
    });
  }

  const fileInput = document.querySelector(`${viewerQueryPrefix} #file-input`);
  if (fileInput) {
    fileInput.addEventListener('change', loadFile);
  }

  const reloadButton = <HTMLButtonElement>document.querySelector(`${viewerQueryPrefix} #reload-button`);
  if (reloadButton) {
    reloadButton.onclick = loadFile;
  }

  turnSlider = <HTMLInputElement>document.querySelector(`${viewerQueryPrefix} #turn-slider`);
  if (turnSlider) {
    turnSlider.addEventListener('change', event => {
      forceUpdateTurn(parseInt((<HTMLInputElement>event.target).value, 10));
    });
  }

  toggleButton = document.querySelector(`${viewerQueryPrefix} #play-toggle`);
  if (toggleButton) {
    toggleButton.onclick = () => {
      if (timerId) {
        stopAnimation();
      } else {
        startAnimation();
      }
    };
  }

  const prevButton = <HTMLButtonElement>document.querySelector(`${viewerQueryPrefix} #turn-prev`);
  if (prevButton) {
    prevButton.onclick = () => {
      forceUpdateTurn(Math.max(nextTurn - 1, 0));
    };
  }

  const nextButton = <HTMLButtonElement>document.querySelector(`${viewerQueryPrefix} #turn-next`);
  if (nextButton) {
    nextButton.onclick = advanceFrame;
  }
}

function loadFile() {
  stopAnimation();

  const inputElement = <HTMLInputElement>document.querySelector(`${viewerQueryPrefix} #file-input`);
  if (!inputElement || !inputElement.files) {
    return;
  }

  const file = inputElement.files[0];
  const reader = new FileReader();

  reader.onload = () => {
    try {
      commands = preprocessReplay(JSON.parse(<string>reader.result));
      forceUpdateTurn(0);
      startAnimation();
    } catch (e) {
      alert('データの準備に失敗しました。\n標準では game.log が該当の出力ファイルです。');
      console.log(e);
    }
  };
  reader.readAsText(file, 'utf-8');
}

function tryLoadingWindowReplay() {
  const replay = (<any>window).replay;
  if (!replay) {
    return;
  }

  const playerNames = (<any>window).playerNames;
  const userNames = (<any>window).userNames;
  if (playerNames && playerNames.length && userNames && userNames.length) {
    for (let i = 0; i < 2; i++) {
      const elem = document.querySelector(`#player-${i + 1} .header`);
      if (elem) {
        elem.textContent = `${playerNames[i]} (${userNames[i]})`;
      }
    }
  }

  const fileInput = document.querySelector(`${viewerQueryPrefix} #file-input`);
  if (fileInput) {
    fileInput.remove();
  }

  commands = preprocessReplay(replay);
  forceUpdateTurn(0);
  startAnimation();
}

function preprocessReplay(replay: any) {
  if (Array.isArray(replay)) {
    // old format log data.
    resultsAvailable = false;
    return <TurnRecord[]>replay;
  } else {
    const result = <GameResult>replay.gameResult;
    // new format log data.
    if (winnerText) {
      winnerText.textContent = `PLAYER ${result.winner} WINS!`;
    }
    if (reasonText) {
      reasonText.textContent = result.defeatReason;
    }
    resultsAvailable = true;
    return <TurnRecord[]>replay.commands;
  }
}

window.addEventListener('load', () => {
  prepareField();
  prepareButtons();
  tryLoadingWindowReplay();
});
