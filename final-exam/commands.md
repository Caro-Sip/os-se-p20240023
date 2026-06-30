# commands.md — exact commands I ran, per part

> Paste the **real** commands you ran, in order, in the fenced blocks below. Graded for
> command competency and is your defence if any output is questioned. One block per part.
> Delete the hint comments and replace with your actual commands.

## Part A — Threads, Mapping & Signals

```bash
# compile the threaded program (mind the threading flag), run it
gcc -pthread partA_threads/thread_demo.c -o partA_threads/thread_demo
./partA_threads/thread_demo &
PID=$!
sleep 2
ps -T -p $PID > partA_threads/thread_map.txt
wait $PID

# compile/run signal_demo and demonstrate catching the interactive interrupt
gcc partA_threads/signal_demo.c -o partA_threads/signal_demo
./partA_threads/signal_demo &
PID=$!
sleep 1
kill -INT $PID
wait $PID
```

## Part B — Permissions, Special Bits & ACLs

```bash
# Create shared directory and private file
mkdir -p partB_security/shared_dir
echo "TechCorp Top Secret Widget Specs" > partB_security/shared_dir/private_file

# Set fine-grained permissions (owner read/write only for file; traversable but not listable for dir)
chmod 600 partB_security/shared_dir/private_file
chmod 711 partB_security/shared_dir

# Record permissions report
echo "=== B1 Perm Report ===" > partB_security/perm_report.txt
echo "--- Directory permissions (ls -ld) ---" >> partB_security/perm_report.txt
ls -ld partB_security/shared_dir >> partB_security/perm_report.txt
echo "--- File permissions (ls -l) ---" >> partB_security/perm_report.txt
ls -l partB_security/shared_dir/private_file >> partB_security/perm_report.txt
echo "--- Directory stat ---" >> partB_security/perm_report.txt
stat partB_security/shared_dir >> partB_security/perm_report.txt
echo "--- File stat ---" >> partB_security/perm_report.txt
stat partB_security/shared_dir/private_file >> partB_security/perm_report.txt

# Demonstrate setgid + sticky bit
mkdir -p partB_security/special_dir
chmod g+s partB_security/special_dir
chmod +t partB_security/special_dir

# Compile and set setuid bit
gcc partB_security/setuid_demo.c -o partB_security/setuid_demo
chmod u+s partB_security/setuid_demo
./partB_security/setuid_demo
```

## Part C — Bash Scripting, PATH & Safe Scanning

```bash
# Set permissions and run greeter by modifying PATH
chmod +x partC_scripting/scripts/greeter
export PATH="$PATH:/home/se-suon-caro/os-se-p20240023/final-exam/partC_scripting/scripts"
which greeter
greeter

# Save resolved PATH location into path_report.txt
echo "PATH environment variable:" > partC_scripting/path_report.txt
echo "$PATH" >> partC_scripting/path_report.txt
echo "" >> partC_scripting/path_report.txt
echo "Resolved location of greeter:" >> partC_scripting/path_report.txt
which greeter >> partC_scripting/path_report.txt

# Set up test directories and files for collector
mkdir -p partC_scripting/test_dir1 partC_scripting/test_dir2
echo "Content of File A (Readable)" > partC_scripting/test_dir1/file_a.txt
echo "Content of File B (Unreadable)" > partC_scripting/test_dir1/file_b.txt
chmod 000 partC_scripting/test_dir1/file_b.txt
echo "Content of File C (Readable)" > partC_scripting/test_dir2/file_c.txt
echo "Content of File D (Readable)" > partC_scripting/test_dir2/file_d.txt

# Run collector script
chmod +x partC_scripting/scripts/collector
cd partC_scripting
./scripts/collector test_dir1 test_dir2 non_existent_dir
cd ..
```

## Part D — Race Condition & flock

```bash
# Make buy_widget and swarm executable
chmod +x partD_secure/scripts/buy_widget
chmod +x partD_secure/scripts/swarm

# Run unpatched swarm (demonstrate race condition)
export NO_LOCK=1
./partD_secure/scripts/swarm
./partD_secure/scripts/swarm
./partD_secure/scripts/swarm

# Run patched swarm (demonstrate file locking)
unset NO_LOCK
./partD_secure/scripts/swarm
./partD_secure/scripts/swarm
./partD_secure/scripts/swarm
```

## Part E — Backups & cron

```bash
# Make scripts executable
chmod +x partE_automation/scripts/backup_project
chmod +x partE_automation/scripts/timed_job
chmod +x partE_automation/scripts/backup_exam

# E1: Create mock project and run backup_project 5 times to test pruning
mkdir -p partE_automation/project/sub
echo "Hello World" > partE_automation/project/file1.txt
echo "Nested file content" > partE_automation/project/sub/file2.txt
for i in {1..5}; do ./partE_automation/scripts/backup_project; sleep 1; done

# E2 & E3: Edit crontab to add scheduled entries
# Open crontab: crontab -e
# Paste the following entries:
# */1 * * * * /home/se-suon-caro/os-se-p20240023/final-exam/partE_automation/scripts/timed_job /home/se-suon-caro/os-se-p20240023/final-exam/partE_automation/logs/cron_recurring.log
# 35 14 * * * /home/se-suon-caro/os-se-p20240023/final-exam/partE_automation/scripts/timed_job /home/se-suon-caro/os-se-p20240023/final-exam/partE_automation/logs/cron_oneshot.log
# */5 * * * * /home/se-suon-caro/os-se-p20240023/final-exam/partE_automation/scripts/backup_exam
# 0 16 * * * /home/se-suon-caro/os-se-p20240023/final-exam/partE_automation/scripts/backup_exam

# Verify and capture reports
crontab -l
cat partE_automation/logs/cron_recurring.log
cat partE_automation/logs/cron_oneshot.log
ls -la ~/exam-backups/
```
