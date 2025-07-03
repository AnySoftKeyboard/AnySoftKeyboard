import { describe, it, beforeEach, afterEach } from 'node:test';
import assert from 'node:assert';
import fs from 'fs';
import path from 'path';
import os from 'os';
import { update_gh_actions } from './github_actions_updater.js';
import { FetcherFunctionType, getLatestGitHubRelease } from './latest_version_locator.js';

const getLatestGitHubReleaseFake: typeof getLatestGitHubRelease = async (
  user: string,
  token: string,
  action: string,
  _?: FetcherFunctionType,
): Promise<string> => {
  switch (action) {
    case 'actions/checkout':
      return 'v4';
    case 'actions/setup-node':
      return 'v4.0.1';
    case 'a-user/an-action-123':
      return 'v1.2.3';
    default:
      throw new Error(`Could not find latest version for ${action}`);
  }
};

describe('update_gh_actions', () => {
  let tmpDir: string;

  beforeEach(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'gh-actions-test-'));
  });

  afterEach(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });

  const createWorkflowFile = (fileName: string, content: string) => {
    const filePath = path.join(tmpDir, fileName);
    fs.writeFileSync(filePath, content, 'utf-8');
    return filePath;
  };

  it('should update multiple actions in a single file (Core functionality)', async () => {
    const workflowContent = `
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3.8.1
`;
    createWorkflowFile('workflow.yml', workflowContent);

    await update_gh_actions(tmpDir, 'user', 'token', getLatestGitHubReleaseFake);

    const updatedContent = fs.readFileSync(path.join(tmpDir, 'workflow.yml'), 'utf-8');
    assert.ok(updatedContent.includes('uses: actions/checkout@v4'));
    assert.ok(updatedContent.includes('uses: actions/setup-node@v4.0.1'));
  });

  it('should not update if action is already at the latest version', async () => {
    const workflowContent = `
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
`;
    createWorkflowFile('workflow.yml', workflowContent);

    await update_gh_actions(tmpDir, 'user', 'token', getLatestGitHubReleaseFake);

    const updatedContent = fs.readFileSync(path.join(tmpDir, 'workflow.yml'), 'utf-8');
    assert.ok(updatedContent.includes('uses: actions/checkout@v4'));
  });

  it('should not update if there are no "uses" statements', async () => {
    const workflowContent = `
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: echo "Hello World"
`;
    createWorkflowFile('workflow.yml', workflowContent);

    await update_gh_actions(tmpDir, 'user', 'token', getLatestGitHubReleaseFake);

    const updatedContent = fs.readFileSync(path.join(tmpDir, 'workflow.yml'), 'utf-8');
    assert.ok(updatedContent.includes('run: echo "Hello World"'));
  });

  it('should not update if "uses" points to a local file path', async () => {
    const workflowContent = `
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: ./.github/actions/my-local-action
`;
    createWorkflowFile('workflow.yml', workflowContent);

    await update_gh_actions(tmpDir, 'user', 'token', getLatestGitHubReleaseFake);

    const updatedContent = fs.readFileSync(path.join(tmpDir, 'workflow.yml'), 'utf-8');
    assert.ok(updatedContent.includes('uses: ./.github/actions/my-local-action'));
  });
});
