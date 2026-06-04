import java.util.concurrent.Semaphore;

class Account {
    String name;
    int balance;

    Account(String name, int balance) {
        this.name = name;
        this.balance = balance;
    }
}

class Transfer {
    // Single semaphore mutex for all transfers
    static Semaphore mutex = new Semaphore(1);

    static void transfer(Account from, Account to, int amount) {
        try {
            System.out.println(Thread.currentThread().getName()
                    + " trying to acquire mutex for transfer");
            mutex.acquire();
            System.out.println(Thread.currentThread().getName()
                    + " acquired mutex, transferring " + amount + " from "
                    + from.name + " to " + to.name);

            // Sleep to simulate processing time
            Thread.sleep(100);

            from.balance -= amount;
            to.balance += amount;

            System.out.println(Thread.currentThread().getName()
                    + " transfer completed");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
            System.out.println(Thread.currentThread().getName()
                    + " released mutex");
        }
    }
}

public class DeadlockFixed {
    public static void main(String[] args) {
        Account account1 = new Account("Account-A", 1000);
        Account account2 = new Account("Account-B", 1000);

        System.out.println("=== Bank Deadlock Prevention (Fixed) ===");
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

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
            System.out.println();
            System.out.println("=== TRANSFER COMPLETED ===");
            System.out.println("Final Account A balance: " + account1.balance);
            System.out.println("Final Account B balance: " + account2.balance);
            System.out.println("Final total: " + (account1.balance + account2.balance));
            System.out.println("No deadlock occurred");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
