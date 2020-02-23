package com.rifkifi.insta.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import jp.shts.android.storiesprogressview.StoriesProgressView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Model.Story;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import java.util.ArrayList;
import java.util.List;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener{

    int counter = 0;
    long pressTime = 0l;
    long limit = 500l;

    LinearLayout ll_seen;
    TextView tv_number_viewer;
    ImageView iv_delete;

    StoriesProgressView storiesProgressView;
    ImageView iv_image, iv_profileimage;
    TextView tv_username;

    List<String> images, storyIds;
    String userId;

    FirebaseUser loginUser;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        loginUser = FirebaseAuth.getInstance().getCurrentUser();

        ll_seen = findViewById(R.id.ll_seen);
        tv_number_viewer = findViewById(R.id.tv_number_viewer);
        iv_delete = findViewById(R.id.iv_delete);

        storiesProgressView = findViewById(R.id.stories);
        iv_image = findViewById(R.id.iv_image);
        iv_profileimage = findViewById(R.id.iv_profileimage);
        tv_username = findViewById(R.id.tv_username);

        ll_seen.setVisibility(View.GONE);
        iv_delete.setVisibility(View.GONE);


        userId = getIntent().getStringExtra("userid");
        getStories(userId);
        userInfo(userId);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        if (userId.equals(loginUser.getUid())){
            ll_seen.setVisibility(View.VISIBLE);
            iv_delete.setVisibility(View.VISIBLE);
        }

        ll_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FollowersActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("storyid", storyIds.get(counter));
                intent.putExtra("title", "views");
                startActivity(intent);
            }
        });

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference dbStory = FirebaseDatabase.getInstance().getReference("Story")
                        .child(userId).child(storyIds.get(counter));

                dbStory.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(StoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    @Override
    public void onNext() {

        Glide.with(getApplicationContext()).load(images.get(++counter)).into(iv_image);
        addViewers(storyIds.get(counter));
        seenNumber(storyIds.get(counter));

    }

    @Override
    public void onPrev() {
        if ((counter-1) < 0){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }  else {
            Glide.with(getApplicationContext()).load(images.get(--counter)).into(iv_image);
            seenNumber(storyIds.get(counter));
        }
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    public void getStories(String userId){
        images = new ArrayList<>();
        storyIds = new ArrayList<>();

        DatabaseReference dbStory = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId);

        dbStory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        images.add(story.getImageurl());
                        storyIds.add(story.getStoryid());
                    }
                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(images.get(counter)).into(iv_image);

                addViewers(storyIds.get(counter));
                seenNumber(storyIds.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userInfo(String userId){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId);

        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                Glide.with(getApplicationContext()).load(userObject.getImageurl()).into(iv_profileimage);
                tv_username.setText(userObject.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addViewers(String storyId){
        FirebaseDatabase.getInstance().getReference("Story").child(userId)
                .child(storyId).child("views")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
    }

    private void seenNumber(String storyId){
        DatabaseReference dbStory = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId).child(storyId).child("views");

        dbStory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_number_viewer.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
