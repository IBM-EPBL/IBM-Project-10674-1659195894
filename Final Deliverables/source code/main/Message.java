package bigbrother.child_monitoring_system;

import java.util.ArrayList;

/**
 * Created by Sofanit on 3/25/2017.
 */

public class Message {

    private ArrayList<String> tokens;
    private String messageTitle;
    private String messageBody;

    public Message (ArrayList<String> tokens, String title, String body){
        this.tokens = tokens;
        messageTitle = title;
        messageBody = body;

    }

    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }
    public void setMessageTitle(String title) {
        messageTitle = title;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageBody(String body) {
        messageBody = body;
    }

    public String getMessageBody() {
        return messageBody;
    }

}