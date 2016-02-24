import java.util.Random;

/**
 * Created by Thomas Nairn on 24/02/2016.
 */
public class Main {
    public static void main(String[] args) {

        LogConsumer consumer = new LogConsumer();
        Thread thread = new Thread(consumer);
        thread.start();
        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            consumer.log(random.nextInt() + " ");
        }
    }
}
