package com.example;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaLockInterruptiblyDemoTest {
    @Test
    void testInterruptibleLockBehavior() throws InterruptedException {
        // Redirect System.out to capture output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            JavaLockInterruptiblyDemo counter = new JavaLockInterruptiblyDemo();

            Runnable task = () -> {
                for (int i = 0; i < 1000; i++) {
                    counter.increment();
                }
            };

            Thread t1 = new Thread(task, "Thread-1");
            Thread t2 = new Thread(task, "Thread-2");

            t1.start();
            t2.start();
            Thread.sleep(10); // Ensure contention
            t1.interrupt();
            t1.join();
            t2.join();

            int finalCount = counter.getCount();
            String output = outContent.toString();

            // Verify interruption occurred
            assertTrue(output.contains("Thread-1: Interrupted while waiting for lock"),
                    "Expected interruption message not found");
            // Verify count is less than 2000 due to interruptions
            assertTrue(finalCount < 2000,
                    "Expected count less than 2000, but got: " + finalCount);
            // Verify count is reasonable (e.g., t2 completes most increments)
            assertTrue(finalCount >= 1000,
                    "Expected count at least 1000, but got: " + finalCount);

        } finally {
            // Restore System.out
            System.setOut(originalOut);
        }
    }
}
