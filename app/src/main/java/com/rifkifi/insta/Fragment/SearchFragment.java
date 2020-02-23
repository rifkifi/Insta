package com.rifkifi.insta.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rifkifi.insta.Adapter.UserAdapter;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;
import com.rifkifi.insta.Utils.UserLoadListener;
import com.rifkifi.insta.ViewModel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements UserLoadListener {
    List<UserObject> userList;
    RecyclerView rv_search;
    EditText et_search;
    UserAdapter userAdapter;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rv_search = view.findViewById(R.id.rv_search);
        et_search = view.findViewById(R.id.et_search);

        rv_search.setHasFixedSize(true);
        rv_search.setLayoutManager(new LinearLayoutManager(getContext()));

        userViewModel = ViewModelProviders.of(SearchFragment.this).get(UserViewModel.class);
        userViewModel.init(getContext());

        userList = new ArrayList<>();

        userAdapter = new UserAdapter(getContext(), userViewModel.getUsers().getValue(), true);
        rv_search.setAdapter(userAdapter);


        //readUsers();
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    public void searchUser(final String s){
        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("username").startAt(s).endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserObject userObject = snapshot.getValue(UserObject.class);
                    userList.add(userObject);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void readUsers(){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users");
        dbUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (et_search.getText().toString().equals("")){
                    userList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        UserObject userObject = snapshot.getValue(UserObject.class);
                        userList.add(userObject);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onUsersLoaded() {
        userViewModel.getUsers().observe(this, new Observer<List<UserObject>>() {
            @Override
            public void onChanged(List<UserObject> userObjects) {
                userAdapter.notifyDataSetChanged();
            }
        });
    }
}
