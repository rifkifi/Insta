package com.rifkifi.insta.Repository;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.Utils.UserLoadListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class UserRepository {

    static UserRepository instance;
    private static Context mContext;
    private List<UserObject> userObjects = new ArrayList<>();

    static UserLoadListener userLoadListener;

    public static UserRepository getInstance(Context context){
        mContext = context;
        if (instance == null){
            instance = new UserRepository();
        }
        userLoadListener = (UserLoadListener) mContext;
        return instance;
    }

    public MutableLiveData<List<UserObject>> getUsers(){
        if (userObjects.size() == -1){
            loadUsers();
        }
        MutableLiveData<List<UserObject>> user = new MutableLiveData<>();
        user.setValue(userObjects);

        return user;
    }

    private void loadUsers() {
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users");
        dbUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserObject userObject = snapshot.getValue(UserObject.class);
                    userObjects.add(userObject);
                }
                userLoadListener.onUsersLoaded();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
