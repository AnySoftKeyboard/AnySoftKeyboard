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

  test('sorting correctly', () => {
    const result = sortContributors(listOfContributors);
    assert.deepEqual(result, listOfContributorsSorted);
  });

  test('generate markdown list correctly', () => {
    const result = generateMarkdownList(listOfContributorsSorted);
    assert.equal(
      result,
      `1. [d_user](https://github.com/d_user) (1.1k)
1. [anysoftkeyboard-bot](https://github.com/anysoftkeyboard-bot) (0.73k) 🤖
1. [a_user](https://github.com/a_user) (123)
1. [b_user](https://github.com/b_user) (123)
1. [c_user](https://github.com/c_user) (12)
1. [f_user](https://github.com/f_user) (12)
1. [e_user](https://github.com/e_user) (1)`,
    );
  });

  test('correctly detects bots', () => {
    const withBots: Contributor[] = [
      { login: 'd_user', contributions: 1123 },
      { login: 'anysoftkeyboard-bot', contributions: 1733 },
      { login: 'some[bot]', contributions: 123 },
      { login: '[another[bot]]', contributions: 1 },
    ];
    const result = generateMarkdownList(withBots);
    assert.equal(
      result,
      `1. [d_user](https://github.com/d_user) (1.1k)
1. [anysoftkeyboard-bot](https://github.com/anysoftkeyboard-bot) (1.7k) 🤖
1. [some](https://github.com/some) (0.12k) 🤖
1. [another](https://github.com/another) (1) 🤖`,
    );
  });

  test('contructs markdown file', () => {
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
