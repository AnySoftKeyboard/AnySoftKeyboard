import * as fs from 'fs';
import * as path from 'path';
import { XMLParser } from 'fast-xml-parser';
import { type Checker } from './checker.js';

const supportedOddOns = [
  ['Keyboards', 'Keyboard', 'keyboards.xml'],
  ['Dictionaries', 'Dictionary', 'dictionaries.xml'],
  ['QuickTextKeys', 'QuickTextKey', 'quick_text_keys.xml'],
  ['KeyboardThemes', 'KeyboardTheme', 'themes.xml'],
];

export function isAddOnsFile(fileName: string): boolean {
  return supportedOddOns.some((postFix) => fileName.toLowerCase().endsWith(postFix[2]));
}

export function getAddOnsFromXml(root): string[] {
  const addOnType = supportedOddOns.find((addOnType) => addOnType[0] in root);
  if (addOnType) {
    const addons = root[addOnType[0]][addOnType[1]];
    if (typeof addons[Symbol.iterator] === 'function') {
      return addons.map((addon) => addon.attr_id);
    } else {
      return [addons.attr_id];
    }
  } else {
    return [];
  }
}

export class UniqueAddOnIdChecker implements Checker {
  get name(): string {
    return 'UniqueAddOnIdChecker';
  }

  async check(root_dir: string, dir_skip_predicate: (dirname: string) => boolean): Promise<void> {
    const idSet = new Set<string>();
    const parserOptions = {
      ignoreAttributes: false,
      attributeNamePrefix: 'attr_',
    };
    return this.checkRec(root_dir, idSet, new XMLParser(parserOptions), dir_skip_predicate).then((sum) =>
      console.log(`Finished without duplications (scanned ${sum} add-ons).`),
    );
  }

  private async checkRec(
    root_dir: string,
    idSet: Set<string>,
    xmlParser: XMLParser,
    dir_skip_predicate: (dirname: string) => boolean,
  ): Promise<number> {
    return fs.promises.readdir(root_dir).then((files) => {
      const promises = files.map((file) => {
        const filePath = path.join(root_dir, file);
        return fs.promises.stat(filePath).then((stat) => {
          if (stat.isDirectory()) {
            if (!dir_skip_predicate(filePath)) {
              // Recursively scan subfolders
              return this.checkRec(filePath, idSet, xmlParser, dir_skip_predicate);
            }
          } else {
            if (isAddOnsFile(file)) {
              return fs.promises.readFile(filePath, 'utf-8').then((xmlData) => {
                const dict = xmlParser.parse(xmlData);
                const addOns = getAddOnsFromXml(dict);
                try {
                  for (const id of addOns) {
                    if (
                      filePath.indexOf('ime/app/src/main/res/xml') >= 0 &&
                      filePath.endsWith('/quick_text_keys.xml')
                    ) {
                      // QuickKeys in the app has multiple implementations
                      continue;
                    }
                    if (idSet.has(id)) {
                      throw new Error(`Duplicate id found: ${id} in file ${filePath}`);
                    } else {
                      idSet.add(id);
                    }
                  }
                  return addOns.length;
                } catch (err) {
                  throw new Error(`${filePath} failed with error: ${err}.`);
                }
              });
            }
          }
          return 0;
        });
      });
      //collecting all promises
      return Promise.all(promises).then((counters) => counters.reduce((acc, curr) => acc + curr, 0));
    });
  }
}
