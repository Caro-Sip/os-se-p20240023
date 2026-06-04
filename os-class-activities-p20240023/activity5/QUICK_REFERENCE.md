# Quick Reference: Semaphores in Java

## Java Semaphore Basics

```java
import java.util.concurrent.Semaphore;

// Create a semaphore with initial permit count
Semaphore sem = new Semaphore(1);

// Acquire a permit (wait if not available)
sem.acquire();        // Blocking
sem.acquireUninterruptibly();  // Can't be interrupted

// Release a permit (wake up one waiting thread)
sem.release();
```

## Common Patterns

### Pattern 1: Mutex (Binary Semaphore)
```java
Semaphore mutex = new Semaphore(1);

// Thread A
mutex.acquire();
try {
    // Critical section
    sharedResource.modify();
} finally {
    mutex.release();
}
```

### Pattern 2: Counting Semaphore
```java
Semaphore available = new Semaphore(5);  // 5 resources

// Thread A: use a resource
available.acquire();
try {
    // Use one resource
    useResource();
} finally {
    available.release();
}
```

### Pattern 3: Ordering with Semaphores
```java
Semaphore step1 = new Semaphore(1);
Semaphore step2 = new Semaphore(0);

// Thread A: must run first
step1.acquire();
doStep1();
step2.release();  // Signal thread B

// Thread B: waits for A
step2.acquire();
doStep2();
```

## Key Concepts

### Deadlock Prevention

❌ **WRONG: Circular wait**
```java
Semaphore A = new Semaphore(1);
Semaphore B = new Semaphore(1);

// Thread 1         // Thread 2
A.acquire();        B.acquire();
B.acquire();  X     A.acquire();  X  (DEADLOCK)
```

✓ **CORRECT: Ordered acquisition**
```java
// All threads acquire in same order: A then B
A.acquire();
B.acquire();
// Use resources
B.release();
A.release();
```

### Signal Guarantee

✓ **CORRECT: Always signal**
```java
sem.acquire();
try {
    // Even if exception, finally ensures signal
    doWork();
} finally {
    sem.release();
}
```

❌ **WRONG: Signal might not happen**
```java
sem.acquire();
doWork();
sem.release();  // If doWork() throws, this never runs
```

## Task 1: Particle Buffer Semaphores

### Three Semaphores
```
┌─────────────────────────────────────────┐
│ Shared Buffer (100 particles max)       │
└─────────────────────────────────────────┘
         ▲                          ▼
    emptyPairs                  fullPairs
    (initial: 50)               (initial: 0)
    
    Producer:                   Consumer:
    - wait(emptyPairs)          - wait(fullPairs)
    - wait(mutex)               - wait(mutex)
    - add to buffer             - remove from buffer
    - signal(mutex)             - signal(mutex)
    - signal(fullPairs)         - signal(emptyPairs)
         │
         └─ mutex (initial: 1) protects buffer access
```

### Invariants
- `emptyPairs + fullPairs ≈ 50` (some rounding due to timing)
- Buffer size `== fullPairs * 2` (always pairs, never odd)
- Pairs always from same machine and pair ID

## Task 2: HELLO Ordering Semaphores

### Semaphore Chain
```
Process 1: H → E → signal(afterE)
              │
              ▼
Process 2: wait(afterE) → L → signal(afterL1)
              │
              ▼
Process 3: wait(afterL1) → L → O
```

### Why It Works
1. Only one process can have `wait()` return at a time
2. Each process signals the next one
3. Creates strict ordering: Process 1 → 2 → 3

## Debugging Tips

### Problem: Program hangs
- Check: Are all `wait()` calls matched with `signal()`?
- Check: Is initial semaphore count correct?
- Check: Are semaphores in correct order? (no circular dependency)

### Problem: Wrong output in Task 2
- Check: Does Process 1 signal before Process 2 waits?
- Check: Does Process 2 signal before Process 3 waits?
- Check: Try adding debug prints like:
  ```java
  System.err.println("Thread " + Thread.currentThread().getName() + " waiting...");
  sem.acquire();
  System.err.println("Thread " + Thread.currentThread().getName() + " acquired!");
  ```

### Problem: Task 1 errors appear randomly
- This is actually correct behavior for "Before" version
- It demonstrates race conditions
- The "After" version should never error

## Common Java Concurrency Classes

| Class | Use |
|-------|-----|
| `Semaphore` | Counting or mutex |
| `Mutex` (or `Semaphore(1)`) | Binary lock |
| `CountDownLatch` | One-time synchronization barrier |
| `CyclicBarrier` | Repeated synchronization point |
| `ReentrantLock` | More flexible than `synchronized` |
| `Object.wait()/notify()` | Low-level synchronization |

For this activity, **Semaphore** is the right tool!

## Running Programs

### Compile
```bash
# Linux/Mac/WSL
javac task1_particles/*.java task2_hello/*.java

# Or individual
javac task1_particles/ParticlesBefore.java
```

### Run
```bash
java -cp task1_particles ParticlesBefore
java -cp task1_particles ParticlesAfter
java -cp task2_hello HelloBefore
java -cp task2_hello HelloAfter
```

### With Manual Classpath
```bash
javac -d . task1_particles/ParticlesBefore.java
java ParticlesBefore
```

## Expected Output

### Task 1A (Before): Something like
```
Produced pairs: 1 | Packaged pairs: 0 | Buffer particles: 2
Produced pairs: 2 | Packaged pairs: 0 | Buffer particles: 4
Produced pairs: 3 | Packaged pairs: 0 | Buffer particles: 6
Produced pairs: 4 | Packaged pairs: 1 | Buffer particles: 6
[After some time...]
Pairs are incorrect
```

### Task 1B (After): Something like
```
=== Particle Pair Buffer WITH Semaphores ===
Producers: 3
Buffer capacity: 100 particles (50 pairs)

Produced pairs: 1 | Packaged pairs: 0 | Buffer particles: 2
Produced pairs: 2 | Packaged pairs: 0 | Buffer particles: 4
Produced pairs: 3 | Packaged pairs: 1 | Buffer particles: 4
Produced pairs: 4 | Packaged pairs: 2 | Buffer particles: 4
[Continues indefinitely without errors...]
```

### Task 2A (Before): Each iteration different
```
Iteration 1: LEHLO
Iteration 2: HELLO
Iteration 3: LELOH
```

### Task 2B (After): Always same
```
Iteration 1: HELLO
Iteration 2: HELLO
Iteration 3: HELLO

✓ SUCCESS: All iterations printed HELLO!
```

---

**Pro Tip**: Semaphores are like traffic lights. `wait()` is a red light, `signal()` is changing to green. No semaphore = everyone tries to drive at once!
