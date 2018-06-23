package challenge.community.farmmart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ViewPost extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    public List<DataModel> blog_list;
    public List<User> user_list;

    private Toolbar viewPost_toolbar;

    TextView viewPost_name, viewPost_title, viewPost_content;
    ImageView viewPost_img, viewPost_user_img, postDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        viewPost_name = findViewById(R.id.viewPost_username);
        viewPost_title = findViewById(R.id.viewPost_title);
        viewPost_content = findViewById(R.id.viewPost_content);
        viewPost_img = findViewById(R.id.viewPost_image);
        viewPost_user_img = findViewById(R.id.viewPost_picture);

        viewPost_toolbar = findViewById(R.id.viewPost_toolbar);
        setSupportActionBar(viewPost_toolbar);
        getSupportActionBar().setTitle("Posted by: ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name = getIntent().getExtras().getString("username");
        viewPost_name.setText(name);
        String title = getIntent().getExtras().getString("title");
        viewPost_title.setText(title);
        String content = getIntent().getExtras().getString("content");
        viewPost_content.setText(content);
        String image = getIntent().getStringExtra("image");
        Uri uri = Uri.parse(image);
        Glide.with(getApplicationContext()).load(uri).into(viewPost_img);
        String picture = getIntent().getStringExtra("user_image");
        Uri pic = Uri.parse(picture);
        Glide.with(getApplicationContext()).load(pic).into(viewPost_user_img);

        final String userPostId = getIntent().getExtras().getString("postId");


        final String userId = firebaseAuth.getCurrentUser().getUid();

        postDelete = findViewById(R.id.postDelete);

        if (userPostId.equals(userId)) {

            postDelete.setEnabled(true);
            postDelete.setVisibility(View.VISIBLE);
        }

        postDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts").document(userPostId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                

                                startActivity(new Intent(ViewPost.this, MainActivity.class));
                                finish();
                                Toast.makeText(ViewPost.this, "Post Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
