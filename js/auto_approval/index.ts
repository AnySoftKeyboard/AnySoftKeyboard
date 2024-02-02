import { context } from '@actions/github';
import { getActionInputs, shouldApprove, approvePr } from './approval.js';
import { setFailed, getInput } from '@actions/core';

const main = async () => {
  const actionInputs = getActionInputs(getInput, context.payload);
  if (shouldApprove(actionInputs)) {
    approvePr(actionInputs.token);
  }
};

main().catch((err) => setFailed(err.message));
