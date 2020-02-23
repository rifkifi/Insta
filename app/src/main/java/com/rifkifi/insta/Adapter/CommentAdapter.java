package com.rifkifi.insta.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.rifkifi.insta.Activity.MainActivity;
import com.rifkifi.insta.Model.CommentObject;
import com.rifkifi.insta.Model.UserObject;
import com.rifkifi.insta.R;

import org.w3c.dom.Comment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<CommentObject> commentList;
    private String postId;
    FirebaseUser loginUser;

    public CommentAdapter(Context context, List<CommentObject> commentList, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        loginUser = FirebaseAuth.getInstance().getCurrentUser();
        final CommentObject comment = commentList.get(position);

        holder.tv_comment.setText(comment.getComment());

        getUserInfo(holder.iv_profileimage, holder.tv_username, comment.getPublisher());

        holder.tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                context.startActivity(intent);
            }
        });

        holder.iv_profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comment.getPublisher().equals(loginUser.getUid())) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Do you want to delete?");
                    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase.getInstance().getReference("Comments")
                                            .child(postId).child(comment.getCommentId())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                return true;
            }

        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_profileimage;
        public TextView tv_username, tv_comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_profileimage = itemView.findViewById(R.id.iv_profileimage);
            tv_username = itemView.findViewById(R.id.tv_username);
            tv_comment = itemView.findViewById(R.id.tv_comment);
        }
    }

    public void getUserInfo(final ImageView iv_profileimage, final TextView tv_username, String publisherId){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);

        dbUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject userObject = dataSnapshot.getValue(UserObject.class);
                Glide.with(context).load(userObject.getImageurl()).into(iv_profileimage);
                tv_username.setText(userObject.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
