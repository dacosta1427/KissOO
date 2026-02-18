#!/usr/bin/env python3
"""
Ralph Loop for Cline CLI Agent (Plan + Todo files) with enhanced feedback.

Features:
- Separate plan and todo files.
- Auto-approve (-y) for autonomous execution.
- Verbose mode to see cline output in real time.
- Optional log file to capture all iterations.
- Checks todo file for completion after each run.
- Default max failures set to 5.
- Added reference to workingRules.md in the prompt.
"""

import argparse
import re
import subprocess
import sys
import time
from pathlib import Path


def count_unchecked_items(todo_path: Path) -> int:
    """Return number of unchecked todo items in the todo file."""
    try:
        content = todo_path.read_text(encoding="utf-8-sig")
    except FileNotFoundError:
        print(f"❌ Todo file not found: {todo_path}")
        sys.exit(1)
    except Exception as e:
        print(f"❌ Error reading {todo_path}: {e}")
        sys.exit(1)

    pattern = re.compile(r"^\s*[-*]\s+\[\s*\]")
    return sum(1 for line in content.splitlines() if pattern.match(line))


def todo_is_complete(todo_path: Path) -> bool:
    return count_unchecked_items(todo_path) == 0


def run_cline(
    prompt: str, verbose: bool = False, raw: bool = False, log_file=None
) -> bool:
    """
    Execute cline with -y flag.
    If raw=True, let cline write directly to terminal (no prefixes).
    If verbose=True and not raw, print lines with [cline] prefix.
    If log_file, append stdout/stderr to that file.
    Returns True if exit code 0.
    """
    cmd = ["cline", "-y", prompt]
    cmd_display = " ".join(str(c) for c in cmd)

    if raw:
        # Direct passthrough – cline inherits our stdout/stderr
        print(f"\n🔧 Running: {cmd_display}")
        try:
            process = subprocess.Popen(cmd)
            process.wait()
            returncode = process.returncode
            if returncode == 0:
                print("✅ cline finished successfully.")
            else:
                print(f"⚠️  cline exited with code {returncode}")
            return returncode == 0
        except Exception as e:
            print(f"❌ Error: {e}")
            return False

    # Otherwise, use the existing line‑by‑line capture (with optional logging)
    if verbose:
        print(f"\n🔧 Running: {cmd_display}")

    try:
        if verbose or log_file:
            process = subprocess.Popen(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                bufsize=1,
            )
            stdout_lines = []
            stderr_lines = []

            for line in process.stdout:
                if verbose:
                    print(f"[cline] {line.rstrip()}")
                if log_file:
                    stdout_lines.append(line)

            for line in process.stderr:
                if verbose:
                    print(f"[cline:err] {line.rstrip()}")
                if log_file:
                    stderr_lines.append(line)

            process.wait()
            returncode = process.returncode

            if log_file:
                with open(log_file, "a", encoding="utf-8") as f:
                    f.write(f"\n--- Iteration at {time.ctime()} ---\n")
                    f.write(f"Command: {cmd_display}\n")
                    f.write("STDOUT:\n")
                    f.writelines(stdout_lines)
                    f.write("\nSTDERR:\n")
                    f.writelines(stderr_lines)
                    f.write(f"\nExit code: {returncode}\n")
        else:
            result = subprocess.run(
                cmd, capture_output=True, text=True, timeout=300, check=False
            )
            returncode = result.returncode
            if result.stdout:
                preview = result.stdout[:200] + (
                    "..." if len(result.stdout) > 200 else ""
                )
                print(f"📤 stdout preview: {preview}")
            if result.stderr:
                preview = result.stderr[:200] + (
                    "..." if len(result.stderr) > 200 else ""
                )
                print(f"⚠️  stderr preview: {preview}")

        if returncode == 0:
            print("✅ cline finished successfully.")
        else:
            print(f"⚠️  cline exited with code {returncode}")
        return returncode == 0

    except subprocess.TimeoutExpired:
        print("⚠️  cline timed out after 5 minutes")
        return False
    except FileNotFoundError:
        print("❌ 'cline' command not found.")
        sys.exit(1)
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False


def main():
    parser = argparse.ArgumentParser(
        description="Ralph loop for Cline with separate plan and todo files."
    )
    parser.add_argument(
        "plan_file",
        type=Path,
        help="Path to the plan Markdown file (context for the agent).",
    )
    parser.add_argument(
        "todo_file",
        type=Path,
        help="Path to the todo Markdown file (contains checkboxes to complete).",
    )
    parser.add_argument(
        "--prompt",
        type=str,
        default="You are working on the plan in {plan}. Please execute the next task(s) from the todo list in {todo}. Update the todo file by marking tasks as complete as you finish them. As per the md/workingRules.md guidelines.",
        help="Prompt template. Use {plan} and {todo} for the filenames.",
    )
    parser.add_argument(
        "--max-failures",
        type=int,
        default=5,  # Changed from 25 to 5
        help="Stop after this many unsuccessful iterations (default: 5).",
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=2.0,
        help="Seconds to wait between cline invocations (default: 2).",
    )
    parser.add_argument(
        "--verbose",
        "-v",
        action="store_true",
        help="Print cline's stdout/stderr in real time.",
    )
    parser.add_argument(
        "--log-file", type=Path, help="Append all cline output to this file."
    )
    parser.add_argument(
        "--debug",
        action="store_true",
        help="Print number of unchecked items before each iteration.",
    )
    parser.add_argument(
        "--raw",
        action="store_true",
        help="Let cline write directly to terminal (no prefixes). Implies --verbose."
    )

    args = parser.parse_args()
    plan_path = args.plan_file
    todo_path = args.todo_file

    # Build the prompt with absolute paths for clarity
    prompt_text = args.prompt.format(plan=plan_path.resolve(), todo=todo_path.resolve())

    failure_count = 0
    iteration = 0

    print(f"🚀 Starting Ralph loop")
    print(f"   Plan file: {plan_path}")
    print(f"   Todo file: {todo_path}")
    print(f"   Prompt: {prompt_text}")
    print(f"   Max failures: {args.max_failures}")
    if args.verbose:
        print("   Verbose mode: ON")
    if args.log_file:
        print(f"   Logging to: {args.log_file}")
    print()

    while failure_count < args.max_failures:
        iteration += 1
        if args.debug:
            unchecked = count_unchecked_items(todo_path)
            print(f"📊 Unchecked items before iteration {iteration}: {unchecked}")

        # Check before running
        if todo_is_complete(todo_path):
            print("✅ Todo list is already complete. Exiting.")
            break

        # --- Task start line ---
        print(f"🔄 Starting task iteration {iteration}...")

        print(f"▶️  Invoking cline...")
        run_cline(prompt_text, verbose=args.verbose, log_file=args.log_file)

        # --- Task end line (success/failure will be determined after checking todo) ---
        # We'll print after checking completion

        time.sleep(args.delay)

        if todo_is_complete(todo_path):
            print(
                f"🏁 Task iteration {iteration} finished: SUCCESS (all todo items complete)."
            )
            print("✅ Todo list is now complete! Success.")
            break
        else:
            failure_count += 1
            print(
                f"🏁 Task iteration {iteration} finished: FAILURE (todo list still incomplete)."
            )
            print(f"⏳ Todo list still incomplete. Failure count: {failure_count}")

    else:
        print(f"\n🛑 Stopped after {args.max_failures} unsuccessful iterations.")
        sys.exit(1)

    print("\n🎉 Ralph loop finished successfully.")


if __name__ == "__main__":
    main()
