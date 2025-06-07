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
  const simpleUserName = login.toLocaleLowerCase();
  if (simpleUserName.endsWith('[bot]') || simpleUserName.endsWith('[bot]]')) return true;

  switch (login.toLocaleLowerCase()) {
    case 'anysoftkeyboard-bot':
      return true;
    default:
      return false;
  }
}

export function generateMarkdownList(contributors: Contributor[]): string {
  const simpleNumber = (c: Contributor): string => {
    if (isBot(c.login)) {
      // a bit different counting
      if (c.contributions >= 2000) {
        return `${Math.floor(c.contributions / 1000)}k`;
      } else if (c.contributions >= 100) {
        return `${(c.contributions / 1000.0).toFixed(1)}k`;
      } else {
        return `${c.contributions.toFixed(0)}`;
      }
    } else {
      if (c.contributions >= 1000) {
        return `${(c.contributions / 1000.0).toFixed(1)}k`;
      } else if (c.contributions >= 500) {
        return `${(c.contributions / 1000.0).toFixed(2)}k`;
      } else {
        return `${c.contributions.toFixed(0)}`;
      }
    }
  };

  const simpleLogin = (c: Contributor): string => {
    return c.login
      .replace(/\[bot]/, '')
      .replace(/^\[/, '')
      .replace(/\]$/, '');
  };

  return contributors
    .map((c) => {
      return { c, user: simpleLogin(c), contributions: simpleNumber(c) };
    })
    .map((c) => {
      return { ...c, postFix: isBot(c.c.login) ? ' 🤖' : '' };
    })
    .map((c) => `1. [${c.user}](https://github.com/${c.user}) (${c.contributions})${c.postFix}`)
    .join('\n');
}

export function constructContributorsContext(markdownTable: string): string {
  return `# Contributors\n\nThank you for the fine contributors:\n\n${markdownTable}\n`;
}
