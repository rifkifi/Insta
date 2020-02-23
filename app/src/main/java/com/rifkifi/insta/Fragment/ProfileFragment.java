package com.rifkifi.insta.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Activity.EditProfileActivity;
import com.rifkifi.insta.Activity.FollowersActivity;
import com.rifkifi.insta.Activity.OptionsActivity;
import com.rifkifi.insta.Adapter.PhotosAdapter;
import com.rifkifi.insta.Model.Post;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {
    ImageView   iv_profileimage, iv_options;
    TextView tv_post, tv_following, tv_followers, tv_fullname, tv_username, tv_bio;

    Button btn_editprofile;
    ImageButton ib_myphotos, ib_savedphotos;

    FirebaseUser loginUser;
    String profileId;
    private List<String> saveId;

    private AlertDialog alertDialog;

    PhotosAdapter photosAdapter;
    List<Post> photoList;
    RecyclerView rv_photos;

    PhotosAdapter savedPhotosAdapter;
    List<Post> savedPhotoList;
    RecyclerView rv_save;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        loginUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        profileId = sharedPreferences.getString("profileId", "none");

        iv_profileimage = view.findViewById(R.id.iv_profileimage);
        iv_options = view.findViewById(R.id.iv_options);
        tv_post = view.findViewById(R.id.tv_post);
        tv_following = view.findViewById(R.id.tv_following);
        tv_followers = view.findViewById(R.id.tv_followers);
        tv_fullname = view.findViewById(R.id.tv_fullName);
        btn_editprofile = view.findViewById(R.id.btn_editprofile);
        ib_myphotos = view.findViewById(R.id.ib_myfotos);
        ib_savedphotos = view.findViewById(R.id.ib_savedphotos);
        tv_username = view.findViewById(R.id.tv_username);
        tv_bio = view.findViewById(R.id.tv_bio);
        rv_photos = view.findViewById(R.id.rv_photos);
        rv_save = view.findViewById(R.id.rv_savedphotos);


        rv_photos.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        rv_photos.setLayoutManager(linearLayoutManager);

        photoList = new ArrayList<>();
        photosAdapter = new PhotosAdapter(getContext(), photoList);
        rv_photos.setAdapter(photosAdapter);

        rv_save.setHasFixedSize(true);
        LinearLayoutManager savedLinearLayoutManager = new GridLayoutManager(getContext(), 3);
        rv_save.setLayoutManager(savedLinearLayoutManager);

        savedPhotoList = new ArrayList<>();
        savedPhotosAdapter = new PhotosAdapter(getContext(), savedPhotoList);
        rv_save.setAdapter(savedPhotosAdapter);

        rv_photos.setVisibility(View.VISIBLE);
        rv_save.setVisibility(View.GONE);

        iv_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivity(intent);
            }
        });

        btn_editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = btn_editprofile.getText().toString();

                if (btn.equals("Edit Profile")){
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (btn.equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(loginUser.getUid()).child("following").child(profileId).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(profileId).child("followers").child(loginUser.getUid()).setValue(true);

                    addNotification();
                } else if (btn.equals("Following")){
                    unFollowAlert(profileId);
                }
            }
        });

        if (loginUser.getUid().equals(profileId)){
            btn_editprofile.setText("Edit Profile");
        } else {
            checkFollow();
            ib_savedphotos.setVisibility(View.GONE);
        }

        ib_myphotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_photos.setVisibility(View.VISIBLE);
                rv_save.setVisibility(View.GONE);
            }
        });
        ib_savedphotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_photos.setVisibility(View.GONE);
                rv_save.setVisibility(View.VISIBLE);
            }
        });

        userInfo();
        getFollowers();
        getFollowing();
        getNrPosts();
        getPhotos();
        getSaved();

        tv_followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });
        tv_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        return view;
    }

    private void unFollowAlert(final String profileId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle("Unfollow");
        // Icon Of Alert Dialog
        alertDialogBuilder.setMessage("Are you sure to unfollow this user?");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                FirebaseDatabase.getInstance().getReference().child("Follow")
                        .child(loginUser.getUid()).child("following").child(profileId).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow")
                        .child(profileId).child("followers").child(loginUser.getUid()).removeValue();
                alertDialog.dismiss();

            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void userInfo(){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users").child(profileId);
        dbUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }

                UserObject userObject = dataSnapshot.getValue(UserObject.class);

                Glide.with(getContext()).load(userObject.getImageurl()).into(iv_profileimage);
                tv_username.setText(userObject.getUsername());
                tv_fullname.setText(userObject.getFullname());
                tv_bio.setText(userObject.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollow(){
        DatabaseReference dbFollow = FirebaseDatabase.getInstance().getReference("Follow")
                .child(loginUser.getUid())
                .child("following");

        dbFollow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    btn_editprofile.setText("Following");
                } else {
                    btn_editprofile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFollowers(){
        DatabaseReference dbFollow = FirebaseDatabase.getInstance().getReference("Follow")
                .child(profileId)
                .child("followers");

        dbFollow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_followers.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFollowing(){
        DatabaseReference dbFollow = FirebaseDatabase.getInstance().getReference("Follow")
                .child(profileId)
                .child("following");

        dbFollow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_following.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNrPosts(){
        DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference("Posts");
        dbPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileId)){
                        i++;
                    }
                }

                tv_post.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getPhotos(){
        DatabaseReference dbPosts = FirebaseDatabase.getInstance().getReference("Posts");

        dbPosts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                photoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileId)){
                        photoList.add(post);
                        Log.println(Log.INFO, "PHOTO", "ADA");
                    }
                    Collections.reverse(photoList);
                    photosAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSaved(){
        saveId = new ArrayList<>();

        DatabaseReference dbSaves = FirebaseDatabase.getInstance().getReference("Saves")
                .child(loginUser.getUid());

        dbSaves.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    saveId.add(snapshot.getKey());
                }

                readSaved();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readSaved() {
        DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference("Posts");

        dbPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                savedPhotoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    for (String id : saveId){
                        if (post.getPostid().equals(id)){
                            savedPhotoList.add(post);
                        }
                    }
                }
                savedPhotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(){
        DatabaseReference dbNotification = FirebaseDatabase.getInstance().getReference("Notifications").child(profileId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", loginUser.getUid());
        hashMap.put("text", "Started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        dbNotification.push().setValue(hashMap);
    }


}
