package com.chowdhuryelab.greeneries;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private TextView fotgotTv, noAccountTv, iconText;
    private Button loginBtn;
    private ImageView iconShopIv;
    private TextInputEditText emailEt, passwordEt;

    private String email, password;

    private CheckBox checkBoxRememberMe;

    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    public static final String spEmail = "emailKey";
    public static final String spPass = "passKey";

    Animation leftAnimation;

    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login2);

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        emailEt = findViewById(R.id.emailET);
        passwordEt = findViewById(R.id.passwordET);
        fotgotTv = findViewById(R.id.forgotTV);
        noAccountTv = findViewById(R.id.noAccount);
        checkBoxRememberMe = findViewById(R.id.remember);
        iconText = findViewById(R.id.iconText);
        loginBtn = findViewById(R.id.loginBtn);
        //iconShopIv = findViewById(R.id.iconIv);
        leftAnimation = AnimationUtils.loadAnimation(this, R.anim.left_animation);

        iconText.setAnimation(leftAnimation);
        //iconShopIv.setAnimation(leftAnimation);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(spEmail)) {
            emailEt.setText(sharedpreferences.getString(spEmail, ""));
        }
        if (sharedpreferences.contains(spPass)) {
            passwordEt.setText(sharedpreferences.getString(spPass, ""));
        }



        noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterBuyerActivity.class));
                finish();
            }
        });
        fotgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        checkBoxRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

              @Override
              public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                  // use isChecked value here to react to state changes on your checkbox
                  shSave();
              }
          }
        );

    }

    public void shSave() {
        String e = emailEt.getText().toString();
        String p = passwordEt.getText().toString();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(spEmail, e);
        editor.putString(spPass, p);
        editor.commit();
    }

    private void loginUser() {
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter Password...", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog.setMessage("Logging In...");
        mProgressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        makeMeOnline();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeMeOnline() {
        mProgressDialog.setMessage("Check User...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            if(accountType.equals("Seller")){
                                mProgressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                finish();
                            }
                            else{
                                mProgressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainBuyerActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
