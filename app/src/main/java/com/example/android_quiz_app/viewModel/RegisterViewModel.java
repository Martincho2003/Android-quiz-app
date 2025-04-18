package com.example.android_quiz_app.viewModel;

import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.repository.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import java.io.IOException;

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
        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            registrationState.setValue(new RegistrationState(false, "Потребителското име е задължително"));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            registrationState.setValue(new RegistrationState(false, "Имейлът е задължителен"));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            registrationState.setValue(new RegistrationState(false, "Паролата е задължителна"));
            return;
        }
        if (password.length() < 8) {
            registrationState.setValue(new RegistrationState(false, "Паролата трябва да е поне 8 символа"));
            return;
        }
        if (!password.equals(confirmPassword)) {
            registrationState.setValue(new RegistrationState(false, "Паролите не съвпадат"));
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        User user = new User(username, 0, "1.1.1970", 0);
                        databaseReference.child(userId).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    registrationState.setValue(new RegistrationState(true, "Регистрацията е успешна"));
                                })
                                .addOnFailureListener(e -> {
                                    auth.getCurrentUser().delete()
                                            .addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    registrationState.setValue(new RegistrationState(false, "Регистрацията неуспешна, моля, опитайте отново"));
                                                } else {
                                                    registrationState.setValue(new RegistrationState(false, "Регистрацията неуспешна, моля, излезте и опитайте отново"));
                                                }
                                            });
                                });
                    } else {
                        String errorMessage = "Регистрацията неуспешна";
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                            if (task.getException() instanceof java.io.IOException) {
                                errorMessage = "Мрежова грешка (напр. reCAPTCHA неуспех). Моля, проверете интернет връзката си и опитайте отново.";
                            }
                        }
                        registrationState.setValue(new RegistrationState(false, errorMessage));
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Регистрацията неуспешна: " + e.getMessage();
                    if (e instanceof java.io.IOException) {
                        errorMessage = "Мрежова грешка (напр. reCAPTCHA неуспех). Моля, проверете интернет връзката си и опитайте отново.";
                    }
                    registrationState.setValue(new RegistrationState(false, errorMessage));
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