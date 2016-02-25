package com.thenairn.asynctest.consumer.Log;

import com.thenairn.asynctest.entity.LogMessage;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

/**
 * Created by Thomas Nairn on 24/02/2016.
 */
public interface LogMessageWriter {

    SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    default void write(Writer writer, LogMessage message) throws IOException { //Took the /? from Android ADB
        writer.write(defaultFormat.format(message.getDate()) + "/? " + message.getMessage() + "\n");
    }

}
