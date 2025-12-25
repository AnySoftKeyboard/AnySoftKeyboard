# GitHub PR State Monitor Agent

Monitor GitHub PR checks continuously and report detailed status updates.

## Purpose

Continuously monitor a GitHub Pull Request's CI/CD checks at regular intervals and provide comprehensive reports on check status. This agent runs in the background, tracking PR state changes and reporting failures with detailed diagnostic information or success when all checks pass.

## Usage

```
Monitor GitHub PR checks and report status
```

### Parameters

When spawning this agent, provide these details in the prompt:

- **PR number** or **PR URL**: The pull request to monitor (e.g., 4583 or https://github.com/owner/repo/pull/4583)
- **Repository**: Repository in format "owner/repo" (optional, will detect from git remote or PR URL)
- **Check interval**: Time between checks in seconds (default: 60)
- **Max wait time**: Maximum monitoring duration in minutes (default: 40)

### Example Prompts

```
Monitor PR #4583 in AnySoftKeyboard/AnySoftKeyboard repository.
Check every 60 seconds, maximum 40 minutes.
Report the first failure with details.
```

```
Monitor https://github.com/AnySoftKeyboard/AnySoftKeyboard/pull/4583
Check interval: 30 seconds
Max duration: 20 minutes
```

```
Monitor PR 4583. Use defaults (60s interval, 40min max).
```

## Behavior

The agent will:

1. **Start Monitoring**: Begin checking PR status at specified intervals using `gh pr checks`
2. **Track All Checks**: Monitor all GitHub Actions workflows and required status checks
3. **Wait for Completion**: Continue checking until checks complete or timeout is reached
4. **Report First Failure**: On detecting a failure, immediately extract and report:
   - Workflow file name (e.g., `.github/workflows/checks.yml`)
   - Job name (e.g., `tests (app)`)
   - Step name (if identifiable from logs)
   - Detailed error message with context (5 lines before, 20 lines after failure)
   - Direct URL to the failed job
5. **Report Success**: If all checks pass, report successful completion
6. **Handle Timeout**: If max duration reached, report current status

## Output Format

### On Failure

```
❌ PR Check Failure Detected

Repository: AnySoftKeyboard/AnySoftKeyboard
PR: #4583

Workflow: .github/workflows/checks.yml
Job: tests (app)
Run: https://github.com/owner/repo/actions/runs/12345/job/67890

Error Details:
---
AnySoftKeyboardGimmicksTest > testVerifyVariousPunctuationsSwapping FAILED
    org.junit.ComparisonFailure: expected:<hello. hello, hello[: ]> but was:<hello. hello, hello[ :]>
        at org.junit.Assert.assertEquals(Assert.java:117)
        at com.anysoftkeyboard.AnySoftKeyboardGimmicksTest.testVerifyVariousPunctuationsSwapping(AnySoftKeyboardGimmicksTest.java:256)
---

Test File: AnySoftKeyboardGimmicksTest.java:256
```

### On Success

```
✅ All PR checks passed successfully!

Repository: AnySoftKeyboard/AnySoftKeyboard
PR: #4583
Total checks: 18
Duration: 12 minutes 34 seconds
```

### On Timeout

```
⏱️  PR monitoring timed out after 40 minutes.

Repository: AnySoftKeyboard/AnySoftKeyboard
PR: #4583

Current status:
- ✅ 15 checks passed
- ⏳ 2 checks pending
- 🔄 1 check in progress
```

## Implementation Details

### Commands Used

```bash
# Check PR status - primary monitoring command
gh pr checks <pr-number> --repo <owner/repo>

# Get detailed check information with JSON output
gh pr checks <pr-number> --repo <owner/repo> --json name,status,conclusion,detailsUrl,link

# Get job logs for failed check
gh api repos/<owner>/<repo>/actions/jobs/<job-id>/logs

# Extract workflow file from job metadata
gh api repos/<owner>/<repo>/actions/runs/<run-id>
```

### Log Parsing Strategy

1. Search logs for keywords: `FAILED`, `ERROR`, `FAILURE`, `BUILD FAILED`
2. Extract context: 5 lines before, 20 lines after the failure
3. Parse test class and line numbers from stack traces
4. Remove ANSI escape codes and timestamps for readability

### Rate Limiting

- Respects GitHub API rate limits
- Uses exponential backoff on rate limit errors
- Typical usage: ~2 API calls per check interval

### Monitoring Algorithm

```
1. START:
   - Record start_time = current_time
   - Parse PR number and repository
   - Validate gh authentication

2. LOOP (every check_interval seconds):
   - Calculate elapsed_time = current_time - start_time

   - IF elapsed_time > max_wait_time:
       GOTO TIMEOUT

   - Fetch PR checks with: gh pr checks <pr> --json
   - Parse check states into: passed[], pending[], in_progress[], failed[]

   - Log progress update with counts and elapsed time

   - IF failed[] is not empty:
       GOTO FAILURE

   - IF pending[] is empty AND in_progress[] is empty:
       GOTO SUCCESS

   - Sleep for check_interval seconds
   - CONTINUE LOOP

3. SUCCESS:
   - Report all checks passed
   - Include: total checks, duration
   - EXIT with success

4. FAILURE:
   - For first failed check:
     * Extract check name, detailsUrl
     * Parse workflow path from run metadata
     * Fetch job logs
     * Parse error context
     * Extract job name, step name, error message
   - Format comprehensive failure report
   - EXIT with failure details

5. TIMEOUT:
   - Report current status snapshot
   - Include: passed count, pending count, in_progress count
   - Provide PR URL for manual inspection
   - EXIT with timeout status
```

## Error Handling

- **Rate Limiting**: Backs off exponentially, reports if rate limited
- **Network Issues**: Retries failed API calls up to 3 times with 5s delay
- **Invalid PR**: Immediately reports error if PR number doesn't exist
- **No Access**: Reports error if lacking permissions to view checks or logs
- **Partial Logs**: If logs truncated, reports with available information

## Dependencies

- **Required**: `gh` CLI installed and authenticated (`gh auth status`)
- **Permissions**: Read access to repository, Actions, and PR checks
- **Network**: Internet connection to access GitHub API

## Notes

- Only reports the FIRST failure detected (stops monitoring after first failure)
- Designed to run asynchronously in the background
- Low resource usage: sleeps between checks
- Provides clickable URLs for easy navigation to GitHub
