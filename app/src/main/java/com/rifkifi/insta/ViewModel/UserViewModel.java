package com.rifkifi.insta.ViewModel;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Fragment.SearchFragment;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.Repository.UserRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {

    private MutableLiveData<List<UserObject>> users;

    public void init (Context context){
        if (users != null){
            return;
        }

        users = UserRepository.getInstance(context).getUsers();

    }

    public LiveData<List<UserObject>> getUsers() {

        if (users == null) {
            users = new MutableLiveData<List<UserObject>>();
        }
        return users;
    }

    /*private void loadUsers() {

        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users");
        dbUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.postValue(null);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserObject userObject = snapshot.getValue(UserObject.class);
                    users.postValue(userObject);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }*/
}

