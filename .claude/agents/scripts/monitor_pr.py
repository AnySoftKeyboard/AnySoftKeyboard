import subprocess
import json
import time
import sys
import re
import argparse

def run_command(command):
    try:
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        return None

def get_job_logs(repo, job_id, retries=3):
    cmd = f"gh api repos/{repo}/actions/jobs/{job_id}/logs"
    for i in range(retries):
        logs = run_command(cmd)
        if logs:
            return logs
        time.sleep(2)
    return None

def parse_failure_context(logs):
    if not logs:
        return "No logs available."
    
    lines = logs.split('\n')
    # Common failure markers
    failure_indices = [i for i, line in enumerate(lines) if "FAILED" in line or "ERROR" in line or "FAILURE" in line or "BUILD FAILED" in line]
    
    if not failure_indices:
        # Fallback: return last 50 lines if no explicit failure tag found
        return "\n".join(lines[-50:])
    
    # Take the first significant failure, but try to find one that isn't just a summary line
    idx = failure_indices[0]
    
    start = max(0, idx - 10)
    end = min(len(lines), idx + 25)
    
    return "\n".join(lines[start:end])

def monitor_pr(pr_number, repo, interval=60, timeout_mins=40):
    print(f"Starting monitoring for PR #{pr_number} in {repo}")
    print(f"Configuration: interval={interval}s, timeout={timeout_mins}m")
    
    start_time = time.time()
    timeout_seconds = timeout_mins * 60
    
    # Track previous status to avoid spamming same output
    last_pending_count = -1

    while True:
        elapsed = time.time() - start_time
        if elapsed > timeout_seconds:
            print("\n⏱️ PR monitoring timed out.")
            return

        # Fetch checks
        cmd = f"gh pr checks {pr_number} --repo {repo} --json name,state,link,workflow,conclusion"
        output = run_command(cmd)
        
        if not output:
             # Transient network error or GH API issue
             time.sleep(10)
             continue

        try:
            checks = json.loads(output)
        except json.JSONDecodeError:
            print("Error decoding JSON from GitHub CLI")
            time.sleep(10)
            continue

        failed = []
        pending = []
        in_progress = []
        success = []

        for check in checks:
            # gh pr checks uses 'state' (PENDING, SUCCESS, FAILURE, etc)
            # Some versions might rely on 'conclusion' for finished jobs. 
            state = check.get('state', 'UNKNOWN')
            
            # Map states to logical buckets
            if state in ['FAILURE', 'TIMED_OUT', 'CANCELLED', 'ACTION_REQUIRED', 'STARTUP_FAILURE']:
                failed.append(check)
            elif state in ['IN_PROGRESS', 'QUEUED', 'WAITING']:
                in_progress.append(check)
            elif state == 'PENDING':
                pending.append(check)
            elif state in ['SUCCESS', 'SKIPPED', 'NEUTRAL']:
                success.append(check)
        
        # 1. Handle Failures IMMEDIATELY
        if failed:
            print("\n❌ PR Check Failure Detected\n")
            first_fail = failed[0]
            print(f"Workflow: {first_fail.get('workflow', 'Unknown')}")
            print(f"Job: {first_fail.get('name', 'Unknown')}")
            print(f"Status: {first_fail.get('state')}")
            print(f"Run: {first_fail.get('link')}")
            
            # Extract job ID to get logs
            # Link format example: https://github.com/owner/repo/actions/runs/123/job/456
            link = first_fail.get('link', '')
            match = re.search(r'job/(\d+)', link)
            
            if match:
                job_id = match.group(1)
                print("Fetching logs...")
                logs = get_job_logs(repo, job_id)
                print("\nError Details:")
                print("---")
                print(parse_failure_context(logs))
                print("---")
            else:
                print("Could not extract Job ID for logs.")
                
            sys.exit(1) # Return non-zero status

        # 2. Handle Success
        if not pending and not in_progress:
            print(f"\n✅ All {len(checks)} PR checks passed successfully!")
            print(f"Duration: {int(elapsed // 60)}m {int(elapsed % 60)}s")
            sys.exit(0)

        # 3. Heartbeat / Status Update
        total_running = len(pending) + len(in_progress)
        if total_running != last_pending_count:
            # State changed, print update
            print(f"Status: {len(success)} passed, {total_running} remaining ({len(in_progress)} running)...")
            last_pending_count = total_running
        
        # Check every interval
        time.sleep(interval)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Monitor GitHub PR Checks')
    parser.add_argument('pr', help='PR Number')
    parser.add_argument('repo', help='Repository (owner/repo)')
    parser.add_argument('--interval', type=int, default=60, help='Check interval in seconds')
    parser.add_argument('--timeout', type=int, default=40, help='Timeout in minutes')
    
    args = parser.parse_args()
    
    monitor_pr(args.pr, args.repo, args.interval, args.timeout)
