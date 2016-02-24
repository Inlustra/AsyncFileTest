import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Inherits from ObjectConsumer, will ensure that the file
 */
public class LogConsumer extends ObjectConsumer<LogMessage> {

    private static final int FILE_INTERVAL_MINUTES = 15;
    private static final long FILE_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(FILE_INTERVAL_MINUTES);

    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    private static final String FILE_EXTENSION = ".log";

    private static final Calendar CALENDAR = Calendar.getInstance();

    static {
        //Set the calendar to lenient to accept the set(Calendar.MINUTE, >60)
        CALENDAR.setLenient(true);
    }

    private final File parentDir;
    private final LogMessageWriter logWriter;

    private long lastFileCreate = -1;
    private FileWriter fileWriter;

    public LogConsumer(File parentDir, LogMessageWriter writer) {
        this.parentDir = parentDir;
        this.logWriter = writer;
    }

    /**
     * Creates a new LogConsumer
     *
     * @param parentDir the directory in which to create new files
     */
    public LogConsumer(File parentDir) {
        this(parentDir, new LogMessageWriter() {
        });
    }

    /**
     * Creates a new LogConsumer at the current working directory
     */
    public LogConsumer() {
        this(new File("").getAbsoluteFile());
    }

    public void log(String string) {
        this.add(new LogMessage(string));
    }


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
     * Checks to see if a new file should be created to handle
     */
    private void checkWriter() {
        if (newFileRequired()) {
            try {
                File file = createFile();
                this.fileWriter = new FileWriter(file);
                this.lastFileCreate = System.currentTimeMillis();
            } catch (IOException e) {
                System.err.println("File could not be created, defaulting to previous file.");
                e.printStackTrace();
                if (fileWriter == null) {
                    // If we couldn't create a new file and didn't
                    // initially have one, throw an exception to kill the thread
                    throw new NullPointerException("Could not create initial file for LogConsumer");
                }
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
        String date = FILE_NAME_FORMAT.format(roundCalender());
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
        CALENDAR.set(Calendar.MINUTE, unroundedMinutes + mod);
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
