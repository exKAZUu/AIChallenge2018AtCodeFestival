import child_process from 'child_process';
import readline from 'readline';
import { RunnerBase } from './RunnerBase';
import { Input, Output } from './types';

export class CommandRunner extends RunnerBase {
  private static readonly EPS_MS = 10; // ms buffer time
  private static readonly INF_TIME_MS = 60 * 60 * 1000; // 1 hour

  private process: child_process.ChildProcess;
  private reader: readline.ReadLine;
  private isAlive = true;
  private readonly pauseCommand?: string;
  private readonly unpauseCommand?: string;

  constructor(runCommand: string, workingDir?: string, pauseCommand?: string, unpauseCommand?: string) {
    super();

    if (runCommand.split(' ').length > 1) {
      const [command, ...args] = runCommand.split(' ');
      this.process = child_process.spawn(command, args, { cwd: workingDir || '.' });
    } else {
      this.process = child_process.spawn(runCommand, [], { cwd: workingDir || '.' });
    }

    this.process.stdout.setEncoding('utf-8');
    this.reader = readline.createInterface(this.process.stdout);
    this.pauseCommand = pauseCommand;
    this.unpauseCommand = unpauseCommand;

    // TODO: call unpauseCommand before running AI program and pauseCommand after running AI program

    this.process.on('disconnect', () => {
      this.isAlive = false;
      console.log('[CommandRunner] process disconnected.');
    });
    this.process.on('error', err => {
      this.isAlive = false;
      console.log(`[CommandRunner] Error on process: ${err.message}`);
    });
    this.process.on('close', (code, signal) => {
      this.isAlive = false;
      console.log(`[CommandRunner] process closed with code ${code} and signal ${signal}`);
    });
    this.process.on('exit', (code, signal) => {
      this.isAlive = false;
      console.log(`[CommandRunner] process exited with code ${code} and signal ${signal}`);
    });

    this.process.stdin.on('error', err => {
      this.isAlive = false;
      console.log(`[CommandRunner] Error on process stdin: ${err.message}`);
    });
  }

  public async sendAndReceiveData(data: Input, timeLimitMs: number): Promise<Output> {
    if (!this.isAlive) {
      return { verdict: 'DEAD', runtimeNs: 0 };
    }

    const timeLimit = timeLimitMs > 0 ? timeLimitMs : CommandRunner.INF_TIME_MS;
    console.log(data, timeLimitMs);

    try {
      let startTime: [number, number];
      let timer: NodeJS.Timeout;

      const promise = Promise.race([
        new Promise<Output>(resolve => {
          this.reader.once('line', (line: string) => {
            const [_, runtimeNs] = process.hrtime(startTime);
            clearTimeout(timer);
            if (runtimeNs > (timeLimit + CommandRunner.EPS_MS) * 1000 * 1000) {
              this.isAlive = false;
              resolve({ verdict: 'TLE', runtimeNs, output: line.trim() });
            } else {
              resolve({ verdict: 'OK', runtimeNs, output: line.trim() });
            }
          });
        }),
        new Promise<Output>(resolve => {
          timer = setTimeout(() => {
            const [_, runtimeNs] = process.hrtime(startTime);
            this.isAlive = false;
            resolve({ verdict: 'TLE', runtimeNs });
          }, timeLimit + CommandRunner.EPS_MS * 100);
        }),
      ]);

      // TODO use util.promisify
      await new Promise((resolve, reject) => {
        this.process.stdin.write(data.commands.join('\n').concat('\n'), 'utf-8', err => {
          if (err) {
            this.isAlive = false;
            reject();
          } else {
            resolve();
          }
        });
      });

      this.process.stdin.uncork();
      startTime = process.hrtime();

      return await promise;
    } catch (e) {
      this.isAlive = false;

      return { verdict: 'RE', runtimeNs: 0 };
    }
  }

  public async close() {
    this.isAlive = false;
    this.reader.close();
    if (!this.process.killed) {
      this.process.kill();
    }
  }
}
