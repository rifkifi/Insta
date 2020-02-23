package com.rifkifi.insta.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Adapter.CommentAdapter;
import com.rifkifi.insta.Model.CommentObject;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    RecyclerView rv_comments;
    CommentAdapter commentAdapter;
    List<CommentObject> commentList;

    EditText et_comment;
    ImageView iv_profileimage;
    TextView tv_post;

    FirebaseUser loginUser;
    String postId, publisherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loginUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();

        postId = intent.getStringExtra("postId");
        publisherId = intent.getStringExtra("publisherId");

        rv_comments = findViewById(R.id.rv_comments);
        rv_comments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv_comments.setLayoutManager(linearLayoutManager);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(CommentsActivity.this, commentList, postId);

        rv_comments.setAdapter(commentAdapter);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_comment = findViewById(R.id.et_comment);
        iv_profileimage = findViewById(R.id.iv_profileimage);
        tv_post = findViewById(R.id.tv_post);
        getProfileImage();


        readComments();

        tv_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_comment.getText().equals("")){
                    Toast.makeText(CommentsActivity.this, "Your comment is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        DatabaseReference dbComments = FirebaseDatabase.getInstance().getReference("Comments")
                .child(postId);

        String commentId = dbComments.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", et_comment.getText().toString());
        hashMap.put("publisher", loginUser.getUid());
        hashMap.put("commentid", commentId);

        dbComments.child(commentId).setValue(hashMap);
        addNotification();
        et_comment.setText("");
    }

    private void getProfileImage(){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users").child(loginUser.getUid());

        dbUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                Glide.with(CommentsActivity.this).load(userObject.getImageurl()).into(iv_profileimage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void readComments(){
        DatabaseReference dbComments = FirebaseDatabase.getInstance().getReference("Comments")
                .child(postId);

        dbComments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        CommentObject commentObject = snapshot.getValue(CommentObject.class);
                        commentList.add(commentObject);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(){
        DatabaseReference dbNotification = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", loginUser.getUid());
        hashMap.put("text", "Commented :" +et_comment.getText().toString());
        hashMap.put("postid", postId);
        hashMap.put("ispost", true);

        dbNotification.push().setValue(hashMap);
    }
}
