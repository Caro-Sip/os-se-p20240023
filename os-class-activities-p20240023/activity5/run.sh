#!/bin/bash
# Run script for Class Activity 5 - Semaphores

set -e

echo "=== Class Activity 5 - Semaphores: Particle Pairs and HELLO Ordering ==="
echo ""

# Compile
echo "[1/4] Compiling Java files..."
javac task1_particles/*.java task2_hello/*.java
echo "✓ Compilation successful!"
echo ""

# Menu
echo "Choose what to run:"
echo "  1) Task 1A: Particles BEFORE semaphores (may fail quickly)"
echo "  2) Task 1B: Particles AFTER semaphores (runs indefinitely)"
echo "  3) Task 2A: HELLO BEFORE semaphores (shows unpredictable output)"
echo "  4) Task 2B: HELLO AFTER semaphores (always prints HELLO)"
echo "  5) Run all in sequence (interactive)"
echo ""
read -p "Enter choice (1-5): " choice

case $choice in
  1)
    echo ""
    echo "Running Task 1A: Particles BEFORE Semaphores"
    echo "Press Ctrl+C to stop when you have enough evidence of the broken behavior"
    echo ""
    java -cp task1_particles particles_before
    ;;
  2)
    echo ""
    echo "Running Task 1B: Particles AFTER Semaphores"
    echo "Press Ctrl+C to stop (program runs indefinitely when correct)"
    echo ""
    java -cp task1_particles particles_after
    ;;
  3)
    echo ""
    echo "Running Task 2A: HELLO BEFORE Semaphores"
    echo "Notice how output varies - sometimes HELLO, sometimes wrong order"
    echo ""
    java -cp task2_hello hello_before
    ;;
  4)
    echo ""
    echo "Running Task 2B: HELLO AFTER Semaphores"
    echo "Should always print HELLO in 3 iterations"
    echo ""
    java -cp task2_hello hello_after
    ;;
  5)
    echo ""
    echo "Running Task 1A: Particles BEFORE Semaphores (30 seconds)"
    timeout 30 java -cp task1_particles particles_before || true
    echo ""
    read -p "Press Enter to continue to Task 1B..."
    
    echo ""
    echo "Running Task 1B: Particles AFTER Semaphores (30 seconds)"
    timeout 30 java -cp task1_particles particles_after || true
    echo ""
    read -p "Press Enter to continue to Task 2A..."
    
    echo ""
    echo "Running Task 2A: HELLO BEFORE Semaphores"
    java -cp task2_hello hello_before
    echo ""
    read -p "Press Enter to continue to Task 2B..."
    
    echo ""
    echo "Running Task 2B: HELLO AFTER Semaphores"
    java -cp task2_hello hello_after
    ;;
  *)
    echo "Invalid choice"
    exit 1
    ;;
esac

echo ""
echo "Done!"
