import * as pty from 'node-pty';
import { Terminal, IDisposable } from 'xterm-headless';
import { sleep } from './utils';

export interface CliOptions {
  command: string;
  options?: string[];
}

export class Cli {
  private isDisposed: boolean = false;
  private pty: pty.IPty | undefined;
  private term: Terminal | undefined;

  constructor(private options: CliOptions) {}

  public run(): void {
    this.pty = pty.spawn(this.options.command, this.options.options || [], {
      name: 'xterm-256color',
      cols: 80,
      rows: 30,
    });
    this.term = new Terminal({
      cols: 80,
      rows: 30,
    });
    this.pty.onData(data => {
      console.log(data);
      this.term?.write(data);
    });
  }

  public lines(): string[] {
    const l = this.term?.buffer.active.length || 0;
    const ret:string[] = [];
    for (let index = 0; index < l; index++) {
      const line = this.term?.buffer.active.getLine(index)?.translateToString();
      if (line) {
        ret.push(line);
      }
    }
    return ret;
  }

  public text(data: string): Cli {
    return this;
  }

  public keyUp(): Cli {
    this.pty?.write('\x1BOA');
    return this;
  }

  // public keyDown(): Cli {
  //   this.pty?.write('\x1BOB');
  //   return this;
  // }

  // public enter(): Cli {
  //   this.pty?.write('\x0D');
  //   return this;
  // }

  public async keyDown(wait: number = 200): Promise<Cli> {
    this.pty?.write('\x1BOB');
    // await this.writeSync('\x1BOB');
    await sleep(wait);
    return this;
  }

  public async enter(wait: number = 200): Promise<Cli> {
    this.pty?.write('\x0D');
    // await this.writeSync('\x0D');
    await sleep(wait);
    return this;
  }

  public dispose(): void {
    if (this.isDisposed) {
      return;
    }
    this.pty?.kill();
    this.term?.dispose();
    this.isDisposed = true;
  }

  // private writeSync(text: string): Promise<void> {
  //   return new Promise<void>(r => this.pty?.write(text, r));
  // }
}
