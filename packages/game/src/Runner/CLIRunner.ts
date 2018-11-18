import readline from 'readline';
import { RunnerBase } from './RunnerBase';
import { Input, Output } from './types';

// TODO (fixme) the process dies when EOF came before the end of execution
export class CLIRunner extends RunnerBase {
  private reader: readline.ReadLine;
  private closed = false;
  private queue: string[] = [];

  constructor() {
    super();

    process.stdin.on('end', () => {
      this.closed = true;
    });

    this.reader = readline.createInterface(process.stdin);

    this.reader.on('line', this.inputListener);

    this.reader.on('close', () => {
      this.closed = true;
    });
  }

  public async sendAndReceiveData(data: Input, timeLimitMs: number): Promise<Output> {
    console.log('===== Input =====');
    data.commands.forEach(c => {
      console.log(c);
    });
    console.log('=================');

    if (this.closed) {
      return { verdict: 'DEAD', runtimeNs: 0 };
    }

    try {
      if (this.queue.length === 0) {
        this.reader.removeListener('line', this.inputListener);

        return new Promise<Output>(resolve => {
          this.reader.once('line', (line: string) => {
            this.reader.on('line', this.inputListener);
            resolve({ verdict: 'OK', runtimeNs: 1, output: line });
          });
        });
      } else {
        const line = this.queue.shift();

        return { verdict: 'OK', runtimeNs: 1, output: line };
      }
    } catch (e) {
      return { verdict: 'RE', runtimeNs: 0 };
    }
  }

  public async close() {
    this.reader.close();
  }

  private inputListener = (line: string) => {
    this.queue.push(line);
  };
}
