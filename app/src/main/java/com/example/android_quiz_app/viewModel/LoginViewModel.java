package com.example.android_quiz_app.viewModel;

import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.android_quiz_app.service.FirebaseManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class LoginViewModel extends ViewModel {

    private FirebaseAuth auth;

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private DatabaseReference usersReference;

    public LoginViewModel() {
        auth = FirebaseAuth.getInstance();
        usersReference = FirebaseManager.getInstance().getUsersReference();
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            loginState.setValue(new LoginState(false, "Email is required"));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            loginState.setValue(new LoginState(false, "Password is required"));
            return;
        }
        if (password.length() < 8) {
            loginState.setValue(new LoginState(false, "Password must be at least 8 characters"));
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginState.setValue(new LoginState(true, "Login successful"));
                    } else {
                        loginState.setValue(new LoginState(false, "Login failed: " + task.getException().getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    loginState.setValue(new LoginState(false, "Login failed: " + e.getMessage()));
                });
    }

    public void loginWithGoogle(String idToken) {
        if (TextUtils.isEmpty(idToken)) {
            loginState.setValue(new LoginState(false, "Google Sign-In failed: No token provided"));
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        usersReference.get().addOnCompleteListener(userTask -> {
                            if(userTask.isSuccessful()) {
                                String currentUid = auth.getCurrentUser().getUid();
                                HashMap<String, Object> users = (HashMap<String, Object>) userTask.getResult().getValue();
                                for (String uid : users.keySet()) {
                                    if (currentUid.equals(uid)) {
                                        loginState.setValue(new LoginState(true, "Google Sign-In successful"));
                                        return;
                                    }
                                }
                                loginState.setValue(new LoginState(true, "Google Sign-Up successful"));
                            }
                        });
                    } else {
                        loginState.setValue(new LoginState(false, "Google Sign-In failed: " + task.getException().getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    loginState.setValue(new LoginState(false, "Google Sign-In failed: " + e.getMessage()));
                });
    }

    public static class LoginState {
        private final boolean isSuccess;
        private final String message;

        public LoginState(boolean isSuccess, String message) {
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