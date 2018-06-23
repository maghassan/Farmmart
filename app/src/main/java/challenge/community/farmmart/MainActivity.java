package challenge.community.farmmart;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mainToolbar;

    private FloatingActionButton addPost;

    private FirebaseFirestore firebaseFirestore;

    private String current_user_id;
    private FirebaseAuth mAuth;

    private RecyclerView blog_list_view;
    private List<DataModel> blogPostList;
    private List<User> user_list;

    private FirebaseAuth firebaseAuth;
    private RecyclerAdapter blogRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("FarmMart");

        blog_list_view = findViewById(R.id.postList);

        blogPostList = new ArrayList<>();
        user_list = new ArrayList<>();

        blogRecyclerAdapter = new RecyclerAdapter(blogPostList, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        addPost = findViewById(R.id.addPostBtn);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent addPost = new Intent(MainActivity.this, PostActivity.class);
                startActivity(addPost);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {

            firebaseFirestore.collection("Posts")
                    .orderBy("time_stamp", Query.Direction.DESCENDING)
                    .limit(25)
                    .addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (!documentSnapshots.isEmpty()) {

                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String blogPostId = doc.getDocument().getId();
                                        final DataModel blogPost = doc.getDocument().toObject(DataModel.class).withId(blogPostId);

                                        String blogUserId = doc.getDocument().getString("user_id");
                                        firebaseFirestore.collection("Users").document(blogUserId).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {

                                                            User user = task.getResult().toObject(User.class);

                                                            user_list.add(user);
                                                            blogPostList.add(blogPost);

                                                            blogRecyclerAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });

        } else if (mAuth.getCurrentUser() == null) {

            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
    }


    public void onStart() {
        super.onStart();

        //sendToLogin();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {

            Intent loginIntent = new Intent(MainActivity.this, Login.class);
            startActivity(loginIntent);
            finish();
        } else {

            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {

                            Intent setupIntent = new Intent(MainActivity.this, AccountSetup.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    } else {

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
