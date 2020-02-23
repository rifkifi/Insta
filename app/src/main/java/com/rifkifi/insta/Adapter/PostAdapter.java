package com.rifkifi.insta.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Activity.CommentsActivity;
import com.rifkifi.insta.Activity.FollowersActivity;
import com.rifkifi.insta.Fragment.PostDetailFragment;
import com.rifkifi.insta.Fragment.ProfileFragment;
import com.rifkifi.insta.Model.Post;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context context;
    public List<Post> post;

    public FirebaseUser firebaseUser;

    public PostAdapter(Context context, List<Post> post) {
        this.context = context;
        this.post = post;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Post postIndex = post.get(position);
        if (postIndex != null){
            Glide.with(context).load(postIndex.getPostimage())
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                    .into(holder.iv_post);
            if (postIndex.getDescription().equals("")){
                holder.tv_description.setVisibility(View.GONE);
            } else {
                holder.tv_description.setVisibility(View.VISIBLE);
                holder.tv_description.setText(postIndex.getDescription());
                Toast.makeText(context, "postIndex.getDescription()", Toast.LENGTH_SHORT).show();
            }

            publisherInfo(holder.iv_userprofile, holder.tv_username, holder.tv_publisher, postIndex.getPublisher());
            isLiked(postIndex.getPostid(), holder.iv_like);
            nrLikes(holder.tv_likes, postIndex.getPostid());
            getComments(postIndex.getPostid(), holder.tv_comments);
            isSaved(postIndex.getPostid(), holder.iv_save);

            holder.iv_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.iv_like.getTag().equals("like")){
                        FirebaseDatabase.getInstance().getReference().child("Likes")
                                .child(postIndex.getPostid())
                                .child(firebaseUser.getUid()).setValue(true);
                        addNotification(postIndex.getPublisher(), postIndex.getPostid());
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("Likes")
                                .child(postIndex.getPostid())
                                .child(firebaseUser.getUid()).removeValue();
                    }
                }
            });

            holder.iv_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CommentsActivity.class);
                    intent.putExtra("postId", postIndex.getPostid());
                    intent.putExtra("publisherId", postIndex.getPublisher());
                    context.startActivity(intent);
                }
            });

            holder.tv_comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CommentsActivity.class);
                    intent.putExtra("postId", postIndex.getPostid());
                    intent.putExtra("publisherId", postIndex.getPublisher());
                    context.startActivity(intent);
                }
            });

            holder.iv_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.iv_save.getTag().equals("save")) {
                        FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid())
                                .child(postIndex.getPostid()).setValue(true);
                    } else {
                        FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid())
                                .child(postIndex.getPostid()).removeValue();
                    }
                }
            });

            holder.iv_userprofile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", postIndex.getPublisher());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_main, new ProfileFragment()).commit();
                }
            });

            holder.tv_username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", postIndex.getPublisher());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_main, new ProfileFragment()).commit();
                }
            });

            holder.tv_publisher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", postIndex.getPublisher());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_main, new ProfileFragment()).commit();
                }
            });

            holder.iv_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("postId", postIndex.getPostid());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_main, new PostDetailFragment(), "post").commit();
                }
            });

            holder.tv_likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, FollowersActivity.class);
                    intent.putExtra("id", postIndex.getPostid());
                    intent.putExtra("title", "Likes");
                    context.startActivity(intent);
                }
            });

            holder.iv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.edit:
                                    editPost(postIndex.getPostid());
                                    return true;
                                case R.id.delete:
                                    FirebaseDatabase.getInstance().getReference("Posts")
                                            .child(postIndex.getPostid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
                                                        SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                                                        editor.putString("profileId", firebaseUser.getUid());
                                                        editor.apply();

                                                        ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                                                                .replace(R.id.fragment_main, new ProfileFragment()).commit();

                                                    }
                                                }
                                            });
                                    return true;
                                case R.id.report:
                                    Toast.makeText(context, "Report clicked!", Toast.LENGTH_SHORT).show();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.inflate(R.menu.menu_post);
                    if (!postIndex.getPublisher().equals(firebaseUser.getUid())){
                        popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                    }
                    popupMenu.show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return post.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_userprofile, iv_post, iv_like, iv_comment, iv_save, iv_more;
        public TextView tv_username, tv_likes, tv_publisher, tv_description, tv_comments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_userprofile = itemView.findViewById(R.id.iv_userprofile);
            iv_post = itemView.findViewById(R.id.iv_post);
            iv_like = itemView.findViewById(R.id.iv_like);
            iv_comment = itemView.findViewById(R.id.iv_comment);
            iv_save = itemView.findViewById(R.id.iv_save);
            tv_username = itemView.findViewById(R.id.tv_username);
            tv_likes = itemView.findViewById(R.id.tv_likes);
            tv_publisher = itemView.findViewById(R.id.tv_publisher);
            tv_description = itemView.findViewById(R.id.tv_description);
            tv_comments = itemView.findViewById(R.id.tv_comments);
            iv_more = itemView.findViewById(R.id.iv_more);

        }
    }

    private void getComments(String postId, final TextView tv_comments){
        DatabaseReference dbComment = FirebaseDatabase.getInstance().getReference("Comments")
                .child(postId);

        dbComment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_comments.setText("View All "+dataSnapshot.getChildrenCount()+" Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void isLiked(String postId, final ImageView imageView){
        final FirebaseUser loginUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference dbLikes = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);

        dbLikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(loginUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void nrLikes(final TextView tv_likes, String postId){
        DatabaseReference dbLikes = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);

        dbLikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_likes.setText(dataSnapshot.getChildrenCount()+" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void publisherInfo(final ImageView iv_userprofile, final TextView tv_username, final TextView tv_publisher, final String userId){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                Glide.with(context).load(userObject.getImageurl()).into(iv_userprofile);
                tv_username.setText(userObject.getUsername());
                tv_publisher.setText(userObject.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isSaved(final String postId, final ImageView iv_save){
        FirebaseUser loginUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbSaves = FirebaseDatabase.getInstance().getReference("Saves")
                .child(loginUser.getUid());

        dbSaves.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).exists()) {
                    iv_save.setImageResource(R.drawable.ic_saved);
                    iv_save.setTag("saved");
                } else {
                    iv_save.setImageResource(R.drawable.ic_save);
                    iv_save.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(String userid, String postid){
        DatabaseReference dbNotification = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "Liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        dbNotification.push().setValue(hashMap);
    }

    private void editPost(final String postId){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Edit Post");

        final EditText editText  = new EditText(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(layoutParams);
        alertDialog.setView(editText);

        getText(postId, editText);

        alertDialog.setPositiveButton("Edit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("description", editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference("Posts")
                                .child(postId).updateChildren(hashMap);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void getText(String postId, final EditText editText){
        DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postId);

        dbPost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
