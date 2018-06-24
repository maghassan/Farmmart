package challenge.community.farmmart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    private TextView postBtn;

    private EditText postTitle, postContent, postPrice;

    private ImageView postImage;

    private static final int MAX_LENGTH = 100;
    private Toolbar newPostToolbar;

    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private Uri postImageUri = null;

    private ProgressBar postProgress;

    private Bitmap compressor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postBtn = findViewById(R.id.postButton);
        postTitle = findViewById(R.id.postTitle);
        postContent = findViewById(R.id.postContent);
        postPrice = findViewById(R.id.postPrice);
        postImage = findViewById(R.id.postImage);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        postProgress = findViewById(R.id.postProgress);
        postProgress.setVisibility(View.GONE);

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1,1)
                        .start(PostActivity.this);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String title = postTitle.getText().toString();
                final String content = postContent.getText().toString();
                final String price = postPrice.getText().toString();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content) && !TextUtils.isEmpty(price) && postImageUri != null) {

                    postProgress.setVisibility(View.VISIBLE);

                    //final String randomName = random();
                    final String randomName = UUID.randomUUID().toString();

                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");

                    filePath.putFile(postImageUri)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    final String downloadUri = task.getResult().getUploadSessionUri().toString();

                                    if (task.isSuccessful()) {

                                        File actualFile = new File(postImageUri.getPath());
                                        try {
                                            compressor = new Compressor(PostActivity.this)
                                                    .setMaxHeight(100)
                                                    .setMaxWidth(100)
                                                    .setQuality(50)
                                                    .compressToBitmap(actualFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        compressor.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                        byte[] thumbData = byteArrayOutputStream.toByteArray();

                                        UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();

                                                Map<String, Object> postMap = new HashMap<>();
                                                postMap.put("image_url", downloadUri);
                                                postMap.put("image_thumb", downloadthumbUri);
                                                postMap.put("post_title", title);
                                                postMap.put("post_content", content);
                                                postMap.put("post_price", "₦" + price);
                                                postMap.put("user_id", current_user_id);
                                                postMap.put("time_stamp", FieldValue.serverTimestamp());

                                                firebaseFirestore.collection("Posts").add(postMap)
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                                if (task.isSuccessful()) {

                                                                    Toast.makeText(PostActivity.this, "Posted Successfully", Toast.LENGTH_LONG).show();
                                                                    Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                                    startActivity(mainIntent);
                                                                    finish();
                                                                } else {

                                                                    postProgress.setVisibility(View.INVISIBLE);
                                                                }
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                                    } else {

                                        postProgress.setVisibility(View.INVISIBLE);

                                        String error = task.getException().getMessage();
                                        Toast.makeText(PostActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                postImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}
