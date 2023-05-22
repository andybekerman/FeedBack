package com.example.feedback.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feedback.Adapters.GroupAdapter;
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
 * The groups activity is the activity where are the groups a user belongs to are displayed
 */

public class GroupsActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener {

    FirebaseDatabase fb;
    DatabaseReference myRefGroups;

    RecyclerView recyclerView;

    GroupAdapter adapter;
    ArrayList<Group> groupList;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        fb = FirebaseDatabase.getInstance();
        myRefGroups = fb.getReference().child("Groups");

        recyclerView = findViewById(R.id.group_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        adapter = new GroupAdapter(groupList);
        recyclerView.setAdapter(adapter);

        updateGroupList();

        recyclerView.addOnItemTouchListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && e.getAction() == MotionEvent.ACTION_UP) {
            int position = rv.getChildAdapterPosition(childView);
            Group clickedGroup = adapter.getGroupAtPosition(position);

            // Handle the item click here

            Intent intent = new Intent(this, AssignmentsActivity.class);
            intent.putExtra("GROUP_ID", clickedGroup.getGroupId());
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && e.getAction() == MotionEvent.ACTION_UP) {
        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.profile: {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivityForResult(intent, 0);
                return true;
            }


            case R.id.createGroup: {
                final Dialog dialog = new Dialog(GroupsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog);

                final TextView tvField = dialog.findViewById(R.id.tvField);
                final EditText etGroupName = dialog.findViewById(R.id.etField1);
                final EditText hide1 = dialog.findViewById(R.id.etField2);
                final ListView hide2 = dialog.findViewById(R.id.listView);
                final Button save = dialog.findViewById(R.id.btnSave);

                hide1.setVisibility(View.GONE);
                hide2.setVisibility(View.GONE);

                tvField.setText("Enter a group name: ");
                etGroupName.setHint("Group Name");

                dialog.show();

                save.setOnClickListener(v -> {
                    if (etGroupName.getText().toString() != "") {
                        Utilities.createGroup(GroupsActivity.this, etGroupName.getText().toString());
                        dialog.dismiss();
                    } else {
                        Toast.makeText(GroupsActivity.this, "Missing Group Name", Toast.LENGTH_SHORT).show();
                    }
                });

                return true;
            }

            case R.id.joinGroup: {
                final Dialog dialog = new Dialog(GroupsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog);

                final TextView tvField = dialog.findViewById(R.id.tvField);
                final EditText etGroupId = dialog.findViewById(R.id.etField1);
                final EditText hide1 = dialog.findViewById(R.id.etField2);
                final ListView hide2 = dialog.findViewById(R.id.listView);
                final Button save = dialog.findViewById(R.id.btnSave);

                hide1.setVisibility(View.GONE);
                hide2.setVisibility(View.GONE);

                tvField.setText("Enter a group id: ");
                etGroupId.setHint("Group Id");

                dialog.show();

                save.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {


                        if (!etGroupId.getText().toString().equals("")) {
                            final boolean result = groupList.stream().anyMatch(g -> g.getGroupId().equals(etGroupId.getText().toString()));

                            if (!result) {
                                Utilities.joinGroup(GroupsActivity.this, etGroupId.getText().toString());
                                dialog.dismiss();
                            } else {
                                Toast.makeText(GroupsActivity.this, "Already member of this group", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This function finds the groups the user belongs to or owns and adds them to a list which is displayed
     */
    private void updateGroupList() {

        myRefGroups.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };

                ArrayList<Group> groups = new ArrayList<>();

                if (snapshot.getValue(t) != null) {
                    groups.addAll(snapshot.getValue(t));
                }

                groupList.clear();

                for (Group g : groups) {
                    if (g.getGroupMembers() != null) {
                        for (User u : g.getGroupMembers()) {
                            if (u.getUserId().equals(sharedPreferences.getString("UID", "")) || g.getLeader().getUserId().equals(sharedPreferences.getString("UID", ""))) {
                                groupList.add(g);
                                break;
                            }
                        }
                    } else if (g.getLeader() != null && g.getLeader().getUserId().equals(sharedPreferences.getString("UID", ""))) {
                        groupList.add(g);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}