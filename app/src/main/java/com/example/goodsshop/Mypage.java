package com.example.goodsshop;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class Mypage extends Fragment {

    View v;
    ProgressBar myPageProgressBar;
    ImageView personImgView;
    TextView shopNameTextView;
    Button profileBtn;
    ConstraintLayout saleCLayout, buyCLayout, jjimCLayout;
    ListView mypagelist;

    SharedPreferences sharedPreferences;
    boolean isUser;
    String getUid, getShopName,getShopImgUrl, getRBarScore;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_mypage, container, false);

        sharedPreferences = v.getContext().getSharedPreferences("getUid", MODE_PRIVATE);
        sharedPreferences = v.getContext().getSharedPreferences("getShopName", MODE_PRIVATE);
        sharedPreferences = v.getContext().getSharedPreferences("getShopImgUrl", MODE_PRIVATE);
        sharedPreferences = v.getContext().getSharedPreferences("getRBarScore", MODE_PRIVATE);

        getRBarScore = sharedPreferences.getString("getRBarScore", null);

        personImgView = v.findViewById(R.id.personImgView);
        myPageProgressBar = v.findViewById(R.id.myPageProgressBar);
        checkUid();

        profileBtn = v.findViewById(R.id.profilebtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SeeOtherShop.class);
                intent.putExtra("getUid", getUid);
                intent.putExtra("otherShopName", getShopName);
                intent.putExtra("profileImgUrl", getShopImgUrl);
                intent.putExtra("otherRBar", Float.parseFloat(getRBarScore));

                startActivity(intent);
            }
        });  //??????????????? ?????? ?????? ????????? ???????????? ??????

        saleCLayout = v.findViewById(R.id.saleConstraintLayout);
        saleCLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Sales.class);
                startActivity(intent);
            }
        });  //?????????????????? ??????

        buyCLayout = v.findViewById(R.id.buyConstraintLayout);
        buyCLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Purchase.class);
                startActivity(intent);
            }
        });  //?????????????????? ??????

        jjimCLayout = v.findViewById(R.id.jjimConstraintLayout);
        jjimCLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Jjim.class);
                startActivity(intent);
            }
        });  //?????????????????? ?????? == ???

        String[] list = {"????????? ??????", "????????????", "??????"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list);

        mypagelist = (ListView) v.findViewById(R.id.mypagelist);
        mypagelist.setAdapter(adapter);

        mypagelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                AlertDialog.Builder dlg = new AlertDialog.Builder(v.getContext());

                switch (position){
                    case 0:
                        intent = new Intent(getActivity(), SetProfile.class);
                        intent.putExtra("getShopName", getShopName);
                        intent.putExtra("getShopImgUrl", getShopImgUrl);
                        intent.putExtra("otherRBar", Float.parseFloat(getRBarScore));
                        startActivity(intent);
                        break;
                    case 1:
                        dlg.setMessage("???????????? ???????????????????");
                        dlg.setNegativeButton("??????", null);
                        dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                signOut();
                            }
                        });
                        dlg.show();

                        break;
                    case 2:
                        dlg.setMessage("???????????? ???????????????????");
                        dlg.setNegativeButton("??????", null);
                        dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAccount();
                            }
                        });
                        dlg.show();
                        break;
                }
            }
        });

        return v;
    }

    // ????????? ???????????? ??????
    private void checkUid(){
        getUid = sharedPreferences.getString("getUid", null);

        if(getUid != null) {  //uid??? ????????? (???????????? ???????????????)
            isUser = true;
            setProfileImg(personImgView);
            setShopName();
        }
        else {  //????????? ????????? ???????????? ????????? ???????????? ??????
            MypageLogin mypageLogin = new MypageLogin();

            Bundle bundle = new Bundle();
            bundle.putString("whatFragment", "Mypage");
            mypageLogin.setArguments(bundle);

            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                    .beginTransaction().replace(R.id.mypageCLayout, mypageLogin).commit();
        }
    }

    // ??? ?????? ????????????
    private void setShopName(){
        getShopName = sharedPreferences.getString("getShopName", null);

        if (getShopName != null) {
            shopNameTextView = v.findViewById(R.id.shopNameTextView);
            shopNameTextView.setText("");

            shopNameTextView.setText(getShopName);
        }
    }

    // ?????? ?????? ???????????? ???????????? ??????
    private void setProfileImg(ImageView img){
        getShopImgUrl = sharedPreferences.getString("getShopImgUrl", null);

        if (getShopImgUrl != null){ // ????????? ????????? ??????????????? ?????????????????? ???????????? ?????????
            Glide.with(v).load(getShopImgUrl).circleCrop().placeholder(R.drawable.ic_baseline_person_24).into(img);
        }
        myPageProgressBar.setVisibility(View.GONE);  //?????????????????? ?????????
    }

    //  ???????????? ??? ???????????????->??????????????? ?????? ??????
    private void signOut(){
        AuthUI.getInstance().signOut(v.getContext())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FirebaseAuth.getInstance().signOut();
                            sharedPreferences.edit().clear().apply();

                            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                    .beginTransaction().replace(R.id.mypageCLayout, new MypageLogin()).commit();
                        }
                    }
                });
    }

    // ?????? ??????
    private void deleteAccount(){
        // ???????????? ?????? ??????
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("users").child(getUid).child("profileImg");
        storageRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                for (StorageReference item : Objects.requireNonNull(task.getResult()).getItems()) {
                    item.delete();

                }
            }
        });

        storageRef = FirebaseStorage.getInstance().getReference().child("regiGoods");
        storageRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                for (StorageReference item : Objects.requireNonNull(task.getResult()).getItems()) {
                    if (item.getName().contains(getUid)){
                        item.delete();
                    }
                }
            }
        });

        storageRef = FirebaseStorage.getInstance().getReference().child("reviews");
        storageRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                for (StorageReference item : Objects.requireNonNull(task.getResult()).getItems()) {
                    if (item.getName().contains(getUid)){
                        item.delete();
                    }
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Auth ????????? ???????????? ????????????????????? ?????? ??????
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        databaseReference.child("users").child(getUid).removeValue();
                        databaseReference.child("regiGoods").child(getUid).removeValue();
                        databaseReference.child("reviews").child(getUid).removeValue();
                        databaseReference.child("purchase").child(getUid).removeValue();
                        databaseReference.child("notifications").child(getUid).removeValue();

                        sharedPreferences.edit().clear().apply();

                        Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                .beginTransaction().replace(R.id.mypageCLayout, new MypageLogin()).commit();
                    }
                });
            }
        }, 1000);
    }
}