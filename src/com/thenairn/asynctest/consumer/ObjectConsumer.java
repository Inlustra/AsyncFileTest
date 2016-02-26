package com.thenairn.asynctest.consumer;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An Object Queue that is backed by a single condition
 * The reason I chose to create this instead of using a simple 
 * BlockingQueue is to enable the addition of multiple conditions whilst
 * also being able to control the locking mechanism 
 * 
 * (Perform actions before locking, such as a flush, without requiring an if statement)
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
                lockAndWait();
                T object = lockAndPoll();
                handleObject(object);
            } catch (InterruptedException e) {
                return; //Interrupt should only be called when close() is called.
            }
        }
    }

    /**
     * Locks the queue while waiting for an object (Avoids
     *
     * @throws InterruptedException
     */
    private T lockAndPoll() throws InterruptedException {
        lock.lock();
        try {
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Locks the o
     *
     * @throws InterruptedException
     */
    private void lockAndWait() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                waitObjectAvailable();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Causes the thread to wait until an Object has been added to the queue
     *
     * @throws InterruptedException
     */
    protected void waitObjectAvailable() throws InterruptedException {
        jobReady.await(); //The Queue is empty, wait for a job to be added.
    }

    /**
     * @param object the object to be added to the disk for handling.
     */
    public void add(T object) {
        if (!running.get()) {
            System.err.println("Consumer is closed. Sinking: " + object);
            return;
        }
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
