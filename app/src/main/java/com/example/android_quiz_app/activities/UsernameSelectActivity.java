package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.service.FirebaseManager;
import com.example.android_quiz_app.viewModel.RegisterViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class UsernameSelectActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private Button submitUsernameButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_select);

        usernameEditText = findViewById(R.id.usernameEditText);
        submitUsernameButton = findViewById(R.id.submitUsernameButton);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseManager.getInstance().getUsersReference();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        submitUsernameButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                usernameEditText.setError("Username is required");
                return;
            }
            createUser(username);
        });
    }

    private void createUser(String username) {
        String userId = auth.getCurrentUser().getUid();
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                boolean usernameExists = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User existingUser = userSnapshot.getValue(User.class);
                    if (existingUser != null && username.equals(existingUser.getUsername())) {
                        usernameExists = true;
                        break;
                    }
                }
                if (usernameExists) {
                    Toast.makeText(this, "Username already exists, please choose another one", Toast.LENGTH_LONG).show();
                    usernameEditText.setText("");
                    submitUsernameButton.setEnabled(true);
                    return;
                }

                User user = new User(username, 0, "1.1.1970", 0);
                databaseReference.child(userId).setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User created successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(UsernameSelectActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            usernameEditText.setText("");
                            submitUsernameButton.setEnabled(true);
                        });
            } else {
                Toast.makeText(this, "Failed to check username: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                usernameEditText.setText("");
                submitUsernameButton.setEnabled(true);
            }
        });
    }
}