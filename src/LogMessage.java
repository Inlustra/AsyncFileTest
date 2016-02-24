import java.util.Date;

/**
 * Created by Thomas Nairn on 24/02/2016.
 */
public class LogMessage {

    private Date date;
    private String message;

    public LogMessage(String message) {
        this.date = new Date();
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }
}
