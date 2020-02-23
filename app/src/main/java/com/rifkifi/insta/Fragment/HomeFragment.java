package com.rifkifi.insta.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Adapter.PostAdapter;
import com.rifkifi.insta.Adapter.StoryAdapter;
import com.rifkifi.insta.Model.Post;
import com.rifkifi.insta.Model.Story;
import com.rifkifi.insta.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rv_home;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<String> followingList;

    private RecyclerView rv_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    ProgressBar progressBar;

    /*@Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("storyList", (ArrayList<? extends Parcelable>) storyList);
    }*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rv_home = view.findViewById(R.id.rv_home);
        rv_home.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rv_home.setLayoutManager(linearLayoutManager);

        progressBar = view.findViewById(R.id.progress_circular);

        postList = new ArrayList<>();
        followingList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        rv_home.setAdapter(postAdapter);

        rv_story = view.findViewById(R.id.rv_story);
        rv_story.setHasFixedSize(true);
        LinearLayoutManager storyLinearLayoutManager = new LinearLayoutManager(getContext()
                , RecyclerView.HORIZONTAL, false);

        rv_story.setLayoutManager(storyLinearLayoutManager);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        rv_story.setAdapter(storyAdapter);

        checkFollowing();

        return view;
    }

    private void checkFollowing(){
        DatabaseReference dbFollow = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        dbFollow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                readPost();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPost() {
        DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference("Posts");

        dbPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    for (String id : followingList){
                        if (post.getPublisher().equals(id)){
                            postList.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStory(){
        DatabaseReference dbStory = FirebaseDatabase.getInstance().getReference("Story");
        dbStory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid(), 0,0));
                for (String id : followingList){
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot snapshot : dataSnapshot.child(id).getChildren()){
                        story = snapshot.getValue(Story.class);
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            countStory++;
                        }
                    }
                    if (countStory > 0) {
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
