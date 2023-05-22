package com.example.feedback.DataClasses;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * An assignment is an object that holds general parameters of a specific assignment including name, description, date of posting.
 * An assignment also stores a specific group member's information, an id for the video the user uploaded, name, user id, a list of comments that were written about the video.
 */
public class Assignment {
    public String name;
    public String description;
    public String videoID;
    public ArrayList<Comment> comments;
    public String date;
    public String uid;//user's id
    public String userName;//user's full name

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Assignment(String name, String description) {
        this.name = name;
        this.description = description;
        this.date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        uid = null;
        videoID = null;
        comments = null;
    }

    public Assignment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Assignment(Assignment a, String uid, String userName) {
        this.name = a.getName();
        this.description = a.getDescription();
        this.date = a.getDate();
        this.uid = uid;
        videoID = null;
        comments = null;
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void onGetName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.userName;
    }

}
