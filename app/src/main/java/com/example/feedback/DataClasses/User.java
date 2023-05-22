package com.example.feedback.DataClasses;

import java.util.ArrayList;

/**
 * A user represents a collection of information about a specific user including, name, username, user id, and list of what groups the user belongs to.
 */

public class User {
    public String username;
    private String fullName;
    private String userId;
    public ArrayList<String> groupIDs;

    public User(String fullName, String userId) {
        this.fullName = fullName;
        this.userId = userId;
        this.username = "";
        this.groupIDs = new ArrayList<>();
    }

    public User(String fullName)
    {
        this.fullName = fullName;
    }

    public User() {
    }

    /**
     * The function receives a group and removes it from the user's group list
     * @param group The group
     */
    public void removeGroupId(Group group)
    {
        if(this.groupIDs != null && this.groupIDs.contains(group.getGroupId()))
            this.groupIDs.remove(group.getGroupId());
    }

    public void addGroupID(String groupID)
    {
        this.groupIDs.add(groupID);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<String> getGroupIDs() {
        return groupIDs;
    }

    public void setGroupIDs(ArrayList<String> groupIDs) {
        this.groupIDs = groupIDs;
    }

    @Override
    public String toString() {
        return this.fullName;
    }
}
