import java.util.concurrent.Semaphore;

class Account {
    String name;
    int balance;
    Semaphore lock = new Semaphore(1);

    Account(String name, int balance) {
        this.name = name;
        this.balance = balance;
    }
}

class Transfer {
    static void transfer(Account from, Account to, int amount) {
        try {
            System.out.println(Thread.currentThread().getName()
                    + " trying to lock FROM " + from.name);
            from.lock.acquire();
            System.out.println(Thread.currentThread().getName()
                    + " locked FROM " + from.name);

            // Sleep to increase chance of deadlock
            Thread.sleep(100);

            System.out.println(Thread.currentThread().getName()
                    + " trying to lock TO " + to.name);
            to.lock.acquire();
            System.out.println(Thread.currentThread().getName()
                    + " locked TO " + to.name);

            from.balance -= amount;
            to.balance += amount;

            System.out.println(Thread.currentThread().getName()
                    + " transfer completed");

            to.lock.release();
            from.lock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class DeadlockSimulation {
    public static void main(String[] args) {
        Account account1 = new Account("Account-A", 1000);
        Account account2 = new Account("Account-B", 1000);

        System.out.println("=== Bank Deadlock Simulation ===");
        System.out.println("Starting total: " + (account1.balance + account2.balance));
        System.out.println("Account A initial balance: " + account1.balance);
        System.out.println("Account B initial balance: " + account2.balance);
        System.out.println();

        Thread t1 = new Thread(() ->
                Transfer.transfer(account1, account2, 100),
                "Worker-1"
        );

        Thread t2 = new Thread(() ->
                Transfer.transfer(account2, account1, 200),
                "Worker-2"
        );

        // Track completion
        long startTime = System.currentTimeMillis();
        long timeout = 5000; // 5 seconds

        t1.start();
        t2.start();

        // Watchdog thread
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(timeout);
                if (t1.isAlive() || t2.isAlive()) {
                    System.out.println();
                    System.out.println("=== DEADLOCK DETECTED ===");
                    System.out.println("Deadlock detected: transactions are stuck");
                    System.out.println("Worker-1 is waiting for Account-B");
                    System.out.println("Worker-2 is waiting for Account-A");
                    System.out.println();
                    System.out.println("Current Account A balance: " + account1.balance);
                    System.out.println("Current Account B balance: " + account2.balance);
                    System.out.println("Current total: " + (account1.balance + account2.balance));
                    System.exit(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        watchdog.setDaemon(true);
        watchdog.start();

        try {
            t1.join();
            t2.join();
            System.out.println();
            System.out.println("Final Account A balance: " + account1.balance);
            System.out.println("Final Account B balance: " + account2.balance);
            System.out.println("Final total: " + (account1.balance + account2.balance));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
