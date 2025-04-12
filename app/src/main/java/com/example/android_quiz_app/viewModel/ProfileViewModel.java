package com.example.android_quiz_app.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.repository.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class ProfileViewModel extends ViewModel {
    private DatabaseReference usersReference;
    private FirebaseAuth auth;
    private final MutableLiveData<ProfileState> profileState = new MutableLiveData<>();
    public ProfileViewModel() {
        auth = FirebaseAuth.getInstance();
        usersReference = FirebaseManager.getInstance().getUsersReference(); }
    public LiveData<ProfileState> getProfileState() { return profileState; }

    public void getCurrentUserInfo() {
        if (auth.getCurrentUser() != null) {
            usersReference.child(auth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    profileState.setValue(new ProfileState(true, "User info loaded", user));
                } else {
                    profileState.setValue(new ProfileState(false, "Failed to load user info: " + task.getException().getMessage(), null));
                }
            });
        }
    }

    public void logout() {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
            profileState.setValue(new ProfileState(true, "Logged out successfully", null));
        } else {
            profileState.setValue(new ProfileState(false, "No user to log out", null));
        }
    }


    public static class ProfileState {
        private final boolean isSuccess;
        private final String message;
        private final User user;

        public ProfileState(boolean isSuccess, String message, User user) {
            this.isSuccess = isSuccess;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }
    }
}
