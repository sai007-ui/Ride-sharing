import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class RideProcessor {
    private static final Logger logger = Logger.getLogger(RideProcessor.class.getName());
    private static final Queue<String> taskQueue = new LinkedList<>();
    private static final List<String> results = new ArrayList<>();
    private static final Lock queueLock = new ReentrantLock();
    private static final Lock resultLock = new ReentrantLock();

    public static void main(String[] args) {
        int numWorkers = 3;
        Thread[] workers = new Thread[numWorkers];

        // Populate task queue
        for (int i = 1; i <= 12; i++) {
            taskQueue.add("RideRequest-" + i);
        }

        // Start workers
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new Thread(new Worker(i));
            workers[i].start();
        }

        // Wait for completion
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Thread interrupted", e);
            }
        }

        // Output results
        System.out.println("\nFinal Results:");
        for (String result : results) {
            System.out.println(result);
        }
    }

    static class Worker implements Runnable {
        private final int id;

        Worker(int id) {
            this.id = id;
        }

        public void run() {
            logger.info("Worker " + id + " started.");
            while (true) {
                String task = null;

                queueLock.lock();
                try {
                    if (!taskQueue.isEmpty()) {
                        task = taskQueue.poll();
                    }
                } finally {
                    queueLock.unlock();
                }

                if (task == null) break;

                try {
                    Thread.sleep(400); // Simulate processing
                    String result = "Worker " + id + " completed " + task;

                    resultLock.lock();
                    try {
                        results.add(result);
                    } finally {
                        resultLock.unlock();
                    }

                    logger.info(result);
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Worker " + id + " interrupted", e);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Worker " + id + " error", e);
                }
            }
            logger.info("Worker " + id + " finished.");
        }
    }
}
