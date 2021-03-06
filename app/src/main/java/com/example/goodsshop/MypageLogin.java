package com.example.goodsshop;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ReportFragment;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MypageLogin extends Fragment {

    View v;
    ProgressBar myPageProgressBar;
    TextView joinTextView;
    TextView findPwTextView;
    Button loginGoogleBtn, loginTwitterBtn, loginFacebookBtn;

    SharedPreferences sharedPreferences;
    boolean isUser;

    FirebaseAuth mFirebaseAuth;
    DatabaseReference mDatabaseRef;
    EditText mEtEmail, mEtPwd;
    Button btn_login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_mypage_login, container, false);

        sharedPreferences = v.getContext().getSharedPreferences("getUid", MODE_PRIVATE);
        sharedPreferences = v.getContext().getSharedPreferences("getShopName", MODE_PRIVATE);
        sharedPreferences = v.getContext().getSharedPreferences("getShopImg", MODE_PRIVATE);

        myPageProgressBar = v.findViewById(R.id.myPageProgressBar);
        myPageProgressBar.bringToFront();

        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        loginGoogleBtn = v.findViewById(R.id.loginGoogleBtn);
        loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedProviders.clear();
                selectedProviders.add(new AuthUI.IdpConfig.GoogleBuilder().build());

                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(selectedProviders)
                        .setDefaultProvider(new AuthUI.IdpConfig.GoogleBuilder().build())
                        .build(), RC_SIGN_IN);
            }
        });  //?????? ????????? ?????? ?????? ???

        loginTwitterBtn = v.findViewById(R.id.loginTwitterBtn);
        loginTwitterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedProviders.clear();
                selectedProviders.add(new AuthUI.IdpConfig.TwitterBuilder().build());

                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(selectedProviders)
                        .setDefaultProvider(new AuthUI.IdpConfig.TwitterBuilder().build()).setIsSmartLockEnabled(true)
                        .build(), RC_SIGN_IN);
            }
        });  //????????? ????????? ?????? ?????? ???

        loginFacebookBtn = v.findViewById(R.id.loginFacebookBtn);
        loginFacebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedProviders.clear();
                selectedProviders.add(new AuthUI.IdpConfig.FacebookBuilder().build());

                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(selectedProviders)
                        .setDefaultProvider(new AuthUI.IdpConfig.FacebookBuilder().build()).setIsSmartLockEnabled(false)
                        .build(), RC_SIGN_IN);
            }
        });  //???????????? ????????? ?????? ?????? ???

        joinTextView = v.findViewById(R.id.joinTextView);
        joinTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Join.class);
                startActivity(intent);
            }
        });  //???????????? ?????? ?????? ???

        // ???????????? ?????? ???????????????
        btn_login = v.findViewById(R.id.loginEmailBtn);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedProviders.clear();
                selectedProviders.add(new AuthUI.IdpConfig.EmailBuilder().build());

                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(selectedProviders)
                        .setDefaultProvider(new AuthUI.IdpConfig.EmailBuilder().build()).setIsSmartLockEnabled(false)
                        .build(), RC_SIGN_IN);
            }
        });

        return v;
    }

    private static final int RC_SIGN_IN = 1000;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    // ????????? ??????????????? ??????
    public boolean chkUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            ((MainActivity) MainActivity.context).checkLogin();  //????????? ??????: mainActivity ??????????????? ????????? ?????? ??????
            String uid = user.getUid();

            databaseReference.child("users").child(uid).child("uid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userUid = snapshot.getValue(String.class);
                    if (userUid != null){   //????????? ????????? ???????????? ???????????? ?????? ?????????
                        isUser = true;

                        sharedPreferences.edit().putString("getUid", uid).apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return isUser;
    }

    String whatFragment = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_OK){
                myPageProgressBar.setVisibility(View.VISIBLE);  //?????????????????? ?????????
                chkUser();

                Bundle bundle = getArguments();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isUser){  //???????????? ?????? ??????
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            if(bundle != null){
                                whatFragment = bundle.getString("whatFragment");

                                if (whatFragment.equals("Chatting")){
                                    Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                            .beginTransaction().replace(R.id.loginCLayout, new Chatting()).commit();
                                }
                                else if (whatFragment.equals("Mypage")){
                                    Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                            .beginTransaction().replace(R.id.loginCLayout, new Mypage()).commit();
                                }
                                else if (whatFragment.equals("Regi")){
                                    startActivity(intent);
                                }
                                else{
                                    startActivity(intent);
                                }
                            }else{
                                Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                        .beginTransaction().replace(R.id.loginCLayout, new Mypage()).commit();
                            }

                            myPageProgressBar.setVisibility(View.GONE);  //?????????????????? ?????????
                        }
                        else{  //????????? ???????????? ????????? ?????????????????? ??????
                            myPageProgressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(getActivity(), SetProfile.class);
                            startActivity(intent);
                        }
                    }
                },2000); //2000ms??? ???????????? ????????? ???????????? ??? ???????????? ????????? ????????? ?????? ????????????. ????????? ????????? ??????????

            }
        }
    }

}