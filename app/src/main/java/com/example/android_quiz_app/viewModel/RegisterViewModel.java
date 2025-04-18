package com.example.android_quiz_app.viewModel;

import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.service.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class RegisterViewModel extends ViewModel {
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private final MutableLiveData<RegistrationState> registrationState = new MutableLiveData<>();

    public RegisterViewModel() {
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseManager.getInstance().getUsersReference();
    }

    public LiveData<RegistrationState> getRegistrationState() {
        return registrationState;
    }

    public void register(String username, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            registrationState.setValue(new RegistrationState(false, "Username is required"));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            registrationState.setValue(new RegistrationState(false, "Email is required"));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            registrationState.setValue(new RegistrationState(false, "Password is required"));
            return;
        }
        if (password.length() < 8) {
            registrationState.setValue(new RegistrationState(false, "Password must be at least 8 characters"));
            return;
        }
        if (!password.equals(confirmPassword)) {
            registrationState.setValue(new RegistrationState(false, "Passwords do not match"));
            return;
        }



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
                    registrationState.setValue(new RegistrationState(false, "Username already exists, please choose another one"));
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                String userId = auth.getCurrentUser().getUid();
                                User user = new User(username, 0, "1.1.1970", 0);

                                databaseReference.child(userId).setValue(user)
                                        .addOnSuccessListener(aVoid -> {
                                            registrationState.setValue(new RegistrationState(true, "Registration successful"));
                                        })
                                        .addOnFailureListener(e -> {
                                            auth.getCurrentUser().delete()
                                                    .addOnCompleteListener(deleteTask -> {
                                                        if (deleteTask.isSuccessful()) {
                                                            registrationState.setValue(new RegistrationState(false, "Registration failed, please try again"));
                                                        } else {
                                                            registrationState.setValue(new RegistrationState(false, "Registration failed, please log out and try again"));
                                                        }
                                                    });
                                        });
                            } else {
                                registrationState.setValue(new RegistrationState(false, "Registration failed: " + task2.getException().getMessage()));
                            }
                        })
                        .addOnFailureListener(e -> {
                            registrationState.setValue(new RegistrationState(false, "Registration failed: " + e.getMessage()));
                        });

            } else {
                registrationState.setValue(new RegistrationState(false, "Failed to check username availability: " + task.getException().getMessage()));
            }
        });
    }

    public static class RegistrationState {
        private final boolean isSuccess;
        private final String message;

        public RegistrationState(boolean isSuccess, String message) {
            this.isSuccess = isSuccess;
            this.message = message;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }
    }
}