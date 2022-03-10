import * as os from 'os';
import * as pty from 'node-pty';
import * as fs from 'fs';
import * as path from 'path';
import { rmRF, mkdirP } from '@actions/io';
import { Terminal, IDisposable } from 'xterm-headless';
import 'jest-extended';
import { KEYS, sleep, tempDir, stdin, sendKeystrokes, sendKeystrokesx } from '../src/utils';
import { Cli, CliOptions } from '../src/cli';

describe('initializr interactive', () => {

  beforeAll(async () => {
    await rmRF(tempDir);
    await mkdirP(tempDir);
  }, 300000);

  afterAll(async () => {
    try {
      await rmRF(tempDir);
    } catch {
      console.log('Failed to remove test directories');
    }
  }, 100000);

  it('create maven project', async () => {
    const demoDir = path.join(tempDir, 'demo');
    var isWindows = os.platform() === 'win32';
    const command = isWindows ? '../build/native/nativeCompile/initializr-cli.exe' : '../build/native/nativeCompile/initializr-cli';
    const cli = new Cli({
      command: command,
      options: [
        'init',
        `--path ${demoDir}`,
        '--language java',
        '--boot-version 2.6.4',
        '--version 0.0.1-SNAPSHOT',
        '--group com.example',
        '--artifact demo',
        '--name demo',
        '--description Demo',
        '--package-name com.example.demo',
        '--dependencies camel,derby',
        '--packaging jar',
        '--java-version 11'
      ]
    });
    cli.run();
    await sleep(1000);
    console.log(cli.lines());
    // await cli.keyDown(1000).then(cli => cli.enter(1000));
    await cli.keyDown();
    console.log(cli.lines());
    await cli.enter();
    // await sleep(1000);
    // console.log(cli.lines());
    // cli.enter();
    // await sleep(1000);
    console.log(cli.lines());
    cli.dispose();
    const buildFile = path.join(demoDir, 'pom.xml');
    expect(fs.existsSync(buildFile)).toBe(true);
  });

  xit('should create maven projectx', async () => {

    const demoDir = path.join(tempDir, 'demo');
    const ptyProcess = pty.spawn('spring-up', [
      'initializr new',
      `--path ${demoDir}`,
      '--language java',
      '--boot-version 2.6.4',
      '--version 0.0.1-SNAPSHOT',
      '--group com.example',
      '--artifact demo',
      '--name demo',
      '--description Demo',
      '--package-name com.example.demo',
      '--dependencies camel,derby',
      '--packaging jar',
      '--java-version 11'
    ], {
      name: 'xterm-256color',
      cols: 80,
      rows: 30,
    });

    const terminal = new Terminal({
      cols: 80,
      rows: 30,
    });

    ptyProcess.onData(data => {
      console.log('xxx1', data);
      terminal.write(data, () => {
        console.log('xxx11');
      });
    });

    terminal.onData(data => {
      console.log('xxx2', data);
      process.stdout.write(data);
    });


    await sleep(1000);
    // terminal.write('h');
    ptyProcess.write('\x1BOA');
    // terminal.write(KEYS.down);
    // ptyProcess.write('\r');
    await sleep(1000);
    // ptyProcess.write(KEYS.enter);
    await sleep(1000);
    console.log('ddd', terminal.buffer.active.getLine(0)?.translateToString());
    console.log('ddd', terminal.buffer.active.getLine(1)?.translateToString());
    console.log('ddd', terminal.buffer.active.getLine(2)?.translateToString());
    // ptyProcess.write(KEYS.enter);
    await sleep(1000);
    console.log('killing');
    ptyProcess.kill();

    const mavenBuild = path.join(demoDir, 'pom.xml');
    expect(fs.existsSync(mavenBuild)).toBe(true);
  });

});
