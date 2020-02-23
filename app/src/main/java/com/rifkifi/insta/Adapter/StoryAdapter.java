package com.rifkifi.insta.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Activity.AddStoryActivity;
import com.rifkifi.insta.Activity.StoryActivity;
import com.rifkifi.insta.Model.Story;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context context;
    private List<Story> story;

    public StoryAdapter(Context context, List<Story> story) {
        this.context = context;
        this.story = story;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.add_story_item, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Story story1 = story.get(position);

        userInfo(holder, story1.getUserid(), position);

        if (holder.getAdapterPosition() != 0){
            seenStory(holder, story1.getUserid());
        }

        if (holder.getAdapterPosition() == 0) {
            myStory(holder.tv_add_story, holder.iv_add_story, holder.ic_add_story, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() == 0 ){
                    myStory(holder.tv_add_story, holder.iv_add_story, holder.ic_add_story, true);
                } else {
                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", story1.getUserid());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return story.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView iv_storyphoto, iv_storyphoto_seen, iv_add_story, ic_add_story;
        public TextView tv_usernamestory, tv_add_story;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_storyphoto = itemView.findViewById(R.id.iv_storyphoto);
            iv_storyphoto_seen = itemView.findViewById(R.id.iv_storyphoto_seen);
            iv_add_story = itemView.findViewById(R.id.iv_add_story);
            tv_usernamestory = itemView.findViewById(R.id.tv_usernamestory);
            tv_add_story = itemView.findViewById(R.id.tv_add_story);
            ic_add_story = itemView.findViewById(R.id.iv_plus);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return 0;
        }
        return 1;
    }

    private void userInfo(final ViewHolder viewHolder, String userId, final int pos){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId);
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                if (pos != 0) {
                    Glide.with(context).load(userObject.getImageurl()).into(viewHolder.iv_storyphoto);
                    Glide.with(context).load(userObject.getImageurl()).into(viewHolder.iv_storyphoto_seen);
                    viewHolder.tv_usernamestory.setText(userObject.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void myStory(final TextView textView, final ImageView iv_profile, final ImageView iv_plus, final boolean click){

        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        dbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                Glide.with(context).load(userObject.getImageurl()).into(iv_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DatabaseReference dbStory = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        dbStory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        count++;
                    }
                }

                if (click){
                    if (count > 0){
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story"
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, StoryActivity.class);
                                        intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, AddStoryActivity.class);
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.show();
                    } else {
                        Intent intent = new Intent(context, AddStoryActivity.class);
                        context.startActivity(intent);
                    }
                } else {
                    if (count > 0){
                        textView.setText("My Story");
                        iv_plus.setVisibility(View.GONE);
                    } else {
                        textView.setText("Add");
                        iv_plus.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(final ViewHolder viewHolder, String userId){
        DatabaseReference dbStory = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId);

        dbStory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (!snapshot.child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()){
                        i++;
                    }
                }
                if (i > 0) {
                    viewHolder.iv_storyphoto.setVisibility(View.VISIBLE);
                    viewHolder.iv_storyphoto_seen.setVisibility(View.GONE);
                } else {
                    viewHolder.iv_storyphoto.setVisibility(View.GONE);
                    viewHolder.iv_storyphoto_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
