package com.thenairn.asynctest;

import com.thenairn.asynctest.consumer.Log.LogConsumer;

import java.util.Date;

public class Main {


    /**
     * Application entry point
     * Creates the consumer in a new thread in order to prevent blocking.
     */
    public static void main(String[] args) {
        LogConsumer consumer = new LogConsumer();
        Thread thread = new Thread(consumer);
        thread.start();
        for(int i = 0; i < 1000; i++) { //Generate 1000 threads to supply the queue with lots of data
            startWaitThread(consumer, (long) (Math.random() * 10)+10);
        }
    }

    /**
     * A method used to spawn threads that will sleep for the 
     * allotted amount of time, and then print the current date to the logger
     */
    private static void startWaitThread(final LogConsumer consumer, final long waitTime) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                consumer.log(new Date().toString());
            }
        }).start();
    }
}
