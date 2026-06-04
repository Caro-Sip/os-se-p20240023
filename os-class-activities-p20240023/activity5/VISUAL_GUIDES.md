# Visual Guides for Semaphore Logic

## Task 1: Particle Buffer Semaphore Architecture

### The Three Semaphores

```
╔════════════════════════════════════════════════════════════════╗
║              Shared Particle Buffer (100 max)                  ║
║              [P1 P2] [P1 P2] [P1 P2] ...                       ║
╚════════════════════════════════════════════════════════════════╝
        ▲                                              ▼
        │                                              │
   PRODUCERS                                      CONSUMER
   (add pairs)                                   (remove pairs)
        │                                              │
        │                                              │
    wait() │◄─── emptyPairs ───────┐                 │
         │      (initial: 50)        │ release()      │
         │      (counts: 0-50)       ├─────────┐      │
         │                           │         │      │
         │      ┌─ mutex ────────────┤         │      │
         │      │ (initial: 1)       │         │      │
         │      │ (protects buffer)  │    release()  │
         │      │                    │         │      │
         │      ▼                    │         │      │
         │   Critical Section        │         ▼      │
         │   - Add P1, P2            │     wait() │
         │   - Update counter        │      │     │
         │      │                    │      │     │
         │      ▼                    │      │     │
         │   signal(fullPairs)◄──────┘      │ Critical Section
         │                                  │ - Remove P1, P2
         └──────────────┐                   │ - Verify pair
                        │                   │ - Update counter
                    signal()            signal()
                        │                   │
                        ▼                   ▼
                    fullPairs          emptyPairs
                    (initial: 0)       (initial: 50)
                    (counts: 0-50)     (counts: 0-50)
```

### Semaphore State Transitions

```
Initial State:
┌─────────────────┬──────────────────┬────────┐
│ emptyPairs (50) │ fullPairs (0)    │ mutex  │
└─────────────────┴──────────────────┴────────┘

Producer adds a pair (M1-0-P1, M1-0-P2):
  1. wait(emptyPairs)    → emptyPairs becomes 49
  2. wait(mutex)         → mutex becomes 0 (locked)
  3. [add to buffer]     → buffer has 2 particles
  4. signal(mutex)       → mutex becomes 1 (unlocked)
  5. signal(fullPairs)   → fullPairs becomes 1

  State: ┌───────────────┬───────────────┬────────┐
         │ emptyPairs(49)│ fullPairs(1)  │ mutex  │
         └───────────────┴───────────────┴────────┘

Consumer removes the pair:
  1. wait(fullPairs)     → fullPairs becomes 0
  2. wait(mutex)         → mutex becomes 0 (locked)
  3. [remove from buffer]→ buffer has 0 particles
  4. signal(mutex)       → mutex becomes 1 (unlocked)
  5. signal(emptyPairs)  → emptyPairs becomes 50

  State: ┌───────────────┬───────────────┬────────┐
         │ emptyPairs(50)│ fullPairs(0)  │ mutex  │
         └───────────────┴───────────────┴────────┘
```

### Producer Thread Waiting Diagram

```
Producers waiting for emptyPairs:

Thread P1: [want to add]
  │
  └─▶ wait(emptyPairs)
      │
      ├─ emptyPairs == 0? ───▶ BLOCK (queue position 1)
      │
      └─ emptyPairs > 0? ───▶ CONTINUE
                              │
                              └─▶ wait(mutex)
                                  │
                                  ├─ mutex locked? ───▶ BLOCK (queue)
                                  │
                                  └─ mutex free? ───▶ ADD PAIR
                                                        │
                                                        └─▶ signal(mutex)
                                                            signal(fullPairs)

Thread P2, P3: (same flow, may block if P1 is in critical section)
```

---

## Task 2: HELLO Ordering Semaphore Chain

### Linear Ordering Chain

```
Process 1                Process 2                Process 3
┌──────────┐            ┌──────────┐            ┌──────────┐
│ Prints H │            │ Prints L │            │ Prints L │
│          │            │          │            │ Prints O │
│ Prints E │            │          │            │          │
└──────────┘            └──────────┘            └──────────┘
     │                        │                        │
     │ signal(afterE)         │                        │
     └───────────────▶ wait(afterE)                    │
                             │                        │
                             │ signal(afterL1)        │
                             └───────────────▶ wait(afterL1)
                                                      │
                                                      ▼
                                                  [Print]
```

### Execution Timeline

```
Time │ Process 1  │ Process 2      │ Process 3       │ Output
─────┼────────────┼────────────────┼─────────────────┼────────
  0  │ wait(startH)
     │ (immediate)
  1  │ PRINT H    │                │                 │ H
  2  │ PRINT E    │                │                 │ HE
  3  │ signal     │                │                 │
     │ (afterE)   │                │                 │
  4  │ [done]     │ wait(afterE)   │                 │
     │            │ [acquired]     │                 │
  5  │            │ PRINT L        │                 │ HEL
  6  │            │ signal(afterL1)│                 │
  7  │            │ [done]         │ wait(afterL1)   │
     │            │                │ [acquired]      │
  8  │            │                │ PRINT L         │ HELL
  9  │            │                │ PRINT O         │ HELLO
 10  │            │                │ [done]          │
     │            │                │                 │
 Final │ Complete   │ Complete       │ Complete        │ HELLO ✓
```

### Semaphore Values Over Time

```
        ┌─────────────┬──────────────┬──────────────┬──────────────┐
        │ startH      │ afterE       │ afterL1      │ afterL2      │
├────────┼─────────────┼──────────────┼──────────────┼──────────────┤
│Initial │      1      │       0      │       0      │       0      │
├────────┼─────────────┼──────────────┼──────────────┼──────────────┤
│P1:     │      0      │       0      │       0      │       0      │
│waits   │   (0→0)     │    (0→0)     │    (0→0)     │    (0→0)     │
│(done)  │             │              │              │              │
├────────┼─────────────┼──────────────┼──────────────┼──────────────┤
│P1:     │      0      │       1      │       0      │       0      │
│signal  │             │   (0→1)      │              │              │
│afterE  │             │   acquired   │              │              │
├────────┼─────────────┼──────────────┼──────────────┼──────────────┤
│P2:     │      0      │       0      │       1      │       0      │
│waits   │             │   (1→0)      │   (0→1)      │              │
│(done)  │             │   acquired   │   acquired   │              │
├────────┼─────────────┼──────────────┼──────────────┼──────────────┤
│P3:     │      0      │       0      │       0      │       0      │
│done    │             │              │   (1→0)      │              │
│        │             │              │   acquired   │              │
└────────┴─────────────┴──────────────┴──────────────┴──────────────┘

Legend:
- (0→1) means semaphore value increased via signal()
- (1→0) means semaphore value decreased via wait()
```

### Why This Prevents Wrong Output

```
Without Semaphores:
┌─────────────────────────────────────────┐
│ All threads race to print               │
├─────────────────────────────────────────┤
│ Possible outputs:                       │
│ • LEHLO (L printed before E)            │
│ • ELLHO (E,L before H)                  │
│ • HLELO (L,O interrupts E)              │
│ • HELLO (by chance only ~0.8% of time) │
└─────────────────────────────────────────┘

With Semaphores:
┌─────────────────────────────────────────┐
│ Chain of waits enforces order:          │
├─────────────────────────────────────────┤
│ startH blocks until released    ▶ H     │
│       ▼                         ▶ E     │
│ afterE blocks until P1 signals  ▶ L     │
│       ▼                         ▶ L     │
│ afterL1 blocks until P2 signals ▶ O     │
│       ▼                                 │
│ Result: ALWAYS HELLO!                   │
└─────────────────────────────────────────┘
```

---

## Deadlock Scenarios

### ❌ WRONG: Circular Wait (Deadlock)

```
Semaphore A = new Semaphore(1);
Semaphore B = new Semaphore(1);

Thread 1:                   Thread 2:
┌──────────────┐           ┌──────────────┐
│ wait(A)      │ ◀─ 1 ─▶  │ wait(B)      │
│              │            │              │
│ wait(B)      │ ◀─ WAIT ─▶ │ wait(A)      │
│              │            │              │
│ [blocked] ◀──┴────────────┘ [blocked]   │
└──────────────┘           └──────────────┘

Situation:
- Thread 1 holds A, needs B (but Thread 2 has B, waiting for A)
- Thread 2 holds B, needs A (but Thread 1 has A, waiting for B)
- DEADLOCK: Neither can proceed
```

### ✓ CORRECT: Ordered Acquisition

```
Semaphore A = new Semaphore(1);
Semaphore B = new Semaphore(1);

Thread 1:                   Thread 2:
┌──────────────┐           ┌──────────────┐
│ wait(A)      │ ◀─ 1 ─▶  │ wait(A)      │
│              │            │              │
│              │            │ [waiting]    │
│ wait(B)      │ ◀─ 1 ─▶  │              │
│              │            │              │
│ [critical]   │            │ wait(B)      │
│              │            │ [waiting]    │
│ signal(B)    │            │              │
│              │            │              │
│ signal(A)    │ ───────▶ [acquired]      │
│              │            │              │
│ [done]       │ ◀─────── [critical]      │
└──────────────┘           └──────────────┘

Key: Both acquire A first, then B
- Prevents circular wait
- Thread 1 releases, Thread 2 proceeds
- NO DEADLOCK
```

---

## Buffer State Visualization

### Task 1: Buffer Particles Over Time

```
Time │ Buffer State                    │ Produced │ Consumed │ Empty
─────┼─────────────────────────────────┼──────────┼──────────┼───────
  0  │ []                              │    0     │    0     │  50
  1  │ [M1-0-P1 M1-0-P2]              │    1     │    0     │  49
  2  │ [M1-0-P1 M1-0-P2 M2-1-P1 M2-1-P2] │  2   │    0     │  48
  3  │ [M2-1-P1 M2-1-P2]              │    2     │    1     │  49
  4  │ [M2-1-P1 M2-1-P2 M3-2-P1 M3-2-P2] │  3   │    1     │  48
  5  │ [M3-2-P1 M3-2-P2]              │    3     │    2     │  49
  6  │ [M3-2-P1 M3-2-P2 M1-3-P1 M1-3-P2] │  4   │    2     │  48
...

Buffer Rules Enforced:
✓ Never > 100 particles (emptyPairs semaphore)
✓ Always even count (pair integrity)
✓ Never < 0 particles (fullPairs semaphore)
✓ Consumed ≤ Produced (logical ordering)
```

---

## Comparison: Before vs After

### Task 1: Before vs After

```
BEFORE (Without Semaphores):
┌─────────────────────────────────┬───────────────────────────────┐
│ Shared Buffer                   │ No Protection                 │
├─────────────────────────────────┼───────────────────────────────┤
│ Problem: Race Conditions        │ Result: Errors/Crashes        │
│ • Buffer overflow               │ • "The producing machine..."  │
│ • Buffer underflow              │ • "The packaging machine..."  │
│ • Pair corruption               │ • "Pairs are incorrect"       │
│ • Lost updates                  │                               │
└─────────────────────────────────┴───────────────────────────────┘

AFTER (With Semaphores):
┌─────────────────────────────────┬───────────────────────────────┐
│ Shared Buffer                   │ Protected by 3 Semaphores    │
├─────────────────────────────────┼───────────────────────────────┤
│ Result: Correct Behavior        │ Running:                      │
│ • Buffer never overflows        │ Produced pairs: 42            │
│ • No underflow                  │ Packaged pairs: 39            │
│ • Pairs stay intact             │ Buffer particles: 6           │
│ • Counters accurate             │ [Infinite clean execution]    │
└─────────────────────────────────┴───────────────────────────────┘
```

### Task 2: Before vs After

```
BEFORE (Without Ordering):
┌──────────────────┬──────────────────────────┐
│ Possible Output  │ Why                      │
├──────────────────┼──────────────────────────┤
│ HELLO            │ By lucky timing (rare)   │
│ LEHLO            │ L printed before E       │
│ HELLOL           │ Wrong length/order       │
│ ELLHO            │ E-L-L before H           │
│ HLELO            │ Threads interleaved      │
│ (varies each run)│ (race condition)         │
└──────────────────┴──────────────────────────┘

AFTER (With Semaphores):
┌──────────────────┬──────────────────────────┐
│ Output           │ Why                      │
├──────────────────┼──────────────────────────┤
│ HELLO            │ Guaranteed every time    │
│ HELLO            │ Strict semaphore chain   │
│ HELLO            │ No race conditions       │
│ [Always correct] │ (100% reproducible)      │
└──────────────────┴──────────────────────────┘
```

---

## Key Formulas & Rules

### Task 1 Invariants

```
Buffer size = fullPairs.availablePermits() * 2
(because each pair = 2 particles)

Total capacity = 100 particles = 50 pairs

emptyPairs + fullPairs ≈ 50
(with small timing-dependent variance)

Produced ≥ Consumed
(in normal operation)

Safety check:
if (buffer.size() > 100) → "The producing machine is broken"
if (buffer.isEmpty()) → "The packaging machine is broken"
```

### Task 2 Ordering Chain

```
Order of execution:
P1 prints H
P1 prints E
P1 signals(afterE)
P2 waits(afterE)  ← blocks until above completes
P2 prints L
P2 signals(afterL1)
P3 waits(afterL1) ← blocks until above completes
P3 prints L
P3 prints O

Total sequential steps: 9
Parallel threads: 3
Final output: HELLO (deterministic)
```

---

**These diagrams help visualize the semaphore mechanics. Reference them while reading the code!**
