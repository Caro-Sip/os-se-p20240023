# Class Activity 5 - Semaphores: Particle Pairs and HELLO Ordering

- **Student Name:** Suon Caro
- **Student ID:** p20240023
- **Programming Language Used:** Java
- **Date:** 04 June 2026

---

## Overview

This activity implements two semaphore synchronization problems in Java:

1. **Task 1**: A particle pair buffer protected by semaphores with multiple producers and one consumer.
2. **Task 2**: Three concurrent processes printing "HELLO" in the correct order using semaphores.

Both tasks include "before" and "after" versions to demonstrate why semaphores are essential.

---

## How to Compile and Run

### Prerequisites
- Java 8 or later
- Command line access (Linux, macOS, WSL, or Windows with PowerShell/CMD)

### Compilation

From the `activity5` directory, compile all programs:

```bash
# Compile Task 1 Before
javac task1_particles/ParticlesBefore.java

# Compile Task 1 After
javac task1_particles/ParticlesAfter.java

# Compile Task 2 Before
javac task2_hello/HelloBefore.java

# Compile Task 2 After
javac task2_hello/HelloAfter.java
```

Or compile all at once:

```bash
javac task1_particles/*.java task2_hello/*.java
```

### Running the Programs

#### Task 1A: Particle Buffer BEFORE Semaphores

```bash
java -cp task1_particles ParticlesBefore
```

**Expected behavior:**
- Multiple producer threads add particle pairs to the buffer
- One consumer thread removes particles
- Without proper synchronization, you should see:
  - Race conditions where particles get misaligned
  - Possible buffer overflow or underflow
  - Error messages like "The producing machine is broken" or "Pairs are incorrect"

**To stop:** Press `Ctrl+C`

#### Task 1B: Particle Buffer AFTER Semaphores

```bash
java -cp task1_particles ParticlesAfter
```

**Expected behavior:**
- 3 producers and 1 consumer running safely
- Output shows: `Produced pairs: X | Packaged pairs: Y | Buffer particles: Z`
- Should run indefinitely without errors
- Produced count always ≥ Packaged count
- Buffer size stays between 0 and 100 particles

**To stop:** Press `Ctrl+C`

#### Task 2A: HELLO BEFORE Semaphores

```bash
java -cp task2_hello HelloBefore
```

**Expected behavior:**
- 5 iterations of concurrent letter printing
- Output is unpredictable — order varies each iteration
- You might see: `HELLO`, `LEHLO`, `ELLHO`, `HLELO`, etc.
- Demonstrates why ordering control is needed

#### Task 2B: HELLO AFTER Semaphores

```bash
java -cp task2_hello HelloAfter
```

**Expected behavior:**
- 3 iterations of properly coordinated printing
- Output: `HELLO` in every iteration
- Demonstrates perfect synchronization via semaphores
- Program exits cleanly after completion

---

## Task 1A: Particle Pair Buffer Before Semaphores

### Design

- **Buffer**: `ArrayList<Particle>` with max 100 particles (50 pairs)
- **Particle Format**: `M<machineId>-<pairId>-<type>` (e.g., `M2-17-P1`)
- **Producers**: 3 threads, each creating particle pairs
- **Consumer**: 1 thread, removing particles in pairs
- **Synchronization**: NONE (intentionally broken)

### Why It Fails

Without semaphores, multiple race conditions occur:

1. **Buffer Overflow**: Two producers might both check `buffer.size() < BUFFER_SIZE` before either adds particles, causing overflow.
2. **Pair Misalignment**: Producer A adds `M1-5-P1`, then consumer removes it. Producer B adds `M2-6-P1` and `M2-6-P2`. Consumer gets `M2-6-P1` and `M1-5-P2` — not from the same pair!
3. **Underflow**: Consumer checks if buffer has 2 particles, but before it removes them, another consumer thread (in a multi-consumer scenario) removes one.

### Output Example

```
Produced pairs: 0 | Packaged pairs: 0 | Buffer particles: 0
Produced pairs: 1 | Packaged pairs: 0 | Buffer particles: 2
Produced pairs: 2 | Packaged pairs: 0 | Buffer particles: 4
Produced pairs: 2 | Packaged pairs: 1 | Buffer particles: 2
Pairs are incorrect
```

---

## Task 1B: Particle Pair Buffer After Semaphores

### Design

**Semaphores Used:**

| Semaphore | Initial Value | Purpose |
|-----------|---------------|---------|
| `emptyPairs` | 50 | Counts available pair slots (each pair = 2 particles) |
| `fullPairs` | 0 | Counts complete pairs ready to package |
| `mutex` | 1 | Mutex protecting shared buffer |

### Producer Algorithm

```
loop forever:
    wait(emptyPairs)              // Block if no space for a pair
    wait(mutex)                   // Enter critical section
        create particle pair (P1, P2)
        add P1 and P2 to buffer consecutively
        increment produced counter
    signal(mutex)                 // Exit critical section
    signal(fullPairs)             // Announce pair available
```

### Consumer Algorithm

```
loop forever:
    wait(fullPairs)               // Block if no complete pairs
    wait(mutex)                   // Enter critical section
        remove P1 and P2 from buffer
        verify P1 and P2 are from same pair
        if mismatch: throw error "Pairs are incorrect"
        increment packaged counter
    signal(mutex)                 // Exit critical section
    signal(emptyPairs)            // Announce space available
```

### Key Invariants

1. **Buffer never overflows**: `emptyPairs` prevents adding when full
2. **Pairs stay together**: `mutex` ensures atomic add/remove of both particles
3. **Pairs correctly identified**: Format `M<id>-<pairId>-<type>` lets us verify machine and pair ID match
4. **No underflow**: `fullPairs` prevents removal when buffer is empty

### Output Example

```
=== Particle Pair Buffer WITH Semaphores ===
Producers: 3
Buffer capacity: 100 particles (50 pairs)
Semaphores: empty_pairs=50, full_pairs=0, mutex=1

Produced pairs: 1 | Packaged pairs: 0 | Buffer particles: 2
Produced pairs: 2 | Packaged pairs: 0 | Buffer particles: 4
Produced pairs: 3 | Packaged pairs: 1 | Buffer particles: 4
Produced pairs: 4 | Packaged pairs: 2 | Buffer particles: 4
Produced pairs: 5 | Packaged pairs: 3 | Buffer particles: 4
...
```

### Running Until Error (If Any)

The program should run indefinitely without errors. If you see an error message, the semaphore logic has a bug:

- `The producing machine is broken` → Semaphore logic failed to prevent overflow
- `The packaging machine is broken` → Semaphore logic failed to prevent underflow
- `Pairs are incorrect` → Particles from different pairs got mixed (unlikely with correct mutex)

---

## Task 2A: HELLO Before Semaphores

### Design

- **Process 1**: Prints `H`, then `E`
- **Process 2**: Prints first `L`
- **Process 3**: Prints second `L`, then `O`
- **Synchronization**: CyclicBarrier only (threads start simultaneously, nothing controls order)

### Why Output is Wrong

Without ordering semaphores, all three processes race to print. Possible outputs:

- `LEHLO` — Process 2 prints L before Process 1 finishes E
- `HELLOL` — Actually impossible here, but shows the chaos
- `ELLHO` — Process 1 only prints E before others start
- Sometimes `HELLO` by pure chance

The race condition occurs because:
1. All three threads start simultaneously (via CyclicBarrier)
2. Each thread has independent sleep/no-sleep timing
3. Scheduler can context-switch at any point
4. No semaphores prevent L from printing before E

### Output Example

```
=== HELLO BEFORE Semaphores ===
Running 5 iterations to show unpredictable ordering...

Iteration 1: LEHLO
Iteration 2: HELLO
Iteration 3: LELOH
Iteration 4: LHELO
Iteration 5: HELLO

Notice: The order is unpredictable! Sometimes you might see:
  HELLO (correct by chance)
  LEHLO (L printed first due to race condition)
  ELLHO (E before L due to timing)
  ...and other permutations
```

---

## Task 2B: HELLO After Semaphores

### Design

**Semaphores Used:**

| Semaphore | Initial Value | Meaning |
|-----------|---------------|---------|
| `startH` | 1 | Allows Process 1 to print H first |
| `afterH` | 0 | Signals when H is done, allows Process 2 to prepare |
| `afterE` | 0 | Signals when E is done, allows Process 2 to print L |
| `afterL1` | 0 | Signals when first L is done, allows Process 3 to print |
| `afterL2` | 0 | Signals when second L is done (for potential extensions) |

### Ordering Algorithm

**Process 1:**
```
wait(startH)           // Initially available
print H
signal(afterH)
print E
signal(afterE)         // Now Process 2 can proceed
```

**Process 2:**
```
wait(afterE)           // Blocked until E is printed
print L
signal(afterL1)        // Now Process 3 can proceed
```

**Process 3:**
```
wait(afterL1)          // Blocked until first L is printed
print L
signal(afterL2)
print O
```

### Key Points

- **Correct Order**: H → E → L → L → O is enforced by semaphore chain
- **No Deadlock**: Each process signals the next one in the chain
- **No Busy Waiting**: Threads block on semaphores, not spinning
- **Deterministic**: Always produces exactly "HELLO"

### Output Example

```
=== HELLO AFTER Semaphores ===

Expected output: HELLO
Running 3 iterations with proper semaphore ordering...

Iteration 1: HELLO
Iteration 2: HELLO
Iteration 3: HELLO

✓ SUCCESS: All iterations printed HELLO in the correct order!
```

---

## Answers to Questions

### 1. In Task 1, why does a producer need to wait before adding a pair to the buffer?

**Answer:**
A producer must wait on the `emptyPairs` semaphore to ensure there is space in the buffer for a new pair (2 consecutive particles). Without this wait:
- Multiple producers could simultaneously check if space is available
- Both might find space and add pairs, exceeding the 100-particle limit (buffer overflow)
- The `emptyPairs` semaphore (initialized to 50) ensures at most 50 pairs are in the buffer
- When `emptyPairs` reaches 0, producers block until the consumer removes pairs and calls `signal(emptyPairs)`

**Semaphore**: `emptyPairs`

---

### 2. In Task 1, why does the consumer need to wait before removing a pair from the buffer?

**Answer:**
The consumer must wait on the `fullPairs` semaphore to ensure that a complete pair is available in the buffer before attempting to remove particles. Without this wait:
- The consumer might remove a single particle (if not carefully guarded), breaking pair integrity
- Multiple consumer threads (if present) could race and try to remove particles simultaneously
- The `fullPairs` semaphore (initialized to 0) ensures particles only exist in complete pairs
- It increments each time a producer adds a pair and decrements each time the consumer removes a pair
- When `fullPairs` is 0, the consumer blocks until a pair is produced

**Semaphore**: `fullPairs`

---

### 3. Which semaphore protects the critical section in your particle buffer program?

**Answer:**
The `mutex` semaphore (initialized to 1) protects the critical section. It ensures that:
- Buffer modification (adding or removing particles) is atomic
- No two threads (producer or consumer) can access the buffer simultaneously
- Particle pair integrity is preserved (P1 and P2 added/removed together)
- Counters (`producedPairs`, `packagedPairs`) are updated safely

**Code pattern:**
```java
wait(mutex);
try {
    // Critical section: buffer access
} finally {
    signal(mutex);
}
```

---

### 4. How does your program verify that P1 and P2 belong to the same pair?

**Answer:**
Each particle is created with a unique identifier format: `M<machineId>-<pairId>-<type>` (e.g., `M2-17-P1`).

**Verification logic:**
```java
if (!p1.machineId.equals(p2.machineId) || p1.pairId != p2.pairId) {
    throw new RuntimeException("Pairs are incorrect");
}
```

This checks:
1. Both particles are from the **same machine** (same `machineId`)
2. Both particles have the **same pair ID** (same `pairId`)
3. One is marked `P1` and the other `P2` (by construction, since producer creates them together)

If either check fails, an error is thrown immediately, halting execution.

---

### 5. In Task 2, why can the program print letters in the wrong order without semaphores?

**Answer:**
Without semaphores, the three processes are completely independent:
- All three start simultaneously (synchronized only by a `CyclicBarrier`)
- Each thread's execution is independent and unpredictable
- The OS scheduler can context-switch at any point
- Even small random sleep differences can cause ordering changes
- There is nothing preventing Process 2 (L) from running before Process 1 (H, E)

**Race condition example:**
1. All three threads are created and start running
2. Process 2 executes `print L` immediately (short sleep or none)
3. Process 1 hasn't executed `print H` yet
4. Process 3 executes `print L` and `print O`
5. Then Process 1 executes `print H` and `print E`
6. Result: `LLLO...HE` or some other permutation

Without ordering constraints, the output is **unpredictable** on each run.

---

### 6. Which semaphore or synchronization step forces H to print before E, L, L, and O?

**Answer:**
Multiple semaphores work together in a chain:

1. **`startH` (initial 1)**: Only Process 1 can proceed initially, so H prints first.
2. **`afterH` → `afterE` chain**: Process 1 must print H and E before signaling `afterE`, preventing Process 2 from printing L.
3. **`afterE` → `afterL1` chain**: Process 2 only prints L after Process 1 signals `afterE`, ensuring L comes after E.
4. **`afterL1` chain**: Process 3 only prints the second L and O after Process 2 signals `afterL1`.

**Key insight**: Each process waits on a semaphore, blocking until the **previous** process completes. This creates a strict ordering:

```
Process 1: H → signal(afterH) → E → signal(afterE)
              ↓
        Process 2: wait(afterE) → L → signal(afterL1)
                                 ↓
                   Process 3: wait(afterL1) → L → O
```

---

### 7. What could cause deadlock in either of your simulations?

**Answer:**

#### Task 1 Deadlock Scenarios

1. **Incorrect semaphore order in producer/consumer**:
   - If producer does: `wait(mutex)` → `wait(emptyPairs)` (reversed order)
   - If consumer does: `wait(mutex)` → `wait(fullPairs)` (reversed order)
   - **Deadlock**: Producer holds `mutex`, waiting for `emptyPairs`. Consumer holds `emptyPairs`, waiting for `mutex`.

2. **Missing signal calls**:
   - If producer acquires `mutex` but crashes without `signal(mutex)`
   - Consumer blocks forever on `wait(mutex)`

3. **Insufficient buffer space**:
   - If multiple producers and insufficient initial `emptyPairs` (< number of producers)
   - Unlikely to cause deadlock in this design, but could cause starvation

#### Task 2 Deadlock Scenarios

1. **Circular wait in semaphore chain**:
   - If Process 1 waits on `afterE` and Process 2 waits on `afterH`
   - Circular dependency: Process 1 can't signal `afterE` because it's blocked on `afterE` waiting for `afterH`
   - **Deadlock**: Neither process makes progress

2. **Missing initial signal**:
   - If `startH` is initialized to 0 instead of 1
   - Process 1 blocks forever on `wait(startH)`
   - Deadlock: No one can signal it

3. **Forgot to signal**:
   - If Process 1 doesn't call `signal(afterE)` after printing E
   - Process 2 blocks forever on `wait(afterE)`
   - Deadlock: Process 2 never prints

#### Prevention Strategy

- Always pair `wait()` with `signal()`
- Use `try-finally` to ensure signals even on exceptions
- Order semaphore acquisitions consistently (e.g., always acquire `emptyPairs` before `mutex`)
- Initialize semaphores with correct counts based on resource availability

---

## Reflection

### What did these simulations teach you about using semaphores for shared resources and ordered execution?

**Key Learnings:**

1. **Semaphores solve two distinct problems:**
   - **Counting semaphores** (Task 1) manage finite resources (buffer capacity)
   - **Binary semaphores/ordering** (Task 2) enforce execution order

2. **Critical sections are unavoidable:**
   - Without a mutex, concurrent access to shared data causes corruption
   - Even simple operations like "add two items" must be atomic

3. **Waiting conditions prevent errors:**
   - Producers must wait until space is available (not assume)
   - Consumers must wait until items are ready (not check continuously)
   - This prevents both resource exhaustion and underflow

4. **Order matters:**
   - Race conditions aren't just about data corruption
   - They can cause completely wrong logic flow (printing in wrong order)
   - Semaphores provide a disciplined way to coordinate multiple threads

5. **Deadlock is real and subtle:**
   - Simple mistakes (wrong semaphore order, missing signals) cause deadlock
   - Requires careful design and testing
   - In production, even correct code can deadlock under load (e.g., timeout-induced cascades)

6. **Performance vs. correctness trade-off:**
   - Semaphores add overhead (context switching, signal checking)
   - But correctness is non-negotiable
   - Better to be slow and right than fast and broken

---

## Files Submitted

```
activity5/
├── README.md (this file)
├── task1_particles/
│   ├── ParticlesBefore.java
│   └── ParticlesAfter.java
├── task2_hello/
│   ├── HelloBefore.java
│   └── HelloAfter.java
└── screenshots/
    ├── task1_before_semaphore.png
    ├── task1_after_semaphore.png
    ├── task2_before_semaphore.png
    └── task2_after_semaphore.png
```

---

## Testing Notes

- All programs are deterministic (given the same timing)
- Task 2 After should always print "HELLO" exactly
- Task 1 After should run without errors indefinitely
- Task 1 Before may fail immediately or after several iterations (race-dependent)
- Task 2 Before will show varied output across iterations