import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by Thomas Nairn on 24/02/2016.
 */
public interface LogMessageWriter {

    SimpleDateFormat defaultFormat = new SimpleDateFormat();

    default void write(FileWriter writer, LogMessage message) throws IOException { //Took the /? from Android ADB
        writer.write(defaultFormat.format(message.getDate()) + "/? " + message.getMessage() + "\n");
    }

}
