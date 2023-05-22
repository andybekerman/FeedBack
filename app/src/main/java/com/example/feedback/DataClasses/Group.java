package com.example.feedback.DataClasses;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class Group {

    public User leader;
    public String groupName;
    private ArrayList<User> groupMembers;
    private String groupId;
    private HashMap<String, ArrayList<Assignment>> assignments;

    public Group(User leader, String groupName) {
        this.leader = leader;
        this.groupName = groupName;
        this.groupMembers = new ArrayList<>();
    }

    public Group() {
    }

    public void addMember(User user) {
        this.groupMembers.add(user);
    }

    /**
     * The function receives a user and removes him from group members list and deletes all his assignments from the assignments hashmap
     *
     * @param uid The user's id
     */
    public void RemoveMember(String uid) {

        for (User u : this.groupMembers)
            if (uid.equals(u.getUserId()))
                groupMembers.remove(u);

        if (this.assignments != null) {
            for (String s : this.assignments.keySet()) {
                for (Assignment a : this.assignments.get(s)) {
                    if (a.getUid().equals(uid)) {
                        if (a.getVideoID() != null)//removes any videos uploaded by user to group assignments
                            FirebaseStorage.getInstance().getReference().child("videos/" + a.getVideoID()).delete();

                        this.assignments.get(s).remove(a);
                    }
                }
            }
        }
    }

    /**
     * The function receives an assignment and adds it to the assignments hashmap
     *
     * @param a The assignment
     */
    public void addAssignment(Assignment a) {
        ArrayList<Assignment> arraylist = new ArrayList<Assignment>();
        arraylist.add(a);
        this.assignments.put(a.getName(), arraylist);
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public ArrayList<User> getGroupMembers() {
        return this.groupMembers;
    }

    public void setGroupMembers(ArrayList<User> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public HashMap<String, ArrayList<Assignment>> getAssignments() {
        return this.assignments;
    }

    public void setAssignments(HashMap<String, ArrayList<Assignment>> assignments) {
        this.assignments = assignments;
    }
}
