import * as path from 'path';
import { Readable } from 'stream';

export const tempDir = path.join(__dirname, 'springup', 'temp');

export const KEYS = {
  up: '\x1B\x5B\x41',
  down: '\x1B\x5B\x42',
  downx: '\u001B[B',
  downxx: '\u001B[1B',
  enter: '\x0D',
  space: '\x20'
}

export const sleep = (ms: number) => new Promise(r => setTimeout(r, ms));

export function stdin(): Readable {
  const io = new Readable();
  io._read = function () {};
  return io;
}

export function sendKeystrokes(stdin: Readable, keys: Array<Array<string>>) {
  return async () => {
    keys.forEach(async outer => {
      outer.forEach(inner => {
        console.log("sending", inner);
        stdin.push(inner);
      });
      console.log("sleep1");
      await sleep(1000);
      console.log("sleep2");
    });
  };
}

export async function sendKeystrokesx(stdin: Readable, keys: Array<Array<string>>): Promise<void> {
  for (let outer of keys) {
    for (let inner of outer) {
      console.log("sending1", inner);
      stdin.push(inner);
      console.log("sending2", inner.length);
    }
    console.log("sleep1");
    await sleep(1000);
    console.log("sleep2");
  }
}
