import { getOctokit } from '@actions/github';

export interface Contributor {
  login: string;
  contributions: number;
}

/* eslint-disable @typescript-eslint/no-explicit-any*/
export async function getContributors(token: string): Promise<Contributor[]> {
  const octokit = getOctokit(token);
  const data = octokit
    .paginate('GET /repos/{owner}/{repo}/contributors?anon=0', {
      owner: 'AnySoftKeyboard',
      repo: 'AnySoftKeyboard',
      per_page: 100,
      headers: {
        'X-GitHub-Api-Version': '2022-11-28',
      },
    })
    .then((contributors: any[]) => {
      return contributors.map(function (c: any): Contributor {
        return { login: c.login, contributions: c.contributions };
      });
    });

  return data;
}
/* eslint-enable*/

export function sortContributors(contributors: Contributor[]): Contributor[] {
  return contributors.sort(function (c1, c2) {
    const contributionsDiff = c2.contributions - c1.contributions;
    if (contributionsDiff !== 0) {
      return contributionsDiff;
    } else {
      return c1.login.localeCompare(c2.login);
    }
  });
}

function isBot(login: string): boolean {
  switch (login.toLocaleLowerCase()) {
    case 'anysoftkeyboard-bot':
    case '[dependabot[bot]]':
    case 'google-labs-jules':
      return true;
    default:
      return false;
  }
}

export function generateMarkdownList(contributors: Contributor[]): string {
  const simpleNumber = (c: Contributor): string => {
    if (isBot(c.login)) {
      return `${(c.contributions / 1000.0).toFixed(1)}k`;
    } else {
      if (c.contributions > 999) {
        return `${(c.contributions / 1000.0).toFixed(1)}k`;
      } else if (c.contributions > 499) {
        return `${(c.contributions / 1000.0).toFixed(2)}k`;
      } else {
        return `${c.contributions.toFixed(0)}`;
      }
    }
  };

  return contributors
    .map((c) => {
      return `1. [${c.login}](https://github.com/${c.login}) (${simpleNumber(c)})${
        isBot(c.login) ? ' \uD83E\uDD16' : ''
      }`;
    })
    .join('\n');
}

export function constructContributorsContext(markdownTable: string): string {
  return `# Contributors\n\nThank you for the fine contributors:\n\n${markdownTable}\n`;
}
