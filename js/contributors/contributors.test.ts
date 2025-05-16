import test from 'node:test';
import assert from 'node:assert';
import { Contributor, constructContributorsContext, generateMarkdownList, sortContributors } from './contributors.js';

test.describe('Contributors', () => {
  const listOfContributors: Contributor[] = [
    { login: 'a_user', contributions: 123 },
    { login: 'b_user', contributions: 123 },
    { login: 'f_user', contributions: 12 },
    { login: 'd_user', contributions: 1123 },
    { login: 'anysoftkeyboard-bot', contributions: 733 },
    { login: 'e_user', contributions: 1 },
    { login: 'c_user', contributions: 12 },
  ];
  const listOfContributorsSorted: Contributor[] = [
    { login: 'd_user', contributions: 1123 },
    { login: 'anysoftkeyboard-bot', contributions: 733 },
    { login: 'a_user', contributions: 123 },
    { login: 'b_user', contributions: 123 },
    { login: 'c_user', contributions: 12 },
    { login: 'f_user', contributions: 12 },
    { login: 'e_user', contributions: 1 },
  ];

  test.describe('sorting correctly', () => {
    const result = sortContributors(listOfContributors);
    assert.deepEqual(result, listOfContributorsSorted);
  });

  test.describe('generate markdown list correctly', () => {
    const result = generateMarkdownList(listOfContributorsSorted);
    assert.equal(
      result,
      `1. [d_user](https://github.com/d_user) (1.1k)
1. [anysoftkeyboard-bot](https://github.com/anysoftkeyboard-bot) (0.7k) 🤖
1. [a_user](https://github.com/a_user) (123)
1. [b_user](https://github.com/b_user) (123)
1. [c_user](https://github.com/c_user) (12)
1. [f_user](https://github.com/f_user) (12)
1. [e_user](https://github.com/e_user) (1)`,
    );
  });

  test.describe('contructs markdown file', () => {
    const result = constructContributorsContext('1.line1\n1.line2');
    assert.equal(
      result,
      `# Contributors

Thank you for the fine contributors:

1.line1
1.line2
`,
    );
  });
});
