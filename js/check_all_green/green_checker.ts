'use strict';

export interface JobResult {
  result: string;
}

export interface NeedsContext {
  // The needs context is an object where keys are job names
  [jobName: string]: JobResult;
}

export function checkJobStatuses(needsContextJson: string): boolean {
  try {
    const needsContext: NeedsContext = JSON.parse(needsContextJson);

    const jobNames = Object.keys(needsContext);
    const nonSuccessfulJobs: string[] = [];

    console.log('Inspecting job results...');

    for (const jobName of jobNames) {
      const result = needsContext[jobName].result;
      console.log(`  - Job '${jobName}' status: ${result}`);

      // Check if the result is NOT 'success'
      if (result !== 'success') {
        nonSuccessfulJobs.push(`${jobName} (${result})`);
      }
    }

    // --- Outcome ---
    if (nonSuccessfulJobs.length > 0) {
      console.error('\n❌ Failure: One or more required jobs did not succeed:');
      nonSuccessfulJobs.forEach((jobInfo) => console.error(`  - ${jobInfo}`));
      return false;
    } else {
      console.log('\n✅ Success: All required jobs succeeded.');
      return true;
    }
  } catch (e) {
    console.error(`\n❌ Failure: could not run logic on: ${e}. Input:\n${needsContextJson}`);
    return false;
  }
}
