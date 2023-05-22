package com.example.feedback.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feedback.DataClasses.User;
import com.example.feedback.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
 * The sign up activity in the activity where a user registers his information in order to create an account
 */

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRefUsers;

    TextView tvLogin;
    Button btnSignUp;
    EditText etEmail, etName, etPassword;

    User newUser;
    ArrayList<User> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRefUsers = database.getReference("Users");

        users = new ArrayList<User>();

        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);


        btnSignUp.setOnClickListener(this::registerUser);
        tvLogin.setOnClickListener(this::loginPage);

    }

    /**
     * When the sign-up button is pressed and the email and password field are filled, the user is registered into the Firebase database and then redirected back to the LoginActivity
     * @param v The sign-up button view
     */
    public void registerUser(View v) {
        if (!etEmail.getText().toString().isEmpty() && !etName.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty()) {
            mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                Toast.makeText(SignUpActivity.this, "Welcome " + etName.getText().toString(), Toast.LENGTH_SHORT).show();

                                myRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        GenericTypeIndicator<ArrayList<User>> t = new GenericTypeIndicator<ArrayList<User>>() {
                                        };

                                        if (snapshot.getValue(t) != null) {
                                            users.addAll(snapshot.getValue(t));
                                        }
                                        newUser = new User(etName.getText().toString(), user.getUid());
                                        users.add(newUser);
                                        myRefUsers.setValue(users);

                                        Intent intent = new Intent();
                                        intent.putExtra("Email", etEmail.getText().toString());
                                        intent.putExtra("Password", etPassword.getText().toString());
                                        setResult(0, intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        System.out.println("No internet connection");
                                    }
                                });


                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUpActivity.this, "Sign Up failed, email or password invalid", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    /**
     * When the login page button is pressed, the user is redirected back to the LoginActivity
     * @param v The login page button
     */
    public void loginPage(View v) {
        finish();
    }
}