# live_mods.md — Live Modification (curveball) answers

> Released once, late in the exam. **Three curveballs: A, D, E.** For EACH, give: the
> announced instruction, the exact command(s) you ran, the **live value(s)** you acted
> on (your PID / stock / timestamp), and the screenshot. An answer that ignores your
> issued value, or that could have been written *before* the announcement, scores zero.

---

## Curveball A — extra worker(s) that start after the others join

- **Issued value:** `2` extra worker(s)
- **Announced instruction:** Edit `thread_demo.c` to spawn this many **extra** workers that start **only after** the originals have joined; show the new LWP(s) appear in the mapping then disappear.
- **Live value(s) I acted on:** base PID = `23312`; new LWP id(s) that appeared = `23398, 23399`
- **Commands:**

```bash
gcc -pthread -o thread_demo thread_demo.c
./thread_demo
# In another terminal, while extra workers sleep (within 10-second window):
ps -eLo pid,lwp,comm | grep thread_demo
```

- **Screenshot:**

![A live — new LWP appears then is gone](partA_threads/images/live_a.png)

---

## Curveball D — per-buyer purchase cap

- **Issued value:** cap = `6`
- **Announced instruction:** Add a **per-buyer purchase cap** to your purchase script (`buy_widget`) — reject any single order above it; re-run `swarm` and show the locked result respects the cap and stays consistent.
- **Live value(s) I acted on:** stock before = `100`; order(s) rejected for exceeding the cap = `Quantity 10 exceeds per-buyer cap of 6`; final stock after swarm = `60`
- **Commands:**

```bash
# Add per-buyer cap validation to buy_widget
# Edit buy_widget to add:
# PER_BUYER_CAP=6
# if [ "$QTY" -gt "$PER_BUYER_CAP" ]; then
#     echo "Error: Order rejected. Quantity $QTY exceeds per-buyer cap of $PER_BUYER_CAP" >&2
#     exit 1
# fi

# Test cap rejection:
echo "100" > stock.txt
> sales.log
./scripts/buy_widget "TestBuyer_Large" 10
# Output: Error: Order rejected. Quantity 10 exceeds per-buyer cap of 6

# Verify rejected order did NOT log:
./scripts/buy_widget "TestBuyer_Small" 3
# Verify stock and log:
cat stock.txt    # Output: 97
cat sales.log    # Output: 1 line (only the 3-unit purchase)

# Run swarm (40 concurrent buyers × 1 unit each):
echo "100" > stock.txt
> sales.log
./scripts/swarm
# Output: Final stock value: 60

# Verify consistency (run again):
echo "100" > stock.txt
> sales.log
./scripts/swarm
# Output: Final stock value: 60
```

- **Screenshot:**

![D live — locked result respects the cap](partD_secure/images/live_d.png)

---

## Curveball E — idempotent timed_job

- **Issued value:** token = `IDEMPMARK`
- **Announced instruction:** Make `timed_job` **idempotent** using this marker token — it must refuse to run if the token for today is already in its log; trigger it twice and prove the 2nd was skipped.
- **Live value(s) I acted on:** today's marker line = `2026-06-30 15:24:05 - IDEMPMARK - Timed job executed successfully by user: se-suon-caro`; 1st trigger = ran, 2nd trigger = skipped
- **Commands:**

```bash
# Add idempotency guard to timed_job
# Check if IDEMPMARK token for today already exists in log
# If yes, skip execution; if no, run and append line with token + date

# Test idempotency:
> logs/test_idempotent.log

# First run (should execute and log)
./scripts/timed_job logs/test_idempotent.log
# Output: (nothing printed, job runs)

# Second run (should skip)
./scripts/timed_job logs/test_idempotent.log
# Output: Job already executed today (IDEMPMARK found for 2026-06-30), skipping.

# Verify log has only 1 entry
cat logs/test_idempotent.log
wc -l logs/test_idempotent.log
# Output: 1 logs/test_idempotent.log
```

- **Screenshot:**

![E live — 2nd run skipped](partE_automation/images/live_e.png)
