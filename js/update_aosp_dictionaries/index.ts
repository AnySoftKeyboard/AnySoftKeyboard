import { Command } from 'commander';
import fs from 'fs';
import https from 'follow-redirects';
import { tmpdir } from 'os';
import zlib from 'zlib';
import * as tar from 'tar';
import { join } from 'path';
import { setFailed } from '@actions/core';
import yaml from 'js-yaml';
import { TextFileDiff } from './differ.js';

async function downloadFileToTemp(url: string): Promise<string> {
  const tempFile = join(tmpdir(), `${Math.random().toString(16).substring(2)}_dictionaries.tar.gz`);

  return new Promise((resolve, reject) => {
    https.https.get(url, (res) => {
      const fileStream = fs.createWriteStream(tempFile);
      res
        .pipe(fileStream)
        .on('error', (e) => reject(e))
        .on('finish', () => {
          fileStream.close();
          resolve(tempFile);
        });
    });
  });
}

async function decompressTarGz(tarGzFilePath: string): Promise<string> {
  return new Promise((resolve, reject) => {
    return fs.promises
      .mkdir(join(tmpdir(), Math.random().toString(16).substring(2)), { recursive: true })
      .then((folder) => {
        if (folder) return folder;
        else throw Error('could not create a folder in tmp-dir');
      })
      .then((destinationFolder) => {
        fs.createReadStream(tarGzFilePath)
          .on('error', (err) => reject(err))
          .pipe(zlib.createGunzip({ finishFlush: zlib.constants.Z_SYNC_FLUSH }))
          .on('error', (err) => reject(err))
          .pipe(tar.x({ cwd: destinationFolder }))
          .on('error', (err) => reject(err))
          .on('finish', () => resolve(destinationFolder));
      });
  });
}

async function decompressGz(gzFilePath: string): Promise<string> {
  const targetFilename = gzFilePath.replace(/^.*[\\/]/, '').replace('.gz', '');
  return new Promise((resolve, reject) => {
    return fs.promises
      .mkdir(join(tmpdir(), Math.random().toString(16).substring(2)), { recursive: true })
      .then((folder) => {
        if (folder) return folder;
        else throw Error('could not create a folder in tmp-dir');
      })
      .then((destinationFolder) => {
        fs.createReadStream(gzFilePath)
          .on('error', (err) => reject(err))
          .pipe(zlib.createGunzip({ finishFlush: zlib.constants.Z_SYNC_FLUSH }))
          .on('error', (err) => reject(err))
          .pipe(fs.createWriteStream(join(destinationFolder, targetFilename)))
          .on('error', (err) => reject(err))
          .on('finish', () => resolve(join(destinationFolder, targetFilename)));
      });
  });
}

async function readYamlToDictionary(yamlFilePath: string): Promise<{ mapping: Array<Array<string>> }> {
  return fs.promises
    .readFile(yamlFilePath, 'utf-8')
    .then((fileContents) => yaml.load(fileContents) as { mapping: Array<Array<string>> });
}

async function compareArchives(src: string, trgt: string): Promise<Array<string>> {
  const srcFile = await decompressGz(src);
  console.log(`Source file: ${srcFile}`);
  const targetFile = await decompressGz(trgt);
  console.log(`Target file: ${targetFile}`);

  const differ = new TextFileDiff();

  const diffs: string[] = [];
  differ.on('-', (line) => diffs.push(` * REMOVED: '${line}'`));
  differ.on('+', (line) => diffs.push(` * NEW: '${line}'`));

  return differ.diff(srcFile, targetFile).then((_) => diffs);
}

const program = new Command();
program.name('update-aosp-dictionaries').description('CLI to update AOSP gz aosp wordlists').version('0.0.1');

program
  .command('update')
  .requiredOption('--dictionaries_archive <http_url>', 'URL to download the archive')
  .requiredOption('--dictionaries_mapping <path>', 'Path mapping file')
  .action(async (options) => {
    console.log(`Downloading archive from ${options.dictionaries_archive}...`);
    const dictionaries_folder = await downloadFileToTemp(options.dictionaries_archive).then((archive_file) => {
      console.log(`Decompressing ${archive_file}...`);
      return decompressTarGz(archive_file);
    });

    console.log(`Dictionaries available at ${dictionaries_folder}.`);
    console.log(`Reading file mapping from ${options.dictionaries_mapping}...`);
    const mappings = await readYamlToDictionary(options.dictionaries_mapping).then((data) => data.mapping);

    for (const mapping of mappings) {
      const src = `${dictionaries_folder}/${mapping[0]}`;
      const trgt = `${process.env.BUILD_WORKSPACE_DIRECTORY}/${mapping[1]}`;
      console.log('********************');
      console.log(` - comparing remote ${src} to local ${trgt}`);
      try {
        const patchDiffs = await compareArchives(src, trgt);
        patchDiffs.forEach((line) => console.log(line));
        if (patchDiffs.length === 0) {
          console.log(` - skipping, the files are identical.`);
        } else {
          console.log(` - copying...`);
          fs.copyFileSync(src, trgt);
        }
      } catch (e) {
        console.log(`Error while working: ${e}`);
      }
      console.log('********************');
    }
  });

const main = async () => {
  program.parse();
};

main().catch((err) => setFailed(err.message));
