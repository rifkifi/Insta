package com.rifkifi.insta.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.rifkifi.insta.Activity.MainActivity;
import com.rifkifi.insta.Fragment.ProfileFragment;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<UserObject> user;
    private Boolean isFragment;
    private FirebaseUser firebaseUser;
    private AlertDialog alertDialog;

    public UserAdapter(Context context, List<UserObject> user, Boolean isFragment) {
        this.context = context;
        this.user = user;
        this.isFragment = isFragment;
    }


    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final UserObject userObject = user.get(position);

        holder.tv_username.setText(userObject.getUsername());
        holder.tv_fullName.setText(userObject.getFullname());
        Glide.with(context).load(userObject.getImageurl()).into(holder.image_profile);
        isFollowing(userObject.getId(), holder.btn_follow) ;

        if (userObject.getId().equals(firebaseUser.getUid())){
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFragment){

                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", userObject.getId());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_main, new ProfileFragment()).commit();
                } else {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("publisherId", userObject.getId());
                    context.startActivity(intent);
                }
            }
        });
        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btn_follow.getText().toString().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("following").child(userObject.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(userObject.getId()).child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotification(userObject.getId());
                } else {
                    unFollowAlert(userObject);
                }
            }
        });
    }

    private void unFollowAlert(final UserObject userObject) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle("Unfollow");
        // Icon Of Alert Dialog
        alertDialogBuilder.setMessage("Are you sure to unfollow this user?");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                FirebaseDatabase.getInstance().getReference().child("Follow")
                        .child(firebaseUser.getUid()).child("following").child(userObject.getId()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow")
                        .child(userObject.getId()).child("followers").child(firebaseUser.getUid()).removeValue();
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

    @Override
    public int getItemCount() {
        return user.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_username, tv_fullName;
        public CircleImageView image_profile;
        public Button btn_follow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_username = itemView.findViewById(R.id.tv_username);
            tv_fullName = itemView.findViewById(R.id.tv_fullName);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);

        }
    }

    public void isFollowing(final String userid, final Button button){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid()).child("following");

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
                    button.setText("Following");
                } else {
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(String userid){
        DatabaseReference dbNotification = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "Started following you");
        hashMap.put("userid", "");
        hashMap.put("ispost", false);

        dbNotification.push().setValue(hashMap);
    }
}
