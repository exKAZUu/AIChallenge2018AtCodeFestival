export interface Input {
  commands: string[];
}

export interface Output {
  /**
   * コードの実行状態
   * 正常, 既に死んでいる, 死んだ, 時間経過, 内部エラー
   */
  verdict: 'OK' | 'DEAD' | 'RE' | 'TLE' | 'IE';

  /**
   * コードの実行に要したナノ秒での時間
   */
  runtimeNs: number;

  /**
   * verdict === 'OK' の場合、コードの実行結果
   * そのほかの場合、エラーの詳細（ないときもある）
   */
  output?: string;
}
