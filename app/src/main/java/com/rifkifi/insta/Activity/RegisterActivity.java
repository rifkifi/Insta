package com.rifkifi.insta.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rifkifi.insta.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_username, et_email, et_fullname, et_password, et_repassword;
    private TextView tv_login;
    private Button btn_register;

    FirebaseAuth auth;
    DatabaseReference db;

    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();

    }

    public void initialize(){
        et_email = findViewById(R.id.et_email);
        et_username = findViewById(R.id.et_username);
        et_fullname = findViewById(R.id.et_fullname);
        et_password = findViewById(R.id.et_password);
        et_repassword = findViewById(R.id.et_repassword);
        tv_login = findViewById(R.id.tv_login);
        btn_register = findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading = ProgressDialog.show(RegisterActivity.this, null, "Please wait...", true, false);

                String  email = et_email.getText().toString(),
                        fullName = et_fullname.getText().toString(),
                        username = et_username.getText().toString(),
                        password = et_password.getText().toString(),
                        repassword = et_repassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(fullName) || 
                        TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword)){
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                } else if (!password.equals(repassword)){
                    Toast.makeText(RegisterActivity.this, "Password didn't match!", Toast.LENGTH_SHORT).show();
                    et_password.setText(null);
                    et_repassword.setText(null);
                    loading.dismiss();
                } else {
                    register(username, fullName, email, password);
                    loading.dismiss();
                }
            }
        });
    }

    private void register(final String username, final String fullName, final String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser loginUser = auth.getCurrentUser();
                            assert loginUser != null;
                            db = FirebaseDatabase.getInstance().getReference().child("Users").child(loginUser.getUid());

                            HashMap<String, Object> userDetail = new HashMap<>();
                            userDetail.put("id", loginUser.getUid());
                            userDetail.put("username", username);
                            userDetail.put("fullname", fullName);
                            userDetail.put("bio", "");
                            userDetail.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/insta-e94b9.appspot.com/o/profile.png?alt=media&token=4d6ee89f-ccb0-4231-98e8-76e9a37fbb10");

                            db.setValue(userDetail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loading.dismiss();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });

                        } else {
                            loading.dismiss();
                            Toast.makeText(RegisterActivity.this, "You can't register with this email!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}


