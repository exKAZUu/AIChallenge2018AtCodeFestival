export type GameResultReason =
  | 'TIME_LIMIT_EXCEEDED'
  | 'PRESENTATION_ERROR'
  | 'INVALID_DIRECTION'
  | 'INVALID_POSITION'
  | 'INVALID_PLACE_TO_PUT'
  | 'INVALID_VALUE_TO_PUT'
  | 'RUNTIME_ERROR'
  | 'SCORE'
  | 'EVEN_SCORE';

type PlayerStats = {
  time: number;
  score: number;
};

export type GameResult = {
  winner: 'FIRST' | 'SECOND';
  defeatReason: GameResultReason;
  turn: number;
  stats: {
    first: PlayerStats;
    second: PlayerStats;
  };
};

export type TurnRecord = { aiinput?: string; aioutput?: string; runtime?: number; playerIndex?: 0 | 1 };
