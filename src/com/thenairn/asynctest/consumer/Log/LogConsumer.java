package com.thenairn.asynctest.consumer.Log;

import com.thenairn.asynctest.consumer.ObjectConsumer;
import com.thenairn.asynctest.entity.LogMessage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Inherits from com.thenairn.asynctest.consumer.ObjectConsumer, will ensure that the file
 */
public class LogConsumer extends ObjectConsumer<LogMessage> {

    private static final int FILE_INTERVAL_MINUTES = 5;
    private static final long FILE_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(FILE_INTERVAL_MINUTES);

    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    private static final String FILE_EXTENSION = ".log";

    /**
     * Given more time, this buffer size could grow and shrink depending on how many messages we can afford to lose in the case of a crash
     */
    private static final int BUFFER_SIZE = 1024; //Lower the buffer size as we are only writing log lines

    private static final Calendar CALENDAR = Calendar.getInstance();


    private final File parentDir;
    private final LogMessageWriter logWriter;

    private long lastFileCreate = -1;
    private Writer fileWriter;

    public LogConsumer(File parentDir, LogMessageWriter writer) {
        this.parentDir = parentDir;
        this.logWriter = writer;
    }

    /**
     * Creates a new com.thenairn.asynctest.consumer.Log.LogConsumer
     *
     * @param parentDir the directory in which to create new files
     */
    public LogConsumer(File parentDir) {
        this(parentDir, new LogMessageWriter() {
        });
    }

    /**
     * Creates a new com.thenairn.asynctest.consumer.Log.LogConsumer at the current working directory
     */
    public LogConsumer() {
        this(new File("").getAbsoluteFile());
    }

    /**
     * Convenience method to wrap the log message in a com.thenairn.asynctest.entity.LogMessage object with the add date.
     * @param string
     */
    public void log(String string) {
        this.add(new LogMessage(string));
    }


    /**
     * Main logic of the consumer
     *
     * @param logMessage the message to be handled (Written to file)
     */
    @Override
    public void handleObject(LogMessage logMessage) {
        checkWriter();
        try {
            this.logWriter.write(this.fileWriter, logMessage);
        } catch (IOException e) {
            e.printStackTrace(); //Could not write message
        }
    }

    /**
     * Overridden to allow for explicit flushing of the output stream when/if the Queue experiences a lull.
     * (Which it should.)
     *
     * @throws InterruptedException thrown when thread.close is called during this lock block.
     */
    @Override
    protected void waitObjectAvailable() throws InterruptedException {
        if (this.fileWriter != null) {
            try {
                this.fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace(); //Unable to flush
            }
        }
        super.waitObjectAvailable();
    }

    /**
     * Checks to see if a new file should be created, if so, create it, flush and close previous writer
     */
    private void checkWriter() {
        if (newFileRequired()) {
            try {
                closeFileWriter();
                File file = createFile();
                this.fileWriter = new BufferedWriter(new FileWriter(file, true), BUFFER_SIZE);
            } catch (IOException e) {
                System.err.println("File could not be created, defaulting to previous file.");
                e.printStackTrace();
                if (fileWriter == null) {
                    try {
                        close();
                    } catch (IOException e1) {
                        //Close Quietly
                    }
                }
            }
            this.lastFileCreate = System.currentTimeMillis();
        }
    }

    private void closeFileWriter() {
        if (this.fileWriter != null) {
            try {
                this.fileWriter.close();
            } catch (IOException e) {
                //Couldn't close the filewriter, can mostly ignore error as it usually means that the file is already closed
                e.printStackTrace();
            }
        }
    }

    /**
     * @return The created file if it does not currently exist
     * @throws IOException if the file could not be created
     */
    private File createFile() throws IOException {
        File file = getFile();
        if (!file.exists()) {
            if (!file.createNewFile())
                throw new IOException("Could not create new file.");
        }
        return file;
    }

    /**
     * Gets the file closest to the current time based on the FILE_INTERVAL
     *
     * @return a file with the date to the nearest FILE_INTERVAL and extension appended.
     */
    private File getFile() {
        String date = FILE_NAME_FORMAT.format(roundCalender().getTime());
        System.out.println(date);

        return new File(parentDir, date + FILE_EXTENSION);
    }

    /**
     * Rounds the calendar to the nearest FILE_INTERVAL_MINUTES in minutes
     *
     * @return a calendar object with the minutes rounded (In lenient format)
     */
    private Calendar roundCalender() {
        CALENDAR.setTime(new Date());
        int unroundedMinutes = CALENDAR.get(Calendar.MINUTE);
        int mod = unroundedMinutes % FILE_INTERVAL_MINUTES;
        CALENDAR.add(Calendar.MINUTE, mod < (FILE_INTERVAL_MINUTES / 2) ? -mod : (FILE_INTERVAL_MINUTES - mod));
        System.out.println("Rounded calendar: " + CALENDAR.getTime());
        return CALENDAR;
    }

    /**
     * Determines whether or not a new file is required based on the last time a file was created
     *
     * @return true if the last write was greater than the request file interval
     */
    private boolean newFileRequired() {
        return System.currentTimeMillis() - FILE_INTERVAL_MILLIS > lastFileCreate;
    }

    /**
     * Close currently open IO connections and interrupt any currently running locks.
     * Locks within in this class are safe due to try,finally
     */
    @Override
    public void close() throws IOException {
        super.close();
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}
