import { Input, Output } from './types';

export abstract class RunnerBase {
  protected constructor() {}

  /**
   * AIの入力にdata.commandsを改行区切りで入力し、データを1行ぶんだけ受け取るまで待ち、結果を返す
   * @param data 入力するデータ
   * @param timeLimitMs 時間制限。0以下の値の場合は制限なしとする。
   */
  public abstract sendAndReceiveData(data: Input, timeLimitMs: number): Promise<Output>;

  public close() {
    // nothing to do in default
  }
}
