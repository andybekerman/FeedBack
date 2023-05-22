package com.example.feedback;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.feedback.Activities.LoginActivity;
import com.example.feedback.DataClasses.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * The notification service is a class that sends notifications to users when a new assignment is added to one of the groups they belong to
 */
public class NotificationService extends Service {

    private static final String NOTIFICATION_CHANNEL_ID = "notification_channel";
    private static int notificationId = 1;
    FirebaseDatabase db;
    private static DatabaseReference myRefGroups;
    FirebaseAuth mAuth;
    private static ValueEventListener myListener;
    ArrayList<Group> groups;


    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = FirebaseDatabase.getInstance();
        myRefGroups = db.getReference().child("Groups");
        mAuth = FirebaseAuth.getInstance();

        myListener = new ValueEventListener() {
            @SuppressLint("NewApi")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Group>> t = new GenericTypeIndicator<ArrayList<Group>>() {
                };
                boolean result = false;
                Group group = null;

                if (snapshot.getValue(t) != null) {
                    ArrayList<Group> list = new ArrayList<>(snapshot.getValue(t));
                    if (NotificationService.this.groups != null) {
                        for (Group g : list) {
                            if (g.getGroupMembers() != null && g.getGroupMembers().size() > 0)
                                result = g.getGroupMembers().stream().anyMatch(u -> u.getUserId().equals(intent.getStringExtra("UID")));
                            if (result && g.getAssignments() != null) // checks if user is a member of group
                            {
                                for (String s : g.getAssignments().keySet()) //goes over all assignments
                                {
                                    group = NotificationService.this.groups.stream().filter(grp -> grp.getGroupId().equals(g.getGroupId())).findFirst().orElse(null);
                                    if (group != null && group.getAssignments() != null && !group.getAssignments().containsKey(s))//checks if new assignment added
                                        sendNotification(g.getGroupName(), s);
                                }
                            }
                        }
                    }
                    NotificationService.this.groups = list;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        myRefGroups.addValueEventListener(myListener);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * This function receives a group name and an assignment and notifies the user when a new assignment has been uploaded
     * @param groupName The group name
     * @param assignmentName The assignment name
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification(String groupName, String assignmentName) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(groupName + " New Assignment")
                .setContentText(assignmentName)
                .setAutoCancel(true);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, LoginActivity.class), PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(contentIntent);

        notificationManager.notify(notificationId, builder.build());
        notificationId++;
    }

    /**
     * This function turns of the notification service by terminating the listener for the database
     */
    public static void terminateNotification()
    {
        if(myListener != null)
            myRefGroups.removeEventListener(myListener);

    }
}