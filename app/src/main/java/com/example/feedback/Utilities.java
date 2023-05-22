package com.example.feedback;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.feedback.DataClasses.Assignment;
import com.example.feedback.DataClasses.Group;
import com.example.feedback.DataClasses.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Utilities {

    /**
     * The Utilities class is a class that contains all the functions that a group leader or group member use to operate features in groups and assignments
     */

    private static final FirebaseDatabase fb = FirebaseDatabase.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final DatabaseReference myRefUsers = fb.getReference().child("Users"), myRefGroups = fb.getReference().child("Groups");
    private static final StorageReference myRefVideos = storage.getReference().child("videos/");

    /**
     * The function create a new group and adds group id to users list of groups in database
     *
     * @param context   The activity from where the function was called
     * @param groupName The group's name that the user has chosen
     */
    public static void createGroup(Context context, String groupName) {

        myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                SharedPreferences sharedPreferences = context.getSharedPreferences("USER_DETAILS", MODE_PRIVATE);

                Group group = new Group();
                GenericTypeIndicator<ArrayList<User>> t = new GenericTypeIndicator<ArrayList<User>>() {
                };

                if (snapshot.getValue(t) != null) {

                    ArrayList<User> users = new ArrayList<>(snapshot.getValue(t));

                    DatabaseReference newGroupRef = myRefGroups.push();


                    for (User u : users) {
                        if (u.getUserId().equals(sharedPreferences.getString("UID", ""))) {

                            group.setLeader(u);
                            group.setGroupName(groupName);
                            group.setGroupId(newGroupRef.getKey());

                            //adds group to list of groups in database

                            myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                                    };
                                    ArrayList<Group> groups = new ArrayList<>();

                                    if (snapshot.getValue(t) != null) {
                                        groups.addAll(snapshot.getValue(t));
                                    }
                                    groups.add(group);
                                    myRefGroups.setValue(groups);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            if (u.getGroupIDs() != null) {
                                u.addGroupID(group.getGroupId());
                            } else {
                                ArrayList<String> groupIDs = new ArrayList<>();
                                groupIDs.add(group.getGroupId());
                                u.setGroupIDs(groupIDs);
                            }
                            myRefUsers.setValue(users);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * The function adds a user into a group's member list and add the group id into the user's group id list
     *
     * @param context The activity from where the function was called
     * @param groupID The group's group id that the user wants to join
     */

    public static void joinGroup(Context context, String groupID) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("USER_DETAILS", MODE_PRIVATE);

        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Group group = null;
                boolean groupFound = false;

                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };

                if (snapshot.getValue(t) != null) {
                    ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));

                    for (Group g : groups) {
                        if (g.getGroupId().equals(groupID)) {

                            group = g;
                            groupFound = true;

                            break;
                        }
                    }

                    if (groupFound) {

                        Group joinGroup = group;

                        myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                GenericTypeIndicator<ArrayList<User>> t = new GenericTypeIndicator<ArrayList<User>>() {
                                };

                                if (snapshot.getValue(t) != null) {

                                    ArrayList<User> users = new ArrayList<>(snapshot.getValue(t));

                                    for (User u : users) {
                                        {
                                            if (u.getUserId().equals(sharedPreferences.getString("UID", ""))) {
                                                if (u.getGroupIDs() != null) {
                                                    u.addGroupID(joinGroup.getGroupId());

                                                } else {
                                                    ArrayList<String> groupIDs = new ArrayList<>();

                                                    groupIDs.add(joinGroup.getGroupId());
                                                    u.setGroupIDs(groupIDs);
                                                }

                                                myRefUsers.setValue(users);

                                                if (joinGroup.getGroupMembers() != null)
                                                    joinGroup.addMember(u);
                                                else {
                                                    ArrayList<User> groupMembers = new ArrayList<>();
                                                    groupMembers.add(u);
                                                    joinGroup.setGroupMembers(groupMembers);
                                                }

                                                myRefGroups.setValue(groups);
                                                break;
                                            }
                                        }
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else
                        Toast.makeText(context, "Group ID Incorrect", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(context, "No Groups Exists", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    /**
     * The function adds a new assignment into the assignments list of a specified group
     *
     * @param group          The group that the assignment is added to
     * @param assignmentName The name of the assignment
     * @param description    The description of the assignment
     */
    public static void addAssignment(Group group, String assignmentName, String description) {
        ArrayList<Group> groups = new ArrayList<>();

        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };

                if (snapshot.getValue(t) != null) {
                    groups.addAll(snapshot.getValue(t));

                    for (Group g : groups) {
                        if (g.getGroupId().equals(group.getGroupId())) {
                            if (g.getAssignments() != null) {
                                Assignment assignment = new Assignment(assignmentName, description);
                                assignment.setUid(group.getLeader().getUserId());
                                g.addAssignment(assignment);
                            } else {
                                HashMap<String, ArrayList<Assignment>> hashMap = new HashMap<>();
                                ArrayList<Assignment> arrayList = new ArrayList<>();

                                Assignment assignment = new Assignment(assignmentName, description);
                                assignment.setUid(group.getLeader().getUserId());

                                arrayList.add(assignment);
                                hashMap.put(assignmentName, arrayList);
                                g.setAssignments(hashMap);
                            }
                            myRefGroups.setValue(groups);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * The function removes a specified user from a group's member list and removes the group from the user's group list
     *
     * @param group The group
     * @param uid  The user's id
     */
    public static void removeMember(Group group, String uid) {

        ArrayList<User> users = new ArrayList<>();

        myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<User>> t = new GenericTypeIndicator<ArrayList<User>>() {
                };

                if (snapshot.getValue(t) != null) {
                    users.addAll(snapshot.getValue(t));

                    for (User u : users) {
                        if (u.getUserId().equals(uid)){
                            u.removeGroupId(group);
                            myRefUsers.setValue(users);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ArrayList<Group> groups = new ArrayList<>();

        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };

                if (snapshot.getValue(t) != null) {
                    groups.addAll(snapshot.getValue(t));

                    for (Group g : groups) {
                        if (g.getGroupId().equals(group.getGroupId())) {
                            g.RemoveMember(uid);
                            myRefGroups.setValue(groups);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * This function receives a group and removes it from the list of groups in the database, removes the group from all the users group lists, deletes all the videos that were uploaded to the storage by users in the group
     * @param group The group
     */
    public static void deleteGroup(Group group) {
        ArrayList<Group> groups = new ArrayList<>();

        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };
                if (snapshot.getValue(t) != null) {
                    groups.addAll(snapshot.getValue(t));
                    for (Group g : groups) {
                        if (g.getGroupId().equals(group.getGroupId())) {

                            //removes any videos uploaded by users to group assignments
                            if (g.getAssignments() != null) {
                                for (String s : g.getAssignments().keySet()) {
                                    for (Assignment a : g.getAssignments().get(s)) {
                                        if (a.getVideoID() != null)
                                            myRefVideos.child(a.getVideoID()).delete();
                                    }
                                }
                            }

                            groups.remove(g);
                            break;
                        }
                    }
                    myRefGroups.setValue(groups);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ArrayList<User> users = new ArrayList<>();

        myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<User>> t = new GenericTypeIndicator<ArrayList<User>>() {
                };

                if (snapshot.getValue(t) != null) {
                    users.addAll(snapshot.getValue(t));

                    for (User u : users) {
                        u.removeGroupId(group);
                    }
                    myRefUsers.setValue(users);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * The function receives a group id and a new group name and changes the group that matches the group id's name
     * @param gId The group id
     * @param groupName The new group name
     */
    public static void renameGroup(String gId, String groupName) {
        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };
                if (snapshot.getValue(t) != null) {
                    ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));
                    for (Group g : groups) {
                        if (g.getGroupId().equals(gId)) {
                            g.setGroupName(groupName);
                            myRefGroups.setValue(groups);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * The function receives a group id and an assignment name and deletes the assignment from the specified group and all the videos saved to that assignment
     * @param gId The group id
     * @param assignment The group name
     */
    public static void deleteAssignment(String gId, String assignment)
    {
        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };
                if(snapshot.getValue(t) != null)
                {
                    ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));
                    for(Group g : groups)
                    {
                        if(g.getGroupId().equals(gId))
                        {
                            for (Assignment a: g.getAssignments().get(assignment))
                            {
                                if(a.getVideoID() != null)
                                    myRefVideos.child(a.getVideoID()).delete();
                            }
                            g.getAssignments().remove(assignment);
                            myRefGroups.setValue(groups);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
