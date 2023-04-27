package uk.ac.tees.nhsdemo;

import static com.google.common.io.Files.getFileExtension;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    private EditText fullName, email, password, conPassword, mobile, postcode, nhs_number, residential, dob;
    private ProgressBar progressBar;
    private RadioGroup registerGender;
    private RadioButton selectedGenderBtn;
    private DatePickerDialog picker;
    private String countrySelected;
    private Spinner nationalityS;
    private ArrayAdapter<CharSequence> nationalityAdapter;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    ImageView selectImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        Toast.makeText(RegisterActivity.this, "You can register now!", Toast.LENGTH_SHORT).show();

        auth = FirebaseAuth.getInstance();
        // Here we set up the login form
        fullName = findViewById(R.id.full_name);
        email = findViewById(R.id.email_address);
        password = findViewById(R.id.choose_password);
        conPassword = findViewById(R.id.confirm_password);
        mobile = findViewById(R.id.mobileNumber);
        postcode = findViewById(R.id.postcode);
        nhs_number = findViewById(R.id.nhsNumber);
        residential = findViewById(R.id.residential_address);
        dob = findViewById(R.id.date_of_birth);

        // Here we set up our image upload button
        selectImage = findViewById(R.id.profile_image_upload);
        Button uploadImageBtn = findViewById(R.id.select_image_button);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseUser = auth.getCurrentUser();


        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        // Set User's current DP in ImageView (if image is already uploaded).
        // Will use Picasso to save and retrieve image file.
        Glide.with(RegisterActivity.this)
                .load(uri)
                .circleCrop()
                .into(selectImage);


        // Select user profile image
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


        // Spinner initialization
        nationalityS = findViewById(R.id.nationality);

        // Populate ArrayAdapter using string array and a spinner layout that we will define
        nationalityAdapter = ArrayAdapter.createFromResource(this, R.array.array_countries, R.layout.spinner_layout);

        // Specify the layout to use when the list of choices appear
        nationalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner to populate the spinner
        nationalityS.setAdapter(nationalityAdapter);

        nationalityS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countrySelected = nationalityS.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Radio button for gender
        registerGender = findViewById(R.id.register_gender);
        registerGender.clearCheck();

        // Setting up a date picker on EditText for selecting user date of birth
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calender = Calendar.getInstance();
                int day = calender.get(Calendar.DAY_OF_MONTH);
                int month = calender.get(Calendar.MONTH);
                int year = calender.get(Calendar.YEAR);

                // Date picker dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dob.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button register_button = findViewById(R.id.registerButton);
        TextView loginNowBtn = findViewById(R.id.loginNow);

        loginNowBtn.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));


        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedGenderId = registerGender.getCheckedRadioButtonId();
                selectedGenderBtn = findViewById(selectedGenderId);

                // Retrieve user input data into string variables
                String fullNameTxt = fullName.getText().toString().trim();
                String emailTxt = email.getText().toString().trim();
                String passwordTxt = password.getText().toString().trim();
                String conPasswordTxt = conPassword.getText().toString().trim();
                String mobileTxt = mobile.getText().toString().trim();
                String postcodeTxt = postcode.getText().toString().trim();
                String nhs_numberTxt = nhs_number.getText().toString().trim();
                String residentialTxt = residential.getText().toString().trim();
                String txtDoB = dob.getText().toString().trim();
                String txtGender; // Can't obtain value before verifying if any button was selected or not
                progressBar = findViewById(R.id.progressBar);
                String imageUri = selectImage.toString();

                // Validate Mobile Number using matcher and pattern (Regular Expression)

//                String mobileRegex = "^(\\+44\\s?\\d{10}|0044\\s?\\d{10}|0\\s?\\d{10})?$";
//                Matcher mobileMatcher;
//                Pattern mobilePattern = Pattern.compile(mobileRegex);
//                mobileMatcher = mobilePattern.matcher(mobileTxt);


                // checking if the user have filled all the required fields and in the correct manner
                if (TextUtils.isEmpty(fullNameTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter full name", Toast.LENGTH_SHORT).show();
                    fullName.setError("Full name is required");
                    fullName.requestFocus();
                } else if (TextUtils.isEmpty(emailTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                    email.setError("Email is required");
                    email.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    email.setError("Valid email is required");
                    email.requestFocus();
                } else if (TextUtils.isEmpty(passwordTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    password.setError("Password is required");
                    password.requestFocus();
                } else if (passwordTxt.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be more than 6 digits", Toast.LENGTH_SHORT).show();
                    password.setError("Password is too short");
                    password.requestFocus();
                } else if (TextUtils.isEmpty(conPasswordTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter confirm password", Toast.LENGTH_SHORT).show();
                    conPassword.setError("Confirm password required");
                    conPassword.requestFocus();
                } else if (!passwordTxt.equals(conPasswordTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter password as above", Toast.LENGTH_SHORT).show();
                    conPassword.setError("Confirm password");
                    conPassword.requestFocus();
                    // Clear entered password
                    password.clearComposingText();
                    conPassword.clearComposingText();
                } else if (TextUtils.isEmpty(mobileTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
                    mobile.setError("Mobile number is required");
                    mobile.requestFocus();
                } else if (mobileTxt.length() != 10) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    mobile.setError("Mobile number should be 10 digits");
                    mobile.requestFocus();
                } /*else if (!mobileMatcher.find()){
                    Toast.makeText(RegisterActivity.this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    mobile.setError("Mobile number is not valid");
                    mobile.requestFocus();
                }*/ else if (TextUtils.isEmpty(postcodeTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter postcode", Toast.LENGTH_SHORT).show();
                    postcode.setError("Postcode is required");
                    postcode.requestFocus();
                } else if (TextUtils.isEmpty(nhs_numberTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter NHS number", Toast.LENGTH_SHORT).show();
                    nhs_number.setError("NHS number is required");
                    nhs_number.requestFocus();
                } else if (TextUtils.isEmpty(residentialTxt)) {
                    Toast.makeText(RegisterActivity.this, "Please enter residential address", Toast.LENGTH_SHORT).show();
                    residential.setError("Residential address is required");
                    residential.requestFocus();
                } else if (TextUtils.isEmpty(txtDoB)) {
                    Toast.makeText(RegisterActivity.this, "Please enter date of birth", Toast.LENGTH_SHORT).show();
                    dob.setError("Date of birth is required");
                    dob.requestFocus();
                } else if (registerGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    selectedGenderBtn.setError("Gender is required");
                    selectedGenderBtn.requestFocus();
                } else {
                    txtGender = selectedGenderBtn.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(fullNameTxt, emailTxt, passwordTxt, mobileTxt, postcodeTxt, nhs_numberTxt, txtDoB, residentialTxt,
                            txtGender, imageUri);
                }
            }
        });
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(RegisterActivity.this);
        pd.setTitle("Uploading Image...");
        pd.show();
        if (imageUri != null) {

            StorageReference fileReference = storageReference.child(Objects.requireNonNull(auth.getCurrentUser()).getUid() + "."
                    + getFileExtension(imageUri));

            // Upload image to storage
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUri = uri;
                                    firebaseUser = auth.getCurrentUser();

                                    // Finally set the display image of the user after upload
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri).build();
                                    firebaseUser.updateProfile(profileUpdates);
                                }
                            });
                            Snackbar.make(findViewById(android.R.id.content), "Image Uploaded.", Snackbar.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to upload", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            pd.setMessage("Percentage: " + (int) progressPercent + "%");
                        }
                    });
        }
    }

    private void choosePicture () {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //noinspection deprecation
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(selectImage);
            //selectImage.setImageURI(imageUri);
        }
    }

    // Register user using the credentials given
    private void registerUser(String fullNameTxt, String emailTxt, String
            passwordTxt, String mobileTxt, String postcodeTxt,
                              String nhs_numberTxt, String txtDoB, String residentialTxt, String txtGender, String imageUri) {
        auth.createUserWithEmailAndPassword(emailTxt, passwordTxt).addOnCompleteListener(RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            // Here we read and write user details in Firebase Realtime Database
                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(fullNameTxt, emailTxt, mobileTxt,
                                    postcodeTxt, nhs_numberTxt, residentialTxt, txtDoB, txtGender, countrySelected, imageUri);

                            // Extracting and displaying user data reference from the Database for "Registered Users"
                            DatabaseReference profileReference = FirebaseDatabase.getInstance().getReference("Registered Users");

                            profileReference.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        // Send Verification Email to user
                                        firebaseUser.sendEmailVerification();

                                        Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify email", Toast.LENGTH_LONG).show();
                                        // Open user profile after each successful registration
                                        Intent intent = new Intent(RegisterActivity.this, UserActivity.class);

                                        // To prevent user from returning to register activity on pressing back button after registration
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "User registration failed. Please try again", Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
    }
    private String getFileExtension (Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}

