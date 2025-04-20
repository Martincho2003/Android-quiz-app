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
            loginState.setValue(new LoginState(false, "Имейлът е задължителен"));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            loginState.setValue(new LoginState(false, "Паролата е задължителна"));
            return;
        }
        if (password.length() < 8) {
            loginState.setValue(new LoginState(false, "Паролата трябва да е поне 8 символа"));
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginState.setValue(new LoginState(true, "Успешно влязохте в приложението"));
                    } else {
                        loginState.setValue(new LoginState(false, "Неуспешен вход: " + task.getException().getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    loginState.setValue(new LoginState(false, "Неуспешен вход: " + e.getMessage()));
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
                                        loginState.setValue(new LoginState(true, "Успешно регистриране с Google"));
                                        return;
                                    }
                                }
                                loginState.setValue(new LoginState(true, "Успешен вход с Google"));
                            }
                        });
                    } else {
                        loginState.setValue(new LoginState(false, "Неуспешен вход с Google: " + task.getException().getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    loginState.setValue(new LoginState(false, "Неуспешен вход с Google: " + e.getMessage()));
                });
    }

    public void resetPassword(String email) {
        if (TextUtils.isEmpty(email)) {
            loginState.setValue(new LoginState(false, "Нужно е първо да въведете имейл"));
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginState.setValue(new LoginState(true, "Имейл за смяна на паролата е изпратен. Моля проверете и в СПАМ папката!"));
                    } else {
                        loginState.setValue(new LoginState(false, "Грешка при изпращане на имейл: " + task.getException().getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    loginState.setValue(new LoginState(false, "Грешка при изпращане на имейл: " + e.getMessage()));
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