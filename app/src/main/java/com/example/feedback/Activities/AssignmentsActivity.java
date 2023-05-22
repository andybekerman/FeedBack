package com.example.feedback.Activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feedback.Adapters.AssignmentAdapter;
import com.example.feedback.DataClasses.Assignment;
import com.example.feedback.DataClasses.Group;
import com.example.feedback.DataClasses.User;
import com.example.feedback.R;
import com.example.feedback.Utilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * The assignments activity is the activity where all of the assignments and group options of a specific group are displayed
 */

public class AssignmentsActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener {

    RecyclerView recyclerView;
    AssignmentAdapter adapter;
    ArrayList<Assignment> assignmentList;

    SharedPreferences sharedPreferences;

    TextView tvGroupName;
    Button btnActionButton;

    FirebaseDatabase fb;
    DatabaseReference myRefGroups;

    Group currGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments);

        assignmentList = new ArrayList<>();
        fb = FirebaseDatabase.getInstance();
        myRefGroups = fb.getReference().child("Groups");

        tvGroupName = findViewById(R.id.tvGroupName);
        btnActionButton = findViewById(R.id.btnActionButton);

        sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);

        adapter = new AssignmentAdapter(assignmentList);
        recyclerView = findViewById(R.id.assignment_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateAssignmentList();

        recyclerView.addOnItemTouchListener(this);

        btnActionButton.setOnClickListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && e.getAction() == MotionEvent.ACTION_UP) {
            int position = rv.getChildAdapterPosition(childView);
            Assignment clickedAssignment = adapter.getAssignmentAtPosition(position);

            Intent intent = new Intent(this, AssignmentActivity.class);
            intent.putExtra("ASSIGNMENT_NAME", clickedAssignment.getName());
            intent.putExtra("GROUP_ID", getIntent().getStringExtra("GROUP_ID"));
            intent.putExtra("GROUP_LEADER", currGroup.getLeader().getUserId());
            intent.putExtra("ASSIGNMENT_UID", clickedAssignment.getUid());
            startActivity(intent);

            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    /**
     * This function finds a group by it's id then add all the groups assignments into a list
     */
    private void updateAssignmentList() {
        myRefGroups.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };
                boolean changed = false;
                if (snapshot.getValue(t) != null) {
                    ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));

                    for (Group g : groups) {
                        if (g.getGroupId().equals(getIntent().getStringExtra("GROUP_ID"))) {
                            currGroup = g;
                            if (g.getLeader().getUserId().equals(sharedPreferences.getString("UID", ""))) {
                                String leaderCrown = " \uD83D\uDC51";
                                tvGroupName.setText(g.getGroupName() + leaderCrown);
                            } else
                                tvGroupName.setText(g.getGroupName());

                            assignmentList.clear();
                            if (g.getAssignments() != null) {
                                boolean found;
                                for (String s : g.getAssignments().keySet()) {
                                    found = false;
                                    ArrayList<Assignment> assignmentArrayList = g.getAssignments().get(s);
                                    for (Assignment a : assignmentArrayList) {
                                        if (a.getUid().equals(sharedPreferences.getString("UID", ""))) {
                                            assignmentList.add(a);
                                            found = true;
                                        }
                                    }
                                    if (!found) {
                                        Assignment newAssignment = new Assignment(assignmentArrayList.get(0), sharedPreferences.getString("UID", ""), sharedPreferences.getString("NAME", ""));
                                        assignmentArrayList.add(newAssignment);
                                        assignmentList.add(newAssignment);

                                        changed = true;
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }
                    if (changed)
                        myRefGroups.setValue(groups);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == btnActionButton) {
            if (sharedPreferences.getString("UID", "").equals(currGroup.getLeader().getUserId())) {
                //if user is the group leader
                AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentsActivity.this);
                builder.setTitle("Group Options");
                builder.setItems(new CharSequence[]
                        {"Display & Copy Group ID", "Upload Assignment", "Delete Assignment", "Remove Member", "Rename Group", "Delete Group"}, (dialog, which) -> {
                            switch (which) {
                                case 0: {
                                    //Displays a toast message of the group id and copies the the id to the users clipboard

                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                                    // Create a new ClipData object to store the text to be copied
                                    ClipData clip = ClipData.newPlainText("Group Id", currGroup.getGroupId());

                                    // Set the ClipData object as the primary clip on the clipboard
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(AssignmentsActivity.this, currGroup.getGroupId(), Toast.LENGTH_LONG).show();
                                    Toast.makeText(AssignmentsActivity.this, "Group Id copied", Toast.LENGTH_SHORT).show();

                                    break;
                                }
                                case 1: {
                                    //Displays dialog to insert assignment name and description then calls the AddAssignment() function
                                    final Dialog dialog1 = new Dialog(AssignmentsActivity.this);
                                    dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog1.setContentView(R.layout.dialog);

                                    final EditText groupID1 = dialog1.findViewById(R.id.etField1);
                                    final EditText groupID2 = dialog1.findViewById(R.id.etField2);
                                    final ListView listView = dialog1.findViewById(R.id.listView);
                                    final Button save = dialog1.findViewById(R.id.btnSave);
                                    final TextView tvField = dialog1.findViewById(R.id.tvField);

                                    listView.setVisibility(View.GONE);
                                    groupID1.setHint("Assignment Name");
                                    groupID2.setHint("Assignment Description");
                                    tvField.setText("Enter assignment details:");

                                    dialog1.show();

                                    save.setOnClickListener(v1 -> {
                                        if (!groupID1.getText().toString().equals("") && !groupID2.getText().toString().equals("")) {
                                            Utilities.addAssignment(currGroup, groupID1.getText().toString(), groupID2.getText().toString());
                                            dialog1.dismiss();
                                        } else {
                                            Toast.makeText(AssignmentsActivity.this, "One or more of the fields isn't filled", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                }
                                case 2: {
                                    //Displays a list of all assignments and deletes the one that was clicked
                                    if (currGroup.getAssignments() != null && currGroup.getAssignments().size() >= 1) {
                                        final Dialog dialog1 = new Dialog(AssignmentsActivity.this);
                                        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog1.setContentView(R.layout.dialog);

                                        ArrayList<String> assignments = new ArrayList<>(currGroup.getAssignments().keySet());
                                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignmentsActivity.this, android.R.layout.simple_list_item_1, assignments);


                                        final TextView tvField = dialog1.findViewById(R.id.tvField);
                                        final EditText hide1 = dialog1.findViewById(R.id.etField1);
                                        final EditText hide2 = dialog1.findViewById(R.id.etField2);
                                        final Button save = dialog1.findViewById(R.id.btnSave);
                                        final ListView listView = dialog1.findViewById(R.id.listView);

                                        listView.setAdapter(adapter);

                                        tvField.setText("Pick Assignment To Delete");
                                        hide1.setVisibility(View.GONE);
                                        hide2.setVisibility(View.GONE);
                                        save.setVisibility(View.GONE);

                                        dialog1.show();

                                        listView.setOnItemClickListener((parent, view, position, id) -> {
                                            String assignment = assignments.get(position);
                                            Utilities.deleteAssignment(currGroup.getGroupId(),assignment);
                                            dialog1.dismiss();
                                        });
                                    } else
                                        Toast.makeText(AssignmentsActivity.this, "No Assignments Currently In Group", Toast.LENGTH_SHORT).show();

                                    break;
                                }
                                case 3: {
                                    //Displays dialog with list of group members and removes the one that was clicked
                                    if (currGroup.getGroupMembers() != null && currGroup.getGroupMembers().size() >= 1) {
                                        final Dialog dialog1 = new Dialog(AssignmentsActivity.this);
                                        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog1.setContentView(R.layout.dialog);

                                        final ArrayAdapter<User> adapter = new ArrayAdapter<>(AssignmentsActivity.this, android.R.layout.simple_list_item_1, currGroup.getGroupMembers());


                                        final TextView tvField = dialog1.findViewById(R.id.tvField);
                                        final EditText groupID1 = dialog1.findViewById(R.id.etField1);
                                        final EditText groupID2 = dialog1.findViewById(R.id.etField2);
                                        final Button save = dialog1.findViewById(R.id.btnSave);
                                        final ListView listView = dialog1.findViewById(R.id.listView);

                                        listView.setAdapter(adapter);

                                        tvField.setText("Pick Member To Remove");
                                        groupID1.setVisibility(View.GONE);
                                        groupID2.setVisibility(View.GONE);
                                        save.setVisibility(View.GONE);

                                        dialog1.show();

                                        listView.setOnItemClickListener((parent, view, position, id) -> {
                                            User user = currGroup.getGroupMembers().get(position);
                                            Utilities.removeMember(currGroup, user.getUserId());
                                            dialog1.dismiss();
                                        });
                                    } else
                                        Toast.makeText(AssignmentsActivity.this, "No Members Currently In Group", Toast.LENGTH_SHORT).show();

                                    break;
                                }
                                case 4: {
                                    //Displays dialog to insert new group name then the RenameGroup() function is called
                                    final Dialog dialog2 = new Dialog(AssignmentsActivity.this);
                                    dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog2.setContentView(R.layout.dialog);

                                    final TextView tvField = dialog2.findViewById(R.id.tvField);
                                    final EditText etGroupName = dialog2.findViewById(R.id.etField1);
                                    final EditText hide1 = dialog2.findViewById(R.id.etField2);
                                    final ListView hide2 = dialog2.findViewById(R.id.listView);
                                    final Button btnSave = dialog2.findViewById(R.id.btnSave);

                                    hide1.setVisibility(View.GONE);
                                    hide2.setVisibility(View.GONE);

                                    tvField.setText("Enter New Group Name:");
                                    etGroupName.setHint("Group Name");

                                    dialog2.show();

                                    btnSave.setOnClickListener(v1 -> {
                                        if (!etGroupName.getText().toString().equals("")) {
                                            Utilities.renameGroup(currGroup.getGroupId(), etGroupName.getText().toString());
                                            finish();
                                            dialog2.dismiss();
                                        }
                                    });

                                    break;
                                }
                                case 5: {
                                    Utilities.deleteGroup(currGroup);
                                    finish();
                                    break;
                                }

                            }
                        });
                builder.show();
            } else {
                //if user is group member
                AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentsActivity.this);
                builder.setTitle("Group Options");
                builder.setItems(new CharSequence[]{"Leave Group", "Member List"}, (dialog, which) -> {
                    if (which == 0) {
                        //Calls the LeaveGroup() function
                        Utilities.removeMember(currGroup, sharedPreferences.getString("UID", ""));
                        finish();
                    }

                    if (which == 1) {
                        //Displays a dialog with a list of all the group members
                        final Dialog dialog1 = new Dialog(AssignmentsActivity.this);
                        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog1.setContentView(R.layout.dialog);

                        final ArrayList<User> groupUsers = new ArrayList<>();
                        final String leaderCrown = " \uD83D\uDC51";
                        final User leader = new User(currGroup.getLeader().getFullName() + leaderCrown);
                        groupUsers.add(leader);
                        groupUsers.addAll(currGroup.getGroupMembers());

                        final ArrayAdapter<User> adapter = new ArrayAdapter<>(AssignmentsActivity.this, android.R.layout.simple_list_item_1, groupUsers);


                        final TextView tvField = dialog1.findViewById(R.id.tvField);
                        final EditText groupID1 = dialog1.findViewById(R.id.etField1);
                        final EditText groupID2 = dialog1.findViewById(R.id.etField2);
                        final Button save = dialog1.findViewById(R.id.btnSave);
                        final ListView listView = dialog1.findViewById(R.id.listView);

                        listView.setAdapter(adapter);

                        tvField.setText("Group Members");
                        groupID1.setVisibility(View.GONE);
                        groupID2.setVisibility(View.GONE);
                        save.setVisibility(View.GONE);

                        dialog1.show();
                    }
                });

                builder.show();
            }
        }
    }
}