package com.example.android_quiz_app.viewModel;

import android.text.TextUtils;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends ViewModel {

    private FirebaseAuth auth;

    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    public LoginViewModel() {
        auth = FirebaseAuth.getInstance();
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