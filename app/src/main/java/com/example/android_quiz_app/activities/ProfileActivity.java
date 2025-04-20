package com.example.android_quiz_app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.viewModel.ProfileViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, pointsTextView, lastDayPlayedTextView, playedGamesTodayTextView;
    private Button logoutButton, changePasswordButton;
    private ProfileViewModel viewModel;
    private GoogleSignInClient googleSignInClient;
    private ImageView profileImageView, addProfileImageButton, deleteProfileImageButton;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private String currentPhotoPath;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        usernameTextView = findViewById(R.id.usernameTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        lastDayPlayedTextView = findViewById(R.id.lastDayPlayedTextView);
        playedGamesTodayTextView = findViewById(R.id.playedGamesTodayTextView);
        logoutButton = findViewById(R.id.logoutButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        profileImageView = findViewById(R.id.profileImageView);
        deleteProfileImageButton = findViewById(R.id.deleteProfileImageButton);
        addProfileImageButton = findViewById(R.id.addProfileImageButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(String.valueOf(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    bitmap = fixOrientation(currentPhotoPath, bitmap);
                    profileImageView.setImageBitmap(bitmap);

                    savePhotoToStorage(bitmap);
                    deleteProfileImageButton.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Снимката е запазена", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Грешка при зареждане на снимката", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Снимането е отменено", Toast.LENGTH_SHORT).show();
            }
        });

        loadProfilePicture();

        addProfileImageButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent();
            } else {
                requestCameraPermission();
            }
        });

        deleteProfileImageButton.setOnClickListener(v -> deleteProfilePicture());

        viewModel.getProfileState().observe(this, state -> {
            if (state.isSuccess()) {
                updateProfileUI(state.getUser());
                if(state.getMessage().equals("Успешно излизане от профила")) {
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
            if(!state.isGoogleUser()) {
                changePasswordButton.setVisibility(View.VISIBLE);
            }
            Toast.makeText(ProfileActivity.this, state.getMessage(), Toast.LENGTH_LONG).show();
        });

        viewModel.getCurrentUserInfo();

        logoutButton.setOnClickListener(v -> {

            viewModel.logout(googleSignInClient);
        });

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void updateProfileUI(User user) {
        if (user != null) {
            usernameTextView.setText("Потребителско име: " + user.getUsername());
            pointsTextView.setText("Точки: " + user.getPoints());
            lastDayPlayedTextView.setText("Последна игра: " + (user.getLastDayPlayed() != null &&
                    !user.getLastDayPlayed().equals("1.1.1970") ? user.getLastDayPlayed() : "\nНе е играл/а никога"));
            playedGamesTodayTextView.setText("Брой игри за дадения ден: " + user.getPlayedGamesToday());
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Необходимо е разрешение за камерата", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Грешка при създаване на файла", Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android_quiz_app.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void savePhotoToStorage(Bitmap bitmap) throws IOException {
        String oldPath = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                .getString("profilePicturePath", null);
        if (oldPath != null) {
            File oldFile = new File(oldPath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        File file = new File(currentPhotoPath);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.flush();
        }

        getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                .edit()
                .putString("profilePicturePath", currentPhotoPath)
                .apply();
    }

    private void loadProfilePicture() {
        String path = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                .getString("profilePicturePath", null);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                bitmap = fixOrientation(path, bitmap);
                profileImageView.setImageBitmap(bitmap);
                deleteProfileImageButton.setVisibility(View.VISIBLE);
            } else {
                getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                        .edit()
                        .remove("profilePicturePath")
                        .apply();
                deleteProfileImageButton.setVisibility(View.GONE);
            }
        } else {
            deleteProfileImageButton.setVisibility(View.GONE);
        }
    }

    private void deleteProfilePicture() {
        String path = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                .getString("profilePicturePath", null);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
                    .edit()
                    .remove("profilePicturePath")
                    .apply();
            profileImageView.setImageResource(R.drawable.avatar);
            deleteProfileImageButton.setVisibility(View.GONE);
            Toast.makeText(this, "Снимката е изтрита", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap fixOrientation(String filePath, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}