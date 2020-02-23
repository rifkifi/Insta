package com.rifkifi.insta.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    ImageView iv_close, iv_profileimage;
    TextView tv_save, tv_changeProfileImage;

    MaterialEditText et_fullname, et_username, et_bio;

    FirebaseUser loginUser;

    private Uri imageUri;
    private StorageTask uploadTask;
    StorageReference storageUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        iv_close = findViewById(R.id.iv_close);
        iv_profileimage = findViewById(R.id.iv_profileimage);
        tv_save = findViewById(R.id.tv_save);
        tv_changeProfileImage = findViewById(R.id.tv_changeProfileImage);
        et_fullname = findViewById(R.id.et_fullname);
        et_username = findViewById(R.id.et_username);
        et_bio = findViewById(R.id.et_bio);

        loginUser = FirebaseAuth.getInstance().getCurrentUser();
        storageUploads = FirebaseStorage.getInstance().getReference("uploads");

        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("Users").child(loginUser.getUid());

        dbUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                Glide.with(getApplicationContext()).load(userObject.getImageurl()).into(iv_profileimage);

                et_username.setText(userObject.getUsername());
                et_fullname.setText(userObject.getFullname());
                et_bio.setText(userObject.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this);
            }
        });

        iv_profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this);
            }
        });

        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(et_fullname.getText().toString()
                        , et_username.getText().toString()
                        , et_bio.getText().toString());

                finish();
            }
        });
    }

    private void updateProfile(String fullname, String username, String bio) {
        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("Users").child(loginUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("username", username);
        hashMap.put("fullname", fullname);
        hashMap.put("bio", bio);

        dbUsers.updateChildren(hashMap);

    }

    public String getFileExtention(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setMessage("Uploading");
        loading.show();

        if (imageUri != null){
            final StorageReference file = storageUploads.child(System.currentTimeMillis()
            +"."+getFileExtention(imageUri));

            uploadTask = file.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return file.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();

                        String myUrl  = downloadUri.toString();

                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users").child(loginUser.getUid());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl", ""+myUrl);

                        db.updateChildren(hashMap);
                        loading.dismiss();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            uploadImage();
        } else {
            Toast.makeText(this, "Something gone wrong", Toast.LENGTH_SHORT).show();
        }
    }
}
