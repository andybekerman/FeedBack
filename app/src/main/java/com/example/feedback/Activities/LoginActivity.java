package com.example.feedback.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feedback.DataClasses.User;
import com.example.feedback.NotificationService;
import com.example.feedback.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * The login activity is the activity where the user preforms a login in order to get access to his account
 */

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase db;
    DatabaseReference myRefUsers;

    TextView tvSignUp, tvX, tvChangePassword; //tvX is a TextView that is used as a button to delete saved email during login
    Button btnLogin, btnSavedLogin;
    EditText etEmail, etPassword;

    ArrayList<User> users;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        myRefUsers = db.getReference().child("Users");

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnSavedLogin = findViewById(R.id.btnSavedLogin);
        tvX = findViewById(R.id.tvX);
        tvChangePassword = findViewById(R.id.tvChangePassword);

        users = new ArrayList<>();
        sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);

        //Checks if the user is already connected to app
        if (mAuth.getCurrentUser() != null) {
            firebaseUser = mAuth.getCurrentUser();
            checkForUserInfo();
        }

        //Checks if email is saved in shared preferences, if it is then it's displayed
        if (sharedPreferences.getString("EMAIL", "").equals("")) {
            btnSavedLogin.setVisibility(View.GONE);
            tvX.setVisibility(View.GONE);
        } else {
            btnSavedLogin.setText(sharedPreferences.getString("EMAIL", ""));
        }

        //If the X is pressed then the email button is erased and removed from the shared preferences storage
        tvX.setOnClickListener(v -> {
            btnSavedLogin.setVisibility(View.GONE);
            tvX.setVisibility(View.GONE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("EMAIL", "");
            editor.apply();
        });

        btnSavedLogin.setOnClickListener(this::inputEmail);
        btnLogin.setOnClickListener(this::logIn);
        tvSignUp.setOnClickListener(this::signUpPage);
        tvChangePassword.setOnClickListener(this::changePassword);
    }

    /**
     * When the email button is pressed then the email is inserted into the edit text and the email button is removed
     *
     * @param v The email button view
     */
    private void inputEmail(View v) {

        etEmail.setText(sharedPreferences.getString("EMAIL", ""));
        btnSavedLogin.setVisibility(View.GONE);
        tvX.setVisibility(View.GONE);
    }

    /**
     * When the Login button is pressed then the email and password are verified, if they pass then the CheckForUserInfo() function is called else a rejection message is displayed
     *
     * @param v The login button view
     */

    public void logIn(View v) {
        if (!etEmail.getText().toString().equals("") && !etPassword.getText().toString().equals("")) {
            mAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    firebaseUser = mAuth.getCurrentUser();
                    checkForUserInfo();
                } else {
                    Toast.makeText(LoginActivity.this, "The email or password were incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        } else
            Toast.makeText(this, "One or both fields are missing", Toast.LENGTH_LONG).show();
    }

    /**
     * This function takes the user to the main activity page once the user's data has been saved in the shared preferences storage
     */
    private void loginSuccessful() {
        //Starts notification service
        Intent serviceIntent = new Intent(LoginActivity.this, NotificationService.class);
        serviceIntent.putExtra("UID", firebaseUser.getUid());
        startService(serviceIntent);

        Intent intent = new Intent(this, GroupsActivity.class);
        startActivity(intent);
    }

    /**
     * This function checks to see if the user's information is saved in the shared preference storage, if not then the user is found in the database and the SetUserData(User user) function is called, if it is then the LoginSuccessful() function is called
     */
    private void checkForUserInfo() {

        if (sharedPreferences.getString("USERNAME", "").equals("")) {

            myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    GenericTypeIndicator<ArrayList<User>> t = new GenericTypeIndicator<ArrayList<User>>() {
                    };
                    if (snapshot.getValue(t) != null) {
                        users.addAll(snapshot.getValue(t));

                        for (User user : users) {

                            if (user.getUserId().equals(firebaseUser.getUid())) {

                                if (user.getUsername().equals("")) {

                                    final Dialog dialog = new Dialog(LoginActivity.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView(R.layout.dialog);

                                    final TextView tvField = dialog.findViewById(R.id.tvField);
                                    final EditText username = dialog.findViewById(R.id.etField1);
                                    final EditText hide1 = dialog.findViewById(R.id.etField2);
                                    final ListView hide2 = dialog.findViewById(R.id.listView);
                                    final Button save = dialog.findViewById(R.id.btnSave);

                                    hide1.setVisibility(View.GONE);
                                    hide2.setVisibility(View.GONE);

                                    tvField.setText("Enter a username: ");
                                    username.setHint("Username");

                                    dialog.show();

                                    save.setOnClickListener(v -> {
                                        final boolean result = users.stream().anyMatch(newUser -> newUser.getUsername().equals(username.getText().toString()));

                                        if (!result && !username.getText().toString().equals("")) {
                                            user.setUsername(username.getText().toString());
                                            dialog.dismiss();
                                            myRefUsers.setValue(users);
                                            setUserData(user);

                                        } else {
                                            Toast.makeText(LoginActivity.this, "Username taken", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    setUserData(user);
                                }
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println("No internet connection");
                }

            });
        } else {
            loginSuccessful();
        }
    }

    /**
     * When sign up button is pressed the user is sent to the sign up page
     *
     * @param v The sign up button view
     */
    public void signUpPage(View v) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, 0);
    }

    /**
     * This function automatically logs in a new user
     *
     * @param data        The user's login information
     * @param requestCode The numeric code of which page it was called on by
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (data != null && !data.getExtras().isEmpty()) {
                etEmail.setText(data.getStringExtra("Email"));
                etPassword.setText(data.getStringExtra("Password"));

                logIn(btnLogin);
            }
        }

    }


    /**
     * This function saves all the necessary user information in the shared preferences storage, then calls the LoginSuccessful() function
     *
     * @param user The user
     */
    private void setUserData(User user) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("EMAIL", firebaseUser.getEmail());
        editor.putString("UID", firebaseUser.getUid());
        editor.putString("USERNAME", user.getUsername());
        editor.putString("NAME", user.getFullName());

        editor.apply();

        loginSuccessful();
    }

    /**
     * The function is used when the user has forgotten his password, in this case the forgot password button is pressed, the user enters an email and the reset password link is sent to the email
     * @param view Reset Password Button
     */
    private void changePassword(View view) {
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);

        final TextView tvField = dialog.findViewById(R.id.tvField);
        final EditText email = dialog.findViewById(R.id.etField1);
        final EditText hide1 = dialog.findViewById(R.id.etField2);
        final ListView hide2 = dialog.findViewById(R.id.listView);
        final Button btnSend = dialog.findViewById(R.id.btnSave);

        hide1.setVisibility(View.GONE);
        hide2.setVisibility(View.GONE);

        tvField.setText("Enter email to reset password: ");
        email.setHint("Email");
        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        btnSend.setText("Send");

        dialog.show();

        btnSend.setOnClickListener(v -> {
            if(!email.getText().toString().equals("")){
                mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Email for resetting password sent", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else
                Toast.makeText(LoginActivity.this, "Email not entered", Toast.LENGTH_SHORT).show();
        });
    }
}