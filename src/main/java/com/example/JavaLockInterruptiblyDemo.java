package com.example;

import java.util.concurrent.locks.ReentrantLock;

class CounterWithLockInterruptibly {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public void increment() {
        try {
            lock.lockInterruptibly();
            try {
                Thread.sleep(1); // Simulate contention
                count++;
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + ": Interrupted while waiting for lock");
        }
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) {
        CounterWithLockInterruptibly counter = new CounterWithLockInterruptibly();
        System.out.println("Initial count: " + counter.getCount());

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");

        t1.start();
        t2.start();
        try {
            Thread.sleep(10); // Ensure contention before interrupting
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t1.interrupt(); // Interrupt Thread-1
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Final count: " + counter.getCount()); // Likely < 2000
    }
}