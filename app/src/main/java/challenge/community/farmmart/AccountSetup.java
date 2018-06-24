package challenge.community.farmmart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSetup extends AppCompatActivity {

    private EditText setupName, setupContact, setupAddress;
    private TextView setupBtn;

    private Toolbar setupToolbar;

    private CircleImageView setupImage;
    private Uri mainImageURI = null;
    private String user_id;

    private boolean isChanged = false;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProgressBar setupProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        setupProgress = findViewById(R.id.setupProgress);
        setupProgress.setVisibility(View.GONE);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setupToolbar = findViewById(R.id.account_setup_toobar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user_id = firebaseAuth.getCurrentUser().getUid();

        setupName = findViewById(R.id.setupName);
        setupContact = findViewById(R.id.setupContact);
        setupAddress = findViewById(R.id.setupAddress);
        setupBtn = findViewById(R.id.setBtn);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                String name = task.getResult().getString("name");
                                String image = task.getResult().getString("image");
                                String contact = task.getResult().getString("contact");
                                String address = task.getResult().getString("address");


                                mainImageURI = Uri.parse(image);

                                RequestOptions placeholderRequest = new RequestOptions();
                                //placeholderRequest.placeholder(R.drawable.profile);

                                setupName.setText(name);
                                setupContact.setText(contact);
                                setupAddress.setText(address);
                                Glide.with(AccountSetup.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                            }
                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(AccountSetup.this, "Error : " +error, Toast.LENGTH_LONG).show();
                        }

                        setupBtn.setEnabled(true);
                    }
                });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = setupName.getText().toString();
                final String contact = setupContact.getText().toString();
                final String address = setupAddress.getText().toString();

                if (!TextUtils.isEmpty(user_name) &&!TextUtils.isEmpty(contact) && !TextUtils.isEmpty(address) && mainImageURI != null) {

                    setupProgress.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        user_id = firebaseAuth.getCurrentUser().getUid();
                        //setupProgress.setVisibility(View.VISIBLE);

                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");

                        setupImage.setDrawingCacheEnabled(true);
                        setupImage.buildDrawingCache();
                        Bitmap bitmap = setupImage.getDrawingCache();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 15, byteArrayOutputStream);
                        byte[] data = byteArrayOutputStream.toByteArray();

                        UploadTask uploadTask = image_path.putBytes(data);

                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    storeFirestore(task, user_name, contact, address);
                                    //Toast.makeText(AccountSetup.this, "The Image is Uploaded", Toast.LENGTH_LONG).show();
                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(AccountSetup.this, "Error : " + error, Toast.LENGTH_LONG).show();
                                }
                                setupProgress.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {

                        storeFirestore(null, user_name, contact, address);
                    }
                }
            }
        });

        setupImage = findViewById(R.id.setupImage);
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(AccountSetup.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        //Toast.makeText(AccountSetup.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(AccountSetup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {

                        //Toast.makeText(AccountSetup.this, "Permission Granted", Toast.LENGTH_LONG).show();

                        selectImage();
                    }
                } else {

                    selectImage();
                }
            }
        });
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name, String contact, String address) {

        Uri downloadUri;

        if (task != null) {

            downloadUri = task.getResult().getDownloadUrl();
        } else {
            downloadUri = mainImageURI;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("image", downloadUri.toString());
        userMap.put("contact", contact);
        userMap.put("address", address);

        firebaseFirestore.collection("Users").document(user_id).set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(AccountSetup.this, "Success", Toast.LENGTH_LONG).show();
                            Intent mainIntent = new Intent(AccountSetup.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();

                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(AccountSetup.this, "Error : " +error, Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    private void selectImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(AccountSetup.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}
