import { Command } from 'commander';
import * as fs from 'fs';
import { getGitDiff, parseGitDiff, generateXmlReport } from '../diff_parser';

const program = new Command();

program
    .version('1.0.0')
    .description('A tool to parse git diff for Android string changes and generate an XML report.')
    .option('-o, --output <outputFile>', 'Output file path', 'string_changes.xml')
    .action((options) => {
        const diff = getGitDiff();
        const changedStrings = parseGitDiff(diff);
        const xmlReport = generateXmlReport(changedStrings);
        fs.writeFileSync(options.output, xmlReport, 'utf-8');
        console.log(`XML report generated at ${options.output}`);
    });

program.parse(process.argv);
