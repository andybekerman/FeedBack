package com.example.feedback.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.feedback.DataClasses.Assignment;
import com.example.feedback.DataClasses.Group;
import com.example.feedback.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * The assignment activity is the activity where all of a specific assignments information is displayed
 */

public class AssignmentActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final int CAMERA_PERMISSION = 101;
    private final int GALLERY_PERMISSION = 100;

    FirebaseDatabase fb;
    DatabaseReference myRefGroups;
    FirebaseStorage firebaseStorage;
    StorageReference myRefVideos;

    TextView tvAssignmentName, tvAssignmentDate, tvAssignmentDescription, tvVideoView;
    Button btnVideoPage;
    ListView listView;

    ArrayList<Group> groups;
    Group group;
    Assignment assignment;
    ArrayList<Assignment> assignmentArrayList;
    ArrayAdapter<Assignment> adapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        firebaseStorage = FirebaseStorage.getInstance();
        myRefVideos = firebaseStorage.getReference().child("videos/");
        fb = FirebaseDatabase.getInstance();
        myRefGroups = fb.getReference("Groups");

        tvVideoView = findViewById(R.id.tvVideoView);
        tvAssignmentName = findViewById(R.id.tvAssignmentName);
        tvAssignmentDate = findViewById(R.id.tvAssignmentDate);
        tvAssignmentDescription = findViewById(R.id.tvAssignmentDescription);
        btnVideoPage = findViewById(R.id.btnVideoPage);
        listView = findViewById(R.id.listView);

        sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        assignment = new Assignment();
        assignmentArrayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, assignmentArrayList);

        listView.setAdapter(adapter);

        //if user is not group leader
        if (!sharedPreferences.getString("UID", "").equals(getIntent().getStringExtra("GROUP_LEADER"))) {
            listView.setVisibility(View.GONE);
        } else {
            tvVideoView.setVisibility(View.GONE);
            btnVideoPage.setVisibility(View.GONE);
        }

        getAssignmentInfo();

        tvAssignmentName.setText(assignment.getName());
        tvAssignmentDate.setText(assignment.getDate());
        tvAssignmentDescription.setText(assignment.getDescription());

        tvAssignmentName.setOnClickListener(this);

        btnVideoPage.setOnClickListener(this);
        tvVideoView.setOnClickListener(this);

        listView.setOnItemClickListener(this);
    }

    /**
     * The function find all of the assignments information that is relative for the user to know and displays it
     */

    public void getAssignmentInfo() {
        String assignmentName = getIntent().getStringExtra("ASSIGNMENT_NAME");

        myRefGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };
                Assignment assignment0;

                if (snapshot.getValue(t) != null) {
                    groups = new ArrayList<>();
                    groups.addAll(snapshot.getValue(t));

                    for (Group g : groups) {
                        if (g.getGroupId().equals(getIntent().getStringExtra("GROUP_ID"))) { //finds group
                            group = g;
                            if (g.getAssignments() != null) {
                                for (String s : g.getAssignments().keySet()) {
                                    if (s.equals(assignmentName)) { //finds assignment
                                        assignment0 = g.getAssignments().get(s).get(0);

                                        tvAssignmentName.setText(s);
                                        tvAssignmentDate.setText(assignment0.getDate());
                                        tvAssignmentDescription.setText(assignment0.getDescription());

                                        //if user is group leader, then show all member's videos
                                        if (sharedPreferences.getString("UID", "").equals(getIntent().getStringExtra("GROUP_LEADER"))) {
                                            if (g.getAssignments().get(s) != null) {
                                                for (Assignment assignment : g.getAssignments().get(s))
                                                    if (!assignment.getUid().equals(sharedPreferences.getString("UID", "")))
                                                        AssignmentActivity.this.assignmentArrayList.add(assignment);

                                                AssignmentActivity.this.adapter.notifyDataSetChanged();
                                            } else {
                                                listView.setVisibility(View.GONE);
                                            }
                                        } else { // if not group leader, then show only current user's video if one was uploaded
                                            for (Assignment assign : group.getAssignments().get(s)) {
                                                if (assign.getUid() != null && assign.getUid().equals(sharedPreferences.getString("UID", ""))) {
                                                    //if user has uploaded a video, show button to redirect to video view
                                                    if (assign.getVideoID() == null)
                                                        tvVideoView.setVisibility(View.GONE);
                                                    else
                                                        tvVideoView.setVisibility(View.VISIBLE);

                                                    assignment = assign;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
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

    @Override
    public void onClick(View v) {
        if (v == btnVideoPage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentActivity.this);
            builder.setTitle("Pick Your Poison:");
            builder.setItems(new CharSequence[]{"Record Video", "Upload Video"}, (dialog, which) -> {
                switch (which) {

                    case 0:
                        Intent intent1 = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        if (ContextCompat.checkSelfPermission(AssignmentActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(AssignmentActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                        } else {
                            startActivityForResult(intent1, 1);
                        }
                        break;

                    case 1:
                        Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        intent2.setType("video/*");
                        if (ContextCompat.checkSelfPermission(AssignmentActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(AssignmentActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
                        } else {
                            startActivityForResult(intent2, 2);
                        }
                        break;
                }
            });

            builder.show();
        }

        if (v == tvVideoView) {
            Intent intent = new Intent(this, VideoViewerActivity.class);
            intent.putExtra("GROUP_ID", group.getGroupId());
            intent.putExtra("GROUP_LEADER_UID", group.getLeader().getUserId());
            intent.putExtra("ASSIGNMENT_UID", assignment.getUid());
            intent.putExtra("VIDEO_ID", assignment.getVideoID());
            intent.putExtra("ASSIGNMENT_NAME", assignment.getName());
            startActivity(intent);
        }
    }

    /**
     * The function handles when a user uploads a video. The function saves the video in the user's assignment and in the case that a video is already uploaded to that assignment, the old video and comments are deleted
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (data != null && data.getData() != null) {
            String key = myRefGroups.push().getKey();
            myRefVideos.child(key).putFile(data.getData()).addOnCompleteListener(task -> {
                myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                        };
                        if (snapshot.getValue(t) != null) {
                            ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));

                            for (Group g : groups) {
                                if (g.getGroupId().equals(group.getGroupId())) {// if groups is found
                                    for (String s : g.getAssignments().keySet()) {
                                        if (assignment.getName().equals(s)) { //if assignment is found
                                            for (Assignment a : g.getAssignments().get(s)) {
                                                if (a.getUid().equals(assignment.getUid())) { //if user's assignment is found
                                                    if (a.getVideoID() != null) { // deletes video and comments in case new video is uploaded and old video exists
                                                        a.getComments().clear();
                                                        myRefVideos.child(a.getVideoID()).delete().addOnCompleteListener(task1 -> Toast.makeText(AssignmentActivity.this, "previous video deleted successfully", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(AssignmentActivity.this, "previous video not deleted successfully", Toast.LENGTH_SHORT).show());
                                                    }
                                                    a.setVideoID(key);
                                                    assignment.setVideoID(key);
                                                    assignment.setUserName(sharedPreferences.getString("NAME", ""));
                                                    myRefGroups.setValue(groups);
                                                    tvVideoView.setVisibility(View.VISIBLE);
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Toast.makeText(AssignmentActivity.this, "Video Upload Complete", Toast.LENGTH_SHORT).show();

            }).addOnFailureListener(e -> Toast.makeText(AssignmentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * The function handles permission requests from the app when the camera or gallery are trying to be accessed
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent2.setType("video/*");
                startActivityForResult(intent2, 2);

            } else {
                // Permission denied, so show a toast message
                Toast.makeText(this, "Permission denied. Cannot access video gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Assignment assign = assignmentArrayList.get(position);
        Intent intent = new Intent(this, VideoViewerActivity.class);
        intent.putExtra("GROUP_ID", group.getGroupId());
        intent.putExtra("GROUP_LEADER_UID", group.getLeader().getUserId());
        intent.putExtra("ASSIGNMENT_UID", assign.getUid());
        intent.putExtra("VIDEO_ID", assign.getVideoID());
        intent.putExtra("ASSIGNMENT_NAME", assign.getName());
        startActivity(intent);
    }


}