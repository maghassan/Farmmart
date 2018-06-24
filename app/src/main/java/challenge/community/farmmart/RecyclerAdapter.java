package challenge.community.farmmart;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    public List<DataModel> blog_list;
    public List<User> user_list;

    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public RecyclerAdapter(List<DataModel> blog_list, List<User> user_list) {

        this.blog_list = blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String post_Title = blog_list.get(position).getPost_title();
        holder.setITitle(post_Title);

        String postPrice = blog_list.get(position).getPost_price();
        holder.setPrice(postPrice);

        String post_Content = blog_list.get(position).getPost_content();
        holder.setContent(post_Content);

        final String image_url = blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_url);

        final String blog_user_id = blog_list.get(position).getUser_id();

        /*if (blog_user_id.equals(currentUserId)) {

            holder.deletePost.setEnabled(true);
            holder.deletePost.setVisibility(View.VISIBLE);
        }*/

        String userName = user_list.get(position).getName();
        final String userImage = user_list.get(position).getImage();
        String userContact = user_list.get(position).getContact();
        String userAddress = user_list.get(position).getAddress();

        holder.setUserData(userName, userImage);

        /*try {
            long millisecond = blog_list.get(position).getTime_stamp().getTime();
            String dateString = android.text.format.DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {

            //Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }*/

        try {
            GetTimeAgo getTimeAgo = new GetTimeAgo();
            long postTime = blog_list.get(position).getTime_stamp().getTime();
            String timeAgo = getTimeAgo.getTimeAgo(postTime, context);
            holder.setTime(timeAgo);
        } catch (Exception e) {

            //Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {

                    int count = documentSnapshots.size();
                    holder.updateCommentCount(count);
                } else {

                    holder.updateCommentCount(0);
                }
            }
        });

        firebaseFirestore.collection("Posts/" + blogPostId + "/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {

                    int count = documentSnapshots.size();
                    holder.updateLikesCount(count);
                } else {

                    holder.updateLikesCount(0);
                }
            }
        });

        firebaseFirestore.collection("Posts/" + blogPostId + "/likes").document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (documentSnapshot.exists()) {

                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_red));
                        } else {

                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_grey));
                        }
                    }
                });

        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/likes").document(currentUserId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                if (!documentSnapshot.exists()) {

                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("likes_time", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection("Posts/" + blogPostId + "/likes").document(currentUserId).set(likesMap);
                                } else {

                                    firebaseFirestore.collection("Posts/" + blogPostId + "/likes").document(currentUserId).delete();
                                }
                            }
                        });
            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);
            }
        });

        holder.buyProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = holder.titleView.getText().toString();
                String price = holder.priceView.getText().toString();
                String postId = blog_user_id.toString();
                String contact = user_list.get(position).contact;

                Intent buyProducts = new Intent(context, BuyProducts.class);
                buyProducts.putExtra("blog_post_id", blogPostId);

                buyProducts.putExtra("title", title);
                buyProducts.putExtra("price", price);
                buyProducts.putExtra("postId", postId);
                buyProducts.putExtra("contact", contact);

                context.startActivity(buyProducts);
            }
        });

        /*holder.deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        blog_list.remove(position);
                        user_list.remove(position);

                        Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/

        holder.mainBlogCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = holder.titleView.getText().toString();
                String content = holder.contentView.getText().toString();
                //String time = holder.blogDate.getText().toString();
                String username = holder.blogUserName.getText().toString();
                String postId = blog_user_id.toString();

                String image = image_url.toString();
                String user_image = userImage.toString();

                Intent viewPost = new Intent(v.getContext(), ViewPost.class);

                viewPost.putExtra("title", title);
                viewPost.putExtra("content", content);
                //viewPost.putExtra("time", time);
                viewPost.putExtra("username", username);
                viewPost.putExtra("postId", postId);

                viewPost.putExtra("image", image);
                viewPost.putExtra("user_image", user_image);

                v.getContext().startActivity(viewPost);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        private TextView titleView;
        private TextView contentView;
        private TextView priceView;
        private ImageView blogPostImage;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        private ImageView blogCommentBtn;
        private TextView blogCommentCount;

        private ImageView buyProducts;

        private ImageView deletePost;

        private CardView mainBlogCard;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            blogPostImage = mView.findViewById(R.id.blogPostImage);
            blogLikeBtn = mView.findViewById(R.id.likeBtn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
            //deletePost = mView.findViewById(R.id.deletePost);

            buyProducts = mView.findViewById(R.id.buyProduct);

            mainBlogCard = mView.findViewById(R.id.mainBlogPost);
        }

        public void setITitle(String postTitle) {

            titleView = mView.findViewById(R.id.postTitle);
            titleView.setText(postTitle);
        }

        public void setContent(String postContent) {

            contentView = mView.findViewById(R.id.postContent);
            contentView.setText(postContent);
        }

        public void setPrice(String postPrice) {

            priceView = mView.findViewById(R.id.postPrice);
            priceView.setText(postPrice);
        }

        public void setBlogImage(String downloadUri){

            blogPostImage = mView.findViewById(R.id.blogPostImage);
            Glide.with(context).load(downloadUri).into(blogPostImage);
        }

        public void setTime(String date) {

            blogDate = mView.findViewById(R.id.postDate);
            blogDate.setText(date);
        }

        public void setUserData(String name, String image) {

            blogUserName = mView.findViewById(R.id.username);
            blogUserImage = mView.findViewById(R.id.profileImage);

            blogUserName.setText(name);

            RequestOptions requestOptions = new RequestOptions();
            //requestOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(image).into(blogUserImage);
        }

        public void updateLikesCount (int count){

            blogLikeCount = mView.findViewById(R.id.likeCount);
            blogLikeCount.setText(count + " Likes");
        }

        public void updateCommentCount (int countComment) {

            blogCommentCount = mView.findViewById(R.id.blog_comment_count);
            blogCommentCount.setText(countComment + " Comments");
        }
    }
}
