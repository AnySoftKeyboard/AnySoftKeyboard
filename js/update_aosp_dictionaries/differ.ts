// Code copied from https://github.com/niiknow/text-file-diff
// Used under MIT license. See https://github.com/niiknow/text-file-diff/blob/215bbf9c9724727cde06b981cb5954b52915f5e4/LICENSE
import { EventEmitter } from 'events';
import { createReadStream } from 'fs';
import { createInterface } from 'readline';
import stream from 'stream';

// import myDebug = require('debug');
// const debug = myDebug('text-file-diff');

export class StreamLineReader {
  value: string = '';
  nextValue: string = '';
  lineNumber: number = -1;
  it?: AsyncIterableIterator<string>;
  eof: number = -1;
  async init(readStream: stream.Readable): Promise<StreamLineReader> {
    const rl = createInterface({
      input: readStream,
      crlfDelay: Number.POSITIVE_INFINITY,
    });
    this.it = rl[Symbol.asyncIterator]();

    // move to first line
    await this.moveNext();
    await this.moveNext();

    return this;
  }

  async moveNext(): Promise<string> {
    this.value = this.nextValue;

    const nextResult = await this.it?.next();

    if (nextResult?.done) {
      this.eof++;
      nextResult.value = '';
    }

    this.nextValue = nextResult?.value ?? '';
    this.lineNumber++;
    return this.value;
  }
}

/**
 * line by line diff of two files
 */
export class TextFileDiff extends EventEmitter {
  constructor() {
    super();
  }

  /**
   * run diff
   * @param  String file1 path to file 1
   * @param  String file2 path to file 2
   * @return Object         self
   */
  async diff(file1: string, file2: string): Promise<TextFileDiff> {
    const stream1 = createReadStream(file1);
    const stream2 = createReadStream(file2);
    return this.diffStream(stream1, stream2);
  }

  /**
   * run diffStream
   * @param  Readable stream1
   * @param  Readable stream2
   * @return Object         self
   */
  async diffStream(stream1: stream.Readable, stream2: stream.Readable): Promise<TextFileDiff> {
    const lineReader1 = await new StreamLineReader().init(stream1);
    const lineReader2 = await new StreamLineReader().init(stream2);

    const skipHeader = false;
    if (skipHeader) {
      await lineReader1.moveNext();
      await lineReader2.moveNext();
    }

    /* eslint-disable no-await-in-loop */
    // while both files has valid val, check eof counter
    while (lineReader1.eof < 2 && lineReader2.eof < 2) {
      await this.doCompareLineReader(lineReader1, lineReader2);
    }
    /* eslint-enable no-await-in-loop */

    return this;
  }

  private compareFn(line1: string, line2: string) {
    return line1 > line2 ? 1 : line1 < line2 ? -1 : 0;
  }

  async doCompareLineReader(lineReader1: StreamLineReader, lineReader2: StreamLineReader): Promise<void> {
    // forEach line in File1, compare to line in File2
    const line1 = lineReader1.value;
    const line2 = lineReader2.value;
    const cmpar = this.compareFn(line1, line2);

    // debug(line1, line1, cmpar);
    // debug(lineReader1.nextValue, lineReader2.nextValue, 'next', lineReader1.eof, lineReader2.eof);
    // emit on compared
    this.emit('compared', line1, line2, cmpar, lineReader1, lineReader2);

    if (cmpar > 0) {
      // line1 > line2: new line detected
      // if file2 ended before file1, then file2 lost line1
      // else file2 has new line
      if (lineReader2.eof > lineReader1.eof) {
        this.emit('-', line1, lineReader1, lineReader2);
      } else {
        this.emit('+', line2, lineReader1, lineReader2);
      }

      // incr File2 to next line
      await lineReader2.moveNext();
    } else if (cmpar < 0) {
      // line1 < line2: deleted line
      // if file1 ended before file2, then file2 has new line
      // else file1 lost a line
      if (lineReader1.eof > lineReader2.eof) {
        this.emit('+', line2, lineReader1, lineReader2);
      } else {
        this.emit('-', line1, lineReader1, lineReader2);
      }

      // incr File1 to next line
      await lineReader1.moveNext();
    } else {
      // equals: 0 incr both files to next line
      await lineReader1.moveNext();
      await lineReader2.moveNext();
    }
  }
}
