package com.example.android_quiz_app.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.service.FirebaseManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;

public class ProfileViewModel extends ViewModel {
    private DatabaseReference usersReference;
    private FirebaseAuth auth;
    private final MutableLiveData<ProfileState> profileState = new MutableLiveData<>();
    public ProfileViewModel() {
        auth = FirebaseAuth.getInstance();
        usersReference = FirebaseManager.getInstance().getUsersReference();
    }
    public LiveData<ProfileState> getProfileState() { return profileState; }

    public void getCurrentUserInfo() {
        if (auth.getCurrentUser() != null) {
            boolean isGoogleUser = false;
            for (UserInfo userInfo : auth.getCurrentUser().getProviderData()) {
                if (userInfo.getProviderId().equals("google.com")) {
                    isGoogleUser = true;
                    break;
                }
            }
            if (isGoogleUser) {
                usersReference.child(auth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().getValue(User.class);
                        profileState.setValue(new ProfileState(true, "Информацията за потребителя е заредена", user, true));
                    } else {
                        profileState.setValue(new ProfileState(false, "Неуспешно зареждане на информацията за потребителя: " + task.getException().getMessage(), null, true));
                    }
                });
            } else {
                usersReference.child(auth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().getValue(User.class);
                        profileState.setValue(new ProfileState(true, "Информацията за потребителя е заредена", user, false));
                    } else {
                        profileState.setValue(new ProfileState(false, "Неуспешно зареждане на информацията за потребителя: " + task.getException().getMessage(), null, false));
                    }
                });
            }
        }
    }

    public void logout(GoogleSignInClient googleSignInClient) {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
            profileState.setValue(new ProfileState(true, "Logged out successfully", null, false));
        } else {
            profileState.setValue(new ProfileState(false, "No user to log out", null, false));
        }
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        } else {
            profileState.setValue(new ProfileState(false, "Google sign in client is null", null, true));
        }
    }

    public static class ProfileState {
        private final boolean isSuccess;
        private final String message;
        private final User user;
        private final boolean isGoogleUser;

        public ProfileState(boolean isSuccess, String message, User user, boolean isGoogleUser) {
            this.isSuccess = isSuccess;
            this.message = message;
            this.user = user;
            this.isGoogleUser = isGoogleUser;
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
        public boolean isGoogleUser() { return isGoogleUser; }
    }
}
