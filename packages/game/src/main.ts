import yargs = require('yargs'); // tslint:disable-line:no-require-imports
import packageJson = require('../package.json'); // tslint:disable-line:no-require-imports
import { GameManager } from './GameManager';
import { CommandRunner } from './Runner/CommandRunner';
import { RandomRunner } from './Runner/RandomRunner';
import { RunnerBase } from './Runner/RunnerBase';
import { SmartRunner } from './Runner/SmartRunner';

const argv = yargs
  .usage('Usage: $0 [options]')
  .example(
    '$0 -a "java SampleAI" -w "./examples/Java" -a "python SampleAI.py" -w "./examples/Python2"',
    'Run game with two AI programs.'
  )
  .nargs('a', 1)
  .alias('a', 'aiCommand')
  .describe('a', 'Command to run an AI program.')
  .nargs('w', 1)
  .alias('w', 'workingDirectory')
  .describe('w', 'Working directory where AI program runs.')
  .nargs('p', 1)
  .alias('p', 'pauseCommand')
  .nargs('u', 1)
  .alias('u', 'unpauseCommand')
  .help('h')
  .alias('h', 'help')
  .version(packageJson.version)
  .alias('o', 'output')
  .describe('o', 'Output filename of a game log (for visualizer).')
  .default('o', 'game.log')
  .alias('v', 'version').argv;

function getStringArray(args?: string | string[]): string[] {
  return args ? (typeof args === 'string' ? [args] : args) : [];
}

const aiCmds = getStringArray(argv.a);
if (aiCmds.length >= 3) {
  throw new Error('The number of `-a` options must be less than 3.');
}
const workingDirs = getStringArray(argv.w);
if (aiCmds.length !== workingDirs.length && workingDirs.length) {
  throw new Error('The number of `-w` options must be 0 or same with the number of `-a` options.');
}
const pauseCmds = getStringArray(argv.p);
const unpauseCmds = getStringArray(argv.u);
if (pauseCmds.length === unpauseCmds.length && (pauseCmds.length !== 0 && pauseCmds.length !== 2)) {
  throw new Error('The both numbers of `-p` and `-u` options must be 0 or 2.');
}

(async () => {
  const runners: RunnerBase[] = [];
  for (let i = 0; i < 2; i++) {
    const runner = aiCmds[i]
      ? new CommandRunner(aiCmds[i], workingDirs[i], pauseCmds[i], unpauseCmds[i])
      : new RandomRunner();
    runners.push(runner);
  }

  const game = new GameManager(runners[0], runners[1]);
  const result = await game.gameMain();

  runners.forEach(runner => {
    runner.close();
  });

  console.log(result);

  game.writeLogsToFile(argv.output);

  return result;
})().catch(e => {
  console.error('Fatal Error: ', e);
});
