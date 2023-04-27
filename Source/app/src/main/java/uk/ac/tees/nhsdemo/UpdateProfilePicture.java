package uk.ac.tees.nhsdemo;

import static com.google.common.io.Files.getFileExtension;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UpdateProfilePicture extends AppCompatActivity {
    private FirebaseUser firebaseUser;
    private FirebaseAuth profilePicAuth;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_picture);

        Button selectImageBtn = findViewById(R.id.select_pic_btn);
        Button uploadImageBtn = findViewById(R.id.upload_pic_btn);
        selectedImage = findViewById(R.id.profile_pic_upload);

        profilePicAuth = FirebaseAuth.getInstance();
        firebaseUser = profilePicAuth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        // Set User's current DP in ImageView (if image is already uploaded).
        // Will use Picasso to save and retrieve image file.
        Picasso.get().load(uri).into(selectedImage);

        // Upload image to firebase
        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture();
            }
        });


        // User select profile image
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });
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
                selectedImage.setImageURI(imageUri);
            }
        }

        private void uploadPicture() {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Uploading Image...");
            pd.show();
            if (imageUri != null) {
                StorageReference fileReference = storageReference.child(Objects.requireNonNull(profilePicAuth.getCurrentUser()).getUid() + "."
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
                                        firebaseUser = profilePicAuth.getCurrentUser();

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


            private String getFileExtension (Uri uri){
                ContentResolver cR = getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                return mime.getExtensionFromMimeType(cR.getType(uri));
            }
}