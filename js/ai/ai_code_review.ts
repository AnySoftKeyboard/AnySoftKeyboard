import { ChatGoogleGenerativeAI } from '@langchain/google-genai';
import { HumanMessage, SystemMessage } from '@langchain/core/messages';
import { createReadMultipleFilesTool } from './mcp_read_multiple_files.js';

const _PROMPT = `
You are a senior software engineer with extensive experience in Java, Kotlin, and TypeScript. You have in-depth knowledge of the codebase in the repository.
You can read additional guidelines in the @AGENTS.md file.

Your task is to review a code change. You will be given a git diff.

## Available tools:
You have a tool you can use for research:
- read_multiple_files - Use this tool to read the full content of changed files when you need more context


## Review Process:
1. First, read the entire content of any changed files using the read_multiple_files tool
2. Analyze the diff in context of the full files
3. Provide a comprehensive review

## Review Guidelines:
- Focus on functionality, logic, and potential issues
- Do not nitpick on minor style issues
- Do not report on code formatting or style preferences
- Identify the severity of each feedback point using these categories:
  - **Critical**: Bugs, security issues, performance problems, typos, grammar errors
  - **Moderate**: Mismatched patterns, missing required comments, architectural concerns
  - **Could-be-fixed**: Class/function naming issues, minor improvements
  - **Nitpicking**: Variable naming, formatting preferences (avoid these)

## Security Considerations:
- Look for potential security vulnerabilities
- Check for proper input validation
- Verify error handling doesn't expose sensitive information
- Ensure proper authentication/authorization patterns

## Report Format:
- Use markdown format
- Start with a brief summary of the changes
- Organize feedback by severity level
- Provide specific, actionable suggestions
- Include code examples when helpful
- End with an overall assessment
`;
export class AiCodeReviewer {
  private model: ChatGoogleGenerativeAI;

  constructor(apiKey: string) {
    if (!apiKey || apiKey.trim().length === 0) {
      throw new Error('API key cannot be empty');
    }

    this.model = new ChatGoogleGenerativeAI({
      model: 'gemini-2.5-pro',
      apiKey: apiKey,
    });
  }

  async review(diff: string): Promise<string> {
    if (!diff || diff.trim().length === 0) {
      throw new Error('Code diff content cannot be empty');
    }

    try {
      const modelWithTools = this.model.bindTools([createReadMultipleFilesTool()]);

      const response = await modelWithTools.invoke([new SystemMessage(_PROMPT), new HumanMessage(diff)]);

      response.content;
      // Handle the structured response content
      if (typeof response.content === 'string') {
        return response.content;
      } else if (Array.isArray(response.content)) {
        // Extract text from array of MessageContentComplex
        const textBlocks = response.content.filter(
          (block) => typeof block === 'object' && block !== null && 'type' in block && block.type === 'text',
        );
        return textBlocks.map((block) => (block as { text: string }).text).join('');
      } else {
        // Fallback for other response types
        return JSON.stringify(response.content);
      }
    } catch (error) {
      console.error('Error calling Gemini API:', error);
      throw error;
    }
  }
}
