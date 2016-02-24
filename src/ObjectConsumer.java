import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Thomas Nairn on 24/02/2016.
 */
public abstract class ObjectConsumer<T> implements Runnable, Closeable {

    private final Queue<T> queue = new LinkedList<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition jobReady = lock.newCondition();

    @Override
    public void run() {
        while (running.get()) {
            try {
                waitObjectAvailable();
                handleObject(queue.poll());
            } catch (InterruptedException e) {
                return; //Interrupt should only be called when close() is called.
            }
        }
    }

    /**
     * Causes the thread to wait until an Object has been added to the queue
     *
     * @throws InterruptedException
     */
    private void waitObjectAvailable() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                jobReady.await(); //The Queue is empty, wait for a job to be added.
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param object the object to be added to the disk for handling.
     */
    public void add(T object) {
        lock.lock();
        try {
            this.queue.add(object);
            this.jobReady.signal();
        } finally {
            lock.unlock();
        }
    }

    public abstract void handleObject(T t);

    /**
     * Locks within in this class are safe due to try,finally
     */
    @Override
    public void close() throws IOException {
        this.running.set(false);
        Thread.currentThread().interrupt();
    }
}
