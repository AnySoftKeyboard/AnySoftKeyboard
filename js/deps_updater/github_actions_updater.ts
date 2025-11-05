import fs from 'fs';
import path from 'path';
import { getLatestGitHubRelease } from './latest_version_locator.js';

const GITHUB_ACTION_REGEX = /uses:\s*([\w-]+\/[\w-]+)@([\w\.-]+)/g;

export const update_gh_actions = async (
  workflowFolder: string,
  gh_user: string,
  gh_token: string,
  latestVersionGetter: typeof getLatestGitHubRelease = getLatestGitHubRelease,
): Promise<void> => {
  const actions = new Set<string>();
  const files = fs.globSync(path.join(workflowFolder, '*.yml'), {
    withFileTypes: false,
  });

  const readPromises = files
    .map((fileName) => fs.promises.readFile(fileName, 'utf-8'))
    .map((p) => p.then((content) => content.matchAll(GITHUB_ACTION_REGEX)))
    .map((p) =>
      p.then((matches) => {
        for (const match of matches) {
          if (match && match[1]) {
            actions.add(match[1]);
          }
        }
      }),
    );

  await Promise.all(readPromises);
  console.log(`Found ${actions.size} unique GitHub Actions.`);

  const actionVersions: Record<string, string> = {};
  const promises = Array.from(actions).map((action) =>
    latestVersionGetter(gh_user, gh_token, action).then((version) => {
      if (version) {
        actionVersions[action] = version;
        console.log(` - ${action}: latest version ${version}`);
      }
    }),
  );

  // Wait for all promises to resolve
  await Promise.all(promises);

  // Writing new versions
  const writePromises = files
    .map((fileName) => {
      return { fileName, content: fs.readFileSync(fileName, 'utf-8') };
    })
    .map((data) => {
      let newContent = data.content;
      for (const [action, version] of Object.entries(actionVersions)) {
        // Replace any version for this action
        newContent = newContent.replace(
          new RegExp(`uses:\\s+${action.replace('/', '\\/')}@[v]?[\\d.]+`, 'g'),
          `uses: ${action}@${version}`,
        );
      }
      return { fileName: data.fileName, content: newContent };
    })
    .map((data) =>
      fs.promises.writeFile(data.fileName, data.content, {
        encoding: 'utf-8',
        flag: 'w',
      }),
    );

  await Promise.all(writePromises);
};
