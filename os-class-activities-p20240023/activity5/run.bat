@echo off
REM Run script for Class Activity 5 - Semaphores (Windows)

setlocal enabledelayedexpansion

echo === Class Activity 5 - Semaphores: Particle Pairs and HELLO Ordering ===
echo.

REM Compile
echo [1/4] Compiling Java files...
javac task1_particles\*.java task2_hello\*.java
if errorlevel 1 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)
echo. Compilation successful!
echo.

REM Menu
echo Choose what to run:
echo   1) Task 1A: Particles BEFORE semaphores (may fail quickly)
echo   2) Task 1B: Particles AFTER semaphores (runs indefinitely)
echo   3) Task 2A: HELLO BEFORE semaphores (shows unpredictable output)
echo   4) Task 2B: HELLO AFTER semaphores (always prints HELLO)
echo   5) Run all in sequence (interactive)
echo.
set /p choice="Enter choice (1-5): "

echo.
if "%choice%"=="1" (
    echo Running Task 1A: Particles BEFORE Semaphores
    echo Press Ctrl+C to stop when you have enough evidence
    echo.
    java -cp task1_particles particles_before
)
if "%choice%"=="2" (
    echo Running Task 1B: Particles AFTER Semaphores
    echo Press Ctrl+C to stop
    echo.
    java -cp task1_particles particles_after
)
if "%choice%"=="3" (
    echo Running Task 2A: HELLO BEFORE Semaphores
    echo Notice how output varies
    echo.
    java -cp task2_hello hello_before
)
if "%choice%"=="4" (
    echo Running Task 2B: HELLO AFTER Semaphores
    echo Should always print HELLO in 3 iterations
    echo.
    java -cp task2_hello hello_after
)
if "%choice%"=="5" (
    echo Running all tasks (with timeouts)...
    echo.
    echo [Task 1A] Particles BEFORE Semaphores
    timeout /t 30 /nobreak
    java -cp task1_particles particles_before
    
    echo.
    echo [Task 1B] Particles AFTER Semaphores
    timeout /t 30 /nobreak
    java -cp task1_particles particles_after
    
    echo.
    echo [Task 2A] HELLO BEFORE Semaphores
    java -cp task2_hello hello_before
    
    echo.
    pause
    
    echo [Task 2B] HELLO AFTER Semaphores
    java -cp task2_hello hello_after
)

echo.
echo Done!
pause
