package com.rifkifi.insta.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Fragment.PostDetailFragment;
import com.rifkifi.insta.Fragment.ProfileFragment;
import com.rifkifi.insta.Model.Notification;
import com.rifkifi.insta.Model.Post;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private Context context;

    public NotificationAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        final Notification notification = notificationList.get(position);

        holder.tv_comment.setText(notification.getText());
        Log.println(Log.INFO, "POSTID", notification.getPostid());

        getUserInfo(holder.iv_profileimage, holder.tv_username, notification.getUserid());

        if (notification.getIspost()){
            holder.iv_postimage.setVisibility(View.VISIBLE);
            getPostInfo(holder.iv_postimage, notification.getPostid());
        } else {
            holder.iv_postimage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notification.getIspost()){
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

                    editor.putString("postId", notification.getPostid());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main,
                            new PostDetailFragment()).commit();
                } else {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

                    editor.putString("profileId", notification.getUserid());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main,
                            new ProfileFragment()).commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_profileimage, iv_postimage;
        public TextView tv_username, tv_comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_profileimage = itemView.findViewById(R.id.iv_profileimage);
            iv_postimage = itemView.findViewById(R.id.iv_postimage);
            tv_username = itemView.findViewById(R.id.tv_username);
            tv_comment = itemView.findViewById(R.id.tv_comment);

        }
    }

    private void getUserInfo(final ImageView iv_profileimage, final TextView tv_username, String publisherId){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);
        dbUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                tv_username.setText(userObject.getUsername());
                Glide.with(context).load(userObject.getImageurl()).into(iv_profileimage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostInfo(final ImageView iv_postimage, String postId){
        DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        dbPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Log.println(Log.INFO, "POSTIMAGE", post.getPostimage());
                Glide.with(context).load(post.getPostimage()).into(iv_postimage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
