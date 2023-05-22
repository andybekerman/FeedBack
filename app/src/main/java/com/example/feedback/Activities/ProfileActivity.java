package com.example.feedback.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feedback.DataClasses.User;
import com.example.feedback.NotificationService;
import com.example.feedback.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * The profile activity is the activity where a user is able to access his personal information and change it if wished
 */

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseUser currUser;
    FirebaseDatabase fb;
    DatabaseReference myRef;

    TextView tvUsername, tvEmail, tvName, tvChangePassword;
    Button btnSignOut;
    ArrayList<User> users;
    ImageView btnBack;

    SharedPreferences sharedPreferences;
    boolean result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();
        fb = FirebaseDatabase.getInstance();
        myRef = fb.getReference().child("Users");

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);
        tvChangePassword = findViewById(R.id.tvChangePassword);
        btnBack = findViewById(R.id.ivBack);

        sharedPreferences = this.getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        users = new ArrayList<>();
        result = true;

        btnSignOut = findViewById(R.id.btnSignOut);

        tvUsername.setText(sharedPreferences.getString("USERNAME", ""));
        tvEmail.setText(sharedPreferences.getString("EMAIL", ""));
        tvName.setText(sharedPreferences.getString("NAME", ""));

        btnBack.setOnClickListener(this::goBack);
        btnSignOut.setOnClickListener(this::signOut);
        tvEmail.setOnClickListener(this);
        tvUsername.setOnClickListener(this);
        tvChangePassword.setOnClickListener(this);
    }

    /**
     * The function redirects the user to the previous page when the back button is pressed
     *
     * @param view The back button view
     */
    private void goBack(View view) {
        finish();
    }


    /**
     * The function signs out the user from the app and from the firebase authentication and deletes all his information from the app
     *
     * @param view The sign out button view
     */
    private void signOut(View view) {

        mAuth.signOut();

        deleteUserInfo();

        NotificationService.terminateNotification();
        Intent serviceIntent = new Intent(ProfileActivity.this, NotificationService.class);
        stopService(serviceIntent);

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();

    }

    /**
     * The function deletes all the user's information from the SharedPreferences storage
     */
    private void deleteUserInfo() {

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("UID", "");
        editor.putString("USERNAME", "");
        editor.putString("NAME", "");
        editor.putString("GROUPS", "");
        editor.apply();
    }

    @Override
    public void onClick(View v) {

        //if user request username change then he inputs new username and it gets changed
        if (v == tvUsername) {
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    GenericTypeIndicator<ArrayList<User>> t = new GenericTypeIndicator<ArrayList<User>>() {
                    };
                    try {
                        if (snapshot.getValue(t) != null) {
                            users.addAll(snapshot.getValue(t));

                            for (User user : users) {
                                if (user.getUserId().equals(mAuth.getCurrentUser().getUid())) {

                                    final Dialog dialog = new Dialog(ProfileActivity.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView(R.layout.dialog);

                                    final TextView tvField = dialog.findViewById(R.id.tvField);
                                    final EditText username = dialog.findViewById(R.id.etField1);
                                    final EditText hide1 = dialog.findViewById(R.id.etField2);
                                    final ListView hide2 = dialog.findViewById(R.id.listView);
                                    final Button btnSave = dialog.findViewById(R.id.btnSave);

                                    hide1.setVisibility(View.GONE);
                                    hide2.setVisibility(View.GONE);

                                    username.setHint("Username");
                                    tvField.setText("Enter a username: ");

                                    dialog.show();

                                    btnSave.setOnClickListener(v1 -> {
                                        final boolean result = users.stream().anyMatch(newUser -> newUser.getUsername().equals(username.getText().toString()));

                                        if (!result && !username.getText().toString().equals("")) {
                                            user.setUsername(username.getText().toString());
                                            dialog.dismiss();
                                            myRef.setValue(users);
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Username taken", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    break;

                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println("No internet connection");
                }
            });
        }

        //if user requests email change then he inputs new email and it gets changed
        if (v == tvEmail) {
            final Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);

            final TextView tvField = dialog.findViewById(R.id.tvField);
            final EditText email = dialog.findViewById(R.id.etField1);
            final EditText hide1 = dialog.findViewById(R.id.etField2);
            final ListView hide2 = dialog.findViewById(R.id.listView);
            final Button btnSave = dialog.findViewById(R.id.btnSave);

            hide1.setVisibility(View.GONE);
            hide2.setVisibility(View.GONE);

            email.setHint("New Email");
            email.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            tvField.setText("Enter a new Email: ");

            dialog.show();

            btnSave.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {


                    mAuth.fetchSignInMethodsForEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.isSuccessful()) {
                                SignInMethodQueryResult result = task.getResult();
                                if (result != null && result.getSignInMethods() != null &&
                                        result.getSignInMethods().size() > 0) {

                                    ProfileActivity.this.result = true;

                                } else {
                                    ProfileActivity.this.result = false;
                                }
                            }
                        }
                    });


                    if (!result && !email.getText().toString().equals("")) {
                        currUser.updateEmail(email.getText().toString());
                        dialog.dismiss();
                        tvEmail.setText(email.getText().toString());
                    } else {
                        Toast.makeText(ProfileActivity.this, "Email in use", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        //if user request password change then an email is sent to change his password
        if (v == tvChangePassword) {
            mAuth.sendPasswordResetEmail(sharedPreferences.getString("EMAIL", "")).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Email for resetting password sent", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Not able to reset password at this time", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }
}