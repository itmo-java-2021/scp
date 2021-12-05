package scp;

import java.io.Serializable;

public class Message implements Serializable {
    private final String request;
    private final String body;

    public String getBody() {
        return body;
    }

    public String getRequest() {
        return request;
    }


    public Message(String request, String body) {
        this.request = request;
        this.body = body;
    }

}
