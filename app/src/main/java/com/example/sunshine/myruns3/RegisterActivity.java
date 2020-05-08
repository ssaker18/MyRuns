package com.example.sunshine.myruns3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.sunshine.myruns3.utils.Preferences;
import com.google.android.material.textfield.TextInputLayout;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 0;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int CAMERA_OPTION = 0;
    private static final int GALLERY_OPTION = 1;
    private static final String TAG = RegisterActivity.class.getName();
    public String SOURCE = "Source";
    public static String ACTIVITY_NAME = "RegisterActivity";
    private Preferences mSharedPreferences;
    private TextInputLayout mEdit_name_layout;
    private TextInputLayout mEmail_input_layout;
    private TextInputLayout mPassword_input_layout;
    private TextInputLayout mPhone_input_layout;
    private TextInputLayout mMajor_input_layout;
    private TextInputLayout mClass_input_layout;
    private RadioGroup mRadioGender;
    private ImageView mImageView;
    private String mPassword, mEmail, mName, mYearGroup, mPhone, mMajor;
    private int mGender;
    private File mPhotoFile;
    private Uri mImageCaptureUri;
    private Bitmap rotatedBitmap;
    private boolean isPhotoTakenFromCamera;
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    private Intent entryPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up action bar with back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // grab references tro different views
        mEdit_name_layout = findViewById(R.id.edit_name_layout);
        mEmail_input_layout = findViewById(R.id.email_input_layout);
        mPassword_input_layout = findViewById(R.id.password_input_layout);
        mPhone_input_layout = findViewById(R.id.phone_input_layout);
        mMajor_input_layout = findViewById(R.id.major_input_layout);
        mClass_input_layout = findViewById(R.id.class_input_layout);
        mRadioGender = findViewById(R.id.radioGender);
        mImageView = findViewById(R.id.register_profile_picture);

        // set up local storage of user info
        mSharedPreferences = new Preferences(this);

        if (savedInstanceState != null) {
            mImageCaptureUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);
            try {
                if (mImageCaptureUri != null) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            mImageCaptureUri);
                    mImageView.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check entry point used to start RegisterAcitivity
        entryPoint = getIntent();
        if (entryPoint != null) {
            String intentSource = entryPoint.getStringExtra("Source");
            if (intentSource != null){
                switch (intentSource) {
                    case "MainActivity":
                        getSupportActionBar().setTitle("Profile"); // reset Title
                        mEmail_input_layout.setEnabled(false); // disable email reset
                        populateProfile();
                        break;
                    case "LoginActivity":
                        // do nothing: new registration
                        break;
                }
            }

        }
    }

    /*
     * Save Profile Picture URI, to keep photo on system changes like rotation
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image capture uri before the activity goes into background
        outState.putParcelable(URI_INSTANCE_STATE_KEY, mImageCaptureUri);
    }

    /*
     * Callback for when  the user clicks change button
     * Checks user permissions
     * If granted, launches a dialog for user to opt for camera/gallery option
     */
    public void onChangeProfilePicture(View view) {
        checkPermissions();

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)) {
            launchProfilePicOptions();
        }
    }

    /*
     * Creates Dialog for Profile Pic options
     * 1. Camera  2. Gallery
     * Sets onClick Event Listener
     */
    private void launchProfilePicOptions() {
        // pop up the dialog
        AlertDialog.Builder pictureOptions = new AlertDialog.Builder(this);
        pictureOptions.setTitle("Camera Access");
        // Set up click listener, firing intents open camera
        DialogInterface.OnClickListener itemListener = (dialog, item) -> {
            // Item is ID_PHOTO_PICKER_FROM_CAMERA
            // Call the onPhotoPickerItemSelected in the parent
            // activity, i.e., CameraControlActivity in this case
            onPhotoPickerItemSelected(item);
        };
        // Set the item/s to display and create the dialog
        pictureOptions.setItems(R.array.ui_profile_photo_picker_items, itemListener);
        pictureOptions.show();
    }

    /*
     * Listens for Selected Profile Option
     * Starts appropriate implicit intent for camera or gallery
     * depending on option selected
     */
    private void onPhotoPickerItemSelected(int button) {
        switch (button) {
            case CAMERA_OPTION:
                // camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Construct temporary image path and name to save the taken
                // photo
                try {
                    mPhotoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                }
                if (mPhotoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID,
                            mPhotoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                }
                startActivityForResult(intent, REQUEST_CODE_CAMERA_PERMISSION);
                break;
            case GALLERY_OPTION:
                //gallery:
                try {
                    mPhotoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                }
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
        }
    }

    /*
     * Creates a temp file in standard pictures directory
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /*
     * Intents: Camera, Gallery, Crop
     * Handles these different intents by calling respective implementations of
     * each intent handler.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            // Camera Implementation
            case REQUEST_CODE_CAMERA_PERMISSION:
                isPhotoTakenFromCamera = true;
                Bitmap rotatedBitmap = imageOrientationValidator(mPhotoFile); // check orientation
                if (rotatedBitmap != null) {
                    try {
                        FileOutputStream fOut = new FileOutputStream(mPhotoFile);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    // Send image taken from camera for cropping
                    mImageCaptureUri = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID,
                            mPhotoFile);
                    beginCrop(mImageCaptureUri);
                }
                break;

            // Gallery implementation here
            case REQUEST_CODE_GALLERY:
                isPhotoTakenFromCamera = false;
                if (data != null){
                    beginCrop(data.getData());
                }
                break;

            //Crop implementation
            case Crop.REQUEST_CROP:
                // Update image view after image crop
                handleCrop(resultCode, data);
                // Delete temporary image taken by camera after crop.
                if (isPhotoTakenFromCamera) {
                    File f = new File(Objects.requireNonNull(mImageCaptureUri.getPath()));
                    if (f.exists()) {
                        f.delete();
                    }
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * Populates the register form with user data and profile pic (if it exists)
     */
    private void populateProfile() {
        mPassword_input_layout.getEditText().setText(mSharedPreferences.getPassWord());
        mEmail_input_layout.getEditText().setText(mSharedPreferences.getEmail());
        mEdit_name_layout.getEditText().setText(mSharedPreferences.getName());
        ((RadioButton) mRadioGender.getChildAt(mSharedPreferences.getGender())).setChecked(true);
        mPhone_input_layout.getEditText().setText(mSharedPreferences.getPhone());
        mMajor_input_layout.getEditText().setText(mSharedPreferences.getMajor());
        mClass_input_layout.getEditText().setText(mSharedPreferences.getYearGroup());

        // Load profile photo from internal storage
        try {
            FileInputStream fis = openFileInput(getString(R.string.profile_photo_file_name));
            Bitmap bmap = BitmapFactory.decodeStream(fis);
            mImageView.setImageBitmap(bmap);
            fis.close();
        } catch (IOException e) {
            // Default profile
        }
    }

    /*
     * Receives cropped image, and sets the ImageView
     */
    private void handleCrop(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = Crop.getOutput(data);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                mImageCaptureUri = uri;
                mImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Called after User takes a photo from either the camera
     * or the gallery. Fires of intent to crop which is handled by
     * handleCrop()
     */
    private void beginCrop(Uri source) {
        // pass URI as intent to the CROP Activity;
        if (mPhotoFile != null) {
            Uri destination = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID,
                    mPhotoFile);
            Log.d(TAG,  "URI: " + destination.toString());
            Crop.of(source, destination).asSquare().start(this);
        }
    }

    /*
     * Ensures image is adjusted appropriately and returns a bitMap
     * with correct orientation
     */
    private Bitmap imageOrientationValidator(File mPhotoFile) {
        if (mPhotoFile == null) {
            return null;
        }
        ExifInterface ei;

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID,
                            mPhotoFile));
            ei = new ExifInterface(mPhotoFile.getAbsolutePath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    /*
     * Rotates the Camera image depending on the current orientation
     */
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /*
     * Check if the user already granted permissions
     * Proceed if already granted else request for permissions
     * Permissions Requested: CAMERA and EXTERNAL STORAGE
     */
    private void checkPermissions() {
        // if permissions are not granted for camera, and external storage, request for them
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.
                    requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_CAMERA_PERMISSION);

        }
    }

    /*
     * Called when the user responds to permission request
     * On first denial, we show rationale dialog, and offer another chance
     * Proceed to request again if rationale is considered, else don't request again
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            // if permissions are granted, launch camera
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!

                launchProfilePicOptions();
                // set Boolean permission RESULT = true;
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED
                    || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                // permission denied, show rationale
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder rationale = new AlertDialog.Builder(this);
                    rationale.setMessage("We really would love to have you add a profile picture")
                            .setTitle("Camera Access");

                    rationale.setPositiveButton("OK", (dialog, which) -> ActivityCompat.
                            requestPermissions(RegisterActivity.this,
                                    new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_CAMERA_PERMISSION));

                    rationale.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    rationale.show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // handles cases when back button or register button is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.register_action:
                saveRegistration();
            case R.id.save_profile_action:
                saveRegistration();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*
     * Different Options depending on the entry point to the RegisterActivity.
     * if previous Activity was the SignIn Activity
     *    Inflate action bar with register option
     * else
     *    inflate with save option if previous Activity was MainActivity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (entryPoint != null) {
            switch (entryPoint.getStringExtra(SOURCE)) {
                case MainActivity.ACTIVITY_NAME:
                    getMenuInflater().inflate(R.menu.save_edit_profile, menu);
                    break;
                case LoginActivity.ACTIVITY_NAME:
                    getMenuInflater().inflate(R.menu.register, menu);
                    break;
            }

        }
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * Called when register button is clicked to register user and returns to LoginActivity
     * Called when save button is clicked to save edited profile and returns to MainActivity
     * The user is prompted to signIn if password is changed.
     */
    private void saveRegistration() {
        if (hasCorrectDetails()) {
            if (entryPoint != null) {
                switch (entryPoint.getStringExtra(SOURCE)) {
                    case MainActivity.ACTIVITY_NAME:
                        // Check any change in password? We know mPassword is never null since it's required
                        if (!mPassword.equals(mSharedPreferences.getPassWord())) {
                            saveUserData();
                            startActivity(new Intent(RegisterActivity.this,
                                    LoginActivity.class)
                                    .putExtra(SOURCE, RegisterActivity.ACTIVITY_NAME));
                            finish();
                        } else {
                            saveUserData();
                            finish();
                        }
                        break;
                    case LoginActivity.ACTIVITY_NAME:
                        saveUserData();
                        finish();
                        break;
                }
            }

        }
    }

    /*
     * Called after user has correctly entered profile details
     * First, clears Shared Preferences storage, no previous records kept
     * Saves userData, and stores profile (if it exists) in local storage with static filename
     */
    private void saveUserData() {
        mSharedPreferences.clearProfile();
        mSharedPreferences.setName(mName);
        mSharedPreferences.setEmail(mEmail);
        mSharedPreferences.setGender(mGender);
        mSharedPreferences.setPassWord(mPassword);
        mSharedPreferences.setYearGroup(mYearGroup);
        mSharedPreferences.setPhone(mPhone);
        mSharedPreferences.setMajor(mMajor);

        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.profile_photo_file_name), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (entryPoint.getStringExtra(SOURCE).equals(MainActivity.ACTIVITY_NAME)) {
            Toast.makeText(RegisterActivity.this,
                    R.string.edit_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this,
                    R.string.register_success, Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * Checks if registration details are okay
     * Required Fields: Name, Email, Password (6 or more characters)
     */
    private boolean hasCorrectDetails() {
        // set any error to null
        mEdit_name_layout.setError(null);
        mEmail_input_layout.setError(null);
        mPassword_input_layout.setError(null);

        View focusView = null;
        boolean cancel = false;

        // grab data from views
        mPassword = mPassword_input_layout.getEditText().getText().toString().trim();
        mEmail = mEmail_input_layout.getEditText().getText().toString().trim();
        mName = mEdit_name_layout.getEditText().getText().toString().trim();
        mGender = mRadioGender.indexOfChild(findViewById(mRadioGender.getCheckedRadioButtonId()));
        mPhone = mPhone_input_layout.getEditText().getText().toString().trim();
        mMajor = mMajor_input_layout.getEditText().getText().toString().trim();
        mYearGroup = mClass_input_layout.getEditText().getText().toString().trim();


        if (TextUtils.isEmpty(mName)) {
            mEdit_name_layout.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (!isPasswordValid(mPassword)) {
            focusView = mPassword_input_layout;
            focusView.requestFocus();
            cancel = true;
        }

        if (!isEmailValid(mEmail)) {
            focusView = mEdit_name_layout;
            cancel = true;
        }

        if (mGender < 0) {
            Toast.makeText(this, "Gender " + getString(R.string.error_field_required),
                    Toast.LENGTH_SHORT).show();
            focusView = mRadioGender;
            cancel = true;
        }
        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return false;
        } else {
            // all is good, return true
            return true;
        }

    }

    /*
     * Checks if email is valid:
     * A valid email is non-empty and matches email pattern
     */
    private boolean isEmailValid(String email) {
        boolean valid = true;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (TextUtils.isEmpty(email)) {
            mEmail_input_layout.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!email.matches(emailPattern)) {
            mEmail_input_layout.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        return valid;
    }

    /*
     * Checks if passowrd is valid:
     * A valid password is non-empty and has at least 6 characters
     */
    private boolean isPasswordValid(String password) {
        boolean valid = true;
        if (TextUtils.isEmpty(password)) {
            mPassword_input_layout.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (password.length() < 6) {
            mPassword_input_layout.setError(getString(R.string.error_password_too_short));
            valid = false;
        }
        return valid;
    }

}


