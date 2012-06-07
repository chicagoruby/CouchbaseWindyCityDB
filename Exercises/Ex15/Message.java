/**
 *
 * @author rags
 */
public class Message {
    public String userId;
    public String message;
    public String type = "Message";
    public long date;

    public Message(String userId, String message) {
        super();
        this.userId = userId;
        this.message = message;
        date = System.currentTimeMillis();
    }
}
