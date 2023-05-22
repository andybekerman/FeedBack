package com.example.feedback.Activities;


import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.feedback.DataClasses.Assignment;
import com.example.feedback.DataClasses.Comment;
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
import java.util.Comparator;
import java.util.Objects;

/**
 * The video viewer activity is where a user can view an uploaded video and a group leader can write comments about it and a group member can view those comments
 */

public class VideoViewerActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    SharedPreferences sharedPreferences;

    FirebaseDatabase fb;
    DatabaseReference myRefUsers, myRefGroups, myRefSimulations;
    FirebaseStorage storage;
    StorageReference videoRef;

    ImageView ivBack, ivOptions;
    TextView tvAssignmentName;
    VideoView videoView;
    ListView listView;
    EditText etInput;
    Button btnSubmit, btnSimulation1, btnSimulation2;
    LinearLayout btnLayout;

    MediaController mMediaController;
    ArrayAdapter<Comment> adapter;
    ArrayList<Comment> comments;
    Group currGroup;
    Uri videoUri;

    long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_viewer);

        storage = FirebaseStorage.getInstance();
        videoRef = storage.getReference().child("videos/" + getIntent().getStringExtra("VIDEO_ID"));
        fb = FirebaseDatabase.getInstance();
        myRefGroups = fb.getReference("Groups");
        myRefUsers = fb.getReference("Users");
        myRefSimulations = fb.getReference("Simulation");

        ivBack = findViewById(R.id.ivBack);
        ivOptions = findViewById(R.id.ivOptions);
        tvAssignmentName = findViewById(R.id.tvAssignmentName);
        videoView = findViewById(R.id.videoView);
        listView = findViewById(R.id.listView);
        etInput = findViewById(R.id.etInput);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSimulation1 = findViewById(R.id.btnSimulation1);
        btnSimulation2 = findViewById(R.id.btnSimulation2);
        btnLayout = findViewById(R.id.btnLayout);

        sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);

        if (getIntent().getStringExtra("ASSIGNMENT_UID").equals(sharedPreferences.getString("UID", ""))) {
            etInput.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.GONE);
        }

        comments = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comments);
        listView.setAdapter(adapter);
        mMediaController = new MediaController(this);
        videoView.setMediaController(mMediaController);
        mMediaController.setAnchorView(videoView);

        tvAssignmentName.setText(getIntent().getStringExtra("ASSIGNMENT_NAME"));

        retrieveAssignmentInfo();

        btnSubmit.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivOptions.setOnClickListener(this);
        listView.setOnItemClickListener(this);

        btnSimulation1.setOnClickListener(this::setSimulation);
        btnSimulation2.setOnClickListener(this::setSimulation);
    }


    /**
     * When one of the simulation buttons is pressed, the function sends the firebase a simulation code
     * @param v The button view
     */
    private void setSimulation(View v)
    {
        if(v == btnSimulation1)
            myRefSimulations.setValue("Simulation1");

        if(v == btnSimulation2)
            myRefSimulations.setValue("Simulation2");
    }

    /**
     * The function downloads the video that was uploaded for the assignment, in the case a video wasn't upload, a timer button is displayed for the simulation mode, and retrieves the comments
     */
    private void retrieveAssignmentInfo() {
        if(getIntent().getStringExtra("VIDEO_ID") != null){
            videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                videoUri = uri;
                videoView.setVideoURI(videoUri);
                videoView.start();
                btnLayout.setVisibility(View.GONE);
            });
        }
        else
        {
            Toast.makeText(VideoViewerActivity.this, "Video Not Found", Toast.LENGTH_SHORT).show();
            if (VideoViewerActivity.this.getIntent().getStringExtra("GROUP_LEADER_UID").equals(sharedPreferences.getString("UID", ""))) {
                Toast.makeText(VideoViewerActivity.this, "Simulation Mode", Toast.LENGTH_LONG).show();
                btnSubmit.setText("Start Timer");
                videoView.setVisibility(View.GONE);
            }
        }

        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };
                if (snapshot.getValue(t) != null) {
                    ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));

                    for (Group g : groups) {
                        if (g.getGroupId().equals(getIntent().getStringExtra("GROUP_ID"))) {
                            currGroup = g;
                            if (g.getAssignments() != null && g.getAssignments().size() > 0) {
                                for (String s : g.getAssignments().keySet()) {
                                    if (s.equals(getIntent().getStringExtra("ASSIGNMENT_NAME")) && g.getAssignments().get(s) != null) {
                                        for (Assignment assignment : g.getAssignments().get(s)) {
                                            if (assignment.getUid().equals(getIntent().getStringExtra("ASSIGNMENT_UID")) && assignment.getComments() != null && assignment.getComments().size() > 0) {
                                                comments.addAll(assignment.getComments());
                                                adapter.notifyDataSetChanged();
                                                break;
                                            }
                                        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {

        if (v == btnSubmit) { // checks if in simulation mode
            if (btnSubmit.getText().toString().equals("Start Timer")) {
                startTime = System.currentTimeMillis();
                btnSubmit.setText("Submit");
            } else if (btnSubmit.getText().toString().equals("Submit")) {
                if (!etInput.getText().toString().equals("")) {

                    if (videoView.getDuration() != -1) {
                        //when comment is sent the timestamp of the video is added to the comment and then comments are sorted by the timestamps
                        Comparator<Comment> sortByTimestamp = (Comment c1, Comment c2) -> Long.compare(c1.getTimeStamp(), c2.getTimeStamp());

                        comments.add(new Comment(etInput.getText().toString(), videoView.getCurrentPosition()));
                        comments.sort(sortByTimestamp);

                    } else if (startTime != 0) {
                        comments.add(new Comment(etInput.getText().toString(), System.currentTimeMillis() - startTime));
                    }
                    adapter.notifyDataSetChanged();
                    etInput.setText("");
                }
            }
        }
        if (v == ivBack) {
            //when the back button is click, the comments that were written in the video are saved to the database
            if (sharedPreferences.getString("UID", "").equals(getIntent().getStringExtra("GROUP_LEADER_UID"))) {
                myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                        };
                        if (snapshot.getValue(t) != null) {
                            ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));

                            for (Group g : groups) {
                                if (g.getGroupId().equals(currGroup.getGroupId())) {
                                    for (String s : g.getAssignments().keySet()) {
                                        if (s.equals(getIntent().getStringExtra("ASSIGNMENT_NAME"))) {
                                            for (Assignment a : Objects.requireNonNull(g.getAssignments().get(s))) {
                                                if (a.getUid().equals(getIntent().getStringExtra("ASSIGNMENT_UID"))) {
                                                    a.setComments(comments);
                                                    myRefGroups.setValue(groups);
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
            }
            finish();
        }

        if (v == ivOptions) {
            // allows user or group leader to delete uploaded video

            AlertDialog.Builder builder = new AlertDialog.Builder(VideoViewerActivity.this);
            builder.setTitle("Options:");
            builder.setItems(new CharSequence[]{"Delete Video"}, (dialog, which) -> {

                if (videoUri != null) {
                    videoRef.delete().addOnCompleteListener(task -> {
                        Toast.makeText(VideoViewerActivity.this, "Deleted from storage", Toast.LENGTH_SHORT).show();
                        myRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                                };

                                if (snapshot.getValue(t) != null) {
                                    ArrayList<Group> groups = new ArrayList<>(snapshot.getValue(t));
                                    Group g = groups.stream().filter(group -> group.getGroupId().equals(getIntent().getStringExtra("GROUP_ID"))).findFirst().orElse(null);


                                    if (g != null) {
                                        ArrayList<Assignment> assignmentArrayList = g.getAssignments().get(getIntent().getStringExtra("ASSIGNMENT_NAME"));

                                        if (assignmentArrayList != null) {
                                            Assignment a = assignmentArrayList.stream().filter(assignment -> assignment.getUid().equals(getIntent().getStringExtra("ASSIGNMENT_UID"))).findFirst().orElse(null);

                                            if (a != null) {
                                                a.setVideoID(null);
                                                a.setComments(null);
                                                myRefGroups.setValue(groups);
                                                finish();
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }).addOnFailureListener(e ->
                            Toast.makeText(VideoViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                } else
                    Toast.makeText(VideoViewerActivity.this, "No Video Exists", Toast.LENGTH_SHORT).show();
            });
            builder.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //when group leader clicks on comment two options appear, goes to the time stamp, and delete comment
        //when user group member clicks on the comment, goes to time stamp
        if (getIntent().getStringExtra("GROUP_LEADER_UID").equals(sharedPreferences.getString("UID", ""))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Options")
                    .setItems(new String[]{"Time Stamp", "Delete Comment"}, (dialogInterface, i) -> {
                        if (i == 0) {
                            Comment comment = comments.get(position);
                            videoView.seekTo((int) comment.getTimeStamp());
                        } else if (i == 1) {
                            Comparator<Comment> sortByTimestamp = (Comment c1, Comment c2) -> Long.compare(c1.getTimeStamp(), c2.getTimeStamp());

                            comments.remove(position);
                            comments.sort(sortByTimestamp);
                            adapter.notifyDataSetChanged();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Comment comment = comments.get(position);
            videoView.seekTo((int) comment.getTimeStamp());
        }
    }
}