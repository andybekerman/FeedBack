package com.example.feedback.DataClasses;

import java.util.concurrent.TimeUnit;

/**
 * A comment represents a text and a timestamp (of the exact moment in the video when the comment was posted)
 */

public class Comment {

    String text;
    long timeStamp;

    public Comment(String text, long timeStamp) {
        this.text = text;
        this.timeStamp = timeStamp;
    }

    public Comment() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return text + " [" +  TimeUnit.MILLISECONDS.toSeconds(timeStamp) + "sec]";
    }
}
